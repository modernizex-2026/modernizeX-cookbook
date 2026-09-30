package com.appruntime;

import java.lang.reflect.Method;

/**
 * Tham chiếu tới MỘT group trong field-store của chương trình — dùng cho tham số CALL truyền BY
 * REFERENCE mà callee phải ghi trả về (2026-08-21).
 *
 * <p><b>Vì sao cần</b>: từ Schema-v2, một tham số group của CALL được emit thành {@code
 * Utility.groupToString(G)} = <b>ảnh byte bất biến</b>. Với MQ thì đó là lỗi thật: {@code
 * CallHandler.mqCall} tự khai trong javadoc rằng "MQMD/MQOD group reference … is mutated in place
 * by the runtime", nhưng String không ghi lại được, nên chương trình COBOL đọc {@code MQMD-MSGID}
 * ngay sau {@code MQGET} luôn thấy giá trị cũ.
 *
 * <p><b>Vì sao không dựng lại ảnh rồi copy-back</b>: muốn ghi MSGID vào ảnh thì runtime phải biết
 * offset của nó, mà thứ tự field trong ảnh do <b>copybook của khách</b> quyết định — đo được:
 * {@code MQMD-CODEDCHARSETID} nằm ở offset 184 trong cây sinh ra, còn chuẩn IBM là 28. Runtime
 * không có layout đó. Nên phải giữ tham chiếu tới field-store, nơi CÓ layout.
 *
 * <p><b>Tên có phân định là bắt buộc, không phải cho đẹp</b>: một chương trình có thể COPY cùng một
 * copybook vào nhiều group ({@code COPAUA0C} có {@code 01 MQM-OD-REQUEST. COPY CMQODV.} và {@code
 * 01 MQM-OD-REPLY. COPY CMQODV.}), nên tên trần {@code MQOD-OBJECTNAME} là ambiguous và field-store
 * sẽ ném lỗi. Mọi truy cập ở đây đi qua dạng {@code "<CHILD> OF <GROUP>"} trước, chỉ lùi về tên
 * trần khi dạng phân định không tồn tại.
 *
 * <p>Truy cập field-store bằng reflection vì lớp đó ({@code <Prog>Fields} extends {@code
 * DynamicFieldAccessor}) được sinh ra theo từng project, app-runtime không tham chiếu tĩnh được.
 *
 * <p>{@link #toString()} trả về ảnh group, nên mọi chỗ trong runtime đang coi tham số này như chuỗi
 * (ví dụ {@code MqRunner.mqPut1} lấy body bằng {@code messageBody.toString()}) vẫn chạy như trước.
 */
public final class GroupRef {

    private final Object store;
    private final String group;

    public GroupRef(Object store, String group) {
        this.store = store;
        this.group = group == null ? "" : group;
    }

    /** Tên COBOL của group này (vd {@code MQM-OD-REQUEST}). */
    public String groupName() {
        return group;
    }

    /** Field con có tồn tại trong group này không (dạng phân định trước, rồi tên trần). */
    public boolean has(String child) {
        return resolveName(child) != null;
    }

    /** Đọc field con dạng chuỗi; {@code null} nếu không có field đó. */
    public String get(String child) {
        String name = resolveName(child);
        if (name == null) {
            return null;
        }
        return invokeString("getString", name);
    }

    /** Ghi field con dạng chuỗi. Không có field đó thì bỏ qua (suy thoái êm, không ném). */
    public void set(String child, String value) {
        String name = resolveName(child);
        if (name == null) {
            return;
        }
        try {
            Method m;
            try {
                m = store.getClass().getMethod("setString", String.class, String.class);
                m.invoke(store, name, value);
            } catch (NoSuchMethodException varargs) {
                // batch RuntimeFieldAccess only has the subscripted overload (String, String,
                // int...)
                m =
                        store.getClass()
                                .getMethod("setString", String.class, String.class, int[].class);
                m.invoke(store, name, value, new int[0]);
            }
        } catch (ReflectiveOperationException e) {
            // field-store không có setString(String,String) → không ghi được; im lặng ở đây là đúng
            // vì caller (MqRunner) đã cảnh báo một lần cho cả đường này.
        }
    }

    /** Đọc field con dạng số nguyên; {@code fallback} nếu không có field hoặc không đọc được. */
    public int getInt(String child, int fallback) {
        String name = resolveName(child);
        if (name == null) {
            return fallback;
        }
        try {
            Method m = store.getClass().getMethod("getInt", String.class, int[].class);
            Object v = m.invoke(store, name, new int[0]);
            return v instanceof Number n ? n.intValue() : fallback;
        } catch (ReflectiveOperationException e) {
            return fallback;
        }
    }

    /** Ảnh byte của cả group — giữ tương thích với mọi chỗ đang coi tham số này là chuỗi. */
    @Override
    public String toString() {
        String s = invokeString("groupToString", group);
        return s == null ? "" : s;
    }

    /**
     * Dạng phân định trước ({@code "CHILD OF GROUP"}), lùi về tên trần; {@code null} nếu không có.
     */
    private String resolveName(String child) {
        if (child == null || child.isEmpty() || store == null) {
            return null;
        }
        String qualified = group.isEmpty() ? child : child + " OF " + group;
        if (contains(qualified)) {
            return qualified;
        }
        if (contains(child)) {
            return child;
        }
        return null;
    }

    private boolean contains(String name) {
        try {
            Method m;
            try {
                m = store.getClass().getMethod("has", String.class); // online DynamicFieldAccessor
            } catch (NoSuchMethodException batch) {
                m =
                        store.getClass()
                                .getMethod("contains", String.class); // batch RuntimeFieldAccess
            }
            Object r = m.invoke(store, name);
            return Boolean.TRUE.equals(r);
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }

    private String invokeString(String method, String arg) {
        try {
            Method m;
            try {
                m = store.getClass().getMethod(method, String.class);
                return String.valueOf(m.invoke(store, arg));
            } catch (NoSuchMethodException varargs) {
                m = store.getClass().getMethod(method, String.class, int[].class);
                return String.valueOf(m.invoke(store, arg, new int[0]));
            }
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
