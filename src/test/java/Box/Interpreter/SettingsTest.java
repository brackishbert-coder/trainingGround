package Box.Interpreter;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;
import Parser.Stmt;

/**
 * Tests for Layer 4 settings parsing.
 *
 * Settings are presence-only flags detected by the interpreter when a Get/Teg
 * expression has one side = a settings sentinel and the other = a container alias.
 *
 * Recognized patterns (both orderings; all container alias spellings):
 *   CONTAINER.dumpPriority   dumpPriority.CONTAINER
 *   CONTAINER.ytirorpPmud    ytirorpPmud.CONTAINER
 *   TRASH.THREAD             THREAD.TRASH    (and HSART/DAERHT variants)
 *   VOID.THREAD              THREAD.VOID     (and DIOV/DAERHT variants)
 *   DYNAMICSAFTY.TRASH       TRASH.DYNAMICSAFTY  (and all alias variants)
 *   YTFASCIMANYD.VOID        etc.
 */
public class SettingsTest {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean cond) {
        if (cond) { System.out.println("  PASS  " + name); passed++; }
        else      { System.out.println("  FAIL  " + name); failed++; }
    }

    // ---- Helpers ------------------------------------------------------------

    private static Path tmpDir;

    private static Interpreter freshInterp() throws IOException {
        Path dir = Files.createTempDirectory("pcb_settings_test_");
        System.setProperty("user.dir", dir.toString());
        return new Interpreter();
    }

    /** Simulate evaluating CONTAINER.SETTING by calling visitGetExpr directly. */
    private static void evalGet(Interpreter interp, Object containerObj, String settingName) {
        // Build: GET(containerVar, settingToken)
        Token settingTok = new Token(TokenType.IDENTIFIER, settingName, null, null, null, 0,0,0,0);
        // We need a Get expr whose object evaluates to containerObj.
        // Easiest: put containerObj in a temp var and use a Variable expr.
        Token varTok = new Token(TokenType.IDENTIFIER, "__tmp__", null, null, null, 0,0,0,0);
        interp.globals.define("__tmp__", (Token) null, containerObj);
        Expr.Variable varExpr = new Expr.Variable(varTok);
        Expr.Get getExpr = new Expr.Get(varExpr, settingTok);
        interp.evaluate(getExpr);
    }

    /** Simulate evaluating SETTING.CONTAINER by calling visitGetExpr. */
    private static void evalSettingDotContainer(Interpreter interp, String settingName, Object containerObj) {
        Token containerTok = new Token(TokenType.IDENTIFIER, "__cntr__", null, null, null, 0,0,0,0);
        interp.globals.define("__cntr__", (Token) null, containerObj);

        Token settingVarTok = new Token(TokenType.IDENTIFIER, "__stg__", null, null, null, 0,0,0,0);
        Object settingVal = interp.globals.get(
            new Token(TokenType.IDENTIFIER, settingName, null, null, null, 0,0,0,0), false);
        interp.globals.define("__stg__", (Token) null, settingVal);

        Expr.Variable settingVar  = new Expr.Variable(settingVarTok);
        Token cntrNameTok = new Token(TokenType.IDENTIFIER, "__cntr__", null, null, null, 0,0,0,0);
        // Actually we want Get(settingVar, containerName-as-token) — but the name won't match a
        // canonical container alias this way. Instead, register the container under its real alias.
        // Use the alias directly: evaluate setting.__cntr__ where __cntr__ resolves to alias name.
        // Simpler: just inject via the alias directly.
        // We'll use the container alias as the property name in the Get expression.
        // This doesn't work because get(name) would look up "alias" as a property on the sentinel.
        // Use a different approach — inject via setting sentinel dot container-alias-token.
        // We need: eval( Get(settingVar, aliasToken) ) where settingVar -> settingToken sentinel.
        // That means the name of the Get is the alias string. Let's rebuild properly:
        String alias = aliasFor(interp, containerObj);
        Token aliasTok = new Token(TokenType.IDENTIFIER, alias, null, null, null, 0,0,0,0);
        Expr.Get getExpr = new Expr.Get(settingVar, aliasTok);
        interp.evaluate(getExpr);
    }

    private static String aliasFor(Interpreter interp, Object obj) {
        if (obj == interp.bin)   return "BIN";
        if (obj == interp.gap)   return "GAP";
        if (obj == interp.non)   return "NON";
        if (obj == interp.limbo) return "LIMBO";
        if (obj == interp.trash) return "TRASH";
        if (obj == interp.voidC) return "VOID";
        return "UNKNOWN";
    }

    // ---- dumpPriority tests -------------------------------------------------

    private static void testDumpPriorityContainerDotSetting() throws IOException {
        System.out.println("--- CONTAINER.dumpPriority activates flag ---");
        Interpreter i = freshInterp();
        evalGet(i, i.bin,   "dumpPriority");   check("BIN.dumpPriority",   i.activeDumpPriority.contains("BIN"));
        evalGet(i, i.gap,   "ytirorpPmud");    check("GAP.ytirorpPmud",    i.activeDumpPriority.contains("GAP"));
        evalGet(i, i.non,   "dumpPriority");   check("NON.dumpPriority",   i.activeDumpPriority.contains("NON"));
        evalGet(i, i.limbo, "ytirorpPmud");    check("LIMBO.ytirorpPmud",  i.activeDumpPriority.contains("LIMBO"));
        evalGet(i, i.trash, "dumpPriority");   check("TRASH.dumpPriority", i.activeDumpPriority.contains("TRASH"));
        evalGet(i, i.voidC, "ytirorpPmud");    check("VOID.ytirorpPmud",   i.activeDumpPriority.contains("VOID"));
    }

    private static void testDumpPrioritySettingDotContainer() throws IOException {
        System.out.println("--- dumpPriority.CONTAINER activates flag ---");
        Interpreter i = freshInterp();
        i.setForward(true);
        evalSettingDotContainer(i, "dumpPriority", i.bin);   check("dumpPriority.BIN",   i.activeDumpPriority.contains("BIN"));
        evalSettingDotContainer(i, "ytirorpPmud",  i.trash); check("ytirorpPmud.TRASH",  i.activeDumpPriority.contains("TRASH"));
        evalSettingDotContainer(i, "dumpPriority", i.voidC); check("dumpPriority.VOID",  i.activeDumpPriority.contains("VOID"));
        evalSettingDotContainer(i, "ytirorpPmud",  i.gap);   check("ytirorpPmud.GAP",    i.activeDumpPriority.contains("GAP"));
    }

    private static void testDumpPriorityMirrorAliases() throws IOException {
        System.out.println("--- mirror aliases activate same flag ---");
        Interpreter i = freshInterp();
        // Use NIB alias → same bin object
        evalGet(i, i.bin, "dumpPriority"); // BIN.dumpPriority
        check("NIB resolves to BIN flag", i.activeDumpPriority.contains("BIN"));
        // Use HSART alias → same trash object
        evalGet(i, i.trash, "dumpPriority");
        check("HSART resolves to TRASH flag", i.activeDumpPriority.contains("TRASH"));
    }

    // ---- THREAD tests -------------------------------------------------------

    private static void testThreadFlagTrash() throws IOException {
        System.out.println("--- TRASH.THREAD activates trashThreaded ---");
        Interpreter i = freshInterp();
        evalGet(i, i.trash, "THREAD");  check("TRASH.THREAD",   i.trashThreaded);
        check("VOID not affected",      !i.voidThreaded);
    }

    private static void testThreadFlagVoid() throws IOException {
        System.out.println("--- VOID.DAERHT activates voidThreaded ---");
        Interpreter i = freshInterp();
        evalGet(i, i.voidC, "DAERHT");  check("VOID.DAERHT",  i.voidThreaded);
        check("TRASH not affected",     !i.trashThreaded);
    }

    private static void testThreadSettingDotContainer() throws IOException {
        System.out.println("--- THREAD.TRASH activates trashThreaded ---");
        Interpreter i = freshInterp();
        i.setForward(true);
        evalSettingDotContainer(i, "THREAD", i.trash);  check("THREAD.TRASH",  i.trashThreaded);
        evalSettingDotContainer(i, "DAERHT", i.voidC);  check("DAERHT.VOID",   i.voidThreaded);
    }

    private static void testThreadOnlyForTrashAndVoid() throws IOException {
        System.out.println("--- THREAD on non-threaded containers is ignored ---");
        Interpreter i = freshInterp();
        evalGet(i, i.bin,   "THREAD");
        evalGet(i, i.gap,   "THREAD");
        evalGet(i, i.non,   "THREAD");
        evalGet(i, i.limbo, "THREAD");
        check("trashThreaded unchanged", !i.trashThreaded);
        check("voidThreaded unchanged",  !i.voidThreaded);
    }

    // ---- DYNAMICSAFTY tests -------------------------------------------------

    private static void testDynamicSaftyActivated() throws IOException {
        System.out.println("--- DYNAMICSAFTY.TRASH activates dynamicPolicy ---");
        Interpreter i = freshInterp();
        evalGet(i, i.trash, "DYNAMICSAFTY"); check("TRASH.DYNAMICSAFTY sets dynamicPolicy", i.dynamicPolicy);
    }

    private static void testDynamicSaftySettingDotContainer() throws IOException {
        System.out.println("--- DYNAMICSAFTY.VOID (setting.container form) ---");
        Interpreter i = freshInterp();
        i.setForward(true);
        evalSettingDotContainer(i, "DYNAMICSAFTY", i.voidC);  check("DYNAMICSAFTY.VOID", i.dynamicPolicy);
    }

    private static void testDynamicSaftyMirrorSpelling() throws IOException {
        System.out.println("--- YTFASCIMANYD spelling works ---");
        Interpreter i = freshInterp();
        evalGet(i, i.voidC, "YTFASCIMANYD"); check("VOID.YTFASCIMANYD", i.dynamicPolicy);
    }

    // ---- addToErrorSink routing ---------------------------------------------

    private static void testAddToErrorSinkDefaultsToBin() throws IOException {
        System.out.println("--- addToErrorSink routes to BIN by default ---");
        Interpreter i = freshInterp();
        i.addToErrorSink("err1");
        check("BIN has 1 item",   i.bin.body.size() == 1);
        check("TRASH is empty",   contentItems(i.trash) == 0);
    }

    private static void testAddToErrorSinkRoutesToDynamic() throws IOException {
        System.out.println("--- addToErrorSink routes to TRASH when dynamicPolicy=true ---");
        Interpreter i = freshInterp();
        i.dynamicPolicy = true;
        i.addToErrorSink("err2");
        check("TRASH has 1 content item", contentItems(i.trash) == 1);
        check("BIN is empty",             i.bin.body.isEmpty());
    }

    private static void testAddToUnclaimedDefaultsToGap() throws IOException {
        System.out.println("--- addToUnclaimed routes to GAP by default ---");
        Interpreter i = freshInterp();
        i.addToUnclaimed("uncl");
        check("GAP has 1 item",  i.gap.body.size() == 1);
        check("VOID is empty",   contentItems(i.voidC) == 0);
    }

    private static void testAddToUnclaimedRoutesToVoid() throws IOException {
        System.out.println("--- addToUnclaimed routes to VOID when dynamicPolicy=true ---");
        Interpreter i = freshInterp();
        i.dynamicPolicy = true;
        i.addToUnclaimed("uncl2");
        check("VOID has 1 content item", contentItems(i.voidC) == 1);
        check("GAP is empty",            i.gap.body.isEmpty());
    }

    // Count non-structural-bracket body items in a PocketInstance or TkpInstance.
    private static long contentItems(Object container) {
        List<Object> body = null;
        if (container instanceof PocketInstance) body = ((PocketInstance) container).body;
        else if (container instanceof TkpInstance) body = ((TkpInstance) container).body;
        if (body == null) return 0;
        return body.stream().filter(o -> !(o instanceof Parser.Stmt.Expression)).count();
    }

    // ---- Entry point --------------------------------------------------------

    public static void main(String[] args) {
        System.out.println("=== SettingsTest ===\n");

        run("dumpPriority CONTAINER.SETTING", SettingsTest::testDumpPriorityContainerDotSetting);
        run("dumpPriority SETTING.CONTAINER", SettingsTest::testDumpPrioritySettingDotContainer);
        run("dumpPriority mirror aliases",    SettingsTest::testDumpPriorityMirrorAliases);
        run("THREAD flag TRASH",              SettingsTest::testThreadFlagTrash);
        run("THREAD flag VOID",               SettingsTest::testThreadFlagVoid);
        run("THREAD SETTING.CONTAINER",       SettingsTest::testThreadSettingDotContainer);
        run("THREAD ignored for BIN/GAP/NON/LIMBO", SettingsTest::testThreadOnlyForTrashAndVoid);
        run("DYNAMICSAFTY activated",         SettingsTest::testDynamicSaftyActivated);
        run("DYNAMICSAFTY SETTING.CONTAINER", SettingsTest::testDynamicSaftySettingDotContainer);
        run("YTFASCIMANYD mirror spelling",   SettingsTest::testDynamicSaftyMirrorSpelling);
        run("addToErrorSink → BIN default",   SettingsTest::testAddToErrorSinkDefaultsToBin);
        run("addToErrorSink → TRASH dynamic", SettingsTest::testAddToErrorSinkRoutesToDynamic);
        run("addToUnclaimed → GAP default",   SettingsTest::testAddToUnclaimedDefaultsToGap);
        run("addToUnclaimed → VOID dynamic",  SettingsTest::testAddToUnclaimedRoutesToVoid);

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
