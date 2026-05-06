package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Parser.Expr;
import Parser.Stmt;
import Box.Token.Token;
import Box.Token.TokenType;

/**
 * Tests for PocketInstance and TkpInstance token acquisition behavior.
 *
 * These tests exercise the CAS acquire/release cycle, N-tick window,
 * per-pocket direction context capture, and BIN routing on contention.
 *
 * Threading model:
 *   - PocketInstance.startIndependent() starts a daemon tick thread.
 *   - The thread loops: CAS acquire interpreterToken → run N ticks → release in finally.
 *   - On CAS failure: log TokenContentionError message to BIN, increment consecutiveFailures.
 *   - Token is AtomicBoolean; true = available, false = held.
 *
 * Test strategy:
 *   - Hold interpreterToken externally (set false) to force CAS failure.
 *   - Use IMMORTAL policy to prevent starvation transitions during contention tests.
 *   - Use short sleep windows to observe thread effects without racing.
 */
public class PocketThreadTokenTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean cond) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else      { System.out.println("  FAIL  " + name); failed++; }
    }

    private static void eq(String name, Object expected, Object actual) {
        boolean ok = (expected == null) ? (actual == null) : expected.equals(actual);
        if (ok) { System.out.println("  PASS  " + name); passed++; }
        else    { System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual); failed++; }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Stmt.Expression controlStmt(Expr expr) {
        return new Stmt.Expression(expr, null);
    }

    private static Stmt.Expression pocketOpenStmt(String label) {
        Token ctrl = new Token(TokenType.OPENPAREN, label + "(", null, null, null, 0, 0, 0, 0);
        return controlStmt(new Expr.PocketOpen(ctrl));
    }

    private static Stmt.Expression pocketCloseStmt(String label) {
        Token ctrl = new Token(TokenType.CLOSEDPAREN, ")" + label, null, null, null, 0, 0, 0, 0);
        return controlStmt(new Expr.PocketClosed(ctrl));
    }

    private static Token nameToken(String name) {
        return new Token(TokenType.IDENTIFIER, name, null, null, null, 0, 0, 0, 0);
    }

    /**
     * Build a PocketInstance whose body is: [PocketOpen, Flow(chain=5), PocketClose].
     * The embedded Flow ensures isHeatDeath() returns false — the tick thread will spin.
     */
    private static PocketInstance spinningPocket(Interpreter interp) {
        List<Object> body = new ArrayList<>();
        body.add(pocketOpenStmt("t"));
        body.add(new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5));
        body.add(pocketCloseStmt("t"));
        return new PocketInstance(null, body, null, interp);
    }

    /**
     * Build a TkpInstance with the same layout: [PocketOpen, Flow(chain=5), PocketClose].
     * TkpInstance skips evaluateBody(), so the Flow stays in body as placed.
     */
    private static TkpInstance spinningTkp(Interpreter interp) {
        List<Object> body = new ArrayList<>();
        body.add(pocketOpenStmt("t"));
        body.add(new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5));
        body.add(pocketCloseStmt("t"));
        return new TkpInstance(null, body, null, interp);
    }

    private static Environment freshEnv(String name, Object value) {
        Environment env = new Environment();
        env.define(name, (Token) null, value);
        return env;
    }

    // -------------------------------------------------------------------------
    // CAS token: contention logged to BIN
    // -------------------------------------------------------------------------

    private static void testContentionLogsTobin() throws InterruptedException {
        System.out.println("--- CAS failure routes TokenContentionError message to BIN ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false); // hold token — CAS will always fail

        PocketInstance pkt = spinningPocket(interp);
        pkt.starvationPolicy = "IMMORTAL"; // never transition
        Token name = nameToken("p");
        pkt.startIndependent(freshEnv("p", pkt), name);

        Thread.sleep(80);
        pkt.beginDeath(); // stop tick thread before iterating BIN

        check("BIN has content", !new ArrayList<>(interp.bin.body).isEmpty());
        // At least one message has the contention prefix (BIN may have pre-loaded content)
        List<Object> snapshot = new ArrayList<>(interp.bin.body);
        boolean hasContention = false;
        for (Object o : snapshot) {
            if (o instanceof String && ((String) o).startsWith("Token contention [interpreter] pocket=p tick=")) {
                hasContention = true; break;
            }
        }
        check("BIN has at least one contention message with correct format", hasContention);
    }

    private static void testContentionMessageCount() throws InterruptedException {
        System.out.println("--- multiple failures accumulate in BIN ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        PocketInstance pkt = spinningPocket(interp);
        pkt.starvationPolicy = "IMMORTAL";
        pkt.startIndependent(freshEnv("p", pkt), nameToken("p"));

        Thread.sleep(80);
        pkt.beginDeath();

        List<Object> snapshot = new ArrayList<>(interp.bin.body);
        long contentionCount = snapshot.stream()
            .filter(o -> o instanceof String && ((String) o).startsWith("Token contention"))
            .count();
        check("at least 3 contention messages accumulated", contentionCount >= 3);
    }

    private static void testTkpContentionLogsTobin() throws InterruptedException {
        System.out.println("--- TkpInstance CAS failure also routes to BIN ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        TkpInstance tkp = spinningTkp(interp);
        tkp.starvationPolicy = "IMMORTAL";
        tkp.startIndependent(freshEnv("q", tkp), nameToken("q"));

        Thread.sleep(80);
        tkp.destroy(); // stop tick thread

        List<Object> snapshot = new ArrayList<>(interp.bin.body);
        boolean hasContention = false;
        for (Object o : snapshot) {
            if (o instanceof String && ((String) o).startsWith("Token contention [interpreter] pocket=q tick=")) {
                hasContention = true; break;
            }
        }
        check("BIN has tkp contention message with correct format", hasContention);
    }

    // -------------------------------------------------------------------------
    // Token release — token returns to true after window
    // -------------------------------------------------------------------------

    private static void testTokenReleasedAfterWindow() throws InterruptedException {
        System.out.println("--- token released in finally after window completes ---");
        Interpreter interp = new Interpreter();
        check("token starts available", interp.interpreterToken.get());

        PocketInstance pkt = spinningPocket(interp);
        pkt.starvationPolicy = "IMMORTAL";
        pkt.windowSize = 1;
        pkt.startIndependent(freshEnv("p", pkt), nameToken("p"));

        Thread.sleep(100);
        pkt.beginDeath();
        Thread.sleep(20); // let tick thread observe beginDeath and exit

        // After window: token must be true (finally block releases it)
        check("token available after window completes", interp.interpreterToken.get());
    }

    // -------------------------------------------------------------------------
    // Per-pocket context: pktForward and pktInvertedMode captured at start time
    // -------------------------------------------------------------------------

    private static void testContextCapturedForward() throws InterruptedException {
        System.out.println("--- pktForward captured as true when interpreter is forward ---");
        Interpreter interp = new Interpreter();
        interp.setForward(true);
        interp.interpreterToken.set(false);

        PocketInstance pkt = spinningPocket(interp);
        pkt.starvationPolicy = "IMMORTAL";
        Token name = nameToken("fwd");
        pkt.startIndependent(freshEnv("fwd", pkt), name);

        Thread.sleep(20);
        pkt.beginDeath();

        eq("pktForward = true", true, pkt.pktForward);
    }

    private static void testContextCapturedBackward() throws InterruptedException {
        System.out.println("--- pktForward captured as false when interpreter is backward ---");
        Interpreter interp = new Interpreter();
        interp.setForward(false);
        interp.interpreterToken.set(false);

        PocketInstance pkt = spinningPocket(interp);
        pkt.starvationPolicy = "IMMORTAL";
        pkt.startIndependent(freshEnv("bwd", pkt), nameToken("bwd"));

        Thread.sleep(20);
        pkt.beginDeath();

        eq("pktForward = false", false, pkt.pktForward);
    }

    private static void testContextCapturedInverted() throws InterruptedException {
        System.out.println("--- pktInvertedMode captured at startIndependent time ---");
        Interpreter interp = new Interpreter();
        interp.setForward(true);
        interp.setInverted(true);
        interp.interpreterToken.set(false);

        PocketInstance pkt = spinningPocket(interp);
        pkt.starvationPolicy = "IMMORTAL";
        pkt.startIndependent(freshEnv("inv", pkt), nameToken("inv"));

        Thread.sleep(20);
        pkt.beginDeath();

        eq("pktInvertedMode = true", true, pkt.pktInvertedMode);
    }

    private static void testContextNotCapturedBeforeStart() {
        System.out.println("--- pktForward defaults to false before startIndependent ---");
        Interpreter interp = new Interpreter();
        interp.setForward(true);

        PocketInstance pkt = spinningPocket(interp);
        // pktForward is initialized as false (default boolean) before startIndependent
        eq("pktForward default before start", false, pkt.pktForward);
    }

    // -------------------------------------------------------------------------
    // Window size: N ticks per acquisition
    // -------------------------------------------------------------------------

    private static void testDefaultWindowSize() {
        System.out.println("--- default window size is 1 ---");
        Interpreter interp = new Interpreter();
        PocketInstance pkt = spinningPocket(interp);
        eq("windowSize default = 1", 1, pkt.windowSize);
    }

    private static void testWindowSizeSettable() {
        System.out.println("--- window size can be set before start ---");
        Interpreter interp = new Interpreter();
        PocketInstance pkt = spinningPocket(interp);
        pkt.windowSize = 5;
        eq("windowSize = 5", 5, pkt.windowSize);
    }

    // -------------------------------------------------------------------------
    // Policy defaults
    // -------------------------------------------------------------------------

    private static void testDefaultStarvationPolicy() {
        System.out.println("--- default starvation policy is STRICT ---");
        Interpreter interp = new Interpreter();
        PocketInstance pkt = spinningPocket(interp);
        eq("policy = STRICT", "STRICT", pkt.starvationPolicy);
    }

    private static void testDefaultStarvationThreshold() {
        System.out.println("--- default starvation threshold is 3 ---");
        Interpreter interp = new Interpreter();
        PocketInstance pkt = spinningPocket(interp);
        eq("threshold = 3", 3, pkt.starvationThreshold);
    }

    private static void testTkpDefaults() {
        System.out.println("--- TkpInstance defaults match PocketInstance ---");
        Interpreter interp = new Interpreter();
        TkpInstance tkp = spinningTkp(interp);
        eq("tkp policy = STRICT", "STRICT", tkp.starvationPolicy);
        eq("tkp threshold = 3", 3, tkp.starvationThreshold);
        eq("tkp windowSize = 1", 1, tkp.windowSize);
    }

    // -------------------------------------------------------------------------
    // isAlive state
    // -------------------------------------------------------------------------

    private static void testIsAliveBeforeStart() {
        System.out.println("--- pocket is alive before startIndependent ---");
        Interpreter interp = new Interpreter();
        PocketInstance pkt = spinningPocket(interp);
        check("isAlive() before start", pkt.isAlive());
    }

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) throws InterruptedException {
        testContentionLogsTobin();
        testContentionMessageCount();
        testTkpContentionLogsTobin();
        testTokenReleasedAfterWindow();
        testContextCapturedForward();
        testContextCapturedBackward();
        testContextCapturedInverted();
        testContextNotCapturedBeforeStart();
        testDefaultWindowSize();
        testWindowSizeSettable();
        testDefaultStarvationPolicy();
        testDefaultStarvationThreshold();
        testTkpDefaults();
        testIsAliveBeforeStart();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
    }
}
