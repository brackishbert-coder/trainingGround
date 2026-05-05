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

public class CategoryNameAccessTest {
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

    // registers NumberFormatLong: @sg $^X["","-"] @gs , @in $$_^[digits] @ni , @dp $"." @pd , @fr $$_^[digits] @rf
    static Interpreter makeInterp() {
        Interpreter interp = new Interpreter();
        interp.setForward(true);
        List<Token> raw = new ArrayList<>();
        raw.addAll(slotXOR("sg", "gs", "", "-")); raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("in", "ni", DIGITS)); raw.add(tok(TokenType.COMMA));
        raw.addAll(slotLIT("dp", ".", "pd")); raw.add(tok(TokenType.COMMA));
        raw.addAll(slotMANY("fr", "rf", DIGITS));
        ArrayList<Token> links = new ArrayList<>(); links.add(id("Arithmetic"));
        Expr.UserType ut = new Expr.UserType(id("NumberFormatLong"), links, id("PreciseNumbers"), raw);
        List<Declaration> stmts = new ArrayList<>();
        stmts.add(new StmtDecl(new Stmt.Expression(ut, null)));
        interp.interpret(stmts);
        return interp;
    }

    static Stmt.Var inferVarStmt(String name, Expr value) {
        Token nameToken = id(name);
        Token type = new Token(TokenType.BOX, "box", null, null, null, 0,0,0,0);
        return new Stmt.Var(new Expr.Variable(nameToken), nameToken, type, 1, new Expr.Infer(value));
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

    static Token getatOp() { return new Token(TokenType.GETAT, "getat", null, null, null, 0,0,0,0); }
    static Token tategOp() { return new Token(TokenType.TATEG, "tateg", null, null, null, 0,0,0,0); }

    public static void main(String[] args) {

        System.out.println("--- getatByCategory: forward category name access on 3.14 ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> decls = new ArrayList<>();
            decls.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("3.14"))));
            i.interpret(decls);
            BoxInstance c = getVar(i, "c");
            ok(c != null, "c defined after infer");
            ok(c != null && c.body.size() == 4, "c has 4 groups");

            Interpreter.UserTypeEntry entry = i.userTypeRegistry.isEmpty() ? null : i.userTypeRegistry.get(0);
            ok(entry != null, "registry has entry");
            ok(entry != null && "NumberFormatLong".equals(entry.typeName), "entry is NumberFormatLong");

            if (entry != null && c != null) {
                i.typeContextStack.push(entry);
                try {
                    Object sg = i.getatByCategory(c, "sg", getatOp());
                    Object in = i.getatByCategory(c, "in", getatOp());
                    Object dp = i.getatByCategory(c, "dp", getatOp());
                    Object fr = i.getatByCategory(c, "fr", getatOp());
                    eq("",        groupStr(sg), "sg group = '' (no sign)");
                    eq("3",      groupStr(in), "in group = '3'");
                    eq(".",      groupStr(dp), "dp group = '.'");
                    eq("14",     groupStr(fr), "fr group = '14'");
                } finally {
                    i.typeContextStack.pop();
                }
            }
        }

        System.out.println("--- tategByCategory: backward category name access on -27.50 ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> decls = new ArrayList<>();
            decls.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("-27.50"))));
            i.interpret(decls);
            BoxInstance c = getVar(i, "c");
            Interpreter.UserTypeEntry entry = i.userTypeRegistry.get(0);

            if (entry != null && c != null) {
                i.typeContextStack.push(entry);
                try {
                    // tateg reverses the name before lookup: "gs"→"sg", "ni"→"in", "pd"→"dp", "rf"→"fr"
                    Object sg = i.tategByCategory(c, "gs", tategOp());
                    Object in = i.tategByCategory(c, "ni", tategOp());
                    Object dp = i.tategByCategory(c, "pd", tategOp());
                    Object fr = i.tategByCategory(c, "rf", tategOp());
                    eq("-",  groupStr(sg), "tateg gs→sg sign = '-'");
                    eq("27", groupStr(in), "tateg ni→in integer = '27'");
                    eq(".",  groupStr(dp), "tateg pd→dp decimal = '.'");
                    eq("50", groupStr(fr), "tateg rf→fr fraction = '50'");
                } finally {
                    i.typeContextStack.pop();
                }
            }
        }

        System.out.println("--- error: unknown category name ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> decls = new ArrayList<>();
            decls.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("1.0"))));
            i.interpret(decls);
            BoxInstance c = getVar(i, "c");
            Interpreter.UserTypeEntry entry = i.userTypeRegistry.get(0);
            i.typeContextStack.push(entry);
            try {
                boolean threw = false;
                try {
                    i.getatByCategory(c, "badcat", getatOp());
                } catch (RuntimeError e) {
                    threw = true;
                    ok(e.getMessage().contains("badcat"), "error mentions 'badcat'");
                }
                ok(threw, "throws RuntimeError for unknown category");
            } finally {
                i.typeContextStack.pop();
            }
        }

        System.out.println("--- error: no type context ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> decls = new ArrayList<>();
            decls.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("1.0"))));
            i.interpret(decls);
            BoxInstance c = getVar(i, "c");
            // no typeContextStack push
            boolean threw = false;
            try {
                i.getatByCategory(c, "sg", getatOp());
            } catch (RuntimeError e) {
                threw = true;
                ok(e.getMessage().contains("type-aware"), "error mentions 'type-aware'");
            }
            ok(threw, "throws RuntimeError when context stack empty");
        }

        System.out.println("--- all four slots of 3.14159 ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> decls = new ArrayList<>();
            decls.add(new StmtDecl(inferVarStmt("pi", new Expr.Literal("3.14159"))));
            i.interpret(decls);
            BoxInstance pi = getVar(i, "pi");
            Interpreter.UserTypeEntry entry = i.userTypeRegistry.get(0);
            i.typeContextStack.push(entry);
            try {
                eq("",        groupStr(i.getatByCategory(pi, "sg", getatOp())), "pi: no sign");
                eq("3",       groupStr(i.getatByCategory(pi, "in", getatOp())), "pi: integer part");
                eq(".",       groupStr(i.getatByCategory(pi, "dp", getatOp())), "pi: decimal point");
                eq("14159",   groupStr(i.getatByCategory(pi, "fr", getatOp())), "pi: fraction part");
            } finally {
                i.typeContextStack.pop();
            }
        }

        System.out.println("--- SystemDesigned fallback: value without decimal point ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> decls = new ArrayList<>();
            decls.add(new StmtDecl(inferVarStmt("n", new Expr.Literal("-42"))));
            i.interpret(decls);
            BoxInstance n = getVar(i, "n");
            ok(n != null, "n is defined");
            // "-42" has no "." so NumberFormatLong won't match → SystemDesigned: 1 group
            ok(n.body.size() == 1, "no match → SystemDesigned: 1 group");
            eq("-42", n.body.get(0), "SystemDesigned body[0] is raw value string");
        }

        System.out.println("--- context stack nesting: outer context stays after pop ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> decls = new ArrayList<>();
            decls.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("1.5"))));
            i.interpret(decls);
            BoxInstance c = getVar(i, "c");
            Interpreter.UserTypeEntry entry = i.userTypeRegistry.get(0);

            // Push twice — simulates nested method calls
            i.typeContextStack.push(entry);
            i.typeContextStack.push(entry);
            eq(2, i.typeContextStack.size(), "stack depth 2 after two pushes");
            i.typeContextStack.pop();
            eq(1, i.typeContextStack.size(), "stack depth 1 after one pop");
            // still accessible
            Object sg = i.getatByCategory(c, "sg", getatOp());
            ok(sg instanceof BoxInstance, "category still accessible after partial pop");
            i.typeContextStack.pop();
            eq(0, i.typeContextStack.size(), "stack empty after full pop");
        }

        System.out.println("--- negative number with sign ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> decls = new ArrayList<>();
            decls.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("-0.001"))));
            i.interpret(decls);
            BoxInstance c = getVar(i, "c");
            Interpreter.UserTypeEntry entry = i.userTypeRegistry.get(0);
            i.typeContextStack.push(entry);
            try {
                eq("-",   groupStr(i.getatByCategory(c, "sg", getatOp())), "sign = '-'");
                eq("0",   groupStr(i.getatByCategory(c, "in", getatOp())), "integer = '0'");
                eq(".",   groupStr(i.getatByCategory(c, "dp", getatOp())), "dp = '.'");
                eq("001", groupStr(i.getatByCategory(c, "fr", getatOp())), "fraction = '001'");
            } finally {
                i.typeContextStack.pop();
            }
        }

        System.out.println("--- tateg on positive number: all four reversed names ---");
        {
            Interpreter i = makeInterp();
            List<Declaration> decls = new ArrayList<>();
            decls.add(new StmtDecl(inferVarStmt("c", new Expr.Literal("99.9"))));
            i.interpret(decls);
            BoxInstance c = getVar(i, "c");
            Interpreter.UserTypeEntry entry = i.userTypeRegistry.get(0);
            i.typeContextStack.push(entry);
            try {
                eq("",        groupStr(i.tategByCategory(c, "gs", tategOp())), "tateg gs→sg empty sign");
                eq("99",      groupStr(i.tategByCategory(c, "ni", tategOp())), "tateg ni→in integer '99'");
                eq(".",       groupStr(i.tategByCategory(c, "pd", tategOp())), "tateg pd→dp decimal '.'");
                eq("9",       groupStr(i.tategByCategory(c, "rf", tategOp())), "tateg rf→fr fraction '9'");
            } finally {
                i.typeContextStack.pop();
            }
        }

        System.out.println("\n==================");
        System.out.println("CategoryNameAccessTest: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
