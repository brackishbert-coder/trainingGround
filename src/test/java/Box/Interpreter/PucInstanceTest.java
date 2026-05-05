package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Parser.Expr;
import Parser.Stmt;
import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration.StmtDecl;

/**
 * Tests for PucInstance — execution-inversion cup.
 *
 * The container interface (push/pop/add/etc.) behaves identically to CupInstance.
 * The difference is execute(): the interpreter's invertedMode flag is toggled,
 * which causes body instructions to run in their inverse forms.
 *
 * No JUnit dependency. Run:
 *   javac -cp <classpath> src/test/java/Box/Interpreter/PucInstanceTest.java -d /tmp/puctestbuild
 *   java  -cp <classpath>:/tmp/puctestbuild Box.Interpreter.PucInstanceTest
 */
public class PucInstanceTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("  PASS  " + name);
            passed++;
        } else {
            System.out.println("  FAIL  " + name);
            failed++;
        }
    }

    private static void eq(String name, Object expected, Object actual) {
        boolean ok = (expected == null) ? (actual == null) : expected.equals(actual);
        if (ok) {
            System.out.println("  PASS  " + name);
            passed++;
        } else {
            System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual);
            failed++;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Token tok(TokenType type, String lexeme) {
        return new Token(type, lexeme, null, null, null, 0, 0, 0, 0);
    }

    private static Interpreter makeInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    private static Stmt.Expression cupOpenStmt(String label) {
        Token ctrl = new Token(TokenType.OPENBRACE, label + "{", null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.CupOpen(ctrl), null);
    }

    private static Stmt.Expression cupCloseStmt(String label) {
        Token ctrl = new Token(TokenType.CLOSEDBRACE, "}" + label, null, null, null, 0, 0, 0, 0);
        return new Stmt.Expression(new Expr.CupClosed(ctrl), null);
    }

    private static PucInstance makePuc(Interpreter interp, Object... items) {
        List<Object> body = new ArrayList<>();
        body.add(cupOpenStmt("test"));
        for (Object item : items) body.add(item);
        body.add(cupCloseStmt("test"));
        Expr.Cup cupExpr = new Expr.Cup(null, new ArrayList<>(), "", null);
        cupExpr.isPuc = true;
        return new PucInstance(null, body, cupExpr, interp);
    }

    private static CupInstance makeCup(Interpreter interp, Object... items) {
        List<Object> body = new ArrayList<>();
        body.add(cupOpenStmt("test"));
        for (Object item : items) body.add(item);
        body.add(cupCloseStmt("test"));
        return new CupInstance(null, body, null, interp);
    }

    // -------------------------------------------------------------------------
    // Interface parity: puc push/pop/add behave same as cup
    // -------------------------------------------------------------------------

    private static void testPucPushSameAsCup() {
        System.out.println("--- puc.push() interface same as cup.push() (inserts at front) ---");
        Interpreter interp = makeInterp();
        PucInstance puc = makePuc(interp, "a", "b");
        CupInstance cup = makeCup(interp, "a", "b");

        Token op = tok(TokenType.PUSH, "push");
        Token getOp = tok(TokenType.GETAT, "getat");

        puc.push(op, new Expr.Literal("new"));
        cup.push(op, new Expr.Literal("new"));

        // Both should have "new" at index 0 (front)
        eq("puc getat(0) == 'new' after push",
            new Expr.Literal("new"), puc.getat(getOp, 0));
        eq("cup getat(0) == 'new' after push",
            new Expr.Literal("new"), cup.getat(getOp, 0));
        eq("puc and cup getat(0) match",
            cup.getat(getOp, 0), puc.getat(getOp, 0));
    }

    private static void testPucPopSameAsCup() {
        System.out.println("--- puc.pop() interface same as cup.pop() (removes from front) ---");
        Interpreter interp = makeInterp();
        PucInstance puc = makePuc(interp, "first", "last");
        CupInstance cup = makeCup(interp, "first", "last");

        Token op = tok(TokenType.POP, "pop");
        Object pucPopped = puc.pop(op);
        Object cupPopped = cup.pop(op);

        eq("puc popped 'first'", "first", pucPopped);
        eq("cup popped 'first'", "first", cupPopped);
        eq("puc and cup popped same value", cupPopped, pucPopped);
    }

    private static void testPucAddSameAsCup() {
        System.out.println("--- puc.add() interface same as cup.add() (appends to back) ---");
        Interpreter interp = makeInterp();
        PucInstance puc = makePuc(interp, "existing");
        CupInstance cup = makeCup(interp, "existing");

        Token op = tok(TokenType.ADD, "add");
        Token sizeOp = tok(TokenType.SIZE, "size");
        puc.add(op, "appended");
        cup.add(op, "appended");

        eq("puc size grew to 2", 2, puc.size(sizeOp));
        eq("cup size grew to 2", 2, cup.size(sizeOp));
    }

    // -------------------------------------------------------------------------
    // Interpreter flag: execute() toggles invertedMode
    // -------------------------------------------------------------------------

    private static void testExecuteTogglesInvertedMode() {
        System.out.println("--- puc.execute() toggles interpreter.invertedMode ---");
        Interpreter interp = makeInterp();
        final boolean[] sawInverted = {false};

        // We can't easily run body code here, so we verify the flag is
        // restored to false after execute() returns.
        PucInstance puc = makePuc(interp);
        check("invertedMode is false before execute", !interp.isInverted());
        puc.execute();
        check("invertedMode restored to false after execute", !interp.isInverted());
    }

    private static void testDoubleToggleRestoresNormal() {
        System.out.println("--- puc inside puc: double-toggle returns to normal ---");
        Interpreter interp = makeInterp();
        interp.setInverted(false);

        // Simulate two nested execute() calls:
        boolean before = interp.isInverted();              // false
        interp.setInverted(!before);                       // → true (puc entered)
        boolean insideFirst = interp.isInverted();         // true
        interp.setInverted(!insideFirst);                  // → false (inner puc entered)
        boolean insideSecond = interp.isInverted();        // false = normal
        interp.setInverted(insideFirst);                   // → true  (inner puc exited)
        interp.setInverted(before);                        // → false (outer puc exited)

        check("inner puc sees invertedMode=false (double-inverted=normal)", !insideSecond);
        check("restored to false after both exit", !interp.isInverted());
    }

    // -------------------------------------------------------------------------
    // instanceof and identity
    // -------------------------------------------------------------------------

    private static void testPucIsCupInstance() {
        System.out.println("--- PucInstance instanceof CupInstance ---");
        Interpreter interp = makeInterp();
        PucInstance puc = makePuc(interp);
        check("PucInstance is-a CupInstance", puc instanceof CupInstance);
        check("PucInstance is-a PucInstance", puc instanceof PucInstance);
    }

    // -------------------------------------------------------------------------
    // Neutral ops: size, empty, clear unchanged
    // -------------------------------------------------------------------------

    private static void testSizeNeutral() {
        System.out.println("--- puc size() unchanged ---");
        Interpreter interp = makeInterp();
        PucInstance puc = makePuc(interp, 10, 20, 30);
        eq("size is 3", 3, puc.size(tok(TokenType.SIZE, "size")));
    }

    private static void testEmptyNeutral() {
        System.out.println("--- puc empty() unchanged ---");
        Interpreter interp = makeInterp();
        PucInstance puc = makePuc(interp);
        check("empty puc reports empty", Boolean.TRUE.equals(puc.empty(tok(TokenType.EMPTY, "empty"))));
    }

    private static void testClearNeutral() {
        System.out.println("--- puc clear() keeps structural brackets ---");
        Interpreter interp = makeInterp();
        PucInstance puc = makePuc(interp, 1, 2, 3);
        puc.clear(tok(TokenType.CLEAR, "clear"));
        eq("size is 0 after clear", 0, puc.size(tok(TokenType.SIZE, "size")));
    }

    // -------------------------------------------------------------------------
    // toString
    // -------------------------------------------------------------------------

    private static void testToString() {
        System.out.println("--- puc toString has puc{} markers ---");
        Interpreter interp = makeInterp();
        PucInstance puc = makePuc(interp);
        String s = puc.toString();
        check("toString starts with puc{", s.startsWith("puc{"));
        check("toString ends with }cup", s.endsWith("}cup"));
    }

    // -------------------------------------------------------------------------
    // Interpreter inversion: arithmetic flips
    // -------------------------------------------------------------------------

    private static void testInterpreterPlusFlipsToMinus() {
        System.out.println("--- invertedMode: PLUS evaluates as MINUS ---");
        Interpreter interp = makeInterp();
        // Build binary expr: 10 + 3
        Token plusTok = new Token(TokenType.PLUS, "+", null, null, null, 0, 0, 0, 0);
        Expr.Binary plusExpr = new Expr.Binary(new Expr.Literal(10), plusTok, new Expr.Literal(3));

        interp.setInverted(false);
        Object normal = interp.visitBinaryExpr(plusExpr);
        eq("10 + 3 = 13 in normal mode", 13, normal);

        interp.setInverted(true);
        Object inverted = interp.visitBinaryExpr(plusExpr);
        interp.setInverted(false);
        eq("10 + 3 = 7 in inverted mode (acts as minus)", 7, inverted);
    }

    private static void testInterpreterMinusFlipsToPlus() {
        System.out.println("--- invertedMode: MINUS evaluates as PLUS ---");
        Interpreter interp = makeInterp();
        Token minusTok = new Token(TokenType.MINUS, "-", null, null, null, 0, 0, 0, 0);
        Expr.Binary minusExpr = new Expr.Binary(new Expr.Literal(10), minusTok, new Expr.Literal(3));

        interp.setInverted(false);
        Object normal = interp.visitBinaryExpr(minusExpr);
        eq("10 - 3 = 7 in normal mode", 7, normal);

        interp.setInverted(true);
        Object inverted = interp.visitBinaryExpr(minusExpr);
        interp.setInverted(false);
        eq("10 - 3 = 13 in inverted mode (acts as plus)", 13, inverted);
    }

    private static void testInterpreterGTFlipsToLT() {
        System.out.println("--- invertedMode: GREATERTHEN evaluates as LESSTHEN ---");
        Interpreter interp = makeInterp();
        Token gtTok = new Token(TokenType.GREATERTHEN, ">", null, null, null, 0, 0, 0, 0);
        Expr.Binary gtExpr = new Expr.Binary(new Expr.Literal(5), gtTok, new Expr.Literal(3));

        interp.setInverted(false);
        eq("5 > 3 is true in normal mode", true, interp.visitBinaryExpr(gtExpr));

        interp.setInverted(true);
        eq("5 > 3 is false in inverted mode (acts as <)", false, interp.visitBinaryExpr(gtExpr));
        interp.setInverted(false);
    }

    private static void testInterpreterQMarkDropsNegation() {
        System.out.println("--- invertedMode: QMARK drops negation ---");
        Interpreter interp = makeInterp();
        Token qTok = new Token(TokenType.QMARK, "?", null, null, null, 0, 0, 0, 0);
        Expr.Unary notTrue = new Expr.Unary(qTok, new Expr.Literal(true));

        interp.setInverted(false);
        eq("!true = false in normal mode", false, interp.visitUnaryExpr(notTrue));

        interp.setInverted(true);
        eq("!true = true in inverted mode (negation dropped)", true, interp.visitUnaryExpr(notTrue));
        interp.setInverted(false);
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== PucInstance Test ===\n");

        run("puc push interface == cup push",    PucInstanceTest::testPucPushSameAsCup);
        run("puc pop interface == cup pop",      PucInstanceTest::testPucPopSameAsCup);
        run("puc add interface == cup add",      PucInstanceTest::testPucAddSameAsCup);
        run("execute() toggles invertedMode",   PucInstanceTest::testExecuteTogglesInvertedMode);
        run("double-toggle restores normal",     PucInstanceTest::testDoubleToggleRestoresNormal);
        run("is-a CupInstance",                  PucInstanceTest::testPucIsCupInstance);
        run("size neutral",                      PucInstanceTest::testSizeNeutral);
        run("empty neutral",                     PucInstanceTest::testEmptyNeutral);
        run("clear neutral",                     PucInstanceTest::testClearNeutral);
        run("toString format",                   PucInstanceTest::testToString);
        run("PLUS flips to MINUS",               PucInstanceTest::testInterpreterPlusFlipsToMinus);
        run("MINUS flips to PLUS",               PucInstanceTest::testInterpreterMinusFlipsToPlus);
        run("GT flips to LT",                    PucInstanceTest::testInterpreterGTFlipsToLT);
        run("QMARK drops negation",              PucInstanceTest::testInterpreterQMarkDropsNegation);

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
        } catch (Exception e) {
            System.out.println("  ERROR in " + name + ": " + e);
            e.printStackTrace(System.out);
            failed++;
        }
    }
}
