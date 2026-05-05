package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Parser.Expr;
import Box.Token.Token;
import Box.Token.TokenType;

/**
 * Tests for XobInstance — opaque container semantics.
 *
 * XobInstance allows: push, pop, add, size, empty, clear.
 * XobInstance blocks: getat, setat, sub, contains, remove.
 *
 * No JUnit dependency. Run:
 *   javac -cp <classpath> src/test/java/Box/Interpreter/XobInstanceTest.java -d /tmp/xobtestbuild
 *   java  -cp <classpath>:/tmp/xobtestbuild Box.Interpreter.XobInstanceTest
 */
public class XobInstanceTest {

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

    /** Build an XobInstance with the given initial body items (already-evaluated Objects). */
    private static XobInstance makeXob(Interpreter interp, Object... items) {
        // XobInstance extends BoxInstance; body is pre-evaluated raw objects.
        // We bypass evaluateBody by passing Expr.Literal wrappers that evaluate to the items.
        List<Object> body = new ArrayList<>();
        for (Object item : items) {
            body.add(item);
        }
        // Build a minimal Expr.Box with isXob=true
        Expr.Box boxExpr = new Expr.Box(null, new ArrayList<>(), "", null);
        boxExpr.isXob = true;
        // Build BoxClass and XobInstance directly (bypasses evaluateBody since body is raw)
        // Actually construct XobInstance directly with pre-built body
        return new XobInstance(null, body, boxExpr, interp) {
            // evaluateBody() would overwrite body; override to keep our raw body
            // We can't override private evaluateBody(), so we pass already-evaluated items
            // via the constructor. Since evaluateBody() iterates body and calls evaluate(Expr)
            // on Expr instances, passing non-Expr objects means they pass through unchanged.
        };
    }

    // -------------------------------------------------------------------------
    // Permitted operations
    // -------------------------------------------------------------------------

    private static void testSizePermitted() {
        System.out.println("--- size() is permitted on xob ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp, 1, 2, 3);
        Token op = tok(TokenType.SIZE, "size");
        Object sz = xob.size(op);
        eq("size returns 3", 3, sz);
    }

    private static void testEmptyPermitted() {
        System.out.println("--- empty() is permitted on xob ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp);
        Token op = tok(TokenType.EMPTY, "empty");
        check("empty xob returns true", Boolean.TRUE.equals(xob.empty(op)));
    }

    private static void testAddPermitted() {
        System.out.println("--- add() is permitted on xob ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp, 1);
        Token op = tok(TokenType.ADD, "add");
        // add() only accepts Expr subclasses; use a Literal
        Expr.Literal lit = new Expr.Literal(2);
        xob.add(op, lit);
        Token sizeOp = tok(TokenType.SIZE, "size");
        eq("size grew after add", 2, xob.size(sizeOp));
    }

    private static void testClearPermitted() {
        System.out.println("--- clear() is permitted on xob ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp, 10, 20);
        Token op = tok(TokenType.CLEAR, "clear");
        xob.clear(op);
        Token sizeOp = tok(TokenType.SIZE, "size");
        eq("size is 0 after clear", 0, xob.size(sizeOp));
    }

    private static void testPopPermitted() {
        System.out.println("--- pop() is permitted on xob ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp, 42);
        Token op = tok(TokenType.POP, "pop");
        Object popped = xob.pop(op);
        eq("popped value is 42", 42, popped);
        Token sizeOp = tok(TokenType.SIZE, "size");
        eq("xob empty after pop", 0, xob.size(sizeOp));
    }

    // -------------------------------------------------------------------------
    // Blocked operations
    // -------------------------------------------------------------------------

    private static void testGetatBlocked() {
        System.out.println("--- getat() throws on xob ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp, 1, 2, 3);
        Token op = tok(TokenType.GETAT, "getat");
        try {
            xob.getat(op, 0);
            System.out.println("  FAIL  getat() did not throw");
            failed++;
        } catch (RuntimeError e) {
            check("getat() threw RuntimeError", e.getMessage().contains("xob"));
        }
    }

    private static void testSetatBlocked() {
        System.out.println("--- setat() throws on xob ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp, 1, 2, 3);
        Expr.Literal index = new Expr.Literal(0);
        Expr.Literal value = new Expr.Literal(99);
        try {
            xob.setat(index, value);
            System.out.println("  FAIL  setat() did not throw");
            failed++;
        } catch (RuntimeError e) {
            check("setat() threw RuntimeError", e.getMessage().contains("xob"));
        }
    }

    private static void testSubBlocked() {
        System.out.println("--- sub() throws on xob ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp, 1, 2, 3);
        Expr.Literal start = new Expr.Literal(0);
        Expr.Literal end = new Expr.Literal(2);
        try {
            xob.sub(start, end);
            System.out.println("  FAIL  sub() did not throw");
            failed++;
        } catch (RuntimeError e) {
            check("sub() threw RuntimeError", e.getMessage().contains("xob"));
        }
    }

    private static void testContainsBlocked() {
        System.out.println("--- contains() throws on xob ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp, 1, 2, 3);
        Parser.Stmt.Expression stmt = new Parser.Stmt.Expression(new Expr.Literal(1), null);
        try {
            xob.contains(stmt);
            System.out.println("  FAIL  contains() did not throw");
            failed++;
        } catch (RuntimeError e) {
            check("contains() threw RuntimeError", e.getMessage().contains("xob"));
        }
    }

    private static void testRemoveBlocked() {
        System.out.println("--- remove() throws on xob ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp, 1, 2, 3);
        Token op = tok(TokenType.REMOVE, "remove");
        try {
            xob.remove(op, 0);
            System.out.println("  FAIL  remove() did not throw");
            failed++;
        } catch (RuntimeError e) {
            check("remove() threw RuntimeError", e.getMessage().contains("xob"));
        }
    }

    // -------------------------------------------------------------------------
    // isXob flag and instanceof
    // -------------------------------------------------------------------------

    private static void testXobIsBoxInstance() {
        System.out.println("--- XobInstance instanceof BoxInstance ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp);
        check("XobInstance is-a BoxInstance", xob instanceof BoxInstance);
        check("XobInstance is-a XobInstance", xob instanceof XobInstance);
    }

    private static void testToString() {
        System.out.println("--- xob toString has xob[] markers ---");
        Interpreter interp = makeInterp();
        XobInstance xob = makeXob(interp);
        String s = xob.toString();
        check("toString starts with xob[", s.startsWith("xob["));
        check("toString ends with ]box", s.endsWith("]box"));
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== XobInstance Test ===\n");

        run("size() permitted",       XobInstanceTest::testSizePermitted);
        run("empty() permitted",      XobInstanceTest::testEmptyPermitted);
        run("add() permitted",        XobInstanceTest::testAddPermitted);
        run("clear() permitted",      XobInstanceTest::testClearPermitted);
        run("pop() permitted",        XobInstanceTest::testPopPermitted);
        run("getat() blocked",        XobInstanceTest::testGetatBlocked);
        run("setat() blocked",        XobInstanceTest::testSetatBlocked);
        run("sub() blocked",          XobInstanceTest::testSubBlocked);
        run("contains() blocked",     XobInstanceTest::testContainsBlocked);
        run("remove() blocked",       XobInstanceTest::testRemoveBlocked);
        run("is-a BoxInstance",       XobInstanceTest::testXobIsBoxInstance);
        run("toString format",        XobInstanceTest::testToString);

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
