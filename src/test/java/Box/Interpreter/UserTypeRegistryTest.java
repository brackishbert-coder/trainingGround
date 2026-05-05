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

public class UserTypeRegistryTest {
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

    static Token id(String s) {
        return new Token(TokenType.IDENTIFIER, s, s, null, null, 0, 0, 0, 0);
    }
    static Token tok(TokenType t) {
        return new Token(t, "", null, null, null, 0, 0, 0, 0);
    }
    static Token str(String s) {
        return new Token(TokenType.STRING, "\"" + s + "\"", s, null, null, 0, 0, 0, 0);
    }

    static Interpreter interp(Expr.UserType ut) {
        Interpreter i = new Interpreter();
        i.setForward(true);
        List<Declaration> stmts = new ArrayList<>();
        stmts.add(new StmtDecl(new Stmt.Expression(ut, null)));
        i.interpret(stmts);
        return i;
    }

    // build: @sg $_ gs@
    static List<Token> slots_ONE(String cat, String catRev) {
        return Arrays.asList(tok(TokenType.AT), id(cat),
                tok(TokenType.DOLLAR), tok(TokenType.UNDERSCORE),
                id(catRev), tok(TokenType.AT));
    }

    // build: @in $$_^["0","1"] ni@
    static List<Token> slots_MANY(String cat, String catRev, String... vals) {
        List<Token> toks = new ArrayList<>(Arrays.asList(
                tok(TokenType.AT), id(cat),
                tok(TokenType.DOLLAR), tok(TokenType.DOLLAR), tok(TokenType.UNDERSCORE),
                tok(TokenType.POWER), tok(TokenType.OPENSQUARE)));
        for (int i = 0; i < vals.length; i++) {
            toks.add(str(vals[i]));
            if (i < vals.length - 1) toks.add(tok(TokenType.COMMA));
        }
        toks.addAll(Arrays.asList(tok(TokenType.CLOSEDSQUARE), id(catRev), tok(TokenType.AT)));
        return toks;
    }

    // build: @dp $"." pd@
    static List<Token> slots_LIT(String cat, String literal, String catRev) {
        return Arrays.asList(tok(TokenType.AT), id(cat),
                tok(TokenType.DOLLAR), str(literal),
                id(catRev), tok(TokenType.AT));
    }

    // build: @sg $_^X["","-"] gs@
    static List<Token> slots_ONE_XOR(String cat, String catRev, String... vals) {
        List<Token> toks = new ArrayList<>(Arrays.asList(
                tok(TokenType.AT), id(cat),
                tok(TokenType.DOLLAR), tok(TokenType.UNDERSCORE),
                tok(TokenType.POWER), id("X"), tok(TokenType.OPENSQUARE)));
        for (int i = 0; i < vals.length; i++) {
            toks.add(str(vals[i]));
            if (i < vals.length - 1) toks.add(tok(TokenType.COMMA));
        }
        toks.addAll(Arrays.asList(tok(TokenType.CLOSEDSQUARE), id(catRev), tok(TokenType.AT)));
        return toks;
    }

    static List<Token> concat(List<Token>... lists) {
        List<Token> result = new ArrayList<>();
        for (List<Token> l : lists) result.addAll(l);
        return result;
    }

    public static void main(String[] args) {
        System.out.println("--- simple type registers with one slot ---");
        {
            List<Token> raw = new ArrayList<>(slots_ONE("sg", "gs"));
            Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), null, raw);
            Interpreter i = interp(ut);
            ok(i.userTypeRegistry.size() == 1, "registry size 1");
            Interpreter.UserTypeEntry e = i.userTypeRegistry.get(0);
            eq("Foo", e.typeName, "typeName=Foo");
            ok(e.linkNames.isEmpty(), "no links");
            ok(e.templateName == null, "no template");
            ok(e.slots.size() == 1, "one slot");
        }

        System.out.println("--- ONE slot descriptor fields ---");
        {
            List<Token> raw = new ArrayList<>(slots_ONE("sg", "gs"));
            Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), null, raw);
            Interpreter i = interp(ut);
            Expr.SlotDescriptor s = i.userTypeRegistry.get(0).slots.get(0);
            eq("sg", s.category, "category=sg");
            eq(Expr.SlotDescriptor.Multiplicity.ONE, s.mult, "mult=ONE");
            ok(!s.literalSlot, "not literal");
        }

        System.out.println("--- MANY slot descriptor ---");
        {
            List<Token> raw = new ArrayList<>(slots_MANY("in", "ni", "0","1","2"));
            Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), null, raw);
            Interpreter i = interp(ut);
            Expr.SlotDescriptor s = i.userTypeRegistry.get(0).slots.get(0);
            eq("in", s.category, "category=in");
            eq(Expr.SlotDescriptor.Multiplicity.MANY, s.mult, "mult=MANY");
            ok(!s.literalSlot, "not literal");
            eq(3, s.values.size(), "three constraint values");
        }

        System.out.println("--- literal slot ---");
        {
            List<Token> raw = new ArrayList<>(slots_LIT("dp", ".", "pd"));
            Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), null, raw);
            Interpreter i = interp(ut);
            Expr.SlotDescriptor s = i.userTypeRegistry.get(0).slots.get(0);
            eq("dp", s.category, "category=dp");
            ok(s.literalSlot, "literalSlot=true");
            ok(s.values.size() == 1 && ".".equals(s.values.get(0)), "value='.'");
        }

        System.out.println("--- exclusive OR slot ---");
        {
            List<Token> raw = new ArrayList<>(slots_ONE_XOR("sg", "gs", "", "-"));
            Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), null, raw);
            Interpreter i = interp(ut);
            Expr.SlotDescriptor s = i.userTypeRegistry.get(0).slots.get(0);
            ok(s.exclusiveOr, "exclusiveOr=true");
            ok(s.values.size() == 2, "two constraint values");
            ok(s.values.contains(""), "contains empty string");
            ok(s.values.contains("-"), "contains '-'");
        }

        System.out.println("--- link and template stored ---");
        {
            List<Token> raw = new ArrayList<>(slots_ONE("sg", "gs"));
            ArrayList<Token> links = new ArrayList<>();
            links.add(id("Arithmetic"));
            Expr.UserType ut = new Expr.UserType(id("NumberFormatLong"), links, id("PreciseNumbers"), raw);
            Interpreter i = interp(ut);
            Interpreter.UserTypeEntry e = i.userTypeRegistry.get(0);
            eq("NumberFormatLong", e.typeName, "typeName");
            ok(e.linkNames.size() == 1 && "Arithmetic".equals(e.linkNames.get(0)), "link=Arithmetic");
            eq("PreciseNumbers", e.templateName, "template=PreciseNumbers");
        }

        System.out.println("--- two types in declaration order ---");
        {
            Interpreter i = new Interpreter();
            i.setForward(true);
            List<Declaration> stmts = new ArrayList<>();
            stmts.add(new StmtDecl(new Stmt.Expression(
                    new Expr.UserType(id("A"), new ArrayList<>(), null, new ArrayList<>(slots_ONE("sg","gs"))), null)));
            stmts.add(new StmtDecl(new Stmt.Expression(
                    new Expr.UserType(id("B"), new ArrayList<>(), null, new ArrayList<>(slots_ONE("in","ni"))), null)));
            i.interpret(stmts);
            ok(i.userTypeRegistry.size() == 2, "two types");
            eq("A", i.userTypeRegistry.get(0).typeName, "first=A");
            eq("B", i.userTypeRegistry.get(1).typeName, "second=B");
        }

        System.out.println("--- anchor rule: adjacent MANY rejected ---");
        {
            List<Token> raw = new ArrayList<>();
            raw.addAll(slots_MANY("a", "sa", "x"));
            raw.add(tok(TokenType.COMMA));
            raw.addAll(slots_MANY("b", "ab", "y"));
            Expr.UserType ut = new Expr.UserType(id("Bad"), new ArrayList<>(), null, raw);
            boolean threw = false;
            try { interp(ut); } catch (RuntimeException ex) { threw = true; }
            ok(threw, "adjacent MANY without anchor rejected");
        }

        System.out.println("--- anchor rule: MANY + literal + MANY is OK ---");
        {
            List<Token> raw = new ArrayList<>();
            raw.addAll(slots_MANY("in", "ni", "0","1","2","3","4","5","6","7","8","9"));
            raw.add(tok(TokenType.COMMA));
            raw.addAll(slots_LIT("dp", ".", "pd"));
            raw.add(tok(TokenType.COMMA));
            raw.addAll(slots_MANY("fr", "rf", "0","1","2","3","4","5","6","7","8","9"));
            Expr.UserType ut = new Expr.UserType(id("Num"), new ArrayList<>(), null, raw);
            Interpreter i = interp(ut);
            ok(i.userTypeRegistry.size() == 1, "registered");
            ok(i.userTypeRegistry.get(0).slots.size() == 3, "three slots");
        }

        System.out.println("\nTotal: " + passed + " pass, " + failed + " fail");
    }
}
