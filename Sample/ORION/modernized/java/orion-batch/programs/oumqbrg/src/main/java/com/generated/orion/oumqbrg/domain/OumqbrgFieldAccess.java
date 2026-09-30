package com.generated.orion.oumqbrg.domain;

import com.generated.orion.oumqbrg.runtime.OumqbrgDatasets;
import com.generated.orion.runtime.record.RuntimeFieldAccess;

/**
 * Field accessor for OUMQBRG. Backend: RuntimeFieldAccess (raw RecordImage byte[]). Typed methods
 * auto-generated from referenced fields — bodies delegate to the string-key API.
 */
public class OumqbrgFieldAccess extends RuntimeFieldAccess {

    public OumqbrgFieldAccess(WorkingStorage ws, OumqbrgDatasets fileSet) {
        if (ws != null) {
            register(ws.buffer());
        }
        if (fileSet != null && fileSet.getTranFile() != null) {
            register(fileSet.getTranFile().buffer());
        }
    }

    /* ── Typed wrappers (delegate → string-key) ─────────────── */
    public int getCompletionCode() {
        return getInt("COMPLETION-CODE");
    }

    public void setCompletionCode(int value) {
        setInt("COMPLETION-CODE", value);
    }

    public int getMqBufferLen() {
        return getInt("MQ-BUFFER-LEN");
    }

    public void setMqBufferLen(int value) {
        setInt("MQ-BUFFER-LEN", value);
    }

    public int getMqCc() {
        return getInt("MQ-CC");
    }

    public void setMqCc(int value) {
        setInt("MQ-CC", value);
    }

    public int getMqClNone() {
        return getInt("MQ-CL-NONE");
    }

    public void setMqClNone(int value) {
        setInt("MQ-CL-NONE", value);
    }

    public int getMqCloseOptions() {
        return getInt("MQ-CLOSE-OPTIONS");
    }

    public void setMqCloseOptions(int value) {
        setInt("MQ-CLOSE-OPTIONS", value);
    }

    public int getMqHconn() {
        return getInt("MQ-HCONN");
    }

    public void setMqHconn(int value) {
        setInt("MQ-HCONN", value);
    }

    public int getMqHobjOut() {
        return getInt("MQ-HOBJ-OUT");
    }

    public void setMqHobjOut(int value) {
        setInt("MQ-HOBJ-OUT", value);
    }

    public String getMqMdFormat() {
        return getString("MQ-MD-FORMAT");
    }

    public void setMqMdFormat(String value) {
        setString("MQ-MD-FORMAT", value);
    }

    public int getMqMdMsgType() {
        return getInt("MQ-MD-MSG-TYPE");
    }

    public void setMqMdMsgType(int value) {
        setInt("MQ-MD-MSG-TYPE", value);
    }

    public int getMqMtDatagram() {
        return getInt("MQ-MT-DATAGRAM");
    }

    public void setMqMtDatagram(int value) {
        setInt("MQ-MT-DATAGRAM", value);
    }

    public String getMqOdObjectName() {
        return getString("MQ-OD-OBJECT-NAME");
    }

    public void setMqOdObjectName(String value) {
        setString("MQ-OD-OBJECT-NAME", value);
    }

    public int getMqOoFailIfQsg() {
        return getInt("MQ-OO-FAIL-IF-QSG");
    }

    public void setMqOoFailIfQsg(int value) {
        setInt("MQ-OO-FAIL-IF-QSG", value);
    }

    public int getMqOoOutput() {
        return getInt("MQ-OO-OUTPUT");
    }

    public void setMqOoOutput(int value) {
        setInt("MQ-OO-OUTPUT", value);
    }

    public int getMqOpenOptions() {
        return getInt("MQ-OPEN-OPTIONS");
    }

    public void setMqOpenOptions(int value) {
        setInt("MQ-OPEN-OPTIONS", value);
    }

    public int getMqPmNoSyncpoint() {
        return getInt("MQ-PM-NO-SYNCPOINT");
    }

    public void setMqPmNoSyncpoint(int value) {
        setInt("MQ-PM-NO-SYNCPOINT", value);
    }

    public int getMqPmoOptions() {
        return getInt("MQ-PMO-OPTIONS");
    }

    public void setMqPmoOptions(int value) {
        setInt("MQ-PMO-OPTIONS", value);
    }

    public int getMqRc() {
        return getInt("MQ-RC");
    }

    public void setMqRc(int value) {
        setInt("MQ-RC", value);
    }

    public String getOmRecType() {
        return getString("OM-REC-TYPE");
    }

    public void setOmRecType(String value) {
        setString("OM-REC-TYPE", value);
    }

    public String getOmSrcSystem() {
        return getString("OM-SRC-SYSTEM");
    }

    public void setOmSrcSystem(String value) {
        setString("OM-SRC-SYSTEM", value);
    }

    public String getOmTranData() {
        return getString("OM-TRAN-DATA");
    }

    public void setOmTranData(String value) {
        setString("OM-TRAN-DATA", value);
    }

    public String getTrId() {
        return getString("TR-ID");
    }

    public void setTrId(String value) {
        setString("TR-ID", value);
    }

    public String getWsEofFlg() {
        return getString("WS-EOF-FLG");
    }

    public void setWsEofFlg(String value) {
        setString("WS-EOF-FLG", value);
    }

    public String getWsMqConnFlg() {
        return getString("WS-MQ-CONN-FLG");
    }

    public void setWsMqConnFlg(String value) {
        setString("WS-MQ-CONN-FLG", value);
    }

    public String getWsMqDownFlg() {
        return getString("WS-MQ-DOWN-FLG");
    }

    public void setWsMqDownFlg(String value) {
        setString("WS-MQ-DOWN-FLG", value);
    }

    public String getWsMqOp() {
        return getString("WS-MQ-OP");
    }

    public void setWsMqOp(String value) {
        setString("WS-MQ-OP", value);
    }

    public String getWsMqOpenFlg() {
        return getString("WS-MQ-OPEN-FLG");
    }

    public void setWsMqOpenFlg(String value) {
        setString("WS-MQ-OPEN-FLG", value);
    }

    public String getWsOutQueue() {
        return getString("WS-OUT-QUEUE");
    }

    public void setWsOutQueue(String value) {
        setString("WS-OUT-QUEUE", value);
    }

    public int getWsPutCnt() {
        return getInt("WS-PUT-CNT");
    }

    public void setWsPutCnt(int value) {
        setInt("WS-PUT-CNT", value);
    }

    public int getWsPuterrCnt() {
        return getInt("WS-PUTERR-CNT");
    }

    public void setWsPuterrCnt(int value) {
        setInt("WS-PUTERR-CNT", value);
    }

    public String getWsQmgrName() {
        return getString("WS-QMGR-NAME");
    }

    public void setWsQmgrName(String value) {
        setString("WS-QMGR-NAME", value);
    }

    public int getWsReadCnt() {
        return getInt("WS-READ-CNT");
    }

    public void setWsReadCnt(int value) {
        setInt("WS-READ-CNT", value);
    }

    public int getWsSkipCnt() {
        return getInt("WS-SKIP-CNT");
    }

    public void setWsSkipCnt(int value) {
        setInt("WS-SKIP-CNT", value);
    }

    public String getWsTranFs() {
        return getString("WS-TRAN-FS");
    }

    public void setWsTranFs(String value) {
        setString("WS-TRAN-FS", value);
    }

    public String getWsTranOpenFlg() {
        return getString("WS-TRAN-OPEN-FLG");
    }

    public void setWsTranOpenFlg(String value) {
        setString("WS-TRAN-OPEN-FLG", value);
    }
}
