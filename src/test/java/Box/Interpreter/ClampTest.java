package Box.Interpreter;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;

public class ClampTest {
    static int passed = 0, failed = 0;
    static void ok(boolean cond, String name) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else       { System.out.println("  FAIL  " + name); failed++; }
    }
    static void approx(double expected, double actual, String name) {
        boolean ok = Math.abs(actual - expected) < 1e-9;
        if (ok) { System.out.println("  PASS  " + name); passed++; }
        else    { System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual); failed++; }
    }

    static Token tok(TokenType t) { return new Token(t, t.name().toLowerCase(), null, null, null, 0,0,0,0); }

    static Expr clamp(double x, double lo, double hi) {
        return new Expr.Clamp(tok(TokenType.CLAMP), null,
                new Expr.Literal(x), new Expr.Literal(lo), new Expr.Literal(hi));
    }
    static Expr clampBwd(double x, double lo, double hi) {
        return new Expr.Clamp(tok(TokenType.PMALC), tok(TokenType.CLAMP),
                new Expr.Literal(x), new Expr.Literal(lo), new Expr.Literal(hi));
    }

    static double eval(Expr expr) {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return (Double) i.evaluate(expr);
    }

    static double evalInverted(Expr expr) {
        Interpreter i = new Interpreter();
        i.setForward(true);
        i.setInverted(true);
        return (Double) i.evaluate(expr);
    }

    public static void main(String[] args) {

        System.out.println("--- clamp: x below range → lo ---");
        approx(0.0,  eval(clamp(-5.0, 0.0, 10.0)), "clamp(-5, 0, 10) = 0");
        approx(3.0,  eval(clamp(1.0,  3.0, 8.0)),  "clamp(1, 3, 8) = 3");
        approx(-3.0, eval(clamp(-9.0, -3.0, -1.0)),"clamp(-9, -3, -1) = -3");

        System.out.println("--- clamp: x above range → hi ---");
        approx(10.0, eval(clamp(50.0,  0.0, 10.0)), "clamp(50, 0, 10) = 10");
        approx(8.0,  eval(clamp(100.0, 3.0, 8.0)),  "clamp(100, 3, 8) = 8");
        approx(-1.0, eval(clamp(5.0, -3.0, -1.0)),  "clamp(5, -3, -1) = -1");

        System.out.println("--- clamp: x within range → x ---");
        approx(5.0,  eval(clamp(5.0,  0.0, 10.0)),  "clamp(5, 0, 10) = 5");
        approx(3.14, eval(clamp(3.14, 0.0, 10.0)),  "clamp(3.14, 0, 10) = 3.14");
        approx(-2.0, eval(clamp(-2.0, -3.0, -1.0)), "clamp(-2, -3, -1) = -2");

        System.out.println("--- clamp: boundary values ---");
        approx(0.0,  eval(clamp(0.0,  0.0, 10.0)), "clamp(0, 0, 10) = 0  (at lo)");
        approx(10.0, eval(clamp(10.0, 0.0, 10.0)), "clamp(10, 0, 10) = 10  (at hi)");

        System.out.println("--- clamp: degenerate range lo == hi ---");
        approx(5.0, eval(clamp(3.0, 5.0, 5.0)), "clamp(3, 5, 5) = 5");
        approx(5.0, eval(clamp(5.0, 5.0, 5.0)), "clamp(5, 5, 5) = 5");
        approx(5.0, eval(clamp(9.0, 5.0, 5.0)), "clamp(9, 5, 5) = 5");

        System.out.println("--- clamp: fractional precision ---");
        approx(0.5, eval(clamp(0.5,  0.0, 1.0)), "clamp(0.5, 0, 1) = 0.5");
        approx(0.0, eval(clamp(-0.1, 0.0, 1.0)), "clamp(-0.1, 0, 1) = 0");
        approx(1.0, eval(clamp(1.1,  0.0, 1.0)), "clamp(1.1, 0, 1) = 1");

        System.out.println("--- clamp: self-inverse under invertedMode ---");
        approx(5.0,  evalInverted(clamp(5.0,  0.0, 10.0)), "inverted clamp(5, 0, 10) = 5");
        approx(0.0,  evalInverted(clamp(-1.0, 0.0, 10.0)), "inverted clamp(-1, 0, 10) = 0");
        approx(10.0, evalInverted(clamp(20.0, 0.0, 10.0)), "inverted clamp(20, 0, 10) = 10");

        System.out.println("--- clamp: lo > hi throws RuntimeError ---");
        {
            Interpreter i = new Interpreter();
            i.setForward(true);
            boolean threw = false;
            try {
                i.evaluate(clamp(5.0, 10.0, 0.0));
            } catch (RuntimeError e) {
                threw = true;
                ok(e.getMessage().contains("lo"), "error message mentions 'lo'");
                ok(e.getMessage().contains("10.0"), "error message contains lo value");
            }
            ok(threw, "lo > hi throws RuntimeError");
        }

        System.out.println("--- clamp: bidirectional form (pmalc operator) ---");
        approx(5.0,  eval(clampBwd(5.0,  0.0, 10.0)), "bwd clamp(5, 0, 10) = 5");
        approx(0.0,  eval(clampBwd(-5.0, 0.0, 10.0)), "bwd clamp(-5, 0, 10) = 0");
        approx(10.0, eval(clampBwd(15.0, 0.0, 10.0)), "bwd clamp(15, 0, 10) = 10");
        {
            Expr.Clamp c = (Expr.Clamp) clampBwd(5.0, 0.0, 10.0);
            ok(c.bwdToken != null,                      "bwdToken non-null");
            ok(c.bwdToken.type == TokenType.CLAMP,      "bwdToken is CLAMP");
            ok(c.operator.type == TokenType.PMALC,      "operator is PMALC");
        }

        System.out.println("--- Expr.Clamp node fields ---");
        {
            Expr.Clamp c = (Expr.Clamp) clamp(5.0, 0.0, 10.0);
            ok(c.operator.type == TokenType.CLAMP, "operator is CLAMP");
            ok(c.bwdToken == null,                 "no bwdToken in forward form");
            ok(c.value != null,                    "value field non-null");
            ok(c.lo != null,                       "lo field non-null");
            ok(c.hi != null,                       "hi field non-null");
        }

        System.out.println("--- clamp: large and small values ---");
        approx(1e6,  eval(clamp(1e9,  0.0, 1e6)),  "clamp(1e9, 0, 1e6) = 1e6");
        approx(0.0,  eval(clamp(-1e9, 0.0, 1e6)),  "clamp(-1e9, 0, 1e6) = 0");
        approx(1e-9, eval(clamp(1e-9, 0.0, 1.0)),  "clamp(1e-9, 0, 1) = 1e-9");

        System.out.println("\n==================");
        System.out.println("ClampTest: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
