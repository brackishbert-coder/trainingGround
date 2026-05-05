package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for boxify / deboxify operations.
 *
 * boxify   (forward  x.boxify)  — decomposes a String into a BoxInstance
 *                                  whose body is one single-char BoxInstance per char.
 * deboxify (forward  x.deboxify) — recursively flattens any Instance body into
 *                                  a single-string BoxInstance.
 * yfixob   (backward x.yfixob)  — same as boxify, fires on backward strand.
 * yfixobed (backward x.yfixobed) — same as deboxify, fires on backward strand.
 */
public class BoxifyDeboxifyTest {

    // ---- shared interpreter ------------------------------------------------

    private static Interpreter makeInterp() {
        return new Interpreter();
    }

    // ---- helpers -----------------------------------------------------------

    private static BoxInstance boxOf(String s, Interpreter i) {
        return (BoxInstance) Boxer.box(s, i);
    }

    private static String unboxStr(Object o) {
        Object raw = Boxer.unbox(o);
        return (raw instanceof String) ? (String) raw : String.valueOf(raw);
    }

    // ---- boxify (via doBoxify directly) ------------------------------------

    static int pass = 0, fail = 0;

    static void ok(String label, boolean condition) {
        if (condition) { pass++; }
        else           { fail++; System.out.println("FAIL: " + label); }
    }

    // -----------------------------------------------------------------------

    public static void main(String[] args) {

        // --- boxify: basic string -------------------------------------------

        {
            Interpreter i = makeInterp();
            BoxInstance src = boxOf("hello", i);
            BoxInstance result = i.doBoxify(src);
            ok("boxify produces BoxInstance", result instanceof BoxInstance);
            ok("boxify body size == string length", result.body.size() == 5);
            ok("boxify body[0] is BoxInstance", result.body.get(0) instanceof BoxInstance);
            ok("boxify body[0] == 'h'", "h".equals(unboxStr(result.body.get(0))));
            ok("boxify body[4] == 'o'", "o".equals(unboxStr(result.body.get(4))));
        }

        // --- boxify: single char -------------------------------------------

        {
            Interpreter i = makeInterp();
            BoxInstance result = i.doBoxify(boxOf("X", i));
            ok("boxify single char size == 1", result.body.size() == 1);
            ok("boxify single char == 'X'", "X".equals(unboxStr(result.body.get(0))));
        }

        // --- boxify: empty string -----------------------------------------

        {
            Interpreter i = makeInterp();
            BoxInstance result = i.doBoxify(boxOf("", i));
            ok("boxify empty string body is empty", result.body.isEmpty());
        }

        // --- boxify: each char is its own BoxInstance ---------------------

        {
            Interpreter i = makeInterp();
            BoxInstance result = i.doBoxify(boxOf("ab", i));
            ok("boxify each char is BoxInstance[0]", result.body.get(0) instanceof BoxInstance);
            ok("boxify each char is BoxInstance[1]", result.body.get(1) instanceof BoxInstance);
            BoxInstance c0 = (BoxInstance) result.body.get(0);
            BoxInstance c1 = (BoxInstance) result.body.get(1);
            ok("boxify char[0] body size == 1", c0.body.size() == 1);
            ok("boxify char[1] body size == 1", c1.body.size() == 1);
            ok("boxify char[0] value == 'a'", "a".equals(c0.body.get(0)));
            ok("boxify char[1] value == 'b'", "b".equals(c1.body.get(0)));
        }

        // --- boxify: double is stringified --------------------------------

        {
            Interpreter i = makeInterp();
            BoxInstance src = (BoxInstance) Boxer.box(42.0, i);
            BoxInstance result = i.doBoxify(src);
            ok("boxify double produces chars", result.body.size() > 0);
            // "42.0" → 4 chars
            ok("boxify 42.0 produces 4 chars", result.body.size() == 4);
            ok("boxify 42.0 first char '4'", "4".equals(unboxStr(result.body.get(0))));
        }

        // --- deboxify: flat single-string BoxInstance ---------------------

        {
            Interpreter i = makeInterp();
            BoxInstance src = boxOf("hello", i);
            BoxInstance result = i.doDeboxify(src);
            ok("deboxify flat string returns BoxInstance", result instanceof BoxInstance);
            ok("deboxify flat string body size == 1", result.body.size() == 1);
            ok("deboxify flat value == original", "hello".equals(result.body.get(0)));
        }

        // --- deboxify: nested (result of boxify) --------------------------

        {
            Interpreter i = makeInterp();
            BoxInstance boxified = i.doBoxify(boxOf("cat", i));
            BoxInstance result = i.doDeboxify(boxified);
            ok("deboxify(boxify) reconstructs string", "cat".equals(unboxStr(result)));
        }

        // --- deboxify: multi-item body ---------------------------------------

        {
            Interpreter i = makeInterp();
            // Manually build a BoxInstance with two string items
            List<Object> items = new ArrayList<>();
            items.add(boxOf("foo", i));
            items.add(boxOf("bar", i));
            BoxInstance nested = new BoxInstance(null, items, null, i);
            BoxInstance result = i.doDeboxify(nested);
            ok("deboxify multi-item concatenates", "foobar".equals(unboxStr(result)));
        }

        // --- deboxify: deeply nested -----------------------------------------

        {
            Interpreter i = makeInterp();
            // build [["a", "b"], "c"]
            List<Object> inner = new ArrayList<>();
            inner.add(boxOf("a", i));
            inner.add(boxOf("b", i));
            BoxInstance innerBox = new BoxInstance(null, inner, null, i);

            List<Object> outer = new ArrayList<>();
            outer.add(innerBox);
            outer.add(boxOf("c", i));
            BoxInstance outerBox = new BoxInstance(null, outer, null, i);

            BoxInstance result = i.doDeboxify(outerBox);
            ok("deboxify deeply nested reconstructs 'abc'", "abc".equals(unboxStr(result)));
        }

        // --- deboxify: empty body --------------------------------------------

        {
            Interpreter i = makeInterp();
            List<Object> empty = new ArrayList<>();
            BoxInstance src = new BoxInstance(null, empty, null, i);
            BoxInstance result = i.doDeboxify(src);
            ok("deboxify empty body gives empty string", "".equals(unboxStr(result)));
        }

        // --- deboxify: double items -----------------------------------------

        {
            Interpreter i = makeInterp();
            List<Object> items = new ArrayList<>();
            items.add(Boxer.box(1.0, i));
            items.add(Boxer.box(2.0, i));
            BoxInstance src = new BoxInstance(null, items, null, i);
            BoxInstance result = i.doDeboxify(src);
            ok("deboxify doubles concatenates string rep", "1.02.0".equals(unboxStr(result)));
        }

        // --- boxify→deboxify round-trip -------------------------------------

        {
            Interpreter i = makeInterp();
            String original = "roundtrip";
            BoxInstance boxified = i.doBoxify(boxOf(original, i));
            BoxInstance restored = i.doDeboxify(boxified);
            ok("boxify→deboxify round-trip", original.equals(unboxStr(restored)));
        }

        // --- deboxify: null body item ---------------------------------------

        {
            Interpreter i = makeInterp();
            List<Object> items = new ArrayList<>();
            items.add(Boxer.box(null, i));  // null BoxInstance
            BoxInstance src = new BoxInstance(null, items, null, i);
            BoxInstance result = i.doDeboxify(src);
            // null contributes nothing to the string
            ok("deboxify null body item contributes nothing", "".equals(unboxStr(result)));
        }

        // --- doBoxify null input -------------------------------------------

        {
            Interpreter i = makeInterp();
            BoxInstance nullBox = (BoxInstance) Boxer.box(null, i);
            // null unboxes to null; doBoxify returns a wrapped null
            BoxInstance result = i.doBoxify(nullBox);
            ok("boxify null returns BoxInstance", result instanceof BoxInstance);
        }

        // --- yfixob / yfixobed reachable via same helpers -------------------
        // (The visitor wiring is structural; we test the helpers directly since
        //  AST execution requires a full parse pass.)

        {
            Interpreter i = makeInterp();
            BoxInstance src = boxOf("XYZ", i);
            BoxInstance fwd = i.doBoxify(src);
            BoxInstance bwd = i.doBoxify(src);
            ok("yfixob uses same doBoxify: size matches", fwd.body.size() == bwd.body.size());
            ok("yfixob char[0] == 'X'", "X".equals(unboxStr(bwd.body.get(0))));
        }

        // --- summary --------------------------------------------------------

        System.out.println("\nBoxifyDeboxifyTest: " + pass + " passed, " + fail + " failed.");
        if (fail > 0) System.exit(1);
    }
}
