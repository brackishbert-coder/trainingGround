package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Expr;
import Parser.Stmt;

/**
 * Tests for KnotRunner.runKnot() oscillation loop behavior.
 *
 * Standard KNOT fixture layout (no SETUP targets):
 *   0  Literal("body")      — body output, executes on initial forward pass
 *   1  CupOpen("a{")        — outer-left bracket (SETUP region boundary)
 *   2  PocketOpen("x(")     — forward-condition start (OPEN, KNOT shape)
 *   3  condExpr             — condition evaluated in backward range check
 *   4  CupClosed("}a")      — condition true target
 *   5  PocketClosed(")x")   — condition false target / outer-right bracket
 *
 * Oscillation control flow (KNOT = { ... ) shape):
 *
 *   Forward at PocketOpen(2):
 *     backwardConditionStartForTrueEntry fires → checkConditionRangeBackward(4,2) → eval index 3
 *     true  → flip backward, jump to 1
 *     false → return size → EXIT (direction stays forward)
 *
 *   Backward at CupOpen(1):
 *     backwardConditionStartForFalseExit fires  → checkConditionRangeBackward(4,2) → eval index 3
 *     true  → flip forward, jump to 2
 *     false → return -1 → EXIT (direction stays backward)
 *
 * countdown(N) returns true N times then false.
 *   N=0: PocketOpen immediately false → exit forward
 *   N=1: PocketOpen true, CupOpen false → exit backward
 *   N=2: PocketOpen true, CupOpen true, PocketOpen false → exit forward
 *   N=3: 3 true, CupOpen false → exit backward
 *
 * Body item (index 0) executes exactly once on the initial forward pass;
 * oscillation only cycles between indices 1 and 2.
 *
 * SETUP fixture (adds one item between CupOpen and PocketOpen):
 *   0  Literal("body")
 *   1  CupOpen("a{")
 *   2  setupStmt             — SETUP region; toRun=false after construction
 *   3  PocketOpen("x(")
 *   4  condExpr
 *   5  CupClosed("}a")
 *   6  PocketClosed(")x")
 */
public class KnotRunnerOscillationTest {

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

    private static Token tok(TokenType type, String lex) {
        return new Token(type, lex, null, null, null, 0, 0, 0, 0);
    }

    private static Stmt.Expression exprStmt(Expr expr) {
        return new Stmt.Expression(expr, null);
    }

    private static Interpreter makeInterp(boolean forward) {
        Interpreter i = new Interpreter();
        i.setForward(forward);
        return i;
    }

    /**
     * Returns an Expr that evaluates to true N times then false.
     * Uses backward-range check semantics: both PocketOpen and CupOpen checks
     * consume one evaluation from the same countdown.
     */
    private static Expr countdown(int n) {
        final int[] remaining = {n};
        return new Expr() {
            @Override
            public <R> R accept(Declaration.Visitor<R> visitor) {
                return (R) Boolean.valueOf(remaining[0]-- > 0);
            }
            @Override public void reverse() {}
        };
    }

    /**
     * Standard knot fixture: body item + KNOT brackets + condition.
     *   [Literal(bodyVal), CupOpen, PocketOpen, condExpr, CupClosed, PocketClosed]
     */
    private static List<Stmt> knotFixture(Object bodyVal, Expr condExpr) {
        List<Stmt> s = new ArrayList<>();
        if (bodyVal != null) s.add(exprStmt(new Expr.Literal(bodyVal)));
        s.add(exprStmt(new Expr.CupOpen    (tok(TokenType.OPENBRACE,   "a{"))));
        s.add(exprStmt(new Expr.PocketOpen  (tok(TokenType.OPENPAREN,   "x("))));
        s.add(exprStmt(condExpr));
        s.add(exprStmt(new Expr.CupClosed  (tok(TokenType.CLOSEDBRACE, "}a"))));
        s.add(exprStmt(new Expr.PocketClosed(tok(TokenType.CLOSEDPAREN, ")x"))));
        return s;
    }

    /**
     * Knot fixture with a single SETUP item between CupOpen and PocketOpen.
     *   [Literal(bodyVal), CupOpen, setupStmt, PocketOpen, condExpr, CupClosed, PocketClosed]
     */
    private static List<Stmt> knotFixtureWithSetup(Object bodyVal, Stmt setupItem, Expr condExpr) {
        List<Stmt> s = new ArrayList<>();
        if (bodyVal != null) s.add(exprStmt(new Expr.Literal(bodyVal)));
        s.add(exprStmt(new Expr.CupOpen    (tok(TokenType.OPENBRACE,   "a{"))));
        s.add(setupItem);
        s.add(exprStmt(new Expr.PocketOpen  (tok(TokenType.OPENPAREN,   "x("))));
        s.add(exprStmt(condExpr));
        s.add(exprStmt(new Expr.CupClosed  (tok(TokenType.CLOSEDBRACE, "}a"))));
        s.add(exprStmt(new Expr.PocketClosed(tok(TokenType.CLOSEDPAREN, ")x"))));
        return s;
    }

    // ---- Linear body (no control nodes) ----------------------------------------

    private static void testLinearNoControls() {
        System.out.println("--- linear body: no controls, all items returned ---");
        Interpreter interp = makeInterp(true);
        List<Stmt> stmts = new ArrayList<>();
        stmts.add(exprStmt(new Expr.Literal("a")));
        stmts.add(exprStmt(new Expr.Literal("b")));
        stmts.add(exprStmt(new Expr.Literal("c")));
        ArrayList<Object> result = new KnotRunner(null, stmts, interp).runKnot();
        eq("size = 3",  3, result.size());
        eq("item 0 = a", "a", result.get(0));
        eq("item 1 = b", "b", result.get(1));
        eq("item 2 = c", "c", result.get(2));
    }

    private static void testLinearBackwardNoControls() {
        System.out.println("--- linear body: backward, forward-only stmts return null (noisserpxe=null) ---");
        Interpreter interp = makeInterp(false);
        List<Stmt> stmts = new ArrayList<>();
        stmts.add(exprStmt(new Expr.Literal("a")));
        stmts.add(exprStmt(new Expr.Literal("b")));
        stmts.add(exprStmt(new Expr.Literal("c")));
        ArrayList<Object> result = new KnotRunner(null, stmts, interp).runKnot();
        // Stmt.Expression with noisserpxe=null → visitExpressionStmt returns null backward
        // → nothing added to notnull; result is empty
        eq("size = 0 (null results dropped)", 0, result.size());
    }

    // ---- Body item runs before brackets ----------------------------------------

    private static void testBodyRunsOnce_FalseCondition() {
        System.out.println("--- body item runs once, condition false → immediate exit ---");
        Interpreter interp = makeInterp(true);
        ArrayList<Object> result =
            new KnotRunner(null, knotFixture("body_val", countdown(0)), interp).runKnot();
        eq("result size = 1", 1, result.size());
        eq("result[0] = body_val", "body_val", result.get(0));
    }

    private static void testBodyRunsOnceDespiteOscillations() {
        System.out.println("--- body before brackets runs exactly once (countdown=3, 4 checks) ---");
        Interpreter interp = makeInterp(true);
        ArrayList<Object> result =
            new KnotRunner(null, knotFixture("unique_body", countdown(3)), interp).runKnot();
        eq("result size = 1", 1, result.size());
        eq("body item present once", "unique_body", result.get(0));
    }

    // ---- Direction after exit --------------------------------------------------

    private static void testCountdownZero_DirectionForward() {
        System.out.println("--- countdown=0: false at PocketOpen, exits forward ---");
        Interpreter interp = makeInterp(true);
        new KnotRunner(null, knotFixture("x", countdown(0)), interp).runKnot();
        check("direction = forward after exit", interp.isForward());
    }

    private static void testCountdownOne_DirectionBackward() {
        System.out.println("--- countdown=1: true(fwd), false(bwd) → exits backward ---");
        Interpreter interp = makeInterp(true);
        new KnotRunner(null, knotFixture("x", countdown(1)), interp).runKnot();
        check("direction = backward after exit", !interp.isForward());
    }

    private static void testCountdownTwo_DirectionForward() {
        System.out.println("--- countdown=2: true(fwd), true(bwd), false(fwd) → exits forward ---");
        Interpreter interp = makeInterp(true);
        new KnotRunner(null, knotFixture("x", countdown(2)), interp).runKnot();
        check("direction = forward after exit", interp.isForward());
    }

    private static void testCountdownThree_DirectionBackward() {
        System.out.println("--- countdown=3: 3 true then false at CupOpen → exits backward ---");
        Interpreter interp = makeInterp(true);
        new KnotRunner(null, knotFixture("x", countdown(3)), interp).runKnot();
        check("direction = backward after exit", !interp.isForward());
    }

    // ---- Termination -----------------------------------------------------------

    private static void testOscillationTerminates() {
        System.out.println("--- oscillation terminates and does not hang (countdown=5) ---");
        Interpreter interp = makeInterp(true);
        ArrayList<Object> result =
            new KnotRunner(null, knotFixture("item", countdown(5)), interp).runKnot();
        eq("returns exactly one body result", 1, result.size());
    }

    private static void testNoBrackets_EmptyBodyNoThrow() {
        System.out.println("--- empty body, only brackets + false cond: no crash, empty result ---");
        Interpreter interp = makeInterp(true);
        ArrayList<Object> result =
            new KnotRunner(null, knotFixture(null, countdown(0)), interp).runKnot();
        eq("result is empty", 0, result.size());
    }

    // ---- SETUP region ----------------------------------------------------------

    private static void testSetupItemRunsOnceDuringConstruction() {
        System.out.println("--- SETUP item executes once at construction, not during oscillation ---");
        final int[] callCount = {0};
        Expr sideEffect = new Expr() {
            @Override
            public <R> R accept(Declaration.Visitor<R> visitor) {
                callCount[0]++;
                return (R) Integer.valueOf(callCount[0]);
            }
            @Override public void reverse() {}
        };
        Stmt setupItem = new Stmt.Expression(sideEffect, null);

        Interpreter interp = makeInterp(true);
        KnotRunner runner = new KnotRunner(null,
            knotFixtureWithSetup("body", setupItem, countdown(0)), interp);

        eq("setup fires once at construction", 1, callCount[0]);

        runner.runKnot();
        eq("setup not re-fired during runKnot", 1, callCount[0]);
    }

    private static void testSetupItemRunsOnceWithOscillation() {
        System.out.println("--- SETUP item still fires only once even with N oscillations ---");
        final int[] callCount = {0};
        Expr sideEffect = new Expr() {
            @Override
            public <R> R accept(Declaration.Visitor<R> visitor) {
                callCount[0]++;
                return (R) Integer.valueOf(callCount[0]);
            }
            @Override public void reverse() {}
        };
        Stmt setupItem = new Stmt.Expression(sideEffect, null);

        Interpreter interp = makeInterp(true);
        KnotRunner runner = new KnotRunner(null,
            knotFixtureWithSetup("body", setupItem, countdown(4)), interp);

        eq("setup fires once at construction", 1, callCount[0]);
        runner.runKnot();
        eq("still exactly 1 call after 5-check oscillation", 1, callCount[0]);
    }

    // ---- runTonk delegates to runKnot -----------------------------------------

    private static void testRunTonkProducesSameResult() {
        System.out.println("--- runTonk() produces same result as runKnot() ---");
        Interpreter i1 = makeInterp(true);
        Interpreter i2 = makeInterp(true);
        ArrayList<Object> knotRes =
            new KnotRunner(null, knotFixture("val", countdown(0)), i1).runKnot();
        ArrayList<Object> tonkRes =
            new KnotRunner(null, knotFixture("val", countdown(0)), i2).runTonk();
        eq("sizes match",      knotRes.size(), tonkRes.size());
        eq("contents match",   knotRes.get(0), tonkRes.get(0));
    }

    private static void testRunTonkWithOscillationSameDirection() {
        System.out.println("--- runTonk() oscillates and exits with same direction as runKnot() ---");
        Interpreter i1 = makeInterp(true);
        Interpreter i2 = makeInterp(true);
        new KnotRunner(null, knotFixture("v", countdown(3)), i1).runKnot();
        new KnotRunner(null, knotFixture("v", countdown(3)), i2).runTonk();
        check("both exit backward", !i1.isForward() && !i2.isForward());
    }

    // ---- Entry point -----------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== KnotRunnerOscillationTest ===\n");

        run("linear body forward",                   KnotRunnerOscillationTest::testLinearNoControls);
        run("linear body backward",                  KnotRunnerOscillationTest::testLinearBackwardNoControls);
        run("body runs once with false condition",    KnotRunnerOscillationTest::testBodyRunsOnce_FalseCondition);
        run("body runs once despite oscillations",   KnotRunnerOscillationTest::testBodyRunsOnceDespiteOscillations);
        run("countdown=0 → exits forward",           KnotRunnerOscillationTest::testCountdownZero_DirectionForward);
        run("countdown=1 → exits backward",          KnotRunnerOscillationTest::testCountdownOne_DirectionBackward);
        run("countdown=2 → exits forward",           KnotRunnerOscillationTest::testCountdownTwo_DirectionForward);
        run("countdown=3 → exits backward",          KnotRunnerOscillationTest::testCountdownThree_DirectionBackward);
        run("oscillation terminates (countdown=5)",  KnotRunnerOscillationTest::testOscillationTerminates);
        run("empty body + false cond → no crash",    KnotRunnerOscillationTest::testNoBrackets_EmptyBodyNoThrow);
        run("setup item fires once at construction", KnotRunnerOscillationTest::testSetupItemRunsOnceDuringConstruction);
        run("setup item once with oscillations",     KnotRunnerOscillationTest::testSetupItemRunsOnceWithOscillation);
        run("runTonk same result as runKnot",        KnotRunnerOscillationTest::testRunTonkProducesSameResult);
        run("runTonk same direction as runKnot",     KnotRunnerOscillationTest::testRunTonkWithOscillationSameDirection);

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        System.exit(failed == 0 ? 0 : 1);
    }

    @FunctionalInterface interface Throwing { void run() throws Exception; }
    private static void run(String name, Throwing t) {
        try { t.run(); }
        catch (Exception e) {
            System.out.println("  ERROR in " + name + ": " + e);
            e.printStackTrace(System.out);
            failed++;
        }
    }
}
