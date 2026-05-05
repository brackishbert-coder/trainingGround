package Box.Interpreter;

public class Crossing {

    public final MatchPair   outer;
    public final MatchPair   inner;
    public final CrossingKind kind;

    public Crossing(MatchPair outer, MatchPair inner, CrossingKind kind) {
        this.outer = outer;
        this.inner = inner;
        this.kind  = kind;
    }

    @Override
    public String toString() {
        return "Crossing[" + kind
                + " outer=" + outer.pairId
                + " inner=" + inner.pairId + "]";
    }
}
