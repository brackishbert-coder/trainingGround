package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Parser.Expr;
import Parser.Stmt;
import Box.Token.Token;
import Box.Token.TokenType;

/**
 * Tests for BoxFunction bootstrap in PocketInstance and TkpInstance tick loops.
 *
 * When a flow with canBootstrap()=true encounters a BoxFunction in the body:
 *   Option 4 (cargo present): current cargo wrapped in a BoxInstance, passed as sole arg,
 *                             cargoIndex advances, result replaces function in body.
 *   Option 3 (no cargo):      preceding non-control body items consumed up to arity,
 *                             empty BoxInstances fill any gap, result replaces function.
 *   Zero-arg shortcut:        arity=0 and no cargo → empty BoxInstance passed, result replaces.
 *
 * No JUnit dependency. Run:
 *   javac -cp <classpath> src/test/java/Box/Interpreter/FunctionBootstrapTest.java -d /tmp/fnbuild
 *   java  -cp <classpath>:/tmp/fnbuild Box.Interpreter.FunctionBootstrapTest
 */
public class FunctionBootstrapTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) { System.out.println("  PASS  " + name); passed++; }
        else           { System.out.println("  FAIL  " + name); failed++; }
    }

    private static void eq(String name, Object expected, Object actual) {
        boolean ok = (expected == null) ? (actual == null) : expected.equals(actual);
        if (ok) { System.out.println("  PASS  " + name); passed++; }
        else    { System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual); failed++; }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Token tok(TokenType type, String lexeme) {
        return new Token(type, lexeme, null, null, null, 0, 0, 0, 0);
    }

    private static Stmt.Expression pocketOpenStmt(String label) {
        return new Stmt.Expression(new Expr.PocketOpen(tok(TokenType.OPENPAREN, label + "(")), null);
    }

    private static Stmt.Expression pocketCloseStmt(String label) {
        return new Stmt.Expression(new Expr.PocketClosed(tok(TokenType.CLOSEDPAREN, ")" + label)), null);
    }

    private static Interpreter makeInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    /** Stub BoxFunction: overrides call() to return returnValue, arity() to return declared arity. */
    private static BoxFunction stubFn(int arity, Object returnValue) {
        return new BoxFunction(null, "fn", null, null, null, true, false) {
            @Override public int arity() { return arity; }
            @Override public Object call(Interpreter interp, List<Object> args) { return returnValue; }
        };
    }

    /** Capturing stub: records args into capturedArgs[0] and returns returnValue. */
    @SuppressWarnings("unchecked")
    private static BoxFunction capturingFn(int arity, Object returnValue, List<Object>[] capturedArgs) {
        return new BoxFunction(null, "fn", null, null, null, true, false) {
            @Override public int arity() { return arity; }
            @Override public Object call(Interpreter interp, List<Object> args) {
                capturedArgs[0] = new ArrayList<>(args);
                return returnValue;
            }
        };
    }

    /** PocketInstance with given items between structural brackets. */
    private static PocketInstance makePocket(Interpreter interp, Object... items) {
        List<Object> body = new ArrayList<>();
        body.add(pocketOpenStmt("tst"));
        for (Object item : items) body.add(item);
        body.add(pocketCloseStmt("tst"));
        return new PocketInstance(null, body, null, interp);
    }

    /** TkpInstance with given items between structural brackets. */
    private static TkpInstance makeTkp(Interpreter interp, Object... items) {
        List<Object> body = new ArrayList<>();
        body.add(pocketOpenStmt("tst"));
        for (Object item : items) body.add(item);
        body.add(pocketCloseStmt("tst"));
        return new TkpInstance(null, body, null, interp);
    }

    private static BoxInstance emptyBox(Interpreter interp) {
        return new BoxInstance(null, new ArrayList<>(), null, interp);
    }

    private static boolean bodyContainsUnboxed(java.util.List<Object> body, Object expected) {
        for (Object item : body) {
            Object v = Boxer.unbox(item);
            if (expected == null ? v == null : expected.equals(v)) return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // PocketInstance tests
    // -------------------------------------------------------------------------

    /** Zero-arity function, no cargo: called with empty BoxInstance, result in body. */
    private static void testPocketZeroArgBootstrap() {
        System.out.println("--- pocket: zero-arg function bootstrap ---");
        Interpreter interp = makeInterp();
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        BoxFunction fn = stubFn(0, "result");
        PocketInstance pkt = makePocket(interp, f, fn);
        pkt.tick();
        check("fn replaced with result",  bodyContainsUnboxed(pkt.body, "result"));
        check("fn no longer in body",     !pkt.body.contains(fn));
    }

    /** Zero-arity function receives exactly one empty BoxInstance as arg. */
    private static void testPocketZeroArgReceivesEmptyBox() {
        System.out.println("--- pocket: zero-arg receives empty BoxInstance ---");
        Interpreter interp = makeInterp();
        @SuppressWarnings("unchecked") List<Object>[] captured = new List[1];
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        BoxFunction fn = capturingFn(0, "r", captured);
        makePocket(interp, f, fn).tick();
        check("one arg passed",           captured[0] != null && captured[0].size() == 1);
        check("arg is empty BoxInstance", captured[0] != null
                && captured[0].get(0) instanceof BoxInstance
                && ((BoxInstance) captured[0].get(0)).body.isEmpty());
    }

    /** Option 4: flow has cargo → cargo wrapped in BoxInstance, passed as arg. */
    private static void testPocketCargoPackagedAsArg() {
        System.out.println("--- pocket: cargo packaged into BoxInstance arg ---");
        Interpreter interp = makeInterp();
        @SuppressWarnings("unchecked") List<Object>[] captured = new List[1];
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        f.injectCargo("my_cargo");
        BoxFunction fn = capturingFn(1, "done", captured);
        makePocket(interp, f, fn).tick();
        check("one arg passed",               captured[0] != null && captured[0].size() == 1);
        Object arg = captured[0] == null ? null : captured[0].get(0);
        check("arg is BoxInstance",           arg instanceof BoxInstance);
        check("BoxInstance contains cargo",   arg instanceof BoxInstance
                && ((BoxInstance) arg).body.contains("my_cargo"));
    }

    /** Option 4 advances cargoIndex after packaging. */
    private static void testPocketCargoIndexAdvances() {
        System.out.println("--- pocket: cargo index advances after Option 4 ---");
        Interpreter interp = makeInterp();
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 2);
        f.injectCargo("first");
        f.injectCargo("second");

        // First tick: bootstrap fn1 → uses "first", index advances to "second"
        BoxFunction fn1 = stubFn(1, "r1");
        BoxFunction fn2 = stubFn(1, "r2");
        @SuppressWarnings("unchecked") List<Object>[] cap2 = new List[1];
        BoxFunction capFn2 = capturingFn(1, "r2", cap2);

        PocketInstance pkt = makePocket(interp, f, fn1, capFn2);
        pkt.tick();  // bootstraps fn1 (cargo="first")
        pkt.tick();  // bootstraps capFn2 (cargo="second")
        Object arg = cap2[0] == null ? null : cap2[0].get(0);
        check("second tick uses 'second' cargo",
                arg instanceof BoxInstance
                && ((BoxInstance) arg).body.contains("second"));
    }

    /** Option 3: preceding item consumed as arg, result placed at correct index. */
    private static void testPocketPrecedingItemConsumedAsArg() {
        System.out.println("--- pocket: preceding item consumed as arg (Option 3) ---");
        Interpreter interp = makeInterp();
        @SuppressWarnings("unchecked") List<Object>[] captured = new List[1];
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        BoxFunction fn = capturingFn(1, "result", captured);
        PocketInstance pkt = makePocket(interp, f, "arg_val", fn);
        pkt.tick();
        check("arg is the preceding item",    captured[0] != null
                && captured[0].size() == 1
                && "arg_val".equals(captured[0].get(0)));
        check("preceding item removed",       !pkt.body.contains("arg_val"));
        check("result in body",               bodyContainsUnboxed(pkt.body, "result"));
    }

    /** Two-arg function with one preceding item: real arg + empty BoxInstance fill. */
    private static void testPocketTwoArgOnePreceding() {
        System.out.println("--- pocket: two-arg, one preceding item + one fill ---");
        Interpreter interp = makeInterp();
        @SuppressWarnings("unchecked") List<Object>[] captured = new List[1];
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        BoxFunction fn = capturingFn(2, "done", captured);
        makePocket(interp, f, "only_arg", fn).tick();
        check("two args passed",              captured[0] != null && captured[0].size() == 2);
        check("first arg is 'only_arg'",      captured[0] != null
                && "only_arg".equals(captured[0].get(0)));
        check("second arg is empty BoxInstance", captured[0] != null
                && captured[0].get(1) instanceof BoxInstance
                && ((BoxInstance) captured[0].get(1)).body.isEmpty());
    }

    /** Flow token is spent after bootstrap. */
    private static void testPocketFlowTokenSpentAfterBootstrap() {
        System.out.println("--- pocket: flow token spent after bootstrap ---");
        Interpreter interp = makeInterp();
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 2);
        BoxFunction fn1 = stubFn(0, "r1");
        BoxFunction fn2 = stubFn(0, "r2");
        PocketInstance pkt = makePocket(interp, f, fn1, fn2);
        pkt.tick();  // bootstraps fn1, spends one token (chain now 1)
        check("fn1 replaced after first tick",  bodyContainsUnboxed(pkt.body, "r1"));
        pkt.tick();  // bootstraps fn2, spends last token
        check("fn2 replaced after second tick", bodyContainsUnboxed(pkt.body, "r2"));
    }

    // -------------------------------------------------------------------------
    // TkpInstance tests
    // -------------------------------------------------------------------------

    /** TkpInstance: zero-arg bootstrap works identically to pocket. */
    private static void testTkpZeroArgBootstrap() {
        System.out.println("--- tkp: zero-arg function bootstrap ---");
        Interpreter interp = makeInterp();
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        BoxFunction fn = stubFn(0, "tkp_result");
        TkpInstance tkp = makeTkp(interp, f, fn);
        tkp.tick();
        check("fn replaced with result",  bodyContainsUnboxed(tkp.body, "tkp_result"));
        check("fn no longer in body",     !tkp.body.contains(fn));
    }

    /** TkpInstance Option 4: cargo wrapped in BoxInstance, passed as arg. */
    private static void testTkpCargoPackagedAsArg() {
        System.out.println("--- tkp: cargo packaged into BoxInstance arg ---");
        Interpreter interp = makeInterp();
        @SuppressWarnings("unchecked") List<Object>[] captured = new List[1];
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        f.injectCargo("tkp_cargo");
        BoxFunction fn = capturingFn(1, "done", captured);
        makeTkp(interp, f, fn).tick();
        Object arg = captured[0] == null ? null : captured[0].get(0);
        check("arg is BoxInstance",           arg instanceof BoxInstance);
        check("BoxInstance contains cargo",   arg instanceof BoxInstance
                && ((BoxInstance) arg).body.contains("tkp_cargo"));
    }

    /** TkpInstance Option 3: preceding item consumed as arg. */
    private static void testTkpPrecedingItemConsumedAsArg() {
        System.out.println("--- tkp: preceding item consumed as arg ---");
        Interpreter interp = makeInterp();
        @SuppressWarnings("unchecked") List<Object>[] captured = new List[1];
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        BoxFunction fn = capturingFn(1, "res", captured);
        TkpInstance tkp = makeTkp(interp, f, "tkp_arg", fn);
        tkp.tick();
        check("arg is preceding item",    captured[0] != null
                && "tkp_arg".equals(captured[0].get(0)));
        check("preceding item removed",   !tkp.body.contains("tkp_arg"));
        check("result in body",           bodyContainsUnboxed(tkp.body, "res"));
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== Function Bootstrap Test ===\n");
        run("pocket: zero-arg bootstrap",            FunctionBootstrapTest::testPocketZeroArgBootstrap);
        run("pocket: zero-arg gets empty BoxInst",   FunctionBootstrapTest::testPocketZeroArgReceivesEmptyBox);
        run("pocket: cargo packaged as arg",         FunctionBootstrapTest::testPocketCargoPackagedAsArg);
        run("pocket: cargo index advances",          FunctionBootstrapTest::testPocketCargoIndexAdvances);
        run("pocket: preceding item consumed",       FunctionBootstrapTest::testPocketPrecedingItemConsumedAsArg);
        run("pocket: two-arg + fill",                FunctionBootstrapTest::testPocketTwoArgOnePreceding);
        run("pocket: token spent after bootstrap",   FunctionBootstrapTest::testPocketFlowTokenSpentAfterBootstrap);
        run("tkp: zero-arg bootstrap",               FunctionBootstrapTest::testTkpZeroArgBootstrap);
        run("tkp: cargo packaged as arg",            FunctionBootstrapTest::testTkpCargoPackagedAsArg);
        run("tkp: preceding item consumed",          FunctionBootstrapTest::testTkpPrecedingItemConsumedAsArg);
        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
        } catch (Exception e) {
            System.out.println("  ERROR in " + name + ": " + e);
            e.printStackTrace(System.out);
            failed++;
        }
    }
}
