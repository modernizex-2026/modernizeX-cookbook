package com.sakura.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for COBOL interactive screen programs. Routes WebSocket connections to program
 * service beans, manages session lifecycle. Each WebSocket connection runs a COBOL program in a
 * dedicated platform thread.
 */
@Component
public class WebSocketScreenHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(WebSocketScreenHandler.class);
    private static final ObjectMapper JSON = new ObjectMapper();

    private final ApplicationContext applicationContext;
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();

    @Value("${websocket.send-timeout-ms:30000}")
    private int wsSendTimeoutMs;

    @Value("${websocket.max-buffer-bytes:524288}")
    private int wsMaxBufferBytes;

    public WebSocketScreenHandler(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Resolve the COBOL {@code @WSNAME} workstation name for this client (faithful "Option A"): map
     * the client's IP to the workstation name recorded in the ALOGIN session table ({@code ip_add
     * -> cpu_name}, latest login wins). The client does NOT supply its own name (no spoofing, no
     * shared default); an unregistered IP yields {@code ""} so the program STOPs ("terminal not
     * registered"), preserving COBOL run-unit semantics. A client-supplied {@code ?station=} is
     * honoured ONLY when {@code -Dcobol.station.allowClientOverride=true} (dev/test). Resolution
     * runs on the program thread, whose context CL can load the JDBC driver.
     */
    private String resolveStationName(WebSocketSession session, WebSocketScreenRenderer renderer) {
        if (Boolean.getBoolean("cobol.station.allowClientOverride")) {
            String s = renderer.getStationName();
            if (s != null && !s.isBlank()) {
                log.info("@WSNAME via client override ?station={}", s);
                return s;
            }
        }
        String ip = clientIp(session);
        if (ip == null) {
            log.warn(
                    "@WSNAME: no client IP on session {} — workstation unresolved",
                    session.getId());
            return "";
        }
        try {
            javax.sql.DataSource ds = applicationContext.getBean(javax.sql.DataSource.class);
            java.util.List<String> names =
                    new org.springframework.jdbc.core.JdbcTemplate(ds)
                            .query(
                                    "SELECT cpu_name FROM alogin WHERE rtrim(ip_add)=? "
                                            + "ORDER BY login_ymd DESC, login_time DESC",
                                    (rs, i) -> rs.getString(1),
                                    ip);
            if (!names.isEmpty() && names.get(0) != null) {
                String cpu = names.get(0).trim();
                log.info("@WSNAME resolved: client IP {} -> cpu_name={}", ip, cpu);
                return cpu;
            }
            log.warn(
                    "@WSNAME: client IP {} has no ALOGIN session — not registered (program will"
                            + " STOP)",
                    ip);
        } catch (Exception e) {
            log.warn("@WSNAME IP->cpu_name lookup failed for {}: {}", ip, e.getMessage());
        }
        return "";
    }

    /** Client IP from the WS session, normalised to dotted IPv4 where possible. */
    private static String clientIp(WebSocketSession session) {
        try {
            java.net.InetSocketAddress ra = session.getRemoteAddress();
            if (ra == null || ra.getAddress() == null) {
                return null;
            }
            String ip = ra.getAddress().getHostAddress();
            int pct = ip.indexOf('%');
            if (pct >= 0) {
                ip = ip.substring(0, pct); // strip IPv6 zone id
            }
            if (ip.startsWith("::ffff:") && ip.indexOf('.') > 0) {
                ip = ip.substring("::ffff:".length()); // unwrap IPv4-mapped IPv6
            }
            if (ip.equals("::1") || ip.equals("0:0:0:0:0:0:0:1")) {
                ip = "127.0.0.1"; // IPv6 loopback -> IPv4 loopback (same host; browsers often
                // use ::1)
            }
            return ip;
        } catch (Exception e) {
            return null;
        }
    }

    private static class SessionContext {
        final WebSocketScreenRenderer renderer;
        final Thread serviceThread;

        SessionContext(WebSocketScreenRenderer renderer, Thread serviceThread) {
            this.renderer = renderer;
            this.serviceThread = serviceThread;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String programId = extractProgramId(session);
        if (programId == null || programId.isEmpty()) {
            closeWithError(session, "No programId in URL path");
            return;
        }

        log.info("WebSocket connected: session={}, program={}", session.getId(), programId);

        // Wrap session for thread-safe concurrent writes from the service thread
        WebSocketSession concurrentSession =
                new ConcurrentWebSocketSessionDecorator(session, wsSendTimeoutMs, wsMaxBufferBytes);
        WebSocketScreenRenderer renderer = new WebSocketScreenRenderer(concurrentSession);

        // Resolve service bean (prototype scope — fresh instance per session).
        // COBOL program names with '_' (e.g. ABC010_C) become camelCase Java class
        // names (Abc010C) — Spring bean name = Abc010CService → 'abc010CService'.
        // Must convert '_x' → upper(x) when building bean name, otherwise
        // 'abc010_c' + 'Service' = 'abc010_cService' ≠ Spring's 'abc010CService'.
        String beanName;
        {
            StringBuilder _bn = new StringBuilder();
            boolean _up = false;
            for (char _c : programId.toLowerCase().toCharArray()) {
                if (_c == '_') {
                    _up = true;
                    continue;
                }
                _bn.append(_up ? Character.toUpperCase(_c) : _c);
                _up = false;
            }
            beanName = _bn.append("Service").toString();
        }
        Object serviceBean;
        try {
            serviceBean = applicationContext.getBean(beanName);
        } catch (Exception e) {
            log.error(
                    "getBean({}) failed: {}: {}",
                    beanName,
                    e.getClass().getName(),
                    e.getMessage(),
                    e);
            closeWithError(
                    session,
                    "Unknown program: "
                            + programId
                            + " ("
                            + e.getClass().getSimpleName()
                            + ": "
                            + e.getMessage()
                            + ")");
            return;
        }

        // Inject renderer via setter (generated services with SCREEN SECTION have setRenderer)
        try {
            serviceBean
                    .getClass()
                    .getMethod("setRenderer", ScreenRendererInstance.class)
                    .invoke(serviceBean, renderer);
        } catch (Exception e) {
            log.warn("Could not inject renderer into {}: {}", beanName, e.getMessage());
        }

        // Run program in daemon thread (inherit classloader for Spring Boot fat JAR).
        // CRITICAL: use WebSocketScreenHandler.class.getClassLoader() instead of
        // Thread.currentThread().getContextClassLoader(). In a Spring Boot fat JAR
        // running under Tomcat, the WebSocket handler executes on a tomcat-nio thread
        // whose context CL is NOT the LaunchedURLClassLoader — it's the parent app CL
        // that cannot see nested BOOT-INF/lib JARs. Lazy class loads (e.g. PostgreSQL
        // driver inner classes triggered the first time eilwWriteZ000() runs an INSERT,
        // or logback's ThrowableProxy reached via log.error) then fail with
        // NoClassDefFoundError on org.postgresql.util.ByteConverter$PositiveShorts or
        // ch.qos.logback.classic.spi.ThrowableProxy — even though the classes ARE in
        // BOOT-INF/lib/. Using this class's own CL guarantees the LaunchedURLClassLoader.
        final ClassLoader appCl = WebSocketScreenHandler.class.getClassLoader();
        Thread thread =
                new Thread(
                        () -> {
                            Thread.currentThread().setContextClassLoader(appCl);
                            // Bind the @WSNAME workstation name for this session thread so
                            // the program (and CALL sub-programs) resolve it via Utility.
                            // derive it from the client IP via ALOGIN
                            // (ip_add -> cpu_name), NOT a client-supplied/shared value.
                            Utility.setStationName(resolveStationName(session, renderer));
                            // Per-session scratch dir for file-mode I/O (temp/print work files).
                            // RawDatasetBase.resolveFilePath() reads this ThreadLocal before
                            // falling
                            // back to env vars. Concurrent sessions get distinct UUID dirs and
                            // never collide on a shared scratch path.
                            java.nio.file.Path scratchDir = null;
                            try {
                                scratchDir =
                                        java.nio.file.Files.createTempDirectory(
                                                "cobol-scratch-" + session.getId() + "-");
                                Utility.setScratchDir(scratchDir);
                            } catch (java.io.IOException scratchEx) {
                                log.warn(
                                        "Could not create scratch dir for session {}: {}",
                                        session.getId(),
                                        scratchEx.getMessage());
                            }
                            try {
                                serviceBean.getClass().getMethod("execute").invoke(serviceBean);
                                sendJson(
                                        session,
                                        JSON.createObjectNode()
                                                .put("type", "programFinished")
                                                .put("code", 0));
                            } catch (Exception e) {
                                Throwable cause = e.getCause() != null ? e.getCause() : e;
                                if (cause instanceof StopRunSignal) {
                                    log.info(
                                            "Program {} stopped: {}",
                                            programId,
                                            cause.getMessage());
                                    sendJson(
                                            session,
                                            JSON.createObjectNode()
                                                    .put("type", "programFinished")
                                                    .put("code", 0)
                                                    .put("reason", cause.getMessage()));
                                } else {
                                    log.error(
                                            "Program {} error: {}",
                                            programId,
                                            cause.getMessage(),
                                            cause);
                                    sendJson(
                                            session,
                                            JSON.createObjectNode()
                                                    .put("type", "error")
                                                    .put("message", cause.getMessage()));
                                }
                            } finally {
                                Utility.clearStationName();
                                Utility.clearScratchDir();
                                // Recursively delete the per-session scratch dir so /tmp doesn't
                                // accumulate stale files across crashes/restarts (avoids the
                                // disk-fill incident pattern that preceded the SQL-mode pivot).
                                if (scratchDir != null) {
                                    try (java.util.stream.Stream<java.nio.file.Path> walk =
                                            java.nio.file.Files.walk(scratchDir)) {
                                        walk.sorted(java.util.Comparator.reverseOrder())
                                                .forEach(
                                                        p -> {
                                                            try {
                                                                java.nio.file.Files.deleteIfExists(
                                                                        p);
                                                            } catch (java.io.IOException ignored) {
                                                            }
                                                        });
                                    } catch (java.io.IOException ignored) {
                                        /* dir may already be gone */
                                    }
                                }
                                sessions.remove(session.getId());
                                try {
                                    if (session.isOpen()) session.close(CloseStatus.NORMAL);
                                } catch (IOException ignored) {
                                }
                            }
                        });
        thread.setName("ws-" + programId + "-" + session.getId());
        thread.setDaemon(true);
        thread.start();

        sessions.put(session.getId(), new SessionContext(renderer, thread));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        SessionContext ctx = sessions.get(session.getId());
        if (ctx != null) {
            ctx.renderer.onClientMessage(message.getPayload());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("WebSocket closed: session={}, status={}", session.getId(), status);
        cleanupSession(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.warn(
                "WebSocket transport error: session={}, error={}",
                session.getId(),
                exception.getMessage());
        cleanupSession(session.getId());
    }

    private void cleanupSession(String sessionId) {
        SessionContext ctx = sessions.remove(sessionId);
        if (ctx != null) {
            // Poison pill: unblock any waiting inputQueue.poll()
            ctx.renderer.onClientMessage("__DISCONNECT__");
            // Interrupt the service thread
            ctx.serviceThread.interrupt();
        }
    }

    private String extractProgramId(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }
        String path = uri.getPath(); // e.g., /ws/<program>
        if (path != null && path.startsWith("/ws/")) {
            return path.substring(4);
        }
        return null;
    }

    private void closeWithError(WebSocketSession session, String message) {
        sendJson(session, JSON.createObjectNode().put("type", "error").put("message", message));
        try {
            session.close(CloseStatus.BAD_DATA);
        } catch (IOException ignored) {
        }
    }

    private static void sendJson(WebSocketSession session, String json) {
        try {
            if (session.isOpen()) {
                synchronized (session) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(WebSocketScreenHandler.class)
                    .warn("Failed to send: {}", e.getMessage());
        }
    }

    private static void sendJson(WebSocketSession session, ObjectNode node) {
        try {
            sendJson(session, JSON.writeValueAsString(node));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize JSON message: {}", e.getMessage());
        }
    }
}
