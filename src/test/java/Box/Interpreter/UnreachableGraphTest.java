package Box.Interpreter;

import java.util.List;

import Parser.Expr;
import Parser.Stmt;
import Box.Token.Token;
import Box.Token.TokenType;

/**
 * Tests for the UnreachableGraph API in ControlGraph and KnotAnalyzer Pass 7.
 *
 * Background:
 *   Pass 6 (computeReachability) labels each region DEFAULT_REACHABLE, REVERSE_REACHABLE,
 *   or UNREACHABLE. In the current implementation, FORWARD_ADJACENCY edges form a
 *   complete chain from the leftmost control node to the rightmost, so all nodes are
 *   reachable from the primary entry via adjacency alone. No currently constructible
 *   structure produces UNREACHABLE regions — buildUnreachableGraph is a no-op (returns
 *   early when unreachable.size() < 2).
 *
 * What this test suite verifies:
 *   1. All standard fixtures (empty, KNOT crossing, nested pockets, single node) produce
 *      0 UNREACHABLE regions.
 *   2. The ControlGraph API methods (getUnreachableRegions, getUnreachableComponents,
 *      getUnreachableRegionEdges, bfsUnreachable, bfsUnreachableComponent) return correct
 *      results when called on a graph with no unreachable regions.
 *   3. Manual construction: when 2 ControlRegions are labelled UNREACHABLE and an
 *      UnreachableEdge is added manually, bfsUnreachable and bfsUnreachableComponent
 *      traverse them correctly.
 *   4. ReachabilityKind classifications are correct for a standard KNOT crossing fixture.
 *
 * Note: because the FA chain makes all nodes reachable, UNREACHABLE is reserved for
 * future structural forms not yet representable in the current PCB grammar.
 */
public class UnreachableGraphTest {

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
    // Fixtures
    // -------------------------------------------------------------------------

    private static Stmt.Expression pocketOpen(String label) {
        Token ctrl = new Token(TokenType.OPENPAREN, label + "(", null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.PocketOpen(ctrl), null);
    }

    private static Stmt.Expression pocketClose(String label) {
        Token ctrl = new Token(TokenType.CLOSEDPAREN, ")" + label, null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.PocketClosed(ctrl), null);
    }

    private static Stmt.Expression cupOpen(String label) {
        Token ctrl = new Token(TokenType.OPENBRACE, label + "{", null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.CupOpen(ctrl), null);
    }

    private static Stmt.Expression cupClose(String label) {
        Token ctrl = new Token(TokenType.CLOSEDBRACE, "}" + label, null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.CupClosed(ctrl), null);
    }

    /** KNOT crossing: ( { ) } — pocket and cup cross */
    private static ControlGraph knotCrossing() {
        return new KnotAnalyzer(List.of(
            pocketOpen("p"), cupOpen("c"), pocketClose("p"), cupClose("c")
        )).analyze();
    }

    /** Nested pockets: ( ( ) ) — no crossing */
    private static ControlGraph nestedPockets() {
        return new KnotAnalyzer(List.of(
            pocketOpen("outer"), pocketOpen("inner"),
            pocketClose("inner"), pocketClose("outer")
        )).analyze();
    }

    /** Single matched pocket pair: ( ) */
    private static ControlGraph singlePocket() {
        return new KnotAnalyzer(List.of(
            pocketOpen("a"), pocketClose("a")
        )).analyze();
    }

    /** Empty — no control nodes at all */
    private static ControlGraph emptyGraph() {
        return new KnotAnalyzer(List.of()).analyze();
    }

    // -------------------------------------------------------------------------
    // Pass 6 — current structures produce 0 UNREACHABLE regions
    // -------------------------------------------------------------------------

    private static void testEmptyGraphNoUnreachable() {
        System.out.println("--- empty graph: 0 unreachable regions ---");
        ControlGraph g = emptyGraph();
        eq("unreachable count = 0", 0, g.getUnreachableRegions().size());
        eq("components empty", 0, g.getUnreachableComponents().size());
        eq("unreachable edges empty", 0, g.getUnreachableRegionEdges().size());
    }

    private static void testSinglePocketNoUnreachable() {
        System.out.println("--- single pocket: 0 unreachable regions ---");
        ControlGraph g = singlePocket();
        eq("unreachable count = 0", 0, g.getUnreachableRegions().size());
    }

    private static void testNestedPocketsNoUnreachable() {
        System.out.println("--- nested pockets: 0 unreachable regions ---");
        ControlGraph g = nestedPockets();
        eq("unreachable count = 0", 0, g.getUnreachableRegions().size());
        // All 3 regions exist (POCKET_ENTRY, AMBIGUOUS, UNWIND) — all DEFAULT_REACHABLE
        eq("3 regions total", 3, g.getRegions().size());
        for (ControlRegion r : g.getRegions())
            check("region " + r.startIndex + ".." + r.endIndex + " is DEFAULT_REACHABLE",
                  r.reachabilityKind == ReachabilityKind.DEFAULT_REACHABLE);
    }

    private static void testKnotCrossingNoUnreachable() {
        System.out.println("--- KNOT crossing: 0 unreachable regions ---");
        ControlGraph g = knotCrossing();
        eq("unreachable count = 0", 0, g.getUnreachableRegions().size());
        // No unreachable → buildUnreachableGraph was a no-op
        eq("unreachable region edges = 0", 0, g.getUnreachableRegionEdges().size());
    }

    private static void testAllRegionsDefaultReachableInCrossing() {
        System.out.println("--- KNOT crossing: all regions DEFAULT_REACHABLE ---");
        // For a TONK (crossing), primary entry = rightmost node; BFS from rightmost
        // via BA chain reaches all nodes. All regions DEFAULT_REACHABLE via secondary
        // (or primary depending on TONK vs KNOT direction).
        ControlGraph g = knotCrossing();
        eq("3 regions in crossing", 3, g.getRegions().size());
        int unreachableCount = 0;
        for (ControlRegion r : g.getRegions())
            if (r.reachabilityKind == ReachabilityKind.UNREACHABLE) unreachableCount++;
        eq("0 UNREACHABLE in crossing", 0, unreachableCount);
    }

    // -------------------------------------------------------------------------
    // API safety: empty-graph cases return correct empty collections
    // -------------------------------------------------------------------------

    private static void testGetUnreachableComponentsEmpty() {
        System.out.println("--- getUnreachableComponents() returns empty when no unreachable ---");
        ControlGraph g = knotCrossing();
        List<List<ControlRegion>> comps = g.getUnreachableComponents();
        check("components empty", comps.isEmpty());
    }

    private static void testGetUnreachableRegionEdgesEmpty() {
        System.out.println("--- getUnreachableRegionEdges() returns empty when no unreachable ---");
        ControlGraph g = nestedPockets();
        check("no unreachable region edges", g.getUnreachableRegionEdges().isEmpty());
    }

    // -------------------------------------------------------------------------
    // Manual construction: inject 2 UNREACHABLE regions, test BFS APIs
    // -------------------------------------------------------------------------

    /**
     * Build a ControlGraph with 2 manually-labelled UNREACHABLE regions and a
     * BOUNDARY UnreachableEdge between them. Then verify BFS traversal works.
     *
     * We reuse the nested-pocket KnotAnalyzer graph's regions but relabel them.
     * After relabelling, we inject an UnreachableEdge and test the BFS methods.
     */
    private static void testManualUnreachableBfsTwoNodes() {
        System.out.println("--- manual: bfsUnreachable traverses 2 UNREACHABLE regions ---");
        ControlGraph g = nestedPockets();
        List<ControlRegion> regions = g.getRegions();
        // We have 3 regions at indices 0,1,2. Label the first two UNREACHABLE.
        ControlRegion a = regions.get(0);
        ControlRegion b = regions.get(1);
        a.reachabilityKind = ReachabilityKind.UNREACHABLE;
        b.reachabilityKind = ReachabilityKind.UNREACHABLE;

        // Add a directed BOUNDARY edge from a → b
        g.addUnreachableRegionEdge(new UnreachableEdge(a, b, UnreachableEdgeKind.BOUNDARY));

        // BFS from a should visit a, then b
        List<ControlRegion> order = g.bfsUnreachable(a);
        eq("bfsUnreachable order size", 2, order.size());
        check("first = a", order.get(0) == a);
        check("second = b", order.get(1) == b);
    }

    private static void testManualUnreachableComponentContainsBoth() {
        System.out.println("--- manual: bfsUnreachableComponent finds both in same component ---");
        ControlGraph g = nestedPockets();
        List<ControlRegion> regions = g.getRegions();
        ControlRegion a = regions.get(0);
        ControlRegion b = regions.get(1);
        a.reachabilityKind = ReachabilityKind.UNREACHABLE;
        b.reachabilityKind = ReachabilityKind.UNREACHABLE;
        g.addUnreachableRegionEdge(new UnreachableEdge(a, b, UnreachableEdgeKind.BOUNDARY));

        // Component from a (undirected) should contain both a and b
        List<ControlRegion> comp = g.bfsUnreachableComponent(a);
        eq("component size = 2", 2, comp.size());
        check("a in component", comp.contains(a));
        check("b in component", comp.contains(b));

        // getUnreachableComponents() should return 1 component
        List<List<ControlRegion>> comps = g.getUnreachableComponents();
        eq("1 component total", 1, comps.size());
        eq("component size = 2", 2, comps.get(0).size());
    }

    private static void testManualUnreachableOwnershipGroup() {
        System.out.println("--- manual: getUnreachableOwnershipGroup returns owned regions ---");
        ControlGraph g = nestedPockets();
        List<ControlRegion> regions = g.getRegions();
        ControlRegion a = regions.get(0);
        ControlRegion b = regions.get(1);
        ControlRegion c = regions.get(2);
        a.reachabilityKind = ReachabilityKind.UNREACHABLE;
        b.reachabilityKind = ReachabilityKind.UNREACHABLE;
        c.reachabilityKind = ReachabilityKind.UNREACHABLE;

        g.addUnreachableRegionEdge(new UnreachableEdge(a, b, UnreachableEdgeKind.OWNERSHIP));
        g.addUnreachableRegionEdge(new UnreachableEdge(a, c, UnreachableEdgeKind.BOUNDARY));

        List<ControlRegion> owned = g.getUnreachableOwnershipGroup(a);
        eq("ownership group size = 2 (a + b)", 2, owned.size());
        check("a in ownership group", owned.contains(a));
        check("b in ownership group", owned.contains(b));
        check("c NOT in ownership group (BOUNDARY, not OWNERSHIP)", !owned.contains(c));
    }

    private static void testManualUnreachableMirrors() {
        System.out.println("--- manual: getUnreachableMirrors returns mirror regions ---");
        ControlGraph g = nestedPockets();
        List<ControlRegion> regions = g.getRegions();
        ControlRegion a = regions.get(0);
        ControlRegion b = regions.get(1);
        a.reachabilityKind = ReachabilityKind.UNREACHABLE;
        b.reachabilityKind = ReachabilityKind.UNREACHABLE;
        g.addUnreachableRegionEdge(new UnreachableEdge(a, b, UnreachableEdgeKind.MIRROR));

        List<ControlRegion> mirrors = g.getUnreachableMirrors(a);
        eq("mirrors of a: size = 2 (a + b)", 2, mirrors.size());
        check("a in mirrors", mirrors.contains(a));
        check("b in mirrors", mirrors.contains(b));
    }

    private static void testManualBfsPriorityOrder() {
        System.out.println("--- manual: bfsUnreachable priority: BOUNDARY before OWNERSHIP ---");
        // Three unreachable regions: a → b (OWNERSHIP), a → c (BOUNDARY).
        // BOUNDARY has lower ordinal than OWNERSHIP, so c is visited before b.
        ControlGraph g = new KnotAnalyzer(List.of(
            pocketOpen("1"), pocketOpen("2"), pocketOpen("3"),
            pocketClose("3"), pocketClose("2"), pocketClose("1")
        )).analyze();

        List<ControlRegion> regions = g.getRegions();
        // 5 regions: relabel first 3 as UNREACHABLE
        ControlRegion a = regions.get(0);
        ControlRegion b = regions.get(1);
        ControlRegion c = regions.get(2);
        a.reachabilityKind = ReachabilityKind.UNREACHABLE;
        b.reachabilityKind = ReachabilityKind.UNREACHABLE;
        c.reachabilityKind = ReachabilityKind.UNREACHABLE;

        // Add OWNERSHIP from a→b and BOUNDARY from a→c
        g.addUnreachableRegionEdge(new UnreachableEdge(a, b, UnreachableEdgeKind.OWNERSHIP));
        g.addUnreachableRegionEdge(new UnreachableEdge(a, c, UnreachableEdgeKind.BOUNDARY));

        List<ControlRegion> order = g.bfsUnreachable(a);
        eq("3 regions visited", 3, order.size());
        check("first = a", order.get(0) == a);
        // BOUNDARY (ordinal lower) sorts before OWNERSHIP → c before b
        check("second = c (BOUNDARY before OWNERSHIP)", order.get(1) == c);
        check("third = b", order.get(2) == b);
    }

    private static void testManualUnreachableSingletonBfs() {
        System.out.println("--- manual: single unreachable region BFS returns just itself ---");
        ControlGraph g = singlePocket();
        List<ControlRegion> regions = g.getRegions();
        ControlRegion a = regions.get(0);
        a.reachabilityKind = ReachabilityKind.UNREACHABLE;
        // No edges added

        List<ControlRegion> order = g.bfsUnreachable(a);
        eq("bfsUnreachable singleton size = 1", 1, order.size());
        check("singleton = a", order.get(0) == a);

        List<ControlRegion> comp = g.bfsUnreachableComponent(a);
        eq("component singleton size = 1", 1, comp.size());
    }

    // -------------------------------------------------------------------------
    // getUnreachableRegions filters by reachabilityKind
    // -------------------------------------------------------------------------

    private static void testGetUnreachableRegionsFiltersCorrectly() {
        System.out.println("--- getUnreachableRegions returns only UNREACHABLE-labelled regions ---");
        ControlGraph g = nestedPockets();
        List<ControlRegion> regions = g.getRegions();
        // Label only the first region UNREACHABLE
        regions.get(0).reachabilityKind = ReachabilityKind.UNREACHABLE;
        // Leave regions.get(1) and regions.get(2) as DEFAULT_REACHABLE

        List<ControlRegion> unreachable = g.getUnreachableRegions();
        eq("only 1 UNREACHABLE region", 1, unreachable.size());
        check("that region is regions[0]", unreachable.get(0) == regions.get(0));
    }

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        testEmptyGraphNoUnreachable();
        testSinglePocketNoUnreachable();
        testNestedPocketsNoUnreachable();
        testKnotCrossingNoUnreachable();
        testAllRegionsDefaultReachableInCrossing();
        testGetUnreachableComponentsEmpty();
        testGetUnreachableRegionEdgesEmpty();
        testManualUnreachableBfsTwoNodes();
        testManualUnreachableComponentContainsBoth();
        testManualUnreachableOwnershipGroup();
        testManualUnreachableMirrors();
        testManualBfsPriorityOrder();
        testManualUnreachableSingletonBfs();
        testGetUnreachableRegionsFiltersCorrectly();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
    }
}
