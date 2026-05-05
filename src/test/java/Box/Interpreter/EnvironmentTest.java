package Box.Interpreter;

import Box.Token.Token;
import Box.Token.TokenType;

/**
 * Tests for Environment: variable scoping, enclosing-chain lookup,
 * depth-indexed access, and local value enumeration.
 */
public class EnvironmentTest {

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

    private static Token idTok(String name) {
        return new Token(TokenType.IDENTIFIER, name, null, null, null, 0, 0, 0, 0);
    }

    // ---- define / get --------------------------------------------------------

    private static void testDefineAndGet() {
        System.out.println("--- define + get in same scope ---");
        Environment env = new Environment();
        env.define("x", (Token) null, 42.0);
        eq("get x", 42.0, env.get(idTok("x"), false));
    }

    private static void testGetNonexistentReturnsNull() {
        System.out.println("--- get nonexistent returns null ---");
        Environment env = new Environment();
        eq("get missing", null, env.get(idTok("missing"), false));
    }

    private static void testGetNullValue() {
        System.out.println("--- get null value returns null ---");
        Environment env = new Environment();
        env.define("n", (Token) null, null);
        eq("null value", null, env.get(idTok("n"), false));
    }

    private static void testMultipleDefines() {
        System.out.println("--- multiple defines in same scope ---");
        Environment env = new Environment();
        env.define("a", (Token) null, 1.0);
        env.define("b", (Token) null, "hello");
        env.define("c", (Token) null, true);
        eq("a", 1.0,     env.get(idTok("a"), false));
        eq("b", "hello", env.get(idTok("b"), false));
        eq("c", true,    env.get(idTok("c"), false));
    }

    private static void testRedefineOverwrites() {
        System.out.println("--- redefine overwrites previous value ---");
        Environment env = new Environment();
        env.define("x", (Token) null, 1.0);
        env.define("x", (Token) null, 2.0);
        eq("x after redefine", 2.0, env.get(idTok("x"), false));
    }

    // ---- enclosing chain lookup ---------------------------------------------

    private static void testChildFindsParentVariable() {
        System.out.println("--- child scope finds parent variable ---");
        Environment parent = new Environment();
        parent.define("p", (Token) null, "parent-val");
        Environment child = new Environment(parent);
        eq("child finds p", "parent-val", child.get(idTok("p"), false));
    }

    private static void testChildShadowsParent() {
        System.out.println("--- child shadow hides parent variable ---");
        Environment parent = new Environment();
        parent.define("x", (Token) null, "parent");
        Environment child = new Environment(parent);
        child.define("x", (Token) null, "child");
        eq("child.x",  "child",  child.get(idTok("x"), false));
        eq("parent.x", "parent", parent.get(idTok("x"), false));
    }

    private static void testGrandchildWalksChain() {
        System.out.println("--- grandchild walks full chain ---");
        Environment grandparent = new Environment();
        grandparent.define("deep", (Token) null, "grandparent-val");
        Environment parent = new Environment(grandparent);
        Environment child  = new Environment(parent);
        eq("grandchild finds deep", "grandparent-val", child.get(idTok("deep"), false));
    }

    private static void testMissingInChainReturnsNull() {
        System.out.println("--- missing in full chain returns null ---");
        Environment parent = new Environment();
        Environment child  = new Environment(parent);
        eq("missing in chain", null, child.get(idTok("absent"), false));
    }

    // ---- getAt / ancestor ---------------------------------------------------

    private static void testGetAtDepthZero() {
        System.out.println("--- getAt(0, name) reads from current scope ---");
        Environment env = new Environment();
        env.define("v", (Token) null, 99.0);
        eq("getAt 0", 99.0, env.getAt(0, "v"));
    }

    private static void testGetAtDepthOne() {
        System.out.println("--- getAt(1, name) reads from enclosing scope ---");
        Environment parent = new Environment();
        parent.define("v", (Token) null, 77.0);
        Environment child = new Environment(parent);
        child.define("local", (Token) null, "local");
        eq("getAt 1 reads parent v", 77.0, child.getAt(1, "v"));
    }

    private static void testGetAtDepthTwo() {
        System.out.println("--- getAt(2, name) reads from grandparent scope ---");
        Environment grandparent = new Environment();
        grandparent.define("v", (Token) null, "gp");
        Environment parent = new Environment(grandparent);
        Environment child  = new Environment(parent);
        eq("getAt 2 reads grandparent v", "gp", child.getAt(2, "v"));
    }

    // ---- assignAt -----------------------------------------------------------

    private static void testAssignAt() {
        System.out.println("--- assignAt updates variable at distance ---");
        Environment parent = new Environment();
        parent.define("x", (Token) null, "old");
        Environment child = new Environment(parent);
        Token xTok = idTok("x");
        Interpreter interp = new Interpreter();
        child.assignAt(1, xTok, "new", "new", interp);
        eq("parent x updated", "new", parent.get(xTok, false));
        eq("child does not shadow", "new", child.get(xTok, false));
    }

    // ---- allLocalValues -----------------------------------------------------

    private static void testAllLocalValues() {
        System.out.println("--- allLocalValues() reflects only locally defined names ---");
        Environment parent = new Environment();
        parent.define("inherited", (Token) null, "p");
        Environment child = new Environment(parent);
        child.define("local1", (Token) null, 1.0);
        child.define("local2", (Token) null, 2.0);
        java.util.Collection<Object> local = child.allLocalValues();
        check("2 local values", local.size() == 2);
        check("local1 present", local.contains(1.0));
        check("local2 present", local.contains(2.0));
        check("inherited not in local", !local.contains("p"));
    }

    // ---- enclosing field ----------------------------------------------------

    private static void testEnclosingIsNull() {
        System.out.println("--- root environment has null enclosing ---");
        Environment env = new Environment();
        check("enclosing is null", env.enclosing == null);
    }

    private static void testEnclosingIsSet() {
        System.out.println("--- child environment enclosing points to parent ---");
        Environment parent = new Environment();
        Environment child  = new Environment(parent);
        check("child.enclosing == parent", child.enclosing == parent);
    }

    // ---- Entry point --------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== EnvironmentTest ===\n");

        run("define and get",              EnvironmentTest::testDefineAndGet);
        run("get nonexistent = null",      EnvironmentTest::testGetNonexistentReturnsNull);
        run("get null value",              EnvironmentTest::testGetNullValue);
        run("multiple defines",            EnvironmentTest::testMultipleDefines);
        run("redefine overwrites",         EnvironmentTest::testRedefineOverwrites);
        run("child finds parent var",      EnvironmentTest::testChildFindsParentVariable);
        run("child shadows parent",        EnvironmentTest::testChildShadowsParent);
        run("grandchild walks chain",      EnvironmentTest::testGrandchildWalksChain);
        run("missing in chain = null",     EnvironmentTest::testMissingInChainReturnsNull);
        run("getAt depth 0",               EnvironmentTest::testGetAtDepthZero);
        run("getAt depth 1",               EnvironmentTest::testGetAtDepthOne);
        run("getAt depth 2",               EnvironmentTest::testGetAtDepthTwo);
        run("assignAt updates distance",   EnvironmentTest::testAssignAt);
        run("allLocalValues local only",   EnvironmentTest::testAllLocalValues);
        run("root enclosing is null",      EnvironmentTest::testEnclosingIsNull);
        run("child enclosing is parent",   EnvironmentTest::testEnclosingIsSet);

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
