package Box.Interpreter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

import Box.Token.Token;
import Box.Token.TokenType;
import Parser.Expr;
import Parser.Stmt;

/**
 * Batch-mode persistence for the six system containers.
 *
 * loadAll()  — called at interpreter startup: validate files, create defaults if
 *              missing, populate in-memory bodies from file contents.
 * dumpAll()  — called at interpreter shutdown: serialize in-memory bodies back
 *              to files.
 *
 * File layout:
 *   BIN  → bin.box    [ ... ]   (box body, flat)
 *   GAP  → gap.xob    [ ... ]   (xob body, flat)
 *   NON  → non.cup    { ... }   (cup body, flat — no bracket stmts needed)
 *   LIMBO→ limbo.puc  { ... }   (puc body, flat — no bracket stmts needed)
 *   TRASH→ trash.pkt  ( ... )   (pkt body — structural bracket stmts injected)
 *   VOID → void.tkp   ( ... )   (tkp body — structural bracket stmts injected)
 *
 * Serialization: String, Double, Boolean, null only.
 * Anything else (Java objects, closures) is silently dropped.
 */
public class ContainerPersistence {

    // -------------------------------------------------------------------------
    // Container descriptors
    // -------------------------------------------------------------------------

    private enum Kind { BOX, XOB, CUP, PUC, PKT, TKP }

    private static final class Def {
        final String name;
        final String file;
        final char   open;
        final char   close;
        final Kind   kind;
        Def(String n, String f, char o, char c, Kind k) {
            name = n; file = f; open = o; close = c; kind = k;
        }
    }

    private static final Def[] DEFS = {
        new Def("BIN",   "bin.box",   '[', ']', Kind.BOX),
        new Def("GAP",   "gap.xob",   '[', ']', Kind.XOB),
        new Def("NON",   "non.cup",   '{', '}', Kind.CUP),
        new Def("LIMBO", "limbo.puc", '{', '}', Kind.PUC),
        new Def("TRASH", "trash.pkt", '(', ')', Kind.PKT),
        new Def("VOID",  "void.tkp",  '(', ')', Kind.TKP),
    };

    // -------------------------------------------------------------------------
    // Load
    // -------------------------------------------------------------------------

    public static void loadAll(Interpreter interp) {
        String dir = System.getProperty("user.dir");
        for (Def def : DEFS) {
            Path path = Paths.get(dir, def.file);
            List<Object> items;
            if (!Files.exists(path)) {
                writeDefault(path, def);
                items = Collections.emptyList();
            } else {
                items = parseItems(validateAndReadInner(path, def));
            }
            populateBody(interp, def, items);
        }
    }

    private static void writeDefault(Path path, Def def) {
        try {
            Files.write(path,
                Arrays.asList(String.valueOf(def.open), String.valueOf(def.close)),
                StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    // Reads the file, validates the bracket markers, returns the inner lines.
    // Throws RuntimeError if the file is structurally invalid.
    private static List<String> validateAndReadInner(Path path, Def def) {
        List<String> all;
        try {
            all = Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeError(null, def.name + " file unreadable: " + e.getMessage());
        }

        // First and last non-blank lines must be the opening and closing brackets.
        int first = -1, last = -1;
        for (int i = 0; i < all.size(); i++) {
            if (!all.get(i).isBlank()) { first = i; break; }
        }
        for (int i = all.size() - 1; i >= 0; i--) {
            if (!all.get(i).isBlank()) { last = i; break; }
        }

        if (first == -1 || first == last) {
            throw new RuntimeError(null,
                def.name + " file invalid: missing open and close brackets "
                + def.open + " " + def.close);
        }
        if (all.get(first).strip().charAt(0) != def.open) {
            throw new RuntimeError(null,
                def.name + " file invalid: expected '" + def.open + "' on first line");
        }
        if (all.get(last).strip().charAt(0) != def.close) {
            throw new RuntimeError(null,
                def.name + " file invalid: expected '" + def.close + "' on last line");
        }

        return all.subList(first + 1, last);
    }

    private static List<Object> parseItems(List<String> lines) {
        List<Object> items = new ArrayList<>();
        for (String line : lines) {
            String s = line.strip();
            if (s.isEmpty()) continue;
            items.add(parseValue(s));
        }
        return items;
    }

    static Object parseValue(String s) {
        if (s.equals("null"))  return null;
        if (s.equals("true"))  return Boolean.TRUE;
        if (s.equals("false")) return Boolean.FALSE;
        try { return Double.parseDouble(s); } catch (NumberFormatException ignored) {}
        return s;
    }

    private static void populateBody(Interpreter interp, Def def, List<Object> items) {
        List<Object> body = bodyFor(interp, def);
        if (body == null || items.isEmpty()) {
            // For PKT/TKP, still inject structural brackets even if no content items.
            if ((def.kind == Kind.PKT || def.kind == Kind.TKP) && body != null) {
                injectPktBrackets(body, items);
            }
            return;
        }
        switch (def.kind) {
            case PKT: case TKP:
                injectPktBrackets(body, items);
                break;
            default:
                body.addAll(items);
                break;
        }
    }

    // Inject ( items... ) structural bracket statements into a pkt/tkp body.
    private static void injectPktBrackets(List<Object> body, List<Object> items) {
        Token open  = new Token(TokenType.OPENPAREN,   "(", null, null, null, -1, -1, -1, -1);
        Token close = new Token(TokenType.CLOSEDPAREN, ")", null, null, null, -1, -1, -1, -1);
        body.add(new Stmt.Expression(new Expr.PocketOpen(open),   null));
        body.addAll(items);
        body.add(new Stmt.Expression(new Expr.PocketClosed(close), null));
    }

    // -------------------------------------------------------------------------
    // Dump
    // -------------------------------------------------------------------------

    public static void dumpAll(Interpreter interp) {
        for (Def def : DEFS) dumpDef(interp, def);
    }

    /** Immediately flush one container to its file by canonical name (e.g. "BIN"). */
    public static void dumpOne(Interpreter interp, String canonicalName) {
        for (Def def : DEFS) {
            if (def.name.equals(canonicalName)) { dumpDef(interp, def); return; }
        }
    }

    /** Flush a pre-captured body snapshot to the file for the named container (thread-safe path). */
    static void dumpSnapshot(List<Object> snapshot, String canonicalName) {
        for (Def def : DEFS) {
            if (def.name.equals(canonicalName)) { dumpBodyToFile(def, snapshot); return; }
        }
    }

    private static void dumpDef(Interpreter interp, Def def) {
        List<Object> body = bodyFor(interp, def);
        if (body == null) return;
        dumpBodyToFile(def, body);
    }

    static void dumpBodyToFile(Def def, List<Object> body) {
        String dir = System.getProperty("user.dir");
        Path path = Paths.get(dir, def.file);
        List<String> lines = new ArrayList<>();
        lines.add(String.valueOf(def.open));
        for (Object item : body) {
            if (isStructuralBracket(item)) continue;
            String s = serialize(item);
            if (s != null) lines.add(s);
        }
        lines.add(String.valueOf(def.close));
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private static List<Object> bodyFor(Interpreter interp, Def def) {
        switch (def.name) {
            case "BIN":   return interp.bin.body;
            case "GAP":   return interp.gap.body;
            case "NON":   return interp.non.body;
            case "LIMBO": return interp.limbo.body;
            case "TRASH": return interp.trash.body;
            case "VOID":  return interp.voidC.body;
        }
        return null;
    }

    private static boolean isStructuralBracket(Object item) {
        if (!(item instanceof Stmt.Expression)) return false;
        Expr e = ((Stmt.Expression) item).expression;
        return e instanceof Expr.PocketOpen  || e instanceof Expr.PocketClosed
            || e instanceof Expr.CupOpen     || e instanceof Expr.CupClosed
            || e instanceof Expr.BoxOpen     || e instanceof Expr.BoxClosed;
    }

    private static String serialize(Object item) {
        if (item == null)               return "null";
        if (item instanceof String)     return (String) item;
        if (item instanceof Double)     return item.toString();
        if (item instanceof Boolean)    return item.toString();
        if (item instanceof Integer)    return item.toString();
        if (item instanceof Long)       return item.toString();
        return null;
    }
}
