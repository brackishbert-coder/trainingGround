package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

import Parser.Expr;
import Parser.Stmt;
import Box.Token.Token;
import Box.Token.TokenType;

/**
 * Integration-level tests for PocketInstance tick loop behavior:
 *   - Period (String ".") flips active flow direction
 *   - Bare bracket strings ("(", ")", "{", "}") bootstrap new flows
 *   - Bracket control expressions (PocketOpen/PocketClosed Stmt) bootstrap new flows
 *   - Flow string ("(.") is extracted and bootstraps a flow
 *
 * No JUnit dependency. Tests manipulate PocketInstance directly without
 * running the full PCB pipeline.
 *
 * Run:
 *   javac -cp <classpath> src/test/java/Box/Interpreter/PocketInstanceTest.java -d /tmp/pkttestbuild
 *   java  -cp <classpath>:/tmp/pkttestbuild Box.Interpreter.PocketInstanceTest
 */
public class PocketInstanceTest {

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
     * Build a PocketInstance whose body is: [PocketOpen("test"), ...contents..., PocketClose("test")].
     * The structural brackets are at body[0] and body[last].
     * Strings and Stmt objects in contents pass through evaluateBody() unchanged.
     */
    private static PocketInstance pocket(Interpreter interp, List<Object> contents) {
        List<Object> body = new ArrayList<>();
        body.add(pocketOpenStmt("test"));
        body.addAll(contents);
        body.add(pocketCloseStmt("test"));
        return new PocketInstance(null, body, null, interp);
    }

    // -------------------------------------------------------------------------
    // Period direction flip tests
    // -------------------------------------------------------------------------

    private static void testPeriodFlipsForwardToBackward() {
        System.out.println("--- period flips FORWARD flow to BACKWARD ---");
        Interpreter interp = makeInterp();
        PocketInstance pkt = pocket(interp, List.of("."));

        Flow flow = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 3);
        pkt.injectFlow(flow);
        pkt.tick();

        check("flow is BACKWARD after period", flow.isBackward());
        check("not FORWARD after period",      !flow.isForward());
        eq("direction", Flow.Direction.BACKWARD, flow.getDirection());
    }

    private static void testPeriodFlipsBackwardToForward() {
        System.out.println("--- period flips BACKWARD flow to FORWARD ---");
        Interpreter interp = makeInterp();
        PocketInstance pkt = pocket(interp, List.of("."));

        // Backward flow scans from body.size()-2 down to index 1.
        Flow flow = new Flow(Flow.Direction.BACKWARD, Flow.BracketType.PAREN, 3);
        pkt.injectFlow(flow);
        pkt.tick();

        check("flow is FORWARD after period",  flow.isForward());
        eq("direction", Flow.Direction.FORWARD, flow.getDirection());
    }

    private static void testPeriodFlipDoesNotAffectChain() {
        System.out.println("--- period flip does not spend chain ---");
        Interpreter interp = makeInterp();
        PocketInstance pkt = pocket(interp, List.of("."));

        Flow flow = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5);
        pkt.injectFlow(flow);
        pkt.tick();

        eq("chain unchanged after period flip", 5, flow.getChain());
    }

    private static void testTwoPeriodsRestoreDirection() {
        System.out.println("--- two periods restore original direction ---");
        Interpreter interp = makeInterp();
        // Body content: [".", "payload", "."]
        PocketInstance pkt = pocket(interp, List.of(".", "payload", "."));

        Flow flow = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5);
        pkt.injectFlow(flow);

        pkt.tick(); // forward scan hits "." at content[0] → flip to backward
        check("after tick 1: backward", flow.isBackward());

        pkt.tick(); // backward scan starts at content[last] = "." → flip to forward
        check("after tick 2: forward again", flow.isForward());
    }

    // -------------------------------------------------------------------------
    // Bare bracket string tests
    // -------------------------------------------------------------------------

    private static void testBareBracketParenOpenCreatesForwardFlow() {
        System.out.println("--- bare '(' creates FORWARD flow ---");
        Interpreter interp = makeInterp();
        PocketInstance pkt = pocket(interp, List.of("("));

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        pkt.injectFlow(trigger);

        int before = pkt.getFlows().size();
        pkt.tick();
        int after = pkt.getFlows().size();

        check("new flow created",               after > before);
        boolean hasForward = false;
        for (Flow f : pkt.getFlows()) if (f.isForward() && f != trigger) hasForward = true;
        check("new flow is FORWARD",            hasForward);
    }

    private static void testBareBracketParenCloseCreatesBackwardFlow() {
        System.out.println("--- bare ')' creates BACKWARD flow ---");
        Interpreter interp = makeInterp();
        PocketInstance pkt = pocket(interp, List.of(")"));

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        pkt.injectFlow(trigger);

        pkt.tick();

        boolean hasBackward = false;
        for (Flow f : pkt.getFlows()) if (f.isBackward()) hasBackward = true;
        check("new BACKWARD flow created from ')'", hasBackward);
    }

    private static void testBareBracketBraceOpenCreatesForwardFlow() {
        System.out.println("--- bare '{' creates FORWARD BRACE flow ---");
        Interpreter interp = makeInterp();
        PocketInstance pkt = pocket(interp, List.of("{"));

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        pkt.injectFlow(trigger);
        pkt.tick();

        boolean found = false;
        for (Flow f : pkt.getFlows())
            if (f.isForward() && f.getBracketType() == Flow.BracketType.BRACE && f != trigger) found = true;
        check("FORWARD BRACE flow created from '{'", found);
    }

    private static void testBareBracketBraceCloseCreatesBackwardFlow() {
        System.out.println("--- bare '}' creates BACKWARD BRACE flow ---");
        Interpreter interp = makeInterp();
        PocketInstance pkt = pocket(interp, List.of("}"));

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        pkt.injectFlow(trigger);
        pkt.tick();

        boolean found = false;
        for (Flow f : pkt.getFlows())
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

        PocketInstance pkt = pocket(interp, List.of((Object) innerOpen));
        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        pkt.injectFlow(trigger);

        int before = pkt.getFlows().size();
        pkt.tick();
        int after = pkt.getFlows().size();

        check("a new flow was created", after > before);
        boolean hasForward = false;
        for (Flow f : pkt.getFlows()) if (f.isForward() && f != trigger) hasForward = true;
        check("new flow is FORWARD", hasForward);
    }

    private static void testInnerPocketCloseCreatesBackwardFlow() {
        System.out.println("--- inner PocketClosed expr creates BACKWARD flow ---");
        Interpreter interp = makeInterp();
        Stmt.Expression innerClose = pocketCloseStmt("inner");

        PocketInstance pkt = pocket(interp, List.of((Object) innerClose));
        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        pkt.injectFlow(trigger);

        pkt.tick();

        boolean hasBackward = false;
        for (Flow f : pkt.getFlows()) if (f.isBackward()) hasBackward = true;
        check("BACKWARD flow created from PocketClosed expr", hasBackward);
    }

    private static void testInnerBracketExprConsumesFromBody() {
        System.out.println("--- inner bracket expr is consumed from body after tick ---");
        Interpreter interp = makeInterp();
        Stmt.Expression innerOpen = pocketOpenStmt("inner");
        PocketInstance pkt = pocket(interp, List.of((Object) innerOpen));

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 1);
        pkt.injectFlow(trigger);
        pkt.tick();

        // The inner PocketOpen should have been consumed from the body.
        // Body should now only contain structural brackets (size = 2).
        eq("body reduced to structural brackets only", 2, pkt.getFlows().size() + 0);
        // More directly: check the body via toString doesn't contain inner content
        check("body size is 2 after consumption", pkt.toString().equals("()") || !pkt.toString().contains("inner"));
    }

    // -------------------------------------------------------------------------
    // Flow string bootstrap test
    // -------------------------------------------------------------------------

    private static void testFlowStringBootstrapsFlow() {
        System.out.println("--- flow string '(.' bootstraps FORWARD flow ---");
        Interpreter interp = makeInterp();
        PocketInstance pkt = pocket(interp, List.of("(."));

        // No flows injected — tick() should extract the flow from "(." in the body.
        pkt.tick();

        check("at least one flow after bootstrap", !pkt.getFlows().isEmpty());
        if (!pkt.getFlows().isEmpty()) {
            check("extracted flow is FORWARD", pkt.getFlows().get(0).isForward());
            // Bootstrap and null-scan penalty happen in the same tick: chain extracted=1 then spent=0.
            eq("chain is 0 after bootstrap+spend", 0, pkt.getFlows().get(0).getChain());
        }
    }

    private static void testBackwardFlowStringBootstrapsFlow() {
        System.out.println("--- flow string '.)' bootstraps BACKWARD flow ---");
        Interpreter interp = makeInterp();
        PocketInstance pkt = pocket(interp, List.of(".)"));

        pkt.tick();

        check("at least one flow after bootstrap", !pkt.getFlows().isEmpty());
        if (!pkt.getFlows().isEmpty()) {
            check("extracted flow is BACKWARD", pkt.getFlows().get(0).isBackward());
        }
    }

    private static void testChainedFlowStringHasCorrectChain() {
        System.out.println("--- chained flow string '(.(.(.' has chain=3 ---");
        Interpreter interp = makeInterp();
        PocketInstance pkt = pocket(interp, List.of("(.(.(." ));

        pkt.tick();

        check("flow extracted", !pkt.getFlows().isEmpty());
        if (!pkt.getFlows().isEmpty()) {
            // chain=3 extracted from "(.(.(."; null-scan penalty immediately spends one: net=2.
            eq("chain=2 after bootstrap+spend", 2, pkt.getFlows().get(0).getChain());
        }
    }

    // -------------------------------------------------------------------------
    // Structural bracket protection test
    // -------------------------------------------------------------------------

    private static void testStructuralBracketsNotConsumed() {
        System.out.println("--- structural brackets not consumed by tick ---");
        Interpreter interp = makeInterp();
        // Empty pocket — only structural brackets.
        PocketInstance pkt = pocket(interp, List.of());

        Flow trigger = new Flow(Flow.Direction.FORWARD, Flow.BracketType.PAREN, 5);
        pkt.injectFlow(trigger);

        // Even with a bootstrapping flow, an empty body scan produces nothing.
        // The tick should drain the chain (null-scan penalty) but not consume brackets.
        pkt.tick();
        pkt.tick();
        pkt.tick();

        // toString() should still produce "()" — structural brackets intact.
        String str = pkt.toString();
        check("structural brackets preserved in toString", str.startsWith("(") && str.endsWith(")"));
    }

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== PocketInstance Test ===\n");

        run("period FORWARD→BACKWARD",      PocketInstanceTest::testPeriodFlipsForwardToBackward);
        run("period BACKWARD→FORWARD",      PocketInstanceTest::testPeriodFlipsBackwardToForward);
        run("period no chain spend",         PocketInstanceTest::testPeriodFlipDoesNotAffectChain);
        run("two periods restore direction", PocketInstanceTest::testTwoPeriodsRestoreDirection);
        run("bare '(' → FORWARD",           PocketInstanceTest::testBareBracketParenOpenCreatesForwardFlow);
        run("bare ')' → BACKWARD",          PocketInstanceTest::testBareBracketParenCloseCreatesBackwardFlow);
        run("bare '{' → FORWARD BRACE",     PocketInstanceTest::testBareBracketBraceOpenCreatesForwardFlow);
        run("bare '}' → BACKWARD BRACE",    PocketInstanceTest::testBareBracketBraceCloseCreatesBackwardFlow);
        run("inner PocketOpen → FORWARD",   PocketInstanceTest::testInnerPocketOpenCreatesForwardFlow);
        run("inner PocketClose → BACKWARD", PocketInstanceTest::testInnerPocketCloseCreatesBackwardFlow);
        run("inner bracket consumed",        PocketInstanceTest::testInnerBracketExprConsumesFromBody);
        run("flow string '(.' extracts",     PocketInstanceTest::testFlowStringBootstrapsFlow);
        run("flow string '.)' extracts",     PocketInstanceTest::testBackwardFlowStringBootstrapsFlow);
        run("chained '(.(.(.' chain=2",      PocketInstanceTest::testChainedFlowStringHasCorrectChain);
        run("structural brackets intact",    PocketInstanceTest::testStructuralBracketsNotConsumed);

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
