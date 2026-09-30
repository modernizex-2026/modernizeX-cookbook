package com.generated.orion.oudate.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for OudateService, ported from COBOL program OUDATE (ORION-CCMS/cbl/OUDATE.cbl): TODY
 * (current date), VALD (validate YYYY-MM-DD), FMT (passthrough), OTHER (status 99).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class OudateServiceTest {

    private static final Charset KDATE_CHARSET = Charset.forName("MS932");

    @Mock private AppService appService;

    private OudateService service;

    @BeforeEach
    void setUp() {
        service = new OudateService();
    }

    @AfterEach
    void tearDown() {
        // no shared mutable state to reset
    }

    /**
     * Builds a 26-byte KDATE-PARM commarea string: KD-FUNC(4) + KD-DATE-IN(10). Remaining
     * KD-DATE-OUT/KD-STATUS are left as blanks by setGroup's space-fill and get overwritten by the
     * service.
     */
    private String buildCommarea(String func, String dateIn) {
        String funcPart = String.format("%-4s", func == null ? "" : func);
        String datePart = dateIn == null ? "" : dateIn;
        return funcPart + datePart;
    }

    private byte[] captureCommareaBytes() {
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(appService).setCommarea(captor.capture());
        return (byte[]) captor.getValue();
    }

    private String kdStatusFrom(byte[] kdateParmBytes) {
        return new String(kdateParmBytes, 24, 2, KDATE_CHARSET);
    }

    private String kdDateOutFrom(byte[] kdateParmBytes) {
        return new String(kdateParmBytes, 14, 10, KDATE_CHARSET);
    }

    private void stubCommarea(String commarea) {
        when(appService.getEibcalen()).thenReturn(26);
        when(appService.getCommarea()).thenReturn(commarea);
    }

    // ── TODY ────────────────────────────────────────────────────────────

    @Test
    void mainLine_functionTody_populatesCurrentDateAndStatusOk() {
        stubCommarea(buildCommarea("TODY", ""));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate before = LocalDate.now();

        service.mainLine(appService);

        LocalDate after = LocalDate.now();
        byte[] out = captureCommareaBytes();
        assertThat(kdStatusFrom(out)).isEqualTo("00");
        // No clock injection point in the service (uses LocalDateTime.now() directly) and
        // static mocking is unavailable on this JVM (Byte Buddy does not yet support the
        // running Java 26 class-file version) — bracket by [before,after] to stay
        // deterministic across a midnight rollover instead of asserting a fixed date.
        assertThat(kdDateOutFrom(out)).isIn(before.format(fmt), after.format(fmt));
    }

    // ── VALD — happy path ───────────────────────────────────────────────

    @Test
    void mainLine_functionValdValidDate_statusOkAndOutputEqualsInput() {
        stubCommarea(buildCommarea("VALD", "2024-03-15"));

        service.mainLine(appService);

        byte[] out = captureCommareaBytes();
        assertThat(kdStatusFrom(out)).isEqualTo("00");
        assertThat(kdDateOutFrom(out)).isEqualTo("2024-03-15");
    }

    @Test
    void mainLine_functionValdDecember31_statusOkBoundaryMaxDayOfMonth() {
        stubCommarea(buildCommarea("VALD", "2024-12-31"));

        service.mainLine(appService);

        assertThat(kdStatusFrom(captureCommareaBytes())).isEqualTo("00");
    }

    /**
     * COBOL 2000-VALIDATE hard-codes WS-DIM=29 for February regardless of leap year (no
     * century/4-year leap check) — Feb 29 in a non-leap year is still accepted.
     */
    @Test
    void mainLine_functionValdFebruary29InNonLeapYear_statusOkPerCobolNoLeapCheck() {
        stubCommarea(buildCommarea("VALD", "2023-02-29"));

        service.mainLine(appService);

        assertThat(kdStatusFrom(captureCommareaBytes())).isEqualTo("00");
    }

    // ── VALD — non-numeric segments ─────────────────────────────────────

    @Test
    void mainLine_functionValdNonNumericYear_status99() {
        stubCommarea(buildCommarea("VALD", "20A4-03-15"));

        service.mainLine(appService);

        assertThat(kdStatusFrom(captureCommareaBytes())).isEqualTo("99");
    }

    @Test
    void mainLine_functionValdNonNumericMonth_status99() {
        stubCommarea(buildCommarea("VALD", "2024-0A-15"));

        service.mainLine(appService);

        assertThat(kdStatusFrom(captureCommareaBytes())).isEqualTo("99");
    }

    @Test
    void mainLine_functionValdNonNumericDay_status99() {
        stubCommarea(buildCommarea("VALD", "2024-03-1A"));

        service.mainLine(appService);

        assertThat(kdStatusFrom(captureCommareaBytes())).isEqualTo("99");
    }

    // ── VALD — month range ───────────────────────────────────────────────

    @Test
    void mainLine_functionValdMonthZero_status99() {
        stubCommarea(buildCommarea("VALD", "2024-00-15"));

        service.mainLine(appService);

        assertThat(kdStatusFrom(captureCommareaBytes())).isEqualTo("99");
    }

    @Test
    void mainLine_functionValdMonthThirteen_status99() {
        stubCommarea(buildCommarea("VALD", "2024-13-01"));

        service.mainLine(appService);

        assertThat(kdStatusFrom(captureCommareaBytes())).isEqualTo("99");
    }

    // ── VALD — day-of-month range ────────────────────────────────────────

    @Test
    void mainLine_functionValdFebruary30_status99() {
        stubCommarea(buildCommarea("VALD", "2024-02-30"));

        service.mainLine(appService);

        assertThat(kdStatusFrom(captureCommareaBytes())).isEqualTo("99");
    }

    @Test
    void mainLine_functionValdApril31_status99() {
        stubCommarea(buildCommarea("VALD", "2024-04-31"));

        service.mainLine(appService);

        assertThat(kdStatusFrom(captureCommareaBytes())).isEqualTo("99");
    }

    @Test
    void mainLine_functionValdDayZero_status99() {
        stubCommarea(buildCommarea("VALD", "2024-03-00"));

        service.mainLine(appService);

        assertThat(kdStatusFrom(captureCommareaBytes())).isEqualTo("99");
    }

    // ── FMT / unknown function ───────────────────────────────────────────

    @Test
    void mainLine_functionFmtWithTrailingSpace_passthroughStatusOk() {
        stubCommarea(buildCommarea("FMT ", "2024-03-15"));

        service.mainLine(appService);

        byte[] out = captureCommareaBytes();
        assertThat(kdStatusFrom(out)).isEqualTo("00");
        assertThat(kdDateOutFrom(out)).isEqualTo("2024-03-15");
    }

    @Test
    void mainLine_functionUnknown_status99() {
        stubCommarea(buildCommarea("XXXX", "2024-03-15"));

        service.mainLine(appService);

        assertThat(kdStatusFrom(captureCommareaBytes())).isEqualTo("99");
    }

    // ── Commarea plumbing variants ───────────────────────────────────────

    @Test
    void mainLine_eibcalenZero_doesNotTouchCommarea() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        verify(appService, never()).getCommarea();
        verify(appService, never()).setCommarea(any());
    }

    @Test
    void mainLine_commareaAsByteArray_writesBackByteArray() {
        String commarea = buildCommarea("VALD", "2024-03-15");
        when(appService.getEibcalen()).thenReturn(26);
        when(appService.getCommarea()).thenReturn(commarea.getBytes(KDATE_CHARSET));

        service.mainLine(appService);

        byte[] out = captureCommareaBytes();
        assertThat(kdStatusFrom(out)).isEqualTo("00");
    }

    @Test
    void mainLine_commareaAsObjectArrayWithByteElement_writesBackBytesIntoArray() {
        String commarea = buildCommarea("VALD", "2024-13-01");
        Object[] params = new Object[] {commarea.getBytes(KDATE_CHARSET)};
        when(appService.getEibcalen()).thenReturn(26);
        when(appService.getCommarea()).thenReturn(params);

        service.mainLine(appService);

        verify(appService, never()).setCommarea(any());
        assertThat(params[0]).isInstanceOf(byte[].class);
        byte[] out = (byte[]) params[0];
        assertThat(kdStatusFrom(out)).isEqualTo("99");
    }

    @Test
    void mainLine_commareaAsObjectArrayWithStringElement_writesBackGroupString() {
        String commarea = buildCommarea("VALD", "2024-03-15");
        Object[] params = new Object[] {commarea};
        when(appService.getEibcalen()).thenReturn(26);
        when(appService.getCommarea()).thenReturn(params);

        service.mainLine(appService);

        verify(appService, never()).setCommarea(any());
        assertThat(params[0]).isInstanceOf(String.class);
        String out = (String) params[0];
        assertThat(out.substring(24, 26)).isEqualTo("00");
    }

    // ── Program metadata ─────────────────────────────────────────────────

    @Test
    void getProgramName_returnsOudate() {
        assertThat(service.getProgramName()).isEqualTo("OUDATE");
    }
}
