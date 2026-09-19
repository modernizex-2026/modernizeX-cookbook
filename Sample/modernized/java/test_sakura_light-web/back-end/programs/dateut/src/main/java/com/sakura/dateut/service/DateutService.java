package com.sakura.dateut.service;

import com.sakura.dateut.domain.DateutFieldAccess;
import com.sakura.dateut.domain.WorkingStorage;
import com.sakura.runtime.BatchServiceBase;
import com.sakura.runtime.DatasetEnums.*;
import com.sakura.runtime.ProgramExitSignal;
import com.sakura.runtime.Utility;
import com.sakura.runtime.io.AbstractDatasets;
import com.sakura.runtime.linkage.DateutLinkParm;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Business logic service generated from COBOL program DATEUT. */
@Service
@Scope("prototype")
public class DateutService extends BatchServiceBase {
    private final DateutLinkParm link = new DateutLinkParm();

    private final DateutFieldAccess ws = new DateutFieldAccess(new WorkingStorage());

    // Howard Hinnant civil-calendar algorithm constants (days_from_civil / civil_from_days).
    private static final int DATE_MMDD_SCALE = 10000;
    private static final int DATE_DD_SCALE = 100;
    private static final int CENTURY_DIVISOR = 100;
    private static final int ERA_LENGTH_YEARS = 400;
    private static final int DAYS_PER_ERA = 146097;
    private static final int EPOCH_SHIFT_DAYS = 719468;
    private static final int MONTH_OFFSET_DIVISOR = 153;
    private static final int DAYS_PER_YEAR = 365;
    private static final int MIN_SUPPORTED_YEAR = 1900;

    /**
     * Program entry point — runs the first COBOL paragraph via runChain. Base class run() wraps
     * this in StopRun/Abort/Exception handling.
     */
    @Override
    protected void mainProcess() {
        // Import anchors for types referenced by emitted paragraph bodies:
        // StopRunSignal, JobAbortException, ParagraphJumpSignal, ProgramExitSignal
        runChain(this::dispatchDateFunction);
    }

    /**
     * Returns COBOL COMPLETION-CODE after run() completes. Used by Tasklet to propagate exit code
     * into StepExecutionContext.
     */
    @Override
    public int getCompletionCode() {
        return ws.getCompletionCode();
    }

    /**
     * Set COBOL COMPLETION-CODE. Called by base class run() on Abort (255) / Exception (12) paths.
     */
    @Override
    protected void setCompletionCode(int code) {
        ws.setCompletionCode(code);
    }

    /**
     * Returns this program's FileSet (or null if no files declared). Base class run() uses this for
     * commit/rollback/closeAll lifecycle.
     */
    @Override
    protected AbstractDatasets getFileSet() {
        return null;
    }

    /** COBOL paragraph: MAIN-000 */
    private void dispatchDateFunction() {
        link.getKdate().setKdStatus("00");
        switch (String.valueOf(link.getKdate().getKdFunc())) {
            case "TODY" -> {
                runChain(this::computeTodayDate);
            }
            case "VALD" -> {
                runChain(this::validateDateFields);
            }
            case "ADDD" -> {
                runChain(this::addDaysToDate);
            }
            case "DIFF" -> {
                runChain(this::computeDateDifference);
            }
            case "EOM " -> {
                runChain(this::computeEndOfMonth);
            }
            case "EOM" -> {
                runChain(this::computeEndOfMonth);
            }
            case "YOBI" -> {
                runChain(this::computeWeekdayForDate);
            }
            default -> {
                link.getKdate().setKdStatus("99");
            }
        }
        throw new ProgramExitSignal();
    }

    /** COBOL paragraph: FN-TODAY-010 */
    private void computeTodayDate() {
        LocalDate accDateWk60 = LocalDate.now();
        String dateValWk60 = accDateWk60.format(DateTimeFormatter.ofPattern("yyMMdd"));
        ws.setWk6(Utility.parseNumeric(dateValWk60).intValue());
        ws.setWkYy((ws.getWk6() / DATE_MMDD_SCALE));
        ws.setWkM(((ws.getWk6() - (ws.getWkYy() * DATE_MMDD_SCALE)) / DATE_DD_SCALE));
        ws.setWkD((ws.getWk6() - ((ws.getWk6() / DATE_DD_SCALE) * DATE_DD_SCALE)));
        if (ws.getWkYy() < 80) {
            ws.setWkY((2000 + ws.getWkYy()));
        } else {
            ws.setWkY((MIN_SUPPORTED_YEAR + ws.getWkYy()));
        }
        runChain(this::buildDateFromYmd);
        runChain(this::convertYmdToSerialDay);
        runChain(this::computeWeekdayFromSerial);
    }

    /** COBOL paragraph: FN-VALIDATE-010 */
    private void validateDateFields() {
        runChain(this::parseDateToYmd);
        if (ws.getWkY() < MIN_SUPPORTED_YEAR || ws.getWkY() > 2999) {
            link.getKdate().setKdStatus("99");
            return;
        }
        if (ws.getWkM() < 1 || ws.getWkM() > 12) {
            link.getKdate().setKdStatus("99");
            return;
        }
        runChain(this::computeDaysInMonth);
        if (ws.getWkD() < 1 || ws.getWkD() > ws.getWkDim()) {
            link.getKdate().setKdStatus("99");
        }
    }

    /** COBOL paragraph: FN-ADDDAYS-010 */
    private void addDaysToDate() {
        runChain(this::parseDateToYmd);
        runChain(this::convertYmdToSerialDay);
        ws.setWkSer(ws.getWkSer() + link.getKdate().getKdDays());
        runChain(this::convertSerialDayToYmd);
        runChain(this::buildDateFromYmd);
    }

    /** COBOL paragraph: FN-DIFF-010 */
    private void computeDateDifference() {
        runChain(this::parseDateToYmd);
        runChain(this::convertYmdToSerialDay);
        ws.setWkSer2(ws.getWkSer());
        link.getKdate().setKdDate1(link.getKdate().getKdDate2());
        runChain(this::parseDateToYmd);
        runChain(this::convertYmdToSerialDay);
        link.getKdate().setKdDays((ws.getWkSer() - ws.getWkSer2()));
    }

    /** COBOL paragraph: FN-EOM-010 */
    private void computeEndOfMonth() {
        runChain(this::parseDateToYmd);
        ws.setWkD(1);
        ws.setWkM(ws.getWkM() + 1);
        if (ws.getWkM() > 12) {
            ws.setWkM(1);
            ws.setWkY(ws.getWkY() + 1);
        }
        runChain(this::buildDateFromYmd);
        runChain(this::parseDateToYmd);
        runChain(this::convertYmdToSerialDay);
        ws.setWkSer(ws.getWkSer() - (1));
        runChain(this::convertSerialDayToYmd);
        runChain(this::buildDateFromYmd);
    }

    /** COBOL paragraph: FN-WEEKDAY-010 */
    private void computeWeekdayForDate() {
        runChain(this::parseDateToYmd);
        runChain(this::convertYmdToSerialDay);
        runChain(this::computeWeekdayFromSerial);
    }

    /** COBOL paragraph: PARSE-010 */
    private void parseDateToYmd() {
        ws.setWkY((link.getKdate().getKdDate1() / DATE_MMDD_SCALE));
        ws.setWkM(
                ((link.getKdate().getKdDate1() - (ws.getWkY() * DATE_MMDD_SCALE)) / DATE_DD_SCALE));
        ws.setWkD(
                ((link.getKdate().getKdDate1() - (ws.getWkY() * DATE_MMDD_SCALE))
                        - (ws.getWkM() * DATE_DD_SCALE)));
    }

    /** COBOL paragraph: BUILD-010 */
    private void buildDateFromYmd() {
        link.getKdate()
                .setKdDate1(
                        (((ws.getWkY() * DATE_MMDD_SCALE) + (ws.getWkM() * DATE_DD_SCALE))
                                + ws.getWkD()));
    }

    /** COBOL paragraph: TOSER-010 */
    private void convertYmdToSerialDay() {
        ws.setWkY2(ws.getWkY());
        if (ws.getWkM() <= 2) {
            ws.setWkY2(ws.getWkY2() - (1));
        }
        ws.setWkEra((ws.getWkY2() / ERA_LENGTH_YEARS));
        ws.setWkYoe((ws.getWkY2() - (ws.getWkEra() * ERA_LENGTH_YEARS)));
        if (ws.getWkM() > 2) {
            ws.setWkMp((ws.getWkM() - 3));
        } else {
            ws.setWkMp((ws.getWkM() + 9));
        }
        ws.setWkDoy((((((MONTH_OFFSET_DIVISOR * ws.getWkMp()) + 2) / 5) + ws.getWkD()) - 1));
        ws.setWkDoe(
                ((((ws.getWkYoe() * DAYS_PER_YEAR) + (ws.getWkYoe() / 4))
                                - (ws.getWkYoe() / CENTURY_DIVISOR))
                        + ws.getWkDoy()));
        ws.setWkSer((((ws.getWkEra() * DAYS_PER_ERA) + ws.getWkDoe()) - EPOCH_SHIFT_DAYS));
    }

    /** COBOL paragraph: FRSER-010 */
    private void convertSerialDayToYmd() {
        ws.setWkZ((ws.getWkSer() + EPOCH_SHIFT_DAYS));
        ws.setWkEra((ws.getWkZ() / DAYS_PER_ERA));
        ws.setWkDoe((ws.getWkZ() - (ws.getWkEra() * DAYS_PER_ERA)));
        ws.setWkYoe(
                ((((ws.getWkDoe() - (ws.getWkDoe() / 1460)) + (ws.getWkDoe() / 36524))
                                - (ws.getWkDoe() / 146096))
                        / DAYS_PER_YEAR));
        ws.setWkY((ws.getWkYoe() + (ws.getWkEra() * ERA_LENGTH_YEARS)));
        ws.setWkDoy(
                (ws.getWkDoe()
                        - (((DAYS_PER_YEAR * ws.getWkYoe()) + (ws.getWkYoe() / 4))
                                - (ws.getWkYoe() / CENTURY_DIVISOR))));
        ws.setWkMp((((5 * ws.getWkDoy()) + 2) / MONTH_OFFSET_DIVISOR));
        ws.setWkD(((ws.getWkDoy() - (((MONTH_OFFSET_DIVISOR * ws.getWkMp()) + 2) / 5)) + 1));
        if (ws.getWkMp() < 10) {
            ws.setWkM((ws.getWkMp() + 3));
        } else {
            ws.setWkM((ws.getWkMp() - 9));
        }
        if (ws.getWkM() <= 2) {
            ws.setWkY(ws.getWkY() + 1);
        }
    }

    /** COBOL paragraph: CWD-010 */
    private void computeWeekdayFromSerial() {
        ws.setWkTmp((ws.getWkSer() + 4));
        ws.setWkTmp((ws.getWkTmp() - ((ws.getWkTmp() / 7) * 7)));
        if (ws.getWkTmp() < 0) {
            ws.setWkTmp(ws.getWkTmp() + 7);
        }
        link.getKdate().setKdWeekday(ws.getWkTmp());
    }

    /** COBOL paragraph: DIM-010 */
    private void computeDaysInMonth() {
        ws.setWkLeap(0);
        ws.setWkTmp((ws.getWkY() - ((ws.getWkY() / 4) * 4)));
        if (ws.getWkTmp() == 0) {
            ws.setWkLeap(1);
        }
        ws.setWkTmp((ws.getWkY() - ((ws.getWkY() / CENTURY_DIVISOR) * CENTURY_DIVISOR)));
        if (ws.getWkTmp() == 0) {
            ws.setWkLeap(0);
        }
        ws.setWkTmp((ws.getWkY() - ((ws.getWkY() / ERA_LENGTH_YEARS) * ERA_LENGTH_YEARS)));
        if (ws.getWkTmp() == 0) {
            ws.setWkLeap(1);
        }
        switch (ws.getWkM()) {
            case 1 -> {
                ws.setWkDim(31);
            }
            case 2 -> {
                if (ws.getWkLeap() == 1) {
                    ws.setWkDim(29);
                } else {
                    ws.setWkDim(28);
                }
            }
            case 3 -> {
                ws.setWkDim(31);
            }
            case 4 -> {
                ws.setWkDim(30);
            }
            case 5 -> {
                ws.setWkDim(31);
            }
            case 6 -> {
                ws.setWkDim(30);
            }
            case 7 -> {
                ws.setWkDim(31);
            }
            case 8 -> {
                ws.setWkDim(31);
            }
            case 9 -> {
                ws.setWkDim(30);
            }
            case 10 -> {
                ws.setWkDim(31);
            }
            case 11 -> {
                ws.setWkDim(30);
            }
            case 12 -> {
                ws.setWkDim(31);
            }
            default -> {
                ws.setWkDim(0);
            }
        }
    }

    /**
     * Execute this program as a COBOL CALL callee. Copies params into Linkage section, runs
     * business logic, copies back.
     */
    public void execute(DateutLinkParm params) {
        link.getKdate().setKdFunc(params.getKdate().getKdFunc());
        link.getKdate().setKdDate1(params.getKdate().getKdDate1());
        link.getKdate().setKdDate2(params.getKdate().getKdDate2());
        link.getKdate().setKdDays(params.getKdate().getKdDays());
        link.getKdate().setKdWeekday(params.getKdate().getKdWeekday());
        link.getKdate().setKdStatus(params.getKdate().getKdStatus());
        executeSubprogram(this::dispatchDateFunction);
        params.getKdate().setKdFunc(link.getKdate().getKdFunc());
        params.getKdate().setKdDate1(link.getKdate().getKdDate1());
        params.getKdate().setKdDate2(link.getKdate().getKdDate2());
        params.getKdate().setKdDays(link.getKdate().getKdDays());
        params.getKdate().setKdWeekday(link.getKdate().getKdWeekday());
        params.getKdate().setKdStatus(link.getKdate().getKdStatus());
    }
}
