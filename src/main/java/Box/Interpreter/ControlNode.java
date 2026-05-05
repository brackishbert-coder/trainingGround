package Box.Interpreter;

import Parser.Stmt;

public class ControlNode {

    public final int             expressionIndex;
    public final ControlFamily   family;
    public final ControlPolarity polarity;
    public final String          rawLabel;
    public final String          normalizedLabel;
    public final Stmt.Expression sourceExpression;

    public MatchResult  matchResult    = MatchResult.UNMATCHED_OPEN;
    public ControlNode  matchedPartner = null;

    public ControlNode(int expressionIndex, ControlFamily family, ControlPolarity polarity,
                       String rawLabel, String normalizedLabel, Stmt.Expression sourceExpression) {
        this.expressionIndex  = expressionIndex;
        this.family           = family;
        this.polarity         = polarity;
        this.rawLabel         = rawLabel;
        this.normalizedLabel  = normalizedLabel;
        this.sourceExpression = sourceExpression;
    }

    @Override
    public String toString() {
        return "[" + expressionIndex + " " + family + "_" + polarity
                + " raw=" + rawLabel + " norm=" + normalizedLabel
                + " match=" + matchResult + "]";
    }
}
