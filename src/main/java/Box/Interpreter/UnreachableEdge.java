package Box.Interpreter;

public class UnreachableEdge {

    public final ControlRegion     from;
    public final ControlRegion     to;
    public final UnreachableEdgeKind kind;

    public UnreachableEdge(ControlRegion from, ControlRegion to, UnreachableEdgeKind kind) {
        this.from = from;
        this.to   = to;
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "UnreachableEdge[" + kind + " " + from + " → " + to + "]";
    }
}
