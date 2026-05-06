package Box.Interpreter;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Declaration.StmtDecl;
import Parser.Expr;
import Parser.Stmt;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MirrorNameValidationTest {
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

    static Token id(String s) { return new Token(TokenType.IDENTIFIER, s, s, null, null, 0,0,0,0); }
    static Token tok(TokenType t) { return new Token(t, "", null, null, null, 0,0,0,0); }
    static Token rev(String s) { return id(new StringBuilder(s).reverse().toString()); }

    // minimal slot: @sg $_ gs@
    static List<Token> oneSlot() {
        return Arrays.asList(tok(TokenType.AT), id("sg"),
                tok(TokenType.DOLLAR), tok(TokenType.UNDERSCORE),
                id("gs"), tok(TokenType.AT));
    }

    static void register(Interpreter i, Expr.UserType ut) {
        i.setForward(true);
        i.visitUserTypeExpr(ut); // call directly so RuntimeError propagates out
    }

    static boolean throws_(Runnable r) {
        try { r.run(); return false; }
        catch (RuntimeException e) { return true; }
    }

    static String messageOf(Runnable r) {
        try { r.run(); return null; }
        catch (RuntimeException e) { return e.getMessage(); }
    }

    // -------------------------------------------------------------------------

    static void testNullMirrorTypeNameThrows() {
        System.out.println("--- null mirrorTypeName throws ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), null,
                new ArrayList<>(oneSlot()), null, new ArrayList<>(), null);
        ok(throws_(() -> register(i, ut)), "null mirrorTypeName throws");
    }

    static void testWrongMirrorTypeNameThrows() {
        System.out.println("--- wrong mirror type name throws ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), null,
                new ArrayList<>(oneSlot()), id("Bar"), new ArrayList<>(), null);
        ok(throws_(() -> register(i, ut)), "wrong mirror name throws");
    }

    static void testWrongMirrorTypeNameMessage() {
        System.out.println("--- wrong mirror type name error mentions names ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), null,
                new ArrayList<>(oneSlot()), id("Bar"), new ArrayList<>(), null);
        String msg = messageOf(() -> register(i, ut));
        ok(msg != null && msg.contains("Bar"), "error mentions wrong mirror name 'Bar'");
        ok(msg != null && msg.contains("Foo"), "error mentions forward name 'Foo'");
    }

    static void testCorrectMirrorTypeName() {
        System.out.println("--- correct mirror type name accepted ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), null,
                new ArrayList<>(oneSlot()), rev("Foo"), new ArrayList<>(), null);
        ok(!throws_(() -> register(i, ut)), "correct mirror accepted");
        eq(1, i.userTypeRegistry.size(), "type registered");
    }

    static void testPalindromeTypeName() {
        System.out.println("--- palindrome type name: mirror equals forward ---");
        // "level" reversed is "level"
        Interpreter i = new Interpreter(); i.setForward(true);
        Expr.UserType ut = new Expr.UserType(id("level"), new ArrayList<>(), null,
                new ArrayList<>(oneSlot()), id("level"), new ArrayList<>(), null);
        ok(!throws_(() -> register(i, ut)), "palindrome name accepted");
    }

    static void testMissingMirrorTemplateThrows() {
        System.out.println("--- template present but mirror template missing throws ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), id("MyTemplate"),
                new ArrayList<>(oneSlot()), rev("Foo"), new ArrayList<>(), null);
        ok(throws_(() -> register(i, ut)), "missing mirror template throws");
    }

    static void testWrongMirrorTemplateThrows() {
        System.out.println("--- wrong mirror template name throws ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), id("MyTemplate"),
                new ArrayList<>(oneSlot()), rev("Foo"), new ArrayList<>(), id("Wrong"));
        ok(throws_(() -> register(i, ut)), "wrong mirror template throws");
    }

    static void testCorrectMirrorTemplate() {
        System.out.println("--- correct mirror template accepted ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), id("MyTemplate"),
                new ArrayList<>(oneSlot()), rev("Foo"), new ArrayList<>(), rev("MyTemplate"));
        ok(!throws_(() -> register(i, ut)), "correct mirror template accepted");
    }

    static void testExtraMirrorTemplateWithNoForwardThrows() {
        System.out.println("--- mirror template present but no forward template throws ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        Expr.UserType ut = new Expr.UserType(id("Foo"), new ArrayList<>(), null,
                new ArrayList<>(oneSlot()), rev("Foo"), new ArrayList<>(), rev("SomeTemplate"));
        ok(throws_(() -> register(i, ut)), "spurious mirror template throws");
    }

    static void testMirrorLinkCountMismatchThrows() {
        System.out.println("--- mirror link count mismatch throws ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        ArrayList<Token> links = new ArrayList<>(); links.add(id("Arithmetic"));
        // mirror has zero links — wrong
        Expr.UserType ut = new Expr.UserType(id("Foo"), links, null,
                new ArrayList<>(oneSlot()), rev("Foo"), new ArrayList<>(), null);
        ok(throws_(() -> register(i, ut)), "link count mismatch throws");
    }

    static void testWrongMirrorLinkNameThrows() {
        System.out.println("--- wrong mirror link name throws ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        ArrayList<Token> links = new ArrayList<>(); links.add(id("Arithmetic"));
        ArrayList<Token> mirrorLinks = new ArrayList<>(); mirrorLinks.add(id("Wrong"));
        Expr.UserType ut = new Expr.UserType(id("Foo"), links, null,
                new ArrayList<>(oneSlot()), rev("Foo"), mirrorLinks, null);
        ok(throws_(() -> register(i, ut)), "wrong mirror link name throws");
    }

    static void testCorrectMirrorLink() {
        System.out.println("--- correct mirror link accepted ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        ArrayList<Token> links = new ArrayList<>(); links.add(id("Arithmetic"));
        ArrayList<Token> mirrorLinks = new ArrayList<>(); mirrorLinks.add(rev("Arithmetic"));
        Expr.UserType ut = new Expr.UserType(id("Foo"), links, null,
                new ArrayList<>(oneSlot()), rev("Foo"), mirrorLinks, null);
        ok(!throws_(() -> register(i, ut)), "correct mirror link accepted");
    }

    static void testMultipleLinksCorrectOrder() {
        System.out.println("--- two links: mirror reversed and in reverse order ---");
        // forward: &Alpha&Beta  →  mirror: &ateB&ahplA (reversed names, reversed order)
        Interpreter i = new Interpreter(); i.setForward(true);
        ArrayList<Token> links = new ArrayList<>();
        links.add(id("Alpha")); links.add(id("Beta"));
        ArrayList<Token> mirrorLinks = new ArrayList<>();
        mirrorLinks.add(rev("Beta")); mirrorLinks.add(rev("Alpha"));
        Expr.UserType ut = new Expr.UserType(id("Foo"), links, null,
                new ArrayList<>(oneSlot()), rev("Foo"), mirrorLinks, null);
        ok(!throws_(() -> register(i, ut)), "two links in correct mirror order accepted");
    }

    static void testMultipleLinksWrongOrderThrows() {
        System.out.println("--- two links: mirror in wrong order throws ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        ArrayList<Token> links = new ArrayList<>();
        links.add(id("Alpha")); links.add(id("Beta"));
        // reversed names but same (forward) order — wrong
        ArrayList<Token> mirrorLinks = new ArrayList<>();
        mirrorLinks.add(rev("Alpha")); mirrorLinks.add(rev("Beta"));
        Expr.UserType ut = new Expr.UserType(id("Foo"), links, null,
                new ArrayList<>(oneSlot()), rev("Foo"), mirrorLinks, null);
        ok(throws_(() -> register(i, ut)), "wrong mirror link order throws");
    }

    static void testFullFormNumberFormatLong() {
        System.out.println("--- full form: NumberFormatLong&Arithmetic#PreciseNumbers ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        ArrayList<Token> links = new ArrayList<>(); links.add(id("Arithmetic"));
        ArrayList<Token> mirrorLinks = new ArrayList<>(); mirrorLinks.add(rev("Arithmetic"));
        Expr.UserType ut = new Expr.UserType(
                id("NumberFormatLong"), links, id("PreciseNumbers"),
                new ArrayList<>(oneSlot()),
                rev("NumberFormatLong"), mirrorLinks, rev("PreciseNumbers"));
        ok(!throws_(() -> register(i, ut)), "full form accepted");
        eq("NumberFormatLong", i.userTypeRegistry.get(0).typeName, "typeName stored");
        eq("gnoLtamroFrebmuN", i.userTypeRegistry.get(0).mirrorTypeName, "mirrorTypeName stored");
        eq("Arithmetic", i.userTypeRegistry.get(0).linkNames.get(0), "linkName stored");
        eq("citemhtirA", i.userTypeRegistry.get(0).mirrorLinkNames.get(0), "mirrorLinkName stored");
        eq("PreciseNumbers", i.userTypeRegistry.get(0).templateName, "templateName stored");
        eq("srebmuNesicerP", i.userTypeRegistry.get(0).mirrorTemplateName, "mirrorTemplateName stored");
    }

    static void testBareFormNoLinkNoTemplate() {
        System.out.println("--- bare form: :TypeName[...]reversedTypeName: ---");
        Interpreter i = new Interpreter(); i.setForward(true);
        Expr.UserType ut = new Expr.UserType(id("MyType"), new ArrayList<>(), null,
                new ArrayList<>(oneSlot()), rev("MyType"), new ArrayList<>(), null);
        ok(!throws_(() -> register(i, ut)), "bare form accepted");
        eq("MyType",   i.userTypeRegistry.get(0).typeName,       "typeName stored");
        eq("epyTyM",   i.userTypeRegistry.get(0).mirrorTypeName, "mirrorTypeName stored");
        ok(i.userTypeRegistry.get(0).linkNames.isEmpty(),       "no links");
        ok(i.userTypeRegistry.get(0).mirrorLinkNames.isEmpty(), "no mirror links");
        ok(i.userTypeRegistry.get(0).templateName == null,      "no template");
        ok(i.userTypeRegistry.get(0).mirrorTemplateName == null,"no mirror template");
    }

    public static void main(String[] args) {
        testNullMirrorTypeNameThrows();
        testWrongMirrorTypeNameThrows();
        testWrongMirrorTypeNameMessage();
        testCorrectMirrorTypeName();
        testPalindromeTypeName();
        testMissingMirrorTemplateThrows();
        testWrongMirrorTemplateThrows();
        testCorrectMirrorTemplate();
        testExtraMirrorTemplateWithNoForwardThrows();
        testMirrorLinkCountMismatchThrows();
        testWrongMirrorLinkNameThrows();
        testCorrectMirrorLink();
        testMultipleLinksCorrectOrder();
        testMultipleLinksWrongOrderThrows();
        testFullFormNumberFormatLong();
        testBareFormNoLinkNoTemplate();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        if (failed > 0) System.exit(1);
    }
}
