package Box.Interpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for PocketInstance cascading death.
 *
 * Covers:
 *   - destroy() triggers beginDeath() on nested PocketInstances
 *   - Non-PocketInstance body items are unaffected (no ClassCastException)
 *   - Multiple nested pockets in one body all triggered
 *   - Already-dead nested pocket is skipped (idempotent)
 *   - Empty body: no-op, no exception
 *   - destroy() is idempotent on the parent
 *   - One-level cascade: nested pocket goes to isStripping; grandchild unaffected
 */
public class PocketCascadeDeathTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean cond) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else       { System.out.println("  FAIL  " + name); failed++; }
    }

    private static Interpreter makeInterp() {
        Interpreter i = new Interpreter();
        i.setForward(true);
        return i;
    }

    private static PocketInstance rawPocket(Interpreter interp, List<Object> contents) {
        List<Object> body = new ArrayList<>(contents);
        return new PocketInstance(null, body, null, interp);
    }

    // ---- cascade: single nested pocket ----------------------------------------

    private static void testSingleNestedPocketTriggered() {
        System.out.println("--- single nested pocket triggered on destroy ---");
        Interpreter i = makeInterp();
        PocketInstance nested = rawPocket(i, new ArrayList<>());
        PocketInstance parent = rawPocket(i, List.of(nested));

        check("nested alive before", nested.isAlive());
        parent.destroy();
        check("parent dead after destroy", !parent.isAlive());
        check("nested no longer alive after parent destroy", !nested.isAlive());
        check("nested is stripping (beginDeath called)", nested.isStripping());
    }

    // ---- cascade: multiple nested pockets -------------------------------------

    private static void testMultipleNestedPocketsAllTriggered() {
        System.out.println("--- all nested pockets triggered when multiple in body ---");
        Interpreter i = makeInterp();
        PocketInstance n1 = rawPocket(i, new ArrayList<>());
        PocketInstance n2 = rawPocket(i, new ArrayList<>());
        PocketInstance n3 = rawPocket(i, new ArrayList<>());
        PocketInstance parent = rawPocket(i, List.of(n1, "some string", n2, 42.0, n3));

        parent.destroy();
        check("n1 no longer alive", !n1.isAlive());
        check("n2 no longer alive", !n2.isAlive());
        check("n3 no longer alive", !n3.isAlive());
        check("n1 is stripping", n1.isStripping());
        check("n2 is stripping", n2.isStripping());
        check("n3 is stripping", n3.isStripping());
    }

    // ---- non-pocket items: no exception ---------------------------------------

    private static void testNonPocketBodyItemsSkipped() {
        System.out.println("--- non-pocket body items do not throw ---");
        Interpreter i = makeInterp();
        List<Object> mixed = new ArrayList<>();
        mixed.add("hello");
        mixed.add(42.0);
        mixed.add(Boolean.TRUE);
        mixed.add(null);
        PocketInstance parent = rawPocket(i, mixed);
        boolean threw = false;
        try {
            parent.destroy();
        } catch (Exception e) {
            threw = true;
        }
        check("no exception on mixed body", !threw);
        check("parent dead", !parent.isAlive());
    }

    // ---- already-dead nested pocket: idempotent ------------------------------

    private static void testAlreadyDeadNestedPocketSkipped() {
        System.out.println("--- already-dead nested pocket skipped (beginDeath idempotent) ---");
        Interpreter i = makeInterp();
        PocketInstance nested = rawPocket(i, new ArrayList<>());
        nested.destroy(); // kill it first
        PocketInstance parent = rawPocket(i, List.of(nested));

        boolean threw = false;
        try {
            parent.destroy();
        } catch (Exception e) {
            threw = true;
        }
        check("no exception when nested already dead", !threw);
        check("nested still dead (not double-triggered)", !nested.isAlive());
    }

    // ---- empty body: no-op ---------------------------------------------------

    private static void testEmptyBodyDestroy() {
        System.out.println("--- destroy on empty body: no exception ---");
        Interpreter i = makeInterp();
        PocketInstance parent = rawPocket(i, new ArrayList<>());
        boolean threw = false;
        try { parent.destroy(); } catch (Exception e) { threw = true; }
        check("no exception on empty body", !threw);
        check("parent dead", !parent.isAlive());
    }

    // ---- parent destroy is idempotent ----------------------------------------

    private static void testParentDestroyIdempotent() {
        System.out.println("--- parent destroy idempotent (double-destroy safe) ---");
        Interpreter i = makeInterp();
        PocketInstance nested = rawPocket(i, new ArrayList<>());
        PocketInstance parent = rawPocket(i, List.of(nested));
        parent.destroy();
        boolean threw = false;
        try { parent.destroy(); } catch (Exception e) { threw = true; }
        check("no exception on double destroy", !threw);
        check("parent still dead", !parent.isAlive());
    }

    // ---- one-level cascade only: grandchild unaffected -----------------------

    private static void testOneLevelCascadeOnly() {
        System.out.println("--- cascade is one level: grandchild unaffected by parent.destroy() ---");
        Interpreter i = makeInterp();
        PocketInstance grandchild = rawPocket(i, new ArrayList<>());
        PocketInstance child = rawPocket(i, List.of(grandchild));
        PocketInstance parent = rawPocket(i, List.of(child));

        parent.destroy();
        check("parent dead", !parent.isAlive());
        check("child stripping", child.isStripping());
        // beginDeath() on child does NOT walk child's body — grandchild unaffected
        check("grandchild still alive (one-level only)", grandchild.isAlive());
    }

    // ---- body cleared after cascade -----------------------------------------

    private static void testBodyClearedAfterDestroy() {
        System.out.println("--- body cleared after destroy ---");
        Interpreter i = makeInterp();
        PocketInstance nested = rawPocket(i, new ArrayList<>());
        PocketInstance parent = rawPocket(i, new ArrayList<>(List.of("a", nested, "b")));

        check("body non-empty before", !parent.body.isEmpty());
        parent.destroy();
        check("body empty after destroy", parent.body.isEmpty());
    }

    // ---- main ---------------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== PocketCascadeDeathTest ===\n");

        testSingleNestedPocketTriggered();
        testMultipleNestedPocketsAllTriggered();
        testNonPocketBodyItemsSkipped();
        testAlreadyDeadNestedPocketSkipped();
        testEmptyBodyDestroy();
        testParentDestroyIdempotent();
        testOneLevelCascadeOnly();
        testBodyClearedAfterDestroy();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        System.exit(failed == 0 ? 0 : 1);
    }
}
