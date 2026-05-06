package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Expr;
import Parser.Stmt;

/**
 * Tests for KnotRunner edge-driven traversal: UNWIND body-skip, ENTRY body-skip,
 * priorEdge tracking, and cascading UNWIND chains.
 *
 * Traversal rules under test:
 *
 *   UNWIND region (same-family CLOSE..CLOSE):
 *     Forward: UNWIND edge fires at left CLOSE → jumps to enclosing pair's CLOSE.
 *       Body items between the two CLOSEs are NEVER visited.
 *     Backward: UNWIND edge fires at right CLOSE → jumps to enclosing pair's OPEN.
 *       Body items in the same span are NEVER visited from behind either.
 *
 *   ENTRY region (same-family OPEN..OPEN):
 *     Forward: ENTRY edge fires at left OPEN → jumps to nearest nested pair's OPEN.
 *       Body items in the ENTRY span are SKIPPED on the forward pass.
 *     Backward: BA edge arrives at right OPEN, then walk continues left past body items.
 *       Body items in the ENTRY span ARE collected on the backward pass.
 *
 *   priorEdge tracking:
 *     Set on every selectStep return; null after forward boundary flip; CONDITION_FALSE
 *     when condition is false-exited.
 *
 * Fixtures
 * --------
 *   unwindFixture: [Literal("A"), p(, q(, )q, Literal("skip"), )p]
 *     Indices:       0             1   2   3   4                 5
 *     Region [3,5] = UNWIND.  "skip" at 4 never collected.
 *
 *   entryFixture: [bidir("B"), p(, bidir("gap"), q(, )q, )p]
 *     Indices:      0           1   2              3   4   5
 *     Region [1,3] = POCKET_ENTRY.  "gap" at 2 skipped forward, collected backward.
 *     Body stmts are bidirectional (noisserpxe != null) so they return in both directions.
 *
 *   cascadeFixture: [Literal("out"), p(, q(, r(, )r, Literal("skipA"), )q, Literal("skipB"), )p]
 *     Indices:        0               1   2   3   4   5                  6   7                  8
 *     Region [4,6] = UNWIND ("skipA" at 5 never collected).
 *     Region [6,8] = UNWIND ("skipB" at 7 never collected).
 *     Forward: ENTRY at 1→3, UNWIND 4→6, UNWIND 6→8, boundary flip.
 *     Backward: UNWIND 6→1, walk to 0.
 */
public class KnotRunnerFullTraversalTest {

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
    // Token / statement helpers
    // -------------------------------------------------------------------------

    private static Token tok(TokenType type, String lex) {
        return new Token(type, lex, null, null, null, 0, 0, 0, 0);
    }

    /** Stmt whose result is visible on the FORWARD pass only (noisserpxe=null). */
    private static Stmt.Expression fwd(Object val) {
        return new Stmt.Expression(new Expr.Literal(val), null);
    }

    /** Stmt whose result is visible on BOTH passes (same Literal used both ways). */
    private static Stmt.Expression bidir(Object val) {
        Expr e = new Expr.Literal(val);
        return new Stmt.Expression(e, e);
    }

    private static Stmt.Expression pocketOpen(String label) {
        return new Stmt.Expression(new Expr.PocketOpen(tok(TokenType.OPENPAREN, label + "(")), null);
    }

    private static Stmt.Expression pocketClose(String label) {
        return new Stmt.Expression(new Expr.PocketClosed(tok(TokenType.CLOSEDPAREN, ")" + label)), null);
    }

    private static Stmt.Expression cupOpen(String label) {
        return new Stmt.Expression(new Expr.CupOpen(tok(TokenType.OPENBRACE, label + "{")), null);
    }

    private static Stmt.Expression cupClose(String label) {
        return new Stmt.Expression(new Expr.CupClosed(tok(TokenType.CLOSEDBRACE, "}" + label)), null);
    }

    private static Interpreter fwdInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    /** Expr that evaluates to true N times then false (shared across calls). */
    private static Expr countdown(int n) {
        final int[] rem = {n};
        return new Expr() {
            @Override public <R> R accept(Declaration.Visitor<R> v) {
                return (R) Boolean.valueOf(rem[0]-- > 0);
            }
            @Override public void reverse() {}
        };
    }

    // -------------------------------------------------------------------------
    // KNOT fixture (from KnotRunnerOscillationTest pattern, for regression +
    // priorEdge tests)
    //   0: Literal(bodyVal)
    //   1: CupOpen("a{")
    //   2: PocketOpen("x(")
    //   3: condExpr
    //   4: CupClosed("}a")
    //   5: PocketClosed(")x")
    // -------------------------------------------------------------------------

    private static List<Stmt> knotFixture(Object bodyVal, Expr condExpr) {
        List<Stmt> s = new ArrayList<>();
        s.add(fwd(bodyVal));
        s.add(cupOpen("a"));
        s.add(pocketOpen("x"));
        s.add(new Stmt.Expression(condExpr, null));
        s.add(cupClose("a"));
        s.add(pocketClose("x"));
        return s;
    }

    // -------------------------------------------------------------------------
    // UNWIND fixture
    //   0: Literal("A")           — body, forward-only
    //   1: PocketOpen("p(")       — node at idx 1
    //   2: PocketOpen("q(")       — node at idx 2
    //   3: PocketClosed(")q")     — node at idx 3  (UNWIND region starts here)
    //   4: Literal("skip")        — inside UNWIND region [3..5]
    //   5: PocketClosed(")p")     — node at idx 5  (UNWIND region ends here)
    //
    // ENTRY fires at node1 (POCKET_ENTRY region [1,2]) → jumps to node2 (idx 2).
    // UNWIND fires at node2 (region [3,5]) → jumps to node3 (idx 5).
    // Boundary flip at idx 5. Backward: BA→3→2→1→0(A).
    // "skip" is never in the traversal path.
    // -------------------------------------------------------------------------

    private static List<Stmt> unwindFixture() {
        List<Stmt> s = new ArrayList<>();
        s.add(bidir("A"));      // 0
        s.add(pocketOpen("p")); // 1
        s.add(pocketOpen("q")); // 2
        s.add(pocketClose("q")); // 3
        s.add(fwd("skip"));     // 4
        s.add(pocketClose("p")); // 5
        return s;
    }

    // -------------------------------------------------------------------------
    // ENTRY fixture
    //   0: bidir("B")            — collected forward AND backward
    //   1: PocketOpen("p(")      — node at idx 1  (POCKET_ENTRY region starts here)
    //   2: bidir("gap")          — inside ENTRY region [1..3], skipped forward
    //   3: PocketOpen("q(")      — node at idx 3  (POCKET_ENTRY region ends here)
    //   4: PocketClosed(")q")    — node at idx 4  (UNWIND region [4..5] starts here)
    //   5: PocketClosed(")p")    — node at idx 5
    //
    // ENTRY fires at node1 (POCKET_ENTRY [1,3]) → jumps to node2 (idx 3).
    // Walk continues: node2 → node3 (UNWIND [4,5]) → UNWIND → idx 5.
    // Boundary flip. Backward: BA→4→3→2("gap")→1→0("B").
    // -------------------------------------------------------------------------

    private static List<Stmt> entryFixture() {
        List<Stmt> s = new ArrayList<>();
        s.add(bidir("B"));       // 0
        s.add(pocketOpen("p"));  // 1
        s.add(bidir("gap"));     // 2
        s.add(pocketOpen("q"));  // 3
        s.add(pocketClose("q")); // 4
        s.add(pocketClose("p")); // 5
        return s;
    }

    // -------------------------------------------------------------------------
    // Cascading UNWIND fixture (3-level nesting)
    //   0: Literal("out")       — body
    //   1: PocketOpen("p(")     — node at idx 1
    //   2: PocketOpen("q(")     — node at idx 2
    //   3: PocketOpen("r(")     — node at idx 3
    //   4: PocketClosed(")r")   — node at idx 4  (first UNWIND [4..6] starts)
    //   5: Literal("skipA")     — inside first UNWIND region
    //   6: PocketClosed(")q")   — node at idx 6  (first UNWIND ends / second [6..8] starts)
    //   7: Literal("skipB")     — inside second UNWIND region
    //   8: PocketClosed(")p")   — node at idx 8  (second UNWIND ends)
    //
    // Forward: ENTRY at 1→3 (innermost "r"), then UNWIND 4→6, UNWIND 6→8, boundary flip.
    // Backward: UNWIND at 6 → node0 (idx 1), walk 1→0(out), exit.
    // "skipA" and "skipB" are never visited.
    // -------------------------------------------------------------------------

    private static List<Stmt> cascadeFixture() {
        List<Stmt> s = new ArrayList<>();
        s.add(bidir("out"));     // 0
        s.add(pocketOpen("p"));  // 1
        s.add(pocketOpen("q"));  // 2
        s.add(pocketOpen("r"));  // 3
        s.add(pocketClose("r")); // 4
        s.add(fwd("skipA"));     // 5
        s.add(pocketClose("q")); // 6
        s.add(fwd("skipB"));     // 7
        s.add(pocketClose("p")); // 8
        return s;
    }

    // -------------------------------------------------------------------------
    // UNWIND tests
    // -------------------------------------------------------------------------

    private static void testUnwindBodyNotCollected() {
        System.out.println("--- UNWIND: body in UNWIND region never collected ---");
        Interpreter interp = fwdInterp();
        ArrayList<Object> result = new KnotRunner(null, unwindFixture(), interp).runKnot();
        check("'skip' absent from result", !result.contains("skip"));
    }

    private static void testUnwindResultContainsBody() {
        System.out.println("--- UNWIND: outer body item 'A' collected twice ---");
        Interpreter interp = fwdInterp();
        ArrayList<Object> result = new KnotRunner(null, unwindFixture(), interp).runKnot();
        eq("result size = 2", 2, result.size());
        eq("result[0] = A", "A", result.get(0));
        eq("result[1] = A", "A", result.get(1));
    }

    private static void testUnwindPriorEdgeNull() {
        System.out.println("--- UNWIND: priorEdge is null after boundary flip traversal ---");
        Interpreter interp = fwdInterp();
        KnotRunner runner = new KnotRunner(null, unwindFixture(), interp);
        runner.runKnot();
        check("priorEdge null after unwind traversal", runner.getPriorEdge() == null);
    }

    private static void testUnwindExitsBackward() {
        System.out.println("--- UNWIND: traversal exits in backward direction ---");
        Interpreter interp = fwdInterp();
        new KnotRunner(null, unwindFixture(), interp).runKnot();
        check("direction = backward after unwind traversal", !interp.isForward());
    }

    // -------------------------------------------------------------------------
    // ENTRY tests
    // -------------------------------------------------------------------------

    private static void testEntryGapCollectedOnce() {
        System.out.println("--- ENTRY: 'gap' collected exactly once (backward pass) ---");
        Interpreter interp = fwdInterp();
        ArrayList<Object> result = new KnotRunner(null, entryFixture(), interp).runKnot();
        long gapCount = result.stream().filter(o -> "gap".equals(o)).count();
        eq("'gap' appears exactly once", 1L, gapCount);
    }

    private static void testEntryResultOrder() {
        System.out.println("--- ENTRY: result order is [B, gap, B] ---");
        Interpreter interp = fwdInterp();
        ArrayList<Object> result = new KnotRunner(null, entryFixture(), interp).runKnot();
        eq("result size = 3", 3, result.size());
        eq("result[0] = B",   "B",   result.get(0));
        eq("result[1] = gap", "gap", result.get(1));
        eq("result[2] = B",   "B",   result.get(2));
    }

    private static void testEntryGapAfterFirstBody() {
        System.out.println("--- ENTRY: 'gap' appears after first 'B' (confirms backward collection) ---");
        Interpreter interp = fwdInterp();
        ArrayList<Object> result = new KnotRunner(null, entryFixture(), interp).runKnot();
        int firstB   = result.indexOf("B");
        int gapIndex = result.indexOf("gap");
        check("gap collected after first B", gapIndex > firstB);
    }

    // -------------------------------------------------------------------------
    // priorEdge tests (using knotFixture + countdown)
    // -------------------------------------------------------------------------

    private static void testPriorEdgeKindConditionFalse() {
        System.out.println("--- priorEdge: kind = CONDITION_FALSE after countdown(2) ---");
        Interpreter interp = fwdInterp();
        KnotRunner runner = new KnotRunner(null, knotFixture("body", countdown(2)), interp);
        runner.runKnot();
        TraversalEdge pe = runner.getPriorEdge();
        check("priorEdge not null", pe != null);
        if (pe != null) eq("priorEdge.kind = CONDITION_FALSE", EdgeKind.CONDITION_FALSE, pe.kind);
    }

    private static void testPriorEdgeFromNodeIndex() {
        System.out.println("--- priorEdge: from.expressionIndex = 4 (CupClosed) after countdown(2) ---");
        Interpreter interp = fwdInterp();
        KnotRunner runner = new KnotRunner(null, knotFixture("body", countdown(2)), interp);
        runner.runKnot();
        TraversalEdge pe = runner.getPriorEdge();
        if (pe != null) eq("priorEdge.from.expressionIndex = 4", 4, pe.from.expressionIndex);
        else check("priorEdge.from.expressionIndex = 4 — priorEdge was null", false);
    }

    private static void testDirectionForwardAfterConditionFalse() {
        System.out.println("--- priorEdge: direction = forward after CONDITION_FALSE exit (countdown=2) ---");
        Interpreter interp = fwdInterp();
        new KnotRunner(null, knotFixture("body", countdown(2)), interp).runKnot();
        check("direction = forward after countdown(2)", interp.isForward());
    }

    private static void testPriorEdgeNullInitiallyOnFreshRunner() {
        System.out.println("--- priorEdge: null before runKnot() is called ---");
        Interpreter interp = fwdInterp();
        KnotRunner runner = new KnotRunner(null, unwindFixture(), interp);
        check("priorEdge null on fresh runner", runner.getPriorEdge() == null);
    }

    // -------------------------------------------------------------------------
    // Cascading UNWIND tests
    // -------------------------------------------------------------------------

    private static void testCascadeSkipANotCollected() {
        System.out.println("--- cascade UNWIND: 'skipA' never collected ---");
        Interpreter interp = fwdInterp();
        ArrayList<Object> result = new KnotRunner(null, cascadeFixture(), interp).runKnot();
        check("'skipA' absent from result", !result.contains("skipA"));
    }

    private static void testCascadeSkipBNotCollected() {
        System.out.println("--- cascade UNWIND: 'skipB' never collected ---");
        Interpreter interp = fwdInterp();
        ArrayList<Object> result = new KnotRunner(null, cascadeFixture(), interp).runKnot();
        check("'skipB' absent from result", !result.contains("skipB"));
    }

    private static void testCascadeResult() {
        System.out.println("--- cascade UNWIND: result is ['out', 'out'] ---");
        Interpreter interp = fwdInterp();
        ArrayList<Object> result = new KnotRunner(null, cascadeFixture(), interp).runKnot();
        eq("result size = 2", 2, result.size());
        eq("result[0] = out", "out", result.get(0));
        eq("result[1] = out", "out", result.get(1));
    }

    private static void testCascadePriorEdgeIsUnwind() {
        System.out.println("--- cascade UNWIND: priorEdge is backward UNWIND edge after traversal ---");
        // Backward UNWIND fires at node4 (idx=6) → jumps to node0 (idx=1).
        // This is the last control action, so priorEdge = that UNWIND edge.
        Interpreter interp = fwdInterp();
        KnotRunner runner = new KnotRunner(null, cascadeFixture(), interp);
        runner.runKnot();
        TraversalEdge pe = runner.getPriorEdge();
        check("priorEdge not null (set by backward UNWIND)", pe != null);
        if (pe != null) {
            eq("priorEdge.kind = UNWIND", EdgeKind.UNWIND, pe.kind);
            eq("priorEdge.from.expressionIndex = 6", 6, pe.from.expressionIndex);
            eq("priorEdge.to.expressionIndex = 1",   1, pe.to.expressionIndex);
        }
    }

    // -------------------------------------------------------------------------
    // ENTRY targets the innermost nested pair (smallest span), not the outermost
    // -------------------------------------------------------------------------

    private static void testEntryTargetsInnermostPair() {
        System.out.println("--- ENTRY: forward ENTRY jumps to innermost nested OPEN, not outermost ---");
        // Structure: p( q( r( )r )q )p
        // ENTRY at p( should jump to r( (smallest span), skipping q(.
        // Verify by checking result doesn't contain any item placed between p( and r(.
        List<Stmt> stmts = new ArrayList<>();
        stmts.add(fwd("before"));   // 0
        stmts.add(pocketOpen("p")); // 1 — ENTRY here
        stmts.add(fwd("gap1"));     // 2 — in POCKET_ENTRY region [1,3]
        stmts.add(pocketOpen("q")); // 3 — node
        stmts.add(fwd("gap2"));     // 4 — in POCKET_ENTRY region [3,5]
        stmts.add(pocketOpen("r")); // 5 — innermost open (target of ENTRY)
        stmts.add(pocketClose("r")); // 6
        stmts.add(pocketClose("q")); // 7
        stmts.add(pocketClose("p")); // 8
        Interpreter interp = fwdInterp();
        ArrayList<Object> result = new KnotRunner(null, stmts, interp).runKnot();
        check("'gap1' not collected on forward pass (skipped by ENTRY)", !result.contains("gap1"));
    }

    // -------------------------------------------------------------------------
    // Regression: standard KNOT oscillation still works with edge-driven traversal
    // -------------------------------------------------------------------------

    private static void testRegressionCountdown0ExitsForward() {
        System.out.println("--- regression: countdown=0 → exits forward ---");
        Interpreter interp = fwdInterp();
        new KnotRunner(null, knotFixture("x", countdown(0)), interp).runKnot();
        check("direction = forward after countdown(0)", interp.isForward());
    }

    private static void testRegressionCountdown1ExitsBackward() {
        System.out.println("--- regression: countdown=1 → exits backward ---");
        Interpreter interp = fwdInterp();
        new KnotRunner(null, knotFixture("x", countdown(1)), interp).runKnot();
        check("direction = backward after countdown(1)", !interp.isForward());
    }

    private static void testRegressionBodyCollectedOnce() {
        System.out.println("--- regression: body item collected exactly once regardless of oscillation ---");
        Interpreter interp = fwdInterp();
        ArrayList<Object> result =
            new KnotRunner(null, knotFixture("unique", countdown(4)), interp).runKnot();
        eq("result size = 1", 1, result.size());
        eq("result[0] = unique", "unique", result.get(0));
    }

    private static void testRegressionCountdown2ExitsForward() {
        System.out.println("--- regression: countdown=2 → exits forward ---");
        Interpreter interp = fwdInterp();
        new KnotRunner(null, knotFixture("x", countdown(2)), interp).runKnot();
        check("direction = forward after countdown(2)", interp.isForward());
    }

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== KnotRunnerFullTraversalTest ===\n");

        run("UNWIND body not collected",              KnotRunnerFullTraversalTest::testUnwindBodyNotCollected);
        run("UNWIND result contains body twice",      KnotRunnerFullTraversalTest::testUnwindResultContainsBody);
        run("UNWIND priorEdge null after traversal",  KnotRunnerFullTraversalTest::testUnwindPriorEdgeNull);
        run("UNWIND exits backward",                  KnotRunnerFullTraversalTest::testUnwindExitsBackward);
        run("ENTRY gap collected once",               KnotRunnerFullTraversalTest::testEntryGapCollectedOnce);
        run("ENTRY result order [B,gap,B]",           KnotRunnerFullTraversalTest::testEntryResultOrder);
        run("ENTRY gap after first body item",        KnotRunnerFullTraversalTest::testEntryGapAfterFirstBody);
        run("priorEdge kind = CONDITION_FALSE",       KnotRunnerFullTraversalTest::testPriorEdgeKindConditionFalse);
        run("priorEdge from.expressionIndex = 4",     KnotRunnerFullTraversalTest::testPriorEdgeFromNodeIndex);
        run("direction forward after CONDITION_FALSE",KnotRunnerFullTraversalTest::testDirectionForwardAfterConditionFalse);
        run("priorEdge null on fresh runner",         KnotRunnerFullTraversalTest::testPriorEdgeNullInitiallyOnFreshRunner);
        run("cascade skipA not collected",            KnotRunnerFullTraversalTest::testCascadeSkipANotCollected);
        run("cascade skipB not collected",            KnotRunnerFullTraversalTest::testCascadeSkipBNotCollected);
        run("cascade result [out,out]",               KnotRunnerFullTraversalTest::testCascadeResult);
        run("cascade priorEdge is backward UNWIND",   KnotRunnerFullTraversalTest::testCascadePriorEdgeIsUnwind);
        run("ENTRY targets innermost pair",           KnotRunnerFullTraversalTest::testEntryTargetsInnermostPair);
        run("regression countdown=0 forward",        KnotRunnerFullTraversalTest::testRegressionCountdown0ExitsForward);
        run("regression countdown=1 backward",       KnotRunnerFullTraversalTest::testRegressionCountdown1ExitsBackward);
        run("regression body collected once",        KnotRunnerFullTraversalTest::testRegressionBodyCollectedOnce);
        run("regression countdown=2 forward",        KnotRunnerFullTraversalTest::testRegressionCountdown2ExitsForward);

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
