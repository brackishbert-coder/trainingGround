package Box.Interpreter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Tests for ContainerPersistence — batch-mode file load/dump for all six
 * system containers.
 *
 * Runs against a temporary directory to avoid touching the real working dir.
 */
public class ContainerPersistenceTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) { System.out.println("  PASS  " + name); passed++; }
        else           { System.out.println("  FAIL  " + name); failed++; }
    }

    private static void eq(String name, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (ok)  { System.out.println("  PASS  " + name); passed++; }
        else     { System.out.println("  FAIL  " + name + "  expected=" + expected + "  got=" + actual); failed++; }
    }

    // ---- Helpers ------------------------------------------------------------

    private static Path tmpDir;

    private static void setWorkDir(Path dir) {
        System.setProperty("user.dir", dir.toString());
    }

    private static void writeFile(Path dir, String name, String... lines) throws IOException {
        Files.write(dir.resolve(name), Arrays.asList(lines), StandardCharsets.UTF_8);
    }

    private static List<String> readFile(Path dir, String name) throws IOException {
        return Files.readAllLines(dir.resolve(name), StandardCharsets.UTF_8);
    }

    private static Interpreter freshInterp() {
        return new Interpreter();
    }

    // ---- Tests --------------------------------------------------------------

    /** Default files created when none exist; all containers start empty. */
    private static void testDefaultFilesCreated() throws IOException {
        System.out.println("--- default files created when missing ---");
        Path dir = Files.createTempDirectory("pcb_persist_test_");
        setWorkDir(dir);

        new Interpreter();  // triggers loadAll → creates defaults

        String[] expected = { "bin.box", "gap.xob", "non.cup", "limbo.puc", "trash.pkt", "void.tkp" };
        for (String f : expected) {
            check("created " + f, Files.exists(dir.resolve(f)));
        }

        // Verify default content is just open+close brackets
        List<String> binLines = readFile(dir, "bin.box");
        check("bin.box first line is [",  binLines.get(0).strip().equals("["));
        check("bin.box last line is ]",   binLines.get(binLines.size()-1).strip().equals("]"));

        List<String> trashLines = readFile(dir, "trash.pkt");
        check("trash.pkt first line is (", trashLines.get(0).strip().equals("("));
        check("trash.pkt last line is )",  trashLines.get(trashLines.size()-1).strip().equals(")"));
    }

    /** Values dumped to BIN survive a round-trip through the file. */
    private static void testBinRoundTrip() throws IOException {
        System.out.println("--- BIN round-trip ---");
        Path dir = Files.createTempDirectory("pcb_persist_bin_");
        setWorkDir(dir);

        Interpreter interp = freshInterp();
        interp.bin.body.add("error_message");
        interp.bin.body.add(42.0);
        interp.bin.body.add(Boolean.TRUE);
        ContainerPersistence.dumpAll(interp);

        // New interpreter loads the dumped file
        Interpreter interp2 = freshInterp();
        check("BIN has 3 items after reload", interp2.bin.body.size() == 3);
        eq("BIN[0] is error_message", "error_message", interp2.bin.body.get(0));
        eq("BIN[1] is 42.0",          42.0,            interp2.bin.body.get(1));
        eq("BIN[2] is true",          Boolean.TRUE,     interp2.bin.body.get(2));
    }

    /** GAP (xob) round-trip. */
    private static void testGapRoundTrip() throws IOException {
        System.out.println("--- GAP round-trip ---");
        Path dir = Files.createTempDirectory("pcb_persist_gap_");
        setWorkDir(dir);

        Interpreter interp = freshInterp();
        interp.gap.body.add("unclaimed");
        interp.gap.body.add(3.14);
        ContainerPersistence.dumpAll(interp);

        Interpreter interp2 = freshInterp();
        check("GAP has 2 items",  interp2.gap.body.size() == 2);
        eq("GAP[0]", "unclaimed", interp2.gap.body.get(0));
        eq("GAP[1]", 3.14,        interp2.gap.body.get(1));
    }

    /** TRASH (pkt) round-trip — structural brackets injected, content accessible. */
    private static void testTrashRoundTrip() throws IOException {
        System.out.println("--- TRASH round-trip ---");
        Path dir = Files.createTempDirectory("pcb_persist_trash_");
        setWorkDir(dir);

        Interpreter interp = freshInterp();
        // Add after structural brackets (at position 1, before the close bracket)
        interp.trash.body.add(1, "mismatch error");
        ContainerPersistence.dumpAll(interp);

        Interpreter interp2 = freshInterp();
        // Body should be [PocketOpen, "mismatch error", PocketClosed]
        long contentCount = interp2.trash.body.stream()
            .filter(o -> !(o instanceof Parser.Stmt.Expression))
            .count();
        eq("TRASH content items = 1", 1L, contentCount);
        // Find the content item
        Object content = interp2.trash.body.stream()
            .filter(o -> !(o instanceof Parser.Stmt.Expression))
            .findFirst().orElse(null);
        eq("TRASH content = mismatch error", "mismatch error", content);
    }

    /** VOID (tkp) round-trip — structural brackets injected. */
    private static void testVoidRoundTrip() throws IOException {
        System.out.println("--- VOID round-trip ---");
        Path dir = Files.createTempDirectory("pcb_persist_void_");
        setWorkDir(dir);

        Interpreter interp = freshInterp();
        interp.voidC.body.add(1, "unclaimed_value");
        ContainerPersistence.dumpAll(interp);

        Interpreter interp2 = freshInterp();
        long contentCount = interp2.voidC.body.stream()
            .filter(o -> !(o instanceof Parser.Stmt.Expression))
            .count();
        eq("VOID content items = 1", 1L, contentCount);
    }

    /** NON (cup) round-trip. */
    private static void testNonRoundTrip() throws IOException {
        System.out.println("--- NON round-trip ---");
        Path dir = Files.createTempDirectory("pcb_persist_non_");
        setWorkDir(dir);

        Interpreter interp = freshInterp();
        interp.non.body.add("staged");
        ContainerPersistence.dumpAll(interp);

        Interpreter interp2 = freshInterp();
        check("NON has 1 item", interp2.non.body.size() == 1);
        eq("NON[0]", "staged", interp2.non.body.get(0));
    }

    /** LIMBO (puc) round-trip. */
    private static void testLimboRoundTrip() throws IOException {
        System.out.println("--- LIMBO round-trip ---");
        Path dir = Files.createTempDirectory("pcb_persist_limbo_");
        setWorkDir(dir);

        Interpreter interp = freshInterp();
        interp.limbo.body.add("waiting");
        interp.limbo.body.add(false);
        ContainerPersistence.dumpAll(interp);

        Interpreter interp2 = freshInterp();
        check("LIMBO has 2 items", interp2.limbo.body.size() == 2);
        eq("LIMBO[0]", "waiting",      interp2.limbo.body.get(0));
        eq("LIMBO[1]", Boolean.FALSE,  interp2.limbo.body.get(1));
    }

    /** Invalid BIN file (missing close bracket) causes RuntimeError on load. */
    private static void testInvalidBinFileRefused() throws IOException {
        System.out.println("--- invalid BIN file refused ---");
        Path dir = Files.createTempDirectory("pcb_persist_invalid_");
        setWorkDir(dir);

        writeFile(dir, "bin.box", "not a box at all");  // no brackets

        boolean threw = false;
        try { new Interpreter(); } catch (RuntimeError e) { threw = true; }
        check("RuntimeError on invalid BIN", threw);
    }

    /** Invalid TRASH file (wrong bracket type) refused. */
    private static void testWrongBracketTypeRefused() throws IOException {
        System.out.println("--- wrong bracket type in TRASH refused ---");
        Path dir = Files.createTempDirectory("pcb_persist_wrongbracket_");
        setWorkDir(dir);

        // Write all valid defaults first
        new Interpreter();
        // Overwrite trash.pkt with square brackets — wrong type for pkt
        writeFile(dir, "trash.pkt", "[", "]");

        boolean threw = false;
        try { new Interpreter(); } catch (RuntimeError e) { threw = true; }
        check("RuntimeError on wrong bracket in TRASH", threw);
    }

    /** Null round-trips correctly. */
    private static void testNullRoundTrip() throws IOException {
        System.out.println("--- null serializes and deserializes ---");
        Path dir = Files.createTempDirectory("pcb_persist_null_");
        setWorkDir(dir);

        Interpreter interp = freshInterp();
        interp.bin.body.add(null);
        ContainerPersistence.dumpAll(interp);

        Interpreter interp2 = freshInterp();
        check("BIN has 1 item",    interp2.bin.body.size() == 1);
        check("BIN[0] is null",    interp2.bin.body.get(0) == null);
    }

    /** Non-serializable items (e.g. BoxInstance) silently dropped. */
    private static void testNonSerializableDropped() throws IOException {
        System.out.println("--- non-serializable items dropped ---");
        Path dir = Files.createTempDirectory("pcb_persist_drop_");
        setWorkDir(dir);

        Interpreter interp = freshInterp();
        interp.bin.body.add("before");
        interp.bin.body.add(new BoxInstance(null, new java.util.ArrayList<>(), null, interp));
        interp.bin.body.add("after");
        ContainerPersistence.dumpAll(interp);

        Interpreter interp2 = freshInterp();
        eq("BIN has 2 items (BoxInstance dropped)", 2, interp2.bin.body.size());
        eq("BIN[0]", "before", interp2.bin.body.get(0));
        eq("BIN[1]", "after",  interp2.bin.body.get(1));
    }

    // ---- Entry point --------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== ContainerPersistenceTest ===\n");

        run("default files created",     ContainerPersistenceTest::testDefaultFilesCreated);
        run("BIN round-trip",            ContainerPersistenceTest::testBinRoundTrip);
        run("GAP round-trip",            ContainerPersistenceTest::testGapRoundTrip);
        run("TRASH round-trip",          ContainerPersistenceTest::testTrashRoundTrip);
        run("VOID round-trip",           ContainerPersistenceTest::testVoidRoundTrip);
        run("NON round-trip",            ContainerPersistenceTest::testNonRoundTrip);
        run("LIMBO round-trip",          ContainerPersistenceTest::testLimboRoundTrip);
        run("invalid BIN refused",       ContainerPersistenceTest::testInvalidBinFileRefused);
        run("wrong bracket refused",     ContainerPersistenceTest::testWrongBracketTypeRefused);
        run("null round-trip",           ContainerPersistenceTest::testNullRoundTrip);
        run("non-serializable dropped",  ContainerPersistenceTest::testNonSerializableDropped);

        System.out.println("\n=== " + passed + " passed,  " + failed + " failed ===");
        System.exit(failed == 0 ? 0 : 1);
    }

    @FunctionalInterface interface ThrowingRunnable { void run() throws Exception; }

    private static void run(String name, ThrowingRunnable test) {
        try { test.run(); }
        catch (Exception e) {
            System.out.println("  ERROR in " + name + ": " + e);
            e.printStackTrace(System.out);
            failed++;
        }
    }
}
