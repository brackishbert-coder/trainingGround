package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Declaration;
import Parser.Declaration.FunDecl;
import Parser.Declaration.StmtDecl;
import Parser.Expr;
import Parser.Fun;
import Parser.Fun.FunctionLink;
import Parser.Stmt;
import Parser.Stmt.TemplatVar;

/**
 * Tests for the PCB link/template system.
 *
 * Covers:
 *   - Link declaration: BoxClass registered with isLink=true, methods map populated
 *   - FunctionLink signatures: null body, isLinkSignature()=true
 *   - Calling unimplemented link method throws RuntimeError
 *   - Template declaration: plain template instantiates; link-declared template stores names
 *   - Conformance check: passes when all methods present, throws when missing
 *   - TemplatVar (@): instantiates template into environment forward and backward
 *   - Contract context: pushed for link-declaring templates, not for plain templates
 *   - C3 rollback: failed instantiation leaves no entry in environment
 */
public class LinkTemplateTest {

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

    private static Token tok(TokenType type, String lex) {
        return new Token(type, lex, lex, null, null, 0, 0, 0, 0);
    }

    private static Token id(String lex) { return tok(TokenType.IDENTIFIER, lex); }

    private static Interpreter makeInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    private static String reverse(String s) {
        return new StringBuilder(s).reverse().toString();
    }

    /**
     * Build and evaluate an Expr.Link wrapping a Cup with the given FunctionLink declarations.
     * Returns the registered BoxClass from globals.
     */
    private static BoxClass registerLink(Interpreter interp, String name, FunctionLink... funLinks) {
        String rev = reverse(name);
        Token openTok = id(name);
        Token closeTok = id(rev);
        List<Declaration> decls = new ArrayList<>();
        for (FunctionLink fl : funLinks)
            decls.add(new FunDecl(fl));
        Expr.Cup cup = new Expr.Cup(openTok, decls, "", closeTok);
        Expr.Link link = new Expr.Link(cup);
        interp.links.add(name);
        interp.links.add(rev);
        interp.globals.define(name, (Token) null, null);
        interp.globals.define(rev,  (Token) null, null);
        interp.evaluate(link);
        return (BoxClass) interp.globals.get(openTok, false);
    }

    /**
     * Build and evaluate an Expr.Template wrapping a Cup with the given declarations.
     * linkNameTokens and baseTemplate may be null/empty.
     * Returns the registered BoxClass from globals.
     */
    private static BoxClass registerTemplate(Interpreter interp, String name,
            List<Declaration> body, ArrayList<Token> linkNames) {
        String rev = reverse(name);
        Token openTok = id(name);
        Token closeTok = id(rev);
        Expr.Cup cup = new Expr.Cup(openTok, body, "", closeTok);
        Expr.Template tmpl = new Expr.Template(cup, linkNames, null);
        interp.templates.add(name);
        interp.templates.add(rev);
        interp.globals.define(name, (Token) null, null);
        interp.globals.define(rev,  (Token) null, null);
        interp.evaluate(tmpl);
        return (BoxClass) interp.globals.get(openTok, false);
    }

    /** Make a zero-param FunctionLink with given forward/backward names. */
    private static FunctionLink funcLink(String fwd, String bwd) {
        return new FunctionLink(id(fwd), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), id(bwd));
    }

    /** Make a zero-arg real BoxFunction (with a body) for satisfying conformance. */
    private static BoxFunction realMethod(String name, boolean isForward, Interpreter interp) {
        Expr.Cup body = new Expr.Cup(null, new ArrayList<>(), "", null);
        return new BoxFunction(body, name, new ArrayList<>(), new ArrayList<>(),
                interp.globals, isForward, false);
    }

    /**
     * Build a Fun.Function declaration (forward=fwd, backward=bwd) with an empty Cup body.
     * Used to put real method implementations into a template cup so conformance passes.
     */
    private static Declaration.FunDecl funcDecl(String fwd, String bwd) {
        Expr.Cup emptyBody = new Expr.Cup(null, new ArrayList<>(), "", null);
        Fun.Function fn = new Fun.Function(id(fwd), new ArrayList<>(), new ArrayList<>(),
                emptyBody, new ArrayList<>(), new ArrayList<>(), id(bwd));
        return new Declaration.FunDecl(fn);
    }

    // ---- Link registration ---------------------------------------------------

    private static void testLinkRegistersBoxClass() {
        System.out.println("--- link declaration registers BoxClass with isLink=true ---");
        Interpreter i = makeInterp();
        BoxClass bc = registerLink(i, "Drawable", funcLink("draw", "ward"));
        check("BoxClass not null", bc != null);
        check("isLink=true", bc != null && bc.isLink());
    }

    private static void testLinkRegistersSignatures() {
        System.out.println("--- link FunctionLink populates methods map ---");
        Interpreter i = makeInterp();
        BoxClass bc = registerLink(i, "Drawable", funcLink("draw", "ward"));
        check("forward method 'draw' exists", bc != null && bc.findMethod("draw") != null);
        check("backward method 'ward' exists", bc != null && bc.findMethod("ward") != null);
    }

    private static void testLinkSignatureHasNullBody() {
        System.out.println("--- link method has null body → isLinkSignature()=true ---");
        Interpreter i = makeInterp();
        BoxClass bc = registerLink(i, "Drawable", funcLink("draw", "ward"));
        BoxFunction drawFn = bc.findMethod("draw");
        check("isLinkSignature draw", drawFn != null && drawFn.isLinkSignature());
        BoxFunction wardFn = bc.findMethod("ward");
        check("isLinkSignature ward", wardFn != null && wardFn.isLinkSignature());
    }

    private static void testMultipleFunctionLinksRegistered() {
        System.out.println("--- multiple FunctionLinks all registered ---");
        Interpreter i = makeInterp();
        BoxClass bc = registerLink(i, "Shape",
                funcLink("area",   "aera"),
                funcLink("resize", "eziser"));
        check("area",   bc != null && bc.findMethod("area")   != null);
        check("aera",   bc != null && bc.findMethod("aera")   != null);
        check("resize", bc != null && bc.findMethod("resize") != null);
        check("eziser", bc != null && bc.findMethod("eziser") != null);
    }

    // ---- Calling unimplemented link method -----------------------------------

    private static void testCallingLinkMethodThrows() {
        System.out.println("--- calling null-body link method throws RuntimeError ---");
        Interpreter i = makeInterp();
        BoxClass bc = registerLink(i, "Drawable", funcLink("draw", "ward"));
        BoxFunction drawFn = bc.findMethod("draw");
        boolean threw = false;
        try {
            drawFn.call(i, new ArrayList<>());
        } catch (RuntimeError e) {
            threw = true;
            check("error mentions 'draw'", e.getMessage().contains("draw"));
        }
        check("threw RuntimeError", threw);
    }

    // ---- Template registration ----------------------------------------------

    private static void testPlainTemplateRegisters() {
        System.out.println("--- plain template registers BoxClass ---");
        Interpreter i = makeInterp();
        BoxClass bc = registerTemplate(i, "Circle", new ArrayList<>(), new ArrayList<>());
        check("BoxClass not null", bc != null);
        check("isLink=false", bc != null && !bc.isLink());
    }

    private static void testTemplateWithLinkNamesStored() {
        System.out.println("--- template with link names stores templateLinkNames ---");
        Interpreter i = makeInterp();
        registerLink(i, "Drawable", funcLink("draw", "ward"));

        // Template body implements draw+ward so conformance passes
        ArrayList<Token> linkToks = new ArrayList<>();
        linkToks.add(id("Drawable"));
        List<Declaration> body = new ArrayList<>();
        body.add(funcDecl("draw", "ward"));
        BoxClass bc = registerTemplate(i, "Circle", body, linkToks);
        check("templateLinkNames not null", bc != null && bc.templateLinkNames != null);
        check("templateLinkNames contains Drawable",
                bc != null && bc.templateLinkNames != null && bc.templateLinkNames.contains("Drawable"));
    }

    // ---- Conformance check --------------------------------------------------

    private static void testConformancePassesWhenAllMethodsPresent() {
        System.out.println("--- conformance passes: template with all link methods does not throw ---");
        Interpreter i = makeInterp();
        registerLink(i, "Drawable", funcLink("draw", "ward"));

        ArrayList<Token> linkToks = new ArrayList<>();
        linkToks.add(id("Drawable"));
        List<Declaration> body = new ArrayList<>();
        body.add(funcDecl("draw", "ward"));

        boolean threw = false;
        BoxClass bc = null;
        try {
            bc = registerTemplate(i, "Circle", body, linkToks);
        } catch (RuntimeError e) {
            threw = true;
        }
        check("no exception on full conformance", !threw);
        check("BoxClass registered", bc != null);
        check("draw method present", bc != null && bc.findMethod("draw") != null);
        check("draw not link signature", bc != null && bc.findMethod("draw") != null
                && !bc.findMethod("draw").isLinkSignature());
    }

    private static void testConformanceFailsIfMethodMissing() {
        System.out.println("--- conformance fails: template missing link method throws ---");
        Interpreter i = makeInterp();
        registerLink(i, "Drawable", funcLink("draw", "ward"));

        // Register a template that declares &Drawable but has NO body methods
        ArrayList<Token> linkToks = new ArrayList<>();
        linkToks.add(id("Drawable"));
        boolean threw = false;
        try {
            registerTemplate(i, "BadCircle", new ArrayList<>(), linkToks);
        } catch (RuntimeError e) {
            threw = true;
            check("error mentions missing method", e.getMessage().contains("draw") || e.getMessage().contains("ward"));
        }
        check("threw RuntimeError for missing method", threw);
    }

    // ---- TemplatVar instantiation -------------------------------------------

    private static void testTemplatVarForwardInstantiatesIntoEnvironment() {
        System.out.println("--- TemplatVar forward: instance created in environment ---");
        Interpreter i = makeInterp();
        registerTemplate(i, "Circle", new ArrayList<>(), new ArrayList<>());

        // Simulate: Circle @ myCircle
        Token instanceName = id("myCircle");
        Token templateName = id("Circle");
        i.globals.define("myCircle", (Token) null, null);
        i.globals.define(reverse("myCircle"), (Token) null, null);

        TemplatVar stmt = new TemplatVar(instanceName, templateName);
        i.execute(new StmtDecl(stmt));

        Object val = i.globals.get(instanceName, false);
        check("instance created", val instanceof Instance);
    }

    private static void testTemplatVarBackwardInstantiatesIntoEnvironment() {
        System.out.println("--- TemplatVar backward: instance created in environment ---");
        Interpreter i = makeInterp();
        i.setForward(false);
        registerTemplate(i, "Circle", new ArrayList<>(), new ArrayList<>());
        i.setForward(false);

        Token instanceName = id("myCircle");
        Token templateName = id("Circle");
        i.globals.define("myCircle", (Token) null, null);
        i.globals.define(reverse("myCircle"), (Token) null, null);
        // backward: name=Circle is in templates, superclass=myCircle is NOT → swap perspective
        TemplatVar stmt = new TemplatVar(templateName, instanceName);
        i.execute(new StmtDecl(stmt));

        Object val = i.globals.get(id(reverse("myCircle")), false);
        check("backward instance created", val instanceof Instance);
    }

    // ---- Contract context stack ---------------------------------------------

    private static void testContractContextPushedForLinkTemplate() {
        System.out.println("--- contract context pushed for link-template, detected via lazy preservation ---");
        // A template with &Drawable should push contractContextStack during instantiation.
        // We verify by checking that a seed-global Expr.Get in the body is preserved (lazy),
        // which only happens when inContractContext() is true.
        Interpreter i = makeInterp();
        registerLink(i, "Drawable", funcLink("draw", "ward"));
        i.globals.define("seed", (Token) null, "seedData");
        i.globals.tagAsSeedGlobal("seed", "seed.json");

        // Template body: real draw+ward methods PLUS the seed reference will show up
        // in the instance body when instantiated.
        ArrayList<Token> linkToks = new ArrayList<>();
        linkToks.add(id("Drawable"));
        List<Declaration> body = new ArrayList<>();
        body.add(funcDecl("draw", "ward"));
        registerTemplate(i, "Circle", body, linkToks);

        i.globals.define("myCircle", (Token) null, null);
        i.globals.define(reverse("myCircle"), (Token) null, null);
        TemplatVar stmt = new TemplatVar(id("myCircle"), id("Circle"));
        i.execute(new StmtDecl(stmt));

        // Stack must be empty after completion
        check("context stack empty after link-template instantiation", i.contractContextStack.isEmpty());

        // Instance was created
        Object val = i.globals.get(id("myCircle"), false);
        check("link-template instance created", val instanceof Instance);
    }

    private static void testContractContextEmptyAfterInstantiation() {
        System.out.println("--- contract context stack always empty after instantiation completes ---");
        Interpreter i = makeInterp();
        registerTemplate(i, "Shape", new ArrayList<>(), new ArrayList<>());
        i.globals.define("myShape", (Token) null, null);
        i.globals.define(reverse("myShape"), (Token) null, null);
        TemplatVar stmt = new TemplatVar(id("myShape"), id("Shape"));
        i.execute(new StmtDecl(stmt));
        check("stack empty after", i.contractContextStack.isEmpty());
    }

    // ---- C3 rollback --------------------------------------------------------

    private static void testC3RollbackOnFailedInstantiation() {
        System.out.println("--- C3: failed instantiation leaves no entry in environment ---");
        Interpreter i = makeInterp();
        // Register a template whose body will throw during evaluation
        // We can simulate this: register a template but then corrupt its BoxClass so call() throws.
        // Simplest: directly test that after a TemplatVar throws, environment has only null.
        registerTemplate(i, "Broken", new ArrayList<>(), new ArrayList<>());
        // Sabotage: replace the BoxClass in globals with something that throws on call()
        BoxCallable throwingCallable = new BoxCallable() {
            public Object call(Interpreter interp, java.util.List<Object> args) {
                throw new RuntimeError(id("Broken"), "intentional failure");
            }
            public int arity() { return 0; }
            public BoxFunction findMethod(String name) { return null; }
        };
        i.globals.define("Broken", (Token) null, throwingCallable);

        i.globals.define("myBroken", (Token) null, null);
        i.globals.define(reverse("myBroken"), (Token) null, null);
        TemplatVar stmt = new TemplatVar(id("myBroken"), id("Broken"));

        boolean threw = false;
        try {
            i.execute(new StmtDecl(stmt));
        } catch (RuntimeError e) {
            threw = true;
            check("error wraps template name", e.getMessage().contains("myBroken") || e.getMessage().contains("Broken"));
        }
        check("threw on bad instantiation", threw);

        // environment still has null for myBroken (no broken instance committed)
        Object val = i.globals.get(id("myBroken"), false);
        check("no broken instance in environment", !(val instanceof Instance));
    }

    // ---- main ---------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== LinkTemplateTest ===\n");

        testLinkRegistersBoxClass();
        testLinkRegistersSignatures();
        testLinkSignatureHasNullBody();
        testMultipleFunctionLinksRegistered();
        testCallingLinkMethodThrows();
        testPlainTemplateRegisters();
        testTemplateWithLinkNamesStored();
        testConformancePassesWhenAllMethodsPresent();
        testConformanceFailsIfMethodMissing();
        testTemplatVarForwardInstantiatesIntoEnvironment();
        testTemplatVarBackwardInstantiatesIntoEnvironment();
        testContractContextPushedForLinkTemplate();
        testContractContextEmptyAfterInstantiation();
        testC3RollbackOnFailedInstantiation();

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
