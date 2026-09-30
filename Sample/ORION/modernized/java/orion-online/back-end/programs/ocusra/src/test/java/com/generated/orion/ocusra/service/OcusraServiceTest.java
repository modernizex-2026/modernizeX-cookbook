package com.generated.orion.ocusra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.appruntime.AppService;
import com.appruntime.FormatTimeResult;
import com.generated.orion.ocusra.accessor.OcusraFields;
import com.generated.orion.ocusra.model.WorkingStorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Unit tests for OcusraService, generated from COBOL program OCUSRA. All business logic is private
 * and is exercised solely through the public {@link OcusraService#mainLine(AppService)} entry
 * point, driven by an {@link AppService} mock that emulates CICS SEND/RECEIVE/WRITE/XCTL/RETURN.
 */
@ExtendWith(MockitoExtension.class)
@Timeout(value = 10, unit = java.util.concurrent.TimeUnit.SECONDS)
class OcusraServiceTest {

    private static final String USRSEC = "USRSEC";
    private static final String MAP = "MUSRAA";

    private static final String MSG_PROMPT = "Enter new user details and press ENTER.";
    private static final String MSG_ID_REQ = "User id is required.";
    private static final String MSG_FNAME_REQ = "First name is required.";
    private static final String MSG_LNAME_REQ = "Last name is required.";
    private static final String MSG_PWD_REQ = "Password is required.";
    private static final String MSG_TYPE_BAD = "Type must be A (admin) or U (user).";
    private static final String MSG_OK = "User added successfully.";
    private static final String MSG_DUP = "User id already exists.";
    private static final String MSG_WRITE_ERR = "Error writing user file.";
    private static final String MSG_INVALID_KEY = "Invalid key pressed. Please try again.";

    @Mock private AppService appService;

    private OcusraService service;

    /** Tracks the EIBRESP value the next getEibresp() call should return. */
    private final AtomicInteger nextResp = new AtomicInteger(0);

    @BeforeEach
    void setUp() {
        service = new OcusraService();
        lenient().when(appService.getEibresp()).thenAnswer(inv -> nextResp.get());
        lenient()
                .when(appService.formatTime(any(), anyString(), anyString(), anyString()))
                .thenReturn(new FormatTimeResult("2026-09-22", "10:00:00"));
    }

    /** Builds a serialized ORION-COMMAREA string carrying the given CA-PGM-CONTEXT. */
    private String commareaWithContext(int context) {
        OcusraFields helper = new OcusraFields(new WorkingStorage());
        helper.setCaPgmContext(context);
        return helper.getOrionCommarea();
    }

    /** Stubs a non-zero EIBCALEN and a pseudo-conversational commarea for the given context. */
    private void givenPseudoConversation(int context) {
        when(appService.getEibcalen()).thenReturn(100);
        when(appService.getSerializedCommarea()).thenReturn(commareaWithContext(context));
    }

    /** Stubs receiveMap to populate the MUSRAAI input fields captured from production code. */
    private void givenReceiveMapPopulates(Consumer<OcusraFields> populate) {
        doAnswer(
                        inv -> {
                            OcusraFields f = inv.getArgument(1);
                            populate.accept(f);
                            return null;
                        })
                .when(appService)
                .receiveMap(eq(MAP), any());
    }

    private void givenWriteFile(int resp) {
        doAnswer(
                        inv -> {
                            nextResp.set(resp);
                            return null;
                        })
                .when(appService)
                .writeFile(
                        org.mockito.ArgumentMatchers.argThat(s -> s.trim().equals(USRSEC)),
                        any(),
                        anyString(),
                        anyInt());
    }

    /** Populates a full set of valid input fields for 2100-ADD-USER. */
    private void givenValidUserInput() {
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("jdoe");
                    f.setUsfnami("John");
                    f.setUslnami("Doe");
                    f.setUspwdi("secret1");
                    f.setUstypei("U");
                });
    }

    // ───────────────────────── 0000-MAIN / 1000-SEND-INITIAL ─────────────────────────

    @Test
    void mainLine_firstCallEibcalenZero_sendsInitialScreenWithPrompt() {
        when(appService.getEibcalen()).thenReturn(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcusraFields out = (OcusraFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
        assertThat(out.getTrnnameo().trim()).isEqualTo("ORUA");
        assertThat(out.getPgmnameo().trim()).isEqualTo("OCUSRA");
        verify(appService).returnTransid(eq("ORUA"), any(), eq(692));
    }

    @Test
    void mainLine_pseudoConversationalFirstEnter_contextZero_sendsInitialScreen() {
        givenPseudoConversation(0);

        service.mainLine(appService);

        verify(appService).sendMap(eq(MAP), any(), any(), eq(true), eq(false), eq(false));
        verify(appService).returnTransid(eq("ORUA"), any(), eq(692));
    }

    // ───────────────────────── 2000-PROCESS-INPUT ─────────────────────────

    @Test
    void mainLine_pf3Pressed_transfersControlToAdminMenu() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("3");

        service.mainLine(appService);

        verify(appService)
                .xctl(
                        org.mockito.ArgumentMatchers.argThat(s -> s.trim().equals("OCADMEN")),
                        any(),
                        eq(692));
        verify(appService, never())
                .sendMap(anyString(), any(), any(), anyBoolean(), anyBoolean(), anyBoolean());
    }

    @Test
    void mainLine_pf4Pressed_clearsAndResendsInitialScreen() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("4");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP), sendMapOut.capture(), any(), eq(true), eq(false), eq(false));
        OcusraFields out = (OcusraFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_PROMPT);
    }

    @Test
    void mainLine_unmappedKeyPressed_showsInvalidKeyMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("X");

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusraFields out = (OcusraFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_INVALID_KEY);
        verify(appService, never()).receiveMap(anyString(), any());
    }

    // ───────────────────────── 2100-ADD-USER happy path ─────────────────────────

    @Test
    void mainLine_addUser_allFieldsValid_writesUserAndConfirms() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidUserInput();
        givenWriteFile(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> userWritten = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .writeFile(
                        org.mockito.ArgumentMatchers.argThat(s -> s.trim().equals(USRSEC)),
                        userWritten.capture(),
                        anyString(),
                        anyInt());
        OcusraFields written = (OcusraFields) userWritten.getValue();
        assertThat(written.getUsId().trim()).isEqualTo("jdoe");
        assertThat(written.getUsFirstName().trim()).isEqualTo("John");
        assertThat(written.getUsLastName().trim()).isEqualTo("Doe");
        assertThat(written.getUsPassword().trim()).isEqualTo("secret1");
        assertThat(written.getUsType().trim()).isEqualTo("U");

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusraFields out = (OcusraFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(MSG_OK);
    }

    @Test
    void mainLine_addUser_typeAdmin_writesAdminType() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("admin1");
                    f.setUsfnami("Ann");
                    f.setUslnami("Admin");
                    f.setUspwdi("adminpwd");
                    f.setUstypei("A");
                });
        givenWriteFile(0);

        service.mainLine(appService);

        ArgumentCaptor<Object> userWritten = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .writeFile(
                        org.mockito.ArgumentMatchers.argThat(s -> s.trim().equals(USRSEC)),
                        userWritten.capture(),
                        anyString(),
                        anyInt());
        OcusraFields written = (OcusraFields) userWritten.getValue();
        assertThat(written.getUsType().trim()).isEqualTo("A");
    }

    // ───────────────────────── 2120-VALIDATE-INPUT edge cases ─────────────────────────

    @Test
    void mainLine_addUser_useridBlank_rejectsBeforeWrite() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("        ");
                    f.setUsfnami("John");
                    f.setUslnami("Doe");
                    f.setUspwdi("secret1");
                    f.setUstypei("U");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_ID_REQ);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    @Test
    void mainLine_addUser_firstNameBlank_rejectsWithFnameReqMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("jdoe");
                    f.setUsfnami("                    ");
                    f.setUslnami("Doe");
                    f.setUspwdi("secret1");
                    f.setUstypei("U");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_FNAME_REQ);
    }

    @Test
    void mainLine_addUser_lastNameBlank_rejectsWithLnameReqMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("jdoe");
                    f.setUsfnami("John");
                    f.setUslnami("                    ");
                    f.setUspwdi("secret1");
                    f.setUstypei("U");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_LNAME_REQ);
    }

    @Test
    void mainLine_addUser_passwordBlank_rejectsWithPwdReqMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("jdoe");
                    f.setUsfnami("John");
                    f.setUslnami("Doe");
                    f.setUspwdi("        ");
                    f.setUstypei("U");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_PWD_REQ);
    }

    @Test
    void mainLine_addUser_typeInvalid_rejectsWithTypeBadMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("jdoe");
                    f.setUsfnami("John");
                    f.setUslnami("Doe");
                    f.setUspwdi("secret1");
                    f.setUstypei("Z");
                });

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_TYPE_BAD);
        verify(appService, never()).writeFile(anyString(), any(), anyString(), anyInt());
    }

    // ───────────────────────── 3000-WRITE-USER error branches ─────────────────────────

    @Test
    void mainLine_writeUser_duplicateId_rejectsWithDupMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidUserInput();
        givenWriteFile(14); // DFHRESP(DUPREC)

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_DUP);
        verify(appService)
                .writeFile(
                        org.mockito.ArgumentMatchers.argThat(s -> s.trim().equals(USRSEC)),
                        any(),
                        anyString(),
                        anyInt());
    }

    @Test
    void mainLine_writeUser_unexpectedFileError_rejectsWithWriteErrMessage() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenValidUserInput();
        givenWriteFile(99); // unexpected error

        service.mainLine(appService);

        assertFinalErrorMessage(MSG_WRITE_ERR);
    }

    @Test
    void mainLine_addUser_repopulatesEnteredFieldsOnValidationError() {
        givenPseudoConversation(1);
        when(appService.getEibaid()).thenReturn("'");
        givenReceiveMapPopulates(
                f -> {
                    f.setUseridi("jdoe");
                    f.setUsfnami("John");
                    f.setUslnami("Doe");
                    f.setUspwdi("secret1");
                    f.setUstypei("Q"); // invalid type -> validation fails after echoing fields
                });

        service.mainLine(appService);

        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusraFields out = (OcusraFields) sendMapOut.getValue();
        assertThat(out.getUserido().trim()).isEqualTo("jdoe");
        assertThat(out.getUsfnamo().trim()).isEqualTo("John");
        assertThat(out.getUslnamo().trim()).isEqualTo("Doe");
    }

    // ───────────────────────── trivial metadata accessors ─────────────────────────

    @Test
    void getProgramName_returnsOcusra() {
        assertEquals("OCUSRA", service.getProgramName());
    }

    @Test
    void getTransId_returnsOrua() {
        assertEquals("ORUA", service.getTransId());
    }

    @Test
    void getButtonDefs_delegatesToMetadataWithoutError() {
        assertNotNull(service.getButtonDefs());
    }

    @Test
    void getFieldMapping_delegatesToMetadataForKnownMap() {
        assertNotNull(service.getFieldMapping(MAP));
    }

    @Test
    void getFieldMapping_unknownMap_returnsEmptyMapping() {
        assertNotNull(service.getFieldMapping("UNKNOWN"));
    }

    // ───────────────────────── shared assertion helper ─────────────────────────

    private void assertFinalErrorMessage(String expectedMessage) {
        ArgumentCaptor<Object> sendMapOut = ArgumentCaptor.forClass(Object.class);
        verify(appService)
                .sendMap(eq(MAP), sendMapOut.capture(), any(), eq(false), eq(false), eq(false));
        OcusraFields out = (OcusraFields) sendMapOut.getValue();
        assertThat(out.getErrmsgo().trim()).isEqualTo(expectedMessage);
    }
}
