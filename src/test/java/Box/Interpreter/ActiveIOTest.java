package Box.Interpreter;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Tests for Layer 5 active I/O mode.
 *
 * dumpPriority active: any mutation to the container immediately re-dumps its
 * file to disk without waiting for JVM shutdown.
 *
 * THREAD flag: when dumpPriority is also active, the dump runs on a dedicated
 * worker thread (one per threadable container: TRASH and VOID).
 */
public class ActiveIOTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean cond) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else      { System.out.println("  FAIL  " + name); failed++; }
    }

    // ---- Helpers ------------------------------------------------------------

    private static Interpreter freshInterp() throws IOException {
        Path dir = Files.createTempDirectory("pcb_activeio_test_");
        System.setProperty("user.dir", dir.toString());
        return new Interpreter();
    }

    private static List<String> readInner(Interpreter interp, String filename) throws IOException {
        Path path = Paths.get(System.getProperty("user.dir"), filename);
        List<String> all = Files.readAllLines(path);
        // Strip first and last (bracket) lines, collect non-blank
        List<String> inner = new ArrayList<>();
        for (int i = 1; i < all.size() - 1; i++) {
            String s = all.get(i).strip();
            if (!s.isEmpty()) inner.add(s);
        }
        return inner;
    }

    /** Activate dumpPriority for the given container via dot-access expression. */
    private static void activateDumpPriority(Interpreter interp, Object container) {
        Box.Token.Token settingTok = new Box.Token.Token(
            Box.Token.TokenType.IDENTIFIER, "dumpPriority", null, null, null, 0,0,0,0);
        Box.Token.Token varTok = new Box.Token.Token(
            Box.Token.TokenType.IDENTIFIER, "__tmp__", null, null, null, 0,0,0,0);
        interp.globals.define("__tmp__", (Box.Token.Token) null, container);
        Parser.Expr.Variable varExpr = new Parser.Expr.Variable(varTok);
        Parser.Expr.Get getExpr = new Parser.Expr.Get(varExpr, settingTok);
        interp.evaluate(getExpr);
    }

    /** Activate THREAD flag for the given container. */
    private static void activateThread(Interpreter interp, Object container) {
        Box.Token.Token settingTok = new Box.Token.Token(
            Box.Token.TokenType.IDENTIFIER, "THREAD", null, null, null, 0,0,0,0);
        Box.Token.Token varTok = new Box.Token.Token(
            Box.Token.TokenType.IDENTIFIER, "__tmp2__", null, null, null, 0,0,0,0);
        interp.globals.define("__tmp2__", (Box.Token.Token) null, container);
        Parser.Expr.Variable varExpr = new Parser.Expr.Variable(varTok);
        Parser.Expr.Get getExpr = new Parser.Expr.Get(varExpr, settingTok);
        interp.evaluate(getExpr);
    }

    // ---- Synchronous active I/O (BIN) ---------------------------------------

    private static void testBinDumpPriorityFlushesOnAdd() throws IOException {
        System.out.println("--- BIN.dumpPriority: file updated immediately on addToErrorSink ---");
        Interpreter i = freshInterp();
        activateDumpPriority(i, i.bin);
        check("activeDumpPriority contains BIN", i.activeDumpPriority.contains("BIN"));

        i.addToErrorSink("hello");
        List<String> inner = readInner(i, "bin.box");
        check("file has 1 item",      inner.size() == 1);
        check("item value is hello",  inner.get(0).equals("hello"));
    }

    private static void testBinMultipleFlushes() throws IOException {
        System.out.println("--- BIN.dumpPriority: file reflects each addition ---");
        Interpreter i = freshInterp();
        activateDumpPriority(i, i.bin);

        i.addToErrorSink("a");
        check("after 1st add: 1 item", readInner(i, "bin.box").size() == 1);
        i.addToErrorSink("b");
        check("after 2nd add: 2 items", readInner(i, "bin.box").size() == 2);
        i.addToErrorSink(42.0);
        List<String> inner = readInner(i, "bin.box");
        check("after 3rd add: 3 items",        inner.size() == 3);
        check("third item serialized as 42.0", inner.get(2).equals("42.0"));
    }

    private static void testNoDumpPriorityNoImmediateFlush() throws IOException {
        System.out.println("--- No dumpPriority: file NOT updated until shutdown ---");
        Interpreter i = freshInterp();
        // Don't activate dumpPriority
        i.addToErrorSink("should-not-appear-yet");
        List<String> inner = readInner(i, "bin.box");
        check("file still empty before shutdown", inner.isEmpty());
    }

    // ---- Synchronous active I/O (GAP) ---------------------------------------

    private static void testGapDumpPriorityFlushesOnAdd() throws IOException {
        System.out.println("--- GAP.dumpPriority: file updated immediately on addToUnclaimed ---");
        Interpreter i = freshInterp();
        activateDumpPriority(i, i.gap);

        i.addToUnclaimed("gapval");
        List<String> inner = readInner(i, "gap.xob");
        check("gap file has 1 item",  inner.size() == 1);
        check("item value is gapval", inner.get(0).equals("gapval"));
    }

    // ---- Synchronous active I/O (NON / LIMBO) -------------------------------

    private static void testNonDumpPriorityFlushesOnAdd() throws IOException {
        System.out.println("--- NON.dumpPriority: file updated immediately on body.add ---");
        Interpreter i = freshInterp();
        activateDumpPriority(i, i.non);

        i.non.body.add("staged");
        i.notifyContainerModified(i.non);
        List<String> inner = readInner(i, "non.cup");
        check("non file has 1 item",   inner.size() == 1);
        check("item value is staged",  inner.get(0).equals("staged"));
    }

    private static void testLimboDumpPriorityFlushesOnAdd() throws IOException {
        System.out.println("--- LIMBO.dumpPriority: file updated immediately on body.add ---");
        Interpreter i = freshInterp();
        activateDumpPriority(i, i.limbo);

        i.limbo.body.add("limboval");
        i.notifyContainerModified(i.limbo);
        List<String> inner = readInner(i, "limbo.puc");
        check("limbo file has 1 item",    inner.size() == 1);
        check("item value is limboval",   inner.get(0).equals("limboval"));
    }

    // ---- Dynamic policy + dumpPriority (TRASH / VOID synchronous) -----------

    private static void testTrashDumpPrioritySync() throws IOException {
        System.out.println("--- TRASH.dumpPriority (no THREAD): synchronous flush ---");
        Interpreter i = freshInterp();
        i.dynamicPolicy = true;
        activateDumpPriority(i, i.trash);
        check("trashThreaded is false", !i.trashThreaded);

        i.addToErrorSink("trashval");
        List<String> inner = readInner(i, "trash.pkt");
        check("trash file has 1 item",     inner.size() == 1);
        check("item value is trashval",    inner.get(0).equals("trashval"));
    }

    private static void testVoidDumpPrioritySync() throws IOException {
        System.out.println("--- VOID.dumpPriority (no THREAD): synchronous flush ---");
        Interpreter i = freshInterp();
        i.dynamicPolicy = true;
        activateDumpPriority(i, i.voidC);
        check("voidThreaded is false", !i.voidThreaded);

        i.addToUnclaimed("voidval");
        List<String> inner = readInner(i, "void.tkp");
        check("void file has 1 item",   inner.size() == 1);
        check("item value is voidval",  inner.get(0).equals("voidval"));
    }

    // ---- Threaded active I/O (TRASH + THREAD + dumpPriority) ----------------

    private static void testTrashThreadedFlush() throws IOException {
        System.out.println("--- TRASH.THREAD + dumpPriority: flush via worker thread ---");
        Interpreter i = freshInterp();
        i.dynamicPolicy = true;
        activateThread(i, i.trash);
        activateDumpPriority(i, i.trash);
        check("trashThreaded is true", i.trashThreaded);

        i.addToErrorSink("threaded1");
        i.addToErrorSink("threaded2");
        i.awaitActiveWorkers();

        List<String> inner = readInner(i, "trash.pkt");
        check("trash file has 2 items after await",   inner.size() == 2);
        check("first item is threaded1",              inner.get(0).equals("threaded1"));
        check("second item is threaded2",             inner.get(1).equals("threaded2"));
    }

    private static void testVoidThreadedFlush() throws IOException {
        System.out.println("--- VOID.THREAD + dumpPriority: flush via worker thread ---");
        Interpreter i = freshInterp();
        i.dynamicPolicy = true;
        activateThread(i, i.voidC);
        activateDumpPriority(i, i.voidC);
        check("voidThreaded is true", i.voidThreaded);

        i.addToUnclaimed("vt1");
        i.addToUnclaimed("vt2");
        i.addToUnclaimed("vt3");
        i.awaitActiveWorkers();

        List<String> inner = readInner(i, "void.tkp");
        check("void file has 3 items after await", inner.size() == 3);
        check("first item is vt1",                 inner.get(0).equals("vt1"));
        check("last item is vt3",                  inner.get(2).equals("vt3"));
    }

    private static void testThreadWithoutDumpPriorityDoesNotFlush() throws IOException {
        System.out.println("--- THREAD without dumpPriority: no active I/O ---");
        Interpreter i = freshInterp();
        i.dynamicPolicy = true;
        activateThread(i, i.trash);
        // No dumpPriority activated
        i.addToErrorSink("no-flush");
        i.awaitActiveWorkers();
        List<String> inner = readInner(i, "trash.pkt");
        check("trash file empty without dumpPriority", inner.isEmpty());
    }

    // ---- Null / non-serializable items skip serialization -------------------

    private static void testNullFlushedAsNull() throws IOException {
        System.out.println("--- dumpPriority: null serialized as 'null' ---");
        Interpreter i = freshInterp();
        activateDumpPriority(i, i.bin);
        i.addToErrorSink(null);
        List<String> inner = readInner(i, "bin.box");
        check("file has 1 item",          inner.size() == 1);
        check("null serialized as null",  inner.get(0).equals("null"));
    }

    private static void testNonSerializableDropped() throws IOException {
        System.out.println("--- dumpPriority: non-serializable item dropped from file ---");
        Interpreter i = freshInterp();
        activateDumpPriority(i, i.bin);
        i.bin.body.add(new Object()); // Java object — not serializable
        i.notifyContainerModified(i.bin);
        List<String> inner = readInner(i, "bin.box");
        check("file is empty after dropping non-serializable", inner.isEmpty());
    }

    // ---- awaitActiveWorkers is a no-op when no workers started --------------

    private static void testAwaitWorkersNoOp() throws IOException {
        System.out.println("--- awaitActiveWorkers is safe when no workers created ---");
        Interpreter i = freshInterp();
        i.awaitActiveWorkers(); // must not throw
        check("no exception thrown", true);
    }

    // ---- Entry point --------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== ActiveIOTest ===\n");

        run("BIN dumpPriority flushes on add",         ActiveIOTest::testBinDumpPriorityFlushesOnAdd);
        run("BIN dumpPriority multiple flushes",       ActiveIOTest::testBinMultipleFlushes);
        run("No dumpPriority no immediate flush",      ActiveIOTest::testNoDumpPriorityNoImmediateFlush);
        run("GAP dumpPriority flushes on add",         ActiveIOTest::testGapDumpPriorityFlushesOnAdd);
        run("NON dumpPriority flushes on add",         ActiveIOTest::testNonDumpPriorityFlushesOnAdd);
        run("LIMBO dumpPriority flushes on add",       ActiveIOTest::testLimboDumpPriorityFlushesOnAdd);
        run("TRASH dumpPriority sync (no THREAD)",     ActiveIOTest::testTrashDumpPrioritySync);
        run("VOID dumpPriority sync (no THREAD)",      ActiveIOTest::testVoidDumpPrioritySync);
        run("TRASH THREAD + dumpPriority async flush", ActiveIOTest::testTrashThreadedFlush);
        run("VOID THREAD + dumpPriority async flush",  ActiveIOTest::testVoidThreadedFlush);
        run("THREAD without dumpPriority no flush",    ActiveIOTest::testThreadWithoutDumpPriorityDoesNotFlush);
        run("null serialized as null",                 ActiveIOTest::testNullFlushedAsNull);
        run("non-serializable dropped",                ActiveIOTest::testNonSerializableDropped);
        run("awaitActiveWorkers no-op",                ActiveIOTest::testAwaitWorkersNoOp);

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
