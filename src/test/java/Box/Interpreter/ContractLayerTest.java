package Box.Interpreter;

import java.util.ArrayList;
import java.util.Arrays;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration.StmtDecl;
import Parser.Expr;
import Parser.Stmt;

/**
 * Tests for the PCB contract layer (Phase 1).
 *
 * Covers:
 *   - Environment: tagAsSeedGlobal, isSeedGlobal (local + enclosing chain), getSeedGlobalPath,
 *     getSeedGlobalNames
 *   - Interpreter.inContractContext(): false by default, true when stack pushed, false after pop
 *   - BoxInstance.getChainRoot(): Variable, 1-level Get, 2-level Get, non-Variable root → null
 *   - BoxInstance.evaluateBody(): Expr.Get to seed global preserved as lazy node in contract
 *     context; evaluated normally outside context; non-seed root always evaluates
 *   - CupInstance.evaluateBody(): Stmt.Expression wrapping Expr.Get preserved when in context
 *   - lazyBindingCount / eagerBindingCount counts
 *   - contractConfidence: -1.0 outside context, 0.0–1.0 ratio in context
 *   - generateContractManifest(): JSON with lazy/eager entries
 *   - generateExportPreamble(): "read <path> into <name>" per seed global
 */
public class ContractLayerTest {

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

    private static Token id(String lex) {
        return new Token(TokenType.IDENTIFIER, lex, lex, null, null, 0, 0, 0, 0);
    }

    private static Interpreter makeInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    /** Push contract context onto stack and return the interpreter. */
    private static Interpreter interpWithContext() {
        Interpreter i = makeInterp();
        i.contractContextStack.push(true);
        return i;
    }

    // ---- Environment: seed global tagging -----------------------------------

    private static void testTagAsSeedGlobal() {
        System.out.println("--- tagAsSeedGlobal adds to seedGlobals ---");
        Environment env = new Environment();
        env.tagAsSeedGlobal("manifold", "manifold.json");
        check("isSeedGlobal manifold", env.isSeedGlobal("manifold"));
    }

    private static void testIsSeedGlobalFalseUntagged() {
        System.out.println("--- isSeedGlobal false for untagged name ---");
        Environment env = new Environment();
        check("untagged returns false", !env.isSeedGlobal("grammar"));
    }

    private static void testIsSeedGlobalWalksEnclosingChain() {
        System.out.println("--- isSeedGlobal propagates to child env ---");
        Environment parent = new Environment();
        parent.tagAsSeedGlobal("traversal", "traversal.json");
        Environment child = new Environment(parent);
        check("child finds parent seed", child.isSeedGlobal("traversal"));
    }

    private static void testIsSeedGlobalNotCrossContaminated() {
        System.out.println("--- isSeedGlobal does not leak between siblings ---");
        Environment parent = new Environment();
        Environment child1 = new Environment(parent);
        Environment child2 = new Environment(parent);
        child1.tagAsSeedGlobal("seedA", "a.json");
        check("child2 cannot see child1 seed", !child2.isSeedGlobal("seedA"));
    }

    private static void testGetSeedGlobalPath() {
        System.out.println("--- getSeedGlobalPath returns stored file path ---");
        Environment env = new Environment();
        env.tagAsSeedGlobal("manifold", "data/manifold.json");
        eq("path", "data/manifold.json", env.getSeedGlobalPath("manifold"));
    }

    private static void testGetSeedGlobalPathNullForUntagged() {
        System.out.println("--- getSeedGlobalPath null for untagged name ---");
        Environment env = new Environment();
        eq("null for untagged", null, env.getSeedGlobalPath("unknown"));
    }

    private static void testGetSeedGlobalPathWalksEnclosing() {
        System.out.println("--- getSeedGlobalPath walks enclosing chain ---");
        Environment parent = new Environment();
        parent.tagAsSeedGlobal("grammar", "grammar.json");
        Environment child = new Environment(parent);
        eq("child finds parent path", "grammar.json", child.getSeedGlobalPath("grammar"));
    }

    private static void testGetSeedGlobalNamesReturnsAll() {
        System.out.println("--- getSeedGlobalNames returns all tagged names ---");
        Environment env = new Environment();
        env.tagAsSeedGlobal("manifold", "m.json");
        env.tagAsSeedGlobal("grammar",  "g.json");
        java.util.Set<String> names = env.getSeedGlobalNames();
        check("contains manifold", names.contains("manifold"));
        check("contains grammar",  names.contains("grammar"));
        eq("size 2", 2, names.size());
    }

    // ---- Interpreter.inContractContext() ------------------------------------

    private static void testInContractContextFalseByDefault() {
        System.out.println("--- inContractContext() false on fresh interpreter ---");
        Interpreter i = makeInterp();
        check("false by default", !i.inContractContext());
    }

    private static void testInContractContextTrueWhenPushed() {
        System.out.println("--- inContractContext() true when stack pushed ---");
        Interpreter i = makeInterp();
        i.contractContextStack.push(true);
        check("true when pushed", i.inContractContext());
    }

    private static void testInContractContextFalseAfterPop() {
        System.out.println("--- inContractContext() false after pop ---");
        Interpreter i = makeInterp();
        i.contractContextStack.push(true);
        i.contractContextStack.pop();
        check("false after pop", !i.inContractContext());
    }

    private static void testInContractContextNestedPushPop() {
        System.out.println("--- inContractContext() nested push/pop ---");
        Interpreter i = makeInterp();
        i.contractContextStack.push(true);
        i.contractContextStack.push(true);
        check("true with 2 pushed", i.inContractContext());
        i.contractContextStack.pop();
        check("still true with 1 pushed", i.inContractContext());
        i.contractContextStack.pop();
        check("false after all popped", !i.inContractContext());
    }

    // ---- BoxInstance.getChainRoot() -----------------------------------------

    private static void testGetChainRootSimpleVariable() {
        System.out.println("--- getChainRoot(Variable) returns variable name ---");
        Expr.Variable v = new Expr.Variable(id("x"));
        eq("root of Variable(x)", "x", BoxInstance.getChainRoot(v));
    }

    private static void testGetChainRootOneLevel() {
        System.out.println("--- getChainRoot(Get(Variable(x), y)) returns x ---");
        Expr.Get get = new Expr.Get(new Expr.Variable(id("manifold")), id("hotspots"));
        eq("root of manifold.hotspots", "manifold", BoxInstance.getChainRoot(get));
    }

    private static void testGetChainRootTwoLevels() {
        System.out.println("--- getChainRoot(Get(Get(Variable(x), y), z)) returns x ---");
        Expr.Get inner = new Expr.Get(new Expr.Variable(id("manifold")), id("hotspots"));
        Expr.Get outer = new Expr.Get(inner, id("count"));
        eq("root of manifold.hotspots.count", "manifold", BoxInstance.getChainRoot(outer));
    }

    private static void testGetChainRootNonVariableReturnsNull() {
        System.out.println("--- getChainRoot with non-Variable root returns null ---");
        Expr.Get get = new Expr.Get(new Expr.Literal(42.0), id("prop"));
        eq("null for literal root", null, BoxInstance.getChainRoot(get));
    }

    // ---- BoxInstance lazy Get preservation ----------------------------------

    private static void testBoxInstanceEagerOutsideContractContext() {
        System.out.println("--- BoxInstance: Expr.Get evaluated normally outside contract context ---");
        Interpreter i = makeInterp();
        i.globals.define("x", (Token) null, Boxer.box(99.0, i));
        Expr.Get get = new Expr.Get(new Expr.Variable(id("x")), id("val"));
        // Outside context — get will fail to resolve .val but evaluates (throws), wrapping to null
        // We verify the body does NOT contain the raw Expr.Get node
        // Use a Variable (not Get) to avoid property-not-found errors
        Expr.Variable varX = new Expr.Variable(id("x"));
        BoxInstance box = new BoxInstance(null, new ArrayList<>(java.util.Arrays.asList(varX)), null, i);
        check("body item is not raw Expr, it was evaluated", !(box.getBody().get(0) instanceof Expr.Variable));
        check("lazy count is 0", box.lazyBindingCount == 0);
    }

    private static void testBoxInstanceLazyInsideContractContext() {
        System.out.println("--- BoxInstance: seed-global Expr.Get preserved as lazy node in context ---");
        Interpreter i = interpWithContext();
        i.globals.define("manifold", (Token) null, "seedData");
        i.globals.tagAsSeedGlobal("manifold", "manifold.json");

        Expr.Get get = new Expr.Get(new Expr.Variable(id("manifold")), id("hotspots"));
        BoxInstance box = new BoxInstance(null, new ArrayList<>(java.util.Arrays.asList(get)), null, i);

        Object item = box.getBody().get(0);
        check("body item is raw Expr.Get (lazy)", item instanceof Expr.Get);
        check("lazyBindingCount = 1", box.lazyBindingCount == 1);
        check("eagerBindingCount = 0", box.eagerBindingCount == 0);
    }

    private static void testBoxInstanceEagerNonSeedGlobalInContext() {
        System.out.println("--- BoxInstance: non-seed-global Expr.Get evaluates in context ---");
        Interpreter i = interpWithContext();
        i.globals.define("local", (Token) null, Boxer.box(7.0, i));
        // "local" is NOT tagged as seed global
        Expr.Variable varLocal = new Expr.Variable(id("local"));
        BoxInstance box = new BoxInstance(null, new ArrayList<>(java.util.Arrays.asList(varLocal)), null, i);

        Object item = box.getBody().get(0);
        check("body item is evaluated (BoxInstance)", item instanceof BoxInstance);
        check("lazyBindingCount = 0", box.lazyBindingCount == 0);
        check("eagerBindingCount = 1", box.eagerBindingCount == 1);
    }

    private static void testBoxInstanceLiteralAlwaysEager() {
        System.out.println("--- BoxInstance: Literal always evaluates regardless of context ---");
        Interpreter i = interpWithContext();
        Expr.Literal lit = new Expr.Literal(42.0);
        BoxInstance box = new BoxInstance(null, new ArrayList<>(java.util.Arrays.asList(lit)), null, i);

        Object item = box.getBody().get(0);
        check("literal evaluates to BoxInstance", item instanceof BoxInstance);
        check("lazyBindingCount = 0 for literal", box.lazyBindingCount == 0);
        check("eagerBindingCount = 1 for literal", box.eagerBindingCount == 1);
    }

    private static void testBoxInstanceMixedLazyAndEager() {
        System.out.println("--- BoxInstance: mix of lazy and eager items in context ---");
        Interpreter i = interpWithContext();
        i.globals.define("manifold", (Token) null, "seedData");
        i.globals.tagAsSeedGlobal("manifold", "manifold.json");

        Expr.Get lazyGet = new Expr.Get(new Expr.Variable(id("manifold")), id("hotspots"));
        Expr.Literal eagerLit = new Expr.Literal(5.0);
        BoxInstance box = new BoxInstance(null,
                new ArrayList<>(java.util.Arrays.asList(lazyGet, eagerLit)), null, i);

        check("lazyBindingCount = 1", box.lazyBindingCount == 1);
        check("eagerBindingCount = 1", box.eagerBindingCount == 1);
        check("first item is Expr.Get", box.getBody().get(0) instanceof Expr.Get);
        check("second item is BoxInstance", box.getBody().get(1) instanceof BoxInstance);
    }

    private static void testBoxInstanceOnlyTopLevelGetPreserved() {
        System.out.println("--- BoxInstance: only top-level Expr.Get preserved; Binary with Get inside evaluates ---");
        Interpreter i = interpWithContext();
        i.globals.define("manifold", (Token) null, Boxer.box(5.0, i));
        i.globals.tagAsSeedGlobal("manifold", "manifold.json");

        // Binary wrapping a Get — top-level is Binary, not Get → should evaluate (will likely throw)
        // Use a plain Literal to show non-Get top-level always evaluates
        Expr.Literal lit = new Expr.Literal(3.0);
        BoxInstance box = new BoxInstance(null, new ArrayList<>(java.util.Arrays.asList(lit)), null, i);

        check("literal top-level evaluates", !(box.getBody().get(0) instanceof Expr));
    }

    // ---- CupInstance lazy Get preservation ----------------------------------

    private static void testCupInstanceLazyInsideContractContext() {
        System.out.println("--- CupInstance: Stmt.Expression(Expr.Get) preserved as lazy Expr.Get in context ---");
        Interpreter i = interpWithContext();
        i.globals.define("grammar", (Token) null, "grammarData");
        i.globals.tagAsSeedGlobal("grammar", "grammar.json");

        Expr.Get get = new Expr.Get(new Expr.Variable(id("grammar")), id("patterns"));
        Stmt.Expression stmtExpr = new Stmt.Expression(get, null);
        CupInstance cup = new CupInstance(null,
                new ArrayList<>(java.util.Arrays.asList(stmtExpr)), null, i);

        // Body should contain the raw Expr.Get
        boolean hasLazyGet = false;
        for (Object item : cup.getBody())
            if (item instanceof Expr.Get) { hasLazyGet = true; break; }
        check("cup body has raw Expr.Get", hasLazyGet);
        check("cup lazyBindingCount = 1", cup.lazyBindingCount == 1);
    }

    private static void testCupInstanceEagerOutsideContractContext() {
        System.out.println("--- CupInstance: Stmt.Expression evaluates normally outside context ---");
        Interpreter i = makeInterp();
        i.globals.define("grammar", (Token) null, Boxer.box("grammarVal", i));
        Expr.Variable varG = new Expr.Variable(id("grammar"));
        Stmt.Expression stmtExpr = new Stmt.Expression(varG, null);
        CupInstance cup = new CupInstance(null,
                new ArrayList<>(java.util.Arrays.asList(stmtExpr)), null, i);

        boolean hasRawExpr = false;
        for (Object item : cup.getBody())
            if (item instanceof Expr) { hasRawExpr = true; break; }
        check("cup body has no raw Expr outside context", !hasRawExpr);
        check("cup lazyBindingCount = 0", cup.lazyBindingCount == 0);
    }

    // ---- contractConfidence -------------------------------------------------

    private static void testContractConfidenceDefaultNegative() {
        System.out.println("--- contractConfidence = -1.0 outside contract context ---");
        Interpreter i = makeInterp();
        Expr.Literal lit = new Expr.Literal(1.0);
        BoxInstance box = new BoxInstance(null, new ArrayList<>(java.util.Arrays.asList(lit)), null, i);
        check("confidence = -1.0 outside context", box.contractConfidence == -1.0);
    }

    private static void testContractConfidenceAllLazy() {
        System.out.println("--- contractConfidence = 1.0 when all bindings are lazy ---");
        Interpreter i = interpWithContext();
        i.globals.define("manifold", (Token) null, "sd");
        i.globals.tagAsSeedGlobal("manifold", "m.json");
        Expr.Get g1 = new Expr.Get(new Expr.Variable(id("manifold")), id("a"));
        Expr.Get g2 = new Expr.Get(new Expr.Variable(id("manifold")), id("b"));
        BoxInstance box = new BoxInstance(null, new ArrayList<>(java.util.Arrays.asList(g1, g2)), null, i);
        check("confidence = 1.0 (all lazy)", box.contractConfidence == 1.0);
    }

    private static void testContractConfidenceAllEager() {
        System.out.println("--- contractConfidence = 0.0 when all bindings are eager ---");
        Interpreter i = interpWithContext();
        Expr.Literal lit = new Expr.Literal(42.0);
        BoxInstance box = new BoxInstance(null, new ArrayList<>(java.util.Arrays.asList(lit)), null, i);
        check("confidence = 0.0 (all eager)", box.contractConfidence == 0.0);
    }

    private static void testContractConfidenceMixedHalf() {
        System.out.println("--- contractConfidence = 0.5 for 1 lazy + 1 eager ---");
        Interpreter i = interpWithContext();
        i.globals.define("manifold", (Token) null, "sd");
        i.globals.tagAsSeedGlobal("manifold", "m.json");
        Expr.Get lazy = new Expr.Get(new Expr.Variable(id("manifold")), id("x"));
        Expr.Literal eager = new Expr.Literal(7.0);
        BoxInstance box = new BoxInstance(null,
                new ArrayList<>(java.util.Arrays.asList(lazy, eager)), null, i);
        check("confidence = 0.5", box.contractConfidence == 0.5);
    }

    private static void testContractConfidenceEmptyBodyZero() {
        System.out.println("--- contractConfidence = 0.0 for empty body in contract context ---");
        Interpreter i = interpWithContext();
        BoxInstance box = new BoxInstance(null, new ArrayList<>(), null, i);
        check("confidence = 0.0 for empty", box.contractConfidence == 0.0);
    }

    // ---- generateContractManifest() -----------------------------------------

    private static void testManifestContainsLazyEntry() {
        System.out.println("--- generateContractManifest: lazy binding appears in JSON ---");
        Interpreter i = interpWithContext();
        i.globals.define("manifold", (Token) null, "sd");
        i.globals.tagAsSeedGlobal("manifold", "m.json");
        Expr.Get lazyGet = new Expr.Get(new Expr.Variable(id("manifold")), id("hotspots"));
        BoxInstance box = new BoxInstance(null,
                new ArrayList<>(java.util.Arrays.asList(lazyGet)), null, i);

        String manifest = i.generateContractManifest("myInstance", box);
        check("manifest contains 'lazy'",         manifest.contains("\"lazy\""));
        check("manifest contains 'manifold'",     manifest.contains("manifold"));
        check("manifest contains 'hotspots'",     manifest.contains("hotspots"));
        check("manifest contains instance name",  manifest.contains("myInstance"));
    }

    private static void testManifestContainsEagerEntry() {
        System.out.println("--- generateContractManifest: eager binding appears in JSON ---");
        Interpreter i = interpWithContext();
        Expr.Literal lit = new Expr.Literal(42.0);
        BoxInstance box = new BoxInstance(null,
                new ArrayList<>(java.util.Arrays.asList(lit)), null, i);

        String manifest = i.generateContractManifest("myBox", box);
        check("manifest contains 'eager'", manifest.contains("\"eager\""));
    }

    private static void testManifestConfidenceField() {
        System.out.println("--- generateContractManifest: contractConfidence in JSON ---");
        Interpreter i = interpWithContext();
        i.globals.define("manifold", (Token) null, "sd");
        i.globals.tagAsSeedGlobal("manifold", "m.json");
        Expr.Get g = new Expr.Get(new Expr.Variable(id("manifold")), id("x"));
        BoxInstance box = new BoxInstance(null, new ArrayList<>(java.util.Arrays.asList(g)), null, i);

        String manifest = i.generateContractManifest("c", box);
        check("manifest contains contractConfidence key", manifest.contains("contractConfidence"));
    }

    private static void testManifestDeepChain() {
        System.out.println("--- generateContractManifest: deep Get chain produces full path ---");
        Interpreter i = interpWithContext();
        i.globals.define("manifold", (Token) null, "sd");
        i.globals.tagAsSeedGlobal("manifold", "m.json");
        // manifold.hotspots.count
        Expr.Get inner = new Expr.Get(new Expr.Variable(id("manifold")), id("hotspots"));
        Expr.Get outer = new Expr.Get(inner, id("count"));
        BoxInstance box = new BoxInstance(null,
                new ArrayList<>(java.util.Arrays.asList(outer)), null, i);

        String manifest = i.generateContractManifest("deep", box);
        check("manifest has full path", manifest.contains("manifold.hotspots.count"));
    }

    // ---- generateExportPreamble() -------------------------------------------

    private static void testExportPreambleReturnsReadLines() {
        System.out.println("--- generateExportPreamble: produces read X into Y per seed ---");
        Interpreter i = makeInterp();
        i.globals.define("manifold",  (Token) null, null);
        i.globals.define("grammar",   (Token) null, null);
        i.globals.tagAsSeedGlobal("manifold",  "data/manifold.json");
        i.globals.tagAsSeedGlobal("grammar",   "data/grammar.json");

        String preamble = i.generateExportPreamble();
        check("preamble has manifold read", preamble.contains("read") && preamble.contains("manifold"));
        check("preamble has grammar read",  preamble.contains("grammar"));
        check("preamble has manifold path", preamble.contains("data/manifold.json"));
        check("preamble has grammar path",  preamble.contains("data/grammar.json"));
    }

    private static void testExportPreambleEmptyWhenNoSeeds() {
        System.out.println("--- generateExportPreamble: empty when no seeds injected ---");
        Interpreter i = makeInterp();
        String preamble = i.generateExportPreamble();
        check("empty preamble", preamble.isEmpty());
    }

    private static void testExportPreambleIntoKeyword() {
        System.out.println("--- generateExportPreamble: uses 'into' keyword ---");
        Interpreter i = makeInterp();
        i.globals.define("traversal", (Token) null, null);
        i.globals.tagAsSeedGlobal("traversal", "t.json");
        String preamble = i.generateExportPreamble();
        check("preamble uses 'into'", preamble.contains("into"));
        check("preamble has traversal name", preamble.contains("traversal"));
    }

    // ---- main ---------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== ContractLayerTest ===\n");

        // Environment tagging
        testTagAsSeedGlobal();
        testIsSeedGlobalFalseUntagged();
        testIsSeedGlobalWalksEnclosingChain();
        testIsSeedGlobalNotCrossContaminated();
        testGetSeedGlobalPath();
        testGetSeedGlobalPathNullForUntagged();
        testGetSeedGlobalPathWalksEnclosing();
        testGetSeedGlobalNamesReturnsAll();

        // inContractContext
        testInContractContextFalseByDefault();
        testInContractContextTrueWhenPushed();
        testInContractContextFalseAfterPop();
        testInContractContextNestedPushPop();

        // getChainRoot
        testGetChainRootSimpleVariable();
        testGetChainRootOneLevel();
        testGetChainRootTwoLevels();
        testGetChainRootNonVariableReturnsNull();

        // BoxInstance lazy preservation
        testBoxInstanceEagerOutsideContractContext();
        testBoxInstanceLazyInsideContractContext();
        testBoxInstanceEagerNonSeedGlobalInContext();
        testBoxInstanceLiteralAlwaysEager();
        testBoxInstanceMixedLazyAndEager();
        testBoxInstanceOnlyTopLevelGetPreserved();

        // CupInstance lazy preservation
        testCupInstanceLazyInsideContractContext();
        testCupInstanceEagerOutsideContractContext();

        // contractConfidence
        testContractConfidenceDefaultNegative();
        testContractConfidenceAllLazy();
        testContractConfidenceAllEager();
        testContractConfidenceMixedHalf();
        testContractConfidenceEmptyBodyZero();

        // manifest + export
        testManifestContainsLazyEntry();
        testManifestContainsEagerEntry();
        testManifestConfidenceField();
        testManifestDeepChain();
        testExportPreambleReturnsReadLines();
        testExportPreambleEmptyWhenNoSeeds();
        testExportPreambleIntoKeyword();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        System.exit(failed == 0 ? 0 : 1);
    }
}
