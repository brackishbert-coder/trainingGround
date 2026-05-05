package Box.Interpreter;

import java.util.List;

import Parser.Expr;

/**
 * PucInstance — execution-inversion cup.
 *
 * The container interface (push/pop/add/remove/getat/setat/sub/clear/empty/size)
 * is identical to CupInstance. The difference is in execute(): when the puc body
 * runs, the interpreter's invertedMode is toggled, which causes every arithmetic,
 * comparison, logical, and container operation inside the body to execute in its
 * inverse form.
 *
 * Inversion table (applied during body execution):
 *   PLUS ↔ MINUS,  TIMES ↔ FORWARDSLASH
 *   > ↔ <,  >= ↔ <=
 *   AND ↔ OR,  DNA ↔ RO
 *   QMARK (logical NOT): drops negation, returns operand truthiness
 *   push/add → pop,  remove/getat → pop
 *   pop → no-op,  setat → add
 *
 * Double inversion (puc inside a puc body) restores normal execution because
 * invertedMode is toggled, not set.
 */
public class PucInstance extends CupInstance implements IPuc {

    public PucInstance(BoxCallable boxClass, List<Object> body, Expr expr, Interpreter interpreter) {
        super(boxClass, body, expr, interpreter);
    }

    /** Skip-init: subclass manages body evaluation. */
    protected PucInstance(BoxCallable boxClass, List<Object> body, Expr expr, Interpreter interpreter, boolean _skipInit) {
        super(boxClass, body, expr, interpreter, true);
    }

    @Override
    public void execute() {
        boolean wasInverted = interpreter.isInverted();
        interpreter.setInverted(!wasInverted);
        try {
            super.execute();
        } finally {
            interpreter.setInverted(wasInverted);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("puc{");
        for (int i = 1; i < body.size() - 1; i++) {
            sb.append(body.get(i).toString()).append(" ");
        }
        sb.append("}cup");
        return sb.toString();
    }
}
