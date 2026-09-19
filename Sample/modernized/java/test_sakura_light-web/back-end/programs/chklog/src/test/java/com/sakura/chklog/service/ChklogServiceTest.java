package com.sakura.chklog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sakura.chklog.runtime.ChklogDatasets;
import com.sakura.runtime.io.UserfDataset;
import com.sakura.runtime.linkage.ChklogLinkParm;
import com.sakura.runtime.linkage.ChklogLinkParm.Klogin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.concurrent.TimeUnit;

/**
 * Unit tests for ChklogService, converted from COBOL program CHKLOG (login/authority validator).
 * Ground truth for input/expected values is CHKLOG.cob + copybooks FUSER.cob / SUSER.cob.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChklogServiceTest {

    private UserfDataset userfSpy;
    private ChklogService service;

    @BeforeEach
    void setUp() {
        userfSpy = spy(new UserfDataset());
        ChklogDatasets fileSet =
                new ChklogDatasets() {
                    @Override
                    public UserfDataset getUserf() {
                        return userfSpy;
                    }
                };
        service = new ChklogService(fileSet);

        doNothing().when(userfSpy).open(any());
        doNothing().when(userfSpy).close();
        doReturn("00").when(userfSpy).getFileStatus();
        doReturn(true).when(userfSpy).readByKey(anyString(), any());
        doReturn(false).when(userfSpy).isInvalidKey();
    }

    private ChklogLinkParm buildParams(String login, String password) {
        ChklogLinkParm params = new ChklogLinkParm();
        params.getKlogin().setKlLogin(login);
        params.getKlogin().setKlPassword(password);
        return params;
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_openFails_setsStatus99AndSkipsReadAndClose() {
        // COBOL: OPEN INPUT USERF; IF FSTS NOT = "00" -> KL-STATUS=99, EXIT PROGRAM (no READ, no
        // CLOSE).
        doReturn("99").when(userfSpy).getFileStatus();

        ChklogLinkParm params = buildParams("USER01", "PASS0000000000");
        service.execute(params);

        assertEquals("99", params.getKlogin().getKlStatus());
        verify(userfSpy, never()).readByKey(anyString(), any());
        verify(userfSpy, never()).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_loginNotFound_setsStatus99AndSkipsAuthorityCheck() {
        // COBOL: READ USERF KEY IS US-LOGIN INVALID KEY -> KL-STATUS=99, CHECK-USER not performed.
        doReturn(true).when(userfSpy).isInvalidKey();

        ChklogLinkParm params = buildParams("NOSUCHUSER", "PASS0000000000");
        service.execute(params);

        assertEquals("99", params.getKlogin().getKlStatus());
        assertEquals(0, params.getKlogin().getKlUserCode());
        verify(userfSpy, times(1)).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_userDeleted_setsStatus99() {
        // COBOL CHECK-010: IF US-DEL-FLAG = 1 -> KL-STATUS=99, GO TO CHECK-999.
        userfSpy.buffer().setInt("US-DEL-FLAG", 1);

        ChklogLinkParm params = buildParams("USER01", "PASS0000000000");
        service.execute(params);

        assertEquals("99", params.getKlogin().getKlStatus());
        assertEquals(0, params.getKlogin().getKlUserCode());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_passwordMismatch_setsStatus99() {
        // COBOL CHECK-010: IF US-PASSWORD NOT = KL-PASSWORD -> KL-STATUS=99, GO TO CHECK-999.
        userfSpy.buffer().setInt("US-DEL-FLAG", 0);
        userfSpy.buffer().setString("US-PASSWORD", "RIGHTPASSWORD");

        ChklogLinkParm params = buildParams("USER01", "WRONGPASSWORD");
        service.execute(params);

        assertEquals("99", params.getKlogin().getKlStatus());
        assertEquals(0, params.getKlogin().getKlUserCode());
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_happyPath_copiesUserCodeNameRoleAndAuthorityFromUserf() {
        // COBOL CHECK-010 success path: copies US-CODE/US-NAME/US-ROLE and builds
        // WK-AUTH from the five US-AUTH-* 1-digit flags, in MASTER/ORDER/SALES/PURCH/CLOSE order.
        userfSpy.buffer().setInt("US-DEL-FLAG", 0);
        userfSpy.buffer().setString("US-PASSWORD", "SECRETPW");
        userfSpy.buffer().setInt("US-CODE", 123456);
        userfSpy.buffer().setString("US-NAME", "JOHN DOE");
        userfSpy.buffer().setInt("US-ROLE", 2);
        userfSpy.buffer().setInt("US-AUTH-MASTER", 1);
        userfSpy.buffer().setInt("US-AUTH-ORDER", 0);
        userfSpy.buffer().setInt("US-AUTH-SALES", 1);
        userfSpy.buffer().setInt("US-AUTH-PURCH", 0);
        userfSpy.buffer().setInt("US-AUTH-CLOSE", 1);

        ChklogLinkParm params = buildParams("USER01", "SECRETPW");
        service.execute(params);

        Klogin result = params.getKlogin();
        assertEquals("00", result.getKlStatus());
        assertEquals(123456, result.getKlUserCode());
        assertEquals("JOHN DOE", result.getKlUserName().trim());
        assertEquals(2, result.getKlRole());
        assertEquals("10101", result.getKlAuth());
        verify(userfSpy, times(1)).close();
    }

    @Test
    @Timeout(value = 10, unit = TimeUnit.SECONDS)
    void execute_badPassword_klUserNameAndKlAuthShouldBeFullyBlankPerCobol() {
        // CONVERT-GAP: COBOL MAIN-000 does MOVE SPACE TO KL-USER-NAME (PIC X(30) -> 30
        // spaces) and MOVE SPACE TO KL-AUTH (PIC X(5) -> 5 spaces) before the READ.
        // ChklogService.validateUserLogin() instead does setKlUserName(" ") /
        // setKlAuth(" ") — a single-space literal, not a field-width blank. On any
        // failure path (no CHECK-010 overwrite) the linkage fields come back 1 char
        // wide instead of COBOL's 30/5-char blank fields. This test encodes the
        // COBOL-correct expectation and is EXPECTED TO FAIL against current Java code.
        userfSpy.buffer().setString("US-PASSWORD", "RIGHTPASSWORD");

        ChklogLinkParm params = buildParams("USER01", "WRONGPASSWORD");
        service.execute(params);

        Klogin result = params.getKlogin();
        assertEquals("99", result.getKlStatus());
        assertEquals(" ".repeat(30), result.getKlUserName());
        assertEquals("     ", result.getKlAuth());
    }
}
