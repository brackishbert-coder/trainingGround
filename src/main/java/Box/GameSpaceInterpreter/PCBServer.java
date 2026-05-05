package Box.GameSpaceInterpreter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * PCBServer
 *
 * Thin HTTP server wrapping SandBox / PCB runtime.
 * Exposes two endpoints for the Combined Seed Workbench:
 *
 *   POST /seeds  — load seed JSON objects into PCB environment
 *   POST /run    — execute PCB source, return output as JSON
 *
 * Start via: PCBServer.start(7070)
 * Stop  via: PCBServer.stop()
 *
 * No external dependencies — uses java.net / com.sun.net.httpserver (Java 21).
 */
public class PCBServer {

    // ── port ────────────────────────────────────────────────────────
    private static final int DEFAULT_PORT = 7070;

    // ── runtime state ───────────────────────────────────────────────
    private static HttpServer        httpServer;
    private static ByteArrayOutputStream baos     = new ByteArrayOutputStream();
    private static SandBox           sandbox;
    private static final ObjectMapper JSON        = new ObjectMapper();

    // seed storage — what was last loaded
    private static JsonNode          manifoldSeed  = null;
    private static JsonNode          grammarSeed   = null;
    private static JsonNode          traversalSeed = null;
    private static JsonNode          userSeed      = null;

    // ── start / stop ────────────────────────────────────────────────

    public static void start() throws IOException {
        start(DEFAULT_PORT);
    }

    public static void start(int port) throws IOException {
        // build sandbox with captured output stream
        baos    = new ByteArrayOutputStream();
        sandbox = new SandBox(baos, null, null, null, null);

        // build HTTP server
        httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        httpServer.createContext("/seeds",  new SeedsHandler());
        httpServer.createContext("/run",    new RunHandler());
        httpServer.createContext("/status", new StatusHandler());
        httpServer.setExecutor(Executors.newCachedThreadPool());
        httpServer.start();

        System.out.println("[PCBServer] listening on port " + port);
        System.out.println("[PCBServer] POST /seeds  — load seed objects");
        System.out.println("[PCBServer] POST /run    — execute PCB source");
        System.out.println("[PCBServer] GET  /status — health check");
    }

    public static void stop() {
        if (httpServer != null) {
            httpServer.stop(0);
            System.out.println("[PCBServer] stopped");
        }
    }

    // ── main (standalone) ───────────────────────────────────────────

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;
        start(port);
        System.out.println("[PCBServer] press Ctrl+C to stop");
        // keep alive
        Thread.currentThread().join();
    }

    // ════════════════════════════════════════════════════════════════
    // POST /seeds
    // Body: { "manifold": {...}, "grammar": {...}, "traversal": {...}, "user": {...} }
    // Response: { "ok": true, "manifest": { ... } }
    // ════════════════════════════════════════════════════════════════

    static class SeedsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
            if (!"POST".equals(ex.getRequestMethod())) { send(ex, 405, error("POST required")); return; }

            try {
                JsonNode body = JSON.readTree(ex.getRequestBody());

                manifoldSeed  = body.has("manifold")  ? body.get("manifold")  : null;
                grammarSeed   = body.has("grammar")   ? body.get("grammar")   : null;
                traversalSeed = body.has("traversal") ? body.get("traversal") : null;
                userSeed      = body.has("user")      ? body.get("user")      : null;

                // Build manifest — describe what PCB objects are available
                ObjectNode manifest = JSON.createObjectNode();

                if (manifoldSeed != null) {
                    manifest.set("manifold", describeNode("ManifoldSeedObject",
                        seedId(manifoldSeed, "manifoldSeedId"), manifoldSeed));
                }
                if (grammarSeed != null) {
                    manifest.set("grammar", describeNode("GrammarSeedObject",
                        seedId(grammarSeed, "grammarSeedId"), grammarSeed));
                }
                if (traversalSeed != null) {
                    manifest.set("traversal", describeNode("TraversalSeedObject",
                        seedId(traversalSeed, "traversalSeedId", "seedId"), traversalSeed));
                }
                if (userSeed != null) {
                    manifest.set("user", describeNode("UserDataObject",
                        seedId(userSeed, "projectId", "id"), userSeed));
                }

                ObjectNode resp = JSON.createObjectNode();
                resp.put("ok", true);
                resp.set("manifest", manifest);

                send(ex, 200, resp.toString());
                System.out.println("[PCBServer] /seeds loaded — "
                    + (manifoldSeed  != null ? "manifold " : "")
                    + (grammarSeed   != null ? "grammar "  : "")
                    + (traversalSeed != null ? "traversal ": "")
                    + (userSeed      != null ? "user"      : ""));

            } catch (Exception e) {
                send(ex, 400, error("failed to parse seeds: " + e.getMessage()));
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // POST /run
    // Body: { "source": "...", "direction": "fwd" }
    // Response: { "ok": true, "output": "...", "executionTime": 123 }
    // ════════════════════════════════════════════════════════════════

    static class RunHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            if ("OPTIONS".equals(ex.getRequestMethod())) { ex.sendResponseHeaders(204, -1); return; }
            if (!"POST".equals(ex.getRequestMethod())) { send(ex, 405, error("POST required")); return; }

            try {
                JsonNode body      = JSON.readTree(ex.getRequestBody());
                String   source    = body.has("source")    ? body.get("source").asText()    : "";
                String   direction = body.has("direction") ? body.get("direction").asText() : "fwd";

                if (source.isBlank()) { send(ex, 400, error("source is empty")); return; }

                // Inject seed variables as PCB preamble so they're available
                String preamble = buildPreamble();
                String fullSource = preamble + "\n" + source;

                // Capture output
                baos.reset();
                Box.Box.Box.resetHadError();

                long t0 = System.currentTimeMillis();
                boolean forward = !"bwd".equals(direction);

                // Redirect System.out to our baos temporarily
                PrintStream oldOut = System.out;
                PrintStream oldErr = System.err;
                PrintStream capture = new PrintStream(baos, true, StandardCharsets.UTF_8);
                System.setOut(capture);
                System.setErr(capture);

                try {
                    sandbox.runJson(fullSource, forward);
                } finally {
                    System.setOut(oldOut);
                    System.setErr(oldErr);
                }

                long elapsed = System.currentTimeMillis() - t0;
                String output = baos.toString(StandardCharsets.UTF_8);

                ObjectNode resp = JSON.createObjectNode();
                resp.put("ok", true);
                resp.put("output", output);
                resp.put("executionTime", elapsed);
                resp.put("direction", direction);
                resp.put("interfaceSeedId", "iface_" + Long.toString(System.currentTimeMillis(), 36));

                // Parse bindings from source
                resp.set("bindings", parseBindings(source));

                send(ex, 200, resp.toString());
                System.out.println("[PCBServer] /run — " + elapsed + "ms — " + direction);

            } catch (Exception e) {
                send(ex, 500, error("execution error: " + e.getMessage()));
                e.printStackTrace();
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    // GET /status
    // ════════════════════════════════════════════════════════════════

    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange ex) throws IOException {
            addCors(ex);
            ObjectNode resp = JSON.createObjectNode();
            resp.put("ok", true);
            resp.put("runtime", "PCBServer");
            resp.put("seedsLoaded",
                (manifoldSeed  != null ? 1 : 0) +
                (grammarSeed   != null ? 1 : 0) +
                (traversalSeed != null ? 1 : 0) +
                (userSeed      != null ? 1 : 0));
            resp.put("manifold",  manifoldSeed  != null);
            resp.put("grammar",   grammarSeed   != null);
            resp.put("traversal", traversalSeed != null);
            resp.put("user",      userSeed      != null);
            send(ex, 200, resp.toString());
        }
    }

    // ── helpers ─────────────────────────────────────────────────────

    /**
     * Build a PCB preamble that makes seed data available as named string variables.
     * Seeds are serialized as JSON strings assigned to box variables.
     * PCB programs can then reference manifoldSeedId, grammarSeedId, etc.
     */
    private static String buildPreamble() throws Exception {
        StringBuilder sb = new StringBuilder();

        if (manifoldSeed != null) {
            String id = seedId(manifoldSeed, "manifoldSeedId");
            sb.append("box manifoldId = \"").append(escPcb(id)).append("\"\n");
        }
        if (grammarSeed != null) {
            String id = seedId(grammarSeed, "grammarSeedId");
            sb.append("box grammarId = \"").append(escPcb(id)).append("\"\n");
        }
        if (traversalSeed != null) {
            String id = seedId(traversalSeed, "traversalSeedId", "seedId");
            sb.append("box traversalId = \"").append(escPcb(id)).append("\"\n");
        }
        if (userSeed != null) {
            String id = seedId(userSeed, "projectId", "id");
            sb.append("box userId = \"").append(escPcb(id)).append("\"\n");
        }

        return sb.toString();
    }

    /**
     * Parse box/cup/pkt [...] binding declarations from PCB source.
     * Returns a JSON array of binding objects.
     */
    private static ArrayNode parseBindings(String source) {
        ArrayNode bindings = JSON.createArrayNode();
        for (String raw : source.split("\n")) {
            String line = raw.trim();
            // match: box|cup|pkt name = [elem, elem, ...]
            if (!line.matches("^(box|cup|pkt)\\s+\\w+\\s*=\\s*\\[.+\\].*")) continue;

            String container = line.split("\\s+")[0];
            String name      = line.split("\\s+")[1];
            String inner     = line.replaceAll("^.*\\[(.*)\\].*$", "$1");
            String[] elems   = inner.split(",");

            if (elems.length < 2) continue;

            ObjectNode b = JSON.createObjectNode();
            b.put("container",   container);
            b.put("bindName",    name);
            b.put("userAnchor",  elems[0].trim());

            ArrayNode refs = JSON.createArrayNode();
            for (int i = 1; i < elems.length; i++) refs.add(elems[i].trim());
            b.set("seedRefs", refs);

            String seedType = container.equals("box") ? "manifold"
                            : container.equals("cup") ? "grammar" : "traversal";
            b.put("seedType", seedType);

            boolean natural = true;
            for (int i = 1; i < elems.length; i++) {
                if (!elems[i].trim().startsWith(seedType)) { natural = false; break; }
            }
            b.put("natural",    natural);
            b.put("confidence", natural ? 0.95 : 0.75);

            bindings.add(b);
        }
        return bindings;
    }

    /**
     * Build a manifest description node for a seed.
     */
    private static ObjectNode describeNode(String type, String seedId, JsonNode node) {
        ObjectNode desc  = JSON.createObjectNode();
        ArrayNode  props = JSON.createArrayNode();
        desc.put("type",   type);
        desc.put("seedId", seedId);
        if (node.isObject()) {
            node.fieldNames().forEachRemaining(props::add);
        }
        desc.set("properties", props);
        return desc;
    }

    /**
     * Extract a seed ID from a JSON node trying multiple field names.
     */
    private static String seedId(JsonNode node, String... fields) {
        for (String f : fields) {
            if (node.has(f) && !node.get(f).isNull()) return node.get(f).asText();
        }
        return "unknown_" + System.currentTimeMillis();
    }

    /** Escape a string for safe embedding in a PCB string literal. */
    private static String escPcb(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /** Send a JSON response. */
    private static void send(HttpExchange ex, int code, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    /** Build a JSON error string. */
    private static String error(String msg) {
        return "{\"ok\":false,\"error\":\"" + msg.replace("\"", "'") + "\"}";
    }

    /** Add CORS headers so the browser workbench can call the server. */
    private static void addCors(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
}
