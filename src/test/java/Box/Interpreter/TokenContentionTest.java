package Box.Interpreter;

/**
 * Tests for TokenContentionError construction, message format, and field access.
 *
 * TokenContentionError is thrown when a pocket/tkp CAS acquire fails:
 *   new TokenContentionError(pocketName, tickNumber, tokenLevel)
 *
 * Expected message format:
 *   "Token contention [interpreter] pocket=p tick=5"
 *   "Token contention [myBox] pocket=alpha tick=0"
 *
 * Fields: pocketName (String), tickNumber (int), tokenLevel (String).
 * Extends RuntimeException — never checked.
 */
public class TokenContentionTest {

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

    // -------------------------------------------------------------------------
    // Construction and field access
    // -------------------------------------------------------------------------

    private static void testFieldsPreserved() {
        System.out.println("--- fields stored correctly ---");
        TokenContentionError e = new TokenContentionError("alpha", 7, "interpreter");
        eq("pocketName", "alpha", e.pocketName);
        eq("tickNumber", 7, e.tickNumber);
        eq("tokenLevel", "interpreter", e.tokenLevel);
    }

    private static void testZeroTick() {
        System.out.println("--- tick=0 is valid ---");
        TokenContentionError e = new TokenContentionError("p", 0, "interpreter");
        eq("tickNumber=0", 0, e.tickNumber);
        eq("pocketName", "p", e.pocketName);
    }

    private static void testContainerTokenLevel() {
        System.out.println("--- container token level ---");
        TokenContentionError e = new TokenContentionError("worker", 42, "myBox");
        eq("tokenLevel=myBox", "myBox", e.tokenLevel);
        eq("pocketName=worker", "worker", e.pocketName);
        eq("tickNumber=42", 42, e.tickNumber);
    }

    // -------------------------------------------------------------------------
    // Message format
    // -------------------------------------------------------------------------

    private static void testMessageFormatInterpreter() {
        System.out.println("--- message format: interpreter token ---");
        TokenContentionError e = new TokenContentionError("p", 5, "interpreter");
        String expected = "Token contention [interpreter] pocket=p tick=5";
        eq("getMessage()", expected, e.getMessage());
    }

    private static void testMessageFormatContainer() {
        System.out.println("--- message format: container token level ---");
        TokenContentionError e = new TokenContentionError("alpha", 0, "myBox");
        String expected = "Token contention [myBox] pocket=alpha tick=0";
        eq("getMessage()", expected, e.getMessage());
    }

    private static void testMessageFormatLargeTickNumber() {
        System.out.println("--- message format: large tick number ---");
        TokenContentionError e = new TokenContentionError("worker", 99999, "interpreter");
        String expected = "Token contention [interpreter] pocket=worker tick=99999";
        eq("getMessage()", expected, e.getMessage());
    }

    // -------------------------------------------------------------------------
    // Inheritance
    // -------------------------------------------------------------------------

    private static void testExtendsRuntimeException() {
        System.out.println("--- extends RuntimeException ---");
        TokenContentionError e = new TokenContentionError("p", 0, "interpreter");
        check("is RuntimeException", e instanceof RuntimeException);
        check("is Throwable", e instanceof Throwable);
    }

    private static void testCatchAsRuntimeException() {
        System.out.println("--- catchable as RuntimeException ---");
        boolean caught = false;
        try {
            throw new TokenContentionError("q", 1, "interpreter");
        } catch (RuntimeException ex) {
            caught = true;
            eq("message preserved in catch", "Token contention [interpreter] pocket=q tick=1", ex.getMessage());
        }
        check("caught as RuntimeException", caught);
    }

    private static void testNeverCheckedException() {
        System.out.println("--- not a checked exception ---");
        TokenContentionError e = new TokenContentionError("p", 0, "interpreter");
        // RuntimeException hierarchy: can be thrown without a throws declaration
        check("runtime exception confirmed", e instanceof RuntimeException);
        // Fields survive being caught as RuntimeException
        try {
            throw e;
        } catch (RuntimeException ex) {
            check("fields survive catch-as-RuntimeException", ex instanceof TokenContentionError);
        }
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    private static void testEmptyPocketName() {
        System.out.println("--- empty pocket name ---");
        TokenContentionError e = new TokenContentionError("", 3, "interpreter");
        eq("empty name preserved", "", e.pocketName);
        eq("message with empty name", "Token contention [interpreter] pocket= tick=3", e.getMessage());
    }

    private static void testSpecialCharsInName() {
        System.out.println("--- special characters in name ---");
        TokenContentionError e = new TokenContentionError("my-pocket", 0, "container.123");
        eq("pocketName with hyphen", "my-pocket", e.pocketName);
        eq("tokenLevel with dot", "container.123", e.tokenLevel);
        eq("message", "Token contention [container.123] pocket=my-pocket tick=0", e.getMessage());
    }

    // -------------------------------------------------------------------------
    // Main
    // -------------------------------------------------------------------------

    public static void main(String[] args) {
        testFieldsPreserved();
        testZeroTick();
        testContainerTokenLevel();
        testMessageFormatInterpreter();
        testMessageFormatContainer();
        testMessageFormatLargeTickNumber();
        testExtendsRuntimeException();
        testCatchAsRuntimeException();
        testNeverCheckedException();
        testEmptyPocketName();
        testSpecialCharsInName();

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
    }
}
