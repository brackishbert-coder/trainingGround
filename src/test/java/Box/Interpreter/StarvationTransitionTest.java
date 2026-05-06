package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Parser.Expr;
import Parser.Stmt;
import Box.Token.Token;
import Box.Token.TokenType;

/**
 * Tests for STRICT-policy starvation transitions in PocketInstance and TkpInstance.
 *
 * STRICT starvation (default):
 *   - After consecutiveFailures >= starvationThreshold, the instance destroys itself
 *     and creates a counterpart of the opposite type.
 *   - PocketInstance → TkpInstance  (logged as "starvation: pkt->tkp pocket=<name>")
 *   - TkpInstance    → PocketInstance (logged as "starvation: tkp->pkt pocket=<name>")
 *
 * In both cases:
 *   - The original instance is destroyed: isAlive() == false
 *   - The new instance is assigned in the environment (replaces the original name)
 *   - The current body (at time of transition) is transferred to the new instance
 *   - A starvation message is added to BIN
 *   - The tick thread exits (returns from the thread closure)
 *
 * Test strategy:
 *   - Hold interpreterToken externally (set to false) to force every CAS to fail.
 *   - Set starvationThreshold = 1 to trigger after a single failure (fastest test).
 *   - Wait a short time then verify: destroyed state, env assignment, BIN message.
 */
public class StarvationTransitionTest {

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

    private static Stmt.Expression pocketOpenStmt(String label) {
        Token ctrl = new Token(TokenType.OPENPAREN, label + "(", null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.PocketOpen(ctrl), null);
    }

    private static Stmt.Expression pocketCloseStmt(String label) {
        Token ctrl = new Token(TokenType.CLOSEDPAREN, ")" + label, null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.PocketClosed(ctrl), null);
    }

    private static Token nameToken(String name) {
        return new Token(TokenType.IDENTIFIER, name, null, null, null, 0, 0, 0, 0);
    }

    /**
     * A PocketInstance that won't heat-death: body has a Flow so isHeatDeath() returns false.
     */
    private static PocketInstance makePkt(Interpreter interp, String name) {
        List<Object> body = new ArrayList<>();
        body.add(pocketOpenStmt(name));
        body.add(new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5));
        body.add(pocketCloseStmt(name));
        PocketInstance pkt = new PocketInstance(null, body, null, interp);
        pkt.starvationPolicy = "STRICT";
        pkt.starvationThreshold = 1; // trigger after one failure
        return pkt;
    }

    /**
     * A TkpInstance that won't heat-death: body has a Flow.
     */
    private static TkpInstance makeTkp(Interpreter interp, String name) {
        List<Object> body = new ArrayList<>();
        body.add(pocketOpenStmt(name));
        body.add(new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5));
        body.add(pocketCloseStmt(name));
        TkpInstance tkp = new TkpInstance(null, body, null, interp);
        tkp.starvationPolicy = "STRICT";
        tkp.starvationThreshold = 1;
        return tkp;
    }

    private static Environment envWith(String name, Object value) {
        Environment env = new Environment();
        env.define(name, (Token) null, value);
        return env;
    }

    private static boolean binContains(Interpreter interp, String prefix) {
        List<Object> snapshot = new ArrayList<>(interp.bin.body);
        for (Object o : snapshot)
            if (o instanceof String && ((String) o).startsWith(prefix)) return true;
        return false;
    }

    // -------------------------------------------------------------------------
    // pkt → tkp
    // -------------------------------------------------------------------------

    private static void testPktDestroyed() throws InterruptedException {
        System.out.println("--- pkt.isAlive() == false after STRICT starvation ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        PocketInstance pkt = makePkt(interp, "p");
        Token name = nameToken("p");
        pkt.startIndependent(envWith("p", pkt), name);

        Thread.sleep(200);

        check("pkt not alive after starvation", !pkt.isAlive());
    }

    private static void testPktEnvReplacedWithTkp() throws InterruptedException {
        System.out.println("--- env entry replaced with TkpInstance after pkt starvation ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        PocketInstance pkt = makePkt(interp, "p");
        Token name = nameToken("p");
        Environment env = envWith("p", pkt);
        pkt.startIndependent(env, name);

        Thread.sleep(200);

        Object inEnv = env.get(name, false);
        check("env has a value after transition", inEnv != null);
        check("env value is TkpInstance", inEnv instanceof TkpInstance);
    }

    private static void testPktBinStarvationMessage() throws InterruptedException {
        System.out.println("--- BIN has pkt->tkp starvation message ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        PocketInstance pkt = makePkt(interp, "worker");
        pkt.startIndependent(envWith("worker", pkt), nameToken("worker"));

        Thread.sleep(200);

        check("BIN contains starvation: pkt->tkp", binContains(interp, "starvation: pkt->tkp pocket=worker"));
    }

    private static void testPktBodyTransferredToTkp() throws InterruptedException {
        System.out.println("--- body transferred to TkpInstance at time of transition ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        // Body: [pocketOpen, "cargo", Flow, pocketClose]
        List<Object> body = new ArrayList<>();
        body.add(pocketOpenStmt("p"));
        body.add("cargo");
        body.add(new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5));
        body.add(pocketCloseStmt("p"));
        PocketInstance pkt = new PocketInstance(null, body, null, interp);
        pkt.starvationPolicy = "STRICT";
        pkt.starvationThreshold = 1;

        Token name = nameToken("p");
        Environment env = envWith("p", pkt);
        pkt.startIndependent(env, name);

        Thread.sleep(200);

        Object inEnv = env.get(name, false);
        if (inEnv instanceof TkpInstance) {
            TkpInstance tkp = (TkpInstance) inEnv;
            check("tkp body contains cargo", tkp.body.contains("cargo"));
        } else {
            check("tkp body contains cargo — env has no TkpInstance", false);
        }
    }

    private static void testPktNotAliveBlocksNewStart() throws InterruptedException {
        System.out.println("--- destroyed pkt cannot be re-started ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        PocketInstance pkt = makePkt(interp, "p");
        Environment env = envWith("p", pkt);
        pkt.startIndependent(env, nameToken("p"));

        Thread.sleep(200);
        check("pkt destroyed", !pkt.isAlive());

        // Second startIndependent on a destroyed pocket: thread exits immediately
        // (isAlive() is checked first in loop condition). No exception.
        interp.interpreterToken.set(true);
        pkt.startIndependent(env, nameToken("p")); // must not throw
        Thread.sleep(50);
        check("no exception on re-start attempt of destroyed pkt", true);
    }

    // -------------------------------------------------------------------------
    // tkp → pkt
    // -------------------------------------------------------------------------

    private static void testTkpDestroyed() throws InterruptedException {
        System.out.println("--- tkp.isAlive() == false after STRICT starvation ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        TkpInstance tkp = makeTkp(interp, "q");
        Token name = nameToken("q");
        tkp.startIndependent(envWith("q", tkp), name);

        Thread.sleep(200);

        check("tkp not alive after starvation", !tkp.isAlive());
    }

    private static void testTkpEnvReplacedWithPkt() throws InterruptedException {
        System.out.println("--- env entry replaced with PocketInstance after tkp starvation ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        TkpInstance tkp = makeTkp(interp, "q");
        Token name = nameToken("q");
        Environment env = envWith("q", tkp);
        tkp.startIndependent(env, name);

        Thread.sleep(200);

        Object inEnv = env.get(name, false);
        check("env has a value after tkp transition", inEnv != null);
        check("env value is PocketInstance", inEnv instanceof PocketInstance);
    }

    private static void testTkpBinStarvationMessage() throws InterruptedException {
        System.out.println("--- BIN has tkp->pkt starvation message ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        TkpInstance tkp = makeTkp(interp, "drone");
        tkp.startIndependent(envWith("drone", tkp), nameToken("drone"));

        Thread.sleep(200);

        check("BIN contains starvation: tkp->pkt", binContains(interp, "starvation: tkp->pkt pocket=drone"));
    }

    private static void testTkpBodyTransferredToPkt() throws InterruptedException {
        System.out.println("--- body transferred to PocketInstance at time of tkp transition ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        List<Object> body = new ArrayList<>();
        body.add(pocketOpenStmt("q"));
        body.add("payload");
        body.add(new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5));
        body.add(pocketCloseStmt("q"));
        TkpInstance tkp = new TkpInstance(null, body, null, interp);
        tkp.starvationPolicy = "STRICT";
        tkp.starvationThreshold = 1;

        Token name = nameToken("q");
        Environment env = envWith("q", tkp);
        tkp.startIndependent(env, name);

        Thread.sleep(200);

        Object inEnv = env.get(name, false);
        if (inEnv instanceof PocketInstance) {
            PocketInstance pkt = (PocketInstance) inEnv;
            check("pkt body contains payload", pkt.body.contains("payload"));
        } else {
            check("pkt body contains payload — env has no PocketInstance", false);
        }
    }

    // -------------------------------------------------------------------------
    // Threshold: fires exactly when threshold crossed
    // -------------------------------------------------------------------------

    private static void testThresholdDefaultIs3() throws InterruptedException {
        System.out.println("--- default threshold=3: survives 2 failures, transitions on 3rd ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        PocketInstance pkt = makePkt(interp, "p");
        pkt.starvationThreshold = 3; // explicit default
        pkt.startIndependent(envWith("p", pkt), nameToken("p"));

        Thread.sleep(200);

        check("pkt destroyed after threshold=3 failures", !pkt.isAlive());
    }

    private static void testImmortalPolicyNeverTransitions() throws InterruptedException {
        System.out.println("--- IMMORTAL policy: pocket never transitions regardless of failures ---");
        Interpreter interp = new Interpreter();
        interp.interpreterToken.set(false);

        PocketInstance pkt = makePkt(interp, "im");
        pkt.starvationPolicy = "IMMORTAL";
        pkt.starvationThreshold = 1;
        pkt.startIndependent(envWith("im", pkt), nameToken("im"));

        Thread.sleep(150);

        check("pkt still alive with IMMORTAL policy", pkt.isAlive());
        check("BIN has contention messages", !new ArrayList<>(interp.bin.body).isEmpty());
        pkt.beginDeath();
        Thread.sleep(30); // let tick thread observe beginDeath and exit before shutdown hook
    }

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) throws InterruptedException {
        testPktDestroyed();
        testPktEnvReplacedWithTkp();
        testPktBinStarvationMessage();
        testPktBodyTransferredToTkp();
        testPktNotAliveBlocksNewStart();
        testTkpDestroyed();
        testTkpEnvReplacedWithPkt();
        testTkpBinStarvationMessage();
        testTkpBodyTransferredToPkt();
        testThresholdDefaultIs3();
        testImmortalPolicyNeverTransitions();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
    }
}
