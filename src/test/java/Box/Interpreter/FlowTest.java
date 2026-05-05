package Box.Interpreter;

/**
 * Standalone tests for Flow, focusing on flipDirection() and related behavior.
 * No JUnit dependency — compile and run directly.
 *
 * Run:
 *   javac -cp <classpath> src/test/java/Box/Interpreter/FlowTest.java -d /tmp/flowtestbuild
 *   java  -cp <classpath>:/tmp/flowtestbuild Box.Interpreter.FlowTest
 */
public class FlowTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("  PASS  " + name);
            passed++;
        } else {
            System.out.println("  FAIL  " + name);
            failed++;
        }
    }

    private static void eq(String name, Object expected, Object actual) {
        boolean ok = (expected == null) ? (actual == null) : expected.equals(actual);
        if (ok) {
            System.out.println("  PASS  " + name);
            passed++;
        } else {
            System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual);
            failed++;
        }
    }

    // -------------------------------------------------------------------------
    // flipDirection
    // -------------------------------------------------------------------------

    private static void testFlipForwardToBackward() {
        System.out.println("--- flipDirection FORWARD→BACKWARD ---");
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 3);
        check("initially forward",        f.isForward());
        check("not initially backward",   !f.isBackward());
        eq("direction FORWARD",           Flow.Direction.FORWARD, f.getDirection());
        f.flipDirection();
        check("backward after flip",      f.isBackward());
        check("not forward after flip",   !f.isForward());
        eq("direction BACKWARD",          Flow.Direction.BACKWARD, f.getDirection());
    }

    private static void testFlipBackwardToForward() {
        System.out.println("--- flipDirection BACKWARD→FORWARD ---");
        Flow f = new Flow(Flow.Direction.BACKWARD, Flow.BracketType.PAREN, 3);
        check("initially backward",  f.isBackward());
        f.flipDirection();
        check("forward after flip",  f.isForward());
        eq("direction FORWARD",      Flow.Direction.FORWARD, f.getDirection());
    }

    private static void testFlipTwiceRestores() {
        System.out.println("--- flipDirection twice restores original ---");
        Flow fwd = new Flow(Flow.Direction.FORWARD,  Flow.BracketType.BRACE, 1);
        Flow bwd = new Flow(Flow.Direction.BACKWARD, Flow.BracketType.BRACE, 1);
        fwd.flipDirection(); fwd.flipDirection();
        bwd.flipDirection(); bwd.flipDirection();
        check("FORWARD restored",  fwd.isForward());
        check("BACKWARD restored", bwd.isBackward());
    }

    private static void testFlipDoesNotAffectOtherFields() {
        System.out.println("--- flip does not affect chain/cargo/bracketType ---");
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.BRACE, 3);
        f.injectCargo("data");
        f.flipDirection();
        eq("chain unchanged",       3,                    f.getChain());
        eq("cargo unchanged",       "data",               f.getCargo());
        eq("bracketType unchanged", Flow.BracketType.BRACE, f.getBracketType());
    }

    private static void testFlipAllBracketTypes() {
        System.out.println("--- flipDirection works for all BracketType values ---");
        for (Flow.BracketType bt : Flow.BracketType.values()) {
            Flow f = new Flow(Flow.Direction.FORWARD, bt, 1);
            f.flipDirection();
            check("backward after flip (" + bt + ")", f.isBackward());
        }
    }

    // -------------------------------------------------------------------------
    // spendToken
    // -------------------------------------------------------------------------

    private static void testSpendToken() {
        System.out.println("--- spendToken ---");
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 2);
        check("canBootstrap with chain=2", f.canBootstrap());
        eq("chain start",  2, f.getChain());
        check("spend 1 succeeds",  f.spendToken());
        eq("chain after 1 spend",  1, f.getChain());
        check("still canBootstrap",f.canBootstrap());
        check("spend 2 succeeds",  f.spendToken());
        eq("chain after 2 spends", 0, f.getChain());
        check("cannot bootstrap",  !f.canBootstrap());
        check("spend past 0 fails",!f.spendToken());
        eq("chain stays 0",        0, f.getChain());
    }

    private static void testSpendTokenResetsIndexAtZero() {
        System.out.println("--- spendToken resets cargoIndex when chain hits zero (buffer kept) ---");
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 2);
        f.injectCargo("payload");
        eq("cargo before spend", "payload", f.getCargo());
        f.spendToken();
        eq("cargo present after first spend (chain=1)", "payload", f.getCargo());
        f.spendToken();
        eq("cargo still present after last spend (index reset, buffer kept)", "payload", f.getCargo());
    }

    private static void testZeroChain() {
        System.out.println("--- zero-chain flow ---");
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 0);
        check("cannot bootstrap",    !f.canBootstrap());
        check("spend fails",         !f.spendToken());
        eq("chain stays 0",          0, f.getChain());
    }

    // -------------------------------------------------------------------------
    // addChain
    // -------------------------------------------------------------------------

    private static void testAddChain() {
        System.out.println("--- addChain ---");
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        f.addChain(4);
        eq("chain after addChain(4)", 5, f.getChain());
        check("canBootstrap after addChain", f.canBootstrap());
    }

    private static void testAddChainFromZero() {
        System.out.println("--- addChain from zero ---");
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 0);
        check("cannot bootstrap before addChain", !f.canBootstrap());
        f.addChain(1);
        check("can bootstrap after addChain(1)",  f.canBootstrap());
        eq("chain",                               1, f.getChain());
    }

    // -------------------------------------------------------------------------
    // bracketType and cargo
    // -------------------------------------------------------------------------

    private static void testBracketTypes() {
        System.out.println("--- bracketType preserved through construction ---");
        for (Flow.BracketType bt : Flow.BracketType.values()) {
            Flow f = new Flow(Flow.Direction.FORWARD, bt, 1);
            eq("bracketType " + bt, bt, f.getBracketType());
        }
    }

    private static void testCargo() {
        System.out.println("--- cargo inject and retrieve ---");
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        eq("cargo initially null", null, f.getCargo());
        f.injectCargo(42);
        eq("cargo after first inject", 42, f.getCargo());
        f.injectCargo("second");
        eq("cargo after second inject (index stays 0)", 42, f.getCargo());
        f.advanceCargoIndex();
        eq("cargo after advance", "second", f.getCargo());
        f.advanceCargoIndex();
        eq("cargo wraps around", 42, f.getCargo());
    }

    private static void testInjectCargoChain() {
        System.out.println("--- injectCargoChain adds chain tokens + cargo ---");
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 0);
        f.injectCargoChain("hello", 3);
        eq("chain after injectCargoChain(3)", 3, f.getChain());
        eq("cargo after injectCargoChain", "hello", f.getCargo());
        check("canBootstrap after injectCargoChain", f.canBootstrap());
    }

    private static void testSpendTokenRemovesLinkedCargo() {
        System.out.println("--- spendToken removes cargo linked to carrying token ---");
        Flow f = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 0);
        f.injectCargoChain("linked", 2);
        eq("cargo before spend", "linked", f.getCargo());
        eq("chain before spend", 2, f.getChain());
        f.spendToken();
        eq("cargo removed after spending carrying token", null, f.getCargo());
        eq("chain after first spend", 1, f.getChain());
        f.spendToken();
        eq("chain after second spend", 0, f.getChain());
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== Flow Test ===\n");
        run("flip FORWARD→BACKWARD",     FlowTest::testFlipForwardToBackward);
        run("flip BACKWARD→FORWARD",     FlowTest::testFlipBackwardToForward);
        run("flip twice restores",       FlowTest::testFlipTwiceRestores);
        run("flip other fields intact",  FlowTest::testFlipDoesNotAffectOtherFields);
        run("flip all bracket types",    FlowTest::testFlipAllBracketTypes);
        run("spendToken",                FlowTest::testSpendToken);
        run("spendToken resets index",    FlowTest::testSpendTokenResetsIndexAtZero);
        run("zero chain",                FlowTest::testZeroChain);
        run("addChain",                  FlowTest::testAddChain);
        run("addChain from zero",        FlowTest::testAddChainFromZero);
        run("bracketType preserved",     FlowTest::testBracketTypes);
        run("cargo inject/retrieve",     FlowTest::testCargo);
        run("injectCargoChain",          FlowTest::testInjectCargoChain);
        run("spendToken removes linked", FlowTest::testSpendTokenRemovesLinkedCargo);
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
