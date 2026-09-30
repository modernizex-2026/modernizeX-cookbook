package com.sakura.getmsg.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sakura.getmsg.runtime.GetmsgDatasets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for GetmsgFieldAccess typed field wrappers (WS-SYS + MSGF record). Pure round-trip
 * checks against the real record-schema layouts — no mocking needed.
 */
class GetmsgFieldAccessTest {

    private GetmsgFieldAccess ws;

    @BeforeEach
    void setUp() {
        ws = new GetmsgFieldAccess(new WorkingStorage(), new GetmsgDatasets());
    }

    @Test
    void setAndGetMgCode_roundTrips() {
        ws.setMgCode("MSG001");

        assertEquals("MSG001", ws.getMgCode());
    }

    @Test
    void setAndGetMgText_roundTrips() {
        String text = "N".repeat(60);

        ws.setMgText(text);

        assertEquals(text, ws.getMgText());
    }

    @Test
    void setAndGetCompletionCode_roundTrips() {
        ws.setCompletionCode(12);

        assertEquals(12, ws.getCompletionCode());
    }

    @Test
    void setAndGetFsts_defaultsToZeroZero_thenRoundTrips() {
        assertEquals("00", ws.getFsts());

        ws.setFsts("35");

        assertEquals("35", ws.getFsts());
    }
}
