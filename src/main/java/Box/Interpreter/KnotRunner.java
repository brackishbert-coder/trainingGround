package Box.Interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import Parser.Expr;
import Parser.Stmt;

public class KnotRunner {

    // -------------------------------------------------------------------------
    // StmtEx — list wrapper with per-slot run-once flags
    // -------------------------------------------------------------------------

    public class StmtEx {
        List<Stmt> stmts = new ArrayList<>();
        List<Boolean> toRun = new ArrayList<>();

        public StmtEx(List<Stmt> stmts) {
            this.stmts = stmts;
            for (int i = 0; i < stmts.size(); i++) toRun.add(true);
        }

        public Stmt    get(int i)                 { return stmts.get(i); }
        public Boolean getToRanAt(int i)          { return toRun.get(i); }
        public void    setToRan(int i, boolean v) { toRun.set(i, v); }
        public int     size()                     { return stmts.size(); }
        public List<Stmt> reversed()              { return stmts.reversed(); }
        public int     indexOf(Stmt s)            { return stmts.indexOf(s); }
        public List<Stmt> getStmts()              { return stmts; }
    }

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private StmtEx         expression;
    private Interpreter    interp;
    private ControlGraph   graph;
    private TraversalEdge  priorEdge  = null;
    private final List<Object> routeTargets = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    public KnotRunner(Expr expr, List<Stmt> stmts, Interpreter interp) {
        this.expression = new StmtEx(stmts);
        this.interp     = interp;
        this.graph      = new KnotAnalyzer(stmts).analyze();
        initializeAllDeclarations();
        runSetupRegions();
    }

    // -------------------------------------------------------------------------
    // Main execution loop
    // -------------------------------------------------------------------------

    public TraversalEdge getPriorEdge() { return priorEdge; }
    public ControlGraph  getGraph()     { return graph; }

    public ArrayList<Object> runKnot() {
        int count = interp.isForward() ? 0 : expression.size() - 1;
        ArrayList<Object> notnull = new ArrayList<>();
        priorEdge = null;

        while (count >= 0 && count < expression.size()) {
            if (graph.getNodeAt(count) != null) {
                Integer next = selectStep(count);
                if (next != null) {
                    count = next;
                    continue;
                }
            } else {
                executeBodyStatement(count, notnull);
            }
            count = advanceOne(count);
        }

        return notnull;
    }

    public ArrayList<Object> runTonk() { return runKnot(); }

    // -------------------------------------------------------------------------
    // Phase C: edge-driven control step
    // -------------------------------------------------------------------------

    /**
     * Select and follow the outgoing edge from the control node at {@code index}.
     * Evaluates conditions, applies direction flips as side effects, sets priorEdge.
     * Returns the next count to jump to, or null if no control action (fall through
     * to body execution and advanceOne).
     */
    private Integer selectStep(int index) {
        return interp.isForward() ? selectStepForward(index) : selectStepBackward(index);
    }

    private Integer selectStepForward(int index) {
        // Oscillation: at false-exit of a forward condition.
        int fwdFalseStart = graph.forwardConditionStartForFalseExit(index);
        if (fwdFalseStart != -1) {
            int trueIndex = graph.forwardConditionTrueTarget(fwdFalseStart);
            if (checkConditionRange(fwdFalseStart, trueIndex)) {
                interp.setForward(false);
                priorEdge = condEdge(fwdFalseStart, EdgeKind.CONDITION_TRUE);
                return priorEdge != null ? priorEdge.to.expressionIndex : index - 1;
            } else {
                priorEdge = condEdge(fwdFalseStart, EdgeKind.CONDITION_FALSE);
                return expression.size();
            }
        }

        // Oscillation: at true-entry of a backward condition (re-check after forward pass).
        int bwdTrueStart = graph.backwardConditionStartForTrueEntry(index);
        if (bwdTrueStart != -1) {
            int bwdTrueIndex = graph.backwardConditionTrueTarget(bwdTrueStart);
            if (checkConditionRangeBackward(bwdTrueStart, bwdTrueIndex)) {
                interp.setForward(false);
                priorEdge = condEdge(bwdTrueStart, EdgeKind.CONDITION_TRUE);
                return priorEdge != null ? priorEdge.to.expressionIndex : index - 1;
            } else {
                priorEdge = condEdge(bwdTrueStart, EdgeKind.CONDITION_FALSE);
                return expression.size();
            }
        }

        // Normal forward condition start.
        if (graph.hasForwardCondition(index)) {
            int startIndex = graph.forwardConditionStart(index);
            int trueIndex  = graph.forwardConditionTrueTarget(index);
            int falseIndex = graph.forwardConditionFalseTarget(index);
            boolean met = checkConditionRange(startIndex, trueIndex);
            priorEdge = condEdge(startIndex, met ? EdgeKind.CONDITION_TRUE : EdgeKind.CONDITION_FALSE);
            if (priorEdge != null) return priorEdge.to.expressionIndex;
            int target = met ? trueIndex : falseIndex;
            if (target >= 0) return target + 1;
        }

        // UNWIND: region ahead is a structural escape (same-family close..close).
        ControlRegion ahead = graph.getRegionStartingAt(index);
        if (ahead != null && ahead.regionKind == RegionKind.UNWIND) {
            TraversalEdge e = unwindEdgeForward(index);
            if (e != null) { priorEdge = e; return e.to.expressionIndex; }
        }

        // ENTRY: region ahead is a structural descent (same-family open..open).
        if (ahead != null && (ahead.regionKind == RegionKind.POCKET_ENTRY
                           || ahead.regionKind == RegionKind.CUP_ENTRY)) {
            TraversalEdge e = entryEdgeForward(index);
            if (e != null) { priorEdge = e; return e.to.expressionIndex; }
        }

        // Forward structural boundary: flip to backward and follow backward adjacency.
        if (isForwardBoundary(index)) {
            interp.setForward(false);
            priorEdge = null;
            TraversalEdge back = condEdge(index, EdgeKind.BACKWARD_ADJACENCY);
            return back != null ? back.to.expressionIndex : index - 1;
        }

        return null;
    }

    private Integer selectStepBackward(int index) {
        // Oscillation: at false-exit of a backward condition.
        int bwdFalseStart = graph.backwardConditionStartForFalseExit(index);
        if (bwdFalseStart != -1) {
            int trueIndex = graph.backwardConditionTrueTarget(bwdFalseStart);
            if (checkConditionRangeBackward(bwdFalseStart, trueIndex)) {
                interp.setForward(true);
                priorEdge = condEdge(bwdFalseStart, EdgeKind.CONDITION_TRUE);
                return priorEdge != null ? priorEdge.to.expressionIndex : index + 1;
            } else {
                priorEdge = condEdge(bwdFalseStart, EdgeKind.CONDITION_FALSE);
                return -1;
            }
        }

        // Oscillation: at true-entry of a forward condition (re-check after backward pass).
        int fwdTrueStart = graph.forwardConditionStartForTrueEntry(index);
        if (fwdTrueStart != -1) {
            int fwdTrueIndex = graph.forwardConditionTrueTarget(fwdTrueStart);
            if (checkConditionRange(fwdTrueStart, fwdTrueIndex)) {
                interp.setForward(true);
                priorEdge = condEdge(fwdTrueStart, EdgeKind.CONDITION_TRUE);
                return priorEdge != null ? priorEdge.to.expressionIndex : index + 1;
            } else {
                priorEdge = condEdge(fwdTrueStart, EdgeKind.CONDITION_FALSE);
                return -1;
            }
        }

        // Normal backward condition start.
        if (graph.hasBackwardCondition(index)) {
            int startIndex = graph.backwardConditionStart(index);
            int trueIndex  = graph.backwardConditionTrueTarget(index);
            int falseIndex = graph.backwardConditionFalseTarget(index);
            boolean met = checkConditionRangeBackward(startIndex, trueIndex);
            priorEdge = condEdge(startIndex, met ? EdgeKind.CONDITION_TRUE : EdgeKind.CONDITION_FALSE);
            if (priorEdge != null) return priorEdge.to.expressionIndex;
            int target = met ? trueIndex : falseIndex;
            if (target >= 0) return target - 1;
        }

        // UNWIND: region behind is a structural escape (same-family close..close).
        ControlRegion behind = graph.getRegionEndingAt(index);
        if (behind != null && behind.regionKind == RegionKind.UNWIND) {
            TraversalEdge e = unwindEdgeBackward(index);
            if (e != null) { priorEdge = e; return e.to.expressionIndex; }
        }

        // ENTRY: region behind is a structural descent (same-family open..open).
        if (behind != null && (behind.regionKind == RegionKind.POCKET_ENTRY
                            || behind.regionKind == RegionKind.CUP_ENTRY)) {
            TraversalEdge e = entryEdgeBackward(index);
            if (e != null) { priorEdge = e; return e.to.expressionIndex; }
        }

        // Backward structural boundary: flip to forward and follow forward adjacency.
        if (isBackwardBoundary(index)) {
            interp.setForward(true);
            priorEdge = null;
            TraversalEdge fwd = condEdge(index, EdgeKind.FORWARD_ADJACENCY);
            return fwd != null ? fwd.to.expressionIndex : index + 1;
        }

        return null;
    }

    private int advanceOne(int index) {
        return index + (interp.isForward() ? 1 : -1);
    }

    private boolean isForwardBoundary(int index)  { return index == expression.size() - 1; }
    private boolean isBackwardBoundary(int index) { return index == 0; }

    /** Find the first outgoing edge of the given kind from the node at nodeIndex. */
    private TraversalEdge condEdge(int nodeIndex, EdgeKind kind) {
        ControlNode n = graph.getNodeAt(nodeIndex);
        if (n == null) return null;
        for (TraversalEdge e : graph.outEdges(n))
            if (e.kind == kind) return e;
        return null;
    }

    /** UNWIND edge going forward: targets the enclosing pair's CLOSE node. */
    private TraversalEdge unwindEdgeForward(int nodeIndex) {
        ControlNode n = graph.getNodeAt(nodeIndex);
        if (n == null) return null;
        for (TraversalEdge e : graph.outEdges(n))
            if (e.kind == EdgeKind.UNWIND && e.to.polarity == ControlPolarity.CLOSE)
                return e;
        return null;
    }

    /** UNWIND edge going backward: targets the enclosing pair's OPEN node. */
    private TraversalEdge unwindEdgeBackward(int nodeIndex) {
        ControlNode n = graph.getNodeAt(nodeIndex);
        if (n == null) return null;
        for (TraversalEdge e : graph.outEdges(n))
            if (e.kind == EdgeKind.UNWIND && e.to.polarity == ControlPolarity.OPEN)
                return e;
        return null;
    }

    /** ENTRY edge going forward: targets the nearest nested pair's OPEN node. */
    private TraversalEdge entryEdgeForward(int nodeIndex) {
        ControlNode n = graph.getNodeAt(nodeIndex);
        if (n == null) return null;
        for (TraversalEdge e : graph.outEdges(n))
            if (e.kind == EdgeKind.ENTRY && e.to.polarity == ControlPolarity.OPEN)
                return e;
        return null;
    }

    /** ENTRY edge going backward: targets the nearest nested pair's CLOSE node. */
    private TraversalEdge entryEdgeBackward(int nodeIndex) {
        ControlNode n = graph.getNodeAt(nodeIndex);
        if (n == null) return null;
        for (TraversalEdge e : graph.outEdges(n))
            if (e.kind == EdgeKind.ENTRY && e.to.polarity == ControlPolarity.CLOSE)
                return e;
        return null;
    }

    /**
     * Phase C: condition-evaluating edge selector.
     * Mirrors the priority order of selectStepForward/selectStepBackward.
     * Returns the edge that would be selected for the given (index, direction) —
     * does NOT evaluate conditions or apply direction flips; use selectStep for execution.
     */
    public TraversalEdge selectEdge(int index, boolean forward) {
        ControlNode node = graph.getNodeAt(index);
        if (node == null) return null;

        if (forward) {
            int fwdFalseStart = graph.forwardConditionStartForFalseExit(index);
            if (fwdFalseStart != -1) return condEdge(fwdFalseStart, EdgeKind.CONDITION_TRUE);

            int bwdTrueStart = graph.backwardConditionStartForTrueEntry(index);
            if (bwdTrueStart != -1) return condEdge(bwdTrueStart, EdgeKind.CONDITION_TRUE);

            if (graph.hasForwardCondition(index))
                return condEdge(graph.forwardConditionStart(index), EdgeKind.CONDITION_TRUE);

            if (isForwardBoundary(index)) return null;
            return condEdge(index, EdgeKind.FORWARD_ADJACENCY);
        } else {
            int bwdFalseStart = graph.backwardConditionStartForFalseExit(index);
            if (bwdFalseStart != -1) return condEdge(bwdFalseStart, EdgeKind.CONDITION_TRUE);

            int fwdTrueStart = graph.forwardConditionStartForTrueEntry(index);
            if (fwdTrueStart != -1) return condEdge(fwdTrueStart, EdgeKind.CONDITION_TRUE);

            if (graph.hasBackwardCondition(index))
                return condEdge(graph.backwardConditionStart(index), EdgeKind.CONDITION_TRUE);

            if (isBackwardBoundary(index)) return null;
            return condEdge(index, EdgeKind.BACKWARD_ADJACENCY);
        }
    }

    // -------------------------------------------------------------------------
    // Body execution
    // -------------------------------------------------------------------------

    private void executeBodyStatement(int index, ArrayList<Object> notnull) {
        if (!expression.getToRanAt(index)) return;
        Object result = interp.execute(expression.get(index));
        if (result != null) notnull.add(result);
    }

    // -------------------------------------------------------------------------
    // Condition range evaluation
    // -------------------------------------------------------------------------

    private boolean checkConditionRange(int startIndex, int trueIndex) {
        if (startIndex == -1 || trueIndex == -1) return false;
        boolean result = true;
        for (int i = startIndex + 1; i < trueIndex; i++) {
            Boolean temp = (Boolean) evalConditionExpr(i);
            if (temp == null) temp = false;
            result &= temp;
        }
        return result;
    }

    private boolean checkConditionRangeBackward(int startIndex, int trueIndex) {
        if (startIndex == -1 || trueIndex == -1) return false;
        boolean result = true;
        for (int i = startIndex - 1; i > trueIndex; i--) {
            Boolean temp = (Boolean) evalConditionExpr(i);
            if (temp == null) temp = false;
            result &= temp;
        }
        return result;
    }

    // Conditions are always written as forward expressions; evaluate
    // stmt.expression directly to bypass the interpreter's direction flag.
    private Object evalConditionExpr(int i) {
        Stmt s = expression.get(i);
        if (s instanceof Stmt.Expression) {
            Expr fwd = ((Stmt.Expression) s).expression;
            if (fwd != null) return interp.evaluate(fwd);
        }
        return interp.evaluate(s);
    }

    // -------------------------------------------------------------------------
    // Setup region initialization
    // -------------------------------------------------------------------------

    // Pre-scan: run every Stmt.Var / Stmt.Rav anywhere in the body so all
    // variables exist in the environment before any condition is evaluated.
    private void initializeAllDeclarations() {
        boolean savedDirection = interp.isForward();
        for (int i = 0; i < expression.size(); i++) {
            if (graph.getNodeAt(i) != null) continue;
            Stmt s = expression.get(i);
            if (s instanceof Stmt.Var) {
                interp.setForward(true);
                interp.execute(s);
                expression.setToRan(i, false);
            } else if (s instanceof Stmt.Rav) {
                interp.setForward(false);
                interp.execute(s);
                expression.setToRan(i, false);
            }
        }
        interp.setForward(savedDirection);
    }

    private void runSetupRegions() {
        for (ControlRegion r : graph.getRegions()) {
            if (r.regionKind == RegionKind.SETUP && !r.isEmpty()) {
                executeSetupRange(r.startIndex + 1, r.endIndex - 1);
            }
        }
    }

    private void executeSetupRange(int start, int end) {
        for (int i = start; i <= end; i++) {
            if (graph.getNodeAt(i) != null || !expression.getToRanAt(i)) continue;
            Stmt s = expression.get(i);
            Object container = resolveContainerRef(s);
            if (container != null) {
                routeTargets.add(container);
            } else {
                interp.execute(s);
            }
            expression.setToRan(i, false);
        }
    }

    private Object resolveContainerRef(Stmt s) {
        if (!(s instanceof Stmt.Expression)) return null;
        Expr e = ((Stmt.Expression) s).expression;
        if (!(e instanceof Expr.Variable)) return null;
        try {
            Object val = interp.evaluate(e);
            if (val instanceof BoxInstance || val instanceof CupInstance
                    || val instanceof PocketInstance || val instanceof TkpInstance)
                return val;
        } catch (RuntimeError ignored) {}
        return null;
    }

    // -------------------------------------------------------------------------
    // Bootstrapping + routing — called from pkt/tkp tick loops.
    // Route targets are container variable refs found in SETUP regions.
    // If the knot/tonk produces any result, it is add-injected into all targets.
    // -------------------------------------------------------------------------

    public void runWithRouting(boolean forKnot) {
        ArrayList<Object> results;
        try {
            results = forKnot ? runKnot() : runTonk();
        } catch (RuntimeError mismatch) {
            interp.addToErrorSink(mismatch.getMessage());
            return;
        }
        if (results.isEmpty() || routeTargets.isEmpty()) return;
        for (Object target : routeTargets) {
            injectIntoContainer(target, results);
        }
    }

    private void injectIntoContainer(Object container, java.util.List<Object> data) {
        java.util.List<Object> boxed = new java.util.ArrayList<>();
        for (Object item : data) boxed.add(Boxer.box(item, interp));
        if (container instanceof PocketInstance)  ((PocketInstance) container).body.addAll(boxed);
        else if (container instanceof TkpInstance) ((TkpInstance)   container).body.addAll(boxed);
        else if (container instanceof BoxInstance)  ((BoxInstance)   container).body.addAll(boxed);
        else if (container instanceof CupInstance)  ((CupInstance)   container).body.addAll(boxed);
        interp.notifyContainerModified(container);
    }

    // -------------------------------------------------------------------------
    // Unreachable region execution
    // -------------------------------------------------------------------------

    /** Execute all statements in an unreachable region, collecting non-null results. */
    private void runUnreachableRegion(ControlRegion r, ArrayList<Object> notnull) {
        for (int i = r.startIndex + 1; i < r.endIndex; i++) {
            if (graph.getNodeAt(i) != null || !expression.getToRanAt(i)) continue;
            executeBodyStatement(i, notnull);
        }
    }

    private List<ControlRegion> selectUnreachableTargets(UnreachableMode mode, ControlRegion seed) {
        switch (mode) {
            case DIRECT_REGION:    return Collections.singletonList(seed);
            case COMPONENT:        return graph.bfsUnreachableComponent(seed);
            case OWNERSHIP:        return graph.getUnreachableOwnershipGroup(seed);
            case CROSSING_CLUSTER: return graph.getUnreachableCrossingCluster(seed);
            case MIRROR:           return graph.getUnreachableMirrors(seed);
            case ALL_STRUCTURAL:   return graph.bfsUnreachable(seed);
            case DIAGNOSTIC:       return graph.getUnreachableRegions();
            default:               return Collections.emptyList();
        }
    }

    /**
     * Execute unreachable regions according to the given mode.
     * Returns non-null results collected across all selected regions.
     * Running unreachable regions does not change their reachability classification.
     */
    public ArrayList<Object> runUnreachable(UnreachableMode mode, ControlRegion seed) {
        ArrayList<Object> notnull = new ArrayList<>();
        for (ControlRegion r : selectUnreachableTargets(mode, seed))
            runUnreachableRegion(r, notnull);
        return notnull;
    }
}
