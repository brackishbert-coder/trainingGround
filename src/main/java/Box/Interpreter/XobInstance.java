package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Expr;
import Parser.Expr.Literal;

public class XobInstance extends BoxInstance implements IXob {

    public XobInstance(BoxCallable boxClass, List<Object> body, Expr expr, Interpreter interpreter) {
        super(boxClass, body, expr, interpreter);
    }

    private static RuntimeError opaque(String op) {
        return new RuntimeError(
            new Token(TokenType.IDENTIFIER, op, null, null, null, -1, -1, -1, -1),
            "xob: '" + op + "' not permitted — xob is opaque");
    }

    @Override
    public Object getat(Token operator, Object value) {
        return withToken(() -> { throw opaque("getat"); });
    }

    @Override
    public void setat(Literal index, Expr toset) {
        withTokenVoid(() -> { throw opaque("setat"); });
    }

    @Override
    public Object sub(Literal start, Literal end) {
        return withToken(() -> { throw opaque("sub"); });
    }

    @Override
    public boolean contains(Declaration contents) {
        return (boolean)(Boolean) withToken(() -> { throw opaque("contains"); });
    }

    @Override
    public Object remove(Token operator, Object value) {
        return withToken(() -> { throw opaque("remove"); });
    }

    @Override
    public String toString() {
        String str = "xob[";
        List<Object> body = this.body;
        for (int i = 0; i < body.size(); i++) {
            if (i == body.size() - 1)
                str += body.get(i).toString();
            else
                str += body.get(i).toString() + ",";
        }
        return str + "]box";
    }
}
