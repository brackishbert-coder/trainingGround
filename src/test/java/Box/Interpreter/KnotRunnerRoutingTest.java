package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Parser.Expr;
import Parser.Stmt;
import Box.Token.Token;
import Box.Token.TokenType;

/**
 * Routing tests for KnotRunner.runWithRouting().
 *
 * Routing targets are container variable references found in the SETUP region
 * of a knot body ({ ... ( shape). When the knot produces output, that output
 * is add-injected into every collected target. No output → no injection.
 *
 * Standard fixture layout:
 *   0  Literal(bodyValue)     — body output (executes before knot brackets)
 *   1  CupOpen("a{")          — KNOT left bracket / SETUP left
 *   2  Variable("b")          — routing target (collected, not executed)
 *   3  Variable("c")          — routing target (collected, not executed)
 *   4  PocketOpen("x(")       — SETUP right / CONDITION left
 *   5  Literal(false)         — condition → false terminates the knot
 *   6  CupClose("}a")         — CONDITION true target
 *   7  PocketClose(")x")      — KNOT right bracket / CONDITION false target
 *
 * No JUnit dependency. Run:
 *   javac -cp <classpath> src/test/java/Box/Interpreter/KnotRunnerRoutingTest.java -d /tmp/routingbuild
 *   java  -cp <classpath>:/tmp/routingbuild Box.Interpreter.KnotRunnerRoutingTest
 */
public class KnotRunnerRoutingTest {

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
    // Fixture builders
    // -------------------------------------------------------------------------

    private static Stmt.Expression exprStmt(Expr expr) {
        return new Stmt.Expression(expr, null);
    }

    private static Token tok(TokenType type, String lexeme) {
        return new Token(type, lexeme, null, null, null, 0, 0, 0, 0);
    }

    /**
     * Standard fixture. bodyValue=null omits the body output statement.
     * targetVarNames go into the SETUP region as Expr.Variable refs.
     * Condition is always false so the knot terminates cleanly.
     */
    private static List<Stmt> fixture(Object bodyValue, String... targetVarNames) {
        List<Stmt> stmts = new ArrayList<>();
        if (bodyValue != null)
            stmts.add(exprStmt(new Expr.Literal(bodyValue)));
        stmts.add(exprStmt(new Expr.CupOpen    (tok(TokenType.OPENBRACE,   "a{"))));
        for (String name : targetVarNames)
            stmts.add(exprStmt(new Expr.Variable(tok(TokenType.IDENTIFIER, name))));
        stmts.add(exprStmt(new Expr.PocketOpen  (tok(TokenType.OPENPAREN,   "x("))));
        stmts.add(exprStmt(new Expr.Literal(Boolean.FALSE)));
        stmts.add(exprStmt(new Expr.CupClosed  (tok(TokenType.CLOSEDBRACE, "}a"))));
        stmts.add(exprStmt(new Expr.PocketClosed(tok(TokenType.CLOSEDPAREN, ")x"))));
        return stmts;
    }

    private static BoxInstance emptyBox(Interpreter interp) {
        return new BoxInstance(null, new ArrayList<>(), null, interp);
    }

    /** True if any item in body unboxes equal to expected. */
    private static boolean bodyContainsUnboxed(java.util.List<Object> body, Object expected) {
        for (Object item : body) {
            Object v = Boxer.unbox(item);
            if (expected == null ? v == null : expected.equals(v)) return true;
        }
        return false;
    }

    private static Interpreter makeInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    /** Single target in SETUP receives the body result via add. */
    private static void testSingleTargetReceivesResult() {
        System.out.println("--- single target in SETUP receives result ---");
        Interpreter interp = makeInterp();
        BoxInstance target = emptyBox(interp);
        interp.environment.define("b", (Token) null, target);

        new KnotRunner(null, fixture("hello", "b"), interp).runWithRouting(true);

        check("target body grew",       target.body.size() == 1);
        check("'hello' in target",      bodyContainsUnboxed(target.body, "hello"));
    }

    /** Two targets both receive the result. */
    private static void testMultipleTargetsAllReceiveResult() {
        System.out.println("--- multiple targets all receive result ---");
        Interpreter interp = makeInterp();
        BoxInstance b = emptyBox(interp);
        BoxInstance c = emptyBox(interp);
        interp.environment.define("b", (Token) null, b);
        interp.environment.define("c", (Token) null, c);

        new KnotRunner(null, fixture("hello", "b", "c"), interp).runWithRouting(true);

        check("b received result",  bodyContainsUnboxed(b.body, "hello"));
        check("c received result",  bodyContainsUnboxed(c.body, "hello"));
    }

    /** Knot produces no output → targets unchanged. */
    private static void testNoResultNoRouting() {
        System.out.println("--- no body output → targets unchanged ---");
        Interpreter interp = makeInterp();
        BoxInstance target = emptyBox(interp);
        interp.environment.define("b", (Token) null, target);

        new KnotRunner(null, fixture(null, "b"), interp).runWithRouting(true);

        eq("target size unchanged", 0, target.body.size());
    }

    /** No targets in SETUP → no crash, no injection. */
    private static void testNoTargetsInSetup() {
        System.out.println("--- no targets in SETUP → no crash ---");
        Interpreter interp = makeInterp();
        new KnotRunner(null, fixture("hello"), interp).runWithRouting(true);
        check("no exception", true);
    }

    /** Container variable refs in SETUP are collected, not executed as statements. */
    private static void testTargetVarNotExecutedAsStatement() {
        System.out.println("--- target var not executed as statement in setup ---");
        Interpreter interp = makeInterp();
        BoxInstance target = emptyBox(interp);
        interp.environment.define("b", (Token) null, target);

        new KnotRunner(null, fixture(null, "b"), interp).runWithRouting(true);

        // If "b" were executed as a statement it would produce a non-null result
        // that might land somewhere. The target body must remain untouched (no result).
        eq("target body still empty after setup", 0, target.body.size());
    }

    /** Multiple body results all go to all targets. */
    private static void testMultipleResultsToMultipleTargets() {
        System.out.println("--- multiple results routed to multiple targets ---");
        Interpreter interp = makeInterp();
        BoxInstance b = emptyBox(interp);
        BoxInstance c = emptyBox(interp);
        interp.environment.define("b", (Token) null, b);
        interp.environment.define("c", (Token) null, c);

        // Two body outputs before the knot brackets
        List<Stmt> stmts = new ArrayList<>();
        stmts.add(exprStmt(new Expr.Literal("x")));
        stmts.add(exprStmt(new Expr.Literal("y")));
        stmts.add(exprStmt(new Expr.CupOpen    (tok(TokenType.OPENBRACE,   "a{"))));
        stmts.add(exprStmt(new Expr.Variable   (tok(TokenType.IDENTIFIER,  "b"))));
        stmts.add(exprStmt(new Expr.Variable   (tok(TokenType.IDENTIFIER,  "c"))));
        stmts.add(exprStmt(new Expr.PocketOpen  (tok(TokenType.OPENPAREN,   "x("))));
        stmts.add(exprStmt(new Expr.Literal(Boolean.FALSE)));
        stmts.add(exprStmt(new Expr.CupClosed  (tok(TokenType.CLOSEDBRACE, "}a"))));
        stmts.add(exprStmt(new Expr.PocketClosed(tok(TokenType.CLOSEDPAREN, ")x"))));

        new KnotRunner(null, stmts, interp).runWithRouting(true);

        check("b contains 'x'", bodyContainsUnboxed(b.body, "x"));
        check("b contains 'y'", bodyContainsUnboxed(b.body, "y"));
        check("c contains 'x'", bodyContainsUnboxed(c.body, "x"));
        check("c contains 'y'", bodyContainsUnboxed(c.body, "y"));
    }

    /** Injection uses add semantics: target body grows by result count per call. */
    private static void testInjectionUsesAdd() {
        System.out.println("--- injection via add: body grows correctly ---");
        Interpreter interp = makeInterp();
        BoxInstance target = emptyBox(interp);
        target.body.add("existing");
        interp.environment.define("b", (Token) null, target);

        new KnotRunner(null, fixture(42, "b"), interp).runWithRouting(true);

        eq("body size is 2 (existing + injected)", 2, target.body.size());
        check("existing item preserved",  target.body.contains("existing"));
        check("injected item present",    bodyContainsUnboxed(target.body, 42));
    }

    /** CupInstance target also receives injection. */
    private static void testCupTargetReceivesResult() {
        System.out.println("--- cup target receives result ---");
        Interpreter interp = makeInterp();
        CupInstance cup = new CupInstance(null, new ArrayList<>(), null, interp);
        interp.environment.define("b", (Token) null, cup);

        new KnotRunner(null, fixture("data", "b"), interp).runWithRouting(true);

        check("cup body contains result", bodyContainsUnboxed(cup.body, "data"));
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== KnotRunner Routing Test ===\n");

        run("single target receives result",       KnotRunnerRoutingTest::testSingleTargetReceivesResult);
        run("multiple targets all receive result", KnotRunnerRoutingTest::testMultipleTargetsAllReceiveResult);
        run("no result → no routing",              KnotRunnerRoutingTest::testNoResultNoRouting);
        run("no targets → no crash",               KnotRunnerRoutingTest::testNoTargetsInSetup);
        run("target var not executed in setup",    KnotRunnerRoutingTest::testTargetVarNotExecutedAsStatement);
        run("multiple results to multiple targets",KnotRunnerRoutingTest::testMultipleResultsToMultipleTargets);
        run("injection uses add semantics",        KnotRunnerRoutingTest::testInjectionUsesAdd);
        run("cup target receives result",          KnotRunnerRoutingTest::testCupTargetReceivesResult);

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
