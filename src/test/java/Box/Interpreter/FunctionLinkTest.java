package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration.FunDecl;
import Parser.Fun.FunctionLink;

/**
 * Tests for standalone FunctionLink declarations (top-level fun.draw.[].ward.nuf).
 *
 * The main bug fixed: visitFunDeclDeclaration was casting Fun to Function unconditionally;
 * a FunDecl(FunctionLink) would ClassCastException before reaching visitFunctionLinkFun.
 */
public class FunctionLinkTest {
    static int passed = 0, failed = 0;

    static void ok(boolean cond, String name) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else      { System.out.println("  FAIL  " + name); failed++; }
    }
    static void eq(Object expected, Object actual, String name) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (ok) { System.out.println("  PASS  " + name); passed++; }
        else    { System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual); failed++; }
    }

    static Token id(String lex) {
        return new Token(TokenType.IDENTIFIER, lex, null, null, null, 0, 0, 0, 0);
    }
    static Token typeTok() {
        return new Token(TokenType.DOUBLE, "double", null, null, null, 0, 0, 0, 0);
    }

    static Interpreter freshInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    static BoxFunction lookup(Interpreter i, String name) {
        try {
            Object v = i.globals.get(id(name), false);
            if (v instanceof BoxFunction) return (BoxFunction) v;
        } catch (Exception e) {}
        try {
            Object v = i.environment.get(id(name), false);
            if (v instanceof BoxFunction) return (BoxFunction) v;
        } catch (Exception e) {}
        return null;
    }

    /** Build a FunctionLink with one param each direction. */
    static FunctionLink fl(String fwd, String bwd) {
        ArrayList<Token> pt = new ArrayList<>(); pt.add(typeTok());
        ArrayList<Token> pn = new ArrayList<>(); pn.add(id("x"));
        return new FunctionLink(id(fwd), pt, pn, pt, pn, id(bwd));
    }

    /** Build a FunctionLink with no params. */
    static FunctionLink flNoParams(String fwd, String bwd) {
        return new FunctionLink(id(fwd), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), id(bwd));
    }

    /** Build a forward-only FunctionLink. */
    static FunctionLink flFwdOnly(String fwd) {
        ArrayList<Token> pt = new ArrayList<>(); pt.add(typeTok());
        ArrayList<Token> pn = new ArrayList<>(); pn.add(id("x"));
        return new FunctionLink(id(fwd), pt, pn, null, null, null);
    }

    /** Build a backward-only FunctionLink. */
    static FunctionLink flBwdOnly(String bwd) {
        ArrayList<Token> pt = new ArrayList<>(); pt.add(typeTok());
        ArrayList<Token> pn = new ArrayList<>(); pn.add(id("x"));
        return new FunctionLink(null, null, null, pt, pn, id(bwd));
    }

    public static void main(String[] args) {

        System.out.println("--- FunDecl(FunctionLink) dispatches without ClassCastException ---");
        {
            Interpreter i = freshInterp();
            FunctionLink link = flNoParams("draw", "ward");
            FunDecl decl = new FunDecl(link);
            boolean threw = false;
            try { i.visitFunDeclDeclaration(decl); }
            catch (ClassCastException e) { threw = true; System.out.println("  FAIL  ClassCastException: " + e); }
            catch (Exception e) { /* other exceptions are OK for this dispatch test */ }
            ok(!threw, "no ClassCastException on FunDecl(FunctionLink)");
        }

        System.out.println("--- forward stub defined in environment ---");
        {
            Interpreter i = freshInterp();
            i.visitFunctionLinkFun(flNoParams("draw", "ward"));
            BoxFunction draw = lookup(i, "draw");
            ok(draw != null, "draw defined");
            ok(draw != null && draw.isLinkSignature(), "draw isLinkSignature");
            ok(draw != null && draw.isForward, "draw isForward=true");
            eq("draw", draw != null ? draw.getName() : null, "draw name");
        }

        System.out.println("--- backward stub defined in environment ---");
        {
            Interpreter i = freshInterp();
            i.visitFunctionLinkFun(flNoParams("draw", "ward"));
            BoxFunction ward = lookup(i, "ward");
            ok(ward != null, "ward defined");
            ok(ward != null && ward.isLinkSignature(), "ward isLinkSignature");
            ok(ward != null && !ward.isForward, "ward isForward=false");
            eq("ward", ward != null ? ward.getName() : null, "ward name");
        }

        System.out.println("--- calling forward stub throws RuntimeError ---");
        {
            Interpreter i = freshInterp();
            i.visitFunctionLinkFun(flNoParams("draw", "ward"));
            BoxFunction draw = lookup(i, "draw");
            boolean threw = false;
            try { draw.call(i, new ArrayList<>()); }
            catch (RuntimeError e) {
                threw = true;
                ok(e.getMessage().contains("draw"), "error mentions function name");
            }
            ok(threw, "calling draw throws RuntimeError");
        }

        System.out.println("--- calling backward stub throws RuntimeError ---");
        {
            Interpreter i = freshInterp();
            i.visitFunctionLinkFun(flNoParams("draw", "ward"));
            BoxFunction ward = lookup(i, "ward");
            boolean threw = false;
            try { ward.call(i, new ArrayList<>()); }
            catch (RuntimeError e) { threw = true; }
            ok(threw, "calling ward throws RuntimeError");
        }

        System.out.println("--- arity matches parameter list ---");
        {
            Interpreter i = freshInterp();
            i.visitFunctionLinkFun(fl("draw", "ward")); // one param each
            BoxFunction draw = lookup(i, "draw");
            BoxFunction ward = lookup(i, "ward");
            eq(1, draw != null ? draw.arity() : -1, "draw arity = 1");
            eq(1, ward != null ? ward.arity() : -1, "ward arity = 1");
        }

        System.out.println("--- zero-param variant ---");
        {
            Interpreter i = freshInterp();
            i.visitFunctionLinkFun(flNoParams("run", "nur"));
            BoxFunction run = lookup(i, "run");
            BoxFunction nur = lookup(i, "nur");
            eq(0, run != null ? run.arity() : -1, "run arity = 0");
            eq(0, nur != null ? nur.arity() : -1, "nur arity = 0");
        }

        System.out.println("--- forward-only FunctionLink ---");
        {
            Interpreter i = freshInterp();
            i.visitFunctionLinkFun(flFwdOnly("shoot"));
            BoxFunction shoot = lookup(i, "shoot");
            ok(shoot != null, "shoot defined");
            ok(shoot != null && shoot.isForward, "shoot isForward");
            ok(lookup(i, "toahs") == null, "backward twin not defined");
        }

        System.out.println("--- backward-only FunctionLink ---");
        {
            Interpreter i = freshInterp();
            i.visitFunctionLinkFun(flBwdOnly("toahs"));
            BoxFunction toahs = lookup(i, "toahs");
            ok(toahs != null, "toahs defined");
            ok(toahs != null && !toahs.isForward, "toahs !isForward");
            ok(lookup(i, "shoot") == null, "forward twin not defined");
        }

        System.out.println("--- second visitFunctionLinkFun overwrites first ---");
        {
            Interpreter i = freshInterp();
            i.visitFunctionLinkFun(flNoParams("draw", "ward"));
            i.visitFunctionLinkFun(flNoParams("draw", "ward")); // redeclare
            BoxFunction draw = lookup(i, "draw");
            ok(draw != null && draw.isLinkSignature(), "draw still a link sig after redeclare");
        }

        System.out.println("--- via FunDecl dispatch: stubs end up in environment ---");
        {
            Interpreter i = freshInterp();
            FunDecl decl = new FunDecl(flNoParams("paint", "tniap"));
            i.visitFunDeclDeclaration(decl);
            BoxFunction paint = lookup(i, "paint");
            BoxFunction tniap = lookup(i, "tniap");
            ok(paint != null, "paint defined via FunDecl");
            ok(tniap != null, "tniap defined via FunDecl");
            ok(paint != null && paint.isLinkSignature(), "paint isLinkSignature via FunDecl");
            ok(tniap != null && tniap.isLinkSignature(), "tniap isLinkSignature via FunDecl");
        }

        System.out.println("\n==================");
        System.out.println("FunctionLinkTest: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
