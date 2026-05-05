package Box.Interpreter;

import Parser.Expr;
import Parser.Stmt;

import java.util.*;

public class KnotAnalyzer {

    private final List<Stmt> stmts;

    public KnotAnalyzer(List<Stmt> stmts) {
        this.stmts = stmts;
    }

    public ControlGraph analyze() {
        List<ControlNode> nodes = scanNodes();
        if (nodes.isEmpty()) {
            MatchTable empty = new MatchTable();
            return new ControlGraph(OuterShell.MIXED, Topology.ORDINARY, Orientation.AMBIGUOUS,
                    RuntimeKind.AMBIGUOUS, empty, new CrossingCluster());
        }
        MatchTable matchTable   = buildMatchTable(nodes);
        CrossingCluster crossings = detectCrossings(matchTable);
        OuterShell outerShell   = classifyOuterShell(nodes);
        Topology topology       = crossings.isEmpty() ? Topology.ORDINARY : Topology.KNOTTED;
        Orientation orientation = classifyOrientation(nodes, outerShell);
        RuntimeKind runtimeKind = classifyRuntimeKind(outerShell, topology, orientation);

        ControlGraph graph = new ControlGraph(outerShell, topology, orientation, runtimeKind,
                matchTable, crossings);
        for (ControlNode n : nodes) graph.addNode(n);

        List<ControlRegion> regions = buildRegions(nodes);
        classifyRegionKinds(regions);
        for (ControlRegion r : regions) graph.addRegion(r);

        buildEdges(graph, nodes, matchTable);
        computeReachability(graph, nodes, runtimeKind);
        buildUnreachableGraph(graph, matchTable);
        return graph;
    }

    // -------------------------------------------------------------------------
    // Pass 1 — scan ControlNodes from expression list
    // -------------------------------------------------------------------------

    private List<ControlNode> scanNodes() {
        List<ControlNode> nodes = new ArrayList<>();
        for (int i = 0; i < stmts.size(); i++) {
            Stmt stmt = stmts.get(i);
            if (!(stmt instanceof Stmt.Expression)) continue;
            Stmt.Expression exprStmt = (Stmt.Expression) stmt;
            Expr expr = exprStmt.expression;

            ControlFamily   family;
            ControlPolarity polarity;
            String          rawLabel;

            if (expr instanceof Expr.PocketOpen) {
                family   = ControlFamily.POCKET;
                polarity = ControlPolarity.OPEN;
                rawLabel = ((Expr.PocketOpen) expr).ctrl != null ? ((Expr.PocketOpen) expr).ctrl.lexeme : null;
            } else if (expr instanceof Expr.PocketClosed) {
                family   = ControlFamily.POCKET;
                polarity = ControlPolarity.CLOSE;
                rawLabel = ((Expr.PocketClosed) expr).ctrl != null ? ((Expr.PocketClosed) expr).ctrl.lexeme : null;
            } else if (expr instanceof Expr.CupOpen) {
                family   = ControlFamily.CUP;
                polarity = ControlPolarity.OPEN;
                rawLabel = ((Expr.CupOpen) expr).ctrl != null ? ((Expr.CupOpen) expr).ctrl.lexeme : null;
            } else if (expr instanceof Expr.CupClosed) {
                family   = ControlFamily.CUP;
                polarity = ControlPolarity.CLOSE;
                rawLabel = ((Expr.CupClosed) expr).ctrl != null ? ((Expr.CupClosed) expr).ctrl.lexeme : null;
            } else {
                continue;
            }

            String normalizedLabel = normalizeLabel(rawLabel, polarity);
            nodes.add(new ControlNode(i, family, polarity, rawLabel, normalizedLabel, exprStmt));
        }
        return nodes;
    }

    private String normalizeLabel(String raw, ControlPolarity polarity) {
        if (raw == null) return null;
        if (polarity == ControlPolarity.OPEN) {
            if (raw.endsWith("(") || raw.endsWith("{"))
                return raw.substring(0, raw.length() - 1);
        } else {
            if (raw.startsWith(")") || raw.startsWith("}"))
                return raw.substring(1);
        }
        return raw;
    }

    // -------------------------------------------------------------------------
    // Pass 2 — build MatchTable (stack-based closest-match per family)
    // -------------------------------------------------------------------------

    private MatchTable buildMatchTable(List<ControlNode> nodes) {
        MatchTable table = new MatchTable();
        Deque<ControlNode> pocketStack = new ArrayDeque<>();
        Deque<ControlNode> cupStack    = new ArrayDeque<>();
        int pairCounter = 0;

        for (ControlNode node : nodes) {
            if (node.polarity == ControlPolarity.OPEN) {
                (node.family == ControlFamily.POCKET ? pocketStack : cupStack).push(node);
            } else {
                if (node.normalizedLabel == null) {
                    table.setResult(node, MatchResult.NULL_LABEL);
                    continue;
                }
                Deque<ControlNode> stack = node.family == ControlFamily.POCKET ? pocketStack : cupStack;
                ControlNode matched = popMatching(stack, node.normalizedLabel, table);
                if (matched != null) {
                    table.add(matched, node, "p" + pairCounter++);
                } else {
                    table.setResult(node, MatchResult.UNMATCHED_CLOSE);
                }
            }
        }
        return table;
    }

    private ControlNode popMatching(Deque<ControlNode> stack, String closeNorm, MatchTable table) {
        // Search stack from top for the first open whose reversed label matches closeNorm.
        // Nodes skipped over remain in the stack — they could match a later close.
        List<ControlNode> skipped = new ArrayList<>();
        ControlNode found = null;
        while (!stack.isEmpty()) {
            ControlNode open = stack.pop();
            if (open.normalizedLabel == null) {
                table.setResult(open, MatchResult.NULL_LABEL);
                skipped.add(open);
                continue;
            }
            String reversed = new StringBuilder(open.normalizedLabel).reverse().toString();
            if (reversed.equals(closeNorm)) {
                found = open;
                break;
            }
            skipped.add(open);
        }
        // Push skipped nodes back in original order (they are still in play).
        for (int i = skipped.size() - 1; i >= 0; i--) {
            stack.push(skipped.get(i));
        }
        return found;
    }

    // -------------------------------------------------------------------------
    // Pass 3 — crossing detection
    // -------------------------------------------------------------------------

    private CrossingCluster detectCrossings(MatchTable matchTable) {
        CrossingCluster cluster = new CrossingCluster();
        List<MatchPair> pairs = matchTable.getAllPairs();
        for (int i = 0; i < pairs.size(); i++) {
            for (int j = i + 1; j < pairs.size(); j++) {
                MatchPair a = pairs.get(i);
                MatchPair b = pairs.get(j);
                int a0 = a.openIndex(), a1 = a.closeIndex();
                int b0 = b.openIndex(), b1 = b.closeIndex();
                if ((a0 < b0 && b0 < a1 && a1 < b1) || (b0 < a0 && a0 < b1 && b1 < a1)) {
                    CrossingKind kind = crossingKind(a.family, b.family);
                    cluster.add(new Crossing(a, b, kind));
                }
            }
        }
        return cluster;
    }

    private CrossingKind crossingKind(ControlFamily outer, ControlFamily inner) {
        if (outer == ControlFamily.POCKET && inner == ControlFamily.POCKET) return CrossingKind.POCKET_OVER_POCKET;
        if (outer == ControlFamily.CUP    && inner == ControlFamily.CUP)    return CrossingKind.CUP_OVER_CUP;
        if (outer == ControlFamily.POCKET && inner == ControlFamily.CUP)    return CrossingKind.POCKET_OVER_CUP;
        if (outer == ControlFamily.CUP    && inner == ControlFamily.POCKET) return CrossingKind.CUP_OVER_POCKET;
        return CrossingKind.MIXED;
    }

    // -------------------------------------------------------------------------
    // Classification — OuterShell, Topology, Orientation, RuntimeKind
    // -------------------------------------------------------------------------

    private OuterShell classifyOuterShell(List<ControlNode> nodes) {
        ControlNode first = nodes.get(0);
        ControlNode last  = nodes.get(nodes.size() - 1);
        if (first.family == last.family) {
            return first.family == ControlFamily.POCKET ? OuterShell.POCKET_SHAPED : OuterShell.CUP_SHAPED;
        }
        return OuterShell.MIXED;
    }

    private Orientation classifyOrientation(List<ControlNode> nodes, OuterShell shell) {
        if (shell != OuterShell.MIXED) return Orientation.AMBIGUOUS;
        ControlNode first = nodes.get(0);
        ControlNode last  = nodes.get(nodes.size() - 1);
        // FORWARD_BIASED (KNOT): leftmost={  rightmost=)
        if (first.family == ControlFamily.CUP    && first.polarity == ControlPolarity.OPEN
         && last.family  == ControlFamily.POCKET && last.polarity  == ControlPolarity.CLOSE) {
            return Orientation.FORWARD_BIASED;
        }
        // BACKWARD_BIASED (TONK): leftmost=(  rightmost=}
        if (first.family == ControlFamily.POCKET && first.polarity == ControlPolarity.OPEN
         && last.family  == ControlFamily.CUP    && last.polarity  == ControlPolarity.CLOSE) {
            return Orientation.BACKWARD_BIASED;
        }
        return Orientation.AMBIGUOUS;
    }

    private RuntimeKind classifyRuntimeKind(OuterShell shell, Topology topo, Orientation orient) {
        switch (shell) {
            case POCKET_SHAPED:
                return topo == Topology.KNOTTED ? RuntimeKind.KNOTTED_POCKET : RuntimeKind.POCKET;
            case CUP_SHAPED:
                return topo == Topology.KNOTTED ? RuntimeKind.KNOTTED_CUP : RuntimeKind.CUP;
            case MIXED:
                if (orient == Orientation.FORWARD_BIASED)  return RuntimeKind.KNOT;
                if (orient == Orientation.BACKWARD_BIASED) return RuntimeKind.TONK;
                return RuntimeKind.AMBIGUOUS;
            default:
                return RuntimeKind.AMBIGUOUS;
        }
    }

    // -------------------------------------------------------------------------
    // Pass 4 — build ControlRegions (one per adjacent control pair)
    // -------------------------------------------------------------------------

    private List<ControlRegion> buildRegions(List<ControlNode> nodes) {
        List<ControlRegion> regions = new ArrayList<>();
        for (int i = 0; i < nodes.size() - 1; i++) {
            ControlNode left  = nodes.get(i);
            ControlNode right = nodes.get(i + 1);
            regions.add(new ControlRegion(left, right, left.expressionIndex, right.expressionIndex));
        }
        return regions;
    }

    private void classifyRegionKinds(List<ControlRegion> regions) {
        for (ControlRegion region : regions) {
            region.regionKind = deriveRegionKind(region.leftControl, region.rightControl);
        }
    }

    private RegionKind deriveRegionKind(ControlNode left, ControlNode right) {
        ControlFamily lf = left.family,  rf = right.family;
        ControlPolarity lp = left.polarity, rp = right.polarity;

        if (lf == rf) {
            if (lp == ControlPolarity.OPEN && rp == ControlPolarity.OPEN)   return RegionKind.POCKET_ENTRY;  // ( ( or { {
            if (lp == ControlPolarity.CLOSE && rp == ControlPolarity.CLOSE) return RegionKind.UNWIND;        // ) ) or } }
        }

        // POCKET_ENTRY and CUP_ENTRY distinction
        if (lf == ControlFamily.POCKET && lp == ControlPolarity.OPEN
         && rf == ControlFamily.POCKET && rp == ControlPolarity.OPEN)  return RegionKind.POCKET_ENTRY;
        if (lf == ControlFamily.CUP    && lp == ControlPolarity.OPEN
         && rf == ControlFamily.CUP    && rp == ControlPolarity.OPEN)  return RegionKind.CUP_ENTRY;

        // SETUP: { ( or ) }
        if (lf == ControlFamily.CUP    && lp == ControlPolarity.OPEN
         && rf == ControlFamily.POCKET && rp == ControlPolarity.OPEN)  return RegionKind.SETUP;
        if (lf == ControlFamily.POCKET && lp == ControlPolarity.CLOSE
         && rf == ControlFamily.CUP    && rp == ControlPolarity.CLOSE) return RegionKind.SETUP;

        // CONDITION: ( } or { )
        if (lf == ControlFamily.POCKET && lp == ControlPolarity.OPEN
         && rf == ControlFamily.CUP    && rp == ControlPolarity.CLOSE) return RegionKind.CONDITION;
        if (lf == ControlFamily.CUP    && lp == ControlPolarity.OPEN
         && rf == ControlFamily.POCKET && rp == ControlPolarity.CLOSE) return RegionKind.CONDITION;

        // BODY variants: } { or ( { or } )
        if (lf == ControlFamily.CUP    && lp == ControlPolarity.CLOSE
         && rf == ControlFamily.CUP    && rp == ControlPolarity.OPEN)  return RegionKind.BODY;
        if (lf == ControlFamily.POCKET && lp == ControlPolarity.OPEN
         && rf == ControlFamily.CUP    && rp == ControlPolarity.OPEN)  return RegionKind.BODY;
        if (lf == ControlFamily.CUP    && lp == ControlPolarity.CLOSE
         && rf == ControlFamily.POCKET && rp == ControlPolarity.CLOSE) return RegionKind.BODY;

        return RegionKind.AMBIGUOUS;
    }

    // -------------------------------------------------------------------------
    // Pass 5 — build TraversalEdges
    // -------------------------------------------------------------------------

    private void buildEdges(ControlGraph graph, List<ControlNode> nodes, MatchTable matchTable) {
        // Adjacency edges (forward and backward) — both directions for every adjacent pair
        for (int i = 0; i < nodes.size() - 1; i++) {
            ControlNode a = nodes.get(i);
            ControlNode b = nodes.get(i + 1);
            graph.addReachabilityEdge(new TraversalEdge(a, b, EdgeKind.FORWARD_ADJACENCY));
            graph.addReachabilityEdge(new TraversalEdge(b, a, EdgeKind.BACKWARD_ADJACENCY));
        }

        // Ownership jump edges — open ↔ close for each matched pair
        for (MatchPair pair : matchTable.getAllPairs()) {
            graph.addReachabilityEdge(new TraversalEdge(pair.open, pair.close, EdgeKind.OWNERSHIP_JUMP));
            graph.addReachabilityEdge(new TraversalEdge(pair.close, pair.open, EdgeKind.OWNERSHIP_JUMP));
        }

        // Condition edges — true and false targets per CONDITION region
        List<ControlRegion> regions = graph.getRegions();
        for (ControlRegion region : regions) {
            if (region.regionKind != RegionKind.CONDITION) continue;
            addConditionEdges(graph, region, matchTable);
        }

        // UNWIND edges — for each node, target the enclosing pair's boundary
        for (ControlNode node : nodes) {
            addUnwindEdges(graph, node, matchTable);
        }

        // ENTRY edges — forward and backward descent into nearest nested interval
        for (ControlNode node : nodes) {
            addEntryEdges(graph, node, nodes, matchTable);
        }
    }

    private void addConditionEdges(ControlGraph graph, ControlRegion region, MatchTable matchTable) {
        ControlNode left  = region.leftControl;
        ControlNode right = region.rightControl;

        // Forward condition: left is the start
        // ( ... } : start=pocket-open, true=cup-close(right), false=matched pocket-close for left
        // { ... ) : start=cup-open,    true=pocket-close(right), false=matched cup-close for left
        if (left.polarity == ControlPolarity.OPEN) {
            ControlNode falseTarget = matchTable.getMatch(left);
            if (falseTarget != null) {
                graph.addReachabilityEdge(new TraversalEdge(left, right,       EdgeKind.CONDITION_TRUE));
                graph.addReachabilityEdge(new TraversalEdge(left, falseTarget, EdgeKind.CONDITION_FALSE));
            }
        }

        // Backward condition: right is the start
        // ) ... { : start=pocket-close, true=cup-open(left), false=matched pocket-open for right
        // } ... ( : start=cup-close,    true=pocket-open(left), false=matched cup-open for right
        if (right.polarity == ControlPolarity.CLOSE) {
            ControlNode falseTarget = matchTable.getMatch(right);
            if (falseTarget != null) {
                graph.addReachabilityEdge(new TraversalEdge(right, left,        EdgeKind.CONDITION_TRUE));
                graph.addReachabilityEdge(new TraversalEdge(right, falseTarget, EdgeKind.CONDITION_FALSE));
            }
        }
    }

    private void addUnwindEdges(ControlGraph graph, ControlNode node, MatchTable matchTable) {
        MatchPair enclosing = findInnermostEnclosing(node, matchTable);
        if (enclosing == null) return;
        // Avoid self-targeting
        if (enclosing.close != node) {
            graph.addReachabilityEdge(new TraversalEdge(node, enclosing.close, EdgeKind.UNWIND));
        }
        if (enclosing.open != node) {
            graph.addReachabilityEdge(new TraversalEdge(node, enclosing.open,  EdgeKind.UNWIND));
        }
    }

    private MatchPair findInnermostEnclosing(ControlNode node, MatchTable matchTable) {
        int idx = node.expressionIndex;
        MatchPair innermost = null;
        int smallestSpan = Integer.MAX_VALUE;
        for (MatchPair pair : matchTable.getAllPairs()) {
            int open  = pair.openIndex();
            int close = pair.closeIndex();
            if (open < idx && idx < close) {
                int span = close - open;
                if (span < smallestSpan) {
                    smallestSpan = span;
                    innermost = pair;
                }
            }
        }
        return innermost;
    }

    private void addEntryEdges(ControlGraph graph, ControlNode node,
                               List<ControlNode> nodes, MatchTable matchTable) {
        int idx = node.expressionIndex;

        // Forward ENTRY: find matched pair with smallest interval whose openIndex > idx
        MatchPair forwardEntry = null;
        int minSpan = Integer.MAX_VALUE;
        for (MatchPair pair : matchTable.getAllPairs()) {
            if (pair.openIndex() > idx) {
                int span = pair.closeIndex() - pair.openIndex();
                if (span < minSpan || (span == minSpan && pair.openIndex() < forwardEntry.openIndex())) {
                    minSpan = span;
                    forwardEntry = pair;
                }
            }
        }
        if (forwardEntry != null && forwardEntry.open != node) {
            graph.addReachabilityEdge(new TraversalEdge(node, forwardEntry.open, EdgeKind.ENTRY));
        }

        // Backward ENTRY: find matched pair with smallest interval whose closeIndex < idx
        MatchPair backwardEntry = null;
        minSpan = Integer.MAX_VALUE;
        for (MatchPair pair : matchTable.getAllPairs()) {
            if (pair.closeIndex() < idx) {
                int span = pair.closeIndex() - pair.openIndex();
                if (span < minSpan || (span == minSpan
                        && (backwardEntry == null || pair.closeIndex() > backwardEntry.closeIndex()))) {
                    minSpan = span;
                    backwardEntry = pair;
                }
            }
        }
        if (backwardEntry != null && backwardEntry.close != node) {
            graph.addReachabilityEdge(new TraversalEdge(node, backwardEntry.close, EdgeKind.ENTRY));
        }
    }

    // -------------------------------------------------------------------------
    // Pass 6 — compute ReachabilityKind via BFS from both entry points
    // -------------------------------------------------------------------------

    private void computeReachability(ControlGraph graph, List<ControlNode> nodes, RuntimeKind runtimeKind) {
        if (nodes.isEmpty()) return;
        ControlNode primaryEntry   = primaryEntry(nodes, runtimeKind);
        ControlNode secondaryEntry = secondaryEntry(nodes, runtimeKind);

        Set<ControlNode> primaryReachable   = bfsNodes(graph, primaryEntry);
        Set<ControlNode> secondaryReachable = bfsNodes(graph, secondaryEntry);

        for (ControlRegion region : graph.getRegions()) {
            boolean fromPrimary   = primaryReachable.contains(region.leftControl)
                                 && primaryReachable.contains(region.rightControl);
            boolean fromSecondary = secondaryReachable.contains(region.leftControl)
                                 && secondaryReachable.contains(region.rightControl);

            if (fromPrimary) {
                region.reachabilityKind = ReachabilityKind.DEFAULT_REACHABLE;
            } else if (fromSecondary) {
                region.reachabilityKind = ReachabilityKind.REVERSE_REACHABLE;
            } else {
                region.reachabilityKind = ReachabilityKind.UNREACHABLE;
            }
        }

        // Mark unreachable regions' edges in the unreachable sub-graph
        for (ControlRegion region : graph.getRegions()) {
            if (region.reachabilityKind == ReachabilityKind.UNREACHABLE) {
                // Find adjacency edge spanning this region and add to unreachable list
                for (TraversalEdge edge : graph.getReachabilityEdges()) {
                    if (edge.from == region.leftControl && edge.to == region.rightControl) {
                        graph.addUnreachableEdge(edge);
                    }
                }
            }
        }
    }

    private ControlNode primaryEntry(List<ControlNode> nodes, RuntimeKind kind) {
        // TONK: primary entry = rightmost (BACKWARD)
        // KNOT and default: primary entry = leftmost (FORWARD)
        if (kind == RuntimeKind.TONK) return nodes.get(nodes.size() - 1);
        return nodes.get(0);
    }

    private ControlNode secondaryEntry(List<ControlNode> nodes, RuntimeKind kind) {
        if (kind == RuntimeKind.TONK) return nodes.get(0);
        return nodes.get(nodes.size() - 1);
    }

    private Set<ControlNode> bfsNodes(ControlGraph graph, ControlNode start) {
        Set<ControlNode> visited = new LinkedHashSet<>();
        Queue<ControlNode> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            ControlNode current = queue.poll();
            for (TraversalEdge edge : graph.outEdges(current)) {
                if (visited.add(edge.to)) {
                    queue.add(edge.to);
                }
            }
        }
        return visited;
    }

    // -------------------------------------------------------------------------
    // Pass 7 — build UnreachableGraph (region-to-region edges for UNREACHABLE regions)
    // -------------------------------------------------------------------------

    private void buildUnreachableGraph(ControlGraph graph, MatchTable matchTable) {
        List<ControlRegion> all        = new ArrayList<>(graph.getRegions());
        List<ControlRegion> unreachable = new ArrayList<>();
        for (ControlRegion r : all)
            if (r.reachabilityKind == ReachabilityKind.UNREACHABLE) unreachable.add(r);
        if (unreachable.size() < 2) return;

        addBoundaryEdges(graph, unreachable);
        addOwnershipEdges(graph, unreachable, matchTable);
        addEnclosureEdges(graph, unreachable, matchTable);
        addCrossingEdges(graph, unreachable, graph.getCrossings());
        addMirrorEdges(graph, unreachable, all);
        addRoleEdges(graph, unreachable, matchTable);
    }

    private void addBoundaryEdges(ControlGraph graph, List<ControlRegion> unreachable) {
        for (ControlRegion a : unreachable) {
            for (ControlRegion b : unreachable) {
                if (a == b) continue;
                if (a.rightControl == b.leftControl || a.leftControl == b.rightControl)
                    graph.addUnreachableRegionEdge(new UnreachableEdge(a, b, UnreachableEdgeKind.BOUNDARY));
            }
        }
    }

    private void addOwnershipEdges(ControlGraph graph, List<ControlRegion> unreachable, MatchTable matchTable) {
        for (MatchPair pair : matchTable.getAllPairs()) {
            int lo = pair.openIndex(), hi = pair.closeIndex();
            List<ControlRegion> owned = new ArrayList<>();
            for (ControlRegion r : unreachable)
                if (lo <= r.startIndex && r.endIndex <= hi) owned.add(r);
            for (ControlRegion a : owned)
                for (ControlRegion b : owned)
                    if (a != b)
                        graph.addUnreachableRegionEdge(new UnreachableEdge(a, b, UnreachableEdgeKind.OWNERSHIP));
        }
    }

    private void addEnclosureEdges(ControlGraph graph, List<ControlRegion> unreachable, MatchTable matchTable) {
        for (ControlRegion a : unreachable) {
            Set<ControlRegion> added = new HashSet<>();
            for (ControlNode c : new ControlNode[]{ a.leftControl, a.rightControl }) {
                ControlNode match = matchTable.getMatch(c);
                if (match == null) continue;
                int lo = Math.min(c.expressionIndex, match.expressionIndex);
                int hi = Math.max(c.expressionIndex, match.expressionIndex);
                for (ControlRegion b : unreachable) {
                    if (b == a || added.contains(b)) continue;
                    if (lo < b.startIndex && b.endIndex < hi) {
                        graph.addUnreachableRegionEdge(new UnreachableEdge(a, b, UnreachableEdgeKind.ENCLOSURE));
                        added.add(b);
                    }
                }
            }
        }
    }

    private void addCrossingEdges(ControlGraph graph, List<ControlRegion> unreachable, CrossingCluster crossings) {
        if (crossings.isEmpty()) return;
        for (Crossing crossing : crossings.getCrossings()) {
            ControlNode oo = crossing.outer.open,  oc = crossing.outer.close;
            ControlNode io = crossing.inner.open,  ic = crossing.inner.close;
            List<ControlRegion> inCrossing = new ArrayList<>();
            for (ControlRegion r : unreachable) {
                ControlNode l = r.leftControl, ri = r.rightControl;
                if (l == oo || l == oc || l == io || l == ic
                 || ri == oo || ri == oc || ri == io || ri == ic)
                    inCrossing.add(r);
            }
            for (ControlRegion a : inCrossing)
                for (ControlRegion b : inCrossing)
                    if (a != b)
                        graph.addUnreachableRegionEdge(new UnreachableEdge(a, b, UnreachableEdgeKind.CROSSING));
        }
    }

    private void addMirrorEdges(ControlGraph graph, List<ControlRegion> unreachable, List<ControlRegion> all) {
        int n = all.size();
        for (ControlRegion a : unreachable) {
            int iA = all.indexOf(a);
            int mirrorIdx = n - 1 - iA;
            if (mirrorIdx == iA || mirrorIdx < 0 || mirrorIdx >= n) continue;
            ControlRegion b = all.get(mirrorIdx);
            if (b.reachabilityKind == ReachabilityKind.UNREACHABLE)
                graph.addUnreachableRegionEdge(new UnreachableEdge(a, b, UnreachableEdgeKind.MIRROR));
        }
    }

    @SuppressWarnings("unchecked")
    private void addRoleEdges(ControlGraph graph, List<ControlRegion> unreachable, MatchTable matchTable) {
        Set<RegionKind> roleKinds = EnumSet.of(RegionKind.SETUP, RegionKind.BODY, RegionKind.CONDITION);
        for (MatchPair pair : matchTable.getAllPairs()) {
            int lo = pair.openIndex(), hi = pair.closeIndex();
            List<ControlRegion> roleRegions = new ArrayList<>();
            for (ControlRegion r : unreachable)
                if (lo <= r.startIndex && r.endIndex <= hi && roleKinds.contains(r.regionKind))
                    roleRegions.add(r);
            for (ControlRegion a : roleRegions)
                for (ControlRegion b : roleRegions)
                    if (a != b && a.regionKind != b.regionKind)
                        graph.addUnreachableRegionEdge(new UnreachableEdge(a, b, UnreachableEdgeKind.ROLE));
        }
    }
}
