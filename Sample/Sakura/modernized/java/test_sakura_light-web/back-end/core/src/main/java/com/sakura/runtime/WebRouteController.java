package com.sakura.runtime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Forwards deep-link program URLs to {@code index.html} so the Vue SPA can render the requested
 * program. Returns 404 at the root path — there is no menu or landing page.
 */
@Controller
@ConditionalOnProperty(name = "screen.renderer", havingValue = "websocket")
public class WebRouteController {

    @GetMapping({
        "/menu00", "/menu00/",
        "/ap0010", "/ap0010/",
        "/ap0020", "/ap0020/",
        "/ar0010", "/ar0010/",
        "/ar0020", "/ar0020/",
        "/iv0010", "/iv0010/",
        "/iv0020", "/iv0020/",
        "/iv0030", "/iv0030/",
        "/iv0040", "/iv0040/",
        "/iv0050", "/iv0050/",
        "/ms0010", "/ms0010/",
        "/ms0020", "/ms0020/",
        "/ms0030", "/ms0030/",
        "/ms0040", "/ms0040/",
        "/ms0050", "/ms0050/",
        "/ms0060", "/ms0060/",
        "/ms0070", "/ms0070/",
        "/ms0080", "/ms0080/",
        "/ms0090", "/ms0090/",
        "/ms0100", "/ms0100/",
        "/ms0110", "/ms0110/",
        "/ms0120", "/ms0120/",
        "/oe0010", "/oe0010/",
        "/oe0020", "/oe0020/",
        "/oe0030", "/oe0030/",
        "/oe0040", "/oe0040/",
        "/pu0010", "/pu0010/",
        "/pu0020", "/pu0020/",
        "/pu0030", "/pu0030/",
        "/pu0040", "/pu0040/",
        "/rc0010", "/rc0010/",
        "/sh0010", "/sh0010/",
        "/sl0010", "/sl0010/",
        "/sl0020", "/sl0020/",
        "/sl0030", "/sl0030/",
        "/sl0040", "/sl0040/",
        "/abortx", "/abortx/",
        "/bt0010", "/bt0010/",
        "/bt0020", "/bt0020/",
        "/bt0030", "/bt0030/",
        "/bt0040", "/bt0040/",
        "/bt0050", "/bt0050/",
        "/bt0060", "/bt0060/",
        "/bt0070", "/bt0070/",
        "/bt0080", "/bt0080/",
        "/bt0090", "/bt0090/",
        "/chklog", "/chklog/",
        "/credit", "/credit/",
        "/dateut", "/dateut/",
        "/numgen", "/numgen/",
        "/oe0050", "/oe0050/",
        "/rp0010", "/rp0010/",
        "/rp0020", "/rp0020/",
        "/rp0030", "/rp0030/",
        "/rp0040", "/rp0040/",
        "/rp0050", "/rp0050/",
        "/rp0060", "/rp0060/",
        "/rp0070", "/rp0070/",
        "/rp0080", "/rp0080/",
        "/rp0090", "/rp0090/",
        "/rp0100", "/rp0100/",
        "/rp0110", "/rp0110/",
        "/rp0120", "/rp0120/",
        "/rp0130", "/rp0130/",
        "/rp0140", "/rp0140/",
        "/taxcal", "/taxcal/"
    })
    public String spaForward() {
        return "forward:/index.html";
    }

    // Note: do NOT add /index.html here — Spring's static resource handler serves it as the
    // forward target for spaForward().
    @GetMapping("/")
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void rootNotFound() {}
}
