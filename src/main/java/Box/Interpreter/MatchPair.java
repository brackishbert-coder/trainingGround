package Box.Interpreter;

public class MatchPair {

    public final String        pairId;
    public final ControlNode   open;
    public final ControlNode   close;
    public final ControlFamily family;

    public MatchPair(String pairId, ControlNode open, ControlNode close) {
        this.pairId = pairId;
        this.open   = open;
        this.close  = close;
        this.family = open.family;
    }

    public int openIndex()  { return open.expressionIndex; }
    public int closeIndex() { return close.expressionIndex; }

    @Override
    public String toString() {
        return "Pair[" + pairId + " " + family
                + " open=" + open.expressionIndex
                + " close=" + close.expressionIndex + "]";
    }
}
