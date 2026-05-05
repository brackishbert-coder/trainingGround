package Box.Interpreter;

public class TraversalEdge {

    public final ControlNode from;
    public final ControlNode to;
    public final EdgeKind    kind;

    public TraversalEdge(ControlNode from, ControlNode to, EdgeKind kind) {
        this.from = from;
        this.to   = to;
        this.kind = kind;
    }

    @Override
    public String toString() {
        return "Edge[" + kind
                + " " + from.expressionIndex
                + "->" + to.expressionIndex + "]";
    }
}
