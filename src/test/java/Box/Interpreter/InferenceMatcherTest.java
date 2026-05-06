package Box.Interpreter;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Declaration.StmtDecl;
import Parser.Expr;
import Parser.Stmt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InferenceMatcherTest {
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

    static Token id(String s) { return new Token(TokenType.IDENTIFIER, s, s, null, null, 0,0,0,0); }
    static Token tok(TokenType t) { return new Token(t, "", null, null, null, 0,0,0,0); }
    static Token str(String s) { return new Token(TokenType.STRING, "\""+s+"\"", s, null, null, 0,0,0,0); }
    static Token rev(String s) { return id(new StringBuilder(s).reverse().toString()); }

    // @cat $$_^[chars...] catRev@
    static List<Token> slotMANY(String cat, String catRev, String... chars) {
        List<Token> t = new ArrayList<>(Arrays.asList(
            tok(TokenType.AT), id(cat),
            tok(TokenType.DOLLAR), tok(TokenType.DOLLAR), tok(TokenType.UNDERSCORE),
            tok(TokenType.POWER), tok(TokenType.OPENSQUARE)));
        for (int i = 0; i < chars.length; i++) {
            t.add(str(chars[i]));
            if (i < chars.length-1) t.add(tok(TokenType.COMMA));
        }
        t.addAll(Arrays.asList(tok(TokenType.CLOSEDSQUARE), id(catRev), tok(TokenType.AT)));
        return t;
    }
    // @cat $"lit" catRev@
    static List<Token> slotLIT(String cat, String lit, String catRev) {
        return Arrays.asList(tok(TokenType.AT), id(cat),
            tok(TokenType.DOLLAR), str(lit),
            id(catRev), tok(TokenType.AT));
    }
    // @cat $_^X[vals...] catRev@
    static List<Token> slotXOR(String cat, String catRev, String... vals) {
        List<Token> t = new ArrayList<>(Arrays.asList(
            tok(TokenType.AT), id(cat),
            tok(TokenType.DOLLAR), tok(TokenType.UNDERSCORE),
            tok(TokenType.POWER), id("X"), tok(TokenType.OPENSQUARE)));
        for (int i = 0; i < vals.length; i++) {
            t.add(str(vals[i]));
            if (i < vals.length-1) t.add(tok(TokenType.COMMA));
        }
        t.addAll(Arrays.asList(tok(TokenType.CLOSEDSQUARE), id(catRev), tok(TokenType.AT)));
        return t;
    }

    static String[] DIGITS = {"0","1","2","3","4","5","6","7","8","9"};

    // Build NumberFormatLong type and register it
    static Interpreter makeInterpWithNumberFormat() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        List<Token> raw = new ArrayList<>();
        raw.addAll(slotXOR("sg", "gs", "", "-")); raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("in", "ni", DIGITS)); raw.add(tok(TokenType.COMMA));
        raw.addAll(slotLIT("dp", ".", "pd")); raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("fr", "rf", DIGITS));
        ArrayList<Token> links = new ArrayList<>();
        links.add(id("Arithmetic"));
        ArrayList<Token> mirrorLinks = new ArrayList<>(); mirrorLinks.add(rev("Arithmetic"));
        Expr.UserType ut = new Expr.UserType(id("NumberFormatLong"), links, id("PreciseNumbers"), raw,
                rev("NumberFormatLong"), mirrorLinks, rev("PreciseNumbers"));
        List<Declaration> stmts = new ArrayList<>();
        stmts.add(new StmtDecl(new Stmt.Expression(ut, null)));
        i.interpret(stmts);
        return i;
    }

    // Run inference on a string value using given interpreter
    static BoxInstance infer(Interpreter i, String value) {
        Expr.Infer inferExpr = new Expr.Infer(new Expr.Literal(value));
        return (BoxInstance) i.visitInferExpr(inferExpr);
    }

    static String groupStr(BoxInstance outer, int idx) {
        Object g = outer.body.get(idx);
        if (!(g instanceof BoxInstance)) return "<not a box>";
        BoxInstance inner = (BoxInstance) g;
        if (inner.body.isEmpty()) return "<empty>";
        return String.valueOf(inner.body.get(0));
    }

    public static void main(String[] args) {
        System.out.println("--- SystemDesigned fallback ---");
        {
            Interpreter i = new Interpreter(); i.setForward(true);
            BoxInstance result = infer(i, "hello");
            ok(result != null, "result not null");
            ok(result.body.size() == 1, "one group");
            eq("hello", result.body.get(0), "value preserved");
        }

        System.out.println("--- NumberFormatLong matches 3.14159 ---");
        {
            Interpreter i = makeInterpWithNumberFormat();
            BoxInstance result = infer(i, "3.14159");
            ok(result != null, "result not null");
            ok(result.body.size() == 4, "four groups");
            eq("", groupStr(result, 0), "group0 sign = ''");
            eq("3", groupStr(result, 1), "group1 integer = '3'");
            eq(".", groupStr(result, 2), "group2 dp = '.'");
            eq("14159", groupStr(result, 3), "group3 fraction = '14159'");
        }

        System.out.println("--- NumberFormatLong matches negative number -27.50 ---");
        {
            Interpreter i = makeInterpWithNumberFormat();
            BoxInstance result = infer(i, "-27.50");
            ok(result != null, "result not null");
            ok(result.body.size() == 4, "four groups");
            eq("-", groupStr(result, 0), "group0 sign = '-'");
            eq("27", groupStr(result, 1), "group1 integer = '27'");
            eq(".", groupStr(result, 2), "group2 dp = '.'");
            eq("50", groupStr(result, 3), "group3 fraction = '50'");
        }

        System.out.println("--- arbitrary precision ---");
        {
            Interpreter i = makeInterpWithNumberFormat();
            BoxInstance result = infer(i, "34234958023903590239502358.0");
            ok(result != null, "matched");
            ok(result.body.size() == 4, "four groups");
            eq("", groupStr(result, 0), "sign=''");
            eq("34234958023903590239502358", groupStr(result, 1), "long integer part");
            eq(".", groupStr(result, 2), "dp='.'");
            eq("0", groupStr(result, 3), "fraction='0'");
        }

        System.out.println("--- fails non-matching input (letters) ---");
        {
            Interpreter i = makeInterpWithNumberFormat();
            // "abc" doesn't match NumberFormatLong → falls to SystemDesigned
            BoxInstance result = infer(i, "abc");
            ok(result != null, "always succeeds");
            ok(result.body.size() == 1, "SystemDesigned wraps in single box");
            eq("abc", result.body.get(0), "value preserved by SystemDesigned");
        }

        System.out.println("--- first declared type wins ---");
        {
            // Register two types: A matches "x", B matches "y"
            Interpreter i = new Interpreter(); i.setForward(true);
            // TypeA: @v $_^X["x"] v@
            List<Token> rawA = new ArrayList<>(slotXOR("v", "v", "x"));
            List<Token> rawB = new ArrayList<>(slotXOR("v", "v", "x", "y"));
            List<Declaration> stmts = new ArrayList<>();
            stmts.add(new StmtDecl(new Stmt.Expression(
                new Expr.UserType(id("TypeA"), new ArrayList<>(), null, rawA, rev("TypeA"), new ArrayList<>(), null), null)));
            stmts.add(new StmtDecl(new Stmt.Expression(
                new Expr.UserType(id("TypeB"), new ArrayList<>(), null, rawB, rev("TypeB"), new ArrayList<>(), null), null)));
            i.interpret(stmts);
            // "x" matches TypeA (first)
            BoxInstance r = infer(i, "x");
            ok(r.body.size() == 1, "one group from TypeA");
            eq("x", groupStr(r, 0), "value x");
        }

        System.out.println("--- Double value stringified correctly ---");
        {
            Interpreter i = makeInterpWithNumberFormat();
            // Simulate ?box c = 3.14 (Double value)
            Expr.Infer inferExpr = new Expr.Infer(new Expr.Literal(3.14));
            BoxInstance result = (BoxInstance) i.visitInferExpr(inferExpr);
            ok(result != null && result.body.size() == 4, "Double 3.14 matched as NumberFormatLong");
            eq("3", groupStr(result, 1), "integer part = '3'");
            eq("14", groupStr(result, 3), "fraction part = '14'");
        }

        System.out.println("\nTotal: " + passed + " pass, " + failed + " fail");
    }
}
