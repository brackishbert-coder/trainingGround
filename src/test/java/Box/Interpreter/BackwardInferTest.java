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

/**
 * Tests for backward ? inference: xob?, puc?, tkp? forms.
 * On the backward strand, the literal value is the spatial mirror of the
 * forward literal — digits/chars reversed. The inference engine reverses it
 * before running the slot matcher.
 */
public class BackwardInferTest {
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
    static Token strTok(String s) { return new Token(TokenType.STRING, "\""+s+"\"", s, null, null, 0,0,0,0); }
    static Token rev(String s) { return id(new StringBuilder(s).reverse().toString()); }

    static String[] DIGITS = {"0","1","2","3","4","5","6","7","8","9"};

    static List<Token> slotMANY(String cat, String catRev, String... chars) {
        List<Token> t = new ArrayList<>(Arrays.asList(
            tok(TokenType.AT), id(cat),
            tok(TokenType.DOLLAR), tok(TokenType.DOLLAR), tok(TokenType.UNDERSCORE),
            tok(TokenType.POWER), tok(TokenType.OPENSQUARE)));
        for (int i = 0; i < chars.length; i++) { t.add(strTok(chars[i])); if (i < chars.length-1) t.add(tok(TokenType.COMMA)); }
        t.addAll(Arrays.asList(tok(TokenType.CLOSEDSQUARE), id(catRev), tok(TokenType.AT)));
        return t;
    }
    static List<Token> slotLIT(String cat, String lit, String catRev) {
        return Arrays.asList(tok(TokenType.AT), id(cat), tok(TokenType.DOLLAR), strTok(lit), id(catRev), tok(TokenType.AT));
    }
    static List<Token> slotXOR(String cat, String catRev, String... vals) {
        List<Token> t = new ArrayList<>(Arrays.asList(
            tok(TokenType.AT), id(cat),
            tok(TokenType.DOLLAR), tok(TokenType.UNDERSCORE),
            tok(TokenType.POWER), id("X"), tok(TokenType.OPENSQUARE)));
        for (int i = 0; i < vals.length; i++) { t.add(strTok(vals[i])); if (i < vals.length-1) t.add(tok(TokenType.COMMA)); }
        t.addAll(Arrays.asList(tok(TokenType.CLOSEDSQUARE), id(catRev), tok(TokenType.AT)));
        return t;
    }

    static Interpreter makeInterp() {
        Interpreter interp = new Interpreter();
        interp.setForward(false); // backward strand
        List<Token> raw = new ArrayList<>();
        raw.addAll(slotXOR("sg", "gs", "", "-")); raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("in", "ni", DIGITS)); raw.add(tok(TokenType.COMMA));
        raw.addAll(slotLIT("dp", ".", "pd")); raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("fr", "rf", DIGITS));
        ArrayList<Token> links = new ArrayList<>(); links.add(id("Arithmetic"));
        ArrayList<Token> mirrorLinks = new ArrayList<>(); mirrorLinks.add(rev("Arithmetic"));
        Expr.UserType ut = new Expr.UserType(id("NumberFormatLong"), links, id("PreciseNumbers"), raw,
                rev("NumberFormatLong"), mirrorLinks, rev("PreciseNumbers"));
        List<Declaration> stmts = new ArrayList<>();
        stmts.add(new StmtDecl(new Stmt.Expression(ut, ut)));
        interp.interpret(stmts);
        return interp;
    }

    // Builds:  value = c xob?
    // i.e. Stmt.Rav(name=c, type=XOB, num=1, initilizer=Stmt.Expression(Infer(value, true), Infer(value, true)))
    static Stmt.Rav ravInferStmt(String name, Expr value) {
        Token nameToken = id(name);
        Token type = new Token(TokenType.XOB, "xob", null, null, null, 0,0,0,0);
        Expr.Infer infer = new Expr.Infer(value, true);
        return new Stmt.Rav(nameToken, type, 1, new Stmt.Expression(infer, infer));
    }

    static BoxInstance getVar(Interpreter interp, String name) {
        Object val = interp.globals.get(id(name), false);
        return val instanceof BoxInstance ? (BoxInstance) val : null;
    }

    static String groupStr(Object g) {
        if (!(g instanceof BoxInstance)) return "<not-a-box:" + g + ">";
        BoxInstance inner = (BoxInstance) g;
        return inner.body.isEmpty() ? "<empty>" : String.valueOf(inner.body.get(0));
    }

    // The isBackward flag on Expr.Infer
    static void testIsBackwardFlag() {
        System.out.println("--- Expr.Infer isBackward flag ---");
        Expr.Infer fwd = new Expr.Infer(new Expr.Literal("x"));
        ok(!fwd.isBackward, "default Infer is forward");

        Expr.Infer bwd = new Expr.Infer(new Expr.Literal("x"), true);
        ok(bwd.isBackward, "explicit backward Infer");

        // reverse() flips the flag
        bwd.reverse();
        ok(!bwd.isBackward, "reverse() flips isBackward");
        bwd.reverse();
        ok(bwd.isBackward, "reverse() flips back again");
    }

    // Forward Infer.visitInferExpr with isBackward=false: no reversal
    static void testForwardInferUnchanged() {
        System.out.println("--- forward Infer: value not reversed ---");
        Interpreter i = new Interpreter();
        i.setForward(true);
        List<Token> raw = new ArrayList<>();
        raw.addAll(slotXOR("sg", "gs", "", "-"));
        raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("in", "ni", DIGITS));
        raw.add(tok(TokenType.COMMA));
        raw.addAll(slotLIT("dp", ".", "pd"));
        raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("fr", "rf", DIGITS));
        Expr.UserType ut = new Expr.UserType(id("NumberFormatLong"), new ArrayList<>(), null, raw,
                rev("NumberFormatLong"), new ArrayList<>(), null);
        i.interpret(Arrays.asList(new StmtDecl(new Stmt.Expression(ut, null))));

        // forward ?box c = 3.14  →  infer "3.14"  → matched
        Token nameToken = id("c");
        Token type = new Token(TokenType.BOX, "box", null, null, null, 0,0,0,0);
        Expr.Infer infer = new Expr.Infer(new Expr.Literal("3.14"), false);
        Stmt.Var varStmt = new Stmt.Var(new Expr.Variable(nameToken), nameToken, type, 1, infer);
        i.interpret(Arrays.asList(new StmtDecl(varStmt)));
        BoxInstance c = getVar(i, "c");
        ok(c != null && c.body.size() == 4, "forward infer matched 4 groups");
        eq("3",  groupStr(c.body.get(1)), "forward: integer = '3'");
        eq("14", groupStr(c.body.get(3)), "forward: fraction = '14'");
    }

    // Backward Infer.visitInferExpr with isBackward=true: value reversed before matching
    static void testBackwardInferReverses() {
        System.out.println("--- backward Infer: value is reversed before matching ---");
        Interpreter i = makeInterp();
        // mirror of "3.14159" is "95141.3"
        List<Declaration> decls = new ArrayList<>();
        decls.add(new StmtDecl(ravInferStmt("c", new Expr.Literal("95141.3"))));
        i.interpret(decls);
        BoxInstance c = getVar(i, "c");
        ok(c != null, "c defined after backward infer");
        ok(c != null && c.body.size() == 4, "c matched 4 groups");
        eq("",      groupStr(c.body.get(0)), "no sign");
        eq("3",     groupStr(c.body.get(1)), "integer = '3'");
        eq(".",     groupStr(c.body.get(2)), "decimal point");
        eq("14159", groupStr(c.body.get(3)), "fraction = '14159'");
    }

    static void testBackwardNegative() {
        System.out.println("--- backward Infer: negative number ---");
        Interpreter i = makeInterp();
        // mirror of "-27.50" is "05.72-"
        List<Declaration> decls = new ArrayList<>();
        decls.add(new StmtDecl(ravInferStmt("c", new Expr.Literal("05.72-"))));
        i.interpret(decls);
        BoxInstance c = getVar(i, "c");
        ok(c != null && c.body.size() == 4, "negative backward matched 4 groups");
        eq("-",  groupStr(c.body.get(0)), "sign = '-'");
        eq("27", groupStr(c.body.get(1)), "integer = '27'");
        eq(".",  groupStr(c.body.get(2)), "dp = '.'");
        eq("50", groupStr(c.body.get(3)), "fraction = '50'");
    }

    static void testBackwardSymmetry() {
        System.out.println("--- forward/backward symmetry: same variable value ---");
        // forward: ?box c = "3.14"   →  groups: ["", "3", ".", "14"]
        // backward: "41.3" = c xob?  →  reverses "41.3" to "3.14"  →  same groups
        Interpreter fwd = new Interpreter();
        fwd.setForward(true);
        List<Token> raw = new ArrayList<>();
        raw.addAll(slotXOR("sg", "gs", "", "-"));
        raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("in", "ni", DIGITS));
        raw.add(tok(TokenType.COMMA));
        raw.addAll(slotLIT("dp", ".", "pd"));
        raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("fr", "rf", DIGITS));
        Expr.UserType ut = new Expr.UserType(id("NumberFormatLong"), new ArrayList<>(), null, raw,
                rev("NumberFormatLong"), new ArrayList<>(), null);
        fwd.interpret(Arrays.asList(new StmtDecl(new Stmt.Expression(ut, null))));
        Token nameF = id("c");
        Token typeF = new Token(TokenType.BOX, "box", null, null, null, 0,0,0,0);
        Stmt.Var varFwd = new Stmt.Var(new Expr.Variable(nameF), nameF, typeF, 1,
                new Expr.Infer(new Expr.Literal("3.14"), false));
        fwd.interpret(Arrays.asList(new StmtDecl(varFwd)));
        BoxInstance cFwd = getVar(fwd, "c");

        Interpreter bwd = makeInterp();
        bwd.interpret(Arrays.asList(new StmtDecl(ravInferStmt("c", new Expr.Literal("41.3")))));
        BoxInstance cBwd = getVar(bwd, "c");

        ok(cFwd != null && cBwd != null, "both defined");
        ok(cFwd != null && cBwd != null && cFwd.body.size() == cBwd.body.size(), "same group count");
        if (cFwd != null && cBwd != null) {
            eq(groupStr(cFwd.body.get(0)), groupStr(cBwd.body.get(0)), "sign matches");
            eq(groupStr(cFwd.body.get(1)), groupStr(cBwd.body.get(1)), "integer matches");
            eq(groupStr(cFwd.body.get(2)), groupStr(cBwd.body.get(2)), "dp matches");
            eq(groupStr(cFwd.body.get(3)), groupStr(cBwd.body.get(3)), "fraction matches");
        }
    }

    static void testBackwardSystemDesigned() {
        System.out.println("--- backward Infer: reversed value still no match → SystemDesigned ---");
        Interpreter i = makeInterp();
        // "42" reversed is "24" — still no "." so SystemDesigned
        List<Declaration> decls = new ArrayList<>();
        decls.add(new StmtDecl(ravInferStmt("n", new Expr.Literal("24"))));
        i.interpret(decls);
        BoxInstance n = getVar(i, "n");
        ok(n != null, "n defined");
        ok(n != null && n.body.size() == 1, "SystemDesigned: 1 group");
        eq("42", n.body.get(0), "body[0] is reversed value '42'");
    }

    static void testBackwardBothNamesbound() {
        System.out.println("--- backward Infer: both name and reverse name bound ---");
        Interpreter i = makeInterp();
        // "95141.3" reversed = "3.14159"
        List<Declaration> decls = new ArrayList<>();
        decls.add(new StmtDecl(ravInferStmt("c", new Expr.Literal("95141.3"))));
        i.interpret(decls);
        // Variable name bound
        Object cFwd = i.globals.get(id("c"), false);
        ok(cFwd instanceof BoxInstance, "c is bound as BoxInstance");
        // Reverse name "c" reversed = "c" (palindrome) — not a good test
        // Let's use a non-palindrome name
        decls = new ArrayList<>();
        decls.add(new StmtDecl(ravInferStmt("num", new Expr.Literal("95141.3"))));
        i.interpret(decls);
        Object numVal = i.globals.get(id("num"), false);
        Object munVal = i.globals.get(id("mun"), false);
        ok(numVal instanceof BoxInstance, "num is bound");
        ok(munVal instanceof BoxInstance, "mun (reverse) is also bound");
        ok(numVal == munVal, "both names point to same instance");
    }

    static void testIsBackwardFlagOnInferNode() {
        System.out.println("--- Expr.Infer.isBackward used in inference ---");
        Interpreter i = new Interpreter();
        i.setForward(true);
        List<Token> raw = new ArrayList<>();
        raw.addAll(slotXOR("sg", "gs", "", "-"));
        raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("in", "ni", DIGITS));
        raw.add(tok(TokenType.COMMA));
        raw.addAll(slotLIT("dp", ".", "pd"));
        raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("fr", "rf", DIGITS));
        Expr.UserType ut = new Expr.UserType(id("NumberFormatLong"), new ArrayList<>(), null, raw,
                rev("NumberFormatLong"), new ArrayList<>(), null);
        i.interpret(Arrays.asList(new StmtDecl(new Stmt.Expression(ut, null))));

        // isBackward=true on a literal "41.3" should match as "3.14"
        Expr.Infer bwdInfer = new Expr.Infer(new Expr.Literal("41.3"), true);
        Token nameToken = id("x");
        Token typeToken = new Token(TokenType.BOX, "box", null, null, null, 0,0,0,0);
        Stmt.Var varStmt = new Stmt.Var(new Expr.Variable(nameToken), nameToken, typeToken, 1, bwdInfer);
        i.interpret(Arrays.asList(new StmtDecl(varStmt)));
        BoxInstance x = getVar(i, "x");
        ok(x != null && x.body.size() == 4, "backward infer on forward strand: 4 groups");
        eq("3",  groupStr(x.body.get(1)), "integer '3' from reversed '41.3'");
        eq("14", groupStr(x.body.get(3)), "fraction '14' from reversed '41.3'");
    }

    public static void main(String[] args) {
        testIsBackwardFlag();
        testForwardInferUnchanged();
        testBackwardInferReverses();
        testBackwardNegative();
        testBackwardSymmetry();
        testBackwardSystemDesigned();
        testBackwardBothNamesbound();
        testIsBackwardFlagOnInferNode();

        System.out.println("\n==================");
        System.out.println("BackwardInferTest: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
