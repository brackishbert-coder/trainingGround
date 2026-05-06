package Box.Interpreter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class ControlGraph {

    public final OuterShell    outerShell;
    public final Topology      topology;
    public final Orientation   orientation;
    public final RuntimeKind   runtimeKind;

    private final List<ControlNode>   nodes        = new ArrayList<>();
    private final MatchTable          matchTable;
    private final List<ControlRegion> regions      = new ArrayList<>();
    private final List<TraversalEdge> reachability = new ArrayList<>();
    private final List<TraversalEdge> defaultTraversal = new ArrayList<>();
    private final List<TraversalEdge> unreachable  = new ArrayList<>();
    private final CrossingCluster     crossings;

    private final Map<ControlNode, List<TraversalEdge>> outEdges    = new LinkedHashMap<>();
    private final Map<ControlNode, List<TraversalEdge>> inEdges     = new LinkedHashMap<>();
    private final Map<Integer,     ControlNode>          nodeByIndex = new LinkedHashMap<>();

    // ---- Region-level UnreachableGraph -----------------------------------------
    private final List<UnreachableEdge>                         unreachableRegionEdges = new ArrayList<>();
    private final Map<ControlRegion, List<UnreachableEdge>>     unreachOutEdges        = new LinkedHashMap<>();

    public ControlGraph(OuterShell outerShell, Topology topology,
                        Orientation orientation, RuntimeKind runtimeKind,
                        MatchTable matchTable, CrossingCluster crossings) {
        this.outerShell  = outerShell;
        this.topology    = topology;
        this.orientation = orientation;
        this.runtimeKind = runtimeKind;
        this.matchTable  = matchTable;
        this.crossings   = crossings;
    }

    public void addNode(ControlNode node) {
        nodes.add(node);
        nodeByIndex.put(node.expressionIndex, node);
        outEdges.putIfAbsent(node, new ArrayList<>());
        inEdges.putIfAbsent(node,  new ArrayList<>());
    }

    public ControlNode getNodeAt(int expressionIndex) {
        return nodeByIndex.get(expressionIndex);
    }

    public void addRegion(ControlRegion region) {
        regions.add(region);
    }

    public void addReachabilityEdge(TraversalEdge edge) {
        reachability.add(edge);
        outEdges.computeIfAbsent(edge.from, k -> new ArrayList<>()).add(edge);
        inEdges.computeIfAbsent(edge.to,   k -> new ArrayList<>()).add(edge);
    }

    public void addDefaultTraversalEdge(TraversalEdge edge) {
        defaultTraversal.add(edge);
    }

    public void addUnreachableEdge(TraversalEdge edge) {
        unreachable.add(edge);
    }

    public List<ControlNode>   getNodes()            { return Collections.unmodifiableList(nodes); }
    public MatchTable          getMatchTable()        { return matchTable; }
    public List<ControlRegion> getRegions()           { return Collections.unmodifiableList(regions); }
    public List<TraversalEdge> getReachabilityEdges() { return Collections.unmodifiableList(reachability); }
    public List<TraversalEdge> getDefaultTraversalEdges() { return Collections.unmodifiableList(defaultTraversal); }
    public List<TraversalEdge> getUnreachableEdges()  { return Collections.unmodifiableList(unreachable); }
    public CrossingCluster     getCrossings()          { return crossings; }

    /** Region whose left boundary node is at startIndex, or null. */
    public ControlRegion getRegionStartingAt(int startIndex) {
        for (ControlRegion r : regions)
            if (r.startIndex == startIndex) return r;
        return null;
    }

    /** Region whose right boundary node is at endIndex, or null. */
    public ControlRegion getRegionEndingAt(int endIndex) {
        for (ControlRegion r : regions)
            if (r.endIndex == endIndex) return r;
        return null;
    }

    // ---- UnreachableGraph region-level API -------------------------------------

    public void addUnreachableRegionEdge(UnreachableEdge edge) {
        unreachableRegionEdges.add(edge);
        unreachOutEdges.computeIfAbsent(edge.from, k -> new ArrayList<>()).add(edge);
    }

    public List<UnreachableEdge> getUnreachableRegionEdges() {
        return Collections.unmodifiableList(unreachableRegionEdges);
    }

    public List<ControlRegion> getUnreachableRegions() {
        List<ControlRegion> result = new ArrayList<>();
        for (ControlRegion r : regions)
            if (r.reachabilityKind == ReachabilityKind.UNREACHABLE) result.add(r);
        return Collections.unmodifiableList(result);
    }

    /** Directed priority-BFS from start following outgoing UnreachableEdges.
     *  Expansion order: BOUNDARY > OWNERSHIP > ENCLOSURE > CROSSING > MIRROR > ROLE.
     *  Within each kind, sort by from.startIndex ascending (invariant 22). */
    public List<ControlRegion> bfsUnreachable(ControlRegion start) {
        List<ControlRegion> order   = new ArrayList<>();
        Set<ControlRegion>  visited = new LinkedHashSet<>();
        Queue<ControlRegion> queue  = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            ControlRegion current = queue.poll();
            order.add(current);
            List<UnreachableEdge> out = new ArrayList<>(
                    unreachOutEdges.getOrDefault(current, Collections.emptyList()));
            out.sort((a, b) -> {
                int cmp = a.kind.ordinal() - b.kind.ordinal();
                return cmp != 0 ? cmp : a.from.startIndex - b.from.startIndex;
            });
            for (UnreachableEdge e : out)
                if (visited.add(e.to)) queue.add(e.to);
        }
        return order;
    }

    /** Undirected BFS for connected-component membership. */
    public List<ControlRegion> bfsUnreachableComponent(ControlRegion start) {
        List<ControlRegion>  order   = new ArrayList<>();
        Set<ControlRegion>   visited = new LinkedHashSet<>();
        Queue<ControlRegion> queue   = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            ControlRegion current = queue.poll();
            order.add(current);
            for (UnreachableEdge e : unreachableRegionEdges) {
                ControlRegion neighbor = (e.from == current) ? e.to
                                       : (e.to   == current) ? e.from : null;
                if (neighbor != null && visited.add(neighbor)) queue.add(neighbor);
            }
        }
        return order;
    }

    public List<List<ControlRegion>> getUnreachableComponents() {
        List<ControlRegion>       all       = getUnreachableRegions();
        Set<ControlRegion>        visited   = new LinkedHashSet<>();
        List<List<ControlRegion>> components = new ArrayList<>();
        for (ControlRegion start : all) {
            if (!visited.contains(start)) {
                List<ControlRegion> comp = bfsUnreachableComponent(start);
                visited.addAll(comp);
                components.add(comp);
            }
        }
        return components;
    }

    public List<ControlRegion> getUnreachableOwnershipGroup(ControlRegion seed) {
        List<ControlRegion> result = new ArrayList<>();
        result.add(seed);
        for (UnreachableEdge e : unreachableRegionEdges)
            if (e.kind == UnreachableEdgeKind.OWNERSHIP && e.from == seed && !result.contains(e.to))
                result.add(e.to);
        return result;
    }

    public List<ControlRegion> getUnreachableMirrors(ControlRegion seed) {
        List<ControlRegion> result = new ArrayList<>();
        result.add(seed);
        for (UnreachableEdge e : unreachableRegionEdges)
            if (e.kind == UnreachableEdgeKind.MIRROR && e.from == seed && !result.contains(e.to))
                result.add(e.to);
        return result;
    }

    public List<ControlRegion> getUnreachableCrossingCluster(ControlRegion seed) {
        List<ControlRegion> result = new ArrayList<>();
        result.add(seed);
        for (UnreachableEdge e : unreachableRegionEdges)
            if (e.kind == UnreachableEdgeKind.CROSSING && e.from == seed && !result.contains(e.to))
                result.add(e.to);
        return result;
    }

    public List<TraversalEdge> outEdges(ControlNode node) {
        return outEdges.getOrDefault(node, Collections.emptyList());
    }

    public List<TraversalEdge> inEdges(ControlNode node) {
        return inEdges.getOrDefault(node, Collections.emptyList());
    }

    // -------------------------------------------------------------------------
    // Condition queries — used by KnotRunner to replace lexeme-based lookups
    // -------------------------------------------------------------------------

    // Forward condition: start node is OPEN polarity.
    // If the node at exprIndex is OPEN, it is checked directly.
    // If the node is CLOSE, its matched OPEN partner is checked (handles the
    // case where runKnot visits the structural close of a forward condition).
    public boolean hasForwardCondition(int exprIndex) {
        ControlNode node = nodeByIndex.get(exprIndex);
        if (node == null) return false;
        if (node.polarity == ControlPolarity.OPEN)
            return hasConditionEdge(node);
        ControlNode partner = matchTable.getMatch(node);
        return partner != null && partner.polarity == ControlPolarity.OPEN && hasConditionEdge(partner);
    }

    public int forwardConditionStart(int exprIndex) {
        ControlNode node = nodeByIndex.get(exprIndex);
        if (node == null) return -1;
        if (node.polarity == ControlPolarity.OPEN) return exprIndex;
        ControlNode partner = matchTable.getMatch(node);
        if (partner != null && partner.polarity == ControlPolarity.OPEN) return partner.expressionIndex;
        return -1;
    }

    public int forwardConditionTrueTarget(int exprIndex) {
        int start = forwardConditionStart(exprIndex);
        return start == -1 ? -1 : conditionTarget(nodeByIndex.get(start), EdgeKind.CONDITION_TRUE);
    }

    public int forwardConditionFalseTarget(int exprIndex) {
        int start = forwardConditionStart(exprIndex);
        return start == -1 ? -1 : conditionTarget(nodeByIndex.get(start), EdgeKind.CONDITION_FALSE);
    }

    // Backward condition: start node is CLOSE polarity.
    // If the node at exprIndex is CLOSE, it is checked directly.
    // If the node is OPEN, its matched CLOSE partner is checked.
    public boolean hasBackwardCondition(int exprIndex) {
        ControlNode node = nodeByIndex.get(exprIndex);
        if (node == null) return false;
        if (node.polarity == ControlPolarity.CLOSE)
            return hasConditionEdge(node);
        ControlNode partner = matchTable.getMatch(node);
        return partner != null && partner.polarity == ControlPolarity.CLOSE && hasConditionEdge(partner);
    }

    public int backwardConditionStart(int exprIndex) {
        ControlNode node = nodeByIndex.get(exprIndex);
        if (node == null) return -1;
        if (node.polarity == ControlPolarity.CLOSE) return exprIndex;
        ControlNode partner = matchTable.getMatch(node);
        if (partner != null && partner.polarity == ControlPolarity.CLOSE) return partner.expressionIndex;
        return -1;
    }

    public int backwardConditionTrueTarget(int exprIndex) {
        int start = backwardConditionStart(exprIndex);
        return start == -1 ? -1 : conditionTarget(nodeByIndex.get(start), EdgeKind.CONDITION_TRUE);
    }

    public int backwardConditionFalseTarget(int exprIndex) {
        int start = backwardConditionStart(exprIndex);
        return start == -1 ? -1 : conditionTarget(nodeByIndex.get(start), EdgeKind.CONDITION_FALSE);
    }

    // Returns the start expressionIndex of a forward condition whose false target == falseExitIndex.
    public int forwardConditionStartForFalseExit(int falseExitIndex) {
        for (TraversalEdge edge : reachability)
            if (edge.kind == EdgeKind.CONDITION_FALSE
                    && edge.from.polarity == ControlPolarity.OPEN
                    && edge.to.expressionIndex == falseExitIndex)
                return edge.from.expressionIndex;
        return -1;
    }

    // Returns the start expressionIndex of a forward condition whose true target == trueEntryIndex.
    public int forwardConditionStartForTrueEntry(int trueEntryIndex) {
        for (TraversalEdge edge : reachability)
            if (edge.kind == EdgeKind.CONDITION_TRUE
                    && edge.from.polarity == ControlPolarity.OPEN
                    && edge.to.expressionIndex == trueEntryIndex)
                return edge.from.expressionIndex;
        return -1;
    }

    // Returns the start expressionIndex of a backward condition whose true target == trueEntryIndex.
    public int backwardConditionStartForTrueEntry(int trueEntryIndex) {
        for (TraversalEdge edge : reachability)
            if (edge.kind == EdgeKind.CONDITION_TRUE
                    && edge.from.polarity == ControlPolarity.CLOSE
                    && edge.to.expressionIndex == trueEntryIndex)
                return edge.from.expressionIndex;
        return -1;
    }

    // Returns the start expressionIndex of a backward condition whose false target == falseExitIndex.
    public int backwardConditionStartForFalseExit(int falseExitIndex) {
        for (TraversalEdge edge : reachability)
            if (edge.kind == EdgeKind.CONDITION_FALSE
                    && edge.from.polarity == ControlPolarity.CLOSE
                    && edge.to.expressionIndex == falseExitIndex)
                return edge.from.expressionIndex;
        return -1;
    }

    private boolean hasConditionEdge(ControlNode node) {
        for (TraversalEdge e : outEdges.getOrDefault(node, Collections.emptyList()))
            if (e.kind == EdgeKind.CONDITION_TRUE) return true;
        return false;
    }

    private int conditionTarget(ControlNode node, EdgeKind kind) {
        if (node == null) return -1;
        for (TraversalEdge e : outEdges.getOrDefault(node, Collections.emptyList()))
            if (e.kind == kind) return e.to.expressionIndex;
        return -1;
    }

    @Override
    public String toString() {
        return "ControlGraph[" + runtimeKind
                + " shell=" + outerShell
                + " topo="  + topology
                + " orient=" + orientation
                + " nodes=" + nodes.size()
                + " pairs=" + matchTable.getAllPairs().size()
                + " regions=" + regions.size()
                + " edges=" + reachability.size() + "]";
    }
}
