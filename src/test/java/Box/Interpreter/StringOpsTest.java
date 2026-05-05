package Box.Interpreter;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;

public class StringOpsTest {
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

    static Token tok(TokenType t) { return new Token(t, t.name().toLowerCase(), null, null, null, 0,0,0,0); }

    static Expr mono(TokenType op, Expr value) {
        return new Expr.Mono(value, tok(op));
    }
    static Expr str(String s) { return new Expr.Literal(s); }

    static Object eval(Expr expr) {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i.evaluate(expr);
    }
    static Object evalInverted(Expr expr) {
        Interpreter i = new Interpreter();
        i.setForward(true);
        i.setInverted(true);
        return i.evaluate(expr);
    }
    static Object evalBwd(Expr expr) {
        // backward — uses findOnom via visitOnomExpr; need Onom node
        Interpreter i = new Interpreter();
        i.setForward(false);
        // Use Mono directly — findMono is called by visitMonoExpr only when forward
        // For backward, wrap in Monoonom with backward token
        return i.evaluate(expr);
    }

    public static void main(String[] args) {

        System.out.println("--- len: string length ---");
        approx(5.0, eval(mono(TokenType.LEN, str("hello"))),        "len(\"hello\") = 5");
        approx(0.0, eval(mono(TokenType.LEN, str(""))),              "len(\"\") = 0");
        approx(1.0, eval(mono(TokenType.LEN, str("x"))),             "len(\"x\") = 1");
        approx(13.0,eval(mono(TokenType.LEN, str("hello, world!"))), "len(\"hello, world!\") = 13");

        System.out.println("--- len: backward token nel ---");
        approx(3.0, eval(mono(TokenType.NEL, str("abc"))), "nel(\"abc\") = 3");

        System.out.println("--- upper: to uppercase ---");
        eq("HELLO",    eval(mono(TokenType.UPPER, str("hello"))),    "upper(\"hello\")");
        eq("HELLO",    eval(mono(TokenType.UPPER, str("HELLO"))),    "upper already upper");
        eq("HELLO123", eval(mono(TokenType.UPPER, str("hello123"))), "upper with digits");
        eq("",         eval(mono(TokenType.UPPER, str(""))),          "upper empty string");

        System.out.println("--- lower: to lowercase ---");
        eq("hello",    eval(mono(TokenType.LOWER, str("HELLO"))),    "lower(\"HELLO\")");
        eq("hello",    eval(mono(TokenType.LOWER, str("hello"))),    "lower already lower");
        eq("hello123", eval(mono(TokenType.LOWER, str("HELLO123"))), "lower with digits");

        System.out.println("--- upper/lower puc inversion ---");
        // under invertedMode: upper → lower, lower → upper
        eq("hello", evalInverted(mono(TokenType.UPPER, str("HELLO"))), "inverted upper → lower");
        eq("HELLO", evalInverted(mono(TokenType.LOWER, str("hello"))), "inverted lower → upper");
        // reppu/rewol are backward-strand twins: same op as upper/lower, same inversion
        eq("hello", evalInverted(mono(TokenType.REPPU, str("HELLO"))), "inverted reppu → lower");
        eq("HELLO", evalInverted(mono(TokenType.REWOL, str("hello"))), "inverted rewol → upper");

        System.out.println("--- reppu/rewol forward (backward strand tokens, same op as twin) ---");
        eq("HELLO", eval(mono(TokenType.REPPU, str("hello"))), "reppu forward → upper (same as upper)");
        eq("hello", eval(mono(TokenType.REWOL, str("HELLO"))), "rewol forward → lower (same as lower)");

        System.out.println("--- rev: string reversal ---");
        eq("olleh",   eval(mono(TokenType.STRREV, str("hello"))),   "rev(\"hello\") = \"olleh\"");
        eq("",        eval(mono(TokenType.STRREV, str(""))),          "rev(\"\") = \"\"");
        eq("a",       eval(mono(TokenType.STRREV, str("a"))),         "rev(\"a\") = \"a\"");
        eq("dcba",    eval(mono(TokenType.STRREV, str("abcd"))),      "rev(\"abcd\") = \"dcba\"");
        eq("321",     eval(mono(TokenType.STRREV, str("123"))),       "rev(\"123\") = \"321\"");

        System.out.println("--- rev: self-inverse ---");
        eq("hello", eval(mono(TokenType.STRREV, mono(TokenType.STRREV, str("hello")))), "rev(rev(s)) = s");

        System.out.println("--- ver: backward token for rev ---");
        eq("olleh", eval(mono(TokenType.VERUTS, str("hello"))), "ver(\"hello\") = \"olleh\"");

        System.out.println("--- trim: remove whitespace ---");
        eq("hello",   eval(mono(TokenType.TRIM, str("  hello  "))),   "trim strips spaces");
        eq("hello",   eval(mono(TokenType.TRIM, str("\thello\n"))),    "trim strips tab/newline");
        eq("hello",   eval(mono(TokenType.TRIM, str("hello"))),        "trim no-op on clean string");
        eq("",        eval(mono(TokenType.TRIM, str("   "))),           "trim all-space → empty");
        eq("he lo",   eval(mono(TokenType.TRIM, str("  he lo  "))),    "trim preserves internal space");

        System.out.println("--- mirt: backward token for trim ---");
        eq("hello", eval(mono(TokenType.MIRT, str("  hello  "))), "mirt strips spaces");

        System.out.println("--- num: string to double ---");
        approx(42.0,   eval(mono(TokenType.NUM, str("42"))),     "num(\"42\") = 42");
        approx(3.14,   eval(mono(TokenType.NUM, str("3.14"))),   "num(\"3.14\") = 3.14");
        approx(-7.0,   eval(mono(TokenType.NUM, str("-7"))),     "num(\"-7\") = -7");
        approx(0.0,    eval(mono(TokenType.NUM, str("0"))),      "num(\"0\") = 0");
        approx(1e10,   eval(mono(TokenType.NUM, str("1e10"))),   "num(\"1e10\") = 1e10");
        approx(42.0,   eval(mono(TokenType.NUM, str("  42  "))), "num trims whitespace");

        System.out.println("--- mun: backward token for num ---");
        approx(99.0, eval(mono(TokenType.MUN, str("99"))), "mun(\"99\") = 99");

        System.out.println("--- num: non-numeric string throws RuntimeError ---");
        {
            Interpreter i = new Interpreter();
            i.setForward(true);
            boolean threw = false;
            try {
                i.evaluate(mono(TokenType.NUM, str("hello")));
            } catch (RuntimeError e) {
                threw = true;
                ok(e.getMessage().contains("hello"), "error message contains bad value");
            }
            ok(threw, "num(non-numeric) throws RuntimeError");
        }

        System.out.println("--- rev under invertedMode: still reverses (self-inverse) ---");
        eq("olleh", evalInverted(mono(TokenType.STRREV, str("hello"))), "inverted rev still reverses");

        System.out.println("--- len under invertedMode: still returns length ---");
        approx(5.0, evalInverted(mono(TokenType.LEN, str("hello"))), "inverted len still 5");

        System.out.println("--- string ops on numeric literal (coercion) ---");
        approx(2.0, eval(mono(TokenType.LEN, new Expr.Literal(42.0))),    "len(42.0) → len(\"42.0\") = 4... no, depends on stringify");
        // stringify(42.0) in PCB — let's just check it doesn't crash and returns a number
        ok(eval(mono(TokenType.LEN, new Expr.Literal(42.0))) instanceof Double, "len(double) returns Double");
        ok(eval(mono(TokenType.UPPER, new Expr.Literal(42.0))) instanceof String, "upper(double) returns String");

        System.out.println("\n==================");
        System.out.println("StringOpsTest: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
