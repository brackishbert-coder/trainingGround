package Box.Interpreter;

import java.util.ArrayList;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration.StmtDecl;
import Parser.Expr;
import Parser.Stmt;

/**
 * Tests for the assert/tressa keyword.
 *
 * Covers:
 *   - Forward assert with true boolean: no exception
 *   - Forward assert with false boolean: throws RuntimeError
 *   - Forward assert with null: treated as falsy, throws
 *   - Forward assert with non-null non-boolean: treated as truthy, passes
 *   - Forward assert with numeric zero: treated as truthy (non-null), passes
 *   - Error message contains "assertion failed"
 *   - Backward (tressa) with false condition: no-op, no exception
 *   - Backward with true condition: no-op, no exception
 *   - Stmt.Assert fields preserved correctly
 */
public class AssertTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean cond) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else       { System.out.println("  FAIL  " + name); failed++; }
    }

    private static Token tok(TokenType type, String lex) {
        return new Token(type, lex, lex, null, null, 0, 0, 0, 0);
    }

    private static Interpreter forwardInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    private static Interpreter backwardInterp() {
        Interpreter i = new Interpreter();
        i.setForward(false);
        return i;
    }

    private static void executeAssert(Interpreter i, Expr condition) {
        Token kw = tok(TokenType.ASSERT, "assert");
        Stmt.Assert stmt = new Stmt.Assert(kw, condition);
        i.execute(new StmtDecl(stmt));
    }

    private static void executeTressa(Interpreter i, Expr condition) {
        Token kw = tok(TokenType.TRESSA, "tressa");
        Stmt.Assert stmt = new Stmt.Assert(kw, condition);
        i.execute(new StmtDecl(stmt));
    }

    // ---- forward: passing conditions ----------------------------------------

    private static void testForwardTruePass() {
        System.out.println("--- forward assert(true): no exception ---");
        Interpreter i = forwardInterp();
        boolean threw = false;
        try { executeAssert(i, new Expr.Literal(Boolean.TRUE)); }
        catch (RuntimeError e) { threw = true; }
        check("no exception for true", !threw);
    }

    private static void testForwardNonNullPass() {
        System.out.println("--- forward assert(non-null string): no exception ---");
        Interpreter i = forwardInterp();
        boolean threw = false;
        try { executeAssert(i, new Expr.Literal("non-null")); }
        catch (RuntimeError e) { threw = true; }
        check("non-null string is truthy", !threw);
    }

    private static void testForwardNumericNonZeroPass() {
        System.out.println("--- forward assert(42.0): no exception (non-null = truthy) ---");
        Interpreter i = forwardInterp();
        boolean threw = false;
        try { executeAssert(i, new Expr.Literal(42.0)); }
        catch (RuntimeError e) { threw = true; }
        check("numeric 42 is truthy", !threw);
    }

    // ---- forward: failing conditions ----------------------------------------

    private static void testForwardFalseThrows() {
        System.out.println("--- forward assert(false): throws RuntimeError ---");
        Interpreter i = forwardInterp();
        boolean threw = false;
        try { executeAssert(i, new Expr.Literal(Boolean.FALSE)); }
        catch (RuntimeError e) {
            threw = true;
            check("message contains 'assertion failed'", e.getMessage().contains("assertion failed"));
        }
        check("threw on false", threw);
    }

    private static void testForwardNullThrows() {
        System.out.println("--- forward assert(null): treated as falsy, throws ---");
        Interpreter i = forwardInterp();
        boolean threw = false;
        try { executeAssert(i, new Expr.Literal(null)); }
        catch (RuntimeError e) {
            threw = true;
            check("null error message contains 'assertion failed'", e.getMessage().contains("assertion failed"));
        }
        check("threw on null", threw);
    }

    private static void testForwardFalseErrorToken() {
        System.out.println("--- forward assert failure: RuntimeError carries ASSERT token ---");
        Interpreter i = forwardInterp();
        RuntimeError caught = null;
        try { executeAssert(i, new Expr.Literal(Boolean.FALSE)); }
        catch (RuntimeError e) { caught = e; }
        check("error was thrown", caught != null);
        check("error token is ASSERT", caught != null && caught.token != null
                && caught.token.type == TokenType.ASSERT);
    }

    // ---- backward: both passing and failing are no-ops ----------------------

    private static void testBackwardFalseNoOp() {
        System.out.println("--- backward tressa(false): no exception (no-op) ---");
        Interpreter i = backwardInterp();
        boolean threw = false;
        try { executeTressa(i, new Expr.Literal(Boolean.FALSE)); }
        catch (RuntimeError e) { threw = true; }
        check("no exception in backward mode for false", !threw);
    }

    private static void testBackwardTrueNoOp() {
        System.out.println("--- backward tressa(true): no exception (no-op) ---");
        Interpreter i = backwardInterp();
        boolean threw = false;
        try { executeTressa(i, new Expr.Literal(Boolean.TRUE)); }
        catch (RuntimeError e) { threw = true; }
        check("no exception in backward mode for true", !threw);
    }

    private static void testBackwardNullNoOp() {
        System.out.println("--- backward tressa(null): no exception (no-op) ---");
        Interpreter i = backwardInterp();
        boolean threw = false;
        try { executeTressa(i, new Expr.Literal(null)); }
        catch (RuntimeError e) { threw = true; }
        check("no exception in backward mode for null", !threw);
    }

    // ---- Stmt.Assert structure -----------------------------------------------

    private static void testStmtAssertFields() {
        System.out.println("--- Stmt.Assert fields stored correctly ---");
        Token kw = tok(TokenType.ASSERT, "assert");
        Expr cond = new Expr.Literal(Boolean.TRUE);
        Stmt.Assert stmt = new Stmt.Assert(kw, cond);
        check("keyword stored", stmt.keyword == kw);
        check("condition stored", stmt.condition == cond);
    }

    private static void testStmtAssertCopyConstructor() {
        System.out.println("--- Stmt.Assert copy constructor ---");
        Token kw = tok(TokenType.ASSERT, "assert");
        Expr cond = new Expr.Literal(Boolean.FALSE);
        Stmt.Assert original = new Stmt.Assert(kw, cond);
        Stmt.Assert copy = new Stmt.Assert(original);
        check("copy keyword", copy.keyword == kw);
        check("copy condition", copy.condition == cond);
    }

    // ---- main ---------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== AssertTest ===\n");

        testForwardTruePass();
        testForwardNonNullPass();
        testForwardNumericNonZeroPass();
        testForwardFalseThrows();
        testForwardNullThrows();
        testForwardFalseErrorToken();
        testBackwardFalseNoOp();
        testBackwardTrueNoOp();
        testBackwardNullNoOp();
        testStmtAssertFields();
        testStmtAssertCopyConstructor();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        System.exit(failed == 0 ? 0 : 1);
    }
}
