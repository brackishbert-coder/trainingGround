package Box.Interpreter;

public enum EdgeKind {
    FORWARD_ADJACENCY,
    BACKWARD_ADJACENCY,
    CONDITION_TRUE,
    CONDITION_FALSE,
    OWNERSHIP_JUMP,
    UNWIND,
    ENTRY
}
