package com.appruntime;

import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * REST controller providing the /api/terminal/execute endpoint. Accepts terminal input as JSON and
 * returns screen response.
 *
 * <p>Implements pseudo-conversational state: CICS RETURN TRANSID stores commarea in HttpSession,
 * restored on the next request for the same program.
 */
@RestController
@RequestMapping("/api/terminal")
public class TerminalController {

    private static final Logger log = LoggerFactory.getLogger(TerminalController.class);

    @Autowired private AppRunner appRunner;

    /**
     * Logical entry path returned in {@code redirect} for the SPA to navigate to (API-only
     * backend).
     */
    public static final String ENTRY_PATH = "/terminal/entry";

    /**
     * LRU 20 entry cho idempotency-set — static nested (Serializable, không capture controller).
     */
    private static final class LruMap extends java.util.LinkedHashMap<String, Boolean> {
        private static final long serialVersionUID = 1L;

        LruMap() {
            super(16, 0.75f, true);
        }

        @Override
        protected boolean removeEldestEntry(java.util.Map.Entry<String, Boolean> e) {
            return size() > 20;
        }
    }

    /**
     * List of available online programs (canonical uppercase names, sorted). The SPA uses this for
     * its program selector; replaces the removed server-rendered index page.
     */
    @GetMapping("/programs")
    public java.util.Set<String> programs() {
        return appRunner.getProgramNames();
    }

    /**
     * Transaction-ID → program-name map (CICS RETURN TRANSID). Drives the SPA's entry gateway: the
     * user types a TransID (e.g. CC00) and the SPA resolves it to its program.
     */
    @GetMapping("/transactions")
    public java.util.Map<String, String> transactions() {
        return appRunner.getTransactionMap();
    }

    @PostMapping("/execute")
    public ResponseEntity<ScreenResponse> execute(
            @RequestBody TerminalInput input, HttpSession session) {
        if (input.getProgramName() == null || input.getProgramName().isEmpty()) {
            ScreenResponse err = new ScreenResponse();
            err.setMessage("ERROR: programName is required");
            return ResponseEntity.badRequest().body(err);
        }
        if (input.getAidKey() == null || input.getAidKey().isEmpty()) {
            input.setAidKey("ENTER");
        }
        try {
            // Idempotency: reject duplicate requests (same requestId already processed)
            String reqId = input.getRequestId();
            if (reqId != null && !reqId.isEmpty()) {
                @SuppressWarnings("unchecked")
                java.util.Set<String> processed =
                        (java.util.Set<String>) session.getAttribute("_processedRequests");
                if (processed == null) {
                    // static nested class (KHÔNG anonymous): anonymous class capture `this`
                    // controller →
                    // session không serialize được với Spring Session Redis (profile `redis`).
                    processed = java.util.Collections.newSetFromMap(new LruMap());
                    session.setAttribute("_processedRequests", processed);
                }
                if (processed.contains(reqId)) {
                    // Return cached last screen instead of re-executing
                    ScreenResponse cached =
                            (ScreenResponse)
                                    session.getAttribute(
                                            SessionKeys.lastScreen(input.getProgramName()));
                    if (cached != null) {
                        return ResponseEntity.ok(cached);
                    }
                }
                processed.add(reqId);
            }

            // Detect session expiry: if session is new but client sent a currentTemplate,
            // the previous session expired — warn user
            if (session.isNew()
                    && input.getCurrentTemplate() != null
                    && !input.getCurrentTemplate().isEmpty()) {
                ScreenResponse warn = new ScreenResponse();
                warn.setMessage("Session expired — please start again");
                warn.setRedirect("/terminal/" + input.getProgramName().toUpperCase());
                return ResponseEntity.ok(warn);
            }

            // SPA follows an intra-program map-switch redirect (e.g. ORDPGM ordmap→ordlst) by
            // re-fetching
            // the program screen with no currentTemplate. The switched map's screen was cached on
            // the
            // redirecting response — the removed Thymeleaf GET handler used to serve it, so the
            // API-only
            // backend serves it here. Serve + consume it instead of re-executing: a re-execute
            // would
            // treat this render as a fresh RECEIVE (ORDPGM 7300-LIST-SELECT with no selection) and
            // emit
            // the wrong screen. One-shot (removed after serving) so the next real input
            // re-executes.
            if (input.getCurrentTemplate() == null || input.getCurrentTemplate().isEmpty()) {
                String prog = input.getProgramName().toUpperCase();
                // An intra-program map-switch (mapResponse) OR an XCTL program-change
                // (xctlResponse) cached
                // the target's SEND MAP screen on the redirecting response. Serve + consume it
                // (one-shot)
                // instead of re-executing: in CICS, XCTL transfers control and the target's SEND
                // MAP is the
                // displayed screen (RETURN TRANSID) — there is NO RECEIVE until the user's next
                // key. A
                // re-execute here would be a phantom RECEIVE (aid=ENTER), reprocessing the fresh
                // screen as
                // input (e.g. COADM01C re-enters with an empty option → "Please enter a valid
                // option" + 00).
                // The next real input re-executes (cache gone), which IS the genuine RECEIVE.
                ScreenResponse cachedMap =
                        (ScreenResponse) session.getAttribute(SessionKeys.mapResponse(prog));
                if (cachedMap == null) {
                    cachedMap =
                            (ScreenResponse) session.getAttribute(SessionKeys.xctlResponse(prog));
                }
                if (cachedMap != null) {
                    session.removeAttribute(SessionKeys.mapResponse(prog));
                    session.removeAttribute(SessionKeys.xctlResponse(prog));
                    return ResponseEntity.ok(cachedMap);
                }
            }

            // Restore commarea + declared length from session (pseudo-conversational: RETURN
            // TRANSID
            // persists it)
            String commareaKey = SessionKeys.commarea(input.getProgramName());
            String lengthKey = SessionKeys.commareaLength(input.getProgramName());
            if (input.getCommarea() == null) {
                input.setCommarea(session.getAttribute(commareaKey));
                Object storedLength = session.getAttribute(lengthKey);
                if (storedLength instanceof Integer) {
                    input.setCommareaLength((Integer) storedLength);
                }
            }

            // FSET restoration: fields declared as FSET in BMS map are transmitted back by the 3270
            // terminal even when not modified, because their MDT bits are pre-set.
            // In the web model, display-only fields (<span>) are not submitted in POST.
            // Restore them from the previous SEND MAP response stored in session.
            restoreFsetFields(input, session);

            ScreenResponse response = appRunner.execute(input);

            // Cache SEND MAP field values so the next RECEIVE MAP can restore FSET fields.
            // FSET fields are display-only <span> elements and are not submitted in POST body.
            // When eraseScreen=true (SEND MAP ERASE), only cache fields from THIS response —
            // do not carry over fields from previous pages (critical for pagination).
            if (response.getFields() != null && response.getTemplateName() != null) {
                if (response.isEraseScreen()) {
                    // ERASE: replace cache entirely with current response fields only
                    session.setAttribute(
                            SessionKeys.lastFields(
                                    input.getProgramName(), response.getTemplateName()),
                            new java.util.HashMap<>(response.getFields()));
                } else {
                    // DATAONLY: merge current fields into existing cache
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> cached =
                            (java.util.Map<String, Object>)
                                    session.getAttribute(
                                            SessionKeys.lastFields(
                                                    input.getProgramName(),
                                                    response.getTemplateName()));
                    if (cached == null) {
                        cached = new java.util.HashMap<>();
                    }
                    cached.putAll(response.getFields());
                    session.setAttribute(
                            SessionKeys.lastFields(
                                    input.getProgramName(), response.getTemplateName()),
                            cached);
                }
            }

            // Detect XCTL: active program differs from request program
            String activeProgram = response.getProgramName();
            if (activeProgram != null && !activeProgram.equalsIgnoreCase(input.getProgramName())) {
                // XCTL target exited without sending a screen (bare RETURN).
                // In real CICS: task ends → user starts new transaction on same terminal.
                // HTTP equivalent: redirect to target as fresh entry (EIBCALEN=0).
                if (response.getTemplateName() == null || response.getTemplateName().isEmpty()) {
                    session.removeAttribute(SessionKeys.commarea(activeProgram));
                    session.removeAttribute(SessionKeys.commareaLength(activeProgram));
                    ScreenResponse redirectResp = new ScreenResponse();
                    redirectResp.setRedirect("/terminal/" + activeProgram);
                    return ResponseEntity.ok(redirectResp);
                }
                // XCTL occurred — source program transferred control.
                // Clean up source program's session state to prevent unbounded growth.
                String srcProg = input.getProgramName().toUpperCase();
                session.removeAttribute(SessionKeys.lastScreen(srcProg));
                session.removeAttribute(SessionKeys.commarea(srcProg));
                session.removeAttribute(SessionKeys.commareaLength(srcProg));
                // Cache response for redirect target
                session.setAttribute(SessionKeys.xctlResponse(activeProgram), response);
                // Save commarea + declared length under TARGET program's key (not caller's)
                if (response.getReturnCommarea() != null) {
                    session.setAttribute(
                            SessionKeys.commarea(activeProgram), response.getReturnCommarea());
                    session.setAttribute(
                            SessionKeys.commareaLength(activeProgram),
                            response.getReturnCommareaLength());
                }
                // Return redirect — terminal.js will navigate to target program's page
                ScreenResponse redirectResp = new ScreenResponse();
                redirectResp.setRedirect("/terminal/" + activeProgram);
                return ResponseEntity.ok(redirectResp);
            }

            // RETURN without TRANSID = session end (e.g., PF3 exit)
            // In CICS: task terminates, terminal returns to blank screen.
            // Clear all program state so F5 on old URL doesn't show stale screen.
            if (response.isSessionEnd()) {
                String programName = input.getProgramName().toUpperCase();
                session.removeAttribute(commareaKey);
                session.removeAttribute(lengthKey);
                session.removeAttribute(SessionKeys.lastScreen(programName));
                ScreenResponse redirectResp = new ScreenResponse();
                redirectResp.setRedirect(ENTRY_PATH);
                return ResponseEntity.ok(redirectResp);
            }

            // Normal flow (no XCTL): save commarea + declared length under same program
            if (response.getReturnCommarea() != null) {
                session.setAttribute(commareaKey, response.getReturnCommarea());
                session.setAttribute(lengthKey, response.getReturnCommareaLength());
            }

            // Detect map switch: response template differs from client's current template.
            // For intra-program map switches (same program, different map within same mapset),
            // return the response directly — the JS client handles template reloading via redirect.
            // Only redirect when the response itself requests it (XCTL to different program).
            String currentTemplate = input.getCurrentTemplate();
            String newTemplate = response.getTemplateName();
            String currentMapName =
                    currentTemplate != null && currentTemplate.contains("/")
                            ? currentTemplate.substring(currentTemplate.lastIndexOf('/') + 1)
                            : currentTemplate;
            if (currentMapName != null
                    && newTemplate != null
                    && !newTemplate.equalsIgnoreCase(currentMapName)) {
                // Intra-program map switch: cache response, redirect so GET handler serves new HTML
                // Keep templateName as-is (just the map name, e.g., "ordlst") — resolveTemplate
                // adds program prefix
                String programName = input.getProgramName().toUpperCase();
                session.setAttribute(SessionKeys.mapResponse(programName), response);
                ScreenResponse redirectResp = new ScreenResponse();
                redirectResp.setRedirect("/terminal/" + programName);
                return ResponseEntity.ok(redirectResp);
            }

            // Cache for F5 refresh — save last screen state per program
            String progKey =
                    response.getProgramName() != null
                            ? response.getProgramName()
                            : input.getProgramName();
            if (progKey != null) {
                session.setAttribute(SessionKeys.lastScreen(progKey.toUpperCase()), response);
            }

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error(
                    "Terminal execute failed for program '{}': {}",
                    input.getProgramName(),
                    e.getMessage(),
                    e);
            ScreenResponse err = new ScreenResponse();
            err.setMessage("ERROR: " + e.getMessage());
            return ResponseEntity.status(404).body(err);
        } catch (Exception e) {
            log.error(
                    "Terminal execute failed for program '{}': {}",
                    input.getProgramName(),
                    e.getMessage(),
                    e);
            ScreenResponse err = new ScreenResponse();
            err.setMessage("ERROR: " + e.getMessage());
            return ResponseEntity.internalServerError().body(err);
        }
    }

    /**
     * Restores FSET-registered fields from the previous SEND MAP response into the current request.
     *
     * <p>On a 3270 terminal, fields with the FSET attribute have their MDT (Modified Data Tag)
     * pre-set, causing the terminal to transmit them back on every AID key even when unmodified. In
     * the web model, these fields are rendered as {@code <span>} (display-only) rather than {@code
     * <input>}, so they are absent from the POST body. This method merges them back from the
     * session-cached previous SEND MAP field values before the program executes RECEIVE MAP.
     */
    @SuppressWarnings("unchecked")
    private void restoreFsetFields(TerminalInput input, HttpSession session) {
        String currentTemplate = input.getCurrentTemplate();
        if (currentTemplate == null || currentTemplate.isEmpty()) {
            return;
        }

        String mapName =
                currentTemplate.contains("/")
                        ? currentTemplate.substring(currentTemplate.lastIndexOf('/') + 1)
                        : currentTemplate;

        Set<String> fsetFields = appRunner.getFsetFields(mapName.toUpperCase());
        if (fsetFields.isEmpty()) {
            return;
        }

        Map<String, Object> previousFields =
                (Map<String, Object>)
                        session.getAttribute(
                                SessionKeys.lastFields(input.getProgramName(), mapName));
        if (previousFields == null) {
            return;
        }

        Map<String, String> inputFields = input.getFields();
        if (inputFields == null) {
            inputFields = new HashMap<>();
            input.setFields(inputFields);
        }

        for (String fsetField : fsetFields) {
            if (!inputFields.containsKey(fsetField)) {
                Object prev = previousFields.get(fsetField);
                if (prev != null) {
                    inputFields.put(fsetField, prev.toString());
                }
            }
        }
    }
}
