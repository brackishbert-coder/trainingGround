package Box.Interpreter;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;

import java.util.Arrays;
import java.util.List;

public class ContainerAggregateTest {
    static int passed = 0, failed = 0;

    static void ok(boolean cond, String name) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else       { System.out.println("  FAIL  " + name); failed++; }
    }
    static void eq(Object expected, Object actual, String name) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (ok) { System.out.println("  PASS  " + name); passed++; }
        else    { System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual); failed++; }
    }
    static void approx(double expected, Object actual, String name) {
        double got = actual instanceof Double ? (Double) actual : Double.NaN;
        boolean ok = Math.abs(got - expected) < 1e-9;
        if (ok) { System.out.println("  PASS  " + name); passed++; }
        else    { System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + got); failed++; }
    }

    static Token tok(TokenType t) {
        return new Token(t, t.name().toLowerCase(), null, null, null, 0, 0, 0, 0);
    }

    // Build a BoxInstance with the given body items
    static BoxInstance box(Object... items) {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return new BoxInstance(null, new java.util.ArrayList<>(Arrays.asList(items)), null, i);
    }

    static Object run(BoxInstance b, TokenType op) {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i.applyContainerAggregate(b.body, op, tok(op));
    }

    static Object runInv(BoxInstance b, TokenType op) {
        Interpreter i = new Interpreter();
        i.setForward(true);
        i.setInverted(true);
        return i.applyContainerAggregate(b.body, op, tok(op));
    }

    static List<Object> bodyOf(Object result) {
        return ((BoxInstance) result).body;
    }

    public static void main(String[] args) {

        System.out.println("--- sum ---");
        approx(15.0, run(box(1.0, 2.0, 3.0, 4.0, 5.0), TokenType.SUM), "sum(1..5) = 15");
        approx(0.0,  run(box(),                         TokenType.SUM), "sum(empty) = 0");
        approx(42.0, run(box(42.0),                     TokenType.SUM), "sum(single) = 42");
        approx(5.0,  run(box(2.0, 3.0, "skip"),         TokenType.SUM), "sum skips non-numeric");
        approx(15.0, run(box(1.0, 2.0, 3.0, 4.0, 5.0), TokenType.MUS), "mus(1..5) = 15 (bwd twin)");

        System.out.println("--- product ---");
        approx(120.0, run(box(1.0, 2.0, 3.0, 4.0, 5.0), TokenType.PRODUCT), "product(1..5) = 120");
        approx(1.0,   run(box(),                          TokenType.PRODUCT), "product(empty) = 1");
        approx(6.0,   run(box(2.0, 3.0),                 TokenType.PRODUCT), "product(2,3) = 6");
        approx(120.0, run(box(1.0, 2.0, 3.0, 4.0, 5.0), TokenType.TCUDORP),"tcudorp twin");

        System.out.println("--- mean ---");
        approx(3.0, run(box(1.0, 2.0, 3.0, 4.0, 5.0), TokenType.MEAN), "mean(1..5) = 3");
        approx(0.0, run(box(),                         TokenType.MEAN), "mean(empty) = 0");
        approx(5.0, run(box(5.0),                     TokenType.MEAN), "mean(single) = 5");
        approx(3.0, run(box(1.0, 2.0, 3.0, 4.0, 5.0), TokenType.NAEM), "naem twin");

        System.out.println("--- sort ascending ---");
        {
            List<Object> s = bodyOf(run(box(3.0, 1.0, 4.0, 1.0, 5.0, 9.0, 2.0, 6.0), TokenType.SORT));
            eq(1.0, s.get(0), "sort: first = 1");
            eq(9.0, s.get(s.size()-1), "sort: last = 9");
            eq(8,   s.size(), "sort: size preserved");
        }
        {
            List<Object> s = bodyOf(run(box("banana", "apple", "cherry"), TokenType.SORT));
            eq("apple",  s.get(0), "sort strings: apple first");
            eq("cherry", s.get(2), "sort strings: cherry last");
        }
        {
            List<Object> s = bodyOf(run(box(3.0, 1.0, 4.0, 1.0, 5.0, 9.0, 2.0, 6.0), TokenType.TROS));
            eq(1.0, s.get(0), "tros twin: first = 1");
        }

        System.out.println("--- sort descending (invertedMode) ---");
        {
            List<Object> s = bodyOf(runInv(box(3.0, 1.0, 5.0, 2.0, 4.0), TokenType.SORT));
            eq(5.0, s.get(0), "inverted sort: first = 5");
            eq(1.0, s.get(4), "inverted sort: last = 1");
        }

        System.out.println("--- minof ---");
        approx(1.0, run(box(3.0, 1.0, 4.0, 1.0, 5.0), TokenType.MINOF), "minof = 1");
        approx(1.0, run(box(3.0, 1.0, 4.0, 1.0, 5.0), TokenType.FONIM), "fonim twin");
        {
            Interpreter i = new Interpreter(); i.setForward(true);
            boolean threw = false;
            try { i.applyContainerAggregate(box().body, TokenType.MINOF, tok(TokenType.MINOF)); }
            catch (RuntimeError e) { threw = true; }
            ok(threw, "minof(empty) throws");
        }

        System.out.println("--- maxof ---");
        approx(9.0, run(box(3.0, 1.0, 4.0, 1.0, 5.0, 9.0, 2.0), TokenType.MAXOF), "maxof = 9");
        approx(9.0, run(box(3.0, 1.0, 4.0, 1.0, 5.0, 9.0, 2.0), TokenType.FOMAM), "fomam twin");
        {
            Interpreter i = new Interpreter(); i.setForward(true);
            boolean threw = false;
            try { i.applyContainerAggregate(box().body, TokenType.MAXOF, tok(TokenType.MAXOF)); }
            catch (RuntimeError e) { threw = true; }
            ok(threw, "maxof(empty) throws");
        }

        System.out.println("--- puc inversion: sum↔product, minof↔maxof ---");
        approx(120.0, runInv(box(1.0, 2.0, 3.0, 4.0, 5.0), TokenType.SUM),     "inverted sum → product");
        approx(15.0,  runInv(box(1.0, 2.0, 3.0, 4.0, 5.0), TokenType.PRODUCT), "inverted product → sum");
        approx(9.0,   runInv(box(3.0, 1.0, 4.0, 5.0, 9.0), TokenType.MINOF),   "inverted minof → maxof");
        approx(1.0,   runInv(box(3.0, 1.0, 4.0, 5.0, 9.0), TokenType.MAXOF),   "inverted maxof → minof");

        System.out.println("--- first / last ---");
        approx(1.0,  run(box(1.0, 2.0, 3.0), TokenType.FIRST), "first = 1");
        approx(3.0,  run(box(1.0, 2.0, 3.0), TokenType.LAST),  "last = 3");
        approx(1.0,  run(box(1.0, 2.0, 3.0), TokenType.TSRIF), "tsrif twin = 1");
        approx(3.0,  run(box(1.0, 2.0, 3.0), TokenType.TSAL),  "tsal twin = 3");
        eq(null,     run(box(),               TokenType.FIRST), "first(empty) = null");
        eq(null,     run(box(),               TokenType.LAST),  "last(empty) = null");

        System.out.println("--- puc inversion: first↔last ---");
        approx(3.0, runInv(box(1.0, 2.0, 3.0), TokenType.FIRST), "inverted first → last");
        approx(1.0, runInv(box(1.0, 2.0, 3.0), TokenType.LAST),  "inverted last → first");

        System.out.println("--- flip ---");
        {
            List<Object> f = bodyOf(run(box(1.0, 2.0, 3.0), TokenType.FLIP));
            eq(3.0, f.get(0), "flip: first = 3");
            eq(1.0, f.get(2), "flip: last = 1");
            eq(3,   f.size(), "flip: size preserved");
        }
        {
            List<Object> f = bodyOf(run(box(1.0, 2.0, 3.0), TokenType.PILF));
            eq(3.0, f.get(0), "pilf twin: first = 3");
        }
        {
            // flip self-inverse
            BoxInstance orig = box(1.0, 2.0, 3.0);
            BoxInstance flipped = (BoxInstance) run(orig, TokenType.FLIP);
            BoxInstance reFlipped = (BoxInstance) run(flipped, TokenType.FLIP);
            eq(orig.body.get(0), reFlipped.body.get(0), "flip(flip(x)) = x at [0]");
            eq(orig.body.get(2), reFlipped.body.get(2), "flip(flip(x)) = x at [2]");
        }
        // flip under invertedMode still flips
        {
            List<Object> f = bodyOf(runInv(box(1.0, 2.0, 3.0), TokenType.FLIP));
            eq(3.0, f.get(0), "inverted flip still reverses");
        }

        System.out.println("--- flat ---");
        {
            BoxInstance nested = box(1.0, box(2.0, 3.0), 4.0);
            List<Object> f = bodyOf(run(nested, TokenType.FLAT));
            eq(4,   f.size(), "flat: 4 items after flattening");
            eq(1.0, f.get(0), "flat[0] = 1");
            eq(2.0, f.get(1), "flat[1] = 2");
            eq(3.0, f.get(2), "flat[2] = 3");
            eq(4.0, f.get(3), "flat[3] = 4");
        }
        {
            List<Object> f = bodyOf(run(box(1.0, 2.0, 3.0), TokenType.FLAT));
            eq(3, f.size(), "flat on flat list: size unchanged");
        }
        {
            List<Object> f = bodyOf(run(box(), TokenType.FLAT));
            eq(0, f.size(), "flat(empty) = empty");
        }
        {
            List<Object> f = bodyOf(run(box(1.0, 2.0, 3.0), TokenType.TALF));
            eq(3, f.size(), "talf twin");
        }

        System.out.println("--- mean is unchanged by inversion ---");
        approx(3.0, runInv(box(1.0, 2.0, 3.0, 4.0, 5.0), TokenType.MEAN), "inverted mean still = 3");

        System.out.println("\n==================");
        System.out.println("ContainerAggregateTest: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
