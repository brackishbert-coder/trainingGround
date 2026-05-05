package Box.Interpreter;

public enum MatchResult {
    MATCHED,
    UNMATCHED_OPEN,
    UNMATCHED_CLOSE,
    DUPLICATE_MATCH_CANDIDATE,
    NULL_LABEL,
    FAMILY_MISMATCH
}
