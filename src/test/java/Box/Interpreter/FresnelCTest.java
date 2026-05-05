package Box.Interpreter;

import java.math.BigDecimal;

import Box.Token.Token;
import Box.Token.TokenType;
import Box.math.Interpreter.MathInterpreter;
import Parser.Expr;

/**
 * Tests for Fresnel C integral: fresnelc.(x) forward, (x).clenserf backward.
 *
 * Known values (to 4 decimal places):
 *   C(0)   = 0.0000
 *   C(0.5) ≈ 0.4923
 *   C(1)   ≈ 0.7799
 *   C(2)   ≈ 0.4883
 *   C(5)   ≈ 0.5636  (asymptotic region, x > 3)
 *   C(10)  ≈ 0.4998  (deep asymptotic)
 *   C(-1)  = -C(1)   (odd function)
 *
 * Covers:
 *   - MathInterpreter.C() direct: C(0), C(0.5), C(1), C(-1), C(5), C(10)
 *   - C differs from S for same input
 *   - Interpreter forward: Mono(Literal(x), FRESNELC) produces correct result
 *   - Interpreter backward: Onom(Literal(x), CLENSERF) produces correct result
 *   - Forward and backward return the same value for same input
 */
public class FresnelCTest {

    private static int passed = 0;
    private static int failed = 0;
    private static final double EPS = 1e-3;

    private static void check(String name, boolean cond) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else       { System.out.println("  FAIL  " + name); failed++; }
    }

    private static void near(String name, double expected, double actual, double tol) {
        boolean ok = Math.abs(expected - actual) <= tol;
        if (ok) System.out.println("  PASS  " + name + "  (" + actual + ")");
        else    System.out.println("  FAIL  " + name + "  expected≈" + expected + "  got=" + actual);
        if (ok) passed++; else failed++;
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

    // ---- MathInterpreter.C() direct -----------------------------------------

    private static void testC_zero() {
        System.out.println("--- C(0) = 0 ---");
        double result = new MathInterpreter().C(BigDecimal.ZERO).doubleValue();
        near("C(0)", 0.0, result, EPS);
    }

    private static void testC_half() {
        System.out.println("--- C(0.5) ≈ 0.4923 ---");
        double result = new MathInterpreter().C(BigDecimal.valueOf(0.5)).doubleValue();
        near("C(0.5)", 0.4923, result, EPS);
    }

    private static void testC_one() {
        System.out.println("--- C(1) ≈ 0.7799 ---");
        double result = new MathInterpreter().C(BigDecimal.ONE).doubleValue();
        near("C(1)", 0.7799, result, EPS);
    }

    private static void testC_two() {
        System.out.println("--- C(2) ≈ 0.4883 ---");
        double result = new MathInterpreter().C(BigDecimal.valueOf(2.0)).doubleValue();
        near("C(2)", 0.4883, result, EPS);
    }

    private static void testC_five_asymptotic() {
        System.out.println("--- C(5) ≈ 0.5636 (asymptotic region) ---");
        double result = new MathInterpreter().C(BigDecimal.valueOf(5.0)).doubleValue();
        near("C(5)", 0.5636, result, EPS);
    }

    private static void testC_ten_asymptotic() {
        System.out.println("--- C(10) ≈ 0.4998 (deep asymptotic) ---");
        double result = new MathInterpreter().C(BigDecimal.valueOf(10.0)).doubleValue();
        near("C(10)", 0.4998, result, 0.005);
    }

    private static void testC_negative_odd() {
        System.out.println("--- C(-x) = -C(x): odd function ---");
        MathInterpreter m = new MathInterpreter();
        double pos = m.C(BigDecimal.ONE).doubleValue();
        double neg = m.C(BigDecimal.ONE.negate()).doubleValue();
        near("C(-1) = -C(1)", -pos, neg, EPS);
    }

    private static void testC_differs_from_S() {
        System.out.println("--- C(1) != S(1): different functions ---");
        MathInterpreter m = new MathInterpreter();
        double c = m.C(BigDecimal.ONE).doubleValue();
        double s = m.S(BigDecimal.ONE).doubleValue();
        check("C(1) ≠ S(1)", Math.abs(c - s) > 0.1);
    }

    // ---- Interpreter forward: Mono(x, FRESNELC) -----------------------------

    private static void testInterpreterForwardFresnelC() {
        System.out.println("--- interpreter forward: fresnelc.(1) ≈ 0.7799 ---");
        Interpreter i = forwardInterp();
        Expr expr = new Expr.Mono(new Expr.Literal(1.0), tok(TokenType.FRESNELC, "fresnelc"));
        Object result = i.evaluate(expr);
        check("result is Double", result instanceof Double);
        near("fresnelc.(1)", 0.7799, result instanceof Double ? (Double) result : Double.NaN, EPS);
    }

    private static void testInterpreterForwardFresnelC_zero() {
        System.out.println("--- interpreter forward: fresnelc.(0) = 0 ---");
        Interpreter i = forwardInterp();
        Expr expr = new Expr.Mono(new Expr.Literal(0.0), tok(TokenType.FRESNELC, "fresnelc"));
        Object result = i.evaluate(expr);
        near("fresnelc.(0)", 0.0, result instanceof Double ? (Double) result : Double.NaN, EPS);
    }

    private static void testInterpreterForwardFresnelC_asymptotic() {
        System.out.println("--- interpreter forward: fresnelc.(5) ≈ 0.5636 ---");
        Interpreter i = forwardInterp();
        Expr expr = new Expr.Mono(new Expr.Literal(5.0), tok(TokenType.FRESNELC, "fresnelc"));
        Object result = i.evaluate(expr);
        near("fresnelc.(5)", 0.5636, result instanceof Double ? (Double) result : Double.NaN, EPS);
    }

    // ---- Interpreter backward: Onom(x, CLENSERF) ----------------------------

    private static void testInterpreterBackwardClenserf() {
        System.out.println("--- interpreter backward: (1).clenserf ≈ 0.7799 ---");
        Interpreter i = backwardInterp();
        Expr expr = new Expr.Onom(new Expr.Literal(1.0), tok(TokenType.CLENSERF, "clenserf"));
        Object result = i.evaluate(expr);
        check("result is Double", result instanceof Double);
        near("(1).clenserf", 0.7799, result instanceof Double ? (Double) result : Double.NaN, EPS);
    }

    private static void testInterpreterBackwardClenserf_zero() {
        System.out.println("--- interpreter backward: (0).clenserf = 0 ---");
        Interpreter i = backwardInterp();
        Expr expr = new Expr.Onom(new Expr.Literal(0.0), tok(TokenType.CLENSERF, "clenserf"));
        Object result = i.evaluate(expr);
        near("(0).clenserf", 0.0, result instanceof Double ? (Double) result : Double.NaN, EPS);
    }

    private static void testForwardBackwardAgree() {
        System.out.println("--- fresnelc forward and clenserf backward agree on same input ---");
        Interpreter fwd = forwardInterp();
        Interpreter bwd = backwardInterp();
        double x = 2.0;
        Expr fwdExpr = new Expr.Mono(new Expr.Literal(x), tok(TokenType.FRESNELC, "fresnelc"));
        Expr bwdExpr = new Expr.Onom(new Expr.Literal(x), tok(TokenType.CLENSERF, "clenserf"));
        double fwdResult = (Double) fwd.evaluate(fwdExpr);
        double bwdResult = (Double) bwd.evaluate(bwdExpr);
        near("forward/backward agree at x=2", fwdResult, bwdResult, EPS);
    }

    // ---- main ---------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== FresnelCTest ===\n");

        testC_zero();
        testC_half();
        testC_one();
        testC_two();
        testC_five_asymptotic();
        testC_ten_asymptotic();
        testC_negative_odd();
        testC_differs_from_S();
        testInterpreterForwardFresnelC();
        testInterpreterForwardFresnelC_zero();
        testInterpreterForwardFresnelC_asymptotic();
        testInterpreterBackwardClenserf();
        testInterpreterBackwardClenserf_zero();
        testForwardBackwardAgree();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        System.exit(failed == 0 ? 0 : 1);
    }
}
