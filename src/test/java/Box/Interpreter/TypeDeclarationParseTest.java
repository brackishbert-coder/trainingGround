package Box.Interpreter;

import Box.Scanner.Scanner;
import Box.Token.Token;
import Parser.Declaration;
import Parser.ParserTest;
import java.util.ArrayList;
import java.util.List;

/**
 * End-to-end integration tests: real PCB source text → Scanner → ParserTest → Interpreter.
 * These are the first tests that exercise the full pipeline for type declarations.
 */
public class TypeDeclarationParseTest {
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

    static boolean throws_(Runnable r) {
        try { r.run(); return false; }
        catch (RuntimeException e) { return true; }
    }

    // -------------------------------------------------------------------------

    static void testBareTypeRegisters() {
        System.out.println("--- bare form :TypeName[slot]NameepyT: registers ---");
        // :Foo[@sg $_ gs@]ooF:
        String src = ":Foo[@sg $_ gs@]ooF:";
        Interpreter i = run(src);
        ok(i.userTypeRegistry.size() == 1, "registry has one entry");
        eq("Foo", i.userTypeRegistry.get(0).typeName, "typeName = Foo");
        eq("ooF", i.userTypeRegistry.get(0).mirrorTypeName, "mirrorTypeName = ooF");
        ok(i.userTypeRegistry.get(0).slots.size() == 1, "one slot");
        eq("sg", i.userTypeRegistry.get(0).slots.get(0).category, "slot category = sg");
    }

    static void testBareTypeMultilineSlots() {
        System.out.println("--- bare form with multiline slot body ---");
        String src = ":Foo[\n  @sg $_ gs@\n]ooF:";
        Interpreter i = run(src);
        ok(i.userTypeRegistry.size() == 1, "registry has one entry");
        eq("Foo", i.userTypeRegistry.get(0).typeName, "typeName = Foo");
        eq("ooF", i.userTypeRegistry.get(0).mirrorTypeName, "mirrorTypeName = ooF");
    }

    static void testWithLink() {
        System.out.println("--- :TypeName&Link[slot]kniL&NameepyT: ---");
        String src = ":MyType&Arithmetic[@sg $_ gs@]citemhtirA&epyTyM:";
        Interpreter i = run(src);
        ok(i.userTypeRegistry.size() == 1, "registry has one entry");
        eq("MyType", i.userTypeRegistry.get(0).typeName, "typeName");
        eq("epyTyM", i.userTypeRegistry.get(0).mirrorTypeName, "mirrorTypeName");
        ok(i.userTypeRegistry.get(0).linkNames.size() == 1, "one link");
        eq("Arithmetic", i.userTypeRegistry.get(0).linkNames.get(0), "linkName");
        ok(i.userTypeRegistry.get(0).mirrorLinkNames.size() == 1, "one mirror link");
        eq("citemhtirA", i.userTypeRegistry.get(0).mirrorLinkNames.get(0), "mirrorLinkName");
    }

    static void testWithTemplate() {
        System.out.println("--- :TypeName#Template[slot]etalpmT#NameepyT: ---");
        String src = ":MyType#MyTmpl[@sg $_ gs@]lpmTyM#epyTyM:";
        Interpreter i = run(src);
        ok(i.userTypeRegistry.size() == 1, "registered");
        eq("MyType", i.userTypeRegistry.get(0).typeName, "typeName");
        eq("MyTmpl", i.userTypeRegistry.get(0).templateName, "templateName");
        eq("lpmTyM", i.userTypeRegistry.get(0).mirrorTemplateName, "mirrorTemplateName");
        eq("epyTyM", i.userTypeRegistry.get(0).mirrorTypeName, "mirrorTypeName");
    }

    static void testFullFormNumberFormatLong() {
        System.out.println("--- full form: NumberFormatLong&Arithmetic#PreciseNumbers ---");
        String src =
            ":NumberFormatLong&Arithmetic#PreciseNumbers[\n" +
            "  @sg $_^X[\"\",\"-\"] gs@,\n" +
            "  @in $$_^[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\"] ni@,\n" +
            "  @dp $\".\" pd@,\n" +
            "  @fr $$_^[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\"] rf@\n" +
            "]srebmuNesicerP#citemhtirA&gnoLtamroFrebmuN:";
        Interpreter i = run(src);
        ok(i.userTypeRegistry.size() == 1, "one type registered");
        Interpreter.UserTypeEntry e = i.userTypeRegistry.get(0);
        eq("NumberFormatLong",   e.typeName,           "typeName");
        eq("gnoLtamroFrebmuN",   e.mirrorTypeName,     "mirrorTypeName");
        eq("Arithmetic",         e.linkNames.get(0),   "linkName");
        eq("citemhtirA",         e.mirrorLinkNames.get(0), "mirrorLinkName");
        eq("PreciseNumbers",     e.templateName,       "templateName");
        eq("srebmuNesicerP",     e.mirrorTemplateName, "mirrorTemplateName");
        ok(e.slots.size() == 4, "four slots");
        eq("sg", e.slots.get(0).category, "slot0 category = sg");
        eq("in", e.slots.get(1).category, "slot1 category = in");
        eq("dp", e.slots.get(2).category, "slot2 category = dp");
        eq("fr", e.slots.get(3).category, "slot3 category = fr");
    }

    static void testInferenceAfterTypeDeclaration() {
        System.out.println("--- type declaration followed by inference produces grouped box ---");
        // declare NumberFormatLong then infer 3.14
        String src =
            ":NumberFormatLong&Arithmetic#PreciseNumbers[\n" +
            "  @sg $_^X[\"\",\"-\"] gs@,\n" +
            "  @in $$_^[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\"] ni@,\n" +
            "  @dp $\".\" pd@,\n" +
            "  @fr $$_^[\"0\",\"1\",\"2\",\"3\",\"4\",\"5\",\"6\",\"7\",\"8\",\"9\"] rf@\n" +
            "]srebmuNesicerP#citemhtirA&gnoLtamroFrebmuN:";
        Interpreter i = run(src);
        ok(i.userTypeRegistry.size() == 1, "type registered before inference");

        // now run inference directly (? infer syntax not yet wired, use visitInferExpr)
        Parser.Expr.Infer infer = new Parser.Expr.Infer(new Parser.Expr.Literal("3.14"));
        BoxInstance result = (BoxInstance) i.visitInferExpr(infer);
        ok(result != null, "inference result not null");
        ok(result.body.size() == 4, "matched as NumberFormatLong: 4 groups");
        BoxInstance sg = (BoxInstance) result.body.get(0);
        BoxInstance in = (BoxInstance) result.body.get(1);
        BoxInstance dp = (BoxInstance) result.body.get(2);
        BoxInstance fr = (BoxInstance) result.body.get(3);
        eq("",   sg.body.get(0), "sign = ''");
        eq("3",  in.body.get(0), "integer = '3'");
        eq(".",  dp.body.get(0), "dp = '.'");
        eq("14", fr.body.get(0), "fraction = '14'");
    }

    static void testTwoTypesDeclarationOrder() {
        System.out.println("--- two type declarations preserve declaration order ---");
        // no whitespace between declarations — top-level newlines are not skipped by the parser
        String src = ":TypeA[@sg $_ gs@]AepyT::TypeB[@in $_ ni@]BepyT:";
        Interpreter i = run(src);
        ok(i.userTypeRegistry.size() == 2, "two types");
        eq("TypeA", i.userTypeRegistry.get(0).typeName, "first = TypeA");
        eq("TypeB", i.userTypeRegistry.get(1).typeName, "second = TypeB");
    }

    static void testWrongMirrorFailsAtRuntime() {
        System.out.println("--- wrong mirror type name: type not registered ---");
        // mirror says 'Wrong' but should be 'ooF' — interpret() catches RuntimeError internally
        Interpreter i = run(":Foo[@sg $_ gs@]Wrong:");
        ok(i.userTypeRegistry.isEmpty(), "type not registered when mirror name is wrong");
    }

    public static void main(String[] args) {
        testBareTypeRegisters();
        testBareTypeMultilineSlots();
        testWithLink();
        testWithTemplate();
        testFullFormNumberFormatLong();
        testInferenceAfterTypeDeclaration();
        testTwoTypesDeclarationOrder();
        testWrongMirrorFailsAtRuntime();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }
}
