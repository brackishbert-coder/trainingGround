package Box.Interpreter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Expr;
import Parser.Stmt;

/**
 * Tests for DYNAMICSAFTY runtime wiring.
 *
 * Two fixes applied:
 *   1. getActiveErrorSink() now returns trash when dynamicPolicy=true (was always bin)
 *   2. interpret() catch block now calls addToErrorSink(e.getMessage()) so runtime
 *      errors actually land in BIN (static) or TRASH (dynamic) at runtime
 */
public class DynamicSaftyTest {
    static int passed = 0, failed = 0;

    static void ok(boolean cond, String name) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else      { System.out.println("  FAIL  " + name); failed++; }
    }

    static Interpreter freshInterp() throws IOException {
        Path dir = Files.createTempDirectory("pcb_dyntest_");
        System.setProperty("user.dir", dir.toString());
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    static Token tok(TokenType t, String lex) {
        return new Token(t, lex, null, null, null, 0, 0, 0, 0);
    }

    /** Program that throws RuntimeError: num("bad_value"). */
    static List<Declaration> errProgram() {
        Token numTok = tok(TokenType.NUM, "num");
        Expr expr = new Expr.Mono(new Expr.Literal("bad_value"), numTok);
        Stmt.Expression stmtExpr = new Stmt.Expression(expr, expr);
        return List.of(new Declaration.StmtDecl(stmtExpr));
    }

    /** Program that succeeds: num("42"). */
    static List<Declaration> okProgram() {
        Token numTok = tok(TokenType.NUM, "num");
        Expr expr = new Expr.Mono(new Expr.Literal("42"), numTok);
        Stmt.Expression stmtExpr = new Stmt.Expression(expr, expr);
        return List.of(new Declaration.StmtDecl(stmtExpr));
    }

    static long binSize(Interpreter i) { return i.bin.body.size(); }

    static long contentItems(Object container) {
        List<Object> body = null;
        if (container instanceof PocketInstance) body = ((PocketInstance) container).body;
        else if (container instanceof TkpInstance) body = ((TkpInstance) container).body;
        if (body == null) return 0;
        return body.stream().filter(o -> !(o instanceof Stmt.Expression)).count();
    }

    public static void main(String[] args) throws IOException {

        System.out.println("--- getActiveErrorSink: default returns bin ---");
        {
            Interpreter i = freshInterp();
            ok(i.getActiveErrorSink() == i.bin, "getActiveErrorSink() == bin by default");
        }

        System.out.println("--- getActiveErrorSink: returns trash when dynamicPolicy=true ---");
        {
            Interpreter i = freshInterp();
            i.dynamicPolicy = true;
            ok(i.getActiveErrorSink() == i.trash, "getActiveErrorSink() == trash when dynamicPolicy");
        }

        System.out.println("--- getActiveErrorSink: reverts when dynamicPolicy reset ---");
        {
            Interpreter i = freshInterp();
            i.dynamicPolicy = true;
            ok(i.getActiveErrorSink() != i.bin, "dynamic: not bin");
            i.dynamicPolicy = false;
            ok(i.getActiveErrorSink() == i.bin, "static: bin again");
        }

        System.out.println("--- interpret() error routes to BIN (static policy) ---");
        {
            Interpreter i = freshInterp();
            long binBefore = binSize(i);
            long trashBefore = contentItems(i.trash);
            i.interpret(errProgram());
            ok(binSize(i) == binBefore + 1, "BIN gained exactly 1 error entry");
            ok(contentItems(i.trash) == trashBefore, "TRASH unchanged");
        }

        System.out.println("--- interpret() error routes to TRASH (dynamic policy) ---");
        {
            Interpreter i = freshInterp();
            i.dynamicPolicy = true;
            long binBefore = binSize(i);
            long trashBefore = contentItems(i.trash);
            i.interpret(errProgram());
            ok(contentItems(i.trash) == trashBefore + 1, "TRASH gained exactly 1 error entry");
            ok(binSize(i) == binBefore, "BIN unchanged");
        }

        System.out.println("--- multiple errors accumulate in sink ---");
        {
            Interpreter i = freshInterp();
            long before = binSize(i);
            i.interpret(errProgram());
            i.interpret(errProgram());
            ok(binSize(i) == before + 2, "BIN has exactly 2 new errors");
        }

        System.out.println("--- error message is stored as string ---");
        {
            Interpreter i = freshInterp();
            long before = binSize(i);
            i.interpret(errProgram());
            ok(binSize(i) == before + 1, "BIN gained 1 item");
            Object stored = i.bin.body.get((int) before); // item at [before] is the new one
            ok(stored instanceof String, "stored value is a String");
            ok(stored != null && stored.toString().contains("bad_value"), "message contains the bad value");
        }

        System.out.println("--- DYNAMICSAFTY activation via dynamicPolicy then interpret() error ---");
        {
            Interpreter i = freshInterp();
            i.dynamicPolicy = true;
            long trashBefore = contentItems(i.trash);
            long binBefore = binSize(i);
            i.interpret(errProgram());
            ok(contentItems(i.trash) == trashBefore + 1, "error in TRASH after dynamicPolicy activated");
            ok(binSize(i) == binBefore, "BIN unchanged");
        }

        System.out.println("--- no error: BIN stays unchanged ---");
        {
            Interpreter i = freshInterp();
            long before = binSize(i);
            i.interpret(okProgram());
            ok(binSize(i) == before, "BIN unchanged after successful program");
        }

        System.out.println("--- switch from static to dynamic mid-run ---");
        {
            Interpreter i = freshInterp();
            long binBefore = binSize(i);
            long trashBefore = contentItems(i.trash);
            i.interpret(errProgram());                    // static: goes to BIN
            i.dynamicPolicy = true;
            i.interpret(errProgram());                    // dynamic: goes to TRASH
            ok(binSize(i) == binBefore + 1, "BIN has 1 error (static phase)");
            ok(contentItems(i.trash) == trashBefore + 1, "TRASH has 1 error (dynamic phase)");
        }

        System.out.println("\n==================");
        System.out.println("DynamicSaftyTest: " + passed + " passed, " + failed + " failed");
        if (failed > 0) System.exit(1);
    }
}
