package Box.Interpreter;

public enum UnreachableMode {
    DIRECT_REGION,    // run exactly one unreachable region
    COMPONENT,        // run all regions in the connected component of the seed
    OWNERSHIP,        // run all unreachable regions inside the same matched pair as the seed
    CROSSING_CLUSTER, // run all unreachable regions in the same crossing cluster as the seed
    MIRROR,           // run the reverse-symmetric counterpart(s) of the seed
    ALL_STRUCTURAL,   // BFS over all unreachable regions from the seed (priority edge order)
    DIAGNOSTIC        // run every unreachable region in index order
}
