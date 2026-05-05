package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Expr;
import Parser.Stmt;

/**
 * Tests for BoxFunction.call() through real Expr.Cup bodies.
 *
 * BoxFunction.call():
 *   1. Creates a child Environment from the closure.
 *   2. Binds parameters into that environment.
 *   3. Calls executeCupExpr(body, env) which iterates over the Cup's declaration list.
 *   4. Catches Returns (forward return) or Snruter (backward return) and extracts the value.
 *   5. Direction mismatch: forward fn catching Snruter, or backward fn catching Returns → RuntimeError.
 *
 * Note: lookUpVariable() falls back to globals when no resolver distance is recorded.
 * This means only global-scope variables are accessible from inside function bodies
 * without a resolver pass. Tests use literals and globals accordingly.
 */
public class BoxFunctionCallTest {

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

    /** Build a Cup body containing the given declarations. */
    private static Expr.Cup cup(Declaration... decls) {
        List<Declaration> list = new ArrayList<>();
        for (Declaration d : decls) list.add(d);
        return new Expr.Cup(null, list, "", null);
    }

    /** Forward return: Stmt.Return(keyword, expr) */
    private static Stmt.Return fwdReturn(Expr value) {
        return new Stmt.Return(tok(TokenType.FUN, "return"), value);
    }

    /** Backward return: Stmt.Nruter(keyword, expr) */
    private static Stmt.Nruter bwdReturn(Expr value) {
        return new Stmt.Nruter(tok(TokenType.FUN, "nruter"), value);
    }

    /** Zero-arg BoxFunction with given Cup body and direction. */
    private static BoxFunction zeroArgFn(Expr.Cup body, boolean isForward, Interpreter interp) {
        return new BoxFunction(body, "testFn",
            new ArrayList<>(), new ArrayList<>(),
            interp.globals, isForward, false);
    }

    // ---- Forward function ---------------------------------------------------

    private static void testForwardFnReturnsLiteral() {
        System.out.println("--- forward fn: Stmt.Return(42.0) returns 42.0 ---");
        Interpreter i = makeInterp(true);
        Expr.Cup body = cup(fwdReturn(new Expr.Literal(42.0)));
        BoxFunction fn = zeroArgFn(body, true, i);
        Object result = fn.call(i, new ArrayList<>());
        eq("result = 42.0", 42.0, result);
    }

    private static void testForwardFnReturnsString() {
        System.out.println("--- forward fn: return string ---");
        Interpreter i = makeInterp(true);
        Expr.Cup body = cup(fwdReturn(new Expr.Literal("hi")));
        BoxFunction fn = zeroArgFn(body, true, i);
        eq("result = hi", "hi", fn.call(i, new ArrayList<>()));
    }

    private static void testForwardFnReturnsBoolean() {
        System.out.println("--- forward fn: return boolean ---");
        Interpreter i = makeInterp(true);
        Expr.Cup body = cup(fwdReturn(new Expr.Literal(Boolean.TRUE)));
        BoxFunction fn = zeroArgFn(body, true, i);
        eq("result = true", true, fn.call(i, new ArrayList<>()));
    }

    private static void testForwardFnNoReturnGivesNull() {
        System.out.println("--- forward fn: no return statement → null ---");
        Interpreter i = makeInterp(true);
        // Body has a literal expression (evaluates to 42.0) but no Stmt.Return.
        // executeCupExpr runs it, no Returns thrown → call() returns null.
        Expr.Cup body = cup(new Stmt.Expression(new Expr.Literal(42.0), null));
        BoxFunction fn = zeroArgFn(body, true, i);
        eq("no return = null", null, fn.call(i, new ArrayList<>()));
    }

    private static void testForwardFnEmptyBodyGivesNull() {
        System.out.println("--- forward fn: empty body → null ---");
        Interpreter i = makeInterp(true);
        Expr.Cup body = cup();
        BoxFunction fn = zeroArgFn(body, true, i);
        eq("empty body = null", null, fn.call(i, new ArrayList<>()));
    }

    private static void testForwardFnReturnsGlobalVar() {
        System.out.println("--- forward fn: return global variable ---");
        Interpreter i = makeInterp(true);
        i.globals.define("g", (Token) null, 77.0);
        Expr.Variable varG = new Expr.Variable(tok(TokenType.IDENTIFIER, "g"));
        Expr.Cup body = cup(fwdReturn(varG));
        BoxFunction fn = zeroArgFn(body, true, i);
        eq("result = 77.0", 77.0, fn.call(i, new ArrayList<>()));
    }

    // ---- Backward function --------------------------------------------------

    private static void testBackwardFnReturnsLiteral() {
        System.out.println("--- backward fn: Stmt.Nruter(99.0) returns 99.0 ---");
        Interpreter i = makeInterp(false); // backward interpreter direction
        Expr.Cup body = cup(bwdReturn(new Expr.Literal(99.0)));
        BoxFunction fn = zeroArgFn(body, false, i);
        Object result = fn.call(i, new ArrayList<>());
        eq("result = 99.0", 99.0, result);
    }

    private static void testBackwardFnEmptyBodyGivesNull() {
        System.out.println("--- backward fn: empty body → null ---");
        Interpreter i = makeInterp(false);
        BoxFunction fn = zeroArgFn(cup(), false, i);
        eq("backward empty = null", null, fn.call(i, new ArrayList<>()));
    }

    // ---- Direction mismatch errors ------------------------------------------

    private static void testForwardFnCatchesSnruterThrowsMismatch() {
        System.out.println("--- forward fn catches Snruter → RuntimeError (mismatch) ---");
        // Set interpreter backward so visitNruterStmt fires and throws Snruter.
        // Forward function (isForward=true) catches Snruter → should throw RuntimeError.
        Interpreter i = makeInterp(false);
        Expr.Cup body = cup(bwdReturn(new Expr.Literal(1.0)));
        BoxFunction fn = zeroArgFn(body, true, i); // fn is FORWARD
        boolean threw = false;
        try {
            fn.call(i, new ArrayList<>());
        } catch (RuntimeError e) {
            threw = true;
            check("error message mentions direction mismatch",
                e.getMessage() != null && e.getMessage().contains("mismatch"));
        }
        check("RuntimeError thrown on forward fn + Snruter", threw);
    }

    private static void testBackwardFnCatchesReturnThrowsMismatch() {
        System.out.println("--- backward fn catches Returns → RuntimeError (mismatch) ---");
        // Set interpreter forward so visitReturnStmt fires and throws Returns.
        // Backward function (isForward=false) catches Returns → should throw RuntimeError.
        Interpreter i = makeInterp(true);
        Expr.Cup body = cup(fwdReturn(new Expr.Literal(1.0)));
        BoxFunction fn = zeroArgFn(body, false, i); // fn is BACKWARD
        boolean threw = false;
        try {
            fn.call(i, new ArrayList<>());
        } catch (RuntimeError e) {
            threw = true;
            check("error message mentions direction mismatch",
                e.getMessage() != null && e.getMessage().contains("mismatch"));
        }
        check("RuntimeError thrown on backward fn + Returns", threw);
    }

    // ---- Environment restoration --------------------------------------------

    private static void testCallRestoresEnvironment() {
        System.out.println("--- call() restores interpreter.environment after execution ---");
        Interpreter i = makeInterp(true);
        Environment before = i.environment;
        Expr.Cup body = cup(fwdReturn(new Expr.Literal(0.0)));
        BoxFunction fn = zeroArgFn(body, true, i);
        fn.call(i, new ArrayList<>());
        check("environment restored", i.environment == before);
    }

    private static void testCallRestoresEnvironmentOnMismatch() {
        System.out.println("--- call() restores environment even when RuntimeError thrown ---");
        Interpreter i = makeInterp(false);
        Environment before = i.environment;
        BoxFunction fn = zeroArgFn(cup(bwdReturn(new Expr.Literal(1.0))), true, i);
        try { fn.call(i, new ArrayList<>()); } catch (RuntimeError ignored) {}
        check("environment restored after mismatch error", i.environment == before);
    }

    // ---- Metadata -----------------------------------------------------------

    private static void testArityZero() {
        System.out.println("--- arity() = 0 for zero-arg function ---");
        Interpreter i = makeInterp(true);
        BoxFunction fn = zeroArgFn(cup(), true, i);
        eq("arity 0", 0, fn.arity());
    }

    private static void testGetName() {
        System.out.println("--- getName() returns function name ---");
        Interpreter i = makeInterp(true);
        BoxFunction fn = zeroArgFn(cup(), true, i);
        eq("getName = testFn", "testFn", fn.getName());
    }

    private static void testToStringIncludesName() {
        System.out.println("--- toString() includes fn name ---");
        Interpreter i = makeInterp(true);
        BoxFunction fn = zeroArgFn(cup(), true, i);
        check("toString contains testFn", fn.toString().contains("testFn"));
    }

    private static void testIsForwardFlag() {
        System.out.println("--- isForward flag is preserved ---");
        Interpreter i = makeInterp(true);
        BoxFunction fwd = zeroArgFn(cup(), true, i);
        BoxFunction bwd = zeroArgFn(cup(), false, i);
        check("forward fn isForward = true",  fwd.isForward);
        check("backward fn isForward = false", !bwd.isForward);
    }

    // ---- Entry point --------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== BoxFunctionCallTest ===\n");

        run("forward fn returns literal",            BoxFunctionCallTest::testForwardFnReturnsLiteral);
        run("forward fn returns string",             BoxFunctionCallTest::testForwardFnReturnsString);
        run("forward fn returns boolean",            BoxFunctionCallTest::testForwardFnReturnsBoolean);
        run("forward fn no return = null",           BoxFunctionCallTest::testForwardFnNoReturnGivesNull);
        run("forward fn empty body = null",          BoxFunctionCallTest::testForwardFnEmptyBodyGivesNull);
        run("forward fn returns global var",         BoxFunctionCallTest::testForwardFnReturnsGlobalVar);
        run("backward fn returns literal",           BoxFunctionCallTest::testBackwardFnReturnsLiteral);
        run("backward fn empty body = null",         BoxFunctionCallTest::testBackwardFnEmptyBodyGivesNull);
        run("forward fn + Snruter → mismatch",       BoxFunctionCallTest::testForwardFnCatchesSnruterThrowsMismatch);
        run("backward fn + Returns → mismatch",      BoxFunctionCallTest::testBackwardFnCatchesReturnThrowsMismatch);
        run("call restores environment",             BoxFunctionCallTest::testCallRestoresEnvironment);
        run("call restores env after error",         BoxFunctionCallTest::testCallRestoresEnvironmentOnMismatch);
        run("arity zero",                            BoxFunctionCallTest::testArityZero);
        run("getName",                               BoxFunctionCallTest::testGetName);
        run("toString includes name",                BoxFunctionCallTest::testToStringIncludesName);
        run("isForward flag preserved",              BoxFunctionCallTest::testIsForwardFlag);

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
