package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;
import Parser.Stmt;

/**
 * Tests for KnotInstance and TonkInstance container operations.
 *
 * Both types share the same data-container semantics: a body with structural
 * brackets at each end and content items in between. KnotInstance uses
 * CupOpen/PocketClose; TonkInstance is symmetric.
 *
 * Body format (for data operations):
 *   [ CupOpen_stmt, item0, item1, ..., PocketClose_stmt ]
 *
 * Key behaviors:
 *   - bodySizeExclude() counts only non-structural items
 *   - getat(i) is 1-indexed internally; valid range is 0..size-2 (last item protected)
 *   - pop() removes first non-structural item; returns null when ≤1 item remains
 *   - add() inserts before the last element then calls evaluateBody(), which:
 *       * keeps structural brackets as-is (isControl check)
 *       * evaluates Stmt.Expression items, replacing them with their result
 *       * leaves raw non-Stmt values unchanged
 *   - clear() removes all non-structural body items
 */
public class KnotTonkInstanceTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean cond) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else      { System.out.println("  FAIL  " + name); failed++; }
    }

    private static void eq(String name, Object expected, Object actual) {
        boolean ok = (expected == null) ? (actual == null) : expected.equals(actual);
        if (ok) { System.out.println("  PASS  " + name); passed++; }
        else    { System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual); failed++; }
    }

    private static Token tok(TokenType type, String lex) {
        return new Token(type, lex, null, null, null, 0, 0, 0, 0);
    }

    private static Stmt.Expression cupOpenStmt() {
        return new Stmt.Expression(new Expr.CupOpen(tok(TokenType.OPENBRACE, "{")), null);
    }

    private static Stmt.Expression pocketCloseStmt() {
        return new Stmt.Expression(new Expr.PocketClosed(tok(TokenType.CLOSEDPAREN, ")")), null);
    }

    private static Stmt.Expression literalStmt(Object value) {
        return new Stmt.Expression(new Expr.Literal(value), null);
    }

    private static Interpreter makeInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    /**
     * Build a KnotInstance body with structural brackets around pre-evaluated raw values.
     * Using raw values (not Stmt.Expression) avoids evaluateBody() complications in the
     * constructor (which is commented out anyway). Content items are placed directly.
     */
    private static KnotInstance knotWith(Interpreter interp, Object... items) {
        List<Object> body = new ArrayList<>();
        body.add(cupOpenStmt());
        for (Object item : items) body.add(item);
        body.add(pocketCloseStmt());
        return new KnotInstance(null, body, null, interp);
    }

    private static TonkInstance tonkWith(Interpreter interp, Object... items) {
        List<Object> body = new ArrayList<>();
        body.add(cupOpenStmt());
        for (Object item : items) body.add(item);
        body.add(pocketCloseStmt());
        return new TonkInstance(null, body, null, interp);
    }

    // ---- KnotInstance — size / empty ----------------------------------------

    private static void testKnotEmptyOnStructuralOnly() {
        System.out.println("--- knot empty() = true with only structural brackets ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i);
        check("empty() = true",  (Boolean) k.empty(tok(TokenType.IDENTIFIER, "op")));
        eq("size() = 0", 0, k.size(tok(TokenType.IDENTIFIER, "op")));
    }

    private static void testKnotSizeCountsRawValues() {
        System.out.println("--- knot size() counts raw (non-structural) values ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i, 1.0, 2.0, 3.0);
        eq("size 3", 3, k.size(tok(TokenType.IDENTIFIER, "op")));
        check("not empty", !(Boolean) k.empty(tok(TokenType.IDENTIFIER, "op")));
    }

    private static void testKnotSizeOneItem() {
        System.out.println("--- knot size() = 1 with single item ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i, "solo");
        eq("size 1", 1, k.size(tok(TokenType.IDENTIFIER, "op")));
    }

    // ---- KnotInstance — getat -----------------------------------------------

    private static void testKnotGetatIndex0() {
        System.out.println("--- knot getat(0) returns first item when ≥3 items ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i, "a", "b", "c");
        eq("getat 0 = a", "a", k.getat(tok(TokenType.IDENTIFIER, "op"), 0));
    }

    private static void testKnotGetatIndex1() {
        System.out.println("--- knot getat(1) returns second item when ≥3 items ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i, "a", "b", "c");
        eq("getat 1 = b", "b", k.getat(tok(TokenType.IDENTIFIER, "op"), 1));
    }

    private static void testKnotGetatLastProtected() {
        System.out.println("--- knot getat(size-1) is out of bounds (last item protected) ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i, "a", "b", "c");
        boolean threw = false;
        try {
            k.getat(tok(TokenType.IDENTIFIER, "op"), 2); // index = size-1 = 2 → out of bounds
        } catch (RuntimeError e) {
            threw = true;
        }
        check("getat last throws RuntimeError", threw);
    }

    // ---- KnotInstance — pop -------------------------------------------------

    private static void testKnotPopRemovesFirst() {
        System.out.println("--- knot pop() removes and returns first item (≥2 items) ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i, "x", "y", "z");
        Token op = tok(TokenType.IDENTIFIER, "op");
        Object popped = k.pop(op);
        eq("popped = x", "x", popped);
        eq("size after pop = 2", 2, k.size(op));
    }

    private static void testKnotPopReturnsNullWhenOneItem() {
        System.out.println("--- knot pop() returns null when only 1 item remains (protected last) ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i, "only");
        Token op = tok(TokenType.IDENTIFIER, "op");
        Object result = k.pop(op);
        eq("pop on 1 item = null", null, result);
        eq("size unchanged = 1", 1, k.size(op));
    }

    private static void testKnotPopReturnsNullWhenEmpty() {
        System.out.println("--- knot pop() returns null on empty body ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i);
        Token op = tok(TokenType.IDENTIFIER, "op");
        // pop() starts at index 1; for [CupOpen, PocketClose], index 1 = PocketClose
        // isIndexControl(1) = true → index becomes 2, which is bodySizeExclude()+2 → returns null
        Object result = k.pop(op);
        eq("pop on empty = null", null, result);
    }

    // ---- KnotInstance — add -------------------------------------------------

    private static void testKnotAddInsertsItem() {
        System.out.println("--- knot add() inserts Stmt and evaluateBody() processes it ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i);
        Token op = tok(TokenType.IDENTIFIER, "op");
        // add() inserts before last element, then calls evaluateBody()
        // evaluateBody() evaluates Stmt.Expression(Literal(99.0)) → 99.0 stored
        k.add(op, literalStmt(99.0));
        eq("size after add = 1", 1, k.size(op));
    }

    private static void testKnotAddTwoItemsGetat() {
        System.out.println("--- knot add twice: getat(0) accessible with ≥3 items ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i);
        Token op = tok(TokenType.IDENTIFIER, "op");
        k.add(op, literalStmt(11.0));
        k.add(op, literalStmt(22.0));
        k.add(op, literalStmt(33.0));
        // After 3 adds + evaluateBody, body = [CupOpen, 11.0, 22.0, 33.0, PocketClose]
        eq("size = 3", 3, k.size(op));
        eq("getat 0 = 11.0", 11.0, Boxer.unbox(k.getat(op, 0)));
        eq("getat 1 = 22.0", 22.0, Boxer.unbox(k.getat(op, 1)));
    }

    private static void testKnotAddRejectsNonStmt() {
        System.out.println("--- knot add() throws RuntimeError for non-Stmt/Expr ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i);
        Token op = tok(TokenType.IDENTIFIER, "op");
        boolean threw = false;
        try {
            k.add(op, "raw string"); // String is not Stmt or Expr
        } catch (RuntimeError e) {
            threw = true;
        }
        check("add raw string throws", threw);
    }

    // ---- KnotInstance — contains --------------------------------------------

    private static void testKnotContainsFindsValue() {
        System.out.println("--- knot contains() finds existing raw value ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i, "needle", "other");
        Stmt.Expression query = literalStmt("needle");
        check("contains needle", k.contains(query));
    }

    private static void testKnotContainsMissing() {
        System.out.println("--- knot contains() returns false for missing value ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i, "a", "b");
        check("not contains c", !k.contains(literalStmt("c")));
    }

    // ---- KnotInstance — clear -----------------------------------------------

    private static void testKnotClearRemovesContent() {
        System.out.println("--- knot clear() removes all content items ---");
        Interpreter i = makeInterp();
        KnotInstance k = knotWith(i, 1.0, 2.0, 3.0);
        Token op = tok(TokenType.IDENTIFIER, "op");
        k.clear(op);
        // After clear(), body contains only the bare Expr objects (not Stmt wrappers).
        // bodySizeExclude() counts objects that are NOT Stmt.Expression → bare Exprs count!
        // But contains() checks via Literal match. The structural bracket Expr objects
        // (CupOpen, PocketClosed) are Expr, not Literal, so they won't match.
        check("after clear: not contains 1.0", !k.contains(literalStmt(1.0)));
        check("after clear: not contains 2.0", !k.contains(literalStmt(2.0)));
    }

    // =========================================================================
    // TonkInstance — same structure, mirror tests
    // =========================================================================

    private static void testTonkEmptyOnStructuralOnly() {
        System.out.println("--- tonk empty() = true with only structural brackets ---");
        Interpreter i = makeInterp();
        TonkInstance t = tonkWith(i);
        check("tonk empty() = true", (Boolean) t.empty(tok(TokenType.IDENTIFIER, "op")));
    }

    private static void testTonkSizeCountsRawValues() {
        System.out.println("--- tonk size() counts raw values ---");
        Interpreter i = makeInterp();
        TonkInstance t = tonkWith(i, 10.0, 20.0);
        eq("tonk size 2", 2, t.size(tok(TokenType.IDENTIFIER, "op")));
    }

    private static void testTonkGetatIndex0() {
        System.out.println("--- tonk getat(0) returns first item (≥3 items) ---");
        Interpreter i = makeInterp();
        TonkInstance t = tonkWith(i, "p", "q", "r");
        eq("tonk getat 0 = p", "p", t.getat(tok(TokenType.IDENTIFIER, "op"), 0));
    }

    private static void testTonkGetatLastProtected() {
        System.out.println("--- tonk getat(size-1) throws (last item protected) ---");
        Interpreter i = makeInterp();
        TonkInstance t = tonkWith(i, "p", "q", "r");
        boolean threw = false;
        try {
            t.getat(tok(TokenType.IDENTIFIER, "op"), 2);
        } catch (RuntimeError e) {
            threw = true;
        }
        check("tonk getat last throws", threw);
    }

    private static void testTonkPopRemovesFirst() {
        System.out.println("--- tonk pop() removes first item (≥2 items) ---");
        Interpreter i = makeInterp();
        TonkInstance t = tonkWith(i, "first", "second", "third");
        Token op = tok(TokenType.IDENTIFIER, "op");
        Object popped = t.pop(op);
        eq("tonk popped = first", "first", popped);
        eq("tonk size after pop = 2", 2, t.size(op));
    }

    private static void testTonkPopNullOnOneItem() {
        System.out.println("--- tonk pop() returns null when 1 item (protected) ---");
        Interpreter i = makeInterp();
        TonkInstance t = tonkWith(i, "only");
        eq("tonk pop 1 item = null", null, t.pop(tok(TokenType.IDENTIFIER, "op")));
    }

    private static void testTonkContainsFindsValue() {
        System.out.println("--- tonk contains() finds existing value ---");
        Interpreter i = makeInterp();
        TonkInstance t = tonkWith(i, "look", "other");
        check("tonk contains look", t.contains(literalStmt("look")));
    }

    private static void testTonkContainsMissing() {
        System.out.println("--- tonk contains() returns false for missing ---");
        Interpreter i = makeInterp();
        TonkInstance t = tonkWith(i, "a");
        check("tonk not contains b", !t.contains(literalStmt("b")));
    }

    private static void testTonkAddInsertsItem() {
        System.out.println("--- tonk add() inserts Stmt ---");
        Interpreter i = makeInterp();
        TonkInstance t = tonkWith(i);
        Token op = tok(TokenType.IDENTIFIER, "op");
        t.add(op, literalStmt(77.0));
        eq("tonk size after add = 1", 1, t.size(op));
    }

    private static void testTonkClearRemovesContent() {
        System.out.println("--- tonk clear() removes content ---");
        Interpreter i = makeInterp();
        TonkInstance t = tonkWith(i, "x", "y");
        t.clear(tok(TokenType.IDENTIFIER, "op"));
        check("after clear: not contains x", !t.contains(literalStmt("x")));
    }

    // ---- Entry point --------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== KnotTonkInstanceTest ===\n");

        run("knot empty on brackets",        KnotTonkInstanceTest::testKnotEmptyOnStructuralOnly);
        run("knot size counts raw",          KnotTonkInstanceTest::testKnotSizeCountsRawValues);
        run("knot size one item",            KnotTonkInstanceTest::testKnotSizeOneItem);
        run("knot getat 0",                  KnotTonkInstanceTest::testKnotGetatIndex0);
        run("knot getat 1",                  KnotTonkInstanceTest::testKnotGetatIndex1);
        run("knot getat last protected",     KnotTonkInstanceTest::testKnotGetatLastProtected);
        run("knot pop removes first",        KnotTonkInstanceTest::testKnotPopRemovesFirst);
        run("knot pop null on 1 item",       KnotTonkInstanceTest::testKnotPopReturnsNullWhenOneItem);
        run("knot pop null on empty",        KnotTonkInstanceTest::testKnotPopReturnsNullWhenEmpty);
        run("knot add inserts item",         KnotTonkInstanceTest::testKnotAddInsertsItem);
        run("knot add two + getat",          KnotTonkInstanceTest::testKnotAddTwoItemsGetat);
        run("knot add rejects non-stmt",     KnotTonkInstanceTest::testKnotAddRejectsNonStmt);
        run("knot contains finds",           KnotTonkInstanceTest::testKnotContainsFindsValue);
        run("knot contains missing",         KnotTonkInstanceTest::testKnotContainsMissing);
        run("knot clear removes content",    KnotTonkInstanceTest::testKnotClearRemovesContent);
        run("tonk empty on brackets",        KnotTonkInstanceTest::testTonkEmptyOnStructuralOnly);
        run("tonk size counts raw",          KnotTonkInstanceTest::testTonkSizeCountsRawValues);
        run("tonk getat 0",                  KnotTonkInstanceTest::testTonkGetatIndex0);
        run("tonk getat last protected",     KnotTonkInstanceTest::testTonkGetatLastProtected);
        run("tonk pop removes first",        KnotTonkInstanceTest::testTonkPopRemovesFirst);
        run("tonk pop null on 1 item",       KnotTonkInstanceTest::testTonkPopNullOnOneItem);
        run("tonk contains finds",           KnotTonkInstanceTest::testTonkContainsFindsValue);
        run("tonk contains missing",         KnotTonkInstanceTest::testTonkContainsMissing);
        run("tonk add inserts item",         KnotTonkInstanceTest::testTonkAddInsertsItem);
        run("tonk clear removes content",    KnotTonkInstanceTest::testTonkClearRemovesContent);

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        System.exit(failed == 0 ? 0 : 1);
    }

    @FunctionalInterface interface Throwing { void run() throws Exception; }
    private static void run(String name, Throwing t) {
        try { t.run(); }
        catch (Exception e) {
            System.out.println("  ERROR in " + name + ": " + e);
            e.printStackTrace(System.out);
            failed++;
        }
    }
}
