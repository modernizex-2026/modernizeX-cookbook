package com.generated.orion.oumsg.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.generated.orion.oumsg.domain.OumsgFieldAccess;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for OumsgService — ported from COBOL program OUMSG (message-text retriever). Ground
 * truth: OUMSG.cbl PROCEDURE DIVISION 0000-MAIN, which maps KM-CODE to KM-TEXT/KM-STATUS via a
 * fixed EVALUATE table plus an OTHER branch.
 *
 * <p>OumsgService has no dependencies to mock (no files, no DB, no screen) — the program is a pure
 * in-memory lookup. KM-CODE is set via reflection into the service's private WorkingStorage-backed
 * field accessor, mirroring how the COBOL LINKAGE SECTION (KMSG-PARM) would be populated by the
 * caller.
 */
@ExtendWith(MockitoExtension.class)
class OumsgServiceTest {

    private OumsgService service;
    private OumsgFieldAccess ws;

    @BeforeEach
    void setUp() throws Exception {
        service = new OumsgService();
        Field wsField = OumsgService.class.getDeclaredField("ws");
        wsField.setAccessible(true);
        ws = (OumsgFieldAccess) wsField.get(service);
    }

    @ParameterizedTest(name = "kmCode={0} -> kmText=''{1}'' kmStatus={2}")
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    @CsvSource({
        "I0001,Operation completed successfully.,00",
        "E0001,Record not found.,00",
        "E0002,Duplicate record.,00",
        "E0003,Please enter required fields.,00",
        "E0004,Invalid data entered.,00",
        "E0005,File access error.,00",
        "W0001,No records to display.,00"
    })
    void execute_knownMessageCode_returnsMappedTextAndOkStatus(
            String kmCode, String expectedText, String expectedStatus) {
        // Given
        ws.setKmCode(kmCode);

        // When
        service.execute();

        // Then
        // CONVERT-GAP: KM-CODE is PIC X(06); COBOL EVALUATE space-pads the 5-char
        // literals ('I0001' -> 'I0001 ') before comparing, so every WHEN matches.
        // The generated switch compares String.valueOf(ws.getKmCode()) — which
        // returns the raw space-padded 6-byte value — against the unpadded 5-char
        // literal, so it NEVER matches any case. Every known code wrongly falls
        // into WHEN OTHER (status "01", text = echoed code) instead of COBOL's "00"
        // + mapped text. This assertion intentionally fails until the switch keys
        // (or the comparison) account for COBOL's space-padding semantics.
        assertThat(ws.getKmStatus()).isEqualTo(expectedStatus);
        assertThat(ws.getKmText().trim()).isEqualTo(expectedText);
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_unknownMessageCode_echoesCodeAsTextWithErrorStatus() {
        // Given: COBOL WHEN OTHER — MOVE KM-CODE TO KM-TEXT, MOVE '01' TO KM-STATUS
        ws.setKmCode("ZZZZZ");

        // When
        service.execute();

        // Then
        assertThat(ws.getKmStatus()).isEqualTo("01");
        assertThat(ws.getKmText().trim()).isEqualTo("ZZZZZ");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_emptyMessageCode_fallsIntoOtherBranchWithErrorStatus() {
        // Given: no code set — WORKING-STORAGE field defaults to spaces, which does
        // not match any WHEN clause, so COBOL falls into WHEN OTHER.
        ws.setKmCode("");

        // When
        service.execute();

        // Then
        assertThat(ws.getKmStatus()).isEqualTo("01");
        assertThat(ws.getKmText().trim()).isEqualTo("");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_lowercaseCodeDoesNotMatchUppercaseTable_fallsIntoOtherBranch() {
        // Given: COBOL EVALUATE comparison is case-sensitive; a lowercase code never
        // matches the uppercase WHEN literals ('i0001' != 'I0001').
        ws.setKmCode("i0001");

        // When
        service.execute();

        // Then
        assertThat(ws.getKmStatus()).isEqualTo("01");
        assertThat(ws.getKmText().trim()).isEqualTo("i0001");
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_returnsCompletionCodeZero_onNormalProgramExit() {
        // Given: 0000-MAIN ends with GOBACK (no ABEND-RTN, no explicit completion code
        // set anywhere in OUMSG) — program exit is normal, base class leaves it at the
        // default (0), never sets 255/12 on this path.
        ws.setKmCode("I0001");

        // When
        service.execute();

        // Then
        assertThat(service.getCompletionCode()).isZero();
    }
}
