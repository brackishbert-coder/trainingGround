package Box.Interpreter;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Declaration.FunDecl;
import Parser.Declaration.StmtDecl;
import Parser.Expr;
import Parser.Fun;
import Parser.Fun.FunctionLink;
import Parser.Stmt;
import java.util.*;

public class LazyTemplateResolutionTest {
    static int passed = 0, failed = 0;
    static void ok(boolean cond, String name) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else       { System.out.println("  FAIL  " + name); failed++; }
    }
    static void eq(Object e, Object a, String name) {
        boolean ok = e == null ? a == null : e.equals(a);
        if (ok) { System.out.println("  PASS  " + name); passed++; }
        else    { System.out.println("  FAIL  " + name + "  expected=" + e + "  got=" + a); failed++; }
    }

    static Token id(String s) { return new Token(TokenType.IDENTIFIER, s, s, null, null, 0,0,0,0); }
    static Token tok(TokenType t) { return new Token(t, "", null, null, null, 0,0,0,0); }
    static Token strTok(String s) { return new Token(TokenType.STRING, "\""+s+"\"", s, null, null, 0,0,0,0); }
    static Token rev(String s) { return id(new StringBuilder(s).reverse().toString()); }
    static String[] DIGITS = {"0","1","2","3","4","5","6","7","8","9"};

    // Build NumberFormatLong raw slot tokens
    static List<Token> nflSlots() {
        List<Token> raw = new ArrayList<>();
        // @sg $_^X["","-"] gs@
        raw.addAll(Arrays.asList(tok(TokenType.AT), id("sg"),
            tok(TokenType.DOLLAR), tok(TokenType.UNDERSCORE),
            tok(TokenType.POWER), id("X"), tok(TokenType.OPENSQUARE),
            strTok(""), tok(TokenType.COMMA), strTok("-"),
            tok(TokenType.CLOSEDSQUARE), id("gs"), tok(TokenType.AT)));
        raw.add(tok(TokenType.COMMA));
        // @in $$_^[0-9] ni@
        raw.addAll(Arrays.asList(tok(TokenType.AT), id("in"),
            tok(TokenType.DOLLAR), tok(TokenType.DOLLAR), tok(TokenType.UNDERSCORE),
            tok(TokenType.POWER), tok(TokenType.OPENSQUARE)));
        for (int k = 0; k < DIGITS.length; k++) { raw.add(strTok(DIGITS[k])); if(k<DIGITS.length-1) raw.add(tok(TokenType.COMMA)); }
        raw.addAll(Arrays.asList(tok(TokenType.CLOSEDSQUARE), id("ni"), tok(TokenType.AT)));
        raw.add(tok(TokenType.COMMA));
        // @dp $"." pd@
        raw.addAll(Arrays.asList(tok(TokenType.AT), id("dp"),
            tok(TokenType.DOLLAR), strTok("."),
            id("pd"), tok(TokenType.AT)));
        raw.add(tok(TokenType.COMMA));
        // @fr $$_^[0-9] rf@
        raw.addAll(Arrays.asList(tok(TokenType.AT), id("fr"),
            tok(TokenType.DOLLAR), tok(TokenType.DOLLAR), tok(TokenType.UNDERSCORE),
            tok(TokenType.POWER), tok(TokenType.OPENSQUARE)));
        for (int k = 0; k < DIGITS.length; k++) { raw.add(strTok(DIGITS[k])); if(k<DIGITS.length-1) raw.add(tok(TokenType.COMMA)); }
        raw.addAll(Arrays.asList(tok(TokenType.CLOSEDSQUARE), id("rf"), tok(TokenType.AT)));
        return raw;
    }

    // Build a minimal Cup template declaration: #PreciseNumbers{ ... }PreciseNumbers
    // Uses Expr.Template wrapping an Expr.Cup with empty body
    static Declaration templateDecl(String name) {
        Token open  = id(name);
        Token close = id(new StringBuilder(name).reverse().toString());
        Expr.Cup cup = new Expr.Cup(open, new ArrayList<>(), name, close);
        Expr.Template tmpl = new Expr.Template(cup, new ArrayList<>(), null);
        return new StmtDecl(new Stmt.Expression(tmpl, null));
    }

    static Interpreter base() {
        Interpreter i = new Interpreter(); i.setForward(true); return i;
    }

    public static void main(String[] args) {

        System.out.println("--- type declared before template: resolvedTemplate null until template appears ---");
        {
            Interpreter i = base();
            ArrayList<Token> links = new ArrayList<>(); links.add(id("Arithmetic"));
            ArrayList<Token> mirrorLinks = new ArrayList<>(); mirrorLinks.add(rev("Arithmetic"));
            Expr.UserType ut = new Expr.UserType(id("NumberFormatLong"), links, id("PreciseNumbers"), nflSlots(),
                    rev("NumberFormatLong"), mirrorLinks, rev("PreciseNumbers"));
            List<Declaration> phase1 = new ArrayList<>();
            phase1.add(new StmtDecl(new Stmt.Expression(ut, null)));
            i.interpret(phase1);

            Interpreter.UserTypeEntry entry = i.userTypeRegistry.get(0);
            ok(entry.resolvedTemplate == null, "resolvedTemplate null before template declared");

            // Now declare template
            List<Declaration> phase2 = new ArrayList<>();
            phase2.add(templateDecl("PreciseNumbers"));
            i.interpret(phase2);

            ok(entry.resolvedTemplate != null, "resolvedTemplate non-null after template declared");
            ok(entry.resolvedTemplate instanceof BoxClass, "resolvedTemplate is BoxClass");
        }

        System.out.println("--- template declared before type: resolves immediately at type declaration ---");
        {
            Interpreter i = base();

            // Register template first
            List<Declaration> phase1 = new ArrayList<>();
            phase1.add(templateDecl("PreciseNumbers"));
            i.interpret(phase1);

            // Now declare type
            ArrayList<Token> links = new ArrayList<>(); links.add(id("Arithmetic"));
            ArrayList<Token> mirrorLinks = new ArrayList<>(); mirrorLinks.add(rev("Arithmetic"));
            Expr.UserType ut = new Expr.UserType(id("NumberFormatLong"), links, id("PreciseNumbers"), nflSlots(),
                    rev("NumberFormatLong"), mirrorLinks, rev("PreciseNumbers"));
            List<Declaration> phase2 = new ArrayList<>();
            phase2.add(new StmtDecl(new Stmt.Expression(ut, null)));
            i.interpret(phase2);

            Interpreter.UserTypeEntry entry = i.userTypeRegistry.get(0);
            ok(entry.resolvedTemplate != null, "resolvedTemplate set immediately");
            ok(entry.resolvedTemplate instanceof BoxClass, "resolvedTemplate is BoxClass");
        }

        System.out.println("--- template never declared: resolvedTemplate stays null (string ops default) ---");
        {
            Interpreter i = base();
            Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), id("Missing"), nflSlots(),
                    rev("Foo"), new ArrayList<>(), rev("Missing"));
            List<Declaration> stmts = new ArrayList<>();
            stmts.add(new StmtDecl(new Stmt.Expression(ut, null)));
            i.interpret(stmts);
            ok(i.userTypeRegistry.get(0).resolvedTemplate == null, "no template = null resolvedTemplate");
        }

        System.out.println("--- type without template: resolvedTemplate stays null ---");
        {
            Interpreter i = base();
            Expr.UserType ut = new Expr.UserType(id("Bare"), new ArrayList<>(), null, nflSlots(),
                    rev("Bare"), new ArrayList<>(), null);
            List<Declaration> stmts = new ArrayList<>();
            stmts.add(new StmtDecl(new Stmt.Expression(ut, null)));
            i.interpret(stmts);
            ok(i.userTypeRegistry.get(0).resolvedTemplate == null, "no templateName = null");
        }

        System.out.println("--- two types, one template: only matching entry resolved ---");
        {
            Interpreter i = base();
            Expr.UserType ut1 = new Expr.UserType(id("A"), new ArrayList<>(), id("PreciseNumbers"), nflSlots(),
                    rev("A"), new ArrayList<>(), rev("PreciseNumbers"));
            Expr.UserType ut2 = new Expr.UserType(id("B"), new ArrayList<>(), id("OtherTemplate"), nflSlots(),
                    rev("B"), new ArrayList<>(), rev("OtherTemplate"));
            List<Declaration> phase1 = new ArrayList<>();
            phase1.add(new StmtDecl(new Stmt.Expression(ut1, null)));
            phase1.add(new StmtDecl(new Stmt.Expression(ut2, null)));
            i.interpret(phase1);

            List<Declaration> phase2 = new ArrayList<>();
            phase2.add(templateDecl("PreciseNumbers"));
            i.interpret(phase2);

            ok(i.userTypeRegistry.get(0).resolvedTemplate != null, "A resolved");
            ok(i.userTypeRegistry.get(1).resolvedTemplate == null, "B stays null");
        }

        System.out.println("--- resolved template is the same BoxClass as in globals ---");
        {
            Interpreter i = base();
            Expr.UserType ut = new Expr.UserType(id("X"), new ArrayList<>(), id("PreciseNumbers"), nflSlots(),
                    rev("X"), new ArrayList<>(), rev("PreciseNumbers"));
            List<Declaration> phase1 = new ArrayList<>();
            phase1.add(new StmtDecl(new Stmt.Expression(ut, null)));
            phase1.add(templateDecl("PreciseNumbers"));
            i.interpret(phase1);

            Interpreter.UserTypeEntry entry = i.userTypeRegistry.get(0);
            Token tplToken = id("PreciseNumbers");
            Object fromGlobals = i.globals.get(tplToken, false);
            ok(entry.resolvedTemplate != null, "resolved");
            ok(entry.resolvedTemplate == fromGlobals, "same BoxClass instance as globals");
        }

        System.out.println("\nTotal: " + passed + " pass, " + failed + " fail");
    }
}
