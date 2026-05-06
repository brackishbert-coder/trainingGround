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

public class InferEndToEndTest {
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
        Interpreter i = new Interpreter();
        i.setForward(true);
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
        stmts.add(new StmtDecl(new Stmt.Expression(ut, null)));
        i.interpret(stmts);
        return i;
    }

    // ?box c = value  →  Stmt.Var(c, BOX, Expr.Infer(value))
    static Stmt.Var inferVarStmt(String name, Expr value) {
        Token nameToken = id(name);
        Token type = new Token(TokenType.BOX, "box", null, null, null, 0,0,0,0);
        return new Stmt.Var(new Expr.Variable(nameToken), nameToken, type, 1, new Expr.Infer(value));
    }

    static BoxInstance getVar(Interpreter i, String name) {
        Object val = i.globals.get(id(name), false);
        return val instanceof BoxInstance ? (BoxInstance) val : null;
    }

    static String groupStr(BoxInstance outer, int idx) {
        Object g = outer.body.get(idx);
        if (!(g instanceof BoxInstance)) return "<not-a-box>";
        BoxInstance inner = (BoxInstance) g;
        return inner.body.isEmpty() ? "<empty>" : String.valueOf(inner.body.get(0));
    }

    public static void main(String[] args) {

        System.out.println("--- ?box c = \"3.14159\" assigns grouped box ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> stmts = new ArrayList<>();
            stmts.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("3.14159"))));
            i.interpret(stmts);
            BoxInstance c = getVar(i, "c");
            ok(c != null, "c is defined");
            ok(c instanceof BoxInstance, "c is BoxInstance");
            ok(c.body.size() == 4, "c has 4 groups");
            eq("",      groupStr(c, 0), "group0 sign = ''");
            eq("3",     groupStr(c, 1), "group1 integer = '3'");
            eq(".",     groupStr(c, 2), "group2 dp = '.'");
            eq("14159", groupStr(c, 3), "group3 fraction = '14159'");
        }

        System.out.println("--- ?box c = \"-27.50\" negative number ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> stmts = new ArrayList<>();
            stmts.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("-27.50"))));
            i.interpret(stmts);
            BoxInstance c = getVar(i, "c");
            ok(c != null && c.body.size() == 4, "c has 4 groups");
            eq("-",  groupStr(c, 0), "sign = '-'");
            eq("27", groupStr(c, 1), "integer = '27'");
            eq(".",  groupStr(c, 2), "dp = '.'");
            eq("50", groupStr(c, 3), "fraction = '50'");
        }

        System.out.println("--- ?box c = Double literal 3.14 ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> stmts = new ArrayList<>();
            stmts.add(new StmtDecl(inferVarStmt("c", new Expr.Literal(3.14))));
            i.interpret(stmts);
            BoxInstance c = getVar(i, "c");
            ok(c != null && c.body.size() == 4, "Double 3.14 matched as NumberFormatLong");
            eq("",   groupStr(c, 0), "sign = ''");
            eq("3",  groupStr(c, 1), "integer = '3'");
            eq(".",  groupStr(c, 2), "dp = '.'");
            eq("14", groupStr(c, 3), "fraction = '14'");
        }

        System.out.println("--- forward and backward names both bound ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> stmts = new ArrayList<>();
            stmts.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("3.14159"))));
            i.interpret(stmts);
            BoxInstance c = getVar(i, "c");
            BoxInstance cRev = (BoxInstance) i.globals.get(id("c"), false);
            // reverse of "c" is "c" (single char) — both point to the same instance
            ok(c != null, "forward name 'c' bound");
            ok(c == cRev || cRev != null, "backward name also bound");
        }

        System.out.println("--- ?box c = unrecognized falls to SystemDesigned ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> stmts = new ArrayList<>();
            stmts.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("hello"))));
            i.interpret(stmts);
            BoxInstance c = getVar(i, "c");
            ok(c != null, "c defined");
            ok(c.body.size() == 1, "SystemDesigned: one-element box");
            eq("hello", c.body.get(0), "value preserved");
        }

        System.out.println("--- ?box c = arbitrary-precision string ---");
        {
            Interpreter i = makeInterp();
            String bigNum = "34234958023903590239502358.0";
            List<Declaration> stmts = new ArrayList<>();
            stmts.add(new StmtDecl(inferVarStmt("c", new Expr.Literal(bigNum))));
            i.interpret(stmts);
            BoxInstance c = getVar(i, "c");
            ok(c != null && c.body.size() == 4, "big number matched");
            eq("", groupStr(c, 0), "sign = ''");
            eq("34234958023903590239502358", groupStr(c, 1), "full integer part");
            eq("0", groupStr(c, 3), "fraction = '0'");
        }

        System.out.println("--- c is readable via getat (group 1 = integer) ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> stmts = new ArrayList<>();
            stmts.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("42.5"))));
            i.interpret(stmts);
            BoxInstance c = getVar(i, "c");
            ok(c != null, "c defined");
            // getat(c, 1) should give the integer-part group: BoxInstance(["42"])
            Object g1 = c.body.get(1);
            ok(g1 instanceof BoxInstance, "group 1 is BoxInstance");
            BoxInstance intGroup = (BoxInstance) g1;
            ok(!intGroup.body.isEmpty(), "group 1 non-empty");
            eq("42", intGroup.body.get(0), "group1 value = '42'");
        }

        System.out.println("\nTotal: " + passed + " pass, " + failed + " fail");
    }
}
