package Box.Interpreter;

public enum UnreachableEdgeKind {
    BOUNDARY,   // two regions share a control node
    OWNERSHIP,  // two regions inside the same matched pair
    ENCLOSURE,  // one region structurally contains another
    CROSSING,   // regions belong to the same crossing cluster
    MIRROR,     // regions are reversal counterparts of each other
    ROLE        // Setup ↔ Body ↔ Condition relationships within same local structure
}
