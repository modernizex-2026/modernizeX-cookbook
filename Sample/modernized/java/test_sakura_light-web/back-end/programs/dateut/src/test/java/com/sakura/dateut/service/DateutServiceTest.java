package com.sakura.dateut.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

import com.sakura.runtime.linkage.DateutLinkParm;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for DateutService (COBOL DATEUT — date utility TODY/VALD/ADDD/DIFF/EOM/YOBI),
 * generated from {@code DATEUT.cob} PROCEDURE DIVISION as ground truth.
 *
 * <p>DateutService is pure computation over a real (buffer-backed) WorkingStorage — no
 * repository/screen/file dependency to mock. Only {@code LocalDate.now()} (used by the TODY
 * function) is intercepted via {@code MockedStatic} so tests are deterministic; every other path
 * exercises the real civil-calendar arithmetic exactly as COBOL does.
 *
 * <p>Ground-truth review of DATEUT.cob vs DateutService.java found the conversion faithful for
 * every branch exercised below (EVALUATE dispatch, PARSE/BUILD/TOSER/FRSER/ CWD/DIM paragraphs, the
 * DIFF side effect that overwrites KD-DATE1 with KD-DATE2, and the duplicate "EOM"/"EOM " EVALUATE
 * arms) — no CONVERT-GAP cases were found for this module.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class DateutServiceTest {

    private DateutService service;
    private MockedStatic<LocalDate> mockedLocalDate;

    @BeforeEach
    void setUp() {
        service = new DateutService();
    }

    @AfterEach
    void tearDown() {
        if (mockedLocalDate != null) {
            mockedLocalDate.close();
            mockedLocalDate = null;
        }
    }

    private DateutLinkParm newParm(String kdFunc, int kdDate1, int kdDate2, int kdDays) {
        DateutLinkParm parm = new DateutLinkParm();
        parm.getKdate().setKdFunc(kdFunc);
        parm.getKdate().setKdDate1(kdDate1);
        parm.getKdate().setKdDate2(kdDate2);
        parm.getKdate().setKdDays(kdDays);
        return parm;
    }

    private int expectedCobolWeekday(int year, int month, int day) {
        // COBOL CWD-010: (serial+4) mod 7, wrapped to [0,6], 0=Sunday..6=Saturday.
        // java.time.DayOfWeek is MON=1..SUN=7, so value()%7 lands on the same 0=Sun scheme.
        return LocalDate.of(year, month, day).getDayOfWeek().getValue() % 7;
    }

    // ---------- TODY ----------

    @Test
    void execute_tody_yyBelow80_mapsToYear2000Plus_andComputesWeekday() {
        mockedLocalDate = mockStatic(LocalDate.class, CALLS_REAL_METHODS);
        LocalDate fixedToday = LocalDate.of(2026, 9, 18);
        mockedLocalDate.when(LocalDate::now).thenReturn(fixedToday);

        DateutLinkParm parm = newParm("TODY", 0, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdDate1()).isEqualTo(20260918);
        assertThat(parm.getKdate().getKdWeekday()).isEqualTo(expectedCobolWeekday(2026, 9, 18));
        assertThat(parm.getKdate().getKdStatus()).isEqualTo("00");
    }

    @Test
    void execute_tody_yyAtOrAbove80_mapsToYear1900Plus() {
        mockedLocalDate = mockStatic(LocalDate.class, CALLS_REAL_METHODS);
        LocalDate fixedToday = LocalDate.of(1985, 6, 15);
        mockedLocalDate.when(LocalDate::now).thenReturn(fixedToday);

        DateutLinkParm parm = newParm("TODY", 0, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdDate1()).isEqualTo(19850615);
        assertThat(parm.getKdate().getKdWeekday()).isEqualTo(expectedCobolWeekday(1985, 6, 15));
        assertThat(parm.getKdate().getKdStatus()).isEqualTo("00");
    }

    // ---------- VALD ----------

    @Test
    void execute_vald_leapYearFeb29_isValid() {
        DateutLinkParm parm = newParm("VALD", 20240229, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdStatus()).isEqualTo("00");
    }

    @Test
    void execute_vald_lastDayOfNonLeapFebruary_isValid() {
        DateutLinkParm parm = newParm("VALD", 20230228, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdStatus()).isEqualTo("00");
    }

    @Test
    void execute_vald_yearBelow1900_isInvalid() {
        DateutLinkParm parm = newParm("VALD", 18991231, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdStatus()).isEqualTo("99");
    }

    @Test
    void execute_vald_yearAbove2999_isInvalid() {
        DateutLinkParm parm = newParm("VALD", 30000101, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdStatus()).isEqualTo("99");
    }

    @Test
    void execute_vald_monthZero_isInvalid() {
        DateutLinkParm parm = newParm("VALD", 20240001, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdStatus()).isEqualTo("99");
    }

    @Test
    void execute_vald_monthAbove12_isInvalid() {
        DateutLinkParm parm = newParm("VALD", 20241301, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdStatus()).isEqualTo("99");
    }

    @Test
    void execute_vald_dayZero_isInvalid() {
        DateutLinkParm parm = newParm("VALD", 20240100, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdStatus()).isEqualTo("99");
    }

    @Test
    void execute_vald_dayExceedsDaysInNonLeapFebruary_isInvalid() {
        DateutLinkParm parm = newParm("VALD", 20230230, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdStatus()).isEqualTo("99");
    }

    @Test
    void execute_vald_thirtyOneDayMonths_lastDayValid_dayAfterInvalid() {
        int[] thirtyOneDayMonths = {1, 3, 5, 7, 8, 10, 12};
        for (int month : thirtyOneDayMonths) {
            DateutLinkParm lastDayValid = newParm("VALD", 20240000 + (month * 100) + 31, 0, 0);
            service.execute(lastDayValid);
            assertThat(lastDayValid.getKdate().getKdStatus())
                    .as("month %d day 31 should be valid", month)
                    .isEqualTo("00");

            service = new DateutService();
            DateutLinkParm dayAfterInvalid = newParm("VALD", 20240000 + (month * 100) + 32, 0, 0);
            service.execute(dayAfterInvalid);
            assertThat(dayAfterInvalid.getKdate().getKdStatus())
                    .as("month %d day 32 should be invalid", month)
                    .isEqualTo("99");

            service = new DateutService();
        }
    }

    @Test
    void execute_vald_thirtyDayMonths_lastDayValid_dayThirtyOneInvalid() {
        int[] thirtyDayMonths = {4, 6, 9, 11};
        for (int month : thirtyDayMonths) {
            DateutLinkParm lastDayValid = newParm("VALD", 20240000 + (month * 100) + 30, 0, 0);
            service.execute(lastDayValid);
            assertThat(lastDayValid.getKdate().getKdStatus())
                    .as("month %d day 30 should be valid", month)
                    .isEqualTo("00");

            service = new DateutService();
            DateutLinkParm dayThirtyOneInvalid =
                    newParm("VALD", 20240000 + (month * 100) + 31, 0, 0);
            service.execute(dayThirtyOneInvalid);
            assertThat(dayThirtyOneInvalid.getKdate().getKdStatus())
                    .as("month %d day 31 should be invalid", month)
                    .isEqualTo("99");

            service = new DateutService();
        }
    }

    @Test
    void execute_vald_year2000_divisibleBy400_isLeap_feb29Valid() {
        // DAYS-IN-MONTH: %4==0 -> leap=1; %100==0 -> leap=0; %400==0 -> leap=1 (2000 hits all
        // three).
        DateutLinkParm parm = newParm("VALD", 20000229, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdStatus()).isEqualTo("00");
    }

    @Test
    void execute_vald_year1900_divisibleBy100NotBy400_isNotLeap_feb29Invalid() {
        DateutLinkParm parm = newParm("VALD", 19000229, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdStatus()).isEqualTo("99");
    }

    // ---------- ADDD ----------

    @Test
    void execute_addd_positiveDaysAcrossMonthBoundary_rollsToNextMonth() {
        DateutLinkParm parm = newParm("ADDD", 20240101, 0, 31);

        service.execute(parm);

        assertThat(parm.getKdate().getKdDate1()).isEqualTo(20240201);
        assertThat(parm.getKdate().getKdStatus()).isEqualTo("00");
    }

    @Test
    void execute_addd_negativeDays_returnsPriorLeapDay() {
        DateutLinkParm parm = newParm("ADDD", 20240301, 0, -1);

        service.execute(parm);

        assertThat(parm.getKdate().getKdDate1()).isEqualTo(20240229);
    }

    // ---------- DIFF ----------

    @Test
    void execute_diff_computesPositiveDayCount_andOverwritesKdDate1WithKdDate2() {
        DateutLinkParm parm = newParm("DIFF", 20240101, 20240201, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdDays()).isEqualTo(31);
        // COBOL FN-DIFF-010 does "MOVE KD-DATE2 TO KD-DATE1" as a side effect before
        // re-parsing — the caller's KD-DATE1 ends up equal to KD-DATE2 after the call.
        assertThat(parm.getKdate().getKdDate1()).isEqualTo(20240201);
    }

    @Test
    void execute_diff_date2BeforeDate1_computesNegativeDayCount() {
        DateutLinkParm parm = newParm("DIFF", 20240201, 20240101, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdDays()).isEqualTo(-31);
    }

    // ---------- EOM ----------

    @Test
    void execute_eom_midMonth_returnsLastDayOfLeapFebruary() {
        DateutLinkParm parm = newParm("EOM ", 20240215, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdDate1()).isEqualTo(20240229);
    }

    @Test
    void execute_eom_december_rollsYearAndReturnsDec31() {
        DateutLinkParm parm = newParm("EOM ", 20241215, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdDate1()).isEqualTo(20241231);
    }

    @Test
    void execute_eomWithoutTrailingSpace_producesSameResultAsEomWithSpace() {
        DateutLinkParm withSpace = newParm("EOM ", 20240215, 0, 0);
        DateutLinkParm withoutSpace = newParm("EOM", 20240215, 0, 0);

        service.execute(withSpace);
        // Fresh service instance: the link/ws state must not leak across CALL-style invocations.
        service = new DateutService();
        service.execute(withoutSpace);

        assertThat(withoutSpace.getKdate().getKdDate1())
                .isEqualTo(withSpace.getKdate().getKdDate1());
        assertThat(withoutSpace.getKdate().getKdStatus()).isEqualTo("00");
    }

    // ---------- YOBI ----------

    @Test
    void execute_yobi_mondayDate_returnsWeekday1() {
        DateutLinkParm parm = newParm("YOBI", 20240101, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdWeekday()).isEqualTo(expectedCobolWeekday(2024, 1, 1));
        assertThat(parm.getKdate().getKdStatus()).isEqualTo("00");
    }

    @Test
    void execute_yobi_sundayDate_returnsWeekday0() {
        DateutLinkParm parm = newParm("YOBI", 20240107, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdWeekday()).isEqualTo(expectedCobolWeekday(2024, 1, 7));
        assertThat(parm.getKdate().getKdWeekday()).isEqualTo(0);
    }

    @Test
    void execute_yobi_dateBeforeEpoch_negativeSerialModuloCorrected() {
        // CWD-010: for dates before 1970-01-01 the serial day is negative, so
        // (WK-SER+4) mod 7 can itself be negative in Java's truncating "%" — exercises the
        // "IF WK-TMP < 0 ADD 7" correction branch that positive-serial dates never trigger.
        DateutLinkParm parm = newParm("YOBI", 19690101, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdWeekday()).isEqualTo(expectedCobolWeekday(1969, 1, 1));
    }

    // ---------- Unknown function ----------

    @Test
    void execute_unknownFunction_setsStatus99() {
        DateutLinkParm parm = newParm("XXXX", 20240101, 0, 0);

        service.execute(parm);

        assertThat(parm.getKdate().getKdStatus()).isEqualTo("99");
    }

    // ---------- Base-class lifecycle plumbing ----------

    @Test
    void getCompletionCode_and_setCompletionCode_delegateToWorkingStorage() {
        assertThat(service.getCompletionCode()).isEqualTo(0);

        service.setCompletionCode(12);

        assertThat(service.getCompletionCode()).isEqualTo(12);
    }

    @Test
    void getFileSet_returnsNull_dateutDeclaresNoFiles() {
        assertThat(service.getFileSet()).isNull();
    }

    @Test
    void execute_noArgEntryPoint_defaultKdFuncDispatchesToDefaultBranch_withoutThrowing() {
        // BatchServiceBase.execute() drives mainProcess() -> dispatchDateFunction() directly,
        // with the service's own (unset) link, whose KD-FUNC defaults to "    " (unknown) —
        // this exercises the EVALUATE OTHER branch and confirms ProgramExitSignal/commit
        // handling in the base class does not propagate an exception to the caller.
        service.execute();

        assertThat(service.getCompletionCode()).isEqualTo(0);
    }
}
