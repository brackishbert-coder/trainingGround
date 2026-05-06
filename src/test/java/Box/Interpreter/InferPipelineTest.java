package Box.Interpreter;

import Box.Scanner.Scanner;
import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Expr;
import Parser.ParserTest;
import Parser.Stmt;
import java.util.ArrayList;
import java.util.List;

/**
 * End-to-end integration tests for the ? inference syntax through the real
 * Scanner -> ParserTest -> Interpreter pipeline.
 *
 * All tests use real PCB source strings — no manually constructed AST nodes.
 */
public class InferPipelineTest {
    static int passed = 0, failed = 0;

    static void ok(boolean cond, String name) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else       { System.out.println("  FAIL  " + name); failed++; }
    }
    static void eq(Object e, Object a, String name) {
        boolean ok = e == null ? a == null : e.equals(a);
        if (ok) { System.out.println("  PASS  " + name); passed++; }
        else    { System.out.println("  FAIL  " + name + "  expected=" + e + "  got=" + a); failed++; }
    }

    static Interpreter run(String source) {
        Scanner scanner = new Scanner(source);
        ArrayList<Token> tokens = scanner.scanTokensFirstPass();
        ParserTest parser = new ParserTest(tokens, true, false);
        List<Declaration> decls = parser.parse();
        Interpreter interp = new Interpreter();
        interp.setForward(true);
        interp.interpret(decls);
        return interp;
    }

    static final String NUMBER_FORMAT_LONG_DECL =
        ":NumberFormatLong&Arithmetic#PreciseNumbers[\n" +
        "  @sg $_^X[\"\",\"-\"] gs@,\n" +
        "  @in $$_^[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\"] ni@,\n" +
        "  @dp $\".\" pd@,\n" +
        "  @fr $$_^[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\"] rf@\n" +
        "]srebmuNesicerP#citemhtirA&gnoLtamroFrebmuN:";

    // -------------------------------------------------------------------------
    // Forward form: ?box c = 3.14

    static void testForwardInferVarDeclared() {
        System.out.println("--- forward: ?box c = 3.14 registers variable c ---");
        Interpreter i = run(NUMBER_FORMAT_LONG_DECL + "\n?box c = 3.14");
        ok(i.userTypeRegistry.size() == 1, "type registered");
        Object val = i.globals.get(new Token(TokenType.IDENTIFIER, "c", null, null, null, 0, 0, 0, 0), false);
        ok(val != null, "variable c exists");
        ok(val instanceof BoxInstance, "c is a BoxInstance");
    }

    static void testForwardInferProducesGroupedBox() {
        System.out.println("--- forward: ?box c = 3.14 produces 4-group NumberFormatLong box ---");
        Interpreter i = run(NUMBER_FORMAT_LONG_DECL + "\n?box c = 3.14");
        Object val = i.globals.get(new Token(TokenType.IDENTIFIER, "c", null, null, null, 0, 0, 0, 0), false);
        BoxInstance box = (BoxInstance) val;
        eq(4, box.body.size(), "4 slot groups");
    }

    static void testForwardInferSlotContents() {
        System.out.println("--- forward: ?box c = 3.14 slot values correct ---");
        Interpreter i = run(NUMBER_FORMAT_LONG_DECL + "\n?box c = 3.14");
        Object val = i.globals.get(new Token(TokenType.IDENTIFIER, "c", null, null, null, 0, 0, 0, 0), false);
        BoxInstance box = (BoxInstance) val;
        BoxInstance sg = (BoxInstance) box.body.get(0);
        BoxInstance in = (BoxInstance) box.body.get(1);
        BoxInstance dp = (BoxInstance) box.body.get(2);
        BoxInstance fr = (BoxInstance) box.body.get(3);
        eq("",   sg.body.get(0), "sign empty");
        eq("3",  in.body.get(0), "integer = 3");
        eq(".",  dp.body.get(0), "decimal point");
        eq("14", fr.body.get(0), "fraction = 14");
    }

    static void testForwardInferNegativeNumber() {
        System.out.println("--- forward: ?box c = -3.14 sign slot = '-' ---");
        Interpreter i = run(NUMBER_FORMAT_LONG_DECL + "\n?box c = -3.14");
        Object val = i.globals.get(new Token(TokenType.IDENTIFIER, "c", null, null, null, 0, 0, 0, 0), false);
        ok(val instanceof BoxInstance, "c is BoxInstance");
        BoxInstance box = (BoxInstance) val;
        ok(box.body.size() == 4, "4 groups");
        BoxInstance sg = (BoxInstance) box.body.get(0);
        eq("-", sg.body.get(0), "sign = '-'");
    }

    static void testForwardInferNoRegisteredTypesFallback() {
        System.out.println("--- forward: ?box c = 3.14 with no types: fallback single-item box ---");
        // No registered types: runInferenceMatcher() falls back to makeGroup(value)
        Interpreter i = run("?box c = 3.14");
        Object val = i.globals.get(new Token(TokenType.IDENTIFIER, "c", null, null, null, 0, 0, 0, 0), false);
        ok(val instanceof BoxInstance, "fallback: c is still a BoxInstance");
        BoxInstance box = (BoxInstance) val;
        eq(1, box.body.size(), "fallback: single-item box");
        eq("3.14", box.body.get(0), "fallback: raw stringified value");
    }

    static void testForwardInferIntegerOnly() {
        System.out.println("--- forward: ?box c = 42 (integer, no decimal) ---");
        // With NumberFormatLong, 42 has: sign='', int='42', no dp, no fr
        // But dp and fr are required (not optional) slots — so NumberFormatLong won't match '42'
        // If no match: c not assigned
        Interpreter i = run(NUMBER_FORMAT_LONG_DECL + "\n?box c = 42");
        // 42 has no '.', so dp slot ($".") won't match — type rejects it
        Object val = i.globals.get(new Token(TokenType.IDENTIFIER, "c", null, null, null, 0, 0, 0, 0), false);
        ok(val == null || !(val instanceof BoxInstance && ((BoxInstance)val).body.size() == 4),
                "integer-only literal does not match NumberFormatLong");
    }

    // -------------------------------------------------------------------------
    // Backward form: 3.14 = c xob?

    static void testBackwardInferVarDeclared() {
        System.out.println("--- backward: 3.14 = c xob? registers variable c ---");
        Interpreter i = run(NUMBER_FORMAT_LONG_DECL + "\n3.14 = c xob?");
        Object val = i.globals.get(new Token(TokenType.IDENTIFIER, "c", null, null, null, 0, 0, 0, 0), false);
        ok(val != null, "variable c exists");
        ok(val instanceof BoxInstance, "c is a BoxInstance");
    }

    static void testBackwardInferProducesGroupedBox() {
        System.out.println("--- backward: 3.14 = c xob? produces 4-group box ---");
        Interpreter i = run(NUMBER_FORMAT_LONG_DECL + "\n3.14 = c xob?");
        Object val = i.globals.get(new Token(TokenType.IDENTIFIER, "c", null, null, null, 0, 0, 0, 0), false);
        BoxInstance box = (BoxInstance) val;
        eq(4, box.body.size(), "4 slot groups");
    }

    static void testBackwardInferSlotContents() {
        System.out.println("--- backward: 3.14 = c xob? slot values correct (string reversed) ---");
        // "3.14" reversed = "41.3" => sign='', integer='41', dp='.', fraction='3'
        Interpreter i = run(NUMBER_FORMAT_LONG_DECL + "\n3.14 = c xob?");
        Object val = i.globals.get(new Token(TokenType.IDENTIFIER, "c", null, null, null, 0, 0, 0, 0), false);
        BoxInstance box = (BoxInstance) val;
        BoxInstance sg = (BoxInstance) box.body.get(0);
        BoxInstance in = (BoxInstance) box.body.get(1);
        BoxInstance dp = (BoxInstance) box.body.get(2);
        BoxInstance fr = (BoxInstance) box.body.get(3);
        eq("",   sg.body.get(0), "sign empty");
        eq("41", in.body.get(0), "integer = 41");
        eq(".",  dp.body.get(0), "decimal point");
        eq("3",  fr.body.get(0), "fraction = 3");
    }

    static void testBackwardInferStringReversed() {
        System.out.println("--- backward: 3.14 = c xob? string is reversed before matching ---");
        // "3.14" reversed = "41.3"
        // NumberFormatLong on "41.3": sign='', integer='41', dp='.', fraction='3'
        Interpreter i = run(NUMBER_FORMAT_LONG_DECL + "\n3.14 = c xob?");
        Object val = i.globals.get(new Token(TokenType.IDENTIFIER, "c", null, null, null, 0, 0, 0, 0), false);
        BoxInstance box = (BoxInstance) val;
        BoxInstance in = (BoxInstance) box.body.get(1);
        BoxInstance fr = (BoxInstance) box.body.get(3);
        eq("41", in.body.get(0), "reversed: integer = 41");
        eq("3",  fr.body.get(0), "reversed: fraction = 3");
    }

    // -------------------------------------------------------------------------
    // Parser whitespace tolerance

    static void testTypeDeclarationThenVarWithNewline() {
        System.out.println("--- type decl and ?box separated by newline both parse ---");
        Interpreter i = run(NUMBER_FORMAT_LONG_DECL + "\n?box c = 3.14");
        ok(i.userTypeRegistry.size() == 1, "type registered after newline-separated ?box");
    }

    static void testTypeDeclarationThenVarWithSpaces() {
        System.out.println("--- ?box with spaces around = and after type keyword ---");
        Interpreter i = run(NUMBER_FORMAT_LONG_DECL + "\n?box c = 3.14");
        Object val = i.globals.get(new Token(TokenType.IDENTIFIER, "c", null, null, null, 0, 0, 0, 0), false);
        ok(val instanceof BoxInstance, "spaces around = handled correctly");
    }

    public static void main(String[] args) {
        testForwardInferVarDeclared();
        testForwardInferProducesGroupedBox();
        testForwardInferSlotContents();
        testForwardInferNegativeNumber();
        testForwardInferNoRegisteredTypesFallback();
        testForwardInferIntegerOnly();
        testBackwardInferVarDeclared();
        testBackwardInferProducesGroupedBox();
        testBackwardInferSlotContents();
        testBackwardInferStringReversed();
        testTypeDeclarationThenVarWithNewline();
        testTypeDeclarationThenVarWithSpaces();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }
}
