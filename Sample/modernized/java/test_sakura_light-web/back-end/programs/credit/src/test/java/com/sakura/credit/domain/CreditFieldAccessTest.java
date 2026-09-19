package com.sakura.credit.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.sakura.credit.runtime.CreditDatasets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

/**
 * Direct round-trip tests for CreditFieldAccess typed wrappers (WORKING-STORAGE COMPLETION-CODE /
 * FSTS and CUSTF record fields CU-BALANCE / CU-CODE / CU-CREDIT-LIMIT). No I/O involved — these are
 * pure buffer get/set delegations.
 */
class CreditFieldAccessTest {

    private CreditFieldAccess ws;

    @BeforeEach
    void setUp() {
        ws = new CreditFieldAccess(new WorkingStorage(), new CreditDatasets());
    }

    @Test
    void completionCode_setThenGet_roundTrips() {
        ws.setCompletionCode(255);

        assertThat(ws.getCompletionCode()).isEqualTo(255);
    }

    @Test
    void cuBalance_setThenGet_roundTrips() {
        ws.setCuBalance(new BigDecimal("12345"));

        assertThat(ws.getCuBalance()).isEqualByComparingTo("12345");
    }

    @Test
    void cuBalance_negativeValue_roundTrips() {
        ws.setCuBalance(new BigDecimal("-999"));

        assertThat(ws.getCuBalance()).isEqualByComparingTo("-999");
    }

    @Test
    void cuCode_setThenGet_roundTrips() {
        ws.setCuCode(100042);

        assertThat(ws.getCuCode()).isEqualTo(100042);
    }

    @Test
    void cuCreditLimit_setThenGet_roundTrips() {
        ws.setCuCreditLimit(new BigDecimal("50000"));

        assertThat(ws.getCuCreditLimit()).isEqualByComparingTo("50000");
    }

    @Test
    void fsts_setThenGet_roundTrips() {
        ws.setFsts("35");

        assertThat(ws.getFsts()).isEqualTo("35");
    }
}
