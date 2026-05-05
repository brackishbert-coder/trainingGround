package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Parser.Expr;
import Parser.Stmt;

/**
 * Pure math helper for vector and matrix operations.
 * Vectors are BoxInstances whose bodies contain only Doubles.
 * Matrices are BoxInstances whose bodies contain only BoxInstances (rows).
 * All methods are package-private; the Interpreter is the only caller.
 */
class VecMatHelper {

    // -------------------------------------------------------------------------
    // Type extraction
    // -------------------------------------------------------------------------

    static List<Double> toVec(List<Object> body, Token op) {
        List<Double> v = new ArrayList<>();
        for (Object item : body) {
            if (item instanceof Double)  { v.add((Double) item); continue; }
            if (item instanceof Integer) { v.add(((Integer) item).doubleValue()); continue; }
            if (item instanceof Long)    { v.add(((Long) item).doubleValue()); continue; }
            if (isStructuralBracket(item)) continue;
            throw new RuntimeError(op, "vector operation requires numeric elements, got: " + item);
        }
        return v;
    }

    static List<List<Double>> toMatrix(BoxInstance box, Token op) {
        List<List<Double>> m = new ArrayList<>();
        for (Object item : box.body) {
            if (item instanceof BoxInstance) { m.add(toVec(((BoxInstance) item).body, op)); continue; }
            if (isStructuralBracket(item)) continue;
            throw new RuntimeError(op, "matrix operation requires a box of boxes (rows), got: " + item);
        }
        if (m.isEmpty()) throw new RuntimeError(op, "matrix operation requires a non-empty box of boxes");
        return m;
    }

    static BoxInstance fromVec(List<Double> v, Interpreter interp) {
        List<Object> body = new ArrayList<>(v);
        return new BoxInstance(null, body, null, interp, true);
    }

    static BoxInstance fromMatrix(List<List<Double>> m, Interpreter interp) {
        List<Object> body = new ArrayList<>();
        for (List<Double> row : m) body.add(fromVec(row, interp));
        return new BoxInstance(null, body, null, interp, true);
    }

    // -------------------------------------------------------------------------
    // Shape detection
    // -------------------------------------------------------------------------

    static boolean is1D(BoxInstance box) {
        for (Object item : box.body) {
            if (isStructuralBracket(item)) continue;
            if (!(item instanceof Double || item instanceof Integer || item instanceof Long)) return false;
        }
        return true;
    }

    static boolean is2D(BoxInstance box) {
        boolean hasRow = false;
        for (Object item : box.body) {
            if (isStructuralBracket(item)) continue;
            if (!(item instanceof BoxInstance)) return false;
            hasRow = true;
        }
        return hasRow;
    }

    private static boolean isStructuralBracket(Object item) {
        if (!(item instanceof Stmt.Expression)) return false;
        Expr e = ((Stmt.Expression) item).expression;
        return e instanceof Expr.PocketOpen  || e instanceof Expr.PocketClosed
            || e instanceof Expr.CupOpen     || e instanceof Expr.CupClosed
            || e instanceof Expr.BoxOpen     || e instanceof Expr.BoxClosed;
    }

    // -------------------------------------------------------------------------
    // Vector operations
    // -------------------------------------------------------------------------

    static double dot(List<Double> a, List<Double> b, Token op) {
        if (a.size() != b.size())
            throw new RuntimeError(op, "vdot: dimension mismatch (" + a.size() + " vs " + b.size() + ")");
        double sum = 0;
        for (int i = 0; i < a.size(); i++) sum += a.get(i) * b.get(i);
        return sum;
    }

    static List<Double> cross(List<Double> a, List<Double> b, Token op) {
        if (a.size() != 3 || b.size() != 3)
            throw new RuntimeError(op, "cross: requires two 3-element vectors (got " + a.size() + " and " + b.size() + ")");
        List<Double> r = new ArrayList<>(3);
        r.add(a.get(1)*b.get(2) - a.get(2)*b.get(1));
        r.add(a.get(2)*b.get(0) - a.get(0)*b.get(2));
        r.add(a.get(0)*b.get(1) - a.get(1)*b.get(0));
        return r;
    }

    static double norm(List<Double> v) {
        double sum = 0;
        for (double x : v) sum += x * x;
        return Math.sqrt(sum);
    }

    static List<Double> unit(List<Double> v, Token op) {
        double mag = norm(v);
        if (mag < 1e-15) throw new RuntimeError(op, "unit: zero-magnitude vector");
        List<Double> r = new ArrayList<>(v.size());
        for (double x : v) r.add(x / mag);
        return r;
    }

    static List<Double> elemAdd(List<Double> a, List<Double> b, Token op) {
        if (a.size() != b.size())
            throw new RuntimeError(op, "vadd: dimension mismatch (" + a.size() + " vs " + b.size() + ")");
        List<Double> r = new ArrayList<>(a.size());
        for (int i = 0; i < a.size(); i++) r.add(a.get(i) + b.get(i));
        return r;
    }

    static List<Double> elemSub(List<Double> a, List<Double> b, Token op) {
        if (a.size() != b.size())
            throw new RuntimeError(op, "vsub: dimension mismatch (" + a.size() + " vs " + b.size() + ")");
        List<Double> r = new ArrayList<>(a.size());
        for (int i = 0; i < a.size(); i++) r.add(a.get(i) - b.get(i));
        return r;
    }

    static List<Double> scalarMul(List<Double> v, double k) {
        List<Double> r = new ArrayList<>(v.size());
        for (double x : v) r.add(x * k);
        return r;
    }

    // -------------------------------------------------------------------------
    // Matrix operations
    // -------------------------------------------------------------------------

    static List<List<Double>> matMul(List<List<Double>> A, List<List<Double>> B, Token op) {
        int rowsA = A.size(), colsA = A.get(0).size();
        int rowsB = B.size(), colsB = B.get(0).size();
        if (colsA != rowsB)
            throw new RuntimeError(op, "vdot: matrix dimension mismatch (" + colsA + " cols × " + rowsB + " rows)");
        List<List<Double>> C = new ArrayList<>(rowsA);
        for (int i = 0; i < rowsA; i++) {
            List<Double> row = new ArrayList<>(colsB);
            for (int j = 0; j < colsB; j++) {
                double s = 0;
                for (int k = 0; k < colsA; k++) s += A.get(i).get(k) * B.get(k).get(j);
                row.add(s);
            }
            C.add(row);
        }
        return C;
    }

    static List<List<Double>> transpose(List<List<Double>> m) {
        int rows = m.size(), cols = m.get(0).size();
        List<List<Double>> t = new ArrayList<>(cols);
        for (int j = 0; j < cols; j++) {
            List<Double> row = new ArrayList<>(rows);
            for (int i = 0; i < rows; i++) row.add(m.get(i).get(j));
            t.add(row);
        }
        return t;
    }

    static double det(List<List<Double>> m, Token op) {
        int n = m.size();
        for (List<Double> row : m)
            if (row.size() != n)
                throw new RuntimeError(op, "vdet: non-square matrix (" + n + " rows, " + row.size() + " cols)");
        return detRec(m, n);
    }

    static double trace(List<List<Double>> m, Token op) {
        int n = m.size();
        for (List<Double> row : m)
            if (row.size() != n)
                throw new RuntimeError(op, "trace: non-square matrix (" + n + " rows, " + row.size() + " cols)");
        double t = 0;
        for (int i = 0; i < n; i++) t += m.get(i).get(i);
        return t;
    }

    static List<List<Double>> inverse(List<List<Double>> m, Token op) {
        int n = m.size();
        for (List<Double> row : m)
            if (row.size() != n)
                throw new RuntimeError(op, "vinv: non-square matrix (" + n + " rows, " + row.size() + " cols)");
        double[][] a = new double[n][2 * n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) a[i][j] = m.get(i).get(j);
            a[i][i + n] = 1.0;
        }
        for (int col = 0; col < n; col++) {
            int maxRow = col;
            for (int r = col + 1; r < n; r++)
                if (Math.abs(a[r][col]) > Math.abs(a[maxRow][col])) maxRow = r;
            double[] tmp = a[col]; a[col] = a[maxRow]; a[maxRow] = tmp;
            if (Math.abs(a[col][col]) < 1e-12)
                throw new RuntimeError(op, "vinv: singular matrix (determinant is zero)");
            double pivot = a[col][col];
            for (int j = 0; j < 2 * n; j++) a[col][j] /= pivot;
            for (int r = 0; r < n; r++) {
                if (r == col) continue;
                double factor = a[r][col];
                for (int j = 0; j < 2 * n; j++) a[r][j] -= factor * a[col][j];
            }
        }
        List<List<Double>> result = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            List<Double> row = new ArrayList<>(n);
            for (int j = 0; j < n; j++) row.add(a[i][j + n]);
            result.add(row);
        }
        return result;
    }

    // -------------------------------------------------------------------------
    // Dispatch helpers for Mono ops (called from findMono/findOnom)
    // -------------------------------------------------------------------------

    /** Dispatch a unary vector/matrix op. Returns null if `type` is not a vec/mat op. */
    static Object dispatchVecMono(BoxInstance box, Box.Token.TokenType type, Token op,
                                  boolean invertedMode, Interpreter interp) {
        switch (type) {
        case NORM: case MRON: {
            List<Double> v = toVec(box.body, op);
            return invertedMode
                ? fromVec(unit(v, op), interp)   // puc: norm → unit
                : norm(v);
        }
        case UNIT: case TINU: {
            List<Double> v = toVec(box.body, op);
            return invertedMode
                ? norm(v)                          // puc: unit → norm
                : fromVec(unit(v, op), interp);
        }
        case TRANS: case SNART: {
            List<List<Double>> m = toMatrix(box, op);
            return fromMatrix(transpose(m), interp);  // self-inverse
        }
        case VDET: case TEDV: {
            List<List<Double>> m = toMatrix(box, op);
            return invertedMode
                ? trace(m, op)                     // puc: det → trace
                : det(m, op);
        }
        case VINV: case VNIV: {
            List<List<Double>> m = toMatrix(box, op);
            return fromMatrix(inverse(m, op), interp);  // self-inverse
        }
        case TRACE: case ECART: {
            List<List<Double>> m = toMatrix(box, op);
            return invertedMode
                ? det(m, op)                       // puc: trace → det
                : trace(m, op);
        }
        default:
            return null;  // not a vec/mat op
        }
    }

    // -------------------------------------------------------------------------
    // Dispatch helpers for Binary ops (called from visitBinaryExpr/visitYranibExpr)
    // -------------------------------------------------------------------------

    static Object dispatchVecBinary(Box.Token.TokenType type, Object rawLeft, Object rawRight,
                                    Token op, boolean invertedMode, Interpreter interp) {
        switch (type) {
        case VDOT: case TODV: {
            if (!(rawLeft instanceof BoxInstance) || !(rawRight instanceof BoxInstance))
                throw new RuntimeError(op, "vdot: both operands must be boxes");
            BoxInstance lb = (BoxInstance) rawLeft, rb = (BoxInstance) rawRight;
            if (is1D(lb) && is1D(rb)) {
                List<Double> a = toVec(lb.body, op), b = toVec(rb.body, op);
                return invertedMode
                    ? fromVec(cross(a, b, op), interp)  // puc: vdot → cross
                    : dot(a, b, op);
            }
            if (is2D(lb) && is2D(rb))
                return fromMatrix(matMul(toMatrix(lb, op), toMatrix(rb, op), op), interp);
            throw new RuntimeError(op, "vdot: operands must both be 1D vectors or both be 2D matrices");
        }
        case CROSS: case SSORC: {
            if (!(rawLeft instanceof BoxInstance) || !(rawRight instanceof BoxInstance))
                throw new RuntimeError(op, "cross: both operands must be boxes");
            BoxInstance lb = (BoxInstance) rawLeft, rb = (BoxInstance) rawRight;
            List<Double> a = toVec(lb.body, op), b = toVec(rb.body, op);
            return invertedMode
                ? dot(a, b, op)                         // puc: cross → dot product
                : fromVec(cross(a, b, op), interp);
        }
        case VADD: case DDAV: {
            if (!(rawLeft instanceof BoxInstance) || !(rawRight instanceof BoxInstance))
                throw new RuntimeError(op, "vadd: both operands must be boxes");
            BoxInstance lb = (BoxInstance) rawLeft, rb = (BoxInstance) rawRight;
            List<Double> a = toVec(lb.body, op), b = toVec(rb.body, op);
            return invertedMode
                ? fromVec(elemSub(a, b, op), interp)    // puc: vadd → vsub
                : fromVec(elemAdd(a, b, op), interp);
        }
        case VSUB: case BUSV: {
            if (!(rawLeft instanceof BoxInstance) || !(rawRight instanceof BoxInstance))
                throw new RuntimeError(op, "vsub: both operands must be boxes");
            BoxInstance lb = (BoxInstance) rawLeft, rb = (BoxInstance) rawRight;
            List<Double> a = toVec(lb.body, op), b = toVec(rb.body, op);
            return invertedMode
                ? fromVec(elemAdd(a, b, op), interp)    // puc: vsub → vadd
                : fromVec(elemSub(a, b, op), interp);
        }
        case VSCALE: case ELACSV: {
            // left=vector box, right=scalar — detect which is which
            BoxInstance vec;
            double k;
            if (rawLeft instanceof BoxInstance && isNumeric(rawRight)) {
                vec = (BoxInstance) rawLeft;
                k = toDouble(rawRight);
            } else if (rawRight instanceof BoxInstance && isNumeric(rawLeft)) {
                vec = (BoxInstance) rawRight;
                k = toDouble(rawLeft);
            } else {
                throw new RuntimeError(op, "vscale: requires a box and a scalar number");
            }
            double scale = invertedMode ? (k == 0 ? 0 : 1.0 / k) : k;  // puc: scale → 1/k
            return fromVec(scalarMul(toVec(vec.body, op), scale), interp);
        }
        default:
            return null;
        }
    }

    private static boolean isNumeric(Object v) {
        return v instanceof Double || v instanceof Integer || v instanceof Long;
    }

    private static double toDouble(Object v) {
        if (v instanceof Double)  return (Double) v;
        if (v instanceof Integer) return ((Integer) v).doubleValue();
        if (v instanceof Long)    return ((Long) v).doubleValue();
        throw new RuntimeError(null, "expected numeric value");
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private static double detRec(List<List<Double>> m, int n) {
        if (n == 1) return m.get(0).get(0);
        if (n == 2) return m.get(0).get(0)*m.get(1).get(1) - m.get(0).get(1)*m.get(1).get(0);
        double d = 0;
        for (int col = 0; col < n; col++)
            d += (col % 2 == 0 ? 1 : -1) * m.get(0).get(col) * detRec(minor(m, 0, col), n - 1);
        return d;
    }

    private static List<List<Double>> minor(List<List<Double>> m, int skipRow, int skipCol) {
        List<List<Double>> sub = new ArrayList<>();
        for (int i = 0; i < m.size(); i++) {
            if (i == skipRow) continue;
            List<Double> row = new ArrayList<>();
            for (int j = 0; j < m.get(i).size(); j++) {
                if (j == skipCol) continue;
                row.add(m.get(i).get(j));
            }
            sub.add(row);
        }
        return sub;
    }
}
