package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;

/**
 * Helpers for the boxing invariant: every value at a storage boundary is a PCB Instance.
 *
 * box()      — wraps a primitive in BoxInstance; passes Instances through; routes
 *              non-boxable values to NON/LIMBO via the interpreter.
 * unbox()    — extracts the raw primitive from a single-item BoxInstance;
 *              no-op if the value is already a raw primitive.
 * isBoxable() — true iff the value can enter the PCB representation layer.
 */
public class Boxer {

    public static boolean isBoxable(Object v) {
        return v == null
            || v instanceof Double
            || v instanceof Integer
            || v instanceof String
            || v instanceof Boolean
            || v instanceof Instance;
    }

    public static Object box(Object value, Interpreter interp) {
        if (value instanceof Instance) return value;
        if (value == null || value instanceof Double || value instanceof Integer
                || value instanceof String || value instanceof Boolean) {
            List<Object> contents = new ArrayList<>();
            contents.add(value);
            return new BoxInstance(null, contents, null, interp);
        }
        // Non-boxable: route to NON/LIMBO.
        if (interp != null) interp.addToErrorSink("non-boxable value at storage boundary: "
                + value.getClass().getName());
        return null;
    }

    public static Object unbox(Object value) {
        if (value instanceof BoxInstance) {
            List<Object> body = ((BoxInstance) value).body;
            if (body.isEmpty()) return null;
            if (body.size() == 1) return body.get(0);
            throw new RuntimeError(
                new Token(TokenType.IDENTIFIER, "unbox", null, null, null, -1, -1, -1, -1),
                "unbox: multi-item BoxInstance requires explicit pop");
        }
        return value;
    }
}
