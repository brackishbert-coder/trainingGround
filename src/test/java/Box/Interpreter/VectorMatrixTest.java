package Box.Interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;

/**
 * Tests for vector and matrix operations.
 *
 * Covers:
 *   VecMatHelper unit tests: toVec, toMatrix, fromVec, fromMatrix, is1D, is2D
 *   Vector ops: dot, cross, norm, unit, elemAdd, elemSub, scalarMul
 *   Matrix ops: matMul, transpose, det, trace, inverse
 *   Interpreter Mono dispatch: NORM, UNIT, TRANS, VDET, VINV, TRACE (forward + backward)
 *   Interpreter Binary dispatch: VDOT, CROSS, VADD, VSUB, VSCALE (forward + backward)
 *   puc inversions: norm↔unit, trans self-inverse, det↔trace, vinv self-inverse, vadd↔vsub
 *   Error cases: dimension mismatch, zero-vector unit, singular inverse, non-square
 */
public class VectorMatrixTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean cond) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else       { System.out.println("  FAIL  " + name); failed++; }
    }

    private static void eq(String name, Object expected, Object actual) {
        boolean ok = (expected == null) ? (actual == null) : expected.equals(actual);
        if (ok) System.out.println("  PASS  " + name);
        else    System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual);
        if (ok) passed++; else failed++;
    }

    private static void near(String name, double expected, double actual) {
        boolean ok = Math.abs(expected - actual) < 1e-9;
        if (ok) System.out.println("  PASS  " + name);
        else    System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual);
        if (ok) passed++; else failed++;
    }

    private static Token tok(TokenType type) {
        return new Token(type, type.name().toLowerCase(), null, null, null, 0, 0, 0, 0);
    }

    private static Interpreter fwd() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    private static Interpreter bwd() {
        Interpreter i = new Interpreter();
        i.setForward(false);
        return i;
    }

    private static Interpreter fwdPuc() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        i.setInverted(true);
        return i;
    }

    private static BoxInstance vec(Interpreter interp, double... vals) {
        List<Object> body = new ArrayList<>();
        for (double v : vals) body.add(v);
        return new BoxInstance(null, body, null, interp, true);
    }

    private static BoxInstance mat(Interpreter interp, double[]... rows) {
        List<Object> body = new ArrayList<>();
        for (double[] row : rows) body.add(vec(interp, row));
        return new BoxInstance(null, body, null, interp, true);
    }

    private static List<Double> doubles(double... vals) {
        List<Double> list = new ArrayList<>();
        for (double v : vals) list.add(v);
        return list;
    }

    private static Expr monoExpr(BoxInstance box, TokenType op) {
        return new Expr.Mono(new Expr.Literal(box), tok(op));
    }

    private static Expr onomExpr(BoxInstance box, TokenType op) {
        return new Expr.Onom(new Expr.Literal(box), tok(op));
    }

    private static Expr binaryExpr(BoxInstance left, TokenType op, BoxInstance right) {
        return new Expr.Binary(new Expr.Literal(left), tok(op), new Expr.Literal(right));
    }

    private static Expr binaryExpr(BoxInstance left, TokenType op, double scalar) {
        return new Expr.Binary(new Expr.Literal(left), tok(op), new Expr.Literal(scalar));
    }

    // -------------------------------------------------------------------------
    // VecMatHelper unit tests
    // -------------------------------------------------------------------------

    private static void testToVec() {
        System.out.println("--- VecMatHelper.toVec ---");
        Interpreter i = fwd();
        BoxInstance b = vec(i, 1.0, 2.0, 3.0);
        List<Double> v = VecMatHelper.toVec(b.body, null);
        eq("size", 3, v.size());
        near("v[0]", 1.0, v.get(0));
        near("v[2]", 3.0, v.get(2));
    }

    private static void testToVecInteger() {
        System.out.println("--- VecMatHelper.toVec with Integer body ---");
        Interpreter i = fwd();
        List<Object> body = new ArrayList<>();
        body.add(Integer.valueOf(7));
        body.add(Integer.valueOf(8));
        BoxInstance b = new BoxInstance(null, body, null, i, true);
        List<Double> v = VecMatHelper.toVec(b.body, null);
        eq("size", 2, v.size());
        near("v[0]", 7.0, v.get(0));
    }

    private static void testToMatrix() {
        System.out.println("--- VecMatHelper.toMatrix ---");
        Interpreter i = fwd();
        BoxInstance m = mat(i, new double[]{1,2}, new double[]{3,4});
        List<List<Double>> rows = VecMatHelper.toMatrix(m, null);
        eq("rows", 2, rows.size());
        near("m[0][0]", 1.0, rows.get(0).get(0));
        near("m[1][1]", 4.0, rows.get(1).get(1));
    }

    private static void testIs1D2D() {
        System.out.println("--- VecMatHelper.is1D / is2D ---");
        Interpreter i = fwd();
        BoxInstance v = vec(i, 1.0, 2.0, 3.0);
        BoxInstance m = mat(i, new double[]{1,2}, new double[]{3,4});
        check("vector is1D", VecMatHelper.is1D(v));
        check("vector !is2D", !VecMatHelper.is2D(v));
        check("matrix !is1D", !VecMatHelper.is1D(m));
        check("matrix is2D", VecMatHelper.is2D(m));
    }

    private static void testFromVec() {
        System.out.println("--- VecMatHelper.fromVec ---");
        Interpreter i = fwd();
        List<Double> data = doubles(3.0, 4.0);
        BoxInstance b = VecMatHelper.fromVec(data, i);
        check("is BoxInstance", b instanceof BoxInstance);
        eq("size", 2, b.body.size());
        near("b[0]", 3.0, (Double) b.body.get(0));
    }

    private static void testFromMatrix() {
        System.out.println("--- VecMatHelper.fromMatrix ---");
        Interpreter i = fwd();
        List<List<Double>> m = new ArrayList<>();
        m.add(doubles(1.0, 2.0));
        m.add(doubles(3.0, 4.0));
        BoxInstance box = VecMatHelper.fromMatrix(m, i);
        check("outer is BoxInstance", box instanceof BoxInstance);
        eq("rows", 2, box.body.size());
        BoxInstance row0 = (BoxInstance) box.body.get(0);
        near("m[0][1]", 2.0, (Double) row0.body.get(1));
    }

    // -------------------------------------------------------------------------
    // Vector operation tests
    // -------------------------------------------------------------------------

    private static void testDotProduct() {
        System.out.println("--- VecMatHelper.dot ---");
        Token op = tok(TokenType.VDOT);
        near("dot([1,2,3],[4,5,6])", 32.0, VecMatHelper.dot(doubles(1,2,3), doubles(4,5,6), op));
        near("dot([1,0],[0,1])", 0.0, VecMatHelper.dot(doubles(1,0), doubles(0,1), op));
        near("dot([3,4],[3,4])", 25.0, VecMatHelper.dot(doubles(3,4), doubles(3,4), op));
    }

    private static void testDotDimensionMismatch() {
        System.out.println("--- dot: dimension mismatch throws ---");
        Token op = tok(TokenType.VDOT);
        try {
            VecMatHelper.dot(doubles(1,2), doubles(1,2,3), op);
            check("should have thrown", false);
        } catch (RuntimeError e) {
            check("throws RuntimeError", e.getMessage().contains("dimension mismatch"));
        }
    }

    private static void testCrossProduct() {
        System.out.println("--- VecMatHelper.cross ---");
        Token op = tok(TokenType.CROSS);
        List<Double> result = VecMatHelper.cross(doubles(1,0,0), doubles(0,1,0), op);
        near("cross x×y = z [0]", 0.0, result.get(0));
        near("cross x×y = z [1]", 0.0, result.get(1));
        near("cross x×y = z [2]", 1.0, result.get(2));
        List<Double> r2 = VecMatHelper.cross(doubles(2,3,4), doubles(5,6,7), op);
        near("cross[0]", 3*7 - 4*6, r2.get(0));
        near("cross[1]", 4*5 - 2*7, r2.get(1));
        near("cross[2]", 2*6 - 3*5, r2.get(2));
    }

    private static void testCrossWrongDimension() {
        System.out.println("--- cross: wrong dimension throws ---");
        Token op = tok(TokenType.CROSS);
        try {
            VecMatHelper.cross(doubles(1,2), doubles(3,4), op);
            check("should have thrown", false);
        } catch (RuntimeError e) {
            check("throws for 2-vectors", e.getMessage().contains("3-element"));
        }
    }

    private static void testNorm() {
        System.out.println("--- VecMatHelper.norm ---");
        near("norm([3,4])", 5.0, VecMatHelper.norm(doubles(3,4)));
        near("norm([1,0,0])", 1.0, VecMatHelper.norm(doubles(1,0,0)));
        near("norm([0,0,0])", 0.0, VecMatHelper.norm(doubles(0,0,0)));
    }

    private static void testUnit() {
        System.out.println("--- VecMatHelper.unit ---");
        Token op = tok(TokenType.UNIT);
        List<Double> u = VecMatHelper.unit(doubles(3,4), op);
        near("unit([3,4])[0]", 0.6, u.get(0));
        near("unit([3,4])[1]", 0.8, u.get(1));
        near("norm of unit", 1.0, VecMatHelper.norm(u));
    }

    private static void testUnitZeroThrows() {
        System.out.println("--- unit: zero-vector throws ---");
        Token op = tok(TokenType.UNIT);
        try {
            VecMatHelper.unit(doubles(0,0,0), op);
            check("should have thrown", false);
        } catch (RuntimeError e) {
            check("throws for zero vector", e.getMessage().contains("zero-magnitude"));
        }
    }

    private static void testElemAdd() {
        System.out.println("--- VecMatHelper.elemAdd ---");
        Token op = tok(TokenType.VADD);
        List<Double> r = VecMatHelper.elemAdd(doubles(1,2,3), doubles(4,5,6), op);
        near("vadd[0]", 5.0, r.get(0));
        near("vadd[1]", 7.0, r.get(1));
        near("vadd[2]", 9.0, r.get(2));
    }

    private static void testElemSub() {
        System.out.println("--- VecMatHelper.elemSub ---");
        Token op = tok(TokenType.VSUB);
        List<Double> r = VecMatHelper.elemSub(doubles(5,7,9), doubles(1,2,3), op);
        near("vsub[0]", 4.0, r.get(0));
        near("vsub[1]", 5.0, r.get(1));
        near("vsub[2]", 6.0, r.get(2));
    }

    private static void testScalarMul() {
        System.out.println("--- VecMatHelper.scalarMul ---");
        List<Double> r = VecMatHelper.scalarMul(doubles(1,2,3), 2.5);
        near("scale[0]", 2.5, r.get(0));
        near("scale[1]", 5.0, r.get(1));
        near("scale[2]", 7.5, r.get(2));
    }

    // -------------------------------------------------------------------------
    // Matrix operation tests
    // -------------------------------------------------------------------------

    private static void testMatMul() {
        System.out.println("--- VecMatHelper.matMul ---");
        Token op = tok(TokenType.VDOT);
        List<List<Double>> A = new ArrayList<>();
        A.add(doubles(1,2)); A.add(doubles(3,4));
        List<List<Double>> B = new ArrayList<>();
        B.add(doubles(5,6)); B.add(doubles(7,8));
        List<List<Double>> C = VecMatHelper.matMul(A, B, op);
        near("C[0][0]", 1*5+2*7, C.get(0).get(0));
        near("C[0][1]", 1*6+2*8, C.get(0).get(1));
        near("C[1][0]", 3*5+4*7, C.get(1).get(0));
        near("C[1][1]", 3*6+4*8, C.get(1).get(1));
    }

    private static void testMatMulDimensionError() {
        System.out.println("--- matMul: dimension mismatch throws ---");
        Token op = tok(TokenType.VDOT);
        List<List<Double>> A = new ArrayList<>();
        A.add(doubles(1,2,3));
        List<List<Double>> B = new ArrayList<>();
        B.add(doubles(1,2)); B.add(doubles(3,4));
        try {
            VecMatHelper.matMul(A, B, op);
            check("should have thrown", false);
        } catch (RuntimeError e) {
            check("throws for dimension mismatch", e.getMessage().contains("dimension mismatch"));
        }
    }

    private static void testTranspose() {
        System.out.println("--- VecMatHelper.transpose ---");
        List<List<Double>> m = new ArrayList<>();
        m.add(doubles(1,2,3)); m.add(doubles(4,5,6));
        List<List<Double>> t = VecMatHelper.transpose(m);
        eq("rows after transpose", 3, t.size());
        eq("cols after transpose", 2, t.get(0).size());
        near("t[0][0]", 1.0, t.get(0).get(0));
        near("t[1][0]", 2.0, t.get(1).get(0));
        near("t[2][1]", 6.0, t.get(2).get(1));
    }

    private static void testTransposeSelfInverse() {
        System.out.println("--- transpose(transpose(A)) == A ---");
        List<List<Double>> m = new ArrayList<>();
        m.add(doubles(1,2)); m.add(doubles(3,4));
        List<List<Double>> tt = VecMatHelper.transpose(VecMatHelper.transpose(m));
        near("tt[0][0]", 1.0, tt.get(0).get(0));
        near("tt[1][1]", 4.0, tt.get(1).get(1));
    }

    private static void testDet2x2() {
        System.out.println("--- VecMatHelper.det 2x2 ---");
        Token op = tok(TokenType.VDET);
        List<List<Double>> m = new ArrayList<>();
        m.add(doubles(3,8)); m.add(doubles(4,6));
        near("det([[3,8],[4,6]])", 3*6 - 8*4, VecMatHelper.det(m, op));
    }

    private static void testDet3x3() {
        System.out.println("--- VecMatHelper.det 3x3 ---");
        Token op = tok(TokenType.VDET);
        List<List<Double>> m = new ArrayList<>();
        m.add(doubles(1,2,3)); m.add(doubles(4,5,6)); m.add(doubles(7,8,9));
        near("det(singular 3x3)", 0.0, VecMatHelper.det(m, op));
        List<List<Double>> m2 = new ArrayList<>();
        m2.add(doubles(2,3,1)); m2.add(doubles(4,1,2)); m2.add(doubles(3,2,5));
        // det = 2(1*5-2*2) - 3(4*5-2*3) + 1(4*2-1*3) = 2(1) - 3(14) + 1(5) = 2-42+5 = -35
        near("det(non-singular 3x3)", -35.0, VecMatHelper.det(m2, op));
    }

    private static void testTrace() {
        System.out.println("--- VecMatHelper.trace ---");
        Token op = tok(TokenType.TRACE);
        List<List<Double>> m = new ArrayList<>();
        m.add(doubles(1,2,3)); m.add(doubles(4,5,6)); m.add(doubles(7,8,9));
        near("trace([[1,2,3],[4,5,6],[7,8,9]])", 15.0, VecMatHelper.trace(m, op));
    }

    private static void testInverse2x2() {
        System.out.println("--- VecMatHelper.inverse 2x2 ---");
        Token op = tok(TokenType.VINV);
        List<List<Double>> m = new ArrayList<>();
        m.add(doubles(4,7)); m.add(doubles(2,6));
        List<List<Double>> inv = VecMatHelper.inverse(m, op);
        // Verify A * A^-1 = I
        List<List<Double>> prod = VecMatHelper.matMul(m, inv, op);
        near("A*A^-1 [0][0]", 1.0, prod.get(0).get(0));
        near("A*A^-1 [0][1]", 0.0, prod.get(0).get(1));
        near("A*A^-1 [1][0]", 0.0, prod.get(1).get(0));
        near("A*A^-1 [1][1]", 1.0, prod.get(1).get(1));
    }

    private static void testInverseSingularThrows() {
        System.out.println("--- inverse: singular matrix throws ---");
        Token op = tok(TokenType.VINV);
        List<List<Double>> m = new ArrayList<>();
        m.add(doubles(1,2)); m.add(doubles(2,4));
        try {
            VecMatHelper.inverse(m, op);
            check("should have thrown", false);
        } catch (RuntimeError e) {
            check("throws for singular", e.getMessage().contains("singular"));
        }
    }

    // -------------------------------------------------------------------------
    // Interpreter dispatch — Mono ops
    // -------------------------------------------------------------------------

    private static void testInterpNorm() {
        System.out.println("--- interpreter NORM dispatch ---");
        Interpreter i = fwd();
        BoxInstance b = vec(i, 3.0, 4.0);
        Object result = i.evaluate(monoExpr(b, TokenType.NORM));
        check("result is Double", result instanceof Double);
        near("norm([3,4]) = 5", 5.0, (Double) result);
    }

    private static void testInterpUnit() {
        System.out.println("--- interpreter UNIT dispatch ---");
        Interpreter i = fwd();
        BoxInstance b = vec(i, 3.0, 4.0);
        Object result = i.evaluate(monoExpr(b, TokenType.UNIT));
        check("result is BoxInstance", result instanceof BoxInstance);
        BoxInstance r = (BoxInstance) result;
        near("unit[0]", 0.6, (Double) r.body.get(0));
        near("unit[1]", 0.8, (Double) r.body.get(1));
    }

    private static void testInterpTrans() {
        System.out.println("--- interpreter TRANS dispatch ---");
        Interpreter i = fwd();
        BoxInstance m = mat(i, new double[]{1,2,3}, new double[]{4,5,6});
        Object result = i.evaluate(monoExpr(m, TokenType.TRANS));
        check("result is BoxInstance", result instanceof BoxInstance);
        BoxInstance r = (BoxInstance) result;
        eq("transposed rows", 3, r.body.size());
        BoxInstance row0 = (BoxInstance) r.body.get(0);
        eq("transposed cols", 2, row0.body.size());
    }

    private static void testInterpVdet() {
        System.out.println("--- interpreter VDET dispatch ---");
        Interpreter i = fwd();
        BoxInstance m = mat(i, new double[]{3,8}, new double[]{4,6});
        Object result = i.evaluate(monoExpr(m, TokenType.VDET));
        check("result is Double", result instanceof Double);
        near("det([[3,8],[4,6]])", -14.0, (Double) result);
    }

    private static void testInterpVinv() {
        System.out.println("--- interpreter VINV dispatch ---");
        Interpreter i = fwd();
        BoxInstance m = mat(i, new double[]{4,7}, new double[]{2,6});
        Object result = i.evaluate(monoExpr(m, TokenType.VINV));
        check("result is BoxInstance", result instanceof BoxInstance);
    }

    private static void testInterpTrace() {
        System.out.println("--- interpreter TRACE dispatch ---");
        Interpreter i = fwd();
        BoxInstance m = mat(i, new double[]{1,2}, new double[]{3,4});
        Object result = i.evaluate(monoExpr(m, TokenType.TRACE));
        check("result is Double", result instanceof Double);
        near("trace([[1,2],[3,4]])", 5.0, (Double) result);
    }

    private static void testInterpBackwardMono() {
        System.out.println("--- interpreter backward mono tokens (MRON/TINU/SNART/TEDV/VNIV/ECART) ---");
        Interpreter i = bwd();
        BoxInstance v34 = vec(i, 3.0, 4.0);
        BoxInstance m22 = mat(i, new double[]{1,2}, new double[]{3,4});

        Object mron = i.evaluate(onomExpr(v34, TokenType.MRON));
        check("MRON returns Double", mron instanceof Double);
        near("MRON = norm([3,4]) = 5", 5.0, (Double) mron);

        Object tinu = i.evaluate(onomExpr(v34, TokenType.TINU));
        check("TINU returns BoxInstance", tinu instanceof BoxInstance);

        Object snart = i.evaluate(onomExpr(m22, TokenType.SNART));
        check("SNART returns BoxInstance", snart instanceof BoxInstance);

        Object tedv = i.evaluate(onomExpr(m22, TokenType.TEDV));
        check("TEDV returns Double", tedv instanceof Double);
        near("TEDV = det([[1,2],[3,4]])", -2.0, (Double) tedv);

        Object ecart = i.evaluate(onomExpr(m22, TokenType.ECART));
        check("ECART returns Double", ecart instanceof Double);
        near("ECART = trace([[1,2],[3,4]])", 5.0, (Double) ecart);
    }

    // -------------------------------------------------------------------------
    // Interpreter dispatch — Binary ops
    // -------------------------------------------------------------------------

    private static void testInterpVdotVectors() {
        System.out.println("--- interpreter VDOT on vectors ---");
        Interpreter i = fwd();
        BoxInstance a = vec(i, 1.0, 2.0, 3.0);
        BoxInstance b = vec(i, 4.0, 5.0, 6.0);
        Object result = i.evaluate(binaryExpr(a, TokenType.VDOT, b));
        check("result is Double", result instanceof Double);
        near("dot([1,2,3],[4,5,6]) = 32", 32.0, (Double) result);
    }

    private static void testInterpVdotMatrices() {
        System.out.println("--- interpreter VDOT on matrices (matmul) ---");
        Interpreter i = fwd();
        BoxInstance A = mat(i, new double[]{1,2}, new double[]{3,4});
        BoxInstance B = mat(i, new double[]{5,6}, new double[]{7,8});
        Object result = i.evaluate(binaryExpr(A, TokenType.VDOT, B));
        check("result is BoxInstance", result instanceof BoxInstance);
        BoxInstance R = (BoxInstance) result;
        BoxInstance row0 = (BoxInstance) R.body.get(0);
        near("matmul[0][0]", 19.0, (Double) row0.body.get(0));
        near("matmul[0][1]", 22.0, (Double) row0.body.get(1));
    }

    private static void testInterpCross() {
        System.out.println("--- interpreter CROSS dispatch ---");
        Interpreter i = fwd();
        BoxInstance a = vec(i, 1.0, 0.0, 0.0);
        BoxInstance b = vec(i, 0.0, 1.0, 0.0);
        Object result = i.evaluate(binaryExpr(a, TokenType.CROSS, b));
        check("cross result is BoxInstance", result instanceof BoxInstance);
        BoxInstance r = (BoxInstance) result;
        near("x×y = (0,0,1) [2]", 1.0, (Double) r.body.get(2));
    }

    private static void testInterpVadd() {
        System.out.println("--- interpreter VADD dispatch ---");
        Interpreter i = fwd();
        BoxInstance a = vec(i, 1.0, 2.0, 3.0);
        BoxInstance b = vec(i, 4.0, 5.0, 6.0);
        Object result = i.evaluate(binaryExpr(a, TokenType.VADD, b));
        check("vadd result is BoxInstance", result instanceof BoxInstance);
        BoxInstance r = (BoxInstance) result;
        near("vadd[0]", 5.0, (Double) r.body.get(0));
    }

    private static void testInterpVsub() {
        System.out.println("--- interpreter VSUB dispatch ---");
        Interpreter i = fwd();
        BoxInstance a = vec(i, 5.0, 7.0, 9.0);
        BoxInstance b = vec(i, 1.0, 2.0, 3.0);
        Object result = i.evaluate(binaryExpr(a, TokenType.VSUB, b));
        check("vsub result is BoxInstance", result instanceof BoxInstance);
        BoxInstance r = (BoxInstance) result;
        near("vsub[2]", 6.0, (Double) r.body.get(2));
    }

    private static void testInterpVscale() {
        System.out.println("--- interpreter VSCALE dispatch ---");
        Interpreter i = fwd();
        BoxInstance v = vec(i, 1.0, 2.0, 3.0);
        Object result = i.evaluate(binaryExpr(v, TokenType.VSCALE, 3.0));
        check("vscale result is BoxInstance", result instanceof BoxInstance);
        BoxInstance r = (BoxInstance) result;
        near("vscale[0]", 3.0, (Double) r.body.get(0));
        near("vscale[2]", 9.0, (Double) r.body.get(2));
    }

    private static void testInterpBackwardBinary() {
        System.out.println("--- interpreter backward binary tokens (TODV/SSORC/DDAV/BUSV/ELACSV) ---");
        // backward ops use Yranib nodes; test via Onom equivalents or direct dispatch check
        Interpreter i = bwd();
        BoxInstance a = vec(i, 1.0, 2.0, 3.0);
        BoxInstance b = vec(i, 4.0, 5.0, 6.0);

        // TODV = backward vdot
        Object todv = i.evaluate(new Expr.Yranib(new Expr.Literal(a), tok(TokenType.TODV), new Expr.Literal(b)));
        check("TODV returns Double", todv instanceof Double);
        near("TODV dot([1,2,3],[4,5,6]) = 32", 32.0, (Double) todv);

        // DDAV = backward vadd
        Object ddav = i.evaluate(new Expr.Yranib(new Expr.Literal(a), tok(TokenType.DDAV), new Expr.Literal(b)));
        check("DDAV returns BoxInstance", ddav instanceof BoxInstance);

        // BUSV = backward vsub
        BoxInstance big = vec(i, 5.0, 7.0, 9.0);
        Object busv = i.evaluate(new Expr.Yranib(new Expr.Literal(big), tok(TokenType.BUSV), new Expr.Literal(a)));
        check("BUSV returns BoxInstance", busv instanceof BoxInstance);
    }

    // -------------------------------------------------------------------------
    // puc inversions
    // -------------------------------------------------------------------------

    private static void testPucNormBecomesUnit() {
        System.out.println("--- puc: NORM → unit ---");
        Interpreter i = fwdPuc();
        BoxInstance b = vec(i, 3.0, 4.0);
        Object result = i.evaluate(monoExpr(b, TokenType.NORM));
        check("puc norm returns BoxInstance (unit)", result instanceof BoxInstance);
        BoxInstance r = (BoxInstance) result;
        near("puc-norm = unit [0]", 0.6, (Double) r.body.get(0));
    }

    private static void testPucUnitBecomesNorm() {
        System.out.println("--- puc: UNIT → norm ---");
        Interpreter i = fwdPuc();
        BoxInstance b = vec(i, 3.0, 4.0);
        Object result = i.evaluate(monoExpr(b, TokenType.UNIT));
        check("puc unit returns Double (norm)", result instanceof Double);
        near("puc-unit = norm([3,4]) = 5", 5.0, (Double) result);
    }

    private static void testPucTransSelfInverse() {
        System.out.println("--- puc: TRANS self-inverse ---");
        Interpreter i = fwdPuc();
        BoxInstance m = mat(i, new double[]{1,2}, new double[]{3,4});
        // trans is self-inverse: puc trans = trans
        Object result = i.evaluate(monoExpr(m, TokenType.TRANS));
        check("puc trans still returns BoxInstance", result instanceof BoxInstance);
    }

    private static void testPucDetBecomesTrace() {
        System.out.println("--- puc: VDET → trace ---");
        Interpreter i = fwdPuc();
        BoxInstance m = mat(i, new double[]{1,2}, new double[]{3,4});
        Object result = i.evaluate(monoExpr(m, TokenType.VDET));
        check("puc vdet returns Double (trace)", result instanceof Double);
        near("puc-vdet = trace([[1,2],[3,4]]) = 5", 5.0, (Double) result);
    }

    private static void testPucTraceBecomesdet() {
        System.out.println("--- puc: TRACE → det ---");
        Interpreter i = fwdPuc();
        BoxInstance m = mat(i, new double[]{1,2}, new double[]{3,4});
        Object result = i.evaluate(monoExpr(m, TokenType.TRACE));
        check("puc trace returns Double (det)", result instanceof Double);
        near("puc-trace = det([[1,2],[3,4]]) = -2", -2.0, (Double) result);
    }

    private static void testPucVinvSelfInverse() {
        System.out.println("--- puc: VINV self-inverse ---");
        Interpreter i = fwdPuc();
        BoxInstance m = mat(i, new double[]{4,7}, new double[]{2,6});
        Object result = i.evaluate(monoExpr(m, TokenType.VINV));
        check("puc vinv still returns BoxInstance", result instanceof BoxInstance);
    }

    private static void testPucVaddBecomesVsub() {
        System.out.println("--- puc: VADD → vsub ---");
        Interpreter i = fwdPuc();
        BoxInstance a = vec(i, 5.0, 7.0);
        BoxInstance b = vec(i, 1.0, 2.0);
        Object result = i.evaluate(binaryExpr(a, TokenType.VADD, b));
        check("puc vadd returns BoxInstance", result instanceof BoxInstance);
        BoxInstance r = (BoxInstance) result;
        near("puc vadd[0] = 5-1 = 4", 4.0, (Double) r.body.get(0));
    }

    private static void testPucVscaleReciprocal() {
        System.out.println("--- puc: VSCALE → scale by 1/k ---");
        Interpreter i = fwdPuc();
        BoxInstance v = vec(i, 4.0, 8.0);
        Object result = i.evaluate(binaryExpr(v, TokenType.VSCALE, 2.0));
        check("puc vscale returns BoxInstance", result instanceof BoxInstance);
        BoxInstance r = (BoxInstance) result;
        near("puc vscale[0] = 4*(1/2) = 2", 2.0, (Double) r.body.get(0));
    }

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        testToVec();
        testToVecInteger();
        testToMatrix();
        testIs1D2D();
        testFromVec();
        testFromMatrix();

        testDotProduct();
        testDotDimensionMismatch();
        testCrossProduct();
        testCrossWrongDimension();
        testNorm();
        testUnit();
        testUnitZeroThrows();
        testElemAdd();
        testElemSub();
        testScalarMul();

        testMatMul();
        testMatMulDimensionError();
        testTranspose();
        testTransposeSelfInverse();
        testDet2x2();
        testDet3x3();
        testTrace();
        testInverse2x2();
        testInverseSingularThrows();

        testInterpNorm();
        testInterpUnit();
        testInterpTrans();
        testInterpVdet();
        testInterpVinv();
        testInterpTrace();
        testInterpBackwardMono();

        testInterpVdotVectors();
        testInterpVdotMatrices();
        testInterpCross();
        testInterpVadd();
        testInterpVsub();
        testInterpVscale();
        testInterpBackwardBinary();

        testPucNormBecomesUnit();
        testPucUnitBecomesNorm();
        testPucTransSelfInverse();
        testPucDetBecomesTrace();
        testPucTraceBecomesdet();
        testPucVinvSelfInverse();
        testPucVaddBecomesVsub();
        testPucVscaleReciprocal();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        System.exit(failed == 0 ? 0 : 1);
    }
}
