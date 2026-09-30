package com.sakura.rp0130.domain;

import com.sakura.rp0130.runtime.Rp0130Datasets;
import com.sakura.runtime.record.RuntimeFieldAccess;

import java.math.BigDecimal;

/**
 * Field accessor for RP0130. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class Rp0130FieldAccess extends RuntimeFieldAccess {

    public Rp0130FieldAccess(WorkingStorage ws, Rp0130Datasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getCustf() != null) {
            register(fileSet.getCustf().buffer());
        }
        if (fileSet != null && fileSet.getArlf() != null) {
            register(fileSet.getArlf().buffer());
        }
        if (fileSet != null && fileSet.getSyscf() != null) {
            register(fileSet.getSyscf().buffer());
        }
        if (fileSet != null && fileSet.getRepf() != null) {
            register(fileSet.getRepf().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public BigDecimal getAlCredit() {
        return getDecimal("AL-CREDIT");
    }

    public void setAlCredit(BigDecimal value) {
        setDecimal("AL-CREDIT", value);
    }

    public int getAlCust() {
        return getInt("AL-CUST");
    }

    public void setAlCust(int value) {
        setInt("AL-CUST", value);
    }

    public int getAlDate() {
        return getInt("AL-DATE");
    }

    public void setAlDate(int value) {
        setInt("AL-DATE", value);
    }

    public BigDecimal getAlDebit() {
        return getDecimal("AL-DEBIT");
    }

    public void setAlDebit(BigDecimal value) {
        setDecimal("AL-DEBIT", value);
    }

    public int getAlKind() {
        return getInt("AL-KIND");
    }

    public void setAlKind(int value) {
        setInt("AL-KIND", value);
    }

    public long getAlRefNo() {
        return getLong("AL-REF-NO");
    }

    public void setAlRefNo(long value) {
        setLong("AL-REF-NO", value);
    }

    public String getAlRemark() {
        return getString("AL-REMARK");
    }

    public void setAlRemark(String value) {
        setString("AL-REMARK", value);
    }

    public long getClBal() {
        return getLong("CL-BAL");
    }

    public void setClBal(long value) {
        setLong("CL-BAL", value);
    }

    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public int getCuCode() {
        return getInt("CU-CODE");
    }

    public void setCuCode(int value) {
        setInt("CU-CODE", value);
    }

    public int getCuDelFlag() {
        return getInt("CU-DEL-FLAG");
    }

    public void setCuDelFlag(int value) {
        setInt("CU-DEL-FLAG", value);
    }

    public String getCuName() {
        return getString("CU-NAME");
    }

    public void setCuName(String value) {
        setString("CU-NAME", value);
    }

    public String getFsts() {
        return getString("FSTS");
    }

    public void setFsts(String value) {
        setString("FSTS", value);
    }

    public String getH1Company() {
        return getString("H1-COMPANY");
    }

    public void setH1Company(String value) {
        setString("H1-COMPANY", value);
    }

    public int getH1Page() {
        return getInt("H1-PAGE");
    }

    public void setH1Page(int value) {
        setInt("H1-PAGE", value);
    }

    public String getH1Title() {
        return getString("H1-TITLE");
    }

    public void setH1Title(String value) {
        setString("H1-TITLE", value);
    }

    public int getH2Date() {
        return getInt("H2-DATE");
    }

    public void setH2Date(int value) {
        setInt("H2-DATE", value);
    }

    public String getH2Info() {
        return getString("H2-INFO");
    }

    public void setH2Info(String value) {
        setString("H2-INFO", value);
    }

    public String getKaDetail() {
        return getString("KA-DETAIL");
    }

    public void setKaDetail(String value) {
        setString("KA-DETAIL", value);
    }

    public String getKaFile() {
        return getString("KA-FILE");
    }

    public void setKaFile(String value) {
        setString("KA-FILE", value);
    }

    public String getKaFsts() {
        return getString("KA-FSTS");
    }

    public void setKaFsts(String value) {
        setString("KA-FSTS", value);
    }

    public String getKaMsgcode() {
        return getString("KA-MSGCODE");
    }

    public void setKaMsgcode(String value) {
        setString("KA-MSGCODE", value);
    }

    public String getKaProgid() {
        return getString("KA-PROGID");
    }

    public void setKaProgid(String value) {
        setString("KA-PROGID", value);
    }

    public String getKabend() {
        return groupToString("KABEND");
    }

    public void setKabend(String value) {
        setGroup("KABEND", value);
    }

    public int getKdDate1() {
        return getInt("KD-DATE1");
    }

    public void setKdDate1(int value) {
        setInt("KD-DATE1", value);
    }

    public int getKdDate2() {
        return getInt("KD-DATE2");
    }

    public void setKdDate2(int value) {
        setInt("KD-DATE2", value);
    }

    public int getKdDays() {
        return getInt("KD-DAYS");
    }

    public void setKdDays(int value) {
        setInt("KD-DAYS", value);
    }

    public String getKdFunc() {
        return getString("KD-FUNC");
    }

    public void setKdFunc(String value) {
        setString("KD-FUNC", value);
    }

    public String getKdStatus() {
        return getString("KD-STATUS");
    }

    public void setKdStatus(String value) {
        setString("KD-STATUS", value);
    }

    public int getKdWeekday() {
        return getInt("KD-WEEKDAY");
    }

    public void setKdWeekday(int value) {
        setInt("KD-WEEKDAY", value);
    }

    public String getKdate() {
        return groupToString("KDATE");
    }

    public void setKdate(String value) {
        setGroup("KDATE", value);
    }

    public long getOpBal() {
        return getLong("OP-BAL");
    }

    public void setOpBal(long value) {
        setLong("OP-BAL", value);
    }

    public String getRcHead() {
        return groupToString("RC-HEAD");
    }

    public void setRcHead(String value) {
        setGroup("RC-HEAD", value);
    }

    public String getRdClose() {
        return groupToString("RD-CLOSE");
    }

    public void setRdClose(String value) {
        setGroup("RD-CLOSE", value);
    }

    public String getRdLine() {
        return groupToString("RD-LINE");
    }

    public void setRdLine(String value) {
        setGroup("RD-LINE", value);
    }

    public String getRdOpen() {
        return groupToString("RD-OPEN");
    }

    public void setRdOpen(String value) {
        setGroup("RD-OPEN", value);
    }

    public String getRdStmt() {
        return groupToString("RD-STMT");
    }

    public void setRdStmt(String value) {
        setGroup("RD-STMT", value);
    }

    public String getRepRec() {
        return getString("REP-REC");
    }

    public void setRepRec(String value) {
        setString("REP-REC", value);
    }

    public long getRlBal() {
        return getLong("RL-BAL");
    }

    public void setRlBal(long value) {
        setLong("RL-BAL", value);
    }

    public long getRlCredit() {
        return getLong("RL-CREDIT");
    }

    public void setRlCredit(long value) {
        setLong("RL-CREDIT", value);
    }

    public int getRlDate() {
        return getInt("RL-DATE");
    }

    public void setRlDate(int value) {
        setInt("RL-DATE", value);
    }

    public long getRlDebit() {
        return getLong("RL-DEBIT");
    }

    public void setRlDebit(long value) {
        setLong("RL-DEBIT", value);
    }

    public String getRlKind() {
        return getString("RL-KIND");
    }

    public void setRlKind(String value) {
        setString("RL-KIND", value);
    }

    public long getRlRef() {
        return getLong("RL-REF");
    }

    public void setRlRef(long value) {
        setLong("RL-REF", value);
    }

    public String getRlRemark() {
        return getString("RL-REMARK");
    }

    public void setRlRemark(String value) {
        setString("RL-REMARK", value);
    }

    public String getRptH1() {
        return groupToString("RPT-H1");
    }

    public void setRptH1(String value) {
        setGroup("RPT-H1", value);
    }

    public String getRptH2() {
        return groupToString("RPT-H2");
    }

    public void setRptH2(String value) {
        setGroup("RPT-H2", value);
    }

    public String getRptRule() {
        return getString("RPT-RULE");
    }

    public void setRptRule(String value) {
        setString("RPT-RULE", value);
    }

    public int getStCode() {
        return getInt("ST-CODE");
    }

    public void setStCode(int value) {
        setInt("ST-CODE", value);
    }

    public String getStName() {
        return getString("ST-NAME");
    }

    public void setStName(String value) {
        setString("ST-NAME", value);
    }

    public String getSyCompanyName() {
        return getString("SY-COMPANY-NAME");
    }

    public void setSyCompanyName(String value) {
        setString("SY-COMPANY-NAME", value);
    }

    public int getSyKey() {
        return getInt("SY-KEY");
    }

    public void setSyKey(int value) {
        setInt("SY-KEY", value);
    }

    public int getWkArlEof() {
        return getInt("WK-ARL-EOF");
    }

    public void setWkArlEof(int value) {
        setInt("WK-ARL-EOF", value);
    }

    public String getWkCompany() {
        return getString("WK-COMPANY");
    }

    public void setWkCompany(String value) {
        setString("WK-COMPANY", value);
    }

    public int getWkCustFrom() {
        return getInt("WK-CUST-FROM");
    }

    public void setWkCustFrom(int value) {
        setInt("WK-CUST-FROM", value);
    }

    public int getWkCustTo() {
        return getInt("WK-CUST-TO");
    }

    public void setWkCustTo(int value) {
        setInt("WK-CUST-TO", value);
    }

    public int getWkDateFrom() {
        return getInt("WK-DATE-FROM");
    }

    public void setWkDateFrom(int value) {
        setInt("WK-DATE-FROM", value);
    }

    public int getWkDateTo() {
        return getInt("WK-DATE-TO");
    }

    public void setWkDateTo(int value) {
        setInt("WK-DATE-TO", value);
    }

    public int getWkHdrDone() {
        return getInt("WK-HDR-DONE");
    }

    public void setWkHdrDone(int value) {
        setInt("WK-HDR-DONE", value);
    }

    public String getWkInCfrom() {
        return getString("WK-IN-CFROM");
    }

    public void setWkInCfrom(String value) {
        setString("WK-IN-CFROM", value);
    }

    public String getWkInCto() {
        return getString("WK-IN-CTO");
    }

    public void setWkInCto(String value) {
        setString("WK-IN-CTO", value);
    }

    public String getWkInDfrom() {
        return getString("WK-IN-DFROM");
    }

    public void setWkInDfrom(String value) {
        setString("WK-IN-DFROM", value);
    }

    public String getWkInDto() {
        return getString("WK-IN-DTO");
    }

    public void setWkInDto(String value) {
        setString("WK-IN-DTO", value);
    }

    public String getWkKindTxt() {
        return getString("WK-KIND-TXT");
    }

    public void setWkKindTxt(String value) {
        setString("WK-KIND-TXT", value);
    }

    public int getWkLine() {
        return getInt("WK-LINE");
    }

    public void setWkLine(int value) {
        setInt("WK-LINE", value);
    }

    public int getWkLineCnt() {
        return getInt("WK-LINE-CNT");
    }

    public void setWkLineCnt(int value) {
        setInt("WK-LINE-CNT", value);
    }

    public int getWkMainEof() {
        return getInt("WK-MAIN-EOF");
    }

    public void setWkMainEof(int value) {
        setInt("WK-MAIN-EOF", value);
    }

    public BigDecimal getWkNet() {
        return getDecimal("WK-NET");
    }

    public void setWkNet(BigDecimal value) {
        setDecimal("WK-NET", value);
    }

    public BigDecimal getWkOpen() {
        return getDecimal("WK-OPEN");
    }

    public void setWkOpen(BigDecimal value) {
        setDecimal("WK-OPEN", value);
    }

    public int getWkPage() {
        return getInt("WK-PAGE");
    }

    public void setWkPage(int value) {
        setInt("WK-PAGE", value);
    }

    public String getWkProgid() {
        return getString("WK-PROGID");
    }

    public void setWkProgid(String value) {
        setString("WK-PROGID", value);
    }

    public BigDecimal getWkRun() {
        return getDecimal("WK-RUN");
    }

    public void setWkRun(BigDecimal value) {
        setDecimal("WK-RUN", value);
    }

    public int getWkStmtCnt() {
        return getInt("WK-STMT-CNT");
    }

    public void setWkStmtCnt(int value) {
        setInt("WK-STMT-CNT", value);
    }

    public int getWkSysdate() {
        return getInt("WK-SYSDATE");
    }

    public void setWkSysdate(int value) {
        setInt("WK-SYSDATE", value);
    }

    public String getWkTitle() {
        return getString("WK-TITLE");
    }

    public void setWkTitle(String value) {
        setString("WK-TITLE", value);
    }

    /* ── Byte-level copy wrappers (preserves COMP-3/BINARY raw bytes) ── */
    public void copyRepRecFromRcHead() {
        copyBytes("REP-REC", "RC-HEAD");
    }

    public void copyRepRecFromRdClose() {
        copyBytes("REP-REC", "RD-CLOSE");
    }

    public void copyRepRecFromRdLine() {
        copyBytes("REP-REC", "RD-LINE");
    }

    public void copyRepRecFromRdOpen() {
        copyBytes("REP-REC", "RD-OPEN");
    }

    public void copyRepRecFromRdStmt() {
        copyBytes("REP-REC", "RD-STMT");
    }

    public void copyRepRecFromRptH1() {
        copyBytes("REP-REC", "RPT-H1");
    }

    public void copyRepRecFromRptH2() {
        copyBytes("REP-REC", "RPT-H2");
    }
}
