package Box.Interpreter;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;
import Parser.Stmt;

import java.util.ArrayList;
import java.util.List;

/**
 * Standalone stress test for KnotAnalyzer, ControlGraph, and the condition
 * query methods used by KnotRunner.  No JUnit dependency — compile and run
 * directly with the classes in /tmp/knotbuild.
 *
 * Run:
 *   javac -cp <classpath>:/tmp/knotbuild  src/test/java/Box/Interpreter/KnotAnalyzerStressTest.java -d /tmp/knotbuild
 *   java  -cp <classpath>:/tmp/knotbuild  Box.Interpreter.KnotAnalyzerStressTest
 */
public class KnotAnalyzerStressTest {

    // -------------------------------------------------------------------------
    // Bracket statement builders
    // -------------------------------------------------------------------------

    private static Stmt pocketOpen(String label) {
        Token ctrl = new Token(TokenType.OPENPAREN, label + "(", null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.PocketOpen(ctrl), null);
    }

    private static Stmt pocketClose(String label) {
        Token ctrl = new Token(TokenType.CLOSEDPAREN, ")" + label, null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.PocketClosed(ctrl), null);
    }

    private static Stmt cupOpen(String label) {
        Token ctrl = new Token(TokenType.OPENBRACE, label + "{", null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.CupOpen(ctrl), null);
    }

    private static Stmt cupClose(String label) {
        Token ctrl = new Token(TokenType.CLOSEDBRACE, "}" + label, null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.CupClosed(ctrl), null);
    }

    // -------------------------------------------------------------------------
    // Assertion helpers
    // -------------------------------------------------------------------------

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
    // Tests
    // -------------------------------------------------------------------------

    /** Empty expression list → AMBIGUOUS, no nodes, no regions. */
    private static void testEmpty() {
        System.out.println("--- empty expression ---");
        ControlGraph g = new KnotAnalyzer(List.of()).analyze();
        eq("runtimeKind", RuntimeKind.AMBIGUOUS, g.runtimeKind);
        eq("nodeCount",   0, g.getNodes().size());
        eq("regionCount", 0, g.getRegions().size());
        eq("pairCount",   0, g.getMatchTable().getAllPairs().size());
    }

    /** {lkjhg )ghjkl — two nodes, outer shell MIXED + FORWARD_BIASED → KNOT */
    private static void testKnotClassification() {
        System.out.println("--- KNOT classification ---");
        List<Stmt> stmts = List.of(cupOpen("lkjhg"), pocketClose("ghjkl"));
        ControlGraph g = new KnotAnalyzer(stmts).analyze();
        eq("outerShell",   OuterShell.MIXED,          g.outerShell);
        eq("orientation",  Orientation.FORWARD_BIASED, g.orientation);
        eq("topology",     Topology.ORDINARY,          g.topology);
        eq("runtimeKind",  RuntimeKind.KNOT,           g.runtimeKind);
        eq("nodeCount",    2,                           g.getNodes().size());
        eq("regionCount",  1,                           g.getRegions().size());
        // region (0,1): { ) = CONDITION
        eq("region kind",  RegionKind.CONDITION, g.getRegions().get(0).regionKind);
    }

    /** (asdfg }gfdsa — MIXED + BACKWARD_BIASED → TONK */
    private static void testTonkClassification() {
        System.out.println("--- TONK classification ---");
        List<Stmt> stmts = List.of(pocketOpen("asdfg"), cupClose("gfdsa"));
        ControlGraph g = new KnotAnalyzer(stmts).analyze();
        eq("outerShell",  OuterShell.MIXED,           g.outerShell);
        eq("orientation", Orientation.BACKWARD_BIASED, g.orientation);
        eq("runtimeKind", RuntimeKind.TONK,            g.runtimeKind);
        // region (0,1): ( } = CONDITION
        eq("region kind", RegionKind.CONDITION, g.getRegions().get(0).regionKind);
    }

    /** (hello )olleh — POCKET_SHAPED, one matched pair */
    private static void testPocketClassification() {
        System.out.println("--- POCKET classification ---");
        List<Stmt> stmts = List.of(pocketOpen("hello"), pocketClose("olleh"));
        ControlGraph g = new KnotAnalyzer(stmts).analyze();
        eq("outerShell",  OuterShell.POCKET_SHAPED, g.outerShell);
        eq("runtimeKind", RuntimeKind.POCKET,        g.runtimeKind);
        eq("topology",    Topology.ORDINARY,          g.topology);
        eq("pairCount",   1, g.getMatchTable().getAllPairs().size());
        MatchPair pair = g.getMatchTable().getAllPairs().get(0);
        eq("pair openIndex",  0, pair.openIndex());
        eq("pair closeIndex", 1, pair.closeIndex());
        eq("pair family", ControlFamily.POCKET, pair.family);
        // region (0,1): ( ) — not in base table → AMBIGUOUS (POCKET_ENTRY is ( ( )
        eq("region kind", RegionKind.AMBIGUOUS, g.getRegions().get(0).regionKind);
    }

    /** {hello }olleh — CUP_SHAPED */
    private static void testCupClassification() {
        System.out.println("--- CUP classification ---");
        List<Stmt> stmts = List.of(cupOpen("hello"), cupClose("olleh"));
        ControlGraph g = new KnotAnalyzer(stmts).analyze();
        eq("outerShell",  OuterShell.CUP_SHAPED, g.outerShell);
        eq("runtimeKind", RuntimeKind.CUP,        g.runtimeKind);
        eq("pairCount",   1, g.getMatchTable().getAllPairs().size());
        // region (0,1): { } — not in base table → AMBIGUOUS (CUP_ENTRY is { { )
        eq("region kind", RegionKind.AMBIGUOUS, g.getRegions().get(0).regionKind);
    }

    /**
     * {abc (xyz }cba )zyx — minimal KNOT with one CONDITION region.
     *
     * indices:  0={abc   1=(xyz   2=}cba   3=)zyx
     * pairs:    {abc(0) ↔ }cba(2),   (xyz(1) ↔ )zyx(3)
     * regions:  (0,1) { ( = SETUP
     *           (1,2) ( } = CONDITION
     *           (2,3) } ) = BODY (BIAS_EXIT)
     */
    private static void testConditionEdges() {
        System.out.println("--- condition edges (minimal KNOT) ---");
        List<Stmt> stmts = List.of(
            cupOpen("abc"),     // 0
            pocketOpen("xyz"),  // 1
            cupClose("cba"),    // 2
            pocketClose("zyx")  // 3
        );
        ControlGraph g = new KnotAnalyzer(stmts).analyze();

        eq("runtimeKind", RuntimeKind.KNOT, g.runtimeKind);
        eq("pairCount",   2, g.getMatchTable().getAllPairs().size());

        List<ControlRegion> regions = g.getRegions();
        eq("regionCount", 3, regions.size());
        eq("region(0,1) SETUP",     RegionKind.SETUP,     regions.get(0).regionKind);
        eq("region(1,2) CONDITION", RegionKind.CONDITION, regions.get(1).regionKind);
        eq("region(2,3) BODY",      RegionKind.BODY,      regions.get(2).regionKind);

        // Forward condition at index 1 (PocketOpen, left boundary of CONDITION)
        check("hasForwardCondition(1)",        g.hasForwardCondition(1));
        eq("fwd trueTarget(1)",  2,            g.forwardConditionTrueTarget(1));
        eq("fwd falseTarget(1)", 3,            g.forwardConditionFalseTarget(1)); // matched partner of (xyz = )zyx at 3

        // Forward condition also reachable via the CLOSE partner (node 3 → matched open = node 1)
        check("hasForwardCondition(3) via partner",  g.hasForwardCondition(3));
        eq("fwd trueTarget(3) via partner",  2,      g.forwardConditionTrueTarget(3));
        eq("fwd falseTarget(3) via partner", 3,      g.forwardConditionFalseTarget(3));

        // Backward condition at index 2 (CupClose, right boundary of CONDITION)
        check("hasBackwardCondition(2)",       g.hasBackwardCondition(2));
        eq("bwd trueTarget(2)",  1,            g.backwardConditionTrueTarget(2));
        eq("bwd falseTarget(2)", 0,            g.backwardConditionFalseTarget(2)); // matched partner of }cba = {abc at 0

        // Backward condition also reachable via the OPEN partner (node 0 → matched close = node 2)
        check("hasBackwardCondition(0) via partner",  g.hasBackwardCondition(0));
        eq("bwd trueTarget(0) via partner",  1,       g.backwardConditionTrueTarget(0));
        eq("bwd falseTarget(0) via partner", 0,       g.backwardConditionFalseTarget(0));

        // Nodes that are NOT condition starts
        check("no fwd condition at 2", !g.hasForwardCondition(2));
        check("no bwd condition at 1", !g.hasBackwardCondition(1));
    }

    /**
     * TONK example from the design doc:
     *
     *   asdfg(  zxcvb(  qwerty(  poiuy{  lkjhg{  )bvcxz  )gfdsa  }yuiop  )ytrewq  }ghjkl
     *     0       1       2        3       4        5       6       7        8        9
     *
     * Pairs: (0,6) (1,5) (2,8) (3,7) (4,9)
     * Condition region: (4,5) = { ) = CONDITION
     * Forward cond at 4: true→5, false→9
     * Backward cond at 5: true→4, false→1
     */
    private static void testTonkDesignDocExample() {
        System.out.println("--- TONK design-doc example ---");
        List<Stmt> stmts = List.of(
            pocketOpen("asdfg"),   // 0
            pocketOpen("zxcvb"),   // 1
            pocketOpen("qwerty"),  // 2
            cupOpen("poiuy"),      // 3
            cupOpen("lkjhg"),      // 4
            pocketClose("bvcxz"),  // 5
            pocketClose("gfdsa"),  // 6
            cupClose("yuiop"),     // 7
            pocketClose("ytrewq"), // 8
            cupClose("ghjkl")      // 9
        );
        ControlGraph g = new KnotAnalyzer(stmts).analyze();

        eq("runtimeKind", RuntimeKind.TONK,             g.runtimeKind);
        eq("outerShell",  OuterShell.MIXED,              g.outerShell);
        eq("orientation", Orientation.BACKWARD_BIASED,   g.orientation);
        eq("nodeCount",   10,                             g.getNodes().size());
        eq("pairCount",   5,                              g.getMatchTable().getAllPairs().size());

        // Key match assertions
        MatchTable mt = g.getMatchTable();
        eq("match(1).expressionIndex", 5, mt.getMatch(g.getNodeAt(1)).expressionIndex); // zxcvb( ↔ )bvcxz
        eq("match(4).expressionIndex", 9, mt.getMatch(g.getNodeAt(4)).expressionIndex); // lkjhg{ ↔ }ghjkl
        eq("match(5).expressionIndex", 1, mt.getMatch(g.getNodeAt(5)).expressionIndex);
        eq("match(0).expressionIndex", 6, mt.getMatch(g.getNodeAt(0)).expressionIndex); // asdfg( ↔ )gfdsa

        // Condition region (4,5) = lkjhg{ ... )bvcxz = { ) = CONDITION
        List<ControlRegion> regions = g.getRegions();
        ControlRegion condRegion = null;
        for (ControlRegion r : regions) {
            if (r.leftControl.expressionIndex == 4 && r.rightControl.expressionIndex == 5) {
                condRegion = r; break;
            }
        }
        check("condRegion(4,5) found", condRegion != null);
        if (condRegion != null)
            eq("condRegion kind", RegionKind.CONDITION, condRegion.regionKind);

        // Forward condition at index 4 (CupOpen lkjhg{)
        check("hasForwardCondition(4)",        g.hasForwardCondition(4));
        eq("fwd trueTarget(4)",  5,             g.forwardConditionTrueTarget(4));
        eq("fwd falseTarget(4)", 9,             g.forwardConditionFalseTarget(4));

        // Forward condition also accessible via matched close of 4 = node 9 (}ghjkl)
        check("hasForwardCondition(9) via partner",  g.hasForwardCondition(9));
        eq("fwd trueTarget(9)",  5,                   g.forwardConditionTrueTarget(9));
        eq("fwd falseTarget(9)", 9,                   g.forwardConditionFalseTarget(9));

        // Backward condition at index 5 (PocketClose )bvcxz)
        check("hasBackwardCondition(5)",       g.hasBackwardCondition(5));
        eq("bwd trueTarget(5)",  4,             g.backwardConditionTrueTarget(5));
        eq("bwd falseTarget(5)", 1,             g.backwardConditionFalseTarget(5));

        // Backward condition accessible via matched open of 5 = node 1 (zxcvb()
        check("hasBackwardCondition(1) via partner",  g.hasBackwardCondition(1));
        eq("bwd trueTarget(1)",  4,                    g.backwardConditionTrueTarget(1));
        eq("bwd falseTarget(1)", 1,                    g.backwardConditionFalseTarget(1));

        // No condition on non-condition boundary nodes
        check("no fwd condition at 3", !g.hasForwardCondition(3));
        check("no bwd condition at 8", !g.hasBackwardCondition(8));
    }

    /**
     * (hello {world )olleh }dlrow — crossing pair, KNOTTED topology.
     *
     * Pair A = (0,2) POCKET, Pair B = (1,3) CUP → interleaved → KNOTTED.
     * Outer shell: PocketOpen(0) and CupClose(3) → MIXED BACKWARD_BIASED → TONK.
     */
    private static void testCrossing() {
        System.out.println("--- crossing detection ---");
        List<Stmt> stmts = List.of(
            pocketOpen("hello"),  // 0
            cupOpen("world"),     // 1
            pocketClose("olleh"), // 2
            cupClose("dlrow")     // 3
        );
        ControlGraph g = new KnotAnalyzer(stmts).analyze();

        eq("pairCount",    2,                  g.getMatchTable().getAllPairs().size());
        eq("crossingCount",1,                  g.getCrossings().size());
        eq("topology",     Topology.KNOTTED,   g.topology);
        eq("runtimeKind",  RuntimeKind.TONK,   g.runtimeKind);
        eq("crossingKind", CrossingKind.POCKET_OVER_CUP,
                           g.getCrossings().getCrossings().get(0).kind);
    }

    /**
     * Nested pockets — no crossings, ORDINARY topology.
     *
     *   (outer (inner )renni )retuo
     *     0      1      2      3
     */
    private static void testNestedPockets() {
        System.out.println("--- nested pockets ---");
        List<Stmt> stmts = List.of(
            pocketOpen("outer"),  // 0
            pocketOpen("inner"),  // 1
            pocketClose("renni"), // 2
            pocketClose("retuo")  // 3
        );
        ControlGraph g = new KnotAnalyzer(stmts).analyze();

        eq("pairCount",    2,                      g.getMatchTable().getAllPairs().size());
        eq("crossingCount",0,                      g.getCrossings().size());
        eq("topology",     Topology.ORDINARY,       g.topology);
        eq("runtimeKind",  RuntimeKind.POCKET,      g.runtimeKind);

        // All nodes MATCHED
        for (ControlNode node : g.getNodes())
            eq("matched[" + node.expressionIndex + "]", MatchResult.MATCHED, node.matchResult);

        // Regions: (0,1) ( ( = POCKET_ENTRY,  (1,2) ( ) = AMBIGUOUS,  (2,3) ) ) = UNWIND
        List<ControlRegion> regions = g.getRegions();
        eq("regionCount", 3, regions.size());
        eq("region(0,1)", RegionKind.POCKET_ENTRY, regions.get(0).regionKind);
        eq("region(1,2)", RegionKind.AMBIGUOUS,     regions.get(1).regionKind);
        eq("region(2,3)", RegionKind.UNWIND,        regions.get(2).regionKind);
    }

    /**
     * Multiple condition regions in one structure.
     *
     * Labels follow the reverse-match rule: reverse(openNorm) == closeNorm.
     * Self-palindromes ("aa", "bb") satisfy this trivially.
     *
     *   {aa (bb }aa )bb {cc (dd }cc )dd
     *    0   1   2   3   4   5   6   7
     *
     * Pairs: (0,2) CUP, (1,3) POCKET, (4,6) CUP, (5,7) POCKET
     * Region (0,1) { ( = SETUP
     * Region (1,2) ( } = CONDITION
     * Region (2,3) } ) = BODY (BIAS_EXIT)
     * Region (3,4) ) { = AMBIGUOUS (not listed in base table)
     * Region (4,5) { ( = SETUP
     * Region (5,6) ( } = CONDITION
     * Region (6,7) } ) = BODY (BIAS_EXIT)
     */
    private static void testMultipleConditions() {
        System.out.println("--- multiple condition regions ---");
        List<Stmt> stmts = List.of(
            cupOpen("aa"),     // 0  — matches }aa at 2 (reverse("aa")="aa")
            pocketOpen("bb"),  // 1  — matches )bb at 3 (reverse("bb")="bb")
            cupClose("aa"),    // 2
            pocketClose("bb"), // 3
            cupOpen("cc"),     // 4  — matches }cc at 6
            pocketOpen("dd"),  // 5  — matches )dd at 7
            cupClose("cc"),    // 6
            pocketClose("dd")  // 7
        );
        ControlGraph g = new KnotAnalyzer(stmts).analyze();

        List<ControlRegion> regions = g.getRegions();
        eq("regionCount",     7,                      regions.size());
        eq("region(0,1) SETUP",     RegionKind.SETUP,     regions.get(0).regionKind);
        eq("region(1,2) CONDITION", RegionKind.CONDITION, regions.get(1).regionKind);
        eq("region(2,3) BODY",      RegionKind.BODY,      regions.get(2).regionKind);
        // region(3,4) ) { is not in the base table → AMBIGUOUS
        eq("region(3,4) AMBIGUOUS", RegionKind.AMBIGUOUS, regions.get(3).regionKind);
        eq("region(4,5) SETUP",     RegionKind.SETUP,     regions.get(4).regionKind);
        eq("region(5,6) CONDITION", RegionKind.CONDITION, regions.get(5).regionKind);
        eq("region(6,7) BODY",      RegionKind.BODY,      regions.get(6).regionKind);

        // Both conditions are independently reachable
        check("hasForwardCondition(1)", g.hasForwardCondition(1));
        check("hasForwardCondition(5)", g.hasForwardCondition(5));
    }

    /**
     * Unmatched nodes are reported correctly.
     */
    private static void testUnmatchedNodes() {
        System.out.println("--- unmatched nodes ---");

        // Open with no close
        ControlGraph g1 = new KnotAnalyzer(List.of(pocketOpen("abc"))).analyze();
        eq("unmatched open result", MatchResult.UNMATCHED_OPEN,
                g1.getNodes().get(0).matchResult);
        eq("open pairCount", 0, g1.getMatchTable().getAllPairs().size());

        // Close with no open
        ControlGraph g2 = new KnotAnalyzer(List.of(pocketClose("xyz"))).analyze();
        eq("unmatched close result", MatchResult.UNMATCHED_CLOSE,
                g2.getNodes().get(0).matchResult);

        // Label mismatch — (abc )xyz — reverse("abc") = "cba" ≠ "xyz"
        ControlGraph g3 = new KnotAnalyzer(List.of(pocketOpen("abc"), pocketClose("xyz"))).analyze();
        eq("mismatch pairCount", 0, g3.getMatchTable().getAllPairs().size());
    }

    /** getNodeAt by expression index. */
    private static void testGetNodeAt() {
        System.out.println("--- getNodeAt ---");
        List<Stmt> stmts = List.of(pocketOpen("abc"), pocketClose("cba"));
        ControlGraph g = new KnotAnalyzer(stmts).analyze();

        check("getNodeAt(0) not null", g.getNodeAt(0) != null);
        check("getNodeAt(1) not null", g.getNodeAt(1) != null);
        check("getNodeAt(99) is null", g.getNodeAt(99) == null);
        eq("node(0) family",   ControlFamily.POCKET,    g.getNodeAt(0).family);
        eq("node(0) polarity", ControlPolarity.OPEN,    g.getNodeAt(0).polarity);
        eq("node(1) polarity", ControlPolarity.CLOSE,   g.getNodeAt(1).polarity);
        eq("node(0) normLabel","abc",                    g.getNodeAt(0).normalizedLabel);
        eq("node(1) normLabel","cba",                    g.getNodeAt(1).normalizedLabel);
    }

    /**
     * Adjacency edges exist in both directions for every adjacent pair
     * (Invariant 18 from the design doc).
     */
    private static void testAdjacencyEdgesSymmetric() {
        System.out.println("--- adjacency edges symmetric (invariant 18) ---");
        List<Stmt> stmts = List.of(cupOpen("a"), pocketOpen("b"), cupClose("a_rev"), pocketClose("b_rev"));
        ControlGraph g = new KnotAnalyzer(stmts).analyze();

        List<ControlNode> nodes = g.getNodes();
        for (int i = 0; i < nodes.size() - 1; i++) {
            ControlNode left  = nodes.get(i);
            ControlNode right = nodes.get(i + 1);

            boolean hasFwd = false, hasBwd = false;
            for (TraversalEdge e : g.outEdges(left))
                if (e.to == right && e.kind == EdgeKind.FORWARD_ADJACENCY) hasFwd = true;
            for (TraversalEdge e : g.outEdges(right))
                if (e.to == left  && e.kind == EdgeKind.BACKWARD_ADJACENCY) hasBwd = true;

            check("fwd adj (" + i + "→" + (i+1) + ")", hasFwd);
            check("bwd adj (" + (i+1) + "→" + i + ")", hasBwd);
        }
    }

    /**
     * Ownership jump edges exist between every matched pair (both directions).
     */
    private static void testOwnershipJumpEdges() {
        System.out.println("--- ownership jump edges ---");
        List<Stmt> stmts = List.of(pocketOpen("hello"), pocketOpen("world"),
                                   pocketClose("dlrow"), pocketClose("olleh"));
        ControlGraph g = new KnotAnalyzer(stmts).analyze();

        eq("pairCount", 2, g.getMatchTable().getAllPairs().size());
        for (MatchPair pair : g.getMatchTable().getAllPairs()) {
            boolean openToClose = false, closeToOpen = false;
            for (TraversalEdge e : g.outEdges(pair.open))
                if (e.to == pair.close && e.kind == EdgeKind.OWNERSHIP_JUMP) openToClose = true;
            for (TraversalEdge e : g.outEdges(pair.close))
                if (e.to == pair.open  && e.kind == EdgeKind.OWNERSHIP_JUMP) closeToOpen = true;
            check("ownership open→close pair " + pair.pairId, openToClose);
            check("ownership close→open pair " + pair.pairId, closeToOpen);
        }
    }

    /**
     * Tests for the four reverse-lookup methods added to ControlGraph for oscillation detection.
     *
     * Structure: {abc (xyz }cba )zyx — indices 0,1,2,3
     *   fwd condition: start=1 (PocketOpen), CONDITION_TRUE→2, CONDITION_FALSE→3
     *   bwd condition: start=2 (CupClose),   CONDITION_TRUE→1, CONDITION_FALSE→0
     */
    private static void testReverseLookupMinimalKnot() {
        System.out.println("--- reverse lookup (minimal KNOT) ---");
        List<Stmt> stmts = List.of(
            cupOpen("abc"),     // 0
            pocketOpen("xyz"),  // 1
            cupClose("cba"),    // 2
            pocketClose("zyx")  // 3
        );
        ControlGraph g = new KnotAnalyzer(stmts).analyze();

        // forwardConditionStartForFalseExit — index 3 is the false-exit of the fwd condition at 1
        eq("fwdFalseStart(3)",  1,  g.forwardConditionStartForFalseExit(3));
        eq("fwdFalseStart(2)", -1,  g.forwardConditionStartForFalseExit(2)); // 2 is true-entry, not false
        eq("fwdFalseStart(0)", -1,  g.forwardConditionStartForFalseExit(0));

        // forwardConditionStartForTrueEntry — index 2 is the true-entry of the fwd condition at 1
        eq("fwdTrueEntry(2)",  1,   g.forwardConditionStartForTrueEntry(2));
        eq("fwdTrueEntry(3)", -1,   g.forwardConditionStartForTrueEntry(3)); // 3 is false-exit, not true
        eq("fwdTrueEntry(0)", -1,   g.forwardConditionStartForTrueEntry(0));

        // backwardConditionStartForTrueEntry — index 1 is the true-entry of the bwd condition at 2
        eq("bwdTrueEntry(1)",  2,   g.backwardConditionStartForTrueEntry(1));
        eq("bwdTrueEntry(0)", -1,   g.backwardConditionStartForTrueEntry(0)); // 0 is false-exit, not true
        eq("bwdTrueEntry(3)", -1,   g.backwardConditionStartForTrueEntry(3));

        // backwardConditionStartForFalseExit — index 0 is the false-exit of the bwd condition at 2
        eq("bwdFalseExit(0)",  2,   g.backwardConditionStartForFalseExit(0));
        eq("bwdFalseExit(1)", -1,   g.backwardConditionStartForFalseExit(1)); // 1 is true-entry, not false
        eq("bwdFalseExit(2)", -1,   g.backwardConditionStartForFalseExit(2)); // 2 is the start, not an exit

        // Cross-check: reverse lookup from false-exit gives the same start as forwardConditionStart
        int fwdStart = g.forwardConditionStartForFalseExit(3);
        eq("fwdStart via falseExit matches conditionStart(1)", g.forwardConditionStart(1), fwdStart);
    }

    /**
     * Reverse-lookup with the TONK design-doc example.
     *
     * fwd condition: start=4 (lkjhg{), CONDITION_TRUE→5, CONDITION_FALSE→9
     * bwd condition: start=5 ()bvcxz), CONDITION_TRUE→4, CONDITION_FALSE→1
     */
    private static void testReverseLookupTonk() {
        System.out.println("--- reverse lookup (TONK doc example) ---");
        List<Stmt> stmts = List.of(
            pocketOpen("asdfg"),   // 0
            pocketOpen("zxcvb"),   // 1
            pocketOpen("qwerty"),  // 2
            cupOpen("poiuy"),      // 3
            cupOpen("lkjhg"),      // 4
            pocketClose("bvcxz"),  // 5
            pocketClose("gfdsa"),  // 6
            cupClose("yuiop"),     // 7
            pocketClose("ytrewq"), // 8
            cupClose("ghjkl")      // 9
        );
        ControlGraph g = new KnotAnalyzer(stmts).analyze();

        // fwd false-exit is 9; fwd true-entry is 5
        eq("fwdFalseStart(9)",  4, g.forwardConditionStartForFalseExit(9));
        eq("fwdFalseStart(5)", -1, g.forwardConditionStartForFalseExit(5)); // 5 is true-entry
        eq("fwdFalseStart(4)", -1, g.forwardConditionStartForFalseExit(4)); // 4 is the start node
        eq("fwdTrueEntry(5)",   4, g.forwardConditionStartForTrueEntry(5));
        eq("fwdTrueEntry(9)",  -1, g.forwardConditionStartForTrueEntry(9)); // 9 is false-exit

        // bwd true-entry is 4; bwd false-exit is 1
        eq("bwdTrueEntry(4)",   5, g.backwardConditionStartForTrueEntry(4));
        eq("bwdTrueEntry(1)",  -1, g.backwardConditionStartForTrueEntry(1)); // 1 is false-exit
        eq("bwdFalseExit(1)",   5, g.backwardConditionStartForFalseExit(1));
        eq("bwdFalseExit(4)",  -1, g.backwardConditionStartForFalseExit(4)); // 4 is true-entry

        // Nodes with no condition edges at all
        eq("fwdFalseStart(3)", -1, g.forwardConditionStartForFalseExit(3));
        eq("bwdFalseExit(7)",  -1, g.backwardConditionStartForFalseExit(7));
    }

    /**
     * Reverse-lookup on a structure with no conditions — all methods return -1.
     */
    private static void testReverseLookupNoConditions() {
        System.out.println("--- reverse lookup (no conditions) ---");
        List<Stmt> stmts = List.of(pocketOpen("hello"), pocketClose("olleh"));
        ControlGraph g = new KnotAnalyzer(stmts).analyze();
        eq("fwdFalseStart(0)", -1, g.forwardConditionStartForFalseExit(0));
        eq("fwdFalseStart(1)", -1, g.forwardConditionStartForFalseExit(1));
        eq("fwdTrueEntry(0)",  -1, g.forwardConditionStartForTrueEntry(0));
        eq("fwdTrueEntry(1)",  -1, g.forwardConditionStartForTrueEntry(1));
        eq("bwdTrueEntry(0)",  -1, g.backwardConditionStartForTrueEntry(0));
        eq("bwdTrueEntry(1)",  -1, g.backwardConditionStartForTrueEntry(1));
        eq("bwdFalseExit(0)",  -1, g.backwardConditionStartForFalseExit(0));
        eq("bwdFalseExit(1)",  -1, g.backwardConditionStartForFalseExit(1));
    }

    /**
     * nodeByIndex is consistent with the expression index of each ControlNode.
     */
    private static void testNodeIndexConsistency() {
        System.out.println("--- node index consistency ---");
        List<Stmt> stmts = List.of(
            cupOpen("a"), pocketOpen("b"), cupClose("a_"), pocketClose("b_")
        );
        ControlGraph g = new KnotAnalyzer(stmts).analyze();
        for (ControlNode node : g.getNodes()) {
            ControlNode lookup = g.getNodeAt(node.expressionIndex);
            check("getNodeAt(" + node.expressionIndex + ") == node", lookup == node);
        }
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== KnotAnalyzer Stress Test ===\n");

        run("empty",                 KnotAnalyzerStressTest::testEmpty);
        run("KNOT classification",   KnotAnalyzerStressTest::testKnotClassification);
        run("TONK classification",   KnotAnalyzerStressTest::testTonkClassification);
        run("POCKET classification", KnotAnalyzerStressTest::testPocketClassification);
        run("CUP classification",    KnotAnalyzerStressTest::testCupClassification);
        run("condition edges",       KnotAnalyzerStressTest::testConditionEdges);
        run("TONK doc example",      KnotAnalyzerStressTest::testTonkDesignDocExample);
        run("crossing detection",    KnotAnalyzerStressTest::testCrossing);
        run("nested pockets",        KnotAnalyzerStressTest::testNestedPockets);
        run("multiple conditions",   KnotAnalyzerStressTest::testMultipleConditions);
        run("unmatched nodes",       KnotAnalyzerStressTest::testUnmatchedNodes);
        run("getNodeAt",             KnotAnalyzerStressTest::testGetNodeAt);
        run("adjacency symmetric",    KnotAnalyzerStressTest::testAdjacencyEdgesSymmetric);
        run("ownership jumps",        KnotAnalyzerStressTest::testOwnershipJumpEdges);
        run("node index consistent",  KnotAnalyzerStressTest::testNodeIndexConsistency);
        run("reverse lookup KNOT",    KnotAnalyzerStressTest::testReverseLookupMinimalKnot);
        run("reverse lookup TONK",    KnotAnalyzerStressTest::testReverseLookupTonk);
        run("reverse lookup none",    KnotAnalyzerStressTest::testReverseLookupNoConditions);

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
