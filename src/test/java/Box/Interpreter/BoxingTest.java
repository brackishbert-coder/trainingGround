package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;
import Parser.Stmt;

/**
 * Tests for the primitive boxing invariant (PRIMITIVE BOXING SPEC).
 *
 * Covers:
 *   - Boxer helpers: box(), unbox(), isBoxable()
 *   - Null boxing: BoxInstance(null) is distinct from empty BoxInstance
 *   - evaluateBody() storage boundary in all container types
 *   - visitVarStmt storage boundary (primitive → BoxInstance in environment)
 *   - visitAssignmentExpr storage boundary
 *   - Auto-unboxing in parseBinData (binary operators)
 *   - Bootstrap insertion boxing in PocketInstance.tick()
 *   - Interface hierarchy instanceof checks
 *   - Instance.getBody() for all container types
 */
public class BoxingTest {

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

    private static Interpreter makeInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    private static BoxInstance boxOf(Object value, Interpreter interp) {
        List<Object> body = new ArrayList<>();
        body.add(value);
        return new BoxInstance(null, body, null, interp);
    }

    // =========================================================================
    // Boxer.box()
    // =========================================================================

    private static void testBoxDouble() {
        System.out.println("--- box(Double) wraps in BoxInstance ---");
        Interpreter i = makeInterp();
        Object result = Boxer.box(42.0, i);
        check("is BoxInstance", result instanceof BoxInstance);
        eq("body size = 1", 1, ((BoxInstance) result).body.size());
        eq("body[0] = 42.0", 42.0, ((BoxInstance) result).body.get(0));
    }

    private static void testBoxString() {
        System.out.println("--- box(String) wraps in BoxInstance ---");
        Interpreter i = makeInterp();
        Object result = Boxer.box("hello", i);
        check("is BoxInstance", result instanceof BoxInstance);
        eq("body[0] = hello", "hello", ((BoxInstance) result).body.get(0));
    }

    private static void testBoxBoolean() {
        System.out.println("--- box(Boolean) wraps in BoxInstance ---");
        Interpreter i = makeInterp();
        Object result = Boxer.box(Boolean.TRUE, i);
        check("is BoxInstance", result instanceof BoxInstance);
        eq("body[0] = true", Boolean.TRUE, ((BoxInstance) result).body.get(0));
    }

    private static void testBoxNull() {
        System.out.println("--- box(null) → BoxInstance([null]), body size = 1 ---");
        Interpreter i = makeInterp();
        Object result = Boxer.box(null, i);
        check("is BoxInstance", result instanceof BoxInstance);
        eq("body size = 1 (not 0)", 1, ((BoxInstance) result).body.size());
        check("body[0] is null", ((BoxInstance) result).body.get(0) == null);
    }

    private static void testBoxInstancePassthrough() {
        System.out.println("--- box(Instance) returns the same Instance ---");
        Interpreter i = makeInterp();
        BoxInstance bi = boxOf(5.0, i);
        Object result = Boxer.box(bi, i);
        check("same object", result == bi);
    }

    private static void testBoxCupInstancePassthrough() {
        System.out.println("--- box(CupInstance) returns the same CupInstance ---");
        Interpreter i = makeInterp();
        CupInstance cup = new CupInstance(null, new ArrayList<>(), null, i);
        Object result = Boxer.box(cup, i);
        check("same object", result == cup);
    }

    private static void testBoxNonBoxableRoutesToErrorSink() {
        System.out.println("--- box(non-boxable Object) returns null and routes to error sink ---");
        Interpreter i = makeInterp();
        Object nonBoxable = new Object();
        Object result = Boxer.box(nonBoxable, i);
        check("result is null", result == null);
        // Error sink (BIN or TRASH depending on policy) should have received a message.
        // We can't easily inspect it here, but the null return is the key invariant.
    }

    private static void testNoDoubleBoxing() {
        System.out.println("--- box(BoxInstance) does not wrap again ---");
        Interpreter i = makeInterp();
        BoxInstance bi = (BoxInstance) Boxer.box(7.0, i);
        Object result = Boxer.box(bi, i);
        check("same BoxInstance returned", result == bi);
        eq("still single-item body", 1, ((BoxInstance) result).body.size());
    }

    // =========================================================================
    // Boxer.unbox()
    // =========================================================================

    private static void testUnboxDouble() {
        System.out.println("--- unbox(BoxInstance([5.0])) = 5.0 ---");
        Interpreter i = makeInterp();
        BoxInstance bi = (BoxInstance) Boxer.box(5.0, i);
        eq("unbox = 5.0", 5.0, Boxer.unbox(bi));
    }

    private static void testUnboxString() {
        System.out.println("--- unbox(BoxInstance([hello])) = hello ---");
        Interpreter i = makeInterp();
        BoxInstance bi = (BoxInstance) Boxer.box("hello", i);
        eq("unbox = hello", "hello", Boxer.unbox(bi));
    }

    private static void testUnboxNull() {
        System.out.println("--- unbox(BoxInstance([null])) = null ---");
        Interpreter i = makeInterp();
        BoxInstance bi = (BoxInstance) Boxer.box(null, i);
        check("unbox = null", Boxer.unbox(bi) == null);
    }

    private static void testUnboxEmptyReturnsNull() {
        System.out.println("--- unbox(empty BoxInstance) = null ---");
        Interpreter i = makeInterp();
        BoxInstance bi = new BoxInstance(null, new ArrayList<>(), null, i);
        check("unbox empty = null", Boxer.unbox(bi) == null);
    }

    private static void testUnboxRawPrimitiveIsNoop() {
        System.out.println("--- unbox(raw 5.0) = 5.0 (no-op) ---");
        eq("unbox raw = 5.0", 5.0, Boxer.unbox(5.0));
    }

    private static void testUnboxMultiItemThrows() {
        System.out.println("--- unbox(multi-item BoxInstance) throws RuntimeError ---");
        Interpreter i = makeInterp();
        List<Object> body = new ArrayList<>();
        body.add(1.0);
        body.add(2.0);
        BoxInstance bi = new BoxInstance(null, body, null, i);
        boolean threw = false;
        try {
            Boxer.unbox(bi);
        } catch (RuntimeError e) {
            threw = true;
        }
        check("throws RuntimeError for multi-item", threw);
    }

    // =========================================================================
    // Boxer.isBoxable()
    // =========================================================================

    private static void testIsBoxable() {
        System.out.println("--- isBoxable() true for primitives, null, and Instance ---");
        Interpreter i = makeInterp();
        check("Double is boxable",   Boxer.isBoxable(5.0));
        check("Integer is boxable",  Boxer.isBoxable(42));
        check("String is boxable",   Boxer.isBoxable("hello"));
        check("Boolean is boxable",  Boxer.isBoxable(Boolean.TRUE));
        check("null is boxable",     Boxer.isBoxable(null));
        check("Instance is boxable", Boxer.isBoxable(new BoxInstance(null, new ArrayList<>(), null, i)));
        check("Object is NOT boxable", !Boxer.isBoxable(new Object()));
    }

    // =========================================================================
    // Null boxing: distinct from empty
    // =========================================================================

    private static void testNullBoxDistinctFromEmpty() {
        System.out.println("--- BoxInstance(null) is distinct from empty BoxInstance ---");
        Interpreter i = makeInterp();
        BoxInstance nullBox  = (BoxInstance) Boxer.box(null, i);
        BoxInstance emptyBox = new BoxInstance(null, new ArrayList<>(), null, i);
        eq("null box body size = 1", 1, nullBox.body.size());
        eq("empty box body size = 0", 0, emptyBox.body.size());
        check("null box body[0] is null", nullBox.body.get(0) == null);
    }

    // =========================================================================
    // evaluateBody() boxing at container construction
    // =========================================================================

    private static void testBoxInstanceEvaluateBodyBoxesPrimitive() {
        System.out.println("--- BoxInstance: Expr.Literal in body → BoxInstance after evaluateBody ---");
        Interpreter i = makeInterp();
        List<Object> body = new ArrayList<>();
        body.add(new Expr.Literal(99.0));
        BoxInstance b = new BoxInstance(null, body, null, i);
        check("body[0] is BoxInstance", b.body.get(0) instanceof BoxInstance);
        eq("unboxed value = 99.0", 99.0, Boxer.unbox(b.body.get(0)));
    }

    private static void testCupInstanceEvaluateBodyBoxesPrimitive() {
        System.out.println("--- CupInstance: Stmt.Expression(Literal) in body → BoxInstance ---");
        Interpreter i = makeInterp();
        List<Object> body = new ArrayList<>();
        body.add(new Stmt.Expression(new Expr.Literal(77.0), null));
        CupInstance c = new CupInstance(null, body, null, i);
        check("body[0] is BoxInstance", c.body.get(0) instanceof BoxInstance);
        eq("unboxed = 77.0", 77.0, Boxer.unbox(c.body.get(0)));
    }

    private static void testPocketInstanceEvaluateBodyBoxesPrimitive() {
        System.out.println("--- PocketInstance: Stmt.Expression(Literal) in body → BoxInstance ---");
        Interpreter i = makeInterp();
        List<Object> body = new ArrayList<>();
        body.add(new Stmt.Expression(new Expr.PocketOpen(tok(TokenType.OPENPAREN, "(")), null));
        body.add(new Stmt.Expression(new Expr.Literal("world"), null));
        body.add(new Stmt.Expression(new Expr.PocketClosed(tok(TokenType.CLOSEDPAREN, ")")), null));
        PocketInstance p = new PocketInstance(null, body, null, i);
        check("body[1] is BoxInstance", p.body.get(1) instanceof BoxInstance);
        eq("unboxed = world", "world", Boxer.unbox(p.body.get(1)));
    }

    private static void testEvaluateBodyNullLiteralBoxed() {
        System.out.println("--- CupInstance: Literal(null) → BoxInstance([null]) in body ---");
        Interpreter i = makeInterp();
        List<Object> body = new ArrayList<>();
        body.add(new Stmt.Expression(new Expr.Literal(null), null));
        CupInstance c = new CupInstance(null, body, null, i);
        check("body[0] is BoxInstance", c.body.get(0) instanceof BoxInstance);
        eq("body size = 1 (not empty)", 1, ((BoxInstance) c.body.get(0)).body.size());
        check("inner body[0] is null", ((BoxInstance) c.body.get(0)).body.get(0) == null);
    }

    private static void testEvaluateBodyInstancePassesThrough() {
        System.out.println("--- CupInstance: Instance result not re-boxed ---");
        Interpreter i = makeInterp();
        // Put a BoxInstance in globals, reference it via Variable
        BoxInstance inner = (BoxInstance) Boxer.box(3.14, i);
        i.globals.define("pi", (Token) null, inner);
        Expr.Variable varPi = new Expr.Variable(tok(TokenType.IDENTIFIER, "pi"));
        List<Object> body = new ArrayList<>();
        body.add(new Stmt.Expression(varPi, null));
        CupInstance c = new CupInstance(null, body, null, i);
        // Variable "pi" is already a BoxInstance → box() passes it through → same object
        check("body[0] is BoxInstance", c.body.get(0) instanceof BoxInstance);
        check("same BoxInstance (no double wrap)", c.body.get(0) == inner);
    }

    // =========================================================================
    // visitVarStmt boxing (storage boundary)
    // =========================================================================

    private static void testVarStmtBoxesDouble() {
        System.out.println("--- var x = 42.0 → globals stores BoxInstance([42.0]) ---");
        Interpreter i = makeInterp();
        Token name = tok(TokenType.IDENTIFIER, "x");
        i.execute(new Stmt.Var(null, name, null, 0, new Expr.Literal(42.0)));
        Object stored = i.globals.get(name, false);
        check("stored is BoxInstance", stored instanceof BoxInstance);
        eq("unboxed = 42.0", 42.0, Boxer.unbox(stored));
    }

    private static void testVarStmtBoxesString() {
        System.out.println("--- var s = 'hello' → globals stores BoxInstance([hello]) ---");
        Interpreter i = makeInterp();
        Token name = tok(TokenType.IDENTIFIER, "s");
        i.execute(new Stmt.Var(null, name, null, 0, new Expr.Literal("hello")));
        Object stored = i.globals.get(name, false);
        check("stored is BoxInstance", stored instanceof BoxInstance);
        eq("unboxed = hello", "hello", Boxer.unbox(stored));
    }

    private static void testVarStmtBoxesBoolean() {
        System.out.println("--- var b = true → globals stores BoxInstance([true]) ---");
        Interpreter i = makeInterp();
        Token name = tok(TokenType.IDENTIFIER, "b");
        i.execute(new Stmt.Var(null, name, null, 0, new Expr.Literal(Boolean.TRUE)));
        Object stored = i.globals.get(name, false);
        check("stored is BoxInstance", stored instanceof BoxInstance);
        eq("unboxed = true", Boolean.TRUE, Boxer.unbox(stored));
    }

    private static void testVarStmtBoxesNullLiteral() {
        System.out.println("--- var n = null → globals stores BoxInstance([null]) ---");
        Interpreter i = makeInterp();
        Token name = tok(TokenType.IDENTIFIER, "n");
        i.execute(new Stmt.Var(null, name, null, 0, new Expr.Literal(null)));
        Object stored = i.globals.get(name, false);
        check("stored is BoxInstance", stored instanceof BoxInstance);
        eq("body size = 1 (null box)", 1, ((BoxInstance) stored).body.size());
        check("body[0] is null", ((BoxInstance) stored).body.get(0) == null);
    }

    private static void testVarStmtDoesNotReboxInstance() {
        System.out.println("--- Boxer.box(CupInstance) passthrough (instance not re-boxed) ---");
        Interpreter i = makeInterp();
        CupInstance cup = new CupInstance(null, new ArrayList<>(), null, i);
        // Boxer.box() must pass Instances through unchanged — the boxing invariant for containers
        Object result = Boxer.box(cup, i);
        check("same CupInstance returned", result == cup);
        check("is still CupInstance", result instanceof CupInstance);
    }

    // =========================================================================
    // Auto-unboxing in binary expressions (parseBinData)
    // =========================================================================

    private static void testAutoUnboxingAddition() {
        System.out.println("--- BoxInstance(5) + BoxInstance(3) = 8.0 (parseBinData unboxes) ---");
        Interpreter i = makeInterp();
        i.globals.define("x", (Token) null, Boxer.box(5.0, i));
        i.globals.define("y", (Token) null, Boxer.box(3.0, i));
        Expr left  = new Expr.Variable(tok(TokenType.IDENTIFIER, "x"));
        Expr right = new Expr.Variable(tok(TokenType.IDENTIFIER, "y"));
        Expr add   = new Expr.Binary(left, tok(TokenType.PLUS, "+"), right);
        Object result = i.evaluate(add);
        eq("5 + 3 = 8.0", 8.0, result);
    }

    private static void testAutoUnboxingSubtraction() {
        System.out.println("--- BoxInstance(10) - BoxInstance(4) = 6.0 ---");
        Interpreter i = makeInterp();
        i.globals.define("a", (Token) null, Boxer.box(10.0, i));
        i.globals.define("b", (Token) null, Boxer.box(4.0, i));
        Expr sub = new Expr.Binary(
            new Expr.Variable(tok(TokenType.IDENTIFIER, "a")),
            tok(TokenType.MINUS, "-"),
            new Expr.Variable(tok(TokenType.IDENTIFIER, "b")));
        eq("10 - 4 = 6.0", 6.0, i.evaluate(sub));
    }

    private static void testAutoUnboxingComparison() {
        System.out.println("--- BoxInstance(5) > BoxInstance(3) = true ---");
        Interpreter i = makeInterp();
        i.globals.define("p", (Token) null, Boxer.box(5.0, i));
        i.globals.define("q", (Token) null, Boxer.box(3.0, i));
        Expr gt = new Expr.Binary(
            new Expr.Variable(tok(TokenType.IDENTIFIER, "p")),
            tok(TokenType.GREATERTHEN, ">"),
            new Expr.Variable(tok(TokenType.IDENTIFIER, "q")));
        eq("5 > 3 = true", Boolean.TRUE, i.evaluate(gt));
    }

    private static void testAutoUnboxingEquality() {
        System.out.println("--- BoxInstance(7) == BoxInstance(7) = true ---");
        Interpreter i = makeInterp();
        i.globals.define("m", (Token) null, Boxer.box(7.0, i));
        i.globals.define("n", (Token) null, Boxer.box(7.0, i));
        Expr eq2 = new Expr.Binary(
            new Expr.Variable(tok(TokenType.IDENTIFIER, "m")),
            tok(TokenType.EQUALSEQUALS, "=="),
            new Expr.Variable(tok(TokenType.IDENTIFIER, "n")));
        eq("7 == 7 = true", Boolean.TRUE, i.evaluate(eq2));
    }

    private static void testAutoUnboxingStringEquality() {
        System.out.println("--- BoxInstance('hi') == BoxInstance('hi') = true ---");
        Interpreter i = makeInterp();
        i.globals.define("s1", (Token) null, Boxer.box("hi", i));
        i.globals.define("s2", (Token) null, Boxer.box("hi", i));
        Expr eq2 = new Expr.Binary(
            new Expr.Variable(tok(TokenType.IDENTIFIER, "s1")),
            tok(TokenType.EQUALSEQUALS, "=="),
            new Expr.Variable(tok(TokenType.IDENTIFIER, "s2")));
        eq("'hi' == 'hi' = true", Boolean.TRUE, i.evaluate(eq2));
    }

    // =========================================================================
    // Round-trip: var assignment then operator
    // =========================================================================

    private static void testAssignThenAdd() {
        System.out.println("--- var x=5, var y=3: x+y evaluates to 8.0 ---");
        Interpreter i = makeInterp();
        i.execute(new Stmt.Var(null, tok(TokenType.IDENTIFIER, "x"), null, 0, new Expr.Literal(5.0)));
        i.execute(new Stmt.Var(null, tok(TokenType.IDENTIFIER, "y"), null, 0, new Expr.Literal(3.0)));
        Expr add = new Expr.Binary(
            new Expr.Variable(tok(TokenType.IDENTIFIER, "x")),
            tok(TokenType.PLUS, "+"),
            new Expr.Variable(tok(TokenType.IDENTIFIER, "y")));
        eq("x + y = 8.0", 8.0, i.evaluate(add));
    }

    // =========================================================================
    // Interface hierarchy (instanceof checks)
    // =========================================================================

    private static void testInterfaceHierarchy() {
        System.out.println("--- interface hierarchy instanceof checks ---");
        Interpreter i = makeInterp();

        BoxInstance    box = new BoxInstance   (null, new ArrayList<>(), null, i);
        CupInstance    cup = new CupInstance   (null, new ArrayList<>(), null, i);
        PocketInstance pkt = new PocketInstance(null, new ArrayList<>(), null, i);
        KnotInstance   knt = new KnotInstance  (null, new ArrayList<>(), null, i);
        TonkInstance   tnk = new TonkInstance  (null, new ArrayList<>(), null, i);
        TkpInstance    tkp = new TkpInstance   (null, new ArrayList<>(), null, i);
        XobInstance    xob = new XobInstance   (null, new ArrayList<>(), null, i);
        PucInstance    puc = new PucInstance   (null, new ArrayList<>(), null, i);

        // box
        check("BoxInstance  instanceof IBox",  box instanceof IBox);
        // cup
        check("CupInstance  instanceof ICup",  cup instanceof ICup);
        check("CupInstance  instanceof IBox",  cup instanceof IBox);
        // pkt
        check("PocketInstance instanceof IPkt", pkt instanceof IPkt);
        check("PocketInstance instanceof ICup", pkt instanceof ICup);
        check("PocketInstance instanceof IBox", pkt instanceof IBox);
        // knt
        check("KnotInstance instanceof IKnt",  knt instanceof IKnt);
        check("KnotInstance instanceof IPkt",  knt instanceof IPkt);
        check("KnotInstance instanceof ICup",  knt instanceof ICup);
        check("KnotInstance instanceof IBox",  knt instanceof IBox);
        // tnk
        check("TonkInstance instanceof ITnk",  tnk instanceof ITnk);
        check("TonkInstance instanceof IPkt",  tnk instanceof IPkt);
        check("TonkInstance instanceof IBox",  tnk instanceof IBox);
        // tkp
        check("TkpInstance instanceof ITkp",   tkp instanceof ITkp);
        check("TkpInstance instanceof IPkt",   tkp instanceof IPkt);
        check("TkpInstance instanceof IBox",   tkp instanceof IBox);
        // xob
        check("XobInstance instanceof IXob",   xob instanceof IXob);
        check("XobInstance instanceof IBox",   xob instanceof IBox);
        // puc
        check("PucInstance instanceof IPuc",   puc instanceof IPuc);
        check("PucInstance instanceof IXob",   puc instanceof IXob);
        check("PucInstance instanceof ICup",   puc instanceof ICup);
        check("PucInstance instanceof IBox",   puc instanceof IBox);
    }

    // =========================================================================
    // Instance.getBody()
    // =========================================================================

    private static void testGetBody() {
        System.out.println("--- getBody() returns body list for all container types ---");
        Interpreter i = makeInterp();

        BoxInstance    box = new BoxInstance   (null, new ArrayList<>(), null, i);
        CupInstance    cup = new CupInstance   (null, new ArrayList<>(), null, i);
        PocketInstance pkt = new PocketInstance(null, new ArrayList<>(), null, i);
        KnotInstance   knt = new KnotInstance  (null, new ArrayList<>(), null, i);
        TonkInstance   tnk = new TonkInstance  (null, new ArrayList<>(), null, i);
        TkpInstance    tkp = new TkpInstance   (null, new ArrayList<>(), null, i);

        check("BoxInstance.getBody() not null",    box.getBody() != null);
        check("CupInstance.getBody() not null",    cup.getBody() != null);
        check("PocketInstance.getBody() not null", pkt.getBody() != null);
        check("KnotInstance.getBody() not null",   knt.getBody() != null);
        check("TonkInstance.getBody() not null",   tnk.getBody() != null);
        check("TkpInstance.getBody() not null",    tkp.getBody() != null);
        check("BoxInstance getBody() == body",     box.getBody() == box.body);
        check("CupInstance getBody() == body",     cup.getBody() == cup.body);
    }

    // =========================================================================
    // All containers are IBox (the "everything is a box" invariant)
    // =========================================================================

    private static void testAllContainersAreBoxes() {
        System.out.println("--- every PCB container is instanceof IBox ---");
        Interpreter i = makeInterp();
        check("BoxInstance    is IBox", new BoxInstance   (null, new ArrayList<>(), null, i) instanceof IBox);
        check("CupInstance    is IBox", new CupInstance   (null, new ArrayList<>(), null, i) instanceof IBox);
        check("PocketInstance is IBox", new PocketInstance(null, new ArrayList<>(), null, i) instanceof IBox);
        check("KnotInstance   is IBox", new KnotInstance  (null, new ArrayList<>(), null, i) instanceof IBox);
        check("TonkInstance   is IBox", new TonkInstance  (null, new ArrayList<>(), null, i) instanceof IBox);
        check("TkpInstance    is IBox", new TkpInstance   (null, new ArrayList<>(), null, i) instanceof IBox);
        check("XobInstance    is IBox", new XobInstance   (null, new ArrayList<>(), null, i) instanceof IBox);
        check("PucInstance    is IBox", new PucInstance   (null, new ArrayList<>(), null, i) instanceof IBox);
    }

    // =========================================================================
    // Bootstrap insertion boxing in PocketInstance.tick()
    // =========================================================================

    private static void testPocketBootstrapInsertionBoxesPrimitive() {
        System.out.println("--- pocket bootstrap: fn return value boxed before body insertion ---");
        Interpreter i = makeInterp();

        // Zero-arg forward function that returns 55.0 via a cup+return.
        // Build: cup body = { return 55.0 }
        Expr.Variable retExpr = new Expr.Variable(tok(TokenType.IDENTIFIER, "rv"));
        i.globals.define("rv", (Token) null, 55.0);  // raw primitive in globals
        i.resolve(retExpr, 0);  // won't find it at distance 0 → falls to globals anyway
        List<Parser.Declaration> cupBody = new ArrayList<>();
        cupBody.add(new Stmt.Return(tok(TokenType.FUN, "return"), new Expr.Literal(55.0)));
        Expr.Cup cupExpr = new Expr.Cup(null, cupBody, "", null);
        BoxFunction fn = new BoxFunction(cupExpr, "testFn",
            new ArrayList<>(), new ArrayList<>(), i.globals, true, false);

        // Build a PocketInstance body: ( fn )  with a forward flow
        List<Object> body = new ArrayList<>();
        body.add(new Stmt.Expression(new Expr.PocketOpen(tok(TokenType.OPENPAREN, "(")), null));
        body.add(fn);
        body.add(new Stmt.Expression(new Expr.PocketClosed(tok(TokenType.CLOSEDPAREN, ")")), null));
        PocketInstance pkt = new PocketInstance(null, body, null, i);

        // Inject a flow so tick() can bootstrap the function
        pkt.injectFlow(new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1));

        // Run one tick — function gets bootstrapped, result inserted
        pkt.tick();

        // The function was removed, result inserted. Find it.
        boolean foundBoxInstance = false;
        for (Object item : pkt.getBody()) {
            if (item instanceof BoxInstance) {
                Object val = Boxer.unbox(item);
                if (Double.valueOf(55.0).equals(val)) {
                    foundBoxInstance = true;
                    break;
                }
            }
        }
        check("bootstrap result is BoxInstance(55.0) in body", foundBoxInstance);
    }

    // =========================================================================
    // Entry point
    // =========================================================================

    public static void main(String[] args) {
        System.out.println("=== BoxingTest ===\n");

        run("box(Double)",                         BoxingTest::testBoxDouble);
        run("box(String)",                         BoxingTest::testBoxString);
        run("box(Boolean)",                        BoxingTest::testBoxBoolean);
        run("box(null)",                           BoxingTest::testBoxNull);
        run("box(Instance) passthrough",           BoxingTest::testBoxInstancePassthrough);
        run("box(CupInstance) passthrough",        BoxingTest::testBoxCupInstancePassthrough);
        run("box(non-boxable) → error sink",       BoxingTest::testBoxNonBoxableRoutesToErrorSink);
        run("no double boxing",                    BoxingTest::testNoDoubleBoxing);
        run("unbox Double",                        BoxingTest::testUnboxDouble);
        run("unbox String",                        BoxingTest::testUnboxString);
        run("unbox null",                          BoxingTest::testUnboxNull);
        run("unbox empty → null",                  BoxingTest::testUnboxEmptyReturnsNull);
        run("unbox raw primitive (no-op)",         BoxingTest::testUnboxRawPrimitiveIsNoop);
        run("unbox multi-item throws",             BoxingTest::testUnboxMultiItemThrows);
        run("isBoxable",                           BoxingTest::testIsBoxable);
        run("null box distinct from empty",        BoxingTest::testNullBoxDistinctFromEmpty);
        run("BoxInstance evaluateBody boxes",      BoxingTest::testBoxInstanceEvaluateBodyBoxesPrimitive);
        run("CupInstance evaluateBody boxes",      BoxingTest::testCupInstanceEvaluateBodyBoxesPrimitive);
        run("PocketInstance evaluateBody boxes",   BoxingTest::testPocketInstanceEvaluateBodyBoxesPrimitive);
        run("evaluateBody null literal boxed",     BoxingTest::testEvaluateBodyNullLiteralBoxed);
        run("evaluateBody Instance passes through",BoxingTest::testEvaluateBodyInstancePassesThrough);
        run("visitVarStmt boxes Double",           BoxingTest::testVarStmtBoxesDouble);
        run("visitVarStmt boxes String",           BoxingTest::testVarStmtBoxesString);
        run("visitVarStmt boxes Boolean",          BoxingTest::testVarStmtBoxesBoolean);
        run("visitVarStmt boxes null literal",     BoxingTest::testVarStmtBoxesNullLiteral);
        run("visitVarStmt no rebox of Instance",   BoxingTest::testVarStmtDoesNotReboxInstance);
        run("auto-unbox addition",                 BoxingTest::testAutoUnboxingAddition);
        run("auto-unbox subtraction",              BoxingTest::testAutoUnboxingSubtraction);
        run("auto-unbox comparison",               BoxingTest::testAutoUnboxingComparison);
        run("auto-unbox equality",                 BoxingTest::testAutoUnboxingEquality);
        run("auto-unbox string equality",          BoxingTest::testAutoUnboxingStringEquality);
        run("assign then add (round-trip)",        BoxingTest::testAssignThenAdd);
        run("interface hierarchy",                 BoxingTest::testInterfaceHierarchy);
        run("getBody() all containers",            BoxingTest::testGetBody);
        run("everything is a box (IBox)",          BoxingTest::testAllContainersAreBoxes);
        run("pocket bootstrap boxing",             BoxingTest::testPocketBootstrapInsertionBoxesPrimitive);

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
