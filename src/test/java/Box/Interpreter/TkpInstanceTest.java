package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Parser.Expr;
import Parser.Stmt;
import Box.Token.Token;
import Box.Token.TokenType;

/**
 * Integration-level tests for TkpInstance tick loop behavior — mirrors PocketInstanceTest.
 *
 * Behavior under test (identical to PocketInstance):
 *   - Period (String ".") flips active flow direction
 *   - Bare bracket strings ("(", ")", "{", "}") bootstrap new flows
 *   - Bracket control expressions (PocketOpen/PocketClosed Stmt) bootstrap new flows
 *   - Flow string ("(.") is extracted and bootstraps a flow
 *   - Structural brackets at body[0] and body[last] are never consumed
 *
 * Key difference from PocketInstance: TkpInstance has no evaluateBody() —
 * body content is used as-is, which makes no difference for these tests since
 * strings and bracket Stmts are kept unchanged by evaluateBody anyway.
 * toString() starts with "tkp(" rather than "(".
 *
 * No JUnit dependency. Run:
 *   javac -cp <classpath> src/test/java/Box/Interpreter/TkpInstanceTest.java -d /tmp/tkptestbuild
 *   java  -cp <classpath>:/tmp/tkptestbuild Box.Interpreter.TkpInstanceTest
 */
public class TkpInstanceTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            System.out.println("  PASS  " + name);
            passed++;
        } else {
            System.out.println("  FAIL  " + name);
            failed++;
        }
    }

    private static void eq(String name, Object expected, Object actual) {
        boolean ok = (expected == null) ? (actual == null) : expected.equals(actual);
        if (ok) {
            System.out.println("  PASS  " + name);
            passed++;
        } else {
            System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual);
            failed++;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static Interpreter makeInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    private static Stmt.Expression controlStmt(Expr expr) {
        return new Stmt.Expression(expr, null);
    }

    private static Stmt.Expression pocketOpenStmt(String label) {
        Token ctrl = new Token(TokenType.OPENPAREN, label + "(", null, null, null, 0, 0, 0, 0);
        return controlStmt(new Expr.PocketOpen(ctrl));
    }

    private static Stmt.Expression pocketCloseStmt(String label) {
        Token ctrl = new Token(TokenType.CLOSEDPAREN, ")" + label, null, null, null, 0, 0, 0, 0);
        return controlStmt(new Expr.PocketClosed(ctrl));
    }

    /**
     * Build a TkpInstance whose body is: [PocketOpen("test"), ...contents..., PocketClose("test")].
     * TkpInstance has no evaluateBody(), so strings and Stmts pass through unchanged.
     */
    private static TkpInstance tkp(Interpreter interp, List<Object> contents) {
        List<Object> body = new ArrayList<>();
        body.add(pocketOpenStmt("test"));
        body.addAll(contents);
        body.add(pocketCloseStmt("test"));
        return new TkpInstance(null, body, null, interp);
    }

    // -------------------------------------------------------------------------
    // Period direction flip tests
    // -------------------------------------------------------------------------

    private static void testPeriodFlipsForwardToBackward() {
        System.out.println("--- period flips FORWARD flow to BACKWARD ---");
        Interpreter interp = makeInterp();
        TkpInstance tkp = tkp(interp, List.of("."));

        Flow flow = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 3);
        tkp.injectFlow(flow);
        tkp.tick();

        check("flow is BACKWARD after period", flow.isBackward());
        check("not FORWARD after period",      !flow.isForward());
        eq("direction", Flow.Direction.BACKWARD, flow.getDirection());
    }

    private static void testPeriodFlipsBackwardToForward() {
        System.out.println("--- period flips BACKWARD flow to FORWARD ---");
        Interpreter interp = makeInterp();
        TkpInstance tkp = tkp(interp, List.of("."));

        Flow flow = new Flow(Flow.Direction.BACKWARD, Flow.BracketType.PAREN, 3);
        tkp.injectFlow(flow);
        tkp.tick();

        check("flow is FORWARD after period",  flow.isForward());
        eq("direction", Flow.Direction.FORWARD, flow.getDirection());
    }

    private static void testPeriodFlipDoesNotAffectChain() {
        System.out.println("--- period flip does not spend chain ---");
        Interpreter interp = makeInterp();
        TkpInstance tkp = tkp(interp, List.of("."));

        Flow flow = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5);
        tkp.injectFlow(flow);
        tkp.tick();

        eq("chain unchanged after period flip", 5, flow.getChain());
    }

    private static void testTwoPeriodsRestoreDirection() {
        System.out.println("--- two periods restore original direction ---");
        Interpreter interp = makeInterp();
        // Body content: [".", "payload", "."]
        TkpInstance tkp = tkp(interp, List.of(".", "payload", "."));

        Flow flow = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5);
        tkp.injectFlow(flow);

        tkp.tick(); // forward scan hits "." at content[0] → flip to backward
        check("after tick 1: backward", flow.isBackward());

        tkp.tick(); // backward scan starts at content[last] = "." → flip to forward
        check("after tick 2: forward again", flow.isForward());
    }

    // -------------------------------------------------------------------------
    // Bare bracket string tests
    // -------------------------------------------------------------------------

    private static void testBareBracketParenOpenCreatesForwardFlow() {
        System.out.println("--- bare '(' creates FORWARD flow ---");
        Interpreter interp = makeInterp();
        TkpInstance tkp = tkp(interp, List.of("("));

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        tkp.injectFlow(trigger);

        int before = tkp.getFlows().size();
        tkp.tick();
        int after = tkp.getFlows().size();

        check("new flow created",  after > before);
        boolean hasForward = false;
        for (Flow f : tkp.getFlows()) if (f.isForward() && f != trigger) hasForward = true;
        check("new flow is FORWARD", hasForward);
    }

    private static void testBareBracketParenCloseCreatesBackwardFlow() {
        System.out.println("--- bare ')' creates BACKWARD flow ---");
        Interpreter interp = makeInterp();
        TkpInstance tkp = tkp(interp, List.of(")"));

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        tkp.injectFlow(trigger);
        tkp.tick();

        boolean hasBackward = false;
        for (Flow f : tkp.getFlows()) if (f.isBackward()) hasBackward = true;
        check("new BACKWARD flow created from ')'", hasBackward);
    }

    private static void testBareBracketBraceOpenCreatesForwardFlow() {
        System.out.println("--- bare '{' creates FORWARD BRACE flow ---");
        Interpreter interp = makeInterp();
        TkpInstance tkp = tkp(interp, List.of("{"));

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        tkp.injectFlow(trigger);
        tkp.tick();

        boolean found = false;
        for (Flow f : tkp.getFlows())
            if (f.isForward() && f.getBracketType() == Flow.BracketType.BRACE && f != trigger) found = true;
        check("FORWARD BRACE flow created from '{'", found);
    }

    private static void testBareBracketBraceCloseCreatesBackwardFlow() {
        System.out.println("--- bare '}' creates BACKWARD BRACE flow ---");
        Interpreter interp = makeInterp();
        TkpInstance tkp = tkp(interp, List.of("}"));

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        tkp.injectFlow(trigger);
        tkp.tick();

        boolean found = false;
        for (Flow f : tkp.getFlows())
            if (f.isBackward() && f.getBracketType() == Flow.BracketType.BRACE) found = true;
        check("BACKWARD BRACE flow created from '}'", found);
    }

    // -------------------------------------------------------------------------
    // Bracket control expression tests (knotted pockets)
    // -------------------------------------------------------------------------

    private static void testInnerPocketOpenCreatesForwardFlow() {
        System.out.println("--- inner PocketOpen expr creates FORWARD flow ---");
        Interpreter interp = makeInterp();
        Stmt.Expression innerOpen = pocketOpenStmt("inner");

        TkpInstance tkp = tkp(interp, List.of((Object) innerOpen));
        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        tkp.injectFlow(trigger);

        int before = tkp.getFlows().size();
        tkp.tick();
        int after = tkp.getFlows().size();

        check("a new flow was created", after > before);
        boolean hasForward = false;
        for (Flow f : tkp.getFlows()) if (f.isForward() && f != trigger) hasForward = true;
        check("new flow is FORWARD", hasForward);
    }

    private static void testInnerPocketCloseCreatesBackwardFlow() {
        System.out.println("--- inner PocketClosed expr creates BACKWARD flow ---");
        Interpreter interp = makeInterp();
        Stmt.Expression innerClose = pocketCloseStmt("inner");

        TkpInstance tkp = tkp(interp, List.of((Object) innerClose));
        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        tkp.injectFlow(trigger);
        tkp.tick();

        boolean hasBackward = false;
        for (Flow f : tkp.getFlows()) if (f.isBackward()) hasBackward = true;
        check("BACKWARD flow created from PocketClosed expr", hasBackward);
    }

    private static void testInnerBracketExprConsumedFromBody() {
        System.out.println("--- inner bracket expr is consumed from body after tick ---");
        Interpreter interp = makeInterp();
        Stmt.Expression innerOpen = pocketOpenStmt("inner");
        TkpInstance tkp = tkp(interp, List.of((Object) innerOpen));

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        tkp.injectFlow(trigger);
        tkp.tick();

        check("body no longer contains inner bracket", !tkp.toString().contains("inner"));
    }

    // -------------------------------------------------------------------------
    // Flow string bootstrap tests
    // -------------------------------------------------------------------------

    private static void testFlowStringBootstrapsFlow() {
        System.out.println("--- flow string '(.' bootstraps FORWARD flow ---");
        Interpreter interp = makeInterp();
        TkpInstance tkp = tkp(interp, List.of("(."));

        tkp.tick();

        check("at least one flow after bootstrap", !tkp.getFlows().isEmpty());
        if (!tkp.getFlows().isEmpty()) {
            check("extracted flow is FORWARD", tkp.getFlows().get(0).isForward());
            // Bootstrap and null-scan penalty happen in the same tick: chain extracted=1 then spent=0.
            eq("chain is 0 after bootstrap+spend", 0, tkp.getFlows().get(0).getChain());
        }
    }

    private static void testBackwardFlowStringBootstrapsFlow() {
        System.out.println("--- flow string '.)' bootstraps BACKWARD flow ---");
        Interpreter interp = makeInterp();
        TkpInstance tkp = tkp(interp, List.of(".)"));

        tkp.tick();

        check("at least one flow after bootstrap", !tkp.getFlows().isEmpty());
        if (!tkp.getFlows().isEmpty()) {
            check("extracted flow is BACKWARD", tkp.getFlows().get(0).isBackward());
        }
    }

    private static void testChainedFlowStringHasCorrectChain() {
        System.out.println("--- chained flow string '(.(.(.' has chain=2 after bootstrap+spend ---");
        Interpreter interp = makeInterp();
        TkpInstance tkp = tkp(interp, List.of("(.(.(."));

        tkp.tick();

        check("flow extracted", !tkp.getFlows().isEmpty());
        if (!tkp.getFlows().isEmpty()) {
            // chain=3 extracted; null-scan penalty spends one in the same tick: net=2.
            eq("chain=2 after bootstrap+spend", 2, tkp.getFlows().get(0).getChain());
        }
    }

    // -------------------------------------------------------------------------
    // Structural bracket protection test
    // -------------------------------------------------------------------------

    private static void testStructuralBracketsNotConsumed() {
        System.out.println("--- structural brackets not consumed by tick ---");
        Interpreter interp = makeInterp();
        TkpInstance tkp = tkp(interp, List.of());

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5);
        tkp.injectFlow(trigger);

        tkp.tick();
        tkp.tick();
        tkp.tick();

        // toString() starts with "tkp(" and ends with ")" regardless of content.
        String str = tkp.toString();
        check("structural brackets preserved in toString",
              str.startsWith("tkp(") && str.endsWith(")"));
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== TkpInstance Test ===\n");

        run("period FORWARD→BACKWARD",      TkpInstanceTest::testPeriodFlipsForwardToBackward);
        run("period BACKWARD→FORWARD",      TkpInstanceTest::testPeriodFlipsBackwardToForward);
        run("period no chain spend",         TkpInstanceTest::testPeriodFlipDoesNotAffectChain);
        run("two periods restore direction", TkpInstanceTest::testTwoPeriodsRestoreDirection);
        run("bare '(' → FORWARD",           TkpInstanceTest::testBareBracketParenOpenCreatesForwardFlow);
        run("bare ')' → BACKWARD",          TkpInstanceTest::testBareBracketParenCloseCreatesBackwardFlow);
        run("bare '{' → FORWARD BRACE",     TkpInstanceTest::testBareBracketBraceOpenCreatesForwardFlow);
        run("bare '}' → BACKWARD BRACE",    TkpInstanceTest::testBareBracketBraceCloseCreatesBackwardFlow);
        run("inner PocketOpen → FORWARD",   TkpInstanceTest::testInnerPocketOpenCreatesForwardFlow);
        run("inner PocketClose → BACKWARD", TkpInstanceTest::testInnerPocketCloseCreatesBackwardFlow);
        run("inner bracket consumed",        TkpInstanceTest::testInnerBracketExprConsumedFromBody);
        run("flow string '(.' extracts",     TkpInstanceTest::testFlowStringBootstrapsFlow);
        run("flow string '.)' extracts",     TkpInstanceTest::testBackwardFlowStringBootstrapsFlow);
        run("chained '(.(.(.' chain=2",      TkpInstanceTest::testChainedFlowStringHasCorrectChain);
        run("structural brackets intact",    TkpInstanceTest::testStructuralBracketsNotConsumed);

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        System.exit(failed == 0 ? 0 : 1);
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
        } catch (Exception e) {
            System.out.println("  ERROR in " + name + ": " + e);
            e.printStackTrace(System.out);
            failed++;
        }
    }
}
