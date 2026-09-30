package com.sakura.credit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sakura.credit.domain.CreditFieldAccess;
import com.sakura.credit.domain.WorkingStorage;
import com.sakura.credit.runtime.CreditDatasets;
import com.sakura.runtime.io.CustfDataset;
import com.sakura.runtime.linkage.CreditLinkParm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for CreditService, converted from COBOL program CREDIT (Sakura sub programs). Ground
 * truth: CREDIT.cob MAIN-000 / OPEN-010 / CHK-010.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 10, unit = TimeUnit.SECONDS)
class CreditServiceTest {

    // Real CustfDataset instance (needed so its record buffer/layout loads correctly),
    // wrapped as a Mockito spy so we can stub only the I/O methods.
    @Spy private CustfDataset custf = new CustfDataset();

    // CreditDatasets normally builds its own private CustfDataset internally; we
    // override getCustf() to hand back our spy instead, so CreditService (constructed
    // below) and our test-side field-access helper both operate on the same buffer.
    private final CreditDatasets fileSet =
            new CreditDatasets() {
                @Override
                public CustfDataset getCustf() {
                    return custf;
                }
            };

    private CreditService service;

    // Test-side accessor onto the SAME CUSTF record buffer, used to seed CU-BALANCE /
    // CU-CREDIT-LIMIT as if a real keyed READ had populated them.
    private CreditFieldAccess custfSeed;

    @BeforeEach
    void setUp() {
        service = new CreditService(fileSet);
        custfSeed = new CreditFieldAccess(new WorkingStorage(), fileSet);
        doNothing().when(custf).open(any());
        doNothing().when(custf).close();
    }

    private CreditLinkParm.Kcred buildRequest(int custCode, String amount) {
        CreditLinkParm params = new CreditLinkParm();
        params.getKcred().setKcCust(custCode);
        params.getKcred().setKcAmount(new BigDecimal(amount));
        return params.getKcred();
    }

    private CreditLinkParm toParm(CreditLinkParm.Kcred kcred) {
        CreditLinkParm params = new CreditLinkParm();
        params.setKcred(kcred);
        return params;
    }

    // ── happy path ──────────────────────────────────────────────────────

    @Test
    void execute_balancePlusAmountWithinLimit_notExceeded() {
        // COBOL: KC-LIMIT > 0 AND KC-NEWBAL > KC-LIMIT is FALSE when newbal < limit.
        doReturn("00").when(custf).getFileStatus();
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custfSeed.setCuCreditLimit(new BigDecimal("1000"));
        custfSeed.setCuBalance(new BigDecimal("500"));

        CreditLinkParm.Kcred kcred = buildRequest(100001, "200");
        CreditLinkParm params = toParm(kcred);

        service.execute(params);

        assertThat(params.getKcred().getKcStatus()).isEqualTo("00");
        assertThat(params.getKcred().getKcExceed()).isEqualTo(0);
        assertThat(params.getKcred().getKcLimit()).isEqualByComparingTo("1000");
        assertThat(params.getKcred().getKcBalance()).isEqualByComparingTo("500");
        assertThat(params.getKcred().getKcNewbal()).isEqualByComparingTo("700");
        verify(custf, times(1)).readByKey(any());
    }

    @Test
    void execute_newBalanceEqualsLimit_notExceeded() {
        // COBOL boundary: "KC-NEWBAL > KC-LIMIT" is strictly-greater, so equality does not exceed.
        doReturn("00").when(custf).getFileStatus();
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custfSeed.setCuCreditLimit(new BigDecimal("1000"));
        custfSeed.setCuBalance(new BigDecimal("800"));

        CreditLinkParm params = toParm(buildRequest(100002, "200"));

        service.execute(params);

        assertThat(params.getKcred().getKcNewbal()).isEqualByComparingTo("1000");
        assertThat(params.getKcred().getKcExceed()).isEqualTo(0);
        assertThat(params.getKcred().getKcStatus()).isEqualTo("00");
    }

    @Test
    void execute_balancePlusAmountExceedsLimit_flaggedExceeded() {
        doReturn("00").when(custf).getFileStatus();
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custfSeed.setCuCreditLimit(new BigDecimal("1000"));
        custfSeed.setCuBalance(new BigDecimal("900"));

        CreditLinkParm params = toParm(buildRequest(100003, "200"));

        service.execute(params);

        assertThat(params.getKcred().getKcNewbal()).isEqualByComparingTo("1100");
        assertThat(params.getKcred().getKcExceed()).isEqualTo(1);
        assertThat(params.getKcred().getKcStatus()).isEqualTo("00");
    }

    // ── edge cases ──────────────────────────────────────────────────────

    @Test
    void execute_creditLimitZero_treatedAsNoLimit_neverExceeds() {
        // COBOL: "IF KC-LIMIT > 0 AND ..." — a zero limit means the AND short-circuits false.
        doReturn("00").when(custf).getFileStatus();
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custfSeed.setCuCreditLimit(BigDecimal.ZERO);
        custfSeed.setCuBalance(new BigDecimal("500"));

        CreditLinkParm params = toParm(buildRequest(100004, "1000000"));

        service.execute(params);

        assertThat(params.getKcred().getKcLimit()).isEqualByComparingTo("0");
        assertThat(params.getKcred().getKcExceed()).isEqualTo(0);
        assertThat(params.getKcred().getKcStatus()).isEqualTo("00");
    }

    @Test
    void execute_negativeAmountReducesBalance_notExceeded() {
        doReturn("00").when(custf).getFileStatus();
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custfSeed.setCuCreditLimit(new BigDecimal("1000"));
        custfSeed.setCuBalance(new BigDecimal("500"));

        CreditLinkParm params = toParm(buildRequest(100005, "-300"));

        service.execute(params);

        assertThat(params.getKcred().getKcNewbal()).isEqualByComparingTo("200");
        assertThat(params.getKcred().getKcExceed()).isEqualTo(0);
        assertThat(params.getKcred().getKcStatus()).isEqualTo("00");
    }

    @Test
    void execute_fileStatusThirtyFive_recreatesFileThenSucceeds() {
        // COBOL OPEN-010: FSTS = "35" or "30" triggers OPEN OUTPUT/CLOSE/OPEN INPUT recovery.
        doReturn("35", "00").when(custf).getFileStatus();
        doReturn(true).when(custf).readByKey(any());
        doReturn(false).when(custf).isInvalidKey();
        custfSeed.setCuCreditLimit(new BigDecimal("1000"));
        custfSeed.setCuBalance(new BigDecimal("500"));

        CreditLinkParm params = toParm(buildRequest(100006, "100"));

        service.execute(params);

        assertThat(params.getKcred().getKcStatus()).isEqualTo("00");
        assertThat(params.getKcred().getKcExceed()).isEqualTo(0);
        verify(custf, times(3)).open(any());
        verify(custf, times(2)).close();
    }

    // ── error paths ─────────────────────────────────────────────────────

    @Test
    void execute_customerNotFound_invalidKey_statusNinetyNineAndFieldsStayZero() {
        // COBOL CHK-010: READ INVALID KEY -> MOVE "99" TO KC-STATUS, GO TO CHK-999 (skips
        // KC-LIMIT/KC-BALANCE/KC-NEWBAL/KC-EXCEED, which stay at MAIN-000's zero-init).
        doReturn("00").when(custf).getFileStatus();
        doReturn(false).when(custf).readByKey(any());
        doReturn(true).when(custf).isInvalidKey();

        CreditLinkParm params = toParm(buildRequest(999999, "100"));

        service.execute(params);

        assertThat(params.getKcred().getKcStatus()).isEqualTo("99");
        assertThat(params.getKcred().getKcExceed()).isEqualTo(0);
        assertThat(params.getKcred().getKcLimit()).isEqualByComparingTo("0");
        assertThat(params.getKcred().getKcBalance()).isEqualByComparingTo("0");
        assertThat(params.getKcred().getKcNewbal()).isEqualByComparingTo("0");
    }

    @Test
    void execute_openFileFailsNonRecoverableStatus_statusNinetyNineAndCreditNeverChecked() {
        // COBOL MAIN-000: IF FSTS NOT = "00" after OPEN-FILE -> MOVE "99" TO KC-STATUS,
        // CLOSE-FILE, EXIT PROGRAM — CHECK-CREDIT (and thus READ CUSTF) never runs.
        doReturn("93").when(custf).getFileStatus();

        CreditLinkParm params = toParm(buildRequest(100007, "100"));

        service.execute(params);

        assertThat(params.getKcred().getKcStatus()).isEqualTo("99");
        verify(custf, never()).readByKey(any());
        verify(custf, times(1)).close();
    }
}
