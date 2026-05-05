package Box.Interpreter;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;

/**
 * Tests for basic expression evaluation through the Interpreter visitor.
 *
 * Covers: Literal, Unary (MINUS, QMARK), Binary arithmetic and comparison,
 * and global Variable lookup. All tests run with forward=true.
 *
 * The interpreter's forward flag must be true for visitExpressionStmt and
 * most binary/unary operators to behave in their "normal" direction.
 */
public class InterpreterExpressionTest {

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

    private static Interpreter makeInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    private static Token op(TokenType type) {
        return new Token(type, type.toString(), null, null, null, 0, 0, 0, 0);
    }

    private static Token idTok(String name) {
        return new Token(TokenType.IDENTIFIER, name, null, null, null, 0, 0, 0, 0);
    }

    // ---- Literal ------------------------------------------------------------

    private static void testLiteralDouble() {
        System.out.println("--- Literal Double ---");
        Interpreter i = makeInterp();
        eq("42.0", 42.0, i.evaluate(new Expr.Literal(42.0)));
    }

    private static void testLiteralString() {
        System.out.println("--- Literal String ---");
        Interpreter i = makeInterp();
        eq("hello", "hello", i.evaluate(new Expr.Literal("hello")));
    }

    private static void testLiteralBooleanTrue() {
        System.out.println("--- Literal Boolean true ---");
        Interpreter i = makeInterp();
        eq("true", true, i.evaluate(new Expr.Literal(Boolean.TRUE)));
    }

    private static void testLiteralBooleanFalse() {
        System.out.println("--- Literal Boolean false ---");
        Interpreter i = makeInterp();
        eq("false", false, i.evaluate(new Expr.Literal(Boolean.FALSE)));
    }

    private static void testLiteralNull() {
        System.out.println("--- Literal null ---");
        Interpreter i = makeInterp();
        eq("null literal", null, i.evaluate(new Expr.Literal(null)));
    }

    // ---- Unary --------------------------------------------------------------

    private static void testUnaryMinusDouble() {
        System.out.println("--- Unary MINUS on Double ---");
        Interpreter i = makeInterp();
        Expr expr = new Expr.Unary(op(TokenType.MINUS), new Expr.Literal(5.0));
        eq("-5.0", -5.0, i.evaluate(expr));
    }

    private static void testUnaryMinusNegative() {
        System.out.println("--- Unary MINUS on negative Double (double negation) ---");
        Interpreter i = makeInterp();
        Expr inner = new Expr.Unary(op(TokenType.MINUS), new Expr.Literal(3.0));
        Expr expr  = new Expr.Unary(op(TokenType.MINUS), inner);
        eq("--3.0 == 3.0", 3.0, i.evaluate(expr));
    }

    private static void testUnaryNotTrue() {
        System.out.println("--- Unary QMARK (not) on true ---");
        Interpreter i = makeInterp();
        Expr expr = new Expr.Unary(op(TokenType.QMARK), new Expr.Literal(Boolean.TRUE));
        eq("!true == false", false, i.evaluate(expr));
    }

    private static void testUnaryNotFalse() {
        System.out.println("--- Unary QMARK (not) on false ---");
        Interpreter i = makeInterp();
        Expr expr = new Expr.Unary(op(TokenType.QMARK), new Expr.Literal(Boolean.FALSE));
        eq("!false == true", true, i.evaluate(expr));
    }

    // ---- Binary arithmetic --------------------------------------------------

    private static Expr.Binary bin(TokenType type, double left, double right) {
        return new Expr.Binary(new Expr.Literal(left), op(type), new Expr.Literal(right));
    }

    private static void testBinaryPlus() {
        System.out.println("--- Binary PLUS ---");
        Interpreter i = makeInterp();
        eq("3.0 + 4.0 = 7.0", 7.0, i.evaluate(bin(TokenType.PLUS, 3.0, 4.0)));
    }

    private static void testBinaryMinus() {
        System.out.println("--- Binary MINUS ---");
        Interpreter i = makeInterp();
        eq("10.0 - 3.0 = 7.0", 7.0, i.evaluate(bin(TokenType.MINUS, 10.0, 3.0)));
    }

    private static void testBinaryTimes() {
        System.out.println("--- Binary TIMES ---");
        Interpreter i = makeInterp();
        eq("3.0 * 4.0 = 12.0", 12.0, i.evaluate(bin(TokenType.TIMES, 3.0, 4.0)));
    }

    private static void testBinaryDivide() {
        System.out.println("--- Binary FORWARDSLASH ---");
        Interpreter i = makeInterp();
        eq("10.0 / 2.0 = 5.0", 5.0, i.evaluate(bin(TokenType.FORWARDSLASH, 10.0, 2.0)));
    }

    private static void testBinaryMod() {
        System.out.println("--- Binary MOD ---");
        Interpreter i = makeInterp();
        eq("7.0 % 3.0 = 1.0", 1.0, i.evaluate(bin(TokenType.MOD, 7.0, 3.0)));
    }

    // ---- Binary comparison --------------------------------------------------

    private static void testBinaryLessThenTrue() {
        System.out.println("--- Binary LESSTHEN true ---");
        Interpreter i = makeInterp();
        eq("2.0 < 5.0", true, i.evaluate(bin(TokenType.LESSTHEN, 2.0, 5.0)));
    }

    private static void testBinaryLessThenFalse() {
        System.out.println("--- Binary LESSTHEN false ---");
        Interpreter i = makeInterp();
        eq("5.0 < 2.0 = false", false, i.evaluate(bin(TokenType.LESSTHEN, 5.0, 2.0)));
    }

    private static void testBinaryGreaterThenTrue() {
        System.out.println("--- Binary GREATERTHEN true ---");
        Interpreter i = makeInterp();
        eq("5.0 > 2.0", true, i.evaluate(bin(TokenType.GREATERTHEN, 5.0, 2.0)));
    }

    private static void testBinaryGreaterThenFalse() {
        System.out.println("--- Binary GREATERTHEN false ---");
        Interpreter i = makeInterp();
        eq("2.0 > 5.0 = false", false, i.evaluate(bin(TokenType.GREATERTHEN, 2.0, 5.0)));
    }

    private static void testBinaryEqualsEquals() {
        System.out.println("--- Binary EQUALSEQUALS ---");
        Interpreter i = makeInterp();
        eq("3.0 == 3.0 = true",  true,
            i.evaluate(new Expr.Binary(new Expr.Literal(3.0), op(TokenType.EQUALSEQUALS), new Expr.Literal(3.0))));
        eq("3.0 == 4.0 = false", false,
            i.evaluate(new Expr.Binary(new Expr.Literal(3.0), op(TokenType.EQUALSEQUALS), new Expr.Literal(4.0))));
    }

    private static void testBinaryNotEquals() {
        System.out.println("--- Binary NOTEQUALS ---");
        Interpreter i = makeInterp();
        eq("3.0 != 4.0 = true",  true,
            i.evaluate(new Expr.Binary(new Expr.Literal(3.0), op(TokenType.NOTEQUALS), new Expr.Literal(4.0))));
        eq("3.0 != 3.0 = false", false,
            i.evaluate(new Expr.Binary(new Expr.Literal(3.0), op(TokenType.NOTEQUALS), new Expr.Literal(3.0))));
    }

    private static void testStringEquality() {
        System.out.println("--- String EQUALSEQUALS ---");
        Interpreter i = makeInterp();
        eq("hello == hello", true,
            i.evaluate(new Expr.Binary(new Expr.Literal("hello"), op(TokenType.EQUALSEQUALS), new Expr.Literal("hello"))));
        eq("hello == world = false", false,
            i.evaluate(new Expr.Binary(new Expr.Literal("hello"), op(TokenType.EQUALSEQUALS), new Expr.Literal("world"))));
    }

    // ---- Variable lookup via globals ----------------------------------------

    private static void testVariableLookupFromGlobals() {
        System.out.println("--- Variable lookup from globals ---");
        Interpreter i = makeInterp();
        i.globals.define("myVar", (Token) null, 123.0);
        Expr.Variable varExpr = new Expr.Variable(idTok("myVar"));
        eq("myVar lookup", 123.0, i.evaluate(varExpr));
    }

    private static void testVariableLookupString() {
        System.out.println("--- Variable lookup String from globals ---");
        Interpreter i = makeInterp();
        i.globals.define("greeting", (Token) null, "world");
        Expr.Variable varExpr = new Expr.Variable(idTok("greeting"));
        eq("greeting lookup", "world", i.evaluate(varExpr));
    }

    private static void testVariableLookupBoolean() {
        System.out.println("--- Variable lookup Boolean from globals ---");
        Interpreter i = makeInterp();
        i.globals.define("flag", (Token) null, Boolean.TRUE);
        Expr.Variable varExpr = new Expr.Variable(idTok("flag"));
        eq("flag lookup", true, i.evaluate(varExpr));
    }

    // ---- Nested binary expressions ------------------------------------------

    private static void testNestedBinaryExpr() {
        System.out.println("--- Nested binary: (2+3)*4 ---");
        Interpreter i = makeInterp();
        Expr inner = bin(TokenType.PLUS, 2.0, 3.0);
        Expr expr  = new Expr.Binary(inner, op(TokenType.TIMES), new Expr.Literal(4.0));
        eq("(2+3)*4 = 20.0", 20.0, i.evaluate(expr));
    }

    // ---- Entry point --------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== InterpreterExpressionTest ===\n");

        run("Literal Double",             InterpreterExpressionTest::testLiteralDouble);
        run("Literal String",             InterpreterExpressionTest::testLiteralString);
        run("Literal Boolean true",       InterpreterExpressionTest::testLiteralBooleanTrue);
        run("Literal Boolean false",      InterpreterExpressionTest::testLiteralBooleanFalse);
        run("Literal null",               InterpreterExpressionTest::testLiteralNull);
        run("Unary MINUS double",         InterpreterExpressionTest::testUnaryMinusDouble);
        run("Unary MINUS double negation",InterpreterExpressionTest::testUnaryMinusNegative);
        run("Unary NOT true",             InterpreterExpressionTest::testUnaryNotTrue);
        run("Unary NOT false",            InterpreterExpressionTest::testUnaryNotFalse);
        run("Binary PLUS",                InterpreterExpressionTest::testBinaryPlus);
        run("Binary MINUS",               InterpreterExpressionTest::testBinaryMinus);
        run("Binary TIMES",               InterpreterExpressionTest::testBinaryTimes);
        run("Binary DIVIDE",              InterpreterExpressionTest::testBinaryDivide);
        run("Binary MOD",                 InterpreterExpressionTest::testBinaryMod);
        run("Binary LESSTHEN true",       InterpreterExpressionTest::testBinaryLessThenTrue);
        run("Binary LESSTHEN false",      InterpreterExpressionTest::testBinaryLessThenFalse);
        run("Binary GREATERTHEN true",    InterpreterExpressionTest::testBinaryGreaterThenTrue);
        run("Binary GREATERTHEN false",   InterpreterExpressionTest::testBinaryGreaterThenFalse);
        run("Binary EQUALSEQUALS",        InterpreterExpressionTest::testBinaryEqualsEquals);
        run("Binary NOTEQUALS",           InterpreterExpressionTest::testBinaryNotEquals);
        run("String EQUALSEQUALS",        InterpreterExpressionTest::testStringEquality);
        run("Variable lookup Double",     InterpreterExpressionTest::testVariableLookupFromGlobals);
        run("Variable lookup String",     InterpreterExpressionTest::testVariableLookupString);
        run("Variable lookup Boolean",    InterpreterExpressionTest::testVariableLookupBoolean);
        run("Nested binary (2+3)*4",      InterpreterExpressionTest::testNestedBinaryExpr);

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
