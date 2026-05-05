package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Expr;
import Parser.Stmt;

/**
 * Rolling average computed by knot oscillation.
 *
 * The condition expression has two modes selected by the interpreter's
 * direction state at evaluation time:
 *
 *   Forward pass (stepForwardControl at PocketOpen):
 *     sum += nums[step]; step++; return true
 *
 *   Backward pass (stepBackwardControl at CupOpen):
 *     avgs.add(sum / step); return step < nums.length
 *
 * For nums = [2, 4, 6, 8, 10], five oscillations collect running averages
 * [2.0, 3.0, 4.0, 5.0, 6.0] — each backward pass locks in the average so far,
 * and the sequence converges to the true mean of 6.0.
 *
 * The oscillation is the computation. The knot doesn't loop over elements;
 * each direction flip IS one element's contribution being folded in.
 *
 * Fixture layout (KNOT shape — no body item):
 *
 *   [0] CupOpen("a{")        — outer-left bracket; backward-condition false-exit
 *   [1] PocketOpen("x(")     — backward-condition start (checked on forward pass too)
 *   [2] condExpr             — direction-sensitive: accumulate fwd, report bwd
 *   [3] CupClosed("}a")      — condition true target
 *   [4] PocketClosed(")x")   — condition false target
 *
 * Oscillation control flow (KNOT = { ( } ) shape):
 *
 *   Forward at PocketOpen(1):
 *     backwardConditionStartForTrueEntry fires → checkConditionRangeBackward(3,1) → condExpr[fwd]
 *     true  → flip backward, count = 0
 *     (never false on forward — forward always returns true while elements remain)
 *
 *   Backward at CupOpen(0):
 *     backwardConditionStartForFalseExit fires → checkConditionRangeBackward(3,1) → condExpr[bwd]
 *     true  → flip forward, count = 1
 *     false → return -1 → EXIT backward (when step == nums.length after last report)
 */
public class RollingAverageTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean cond) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else       { System.out.println("  FAIL  " + name); failed++; }
    }

    private static void eq(String name, Object expected, Object actual) {
        boolean ok = (expected == null) ? (actual == null) : expected.equals(actual);
        if (ok) { System.out.println("  PASS  " + name); passed++; }
        else    { System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual); failed++; }
    }

    private static Token tok(TokenType type, String lex) {
        return new Token(type, lex, null, null, null, 0, 0, 0, 0);
    }

    private static Stmt.Expression exprStmt(Expr e) {
        return new Stmt.Expression(e, null);
    }

    private static Interpreter makeInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    /**
     * Build the knot statement list for a rolling average over {@code nums}.
     *
     * {@code step}, {@code sum}, and {@code avgs} are mutable state shared
     * between the condition expression and the calling test so results can be
     * inspected after runKnot() returns.
     *
     * Direction is read from {@code interp} inside the condition expression:
     *   forward  → accumulate one element into sum and advance step
     *   backward → record the current running average into avgs
     */
    private static List<Stmt> rollingAverageKnot(
            double[] nums, int[] step, double[] sum,
            List<Double> avgs, Interpreter interp) {

        Expr condExpr = new Expr() {
            @Override
            public <R> R accept(Declaration.Visitor<R> visitor) {
                if (interp.isForward()) {
                    sum[0] += nums[step[0]];
                    step[0]++;
                    return (R) Boolean.TRUE;
                } else {
                    avgs.add(sum[0] / step[0]);
                    return (R) Boolean.valueOf(step[0] < nums.length);
                }
            }
            @Override public void reverse() {}
        };

        List<Stmt> stmts = new ArrayList<>();
        stmts.add(exprStmt(new Expr.CupOpen    (tok(TokenType.OPENBRACE,   "a{"))));
        stmts.add(exprStmt(new Expr.PocketOpen  (tok(TokenType.OPENPAREN,   "x("))));
        stmts.add(exprStmt(condExpr));
        stmts.add(exprStmt(new Expr.CupClosed  (tok(TokenType.CLOSEDBRACE, "}a"))));
        stmts.add(exprStmt(new Expr.PocketClosed(tok(TokenType.CLOSEDPAREN, ")x"))));
        return stmts;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    private static void testRunningAveragesConvergeToMean() {
        System.out.println("--- five elements: running avgs converge to mean ---");
        Interpreter interp = makeInterp();
        double[] nums = {2.0, 4.0, 6.0, 8.0, 10.0};  // mean = 6.0
        int[]    step = {0};
        double[] sum  = {0.0};
        List<Double> avgs = new ArrayList<>();

        new KnotRunner(null, rollingAverageKnot(nums, step, sum, avgs, interp), interp)
                .runKnot();

        eq("five running averages collected",   5,   avgs.size());
        eq("avg after 1 element  = 2.0",        2.0, avgs.get(0));
        eq("avg after 2 elements = 3.0",        3.0, avgs.get(1));
        eq("avg after 3 elements = 4.0",        4.0, avgs.get(2));
        eq("avg after 4 elements = 5.0",        5.0, avgs.get(3));
        eq("avg after 5 elements = 6.0 (mean)", 6.0, avgs.get(4));
    }

    private static void testFinalAverageEqualsTrueMean() {
        System.out.println("--- final avg == arithmetic mean of the dataset ---");
        Interpreter interp = makeInterp();
        double[] nums = {1.0, 3.0, 5.0, 7.0, 9.0};  // mean = 5.0
        int[]    step = {0};
        double[] sum  = {0.0};
        List<Double> avgs = new ArrayList<>();

        new KnotRunner(null, rollingAverageKnot(nums, step, sum, avgs, interp), interp)
                .runKnot();

        double trueMean = 0;
        for (double v : nums) trueMean += v;
        trueMean /= nums.length;

        eq("five averages collected", 5, avgs.size());
        check("final avg == true mean (within eps)",
              Math.abs(avgs.get(avgs.size() - 1) - trueMean) < 1e-10);
    }

    private static void testSymmetricDataset() {
        System.out.println("--- all-equal dataset: every running avg equals the constant ---");
        Interpreter interp = makeInterp();
        double[] nums = {7.0, 7.0, 7.0, 7.0};
        int[]    step = {0};
        double[] sum  = {0.0};
        List<Double> avgs = new ArrayList<>();

        new KnotRunner(null, rollingAverageKnot(nums, step, sum, avgs, interp), interp)
                .runKnot();

        eq("four averages", 4, avgs.size());
        for (int i = 0; i < avgs.size(); i++)
            eq("avg[" + i + "] == 7.0", 7.0, avgs.get(i));
    }

    private static void testMonotonicConvergenceTowardMean() {
        System.out.println("--- sorted ascending input: distance to mean shrinks each oscillation ---");
        Interpreter interp = makeInterp();
        double[] nums = {2.0, 4.0, 6.0, 8.0, 10.0};
        int[]    step = {0};
        double[] sum  = {0.0};
        List<Double> avgs = new ArrayList<>();

        new KnotRunner(null, rollingAverageKnot(nums, step, sum, avgs, interp), interp)
                .runKnot();

        double mean = 6.0;
        for (int i = 0; i < avgs.size() - 1; i++) {
            double dThis = Math.abs(avgs.get(i)     - mean);
            double dNext = Math.abs(avgs.get(i + 1) - mean);
            check("|avg[" + i + "]-mean| >= |avg[" + (i + 1) + "]-mean|", dThis >= dNext);
        }
    }

    private static void testSingleElement() {
        System.out.println("--- single element: one oscillation, avg == element ---");
        Interpreter interp = makeInterp();
        double[] nums = {42.0};
        int[]    step = {0};
        double[] sum  = {0.0};
        List<Double> avgs = new ArrayList<>();

        new KnotRunner(null, rollingAverageKnot(nums, step, sum, avgs, interp), interp)
                .runKnot();

        eq("one average collected", 1,    avgs.size());
        eq("avg == 42.0",           42.0, avgs.get(0));
    }

    private static void testThreeElements() {
        System.out.println("--- three elements: verify each step of the running sum ---");
        Interpreter interp = makeInterp();
        double[] nums = {10.0, 20.0, 30.0};  // running avgs: 10, 15, 20; mean = 20
        int[]    step = {0};
        double[] sum  = {0.0};
        List<Double> avgs = new ArrayList<>();

        new KnotRunner(null, rollingAverageKnot(nums, step, sum, avgs, interp), interp)
                .runKnot();

        eq("three averages",      3,    avgs.size());
        eq("avg after 10",        10.0, avgs.get(0));
        eq("avg after 10+20",     15.0, avgs.get(1));
        eq("avg after 10+20+30",  20.0, avgs.get(2));
    }

    private static void testExitsBackward() {
        System.out.println("--- knot always exits backward (exit triggered by backward condition) ---");
        Interpreter interp = makeInterp();
        double[] nums = {1.0, 2.0, 3.0};
        int[]    step = {0};
        double[] sum  = {0.0};
        List<Double> avgs = new ArrayList<>();

        new KnotRunner(null, rollingAverageKnot(nums, step, sum, avgs, interp), interp)
                .runKnot();

        check("exits backward (last check is bwd condition returning false)", !interp.isForward());
        eq("three averages collected", 3, avgs.size());
    }

    private static void testAllElementsConsumed() {
        System.out.println("--- all elements processed: step == nums.length after exit ---");
        Interpreter interp = makeInterp();
        double[] nums = {5.0, 10.0, 15.0, 20.0};
        int[]    step = {0};
        double[] sum  = {0.0};
        List<Double> avgs = new ArrayList<>();

        new KnotRunner(null, rollingAverageKnot(nums, step, sum, avgs, interp), interp)
                .runKnot();

        eq("step advanced past all elements", nums.length, step[0]);
        eq("sum equals total of all elements", 50.0, sum[0]);
        eq("final avg == 12.5 (mean)", 12.5, avgs.get(avgs.size() - 1));
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== RollingAverageTest ===\n");

        run("running avgs converge to mean",        RollingAverageTest::testRunningAveragesConvergeToMean);
        run("final avg equals true mean",            RollingAverageTest::testFinalAverageEqualsTrueMean);
        run("symmetric dataset: all avgs equal",     RollingAverageTest::testSymmetricDataset);
        run("monotonic convergence toward mean",     RollingAverageTest::testMonotonicConvergenceTowardMean);
        run("single element",                        RollingAverageTest::testSingleElement);
        run("three elements step-by-step",           RollingAverageTest::testThreeElements);
        run("exits backward",                        RollingAverageTest::testExitsBackward);
        run("all elements consumed",                 RollingAverageTest::testAllElementsConsumed);

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
