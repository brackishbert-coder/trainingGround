package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;
import Parser.Stmt;

/**
 * Tests for BoxFunction.call() with bound parameters.
 *
 * Without a resolver pass, locals is empty and lookUpVariable falls back to
 * globals. To test parameter binding, we call interp.resolve(varRef, 0) for
 * each variable reference used in the body, registering distance=0.
 *
 * WesMap (used by locals) matches Expr.Variable keys by name.lexeme, so
 * any Variable node with the same lexeme will find the registered distance.
 *
 * During call():
 *   1. environment1 = new Environment(closure)
 *   2. params bound: environment1.define(paramName, paramType, argument)
 *   3. interpreter.environment = environment1
 *   4. visitVariableExpr → lookUpVariable → distance=0 → environment1.getAt(0, name)
 *   5. Returns resolved value; environment restored on exit.
 */
public class BoxFunctionArgTest {

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

    private static Interpreter makeInterp(boolean forward) {
        Interpreter i = new Interpreter();
        i.setForward(forward);
        return i;
    }

    /** Cup body containing the given declarations. */
    private static Expr.Cup cup(Parser.Declaration... decls) {
        List<Parser.Declaration> list = new ArrayList<>();
        for (Parser.Declaration d : decls) list.add(d);
        return new Expr.Cup(null, list, "", null);
    }

    private static Stmt.Return fwdReturn(Expr value) {
        return new Stmt.Return(tok(TokenType.FUN, "return"), value);
    }

    private static Stmt.Nruter bwdReturn(Expr value) {
        return new Stmt.Nruter(tok(TokenType.FUN, "nruter"), value);
    }

    /** Variable reference resolved at distance 0 in the given interpreter. */
    private static Expr.Variable resolvedVar(String name, Interpreter interp) {
        Expr.Variable v = new Expr.Variable(tok(TokenType.IDENTIFIER, name));
        interp.resolve(v, 0);
        return v;
    }

    /** Zero-arg forward function. */
    private static BoxFunction fn(Expr.Cup body, List<Token> types, List<Token> names,
                                  Environment closure, boolean isForward) {
        return new BoxFunction(body, "testFn", types, names, closure, isForward, false);
    }

    // ---- Single parameter -------------------------------------------------------

    private static void testSingleParam_double() {
        System.out.println("--- single param: x=42.0 → return x = 42.0 ---");
        Interpreter i = makeInterp(true);
        Expr.Variable varX = resolvedVar("x", i);
        Expr.Cup body = cup(fwdReturn(varX));
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "Number")),
            list(tok(TokenType.IDENTIFIER, "x")),
            i.globals, true);
        eq("result = 42.0", 42.0, f.call(i, list(42.0)));
    }

    private static void testSingleParam_string() {
        System.out.println("--- single param: s='hello' → return s = 'hello' ---");
        Interpreter i = makeInterp(true);
        Expr.Variable varS = resolvedVar("s", i);
        Expr.Cup body = cup(fwdReturn(varS));
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "String")),
            list(tok(TokenType.IDENTIFIER, "s")),
            i.globals, true);
        eq("result = hello", "hello", f.call(i, list("hello")));
    }

    private static void testSingleParam_boolean() {
        System.out.println("--- single param: flag=true → return flag = true ---");
        Interpreter i = makeInterp(true);
        Expr.Variable varFlag = resolvedVar("flag", i);
        Expr.Cup body = cup(fwdReturn(varFlag));
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "Bool")),
            list(tok(TokenType.IDENTIFIER, "flag")),
            i.globals, true);
        eq("result = true", Boolean.TRUE, f.call(i, list(Boolean.TRUE)));
    }

    private static void testSingleParam_null() {
        System.out.println("--- single param: n=null → return n = null ---");
        Interpreter i = makeInterp(true);
        Expr.Variable varN = resolvedVar("n", i);
        Expr.Cup body = cup(fwdReturn(varN));
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "Any")),
            list(tok(TokenType.IDENTIFIER, "n")),
            i.globals, true);
        eq("result = null", null, f.call(i, listNull()));
    }

    // ---- Two parameters --------------------------------------------------------

    private static void testTwoParams_returnsFirst() {
        System.out.println("--- two params: (a=3.0, b=5.0) → return a = 3.0 ---");
        Interpreter i = makeInterp(true);
        Expr.Variable varA = resolvedVar("a", i);
        Expr.Variable varB = resolvedVar("b", i);
        Expr.Cup body = cup(fwdReturn(varA));
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "Number"), tok(TokenType.FUN, "Number")),
            list(tok(TokenType.IDENTIFIER, "a"), tok(TokenType.IDENTIFIER, "b")),
            i.globals, true);
        eq("result = 3.0", 3.0, f.call(i, list(3.0, 5.0)));
    }

    private static void testTwoParams_returnsSecond() {
        System.out.println("--- two params: (a=3.0, b=5.0) → return b = 5.0 ---");
        Interpreter i = makeInterp(true);
        Expr.Variable varA = resolvedVar("a", i);
        Expr.Variable varB = resolvedVar("b", i);
        Expr.Cup body = cup(fwdReturn(varB));
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "Number"), tok(TokenType.FUN, "Number")),
            list(tok(TokenType.IDENTIFIER, "a"), tok(TokenType.IDENTIFIER, "b")),
            i.globals, true);
        eq("result = 5.0", 5.0, f.call(i, list(3.0, 5.0)));
    }

    private static void testTwoParams_differentCalls() {
        System.out.println("--- two params: same fn called twice with different args ---");
        Interpreter i = makeInterp(true);
        Expr.Variable varX = resolvedVar("x", i);
        resolvedVar("y", i);
        Expr.Cup body = cup(fwdReturn(varX));
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "Number"), tok(TokenType.FUN, "Number")),
            list(tok(TokenType.IDENTIFIER, "x"), tok(TokenType.IDENTIFIER, "y")),
            i.globals, true);
        eq("first call = 10.0", 10.0, f.call(i, list(10.0, 20.0)));
        eq("second call = 30.0", 30.0, f.call(i, list(30.0, 40.0)));
    }

    // ---- Param shadows global --------------------------------------------------

    private static void testParamShadowsGlobal() {
        System.out.println("--- param x=42.0 shadows global x=99.0 ---");
        Interpreter i = makeInterp(true);
        i.globals.define("x", (Token) null, 99.0);
        Expr.Variable varX = resolvedVar("x", i);
        Expr.Cup body = cup(fwdReturn(varX));
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "Number")),
            list(tok(TokenType.IDENTIFIER, "x")),
            i.globals, true);
        eq("param value returned (not global)", 42.0, f.call(i, list(42.0)));
    }

    private static void testGlobalUnchangedAfterCall() {
        System.out.println("--- global x=99.0 unchanged after call with param x=42.0 ---");
        Interpreter i = makeInterp(true);
        i.globals.define("x", (Token) null, 99.0);
        Expr.Variable varX = resolvedVar("x", i);
        Expr.Cup body = cup(fwdReturn(varX));
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "Number")),
            list(tok(TokenType.IDENTIFIER, "x")),
            i.globals, true);
        f.call(i, list(42.0));
        Object globalX = i.globals.get(tok(TokenType.IDENTIFIER, "x"), false);
        eq("global x still 99.0", 99.0, globalX);
    }

    // ---- Backward function with param ------------------------------------------

    private static void testBackwardParam() {
        System.out.println("--- backward fn: param y=77.0 returned via Nruter ---");
        Interpreter i = makeInterp(false);
        Expr.Variable varY = resolvedVar("y", i);
        Expr.Cup body = cup(bwdReturn(varY));
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "Number")),
            list(tok(TokenType.IDENTIFIER, "y")),
            i.globals, false);
        eq("result = 77.0", 77.0, f.call(i, list(77.0)));
    }

    // ---- Arity -----------------------------------------------------------------

    private static void testArityMatchesParamCount() {
        System.out.println("--- arity = param count ---");
        Interpreter i = makeInterp(true);
        resolvedVar("a", i);
        resolvedVar("b", i);
        resolvedVar("c", i);
        Expr.Cup body = cup();
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "Number"), tok(TokenType.FUN, "Number"), tok(TokenType.FUN, "Number")),
            list(tok(TokenType.IDENTIFIER, "a"), tok(TokenType.IDENTIFIER, "b"), tok(TokenType.IDENTIFIER, "c")),
            i.globals, true);
        eq("arity = 3", 3, f.arity());
    }

    // ---- Environment restored --------------------------------------------------

    private static void testEnvironmentRestoredAfterParamCall() {
        System.out.println("--- environment restored after call with param ---");
        Interpreter i = makeInterp(true);
        Environment before = i.environment;
        Expr.Variable varX = resolvedVar("x", i);
        Expr.Cup body = cup(fwdReturn(varX));
        BoxFunction f = fn(body,
            list(tok(TokenType.FUN, "Number")),
            list(tok(TokenType.IDENTIFIER, "x")),
            i.globals, true);
        f.call(i, list(1.0));
        check("environment restored", i.environment == before);
    }

    // ---- Helpers ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static <T> List<T> list(T... items) {
        List<T> l = new ArrayList<>();
        for (T item : items) l.add(item);
        return l;
    }

    private static List<Object> listNull() {
        List<Object> l = new ArrayList<>();
        l.add(null);
        return l;
    }

    // ---- Entry point -----------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== BoxFunctionArgTest ===\n");

        run("single param double",              BoxFunctionArgTest::testSingleParam_double);
        run("single param string",              BoxFunctionArgTest::testSingleParam_string);
        run("single param boolean",             BoxFunctionArgTest::testSingleParam_boolean);
        run("single param null",                BoxFunctionArgTest::testSingleParam_null);
        run("two params return first",          BoxFunctionArgTest::testTwoParams_returnsFirst);
        run("two params return second",         BoxFunctionArgTest::testTwoParams_returnsSecond);
        run("same fn called twice",             BoxFunctionArgTest::testTwoParams_differentCalls);
        run("param shadows global",             BoxFunctionArgTest::testParamShadowsGlobal);
        run("global unchanged after call",      BoxFunctionArgTest::testGlobalUnchangedAfterCall);
        run("backward fn with param",           BoxFunctionArgTest::testBackwardParam);
        run("arity = param count",              BoxFunctionArgTest::testArityMatchesParamCount);
        run("environment restored after call",  BoxFunctionArgTest::testEnvironmentRestoredAfterParamCall);

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
