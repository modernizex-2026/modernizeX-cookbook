# -*- coding: utf-8 -*-
"""MENU00（SAKURA 販売管理システム）テストケース生成器。

根拠: design_asis/UI/MENU00_販売管理システム_画面設計書.md（§2 画面/§3 チェック/
§4 イベント/§5 DB CRUD）と reg AST。1 画面 = 1 大項目。中項目 は正準観点ラベル。
本文（name/前提/手順/期待）は業務日本語＋画面上の逐語メッセージ「…」のみ。コード識別子
（フィールド名・段落・行番号・ファイル名・PIC）は 備考 のみ。
本アプリにはメッセージコード体系（EI/EF/GF）が無く、各画面が英語のメッセージ文言を画面下部に
直接表示する（§3 の Message ID 列は全て "-"）。異常系の期待結果はその逐語メッセージを判定に用いる。
"""
import json
from pathlib import Path

cases = []
counters = {}
CLS = {"正常系": ("IT", "HAPPY"), "境界値": ("UT", "BOUNDARY"), "異常系": ("UT", "ABNORMAL")}


def add(area, major, mid, category, name, pre, steps, data, expected, remark, priority="中"):
    prefix, cls = CLS[category]
    k = (prefix, area, cls)
    counters[k] = counters.get(k, 0) + 1
    cid = f"{prefix}_{area}_{cls}_{counters[k]:03d}"
    cases.append({
        "major": major, "mid": mid, "id": cid, "name": name,
        "category": category, "priority": priority,
        "precondition": pre, "steps": steps, "data": data,
        "expected": expected, "automation_id": "", "auto": "×", "remark": remark,
    })


# ── 共通フィクスチャ（F-STD） ──────────────────────────────────────────
# NOTES.md §フィクスチャ に定義。各前提は "F-STD" ＋ 差分で記す。
FSTD = ("F-STD：管理者ユーザー（ログインID「ADMIN01」、5 権限フラグ=1）でサインオン済み。"
        "各マスタに基準データが存在（得意先100001「サクラ商事」、仕入先200001、"
        "商品10000001（在庫管理=1）、倉庫001・002、社員0001、部門0001、商品分類0001、"
        "銀行0001、地域01、税区分1）。論理削除済みデータ（得意先100002・商品10000002 等）を各1件用意。")


# ══════════════════════════════════════════════════════════════════════
# MENU00 — サインオン・メインメニュー（§2.1/§3.1/§4.1）
# ══════════════════════════════════════════════════════════════════════
M = "サインオン・メインメニュー（MENU00）"
A = "MENU"
add(A, M, "画面表示・レイアウト", "正常系",
    "起動直後にログイン画面が表示される",
    "F-STD（未サインオン）。",
    "1. システムを起動する",
    "-",
    "ログイン画面が表示され、Login ID／Password の入力欄と案内「Enter login - PF3 to quit」が表示される。",
    "SDD-SCR MENU00_login / DS-LOGIN / §2.1 / §3.1-1 / L172", "高")
add(A, M, "権限・セキュリティ", "正常系",
    "正しいログインID・パスワードでメインメニューへ進む",
    "F-STD（未サインオン）。管理者「ADMIN01」が登録済み。",
    "1. Login ID「ADMIN01」を入力\n2. Password を入力\n3. ENTER を押す",
    "Login ID=ADMIN01",
    "認証に成功し、メインメニュー（7 業務グループの一覧）が表示される。",
    "CHKLOG 権限照合 / USERF / §4.1-1", "高")
add(A, M, "メッセージ・異常系", "異常系",
    "ログインID・パスワードが不正だと再入力を促す",
    "F-STD（未サインオン）。",
    "1. Login ID「BADUSER」を入力\n2. 誤ったパスワードを入力\n3. ENTER を押す",
    "Login ID=BADUSER",
    "「Invalid login or password」を表示し、ログインを再入力させる。",
    "CHKLOG / §3.1-2 / L188", "高")
add(A, M, "メッセージ・異常系", "異常系",
    "認証失敗が上限回数に達するとシステムを終了する",
    "F-STD（未サインオン）。",
    "1. 誤ったログインID・パスワードで認証失敗を上限回数（3 回）繰り返す",
    "Login ID=BADUSER×3",
    "「Too many attempts - exiting」を表示し、サインオフ（システム終了）する。",
    "§3.1-3 / §4.1-1 / L191", "中")
add(A, M, "操作性・キー", "正常系",
    "ログイン画面で PF3 を押すと終了する",
    "F-STD（未サインオン）。ログイン画面を表示中。",
    "1. Login ID／Password を入力せず PF3 を押す",
    "-",
    "ログインを中止してシステムを終了する。",
    "§4.1-2 / L172 PF3 to quit", "低")
add(A, M, "機能・業務フロー", "正常系",
    "メインメニューで業務グループを選択しサブメニューへ進む",
    "F-STD（サインオン済み、メインメニュー表示中）。",
    "1. 業務グループ番号「1」（マスタ保守）を入力\n2. ENTER を押す",
    "選択=1",
    "マスタ保守のサブメニューが表示される。",
    "WK-CHOICE / §4.1-3 / §2.1 DS-MENU-MST", "高")
add(A, M, "権限・セキュリティ", "異常系",
    "締め権限の無いオペレーターが「7. Batch / Closing」を選ぶと拒否される",
    "F-STD。締め権限フラグ=0 の担当ユーザー「CLERK01」でサインオン済み。",
    "1. メインメニューで「7」を入力\n2. ENTER を押す",
    "選択=7、締め権限=0",
    "「Not authorised for batch/closing」を表示し、サブメニューへ進ませない。",
    "§3.1-5 / §4.1-3 / L366", "高")
add(A, M, "メッセージ・異常系", "異常系",
    "メインメニューで範囲外の番号を入力すると再入力を促す",
    "F-STD（メインメニュー表示中）。",
    "1. 業務グループ番号「9」を入力\n2. ENTER を押す",
    "選択=9",
    "「Invalid selection」を表示し、再入力させる。",
    "§3.1-4 / L222", "中")
add(A, M, "メッセージ・異常系", "異常系",
    "サブメニューで範囲外の番号を入力すると再入力を促す",
    "F-STD。マスタ保守サブメニューを表示中。",
    "1. サブメニュー番号「99」を入力\n2. ENTER を押す",
    "選択=99",
    "「Invalid selection」を表示し、再入力させる。",
    "§3.1-6 / §4.1-5 / L397", "中")
add(A, M, "メッセージ・異常系", "異常系",
    "未提供の業務プログラムを選ぶと起動しない",
    "F-STD。帳票サブメニューを表示中（未提供番号を含む）。",
    "1. 未提供の業務プログラムに対応する番号を入力\n2. ENTER を押す",
    "選択=未提供番号",
    "「Program not available yet」を表示し、プログラムを起動しない。",
    "§3.1-7 / §4.1-5 / L462", "中")
add(A, M, "状態遷移", "正常系",
    "サブメニューで業務プログラムを起動し終了後サブメニューへ戻る",
    "F-STD。マスタ保守サブメニュー表示中。",
    "1. 「得意先マスタ保守」に対応する番号を入力\n2. ENTER を押す\n3. 起動先で PF3=End を押す",
    "選択=得意先マスタ保守",
    "得意先マスタ保守画面へ制御が渡り、終了（PF3）でサブメニューへ戻る。",
    "§4.1-6 動的呼出し", "中")
add(A, M, "機能・業務フロー", "正常系",
    "メインメニューで「0」を選ぶとサインオフする",
    "F-STD（メインメニュー表示中）。",
    "1. 業務グループ番号「0」を入力\n2. ENTER を押す",
    "選択=0",
    "「Signed off - thank you」を表示し、サインオフする。",
    "§3.1-8 / §4.1-4 / L471", "中")
add(A, M, "境界値", "境界値",
    "メインメニュー選択の下限（有効な最小番号 1）を受け付ける",
    "F-STD（メインメニュー表示中）。",
    "1. 業務グループ番号「1」を入力\n2. ENTER を押す",
    "選択=1（最小有効値）",
    "マスタ保守サブメニューへ正常に進む（範囲内のため「Invalid selection」は出ない）。",
    "WK-CHOICE PIC 9(2) / §3.1-4 境界", "低")


# ══════════════════════════════════════════════════════════════════════
# マスタ保守 共通ジェネレータ（MS0010–MS0120）
# 共通キー: ENTER=Read / PF9=Delete / PF3=End（§4.2〜4.13）
# ══════════════════════════════════════════════════════════════════════
def master(area, scr, func, entity, code_label, code_field, code_pic,
           exist_code, deleted_code, new_code, new_details, change_field,
           validations, boundaries, msg_new, msg_del_reg, msg_exist,
           msg_added, msg_updated, msg_deleted, msg_zero,
           physical_delete=False, has_pf9=True, cancel_msg="Delete cancelled"):
    """1 マスタ保守画面ぶんの観点を展開する。validations/boundaries は §3 の行から。"""
    major = f"{func}（{scr}）"
    del_word = "物理削除" if physical_delete else "論理削除（削除フラグ=1）"
    # 画面表示
    add(area, major, "画面表示・レイアウト", "正常系",
        f"{func}画面が初期表示される",
        f"F-STD。メニューから {scr} を起動。",
        "1. 画面の初期表示を確認する",
        "-",
        f"{func}画面が表示され、{code_label}の入力欄とキー凡例（ENTER=Read／PF9=Delete／PF3=End）が表示される。",
        f"{scr} / §2 項目詳細 / {code_field}", "中")
    # 業務フロー: 新規
    add(area, major, "機能・業務フロー", "正常系",
        f"新規{entity}を登録できる",
        f"F-STD。{code_label}={new_code} は未登録。",
        f"1. {code_label}に {new_code} を入力し ENTER\n2. 明細（{new_details}）を入力\n3. Confirm に「Y」を入力して確定",
        f"{code_label}={new_code}",
        f"「{msg_new}」を経て明細入力に移り、確定で「{msg_added}」を表示し {entity}を{('登録する' )}（新規レコードを追加）。",
        f"§3 新規登録 / §4 確認部 / §5 CRUD 登録時", "高")
    # 業務フロー: 変更
    add(area, major, "機能・業務フロー", "正常系",
        f"既存{entity}を変更できる",
        f"F-STD。{code_label}={exist_code} は登録済み。",
        f"1. {code_label}に {exist_code} を入力し ENTER\n2. {change_field}を変更\n3. Confirm に「Y」を入力して確定",
        f"{code_label}={exist_code}",
        f"「{msg_exist}」を表示、変更確定で「{msg_updated}」を表示し {entity}を更新する。",
        f"§4 確認部 / §5 CRUD 修正時", "高")
    if has_pf9:
        # データ整合性: 削除
        add(area, major, "データ整合性・冪等性", "正常系",
            f"既存{entity}を削除できる",
            f"F-STD。{code_label}={exist_code} は登録済み（他取引で未使用）。",
            f"1. {code_label}={exist_code} を ENTER で読込\n2. PF9 を押す\n3. 確認に「Y」を入力し ENTER",
            f"{code_label}={exist_code}",
            f"「Press Y then ENTER to delete」の後「{msg_deleted}」を表示し、{entity}を{del_word}する。",
            f"§4 確認部 / §5 CRUD 削除時（PF9）", "高")
        # 削除取消
        add(area, major, "データ整合性・冪等性", "正常系",
            f"削除確認で「Y」以外を入力すると削除を取りやめる",
            f"F-STD。{code_label}={exist_code} を PF9 で削除対象にした状態。",
            f"1. 削除確認で「N」を入力し ENTER",
            "確認=N",
            f"「{cancel_msg}」を表示し、{entity}を削除しない。",
            f"§3 削除確認（Y 以外）", "中")
    # 状態遷移: 削除済み→再登録
    add(area, major, "状態遷移", "正常系",
        f"論理削除済みの{entity}コードは再登録に切り替わる",
        f"F-STD。{code_label}={deleted_code} は論理削除済み。",
        f"1. {code_label}に {deleted_code} を入力し ENTER",
        f"{code_label}={deleted_code}",
        f"「{msg_del_reg}」を表示し、新規登録（再登録）モードに切り替える。",
        f"§3 論理削除済み照合", "中")
    # 操作性: PF3
    add(area, major, "操作性・キー", "正常系",
        "PF3 でメニューへ戻る",
        f"F-STD。{func}画面を表示中。",
        "1. PF3 を押す",
        "-",
        "業務を終了し、メニューへ戻る。",
        f"§4 ファンクション部 PF3=End", "低")
    # 異常: invalid fkey
    add(area, major, "操作性・キー", "異常系",
        "定義外のファンクションキーを押すとエラー表示",
        f"F-STD。{func}画面を表示中。",
        "1. 定義外のファンクションキー（例: PF10）を押す",
        "PF10",
        "「Invalid function key」を表示し、操作を受け付けない。",
        f"§3 定義外キー", "低")
    # 異常: code zero
    add(area, major, "入力チェック", "異常系",
        f"{code_label}が 0 のとき受け付けない",
        f"F-STD。{func}画面を表示中。",
        f"1. {code_label}に 0 を入力し ENTER",
        f"{code_label}=0",
        f"「{msg_zero}」を表示し、読込を行わない。",
        f"§3 {code_field}=0 / §4 キー入力部", "中")
    # 各バリデーション（§3 E 行）
    for v in validations:
        add(area, major, v.get("mid", "入力チェック"), "異常系",
            v["name"], f"F-STD。{v.get('pre', func + '画面で明細入力中。')}",
            v["steps"], v["data"], f"「{v['msg']}」を表示し、{v.get('eff','当該項目を再入力させる')}。",
            v["remark"], v.get("pri", "中"))
    # 異常: 重複登録
    add(area, major, "メッセージ・異常系", "異常系",
        f"同一{code_label}が既に存在すると登録に失敗する",
        f"F-STD。{code_label}={exist_code} は登録済み。",
        f"1. 新規登録操作の確定時に、同一{code_label}が既に存在する状態を作る",
        f"{code_label}={exist_code}（重複）",
        "「Write failed - duplicate」を表示し、登録を中止する。",
        f"§3 登録確定（重複）/ データ整合性", "中")
    if has_pf9:
        # 異常: nothing to delete
        add(area, major, "メッセージ・異常系", "異常系",
            f"表示中の{entity}が無いのに PF9 を押すと削除できない",
            f"F-STD。{code_label} 未読込（明細未表示）の状態。",
            "1. レコードを読み込まずに PF9 を押す",
            "-",
            "「Nothing to delete」を表示し、削除を行わない。",
            f"§3 削除操作（PF9）対象なし", "低")
    # 異常: I/O 障害（更新/削除/登録の記帳失敗）代表 1 件
    io_msgs = ["Update failed"]
    if has_pf9:
        io_msgs.append("Delete failed")
    add(area, major, "回復・リラン", "異常系",
        "記帳（更新／削除）が I/O 障害で失敗したときエラー表示",
        f"F-STD。{code_label}={exist_code} を読込済み。ファイル I/O 障害を発生させる *(要実データ)*。",
        "1. 変更または削除を確定する（I/O 障害発生）",
        "*(要実データ：I/O 障害環境)*",
        f"該当メッセージ（{' / '.join('「%s」' % m for m in io_msgs)}）を表示し、記帳を確定しない。",
        f"§3 更新／削除確定 失敗（環境障害クラス、代表 1 件に集約）", "低")
    # 境界値
    for b in boundaries:
        add(area, major, "境界値", "境界値",
            b["name"], f"F-STD。{b.get('pre', func + '画面で入力中。')}",
            b["steps"], b["data"], b["expected"], b["remark"], b.get("pri", "低"))


# ── MS0010 得意先マスタ保守（§3.2/§4.2） ──
master("CUSTM", "MS0010", "得意先マスタ保守", "得意先", "得意先コード", "CU-CODE", "9(6)",
    "100001", "100002", "100003",
    "得意先名「新規サクラ」・地域01・担当社員0001・銀行0001・与信限度1000000",
    "得意先名", [
    {"name": "得意先名が未入力だとエラー", "steps": "1. 得意先名を空で ENTER", "data": "得意先名=空",
     "msg": "Name is required", "remark": "§3.2-6 / CU-NAME"},
    {"name": "地域コードが地域マスタに無いとエラー", "steps": "1. 地域コードに 99（未登録）を入力し ENTER",
     "data": "地域=99", "msg": "Region code not found", "remark": "§3.2-7 / CU-REGION → REGNF"},
    {"name": "担当社員コードが社員マスタに無いとエラー", "steps": "1. 担当社員コードに 9999（未登録）を入力し ENTER",
     "data": "担当社員=9999", "msg": "Sales rep not found", "remark": "§3.2-8 / CU-STAFF → STAFF"},
    {"name": "銀行コードが銀行マスタに無いとエラー", "steps": "1. 銀行コードに 9999（未登録）を入力し ENTER",
     "data": "銀行=9999", "msg": "Bank code not found", "remark": "§3.2-9 / CU-BANK → BANKF"},
    {"name": "与信限度額が負数だとエラー", "steps": "1. 与信限度額に -1 を入力し ENTER", "data": "与信限度=-1",
     "msg": "Credit limit cannot be negative", "remark": "§3.2-10 / CU-CREDIT-LIMIT", "mid": "境界値" if False else "入力チェック"},
    ], [
    {"name": "得意先コードの最小有効値 1 を受け付ける", "steps": "1. 得意先コードに 1 を入力し ENTER",
     "data": "得意先コード=1", "expected": "0 ではないため読込に進み、未登録なら「New customer - enter details」を表示する。",
     "remark": "CU-CODE PIC 9(6) 下限（0 は不可、1 は可）"},
    {"name": "得意先コードの最大桁 999999 を受け付ける", "steps": "1. 得意先コードに 999999 を入力し ENTER",
     "data": "得意先コード=999999", "expected": "6 桁の上限値として読込に進む（桁あふれ・切捨てが起きない）。",
     "remark": "CU-CODE PIC 9(6) 上限"},
    {"name": "与信限度額 0 を受け付ける（非負の下限）", "steps": "1. 与信限度額に 0 を入力し ENTER",
     "data": "与信限度=0", "expected": "0 は負数でないため受け付け、「Credit limit cannot be negative」は出ない。",
     "remark": "CU-CREDIT-LIMIT 非負境界"},
    ],
    "New customer - enter details", "Deleted customer - re-registering",
    "Existing customer - change or PF9 delete", "Customer added", "Customer updated",
    "Customer deleted", "Customer code must not be zero")


def write_out():
    out = Path(__file__).resolve().parent
    (out / "cases.json").write_text(
        json.dumps({"meta": META, "cases": cases}, ensure_ascii=False, indent=2),
        encoding="utf-8")
    print(f"cases: {len(cases)}")
    from collections import Counter
    print("per-分類:", dict(Counter(c["category"] for c in cases)))


META = {
    "project": "SAKURA 販売管理システム",
    "function": "販売管理システム (MENU00)",
    "test_level": "機能テスト（画面）",
    "environment": "*fill*",
    "doc_no": "QA-TC-MENU00-001",
    "version": "1.0",
    "date": "2026/08/30",
    "author": "modernizeX",
}

# ── MS0020 仕入先マスタ保守（§3.3/§4.3） ──
master("SUPPM", "MS0020", "仕入先マスタ保守", "仕入先", "仕入先コード", "SP-CODE", "9(6)",
    "200001", "200002", "200003",
    "仕入先名「新規商店」・締日15・支払月1・支払日25・支払方法1・税区分1・銀行0001",
    "仕入先名", [
    {"name": "仕入先名が未入力だとエラー", "steps": "1. 仕入先名を空で ENTER", "data": "仕入先名=空",
     "msg": "Name is required", "remark": "§3.3-6 / SP-NAME"},
    {"name": "締日が 1〜31／99 以外だとエラー", "steps": "1. 締日に 32 を入力し ENTER", "data": "締日=32",
     "msg": "Closing day must be 1-31 or 99", "remark": "§3.3-7 / SP-CLOSE-DAY 9(2)"},
    {"name": "支払日が 1〜31／99 以外だとエラー", "steps": "1. 支払日に 40 を入力し ENTER", "data": "支払日=40",
     "msg": "Pay day must be 1-31 or 99", "remark": "§3.3-8 / SP-PAY-DAY 9(2)"},
    {"name": "支払方法が 1〜3 以外だとエラー", "steps": "1. 支払方法に 4 を入力し ENTER", "data": "支払方法=4",
     "msg": "Pay method must be 1-3", "remark": "§3.3-9 / SP-PAY-METHOD 9(1)"},
    {"name": "税区分が 1〜3 以外だとエラー", "steps": "1. 税区分に 0 を入力し ENTER", "data": "税区分=0",
     "msg": "Tax type must be 1-3", "remark": "§3.3-10 / SP-TAX-TYPE 9(1)"},
    {"name": "銀行コードが銀行マスタに無いとエラー", "steps": "1. 銀行コードに 9999（未登録）を入力し ENTER",
     "data": "銀行=9999", "msg": "Bank code not found", "remark": "§3.3-11 / SP-BANK-CODE → BANKF"},
    ], [
    {"name": "締日の下限 1 を受け付ける", "steps": "1. 締日に 1 を入力し ENTER", "data": "締日=1",
     "expected": "範囲内のため受け付け、「Closing day must be 1-31 or 99」は出ない。", "remark": "SP-CLOSE-DAY 下限"},
    {"name": "締日の上限 31 を受け付ける", "steps": "1. 締日に 31 を入力し ENTER", "data": "締日=31",
     "expected": "範囲内のため受け付ける。", "remark": "SP-CLOSE-DAY 上限"},
    {"name": "締日 0 を拒否する（下限−1）", "steps": "1. 締日に 0 を入力し ENTER", "data": "締日=0",
     "expected": "「Closing day must be 1-31 or 99」を表示し再入力させる。", "remark": "SP-CLOSE-DAY 下限−1"},
    {"name": "締日の特例 99 を受け付ける", "steps": "1. 締日に 99 を入力し ENTER", "data": "締日=99",
     "expected": "末日指定（99）として受け付ける。", "remark": "SP-CLOSE-DAY 特例値"},
    {"name": "支払方法の上限 3 を受け付ける", "steps": "1. 支払方法に 3 を入力し ENTER", "data": "支払方法=3",
     "expected": "範囲内のため受け付ける。", "remark": "SP-PAY-METHOD 上限"},
    {"name": "仕入先コードの上限 999999 を受け付ける", "steps": "1. 仕入先コードに 999999 を入力し ENTER",
     "data": "仕入先コード=999999", "expected": "6 桁上限として読込に進む。", "remark": "SP-CODE PIC 9(6) 上限"},
    ],
    "New supplier - enter details", "Deleted supplier - re-registering",
    "Existing supplier - change or PF9 delete", "Supplier added", "Supplier updated",
    "Supplier deleted", "Supplier code must not be zero")

# ── MS0030 商品マスタ保守（§3.4/§4.4） ──
master("PRODM", "MS0030", "商品マスタ保守", "商品", "商品コード", "PR-CODE", "9(8)",
    "10000001", "10000002", "10000003",
    "商品名「新規商品」・分類0001・税区分1・在庫管理1・原価100・定価200・各ランク単価150・在庫水準0・発注点10・既定仕入先200001・既定倉庫001",
    "定価", [
    {"name": "商品名が未入力だとエラー", "steps": "1. 商品名を空で ENTER", "data": "商品名=空",
     "msg": "Name is required", "remark": "§3.4-6 / PR-NAME"},
    {"name": "商品分類が未入力だとエラー", "steps": "1. 分類コードを空で ENTER", "data": "分類=空",
     "msg": "Category is required", "remark": "§3.4-7 / PR-CATEGORY"},
    {"name": "分類コードが商品分類マスタに無いとエラー", "steps": "1. 分類コードに 9999（未登録）を入力し ENTER",
     "data": "分類=9999", "msg": "Category code not found", "remark": "§3.4-8 / PR-CATEGORY → CATGF"},
    {"name": "税区分が 1〜3 以外だとエラー", "steps": "1. 税区分に 4 を入力し ENTER", "data": "税区分=4",
     "msg": "Tax category must be 1-3", "remark": "§3.4-9 / PR-TAX-CATEGORY 9(1)"},
    {"name": "在庫管理区分が 0／1 以外だとエラー", "steps": "1. 在庫管理区分に 2 を入力し ENTER", "data": "在庫管理=2",
     "msg": "Stock mgmt must be 0 or 1", "remark": "§3.4-10 / PR-STOCK-MNG 9(1)"},
    {"name": "原価が負数だとエラー", "steps": "1. 原価に -1 を入力し ENTER", "data": "原価=-1",
     "msg": "Cost cannot be negative", "remark": "§3.4-11 / PR-STD-COST"},
    {"name": "定価が負数だとエラー", "steps": "1. 定価に -1 を入力し ENTER", "data": "定価=-1",
     "msg": "List price cannot be negative", "remark": "§3.4-12 / PR-LIST-PRICE"},
    {"name": "ランク単価が負数だとエラー", "steps": "1. ランク単価1 に -1 を入力し ENTER", "data": "ランク単価=-1",
     "msg": "Rank price cannot be negative", "remark": "§3.4-13 / PR-RANK-PRICE"},
    {"name": "在庫水準が負数だとエラー", "steps": "1. 安全在庫に -1 を入力し ENTER", "data": "安全在庫=-1",
     "msg": "Stock levels cannot be negative", "remark": "§3.4-14 / PR-SAFETY-STOCK"},
    {"name": "発注点数量が負数だとエラー", "steps": "1. 発注数量に -1 を入力し ENTER", "data": "発注数量=-1",
     "msg": "Reorder qty cannot be negative", "remark": "§3.4-15 / PR-REORDER-QTY"},
    {"name": "既定仕入先が仕入先マスタに無いとエラー", "steps": "1. 既定仕入先に 999999（未登録）を入力し ENTER",
     "data": "既定仕入先=999999", "msg": "Default supplier not found", "remark": "§3.4-16 / PR-DFLT-SUPP → SUPPF"},
    {"name": "既定倉庫が倉庫マスタに無いとエラー", "steps": "1. 既定倉庫に 999（未登録）を入力し ENTER",
     "data": "既定倉庫=999", "msg": "Default warehouse not found", "remark": "§3.4-17 / PR-DFLT-WHSE → WHSEF"},
    ], [
    {"name": "在庫管理区分 0 を受け付ける（下限）", "steps": "1. 在庫管理区分に 0 を入力し ENTER", "data": "在庫管理=0",
     "expected": "0（在庫管理しない）として受け付ける。", "remark": "PR-STOCK-MNG 下限"},
    {"name": "在庫管理区分 1 を受け付ける（上限）", "steps": "1. 在庫管理区分に 1 を入力し ENTER", "data": "在庫管理=1",
     "expected": "1（在庫管理する）として受け付ける。", "remark": "PR-STOCK-MNG 上限"},
    {"name": "各金額 0 を受け付ける（非負の下限）", "steps": "1. 原価・定価に 0 を入力し ENTER", "data": "原価=0／定価=0",
     "expected": "0 は負数でないため受け付け、負数エラーは出ない。", "remark": "PR-STD-COST/PR-LIST-PRICE 非負境界"},
    {"name": "商品コードの上限 99999999 を受け付ける", "steps": "1. 商品コードに 99999999 を入力し ENTER",
     "data": "商品コード=99999999", "expected": "8 桁上限として読込に進む。", "remark": "PR-CODE PIC 9(8) 上限"},
    ],
    "New product - enter details", "Deleted product - re-registering",
    "Existing product - change or PF9 delete", "Product added", "Product updated",
    "Product deleted", "Product code must not be zero")

# ── MS0040 倉庫マスタ保守（§3.5/§4.5） ──
master("WHSEM", "MS0040", "倉庫マスタ保守", "倉庫", "倉庫コード", "WH-CODE", "9(3)",
    "001", "008", "003",
    "倉庫名「新規倉庫」・倉庫区分1・管理者0001", "倉庫名", [
    {"name": "倉庫名が未入力だとエラー", "steps": "1. 倉庫名を空で ENTER", "data": "倉庫名=空",
     "msg": "Name is required", "remark": "§3.5-6 / WH-NAME"},
    {"name": "倉庫区分が 1〜3 以外だとエラー", "steps": "1. 倉庫区分に 4 を入力し ENTER", "data": "倉庫区分=4",
     "msg": "Type must be 1-3", "remark": "§3.5-7 / WH-TYPE 9(1)"},
    {"name": "管理者コードが社員マスタに無いとエラー", "steps": "1. 管理者に 9999（未登録）を入力し ENTER",
     "data": "管理者=9999", "msg": "Manager code not found", "remark": "§3.5-8 / WH-MANAGER → STAFF"},
    ], [
    {"name": "倉庫区分の下限 1 を受け付ける", "steps": "1. 倉庫区分に 1 を入力し ENTER", "data": "倉庫区分=1",
     "expected": "範囲内のため受け付ける。", "remark": "WH-TYPE 下限"},
    {"name": "倉庫区分 0 を拒否する（下限−1）", "steps": "1. 倉庫区分に 0 を入力し ENTER", "data": "倉庫区分=0",
     "expected": "「Type must be 1-3」を表示し再入力させる。", "remark": "WH-TYPE 下限−1"},
    {"name": "倉庫コードの上限 999 を受け付ける", "steps": "1. 倉庫コードに 999 を入力し ENTER", "data": "倉庫コード=999",
     "expected": "3 桁上限として読込に進む。", "remark": "WH-CODE PIC 9(3) 上限"},
    ],
    "New warehouse - enter details", "Deleted warehouse - re-registering",
    "Existing warehouse - change or PF9 delete", "Warehouse added", "Warehouse updated",
    "Warehouse deleted", "Warehouse code must not be zero")

# ── MS0050 社員マスタ保守（§3.6/§4.6） ──
master("STAFM", "MS0050", "社員マスタ保守", "社員", "社員コード", "SF-CODE", "9(4)",
    "0001", "0002", "0003",
    "社員名「新規太郎」・部門0001", "社員名", [
    {"name": "社員名が未入力だとエラー", "steps": "1. 社員名を空で ENTER", "data": "社員名=空",
     "msg": "Name is required", "remark": "§3.6-6 / SF-NAME"},
    {"name": "部門が未入力だとエラー", "steps": "1. 部門コードを空で ENTER", "data": "部門=空",
     "msg": "Department is required", "remark": "§3.6-7 / SF-DEPT"},
    {"name": "部門コードが部門マスタに無いとエラー", "steps": "1. 部門に 9999（未登録）を入力し ENTER",
     "data": "部門=9999", "msg": "Department code not found", "remark": "§3.6-8 / SF-DEPT → DEPTF"},
    {"name": "指定した部門が論理削除済みだとエラー", "steps": "1. 論理削除済みの部門0009 を入力し ENTER",
     "data": "部門=0009（削除済）", "msg": "Department is deleted", "remark": "§3.6-9 / DEPTF 削除フラグ"},
    ], [
    {"name": "社員コードの上限 9999 を受け付ける", "steps": "1. 社員コードに 9999 を入力し ENTER", "data": "社員コード=9999",
     "expected": "4 桁上限として読込に進む。", "remark": "SF-CODE PIC 9(4) 上限"},
    {"name": "社員コードの最小有効値 1 を受け付ける", "steps": "1. 社員コードに 1 を入力し ENTER", "data": "社員コード=1",
     "expected": "0 ではないため読込に進む。", "remark": "SF-CODE 下限（0 不可・1 可）"},
    ],
    "New staff - enter details", "Deleted staff - re-registering",
    "Existing staff - change or PF9 delete", "Staff added", "Staff updated",
    "Staff deleted", "Staff code must not be zero")

# ── MS0060 商品分類マスタ保守（§3.7/§4.7） ──
master("CATGM", "MS0060", "商品分類マスタ保守", "商品分類", "分類コード", "CT-CODE", "9(4)",
    "0001", "0002", "0003",
    "分類名「新規分類」・親分類0001", "分類名", [
    {"name": "分類名が未入力だとエラー", "steps": "1. 分類名を空で ENTER", "data": "分類名=空",
     "msg": "Name is required", "remark": "§3.7-6 / CT-NAME"},
    {"name": "親分類に自分自身を指定するとエラー", "steps": "1. 分類コード0005 に対し親分類0005 を入力し ENTER",
     "data": "分類=0005／親=0005", "msg": "Parent cannot be itself", "remark": "§3.7-7 / CT-PARENT 自己参照"},
    {"name": "親分類が商品分類マスタに無いとエラー", "steps": "1. 親分類に 9999（未登録）を入力し ENTER",
     "data": "親分類=9999", "msg": "Parent category not found", "remark": "§3.7-8 / CT-PARENT → CATGF"},
    {"name": "親分類が論理削除済みだとエラー", "steps": "1. 論理削除済みの親分類0009 を入力し ENTER",
     "data": "親分類=0009（削除済）", "msg": "Parent category is deleted", "remark": "§3.7-9 / CATGF 削除フラグ"},
    ], [
    {"name": "分類コードの上限 9999 を受け付ける", "steps": "1. 分類コードに 9999 を入力し ENTER", "data": "分類コード=9999",
     "expected": "4 桁上限として読込に進む。", "remark": "CT-CODE PIC 9(4) 上限"},
    ],
    "New category - enter details", "Deleted category - re-registering",
    "Existing category - change or PF9 delete", "Category added", "Category updated",
    "Category deleted", "Category code must not be zero", cancel_msg="Delete cancelled")

# ── MS0070 得意先別単価マスタ保守（§3.8/§4.8）— 複合キー ──
master("CPRCM", "MS0070", "得意先別単価マスタ保守", "得意先別単価", "得意先コード＋商品コード", "CP-CUST/CP-PROD", "9(6)+9(8)",
    "得意先100001＋商品10000001", "得意先100001＋商品10000002（削除済単価）", "得意先100001＋商品10000005",
    "単価180・適用開始日20260101・適用終了日20261231", "単価", [
    {"name": "得意先コードが得意先マスタに無いとエラー", "steps": "1. 得意先コードに 999999（未登録）を入力し ENTER",
     "data": "得意先=999999", "msg": "Customer code not found", "remark": "§3.8-4 / CP-CUST → CUSTF", "pre": "MS0070 キー入力中。"},
    {"name": "得意先が論理削除済みだとエラー", "steps": "1. 論理削除済み得意先100002 を入力し ENTER",
     "data": "得意先=100002（削除済）", "msg": "Customer is deleted", "remark": "§3.8-5 / CUSTF 削除フラグ", "pre": "MS0070 キー入力中。"},
    {"name": "商品コードが商品マスタに無いとエラー", "steps": "1. 商品コードに 99999999（未登録）を入力し ENTER",
     "data": "商品=99999999", "msg": "Product code not found", "remark": "§3.8-6 / CP-PROD → PRODF", "pre": "MS0070 キー入力中。"},
    {"name": "商品が論理削除済みだとエラー", "steps": "1. 論理削除済み商品10000002 を入力し ENTER",
     "data": "商品=10000002（削除済）", "msg": "Product is deleted", "remark": "§3.8-7 / PRODF 削除フラグ", "pre": "MS0070 キー入力中。"},
    {"name": "商品コードが 0 だとエラー", "steps": "1. 商品コードに 0 を入力し ENTER", "data": "商品=0",
     "msg": "Product code must not be zero", "remark": "§3.8-3 / CP-PROD=0", "pre": "MS0070 キー入力中。"},
    {"name": "単価が負数だとエラー", "steps": "1. 単価に -1 を入力し ENTER", "data": "単価=-1",
     "msg": "Price cannot be negative", "remark": "§3.8-11 / CP-PRICE"},
    {"name": "適用開始日が未入力だとエラー", "steps": "1. 適用開始日を空で ENTER", "data": "開始日=空",
     "msg": "Start date is required", "remark": "§3.8-12 / CP-START-DATE"},
    {"name": "適用開始日が暦日として不正だとエラー", "steps": "1. 適用開始日に 20260230 を入力し ENTER",
     "data": "開始日=20260230", "msg": "Start date is invalid", "remark": "§3.8-13 / DATEUT 検証"},
    {"name": "適用終了日が暦日として不正だとエラー", "steps": "1. 適用終了日に 20261340 を入力し ENTER",
     "data": "終了日=20261340", "msg": "End date is invalid", "remark": "§3.8-14 / DATEUT 検証"},
    {"name": "適用終了日が適用開始日より前だとエラー", "steps": "1. 開始20260601・終了20260101 を入力し ENTER",
     "data": "開始>終了", "msg": "End date is before start date", "remark": "§3.8-15 / 日付大小"},
    ], [
    {"name": "適用終了日＝適用開始日（同日）を受け付ける", "steps": "1. 開始・終了ともに 20260101 を入力し ENTER",
     "data": "開始=終了=20260101", "expected": "終了日は開始日以降（同日可）のため受け付け、「End date is before start date」は出ない。",
     "remark": "日付境界（開始=終了）"},
    {"name": "単価 0 を受け付ける（非負の下限）", "steps": "1. 単価に 0 を入力し ENTER", "data": "単価=0",
     "expected": "0 は負数でないため受け付ける。", "remark": "CP-PRICE 非負境界"},
    ],
    "New contract price - enter details", "Deleted price - re-registering",
    "Existing price - change or PF9 delete", "Contract price added", "Contract price updated",
    "Contract price deleted", "Customer code must not be zero")

# ── MS0080 部門マスタ保守（§3.9/§4.9） ──
master("DEPTM", "MS0080", "部門マスタ保守", "部門", "部門コード", "DP-CODE", "9(4)",
    "0001", "0002", "0003",
    "部門名「新規部門」・親部門0001", "部門名", [
    {"name": "部門名が未入力だとエラー", "steps": "1. 部門名を空で ENTER", "data": "部門名=空",
     "msg": "Name is required", "remark": "§3.9-6 / DP-NAME"},
    {"name": "親部門に自分自身を指定するとエラー", "steps": "1. 部門0005 に親部門0005 を入力し ENTER",
     "data": "部門=0005／親=0005", "msg": "Parent cannot be the department itself", "remark": "§3.9-7 / DP-PARENT 自己参照"},
    {"name": "親部門が部門マスタに無いとエラー", "steps": "1. 親部門に 9999（未登録）を入力し ENTER",
     "data": "親部門=9999", "msg": "Parent department not found", "remark": "§3.9-8 / DP-PARENT → DEPTF"},
    ], [
    {"name": "部門コードの上限 9999 を受け付ける", "steps": "1. 部門コードに 9999 を入力し ENTER", "data": "部門コード=9999",
     "expected": "4 桁上限として読込に進む。", "remark": "DP-CODE PIC 9(4) 上限"},
    ],
    "New department - enter details", "Deleted department - re-registering",
    "Existing department - change or PF9 delete", "Department added", "Department updated",
    "Department deleted", "Department code must not be zero")

# ── MS0090 消費税率マスタ保守（§3.10/§4.10）— 物理削除あり、複合キー（税区分＋開始日） ──
master("TAXM", "MS0090", "消費税率マスタ保守", "消費税率", "税区分＋適用開始日", "TX-CODE/TX-START-DATE", "9(1)+9(8)",
    "税区分1＋開始日20260101", "税区分1＋開始日20250101", "税区分1＋開始日20270401",
    "税率0.100・税率名「標準税率」", "税率名", [
    {"name": "税区分が 1〜9 の範囲外だとエラー", "steps": "1. 税区分に 0 を入力し ENTER", "data": "税区分=0",
     "msg": "Tax category must be 1 to 9", "remark": "§3.10-2 / TX-CODE 9(1)", "pre": "MS0090 キー入力中。"},
    {"name": "適用開始日が未入力だとエラー", "steps": "1. 適用開始日を空で ENTER", "data": "開始日=空",
     "msg": "Start date is required", "remark": "§3.10-3 / TX-START-DATE", "pre": "MS0090 キー入力中。"},
    {"name": "適用開始日が暦日として不正だとエラー", "steps": "1. 適用開始日に 20260230 を入力し ENTER",
     "data": "開始日=20260230", "msg": "Start date is invalid", "remark": "§3.10-4 / DATEUT 検証", "pre": "MS0090 キー入力中。"},
    {"name": "税率が負数だとエラー", "steps": "1. 税率に -0.1 を入力し ENTER", "data": "税率=-0.1",
     "msg": "Rate cannot be negative", "remark": "§3.10-7 / TX-RATE"},
    {"name": "税率が 1.000 未満の小数でないとエラー", "steps": "1. 税率に 1.5 を入力し ENTER", "data": "税率=1.5",
     "msg": "Rate must be a fraction below 1.000", "remark": "§3.10-8 / TX-RATE S9(2)V9(3)"},
    {"name": "税率名が未入力だとエラー", "steps": "1. 税率名を空で ENTER", "data": "税率名=空",
     "msg": "Rate name is required", "remark": "§3.10-9 / TX-NAME"},
    ], [
    {"name": "税区分の下限 1 を受け付ける", "steps": "1. 税区分に 1 を入力し ENTER", "data": "税区分=1",
     "expected": "範囲 1〜9 内のため受け付ける。", "remark": "TX-CODE 下限", "pre": "MS0090 キー入力中。"},
    {"name": "税区分の上限 9 を受け付ける", "steps": "1. 税区分に 9 を入力し ENTER", "data": "税区分=9",
     "expected": "範囲 1〜9 内のため受け付ける。", "remark": "TX-CODE 上限", "pre": "MS0090 キー入力中。"},
    {"name": "税率 0.999（1.000 未満の上限）を受け付ける", "steps": "1. 税率に 0.999 を入力し ENTER", "data": "税率=0.999",
     "expected": "1.000 未満のため受け付け、「Rate must be a fraction below 1.000」は出ない。", "remark": "TX-RATE 上限境界"},
    {"name": "税率 1.000 を拒否する（上限）", "steps": "1. 税率に 1.000 を入力し ENTER", "data": "税率=1.000",
     "expected": "「Rate must be a fraction below 1.000」を表示し再入力させる。", "remark": "TX-RATE 上限+1"},
    ],
    "New tax rate - enter details", "New tax rate - enter details",
    "Existing tax rate - change or PF9 delete", "Tax rate added", "Tax rate updated",
    "Tax rate deleted", "Tax category must be 1 to 9", physical_delete=True)

# ── MS0100 銀行マスタ保守（§3.11/§4.11） ──
master("BANKM", "MS0100", "銀行マスタ保守", "銀行", "銀行コード", "BK-CODE", "9(4)",
    "0001", "0002", "0003",
    "銀行名「新規銀行」・支店名「本店」", "銀行名", [
    {"name": "銀行名が未入力だとエラー", "steps": "1. 銀行名を空で ENTER", "data": "銀行名=空",
     "msg": "Bank name is required", "remark": "§3.11-6 / BK-NAME"},
    ], [
    {"name": "銀行コードの上限 9999 を受け付ける", "steps": "1. 銀行コードに 9999 を入力し ENTER", "data": "銀行コード=9999",
     "expected": "4 桁上限として読込に進む。", "remark": "BK-CODE PIC 9(4) 上限"},
    ],
    "New bank - enter details", "Deleted bank - re-registering",
    "Existing bank - change or PF9 delete", "Bank added", "Bank updated",
    "Bank deleted", "Bank code must not be zero")

# ── MS0110 地域マスタ保守（§3.12/§4.12） ──
master("REGNM", "MS0110", "地域マスタ保守", "地域", "地域コード", "RG-CODE", "9(3)",
    "01", "02", "03",
    "地域名「新規地域」", "地域名", [
    {"name": "地域名が未入力だとエラー", "steps": "1. 地域名を空で ENTER", "data": "地域名=空",
     "msg": "Region name is required", "remark": "§3.12-6 / RG-NAME"},
    ], [
    {"name": "地域コードの上限 999 を受け付ける", "steps": "1. 地域コードに 999 を入力し ENTER", "data": "地域コード=999",
     "expected": "3 桁上限として読込に進む。", "remark": "RG-CODE PIC 9(3) 上限"},
    ],
    "New region - enter details", "Deleted region - re-registering",
    "Existing region - change or PF9 delete", "Region added", "Region updated",
    "Region deleted", "Region code must not be zero")

# ── MS0120 ユーザーマスタ保守（§3.13/§4.13） ──
master("USERM", "MS0120", "ユーザーマスタ保守", "ユーザー", "ユーザーコード", "US-CODE", "9(6)",
    "100001", "100002", "100005",
    "ログインID「NEWUSER1」・氏名「新規花子」・ロール3・5 権限フラグ0・社員コード0001（担当者はコード＝社員一致）",
    "氏名", [
    {"name": "ログインIDが未入力だとエラー", "steps": "1. ログインIDを空で ENTER", "data": "ログインID=空",
     "msg": "Login ID is required", "remark": "§3.13-7 / US-LOGIN"},
    {"name": "氏名が未入力だとエラー", "steps": "1. 氏名を空で ENTER", "data": "氏名=空",
     "msg": "User name is required", "remark": "§3.13-8 / US-NAME"},
    {"name": "ロールが 1〜3 以外だとエラー", "steps": "1. ロールに 4 を入力し ENTER", "data": "ロール=4",
     "msg": "Role must be 1 admin 2 manager 3 clerk", "remark": "§3.13-9 / US-ROLE"},
    {"name": "マスタ権限フラグが 0／1 以外だとエラー", "steps": "1. マスタ権限に 2 を入力し ENTER", "data": "マスタ権限=2",
     "msg": "Auth Master must be 0 or 1", "remark": "§3.13-10 / 権限フラグ"},
    {"name": "受注権限フラグが 0／1 以外だとエラー", "steps": "1. 受注権限に 2 を入力し ENTER", "data": "受注権限=2",
     "msg": "Auth Order must be 0 or 1", "remark": "§3.13-11 / 権限フラグ"},
    {"name": "売上権限フラグが 0／1 以外だとエラー", "steps": "1. 売上権限に 2 を入力し ENTER", "data": "売上権限=2",
     "msg": "Auth Sales must be 0 or 1", "remark": "§3.13-12 / 権限フラグ"},
    {"name": "仕入権限フラグが 0／1 以外だとエラー", "steps": "1. 仕入権限に 2 を入力し ENTER", "data": "仕入権限=2",
     "msg": "Auth Purchase must be 0 or 1", "remark": "§3.13-13 / 権限フラグ"},
    {"name": "締め権限フラグが 0／1 以外だとエラー", "steps": "1. 締め権限に 2 を入力し ENTER", "data": "締め権限=2",
     "msg": "Auth Close must be 0 or 1", "remark": "§3.13-14 / 権限フラグ"},
    {"name": "管理者以外のユーザーコードが社員コードと一致しないとエラー",
     "steps": "1. ロール3（担当）でユーザーコード100001・社員コード0002（不一致）を入力し ENTER",
     "data": "ユーザーコード≠社員コード", "msg": "Non-admin user code must match a staff code", "remark": "§3.13-15"},
    {"name": "指定した社員コードが社員マスタに無いとエラー", "steps": "1. 社員コードに 9999（未登録）を入力し ENTER",
     "data": "社員=9999", "msg": "Staff code not found", "remark": "§3.13-16 / → STAFF"},
    ], [
    {"name": "ロールの下限 1 を受け付ける", "steps": "1. ロールに 1 を入力し ENTER", "data": "ロール=1",
     "expected": "範囲内（1 管理者）のため受け付ける。", "remark": "US-ROLE 下限"},
    {"name": "ロールの上限 3 を受け付ける", "steps": "1. ロールに 3 を入力し ENTER", "data": "ロール=3",
     "expected": "範囲内（3 担当）のため受け付ける。", "remark": "US-ROLE 上限"},
    {"name": "権限フラグ 1 を受け付ける（上限）", "steps": "1. 各権限フラグに 1 を入力し ENTER", "data": "権限=1",
     "expected": "0／1 の範囲内のため受け付ける。", "remark": "権限フラグ 上限"},
    ],
    "New user - enter details", "Deleted user - re-registering",
    "Existing user - change or PF9 delete", "User added", "User updated",
    "User deleted", "User code must not be zero")
# MS0120 の重複メッセージは実際には "Write failed - duplicate code or login"（§3.13-17）。
# master() 既定は "Write failed - duplicate"。差分を専用 TC で追補する。
add("USERM", "ユーザーマスタ保守（MS0120）", "メッセージ・異常系", "異常系",
    "ユーザーコードまたはログインIDが重複すると登録に失敗する",
    "F-STD。ログインID「ADMIN01」は既に使用中。",
    "1. 新規ユーザー登録で既存のログインID「ADMIN01」を入力して確定",
    "ログインID=ADMIN01（重複）",
    "「Write failed - duplicate code or login」を表示し、登録を中止する。",
    "§3.13-17 / US-CODE・US-LOGIN 重複", "中")


# ══════════════════════════════════════════════════════════════════════
# 受注・売上・出荷（OE / SL / SH）
# ══════════════════════════════════════════════════════════════════════

# ── OE0010 受注入力（§2.14/§3.14/§4.14/§5） ──
A = "ORDENT"; M = "受注入力（OE0010）"
add(A, M, "画面表示・レイアウト", "正常系", "受注入力画面が初期表示される",
    "F-STD。メニューから OE0010 を起動。", "1. 画面の初期表示を確認する", "-",
    "受注入力画面が表示され、案内「Enter order header - PF3 to quit」とヘッダ入力欄が表示される。",
    "OE0010 / §2.14 DS-HEAD / §3.14-1", "中")
add(A, M, "機能・業務フロー", "正常系", "受注ヘッダ・明細を入力し受注を登録できる",
    "F-STD。得意先100001 有効、商品10000001 に在庫あり。",
    "1. 受注日20260901・得意先100001・税区分1 を入力し ENTER\n2. 商品10000001・数量5 を入力し ENTER（明細追加）\n3. PF3 で明細を締める\n4. 合計を確認し Save order に「Y」を入力して確定",
    "得意先=100001／商品=10000001／数量=5",
    "「Header OK - enter detail lines」→「Line added」→「Review totals then confirm」を経て、受注番号を採番し「Order saved」を表示。受注ヘッダ・明細を記帳し、在庫の引当数を加算する。",
    "§4.14 / §5 ORDHF/ORDDF/STOKF・NUMGEN\"ORDER\"・TAXCAL", "高")
add(A, M, "操作性・キー", "正常系", "PF4 で入力中の明細行をクリアする",
    "F-STD。OE0010 明細入力中。", "1. 明細を入力途中で PF4 を押す", "-",
    "「Line cleared」を表示し、入力中の明細行をクリアする。", "§3.14-6 / §4.14-3 PF4=Clear line", "低")
add(A, M, "機能・業務フロー", "正常系", "明細を入力せず確定すると受注を破棄する",
    "F-STD。ヘッダのみ入力しPF3 で締めた状態。", "1. 明細を 1 行も追加せず確定へ進む", "明細=0 行",
    "「No lines entered - order discarded」を表示し、受注を登録しない。", "§3.14-16", "中")
add(A, M, "出力・帳票", "正常系", "数量が引当可能在庫を超えると警告しつつ明細追加できる",
    "F-STD。商品10000001 の引当可能在庫=3。", "1. 商品10000001・数量10 を入力し ENTER", "数量=10（在庫3）",
    "「Warning: quantity exceeds available stock」を表示するが、明細は追加できる（「Line added」）。", "§3.14-12 警告", "中")
for nm, tm, dt, msg, rk, pr in [
    ("得意先コードが未入力だとエラー", "1. 得意先コードを空で ENTER", "得意先=空", "Customer code required", "§3.14-2", "中"),
    ("得意先が得意先マスタに無いとエラー", "1. 得意先コードに 999999（未登録）を入力し ENTER", "得意先=999999", "Customer not found", "§3.14-3", "中"),
    ("得意先が論理削除済みだとエラー", "1. 論理削除済み得意先100002 を入力し ENTER", "得意先=100002（削除済）", "Customer is deleted", "§3.14-4", "中"),
    ("明細で定義外キーを押すとエラー", "1. 明細入力中に定義外キー（PF10）を押す", "PF10", "Invalid key", "§3.14-7", "低"),
    ("商品コードが未入力だとエラー", "1. 商品コードを空で ENTER", "商品=空", "Product code required", "§3.14-8", "中"),
    ("商品が商品マスタに無いとエラー", "1. 商品コードに 99999999（未登録）を入力し ENTER", "商品=99999999", "Product not found", "§3.14-9", "中"),
    ("商品が論理削除済みだとエラー", "1. 論理削除済み商品10000002 を入力し ENTER", "商品=10000002（削除済）", "Product is deleted", "§3.14-10", "中"),
    ("数量が正でないとエラー", "1. 数量に 0 を入力し ENTER", "数量=0", "Quantity must be positive", "§3.14-11", "中"),
    ("採番に失敗すると確定を中止する", "1. 確定時に受注番号の採番が失敗する *(要実データ)*", "*(要実データ)*", "Number assignment failed", "§3.14-18 / NUMGEN", "低"),
    ("受注ヘッダの記帳に失敗するとエラー", "1. 確定時にヘッダ記帳が失敗する *(要実データ)*", "*(要実データ)*", "Header write failed", "§3.14-19", "低"),
]:
    add(A, M, "入力チェック" if "required" in msg or "positive" in msg or "not found" in msg or "deleted" in msg or "Invalid" in msg else "メッセージ・異常系",
        "異常系", nm, f"F-STD。OE0010 入力中。", tm, dt, f"「{msg}」を表示する。", rk, pr)
add(A, M, "境界値", "境界値", "明細が上限 200 行に達すると追加できない",
    "F-STD。受注明細を 200 行入力済み。", "1. 201 行目を追加しようとする", "明細=200 行→201 行目",
    "「Maximum 200 lines reached」を表示し、明細を追加しない。", "§3.14-13 / 明細上限 200", "中")
add(A, M, "境界値", "境界値", "明細 200 行目（上限ちょうど）は追加できる",
    "F-STD。受注明細を 199 行入力済み。", "1. 200 行目を追加する", "明細=199→200 行目",
    "200 行目まで「Line added」で追加でき、上限エラーは出ない。", "§3.14-13 / 上限境界", "低")
add(A, M, "境界値", "境界値", "数量の最小有効値 1 を受け付ける",
    "F-STD。商品10000001 在庫十分。", "1. 数量に 1 を入力し ENTER", "数量=1",
    "正の数量のため「Line added」で追加でき、「Quantity must be positive」は出ない。", "WK-D-QTY S9(9) 下限（0 不可・1 可）", "低")

# ── OE0020 受注照会（§3.15/§4.15） ──
A = "ORDINQ"; M = "受注照会（OE0020）"
add(A, M, "画面表示・レイアウト", "正常系", "受注照会の検索画面が初期表示される",
    "F-STD。メニューから OE0020 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter order number or customer, then ENTER」と検索条件欄が表示される。", "§3.15-1", "中")
add(A, M, "機能・業務フロー", "正常系", "受注番号で受注を照会できる",
    "F-STD。受注番号0000000001 が登録済み。", "1. 受注番号0000000001 を入力し ENTER", "受注番号=0000000001",
    "受注ヘッダと明細が表示される。", "§4.15-6 / ORDHF・ORDDF", "高")
add(A, M, "機能・業務フロー", "正常系", "得意先コードで受注を検索できる",
    "F-STD。得意先100001 に受注あり。", "1. 得意先100001 を入力し ENTER", "得意先=100001",
    "当該得意先の先頭受注が明細まで表示される。", "§4.15-7 代替キー", "中")
add(A, M, "操作性・キー", "正常系", "ページ・受注移動キーで表示を切り替える",
    "F-STD。受注照会を表示中（複数ページ・複数受注）。",
    "1. ENTER で次ページ\n2. PF12 で前ページ\n3. PF6 で次受注\n4. PF5 で前受注\n5. PF3 で戻る", "-",
    "各キーで明細ページ送り／前後受注移動／メニュー復帰が行われる。案内「PF5/6 order  ENTER/PF12 page  PF3 back」を表示。",
    "§4.15-1〜5 / §3.15-8", "低")
for nm, tm, dt, msg, rk, cat in [
    ("定義外キーでエラー", "1. 定義外キー（PF10）を押す", "PF10", "Invalid function key", "§3.15-2", "異常系"),
    ("受注番号も得意先も未入力だとエラー", "1. いずれも入力せず ENTER", "空", "Enter an order number or a customer code", "§3.15-3", "異常系"),
    ("受注番号が見つからないとエラー", "1. 受注番号9999999999（未登録）を入力し ENTER", "受注番号=9999999999", "Order number not found", "§3.15-4", "異常系"),
    ("取消／削除済み受注はエラー", "1. 取消済み受注0000000009 を入力し ENTER", "受注番号=0000000009（取消）", "Order is cancelled / deleted", "§3.15-5", "異常系"),
    ("得意先が得意先マスタに無いとエラー", "1. 得意先999999（未登録）を入力し ENTER", "得意先=999999", "Customer not found", "§3.15-6", "異常系"),
    ("得意先に受注が無いとエラー", "1. 受注の無い得意先100009 を入力し ENTER", "得意先=100009", "No orders for this customer", "§3.15-7", "異常系"),
    ("最終ページで次ページ操作すると案内", "1. 最終ページで PF6/PF12 を押す", "-", "Already at last page - PF6 for next order", "§3.15-9", "正常系"),
    ("先頭ページで前ページ操作すると案内", "1. 先頭ページで PF12 を押す", "-", "Already at first page", "§3.15-10", "正常系"),
    ("これ以上の受注が無いとき案内", "1. 最終受注で PF6 を押す", "-", "No further orders", "§3.15-12", "正常系"),
    ("前の受注が無いとき案内", "1. 先頭受注で PF5 を押す", "-", "No previous order in this browse", "§3.15-13", "正常系"),
]:
    mid = "メッセージ・異常系" if cat == "異常系" else "操作性・キー"
    add(A, M, mid, cat, nm, "F-STD。OE0020 照会中。", tm, dt, f"「{msg}」を表示する。", rk, "中" if cat == "異常系" else "低")

# ── OE0030 受注引当（§3.16/§4.16） ──
A = "ORDALLOC"; M = "受注引当（OE0030）"
add(A, M, "画面表示・レイアウト", "正常系", "受注引当の受注番号入力画面が表示される",
    "F-STD。メニューから OE0030 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter the order number to allocate」が表示される。", "§3.16-1", "中")
add(A, M, "機能・業務フロー", "正常系", "受注を引き当てて在庫の引当数を確定できる",
    "F-STD。受注0000000002（状態0 入力済・明細あり）、商品に在庫十分。",
    "1. 受注番号0000000002 を入力し ENTER\n2. 明細を確認\n3. 引当確認で「Y」を入力", "受注番号=0000000002",
    "「Confirm allocation (Y) or PF3 to cancel」の後「Allocation complete」を表示し、受注ヘッダ・明細・在庫残高の引当数を更新する。",
    "§4.16-7 / §5 STOKF SK-ALLOC 加算", "高")
add(A, M, "データ整合性・冪等性", "正常系", "引当確認で PF3 を押すと引当を取りやめる",
    "F-STD。受注0000000002 を読込・明細表示済み。", "1. 引当確認で PF3 を押す", "-",
    "「Allocation cancelled」を表示し、在庫・受注を更新しない。", "§3.16-9", "中")
for nm, tm, dt, msg, rk, cat in [
    ("定義外キーでエラー", "1. 定義外キー（PF10）を押す", "PF10", "Invalid function key", "§3.16-2", "異常系"),
    ("受注番号が 0 だとエラー", "1. 受注番号に 0 を入力し ENTER", "受注番号=0", "Order number must not be zero", "§3.16-3", "異常系"),
    ("受注が見つからないとエラー", "1. 受注番号9999999999 を入力し ENTER", "受注番号=9999999999", "Order not found", "§3.16-4", "異常系"),
    ("受注が論理削除済みだとエラー", "1. 削除済み受注0000000009 を入力し ENTER", "受注番号=0000000009（削除済）", "Order is deleted", "§3.16-5", "異常系"),
    ("引当できない状態の受注はエラー", "1. 既に出荷済み受注0000000005 を入力し ENTER", "受注番号=0000000005（状態不可）", "Order cannot be allocated in its status", "§3.16-6", "異常系"),
    ("明細の無い受注はエラー", "1. 明細0 行の受注0000000006 を入力し ENTER", "受注番号=0000000006（明細0）", "Order has no detail lines", "§3.16-7", "異常系"),
    ("受注ヘッダ更新に失敗するとエラー", "1. 引当確定時にヘッダ更新が失敗 *(要実データ)*", "*(要実データ)*", "Order header update failed", "§3.16-10", "異常系"),
    ("在庫残高更新に失敗するとエラー", "1. 引当確定時に在庫更新が失敗 *(要実データ)*", "*(要実データ)*", "Stock update failed", "§3.16-11", "異常系"),
    ("受注明細更新に失敗するとエラー", "1. 引当確定時に明細更新が失敗 *(要実データ)*", "*(要実データ)*", "Line update failed", "§3.16-12", "異常系"),
]:
    add(A, M, "入力チェック" if ("not found" in msg or "zero" in msg or "deleted" in msg or "status" in msg or "no detail" in msg or "Invalid" in msg) else "回復・リラン",
        cat, nm, "F-STD。OE0030 操作中。", tm, dt, f"「{msg}」を表示する。", rk, "中")

# ── OE0040 受注保守（§3.17/§4.17） ──
A = "ORDMNT"; M = "受注保守（OE0040）"
add(A, M, "画面表示・レイアウト", "正常系", "受注保守の受注番号入力画面が表示される",
    "F-STD。メニューから OE0040 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter the order number to maintain」が表示される。", "§3.17-1", "中")
add(A, M, "機能・業務フロー", "正常系", "受注を読み込みコマンドで保守・保存できる",
    "F-STD。受注0000000002（未出荷）が登録済み。",
    "1. 受注番号0000000002 を入力し ENTER\n2. コマンド「C」で明細の数量を変更\n3. コマンド「S」で「Y」を入力し保存", "受注番号=0000000002／コマンド=C,S",
    "「Order loaded - enter a command」の後、変更を反映し「Save the changes ? (Y/N)」→「Order saved」を表示。受注ヘッダ・明細を再記帳する。",
    "§4.17-7〜13 / §5 ORDHF/ORDDF", "高")
add(A, M, "機能・業務フロー", "正常系", "コマンド A で明細を追加できる",
    "F-STD。受注0000000002 を保守中。", "1. コマンド「A」で商品10000001・数量2 を追加", "コマンド=A／商品=10000001",
    "商品・数量を検証し「Line added」を表示、明細を1行追加する。", "§4.17-8", "中")
add(A, M, "状態遷移", "正常系", "コマンド X で受注全体を取り消せる",
    "F-STD。受注0000000002 を保守中。", "1. コマンド「X」を実行\n2. 確認で「Y」を入力", "コマンド=X",
    "「Cancel the WHOLE order ? (Y/N)」に「Y」で受注全体を取り消し「Order cancelled」を表示する。", "§4.17-12 / §3.17-18", "中")
add(A, M, "出力・帳票", "異常系", "与信限度を超えると警告する",
    "F-STD。得意先100001 の与信限度を超える受注。", "1. コマンド「S」で保存確定（与信超過）", "与信超過",
    "「Warning: customer credit limit exceeded」を表示する（CREDIT で判定）。", "§3.17-23 / CREDIT", "中")
for nm, tm, dt, msg, rk in [
    ("受注番号が未入力だとエラー", "1. 受注番号を空で ENTER", "受注番号=空", "Order number required", "§3.17-2"),
    ("受注が見つからないとエラー", "1. 受注番号9999999999 を入力し ENTER", "受注番号=9999999999", "Order not found", "§3.17-3"),
    ("受注が論理削除済みだとエラー", "1. 削除済み受注0000000009 を入力し ENTER", "受注番号=0000000009（削除済）", "Order is deleted", "§3.17-4"),
    ("A/C/D/H/X/S 以外のコマンドはエラー", "1. コマンドに「Z」を入力し ENTER", "コマンド=Z", "Enter A C D H X or S", "§3.17-6"),
    ("A: 明細が 200 行に達すると追加不可", "1. 明細200 行の受注でコマンド A を実行", "明細=200 行", "Maximum 200 lines reached", "§3.17-7"),
    ("A: 商品コード未入力はエラー", "1. コマンド A で商品コードを空で ENTER", "商品=空", "Product code required", "§3.17-8"),
    ("A: 商品が商品マスタに無いとエラー", "1. コマンド A で商品99999999 を入力", "商品=99999999", "Product not found", "§3.17-9"),
    ("A: 商品が論理削除済みだとエラー", "1. コマンド A で削除済み商品10000002 を入力", "商品=10000002（削除済）", "Product is deleted", "§3.17-10"),
    ("A: 数量が正でないとエラー", "1. コマンド A で数量0 を入力", "数量=0", "Quantity must be positive", "§3.17-11"),
    ("C: 有効な行番号でないとエラー", "1. コマンド C で存在しない行番号999 を入力", "行番号=999", "Enter a valid line number", "§3.17-12"),
    ("C: 単価が負だとエラー", "1. コマンド C で単価-1 を入力", "単価=-1", "Price cannot be negative", "§3.17-13"),
    ("H: 得意先コード未入力はエラー", "1. コマンド H で得意先を空で ENTER", "得意先=空", "Customer code required", "§3.17-14"),
    ("H: 得意先が無いとエラー", "1. コマンド H で得意先999999 を入力", "得意先=999999", "Customer not found", "§3.17-15"),
    ("H: 得意先が論理削除済みだとエラー", "1. コマンド H で削除済み得意先100002 を入力", "得意先=100002（削除済）", "Customer is deleted", "§3.17-16"),
    ("H: 税区分が 1/2/3 以外だとエラー", "1. コマンド H で税区分4 を入力", "税区分=4", "Tax type must be 1, 2 or 3", "§3.17-17"),
    ("保存: 明細記帳に失敗するとエラー", "1. 保存確定時に明細記帳が失敗 *(要実データ)*", "*(要実データ)*", "Detail line write failed", "§3.17-24"),
    ("保存: ヘッダ再記帳に失敗するとエラー", "1. 保存確定時にヘッダ再記帳が失敗 *(要実データ)*", "*(要実データ)*", "Header rewrite failed", "§3.17-25"),
]:
    add(A, M, "入力チェック" if "write" not in msg and "rewrite" not in msg else "回復・リラン",
        "異常系", nm, "F-STD。OE0040 保守中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "機能・業務フロー", "正常系", "変更が無い状態で保存すると保存対象なしと表示",
    "F-STD。受注0000000002 を読み込み無変更。", "1. コマンド「S」を実行", "変更なし",
    "「Nothing changed - nothing to save」を表示する。", "§3.17-19", "低")
add(A, M, "境界値", "境界値", "受注保守で明細 200 行目まで追加できる",
    "F-STD。受注明細199 行を保守中。", "1. コマンド A で 200 行目を追加", "明細=199→200",
    "200 行目まで追加でき、「Maximum 200 lines reached」は出ない。", "§3.17-7 上限境界", "低")

# ── SL0010 売上・請求入力（§3.18/§4.18） ──
A = "SALEENT"; M = "売上・請求入力（SL0010）"
add(A, M, "画面表示・レイアウト", "正常系", "売上・請求入力の検索画面が表示される",
    "F-STD。メニューから SL0010 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter a shipment number or a customer code」が表示される。", "§3.18-1", "中")
add(A, M, "機能・業務フロー", "正常系", "出荷から売上（請求）を計上できる",
    "F-STD。出荷0000000001（未請求・明細あり）。",
    "1. 出荷番号0000000001 を入力し ENTER\n2. 税区分1 を入力\n3. 明細を確認し ENTER で計上確定", "出荷番号=0000000001／税区分=1",
    "明細を複写し「Confirm to post the invoice」→ 請求番号を採番して「Invoice header write」相当を実行し売上ヘッダ・明細・売掛元帳を記帳、得意先残高を更新する。",
    "§4.18-7,11 / §5 INVHF/INVDF/ARLF/CUSTF・NUMGEN\"INVOICE\"・TAXCAL", "高")
add(A, M, "機能・業務フロー", "正常系", "得意先直接指定で売上を作成できる",
    "F-STD。得意先100001 有効。", "1. 得意先100001 を入力し ENTER\n2. ヘッダ・明細を入力し計上", "得意先=100001",
    "得意先を照合し直接キー入力で売上を作成する経路へ進む。", "§4.18-8", "中")
add(A, M, "操作性・キー", "正常系", "PF4 で明細行をクリアする",
    "F-STD。SL0010 明細入力中。", "1. 明細入力中に PF4 を押す", "-", "「Line cleared」を表示する。", "§3.18-14", "低")
for nm, tm, dt, msg, rk, cat in [
    ("定義外キーでエラー", "1. 定義外キー（PF10）を押す", "PF10", "Invalid function key", "§3.18-2", "異常系"),
    ("出荷番号も得意先も未入力だとエラー", "1. いずれも入力せず ENTER", "空", "Enter shipment number or customer code", "§3.18-3", "異常系"),
    ("出荷が見つからないとエラー", "1. 出荷番号9999999999 を入力し ENTER", "出荷番号=9999999999", "Shipment not found", "§3.18-4", "異常系"),
    ("出荷が論理削除済みだとエラー", "1. 削除済み出荷0000000009 を入力し ENTER", "出荷=0000000009（削除済）", "Shipment is deleted", "§3.18-5", "異常系"),
    ("既に請求済みの出荷はエラー", "1. 請求済み出荷0000000003 を入力し ENTER", "出荷=0000000003（請求済）", "Shipment is already invoiced", "§3.18-6", "異常系"),
    ("取消済みの出荷はエラー", "1. 取消済み出荷0000000008 を入力し ENTER", "出荷=0000000008（取消）", "Shipment is cancelled", "§3.18-7", "異常系"),
    ("出荷の得意先が得意先マスタに無いとエラー", "1. 得意先不整合の出荷を読込", "-", "Customer of shipment not found", "§3.18-8", "異常系"),
    ("出荷に明細が無いとエラー", "1. 明細0 行の出荷を読込", "明細=0", "Shipment has no lines", "§3.18-9", "異常系"),
    ("得意先が得意先マスタに無いとエラー", "1. 得意先999999 を入力し ENTER", "得意先=999999", "Customer not found", "§3.18-10", "異常系"),
    ("得意先が論理削除済みだとエラー", "1. 削除済み得意先100002 を入力し ENTER", "得意先=100002（削除済）", "Customer is deleted", "§3.18-11", "異常系"),
    ("税区分が 1/2/3 以外だとエラー", "1. 税区分4 を入力し ENTER", "税区分=4", "Tax type must be 1, 2 or 3", "§3.18-13", "異常系"),
    ("明細で定義外キーでエラー", "1. 明細入力中に定義外キーを押す", "PF10", "Invalid key", "§3.18-15", "異常系"),
    ("商品コード未入力はエラー", "1. 商品コードを空で ENTER", "商品=空", "Product code required", "§3.18-16", "異常系"),
    ("商品が商品マスタに無いとエラー", "1. 商品99999999 を入力し ENTER", "商品=99999999", "Product not found", "§3.18-17", "異常系"),
    ("数量が正でないとエラー", "1. 数量0 を入力し ENTER", "数量=0", "Quantity must be positive", "§3.18-18", "異常系"),
    ("採番に失敗するとエラー", "1. 確定時に請求番号採番が失敗 *(要実データ)*", "*(要実データ)*", "Invoice number assignment failed", "§3.18-24", "異常系"),
    ("売上ヘッダ記帳に失敗するとエラー", "1. 確定時にヘッダ記帳が失敗 *(要実データ)*", "*(要実データ)*", "Invoice header write failed", "§3.18-25", "異常系"),
    ("売掛元帳記帳に失敗するとエラー", "1. 確定時に売掛元帳記帳が失敗 *(要実データ)*", "*(要実データ)*", "AR ledger write failed", "§3.18-26", "異常系"),
    ("得意先残高更新に失敗するとエラー", "1. 確定時に残高更新が失敗 *(要実データ)*", "*(要実データ)*", "Customer balance update failed", "§3.18-27", "異常系"),
]:
    mid = "回復・リラン" if ("write" in msg or "assignment" in msg or "update failed" in msg) else "入力チェック"
    add(A, M, mid, cat, nm, "F-STD。SL0010 入力中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "機能・業務フロー", "正常系", "明細を入力せず確定すると請求を破棄する",
    "F-STD。SL0010 でヘッダのみ。", "1. 明細0 行で確定へ進む", "明細=0", "「No lines entered - invoice discarded」を表示する。", "§3.18-21", "中")
add(A, M, "境界値", "境界値", "売上明細 200 行目まで追加できる",
    "F-STD。売上明細199 行入力済み。", "1. 200 行目を追加", "明細=199→200",
    "200 行目まで追加でき、「Maximum 200 lines reached」は出ない。", "§3.18-19 上限境界", "低")

# ── SL0020 売上・請求照会（§3.19/§4.19） ──
A = "SALEINQ"; M = "売上・請求照会（SL0020）"
add(A, M, "画面表示・レイアウト", "正常系", "請求照会の検索画面が表示される",
    "F-STD。メニューから SL0020 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter invoice number or customer, then ENTER」が表示される。", "§3.19-1", "中")
add(A, M, "機能・業務フロー", "正常系", "請求番号で請求を照会できる",
    "F-STD。請求番号0000000001 登録済み。", "1. 請求番号0000000001 を入力し ENTER", "請求番号=0000000001",
    "売上ヘッダを読み、照会画面に明細を表示する。", "§4.19-5 / INVHF", "高")
add(A, M, "操作性・キー", "正常系", "ページ・請求移動キーで表示を切り替える",
    "F-STD。請求照会を表示中。", "1. ENTER 次ページ／PF12 前ページ／PF5・PF6 前後請求／PF3 戻る", "-",
    "各キーで明細ページ送り・前後請求移動・メニュー復帰が行われる。", "§4.19-1〜4", "低")
for nm, tm, dt, msg, rk, cat in [
    ("定義外キーでエラー", "1. 定義外キーを押す", "PF10", "Invalid function key", "§3.19-2", "異常系"),
    ("請求番号も得意先も未入力だとエラー", "1. いずれも空で ENTER", "空", "Enter an invoice number or a customer", "§3.19-3", "異常系"),
    ("請求番号が見つからないとエラー", "1. 請求番号9999999999 を入力し ENTER", "請求番号=9999999999", "Invoice number not found", "§3.19-4", "異常系"),
    ("削除済み請求は警告", "1. 削除済み請求0000000009 を入力し ENTER", "請求=0000000009（削除済）", "Invoice is deleted", "§3.19-5", "異常系"),
    ("得意先が得意先マスタに無いとエラー", "1. 得意先999999 を入力し ENTER", "得意先=999999", "Customer not found", "§3.19-6", "異常系"),
    ("得意先に請求が無いとエラー", "1. 請求の無い得意先100009 を入力し ENTER", "得意先=100009", "No invoices for this customer", "§3.19-7", "異常系"),
    ("最終ページで次ページ操作は案内", "1. 最終ページで PF6 を押す", "-", "Already at last page - PF6 for next invoice", "§3.19-8", "正常系"),
    ("これ以上請求が無いとき案内", "1. 最終請求で PF6 を押す", "-", "No further invoices", "§3.19-11", "正常系"),
]:
    add(A, M, "メッセージ・異常系" if cat == "異常系" else "操作性・キー", cat, nm, "F-STD。SL0020 照会中。", tm, dt, f"「{msg}」を表示する。", rk, "中" if cat == "異常系" else "低")

# ── SL0030 売上返品入力（§3.20/§4.20） ──
A = "SALERET"; M = "売上返品入力（SL0030）"
add(A, M, "画面表示・レイアウト", "正常系", "売上返品の得意先入力画面が表示される",
    "F-STD。メニューから SL0030 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter the customer code for the return」が表示される。", "§3.20-1", "中")
add(A, M, "機能・業務フロー", "正常系", "売上返品を計上できる",
    "F-STD。得意先100001 有効、商品10000001。",
    "1. 得意先100001 を入力し ENTER\n2. 税区分1 を入力\n3. 商品10000001・返品数量2 を明細追加\n4. 確認で「Y」", "得意先=100001／商品=10000001／数量=2",
    "「Confirm to post the return」の後、返品をマイナス請求として記帳し在庫へ戻し、売掛元帳・得意先残高へ計上する。返品番号は自動採番。",
    "§4.20-9 / §5 INVHF(IH-KIND=2)/ARLF/STOKF/CUSTF・NUMGEN", "高")
for nm, tm, dt, msg, rk, cat in [
    ("定義外キーでエラー", "1. 定義外キーを押す", "PF10", "Invalid function key", "§3.20-2", "異常系"),
    ("得意先コード未入力はエラー", "1. 得意先を空で ENTER", "得意先=空", "Customer code required", "§3.20-3", "異常系"),
    ("得意先が得意先マスタに無いとエラー", "1. 得意先999999 を入力し ENTER", "得意先=999999", "Customer not found", "§3.20-4", "異常系"),
    ("得意先が論理削除済みだとエラー", "1. 削除済み得意先100002 を入力し ENTER", "得意先=100002（削除済）", "Customer is deleted", "§3.20-5", "異常系"),
    ("税区分が 1/2/3 以外だとエラー", "1. 税区分4 を入力し ENTER", "税区分=4", "Tax type must be 1, 2 or 3", "§3.20-7", "異常系"),
    ("商品コード未入力はエラー", "1. 商品コードを空で ENTER", "商品=空", "Product code required", "§3.20-8", "異常系"),
    ("商品が商品マスタに無いとエラー", "1. 商品99999999 を入力し ENTER", "商品=99999999", "Product not found", "§3.20-9", "異常系"),
    ("返品数量が正値でないとエラー", "1. 返品数量0 を入力し ENTER", "数量=0", "Return qty must be positive", "§3.20-10", "異常系"),
    ("在庫更新に失敗するとエラー", "1. 計上時に在庫更新が失敗 *(要実データ)*", "*(要実データ)*", "Stock update failed", "§3.20-14", "異常系"),
    ("返品ヘッダ記帳に失敗するとエラー", "1. 計上時にヘッダ記帳が失敗 *(要実データ)*", "*(要実データ)*", "Return header write failed", "§3.20-15", "異常系"),
    ("売掛元帳記帳に失敗するとエラー", "1. 計上時に売掛元帳記帳が失敗 *(要実データ)*", "*(要実データ)*", "AR ledger write failed", "§3.20-16", "異常系"),
    ("得意先残高更新に失敗するとエラー", "1. 計上時に残高更新が失敗 *(要実データ)*", "*(要実データ)*", "Customer balance update failed", "§3.20-17", "異常系"),
]:
    add(A, M, "回復・リラン" if ("write" in msg or "update failed" in msg) else "入力チェック", cat, nm, "F-STD。SL0030 入力中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "機能・業務フロー", "正常系", "明細を入力せず確定すると返品を破棄する",
    "F-STD。SL0030 でヘッダのみ。", "1. 明細0 行で確定", "明細=0", "「No lines entered - return discarded」を表示する。", "§3.20-13", "中")
add(A, M, "境界値", "境界値", "返品明細 200 行目まで追加できる",
    "F-STD。返品明細199 行入力済み。", "1. 200 行目を追加", "明細=199→200",
    "200 行目まで追加でき、上限エラーは出ない。", "§3.20-11 上限境界", "低")

# ── SL0040 売上クレジットノート（§3.21/§4.21） ──
A = "SALECN"; M = "売上クレジットノート（SL0040）"
add(A, M, "画面表示・レイアウト", "正常系", "クレジットノートの元請求入力画面が表示される",
    "F-STD。メニューから SL0040 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter the original invoice number to credit」が表示される。", "§3.21-1", "中")
add(A, M, "機能・業務フロー", "正常系", "元請求を指定しクレジットノートを計上できる",
    "F-STD。売上請求0000000001（明細あり）。",
    "1. 元請求番号0000000001 を入力し ENTER\n2. 明細と数量（明細1・数量1）を選択し ENTER\n3. 確認で「Y」", "元請求=0000000001／数量=1",
    "「Pick a line and its credit quantity」→「Confirm to post the credit note」を経て、クレジットノートを記帳し在庫へ戻し、売掛元帳・得意先残高へ計上する。",
    "§4.21-8〜10 / §5 INVHF/ARLF/STOKF/CUSTF・NUMGEN", "高")
add(A, M, "操作性・キー", "正常系", "明細ピックのキー操作ができる",
    "F-STD。SL0040 明細ピック中。", "1. ENTER で数量設定／PF4 で取消／PF6・PF12 でページ／PF3 で確認へ", "-",
    "「Credit quantity set」「Credit quantity cleared」等を表示し、選択・ページ操作が行える。", "§4.21-3〜7", "低")
for nm, tm, dt, msg, rk, cat in [
    ("定義外キーでエラー", "1. 定義外キーを押す", "PF10", "Invalid function key", "§3.21-2", "異常系"),
    ("元請求番号未入力はエラー", "1. 元請求番号を空で ENTER", "元請求=空", "Invoice number required", "§3.21-3", "異常系"),
    ("元請求が見つからないとエラー", "1. 元請求9999999999 を入力し ENTER", "元請求=9999999999", "Original invoice not found", "§3.21-4", "異常系"),
    ("元請求が削除済みだとエラー", "1. 削除済み請求0000000009 を入力し ENTER", "元請求=0000000009（削除済）", "Original invoice is deleted", "§3.21-5", "異常系"),
    ("元伝票が売上でないとエラー", "1. 売上でない伝票を指定", "-", "Source is not a sale - cannot credit", "§3.21-6", "異常系"),
    ("元請求が取消済みだとエラー", "1. 取消済み請求0000000008 を指定", "元請求=0000000008（取消）", "Original invoice is cancelled", "§3.21-7", "異常系"),
    ("元請求に明細が無いとエラー", "1. 明細0 行の請求を指定", "明細=0", "Source invoice has no lines", "§3.21-8", "異常系"),
    ("元請求の得意先が無いとエラー", "1. 得意先不整合の請求を指定", "-", "Customer of invoice not found", "§3.21-9", "異常系"),
    ("有効でない元明細番号はエラー", "1. 存在しない明細番号99 を入力", "明細番号=99", "Enter a valid source line number", "§3.21-11", "異常系"),
    ("クレジット数量が正値でないとエラー", "1. クレジット数量0 を入力", "数量=0", "Credit qty must be positive", "§3.21-12", "異常系"),
    ("クレジット数量が請求済数量を超えるとエラー", "1. 請求済3 に対し数量5 を入力", "数量=5（請求済3）", "Credit qty exceeds invoiced qty", "§3.21-13", "異常系"),
    ("在庫更新に失敗するとエラー", "1. 計上時に在庫更新が失敗 *(要実データ)*", "*(要実データ)*", "Stock update failed", "§3.21-16", "異常系"),
    ("クレジットヘッダ記帳に失敗するとエラー", "1. 計上時にヘッダ記帳が失敗 *(要実データ)*", "*(要実データ)*", "Credit header write failed", "§3.21-17", "異常系"),
    ("売掛元帳記帳に失敗するとエラー", "1. 計上時に売掛元帳記帳が失敗 *(要実データ)*", "*(要実データ)*", "AR ledger write failed", "§3.21-18", "異常系"),
]:
    add(A, M, "回復・リラン" if ("write" in msg or "update failed" in msg) else "入力チェック", cat, nm, "F-STD。SL0040 操作中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "境界値", "境界値", "クレジット数量＝請求済数量（上限）を受け付ける",
    "F-STD。元明細の請求済数量=3。", "1. クレジット数量に 3 を入力し ENTER", "数量=3（請求済3）",
    "請求済数量を上限に等しいため受け付け、「Credit qty exceeds invoiced qty」は出ない。", "§3.21-13 上限境界", "低")

# ── SH0010 出荷入力（§3.22/§4.22） ──
A = "SHIP"; M = "出荷入力（SH0010）"
add(A, M, "画面表示・レイアウト", "正常系", "出荷入力の受注番号入力画面が表示される",
    "F-STD。メニューから SH0010 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter the order number to ship」が表示される。", "§3.22-1", "中")
add(A, M, "機能・業務フロー", "正常系", "引当済み受注を出荷計上できる",
    "F-STD。受注0000000001（引当済・明細あり）。",
    "1. 受注番号0000000001 を入力し ENTER\n2. 明細行番号1・出荷数量3 を入力し ENTER\n3. PF3 で確認へ進み「Y」", "受注番号=0000000001／出荷数=3",
    "「Adjust ship qty by line, PF3 to post」→「Confirm shipment (Y) or PF3 to cancel」を経て、出荷ヘッダ・明細を記帳し在庫を引き落とし、受注明細・受注状態を更新する。出荷番号は採番。",
    "§4.22-8〜10 / §5 SHPHF/STOKF/ORDHF・NUMGEN\"SHIP\"", "高")
add(A, M, "計算・編集ロジック", "境界値", "出荷数量が引当数／残数を超えると上限へ丸める",
    "F-STD。明細の引当数=3。", "1. 出荷数量に 10 を入力し ENTER", "出荷数=10（引当3）",
    "「Quantity capped to allocated / outstanding」を表示し、出荷数を上限（3）へ丸める。", "§3.22-13 上限丸め", "中")
for nm, tm, dt, msg, rk in [
    ("定義外キーでエラー", "1. 定義外キーを押す", "PF10", "Invalid function key", "§3.22-2"),
    ("受注番号が 0 だとエラー", "1. 受注番号に 0 を入力し ENTER", "受注番号=0", "Order number must not be zero", "§3.22-3"),
    ("受注が見つからないとエラー", "1. 受注番号9999999999 を入力し ENTER", "受注番号=9999999999", "Order not found", "§3.22-4"),
    ("受注が論理削除済みだとエラー", "1. 削除済み受注0000000009 を入力し ENTER", "受注番号=0000000009（削除済）", "Order is deleted", "§3.22-5"),
    ("未引当の受注はエラー", "1. 未引当受注0000000002 を入力し ENTER", "受注番号=0000000002（未引当）", "Order is not allocated yet - run OE0030", "§3.22-6"),
    ("出荷できない状態の受注はエラー", "1. 出荷不可状態の受注0000000005 を入力し ENTER", "受注番号=0000000005（状態不可）", "Order cannot be shipped in its status", "§3.22-7"),
    ("明細の無い受注はエラー", "1. 明細0 行の受注を入力し ENTER", "明細=0", "Order has no detail lines", "§3.22-8"),
    ("存在しない明細行番号はエラー", "1. 明細行番号999 を入力し ENTER", "行番号=999", "Line number not found", "§3.22-11"),
    ("出荷数量が負値だとエラー", "1. 出荷数量に -1 を入力し ENTER", "出荷数=-1", "Quantity cannot be negative", "§3.22-12"),
    ("在庫更新に失敗するとエラー", "1. 計上時に在庫更新が失敗 *(要実データ)*", "*(要実データ)*", "Stock update failed", "§3.22-16"),
    ("出荷ヘッダ記帳に失敗するとエラー", "1. 計上時にヘッダ記帳が失敗 *(要実データ)*", "*(要実データ)*", "Shipment header write failed", "§3.22-17"),
    ("受注状態更新に失敗するとエラー", "1. 計上時に受注状態更新が失敗 *(要実データ)*", "*(要実データ)*", "Order status update failed", "§3.22-18"),
]:
    add(A, M, "回復・リラン" if ("write" in msg or "update failed" in msg) else "入力チェック", "異常系", nm, "F-STD。SH0010 操作中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "機能・業務フロー", "正常系", "出荷数が全て 0 だと出荷を破棄する",
    "F-STD。SH0010 出荷数入力中。", "1. 全明細の出荷数を 0 のまま確定", "出荷数=0",
    "「Nothing to ship - shipment discarded」を表示し、出荷しない。", "§3.22-14", "中")
add(A, M, "境界値", "境界値", "出荷数量 0（下限）は当該行を除外する",
    "F-STD。SH0010 出荷数入力中。", "1. 明細行の出荷数量に 0 を入力し ENTER", "出荷数=0",
    "負値エラーは出ず、当該行を出荷対象から外す（0 は許容の下限）。", "§3.22-12 下限境界", "低")

# ══════════════════════════════════════════════════════════════════════
# 購買（PU / RC）
# ══════════════════════════════════════════════════════════════════════

# ── PU0010 発注入力（§3.23/§4.23） ──
A = "POENT"; M = "発注入力（PU0010）"
add(A, M, "画面表示・レイアウト", "正常系", "発注入力画面が初期表示される",
    "F-STD。メニューから PU0010 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter PO header - PF3 to quit」とヘッダ入力欄が表示される。", "§3.23-1", "中")
add(A, M, "機能・業務フロー", "正常系", "発注ヘッダ・明細を入力し発注を登録できる",
    "F-STD。仕入先200001 有効、商品10000001、倉庫001。",
    "1. 仕入先200001 を入力し ENTER\n2. 商品10000001・倉庫001・数量5・単価100 を入力し ENTER\n3. PF3 で締め、確認で「Y」", "仕入先=200001／商品=10000001／数量=5／単価=100",
    "「Header OK - enter detail lines」→「Line added」→「Review totals then confirm」を経て発注番号を採番し「PO saved」を表示。発注ヘッダ・明細を登録する。",
    "§4.23 / §5 POHF/PODF・NUMGEN", "高")
add(A, M, "計算・編集ロジック", "正常系", "単価未入力時は商品の既定原価を採用する",
    "F-STD。商品10000001 に標準原価100 が設定済み。", "1. 単価を空のまま ENTER", "単価=空",
    "商品の最終原価／標準原価を既定単価（100）として採り、明細に追加する。", "§4.23-6", "中")
add(A, M, "操作性・キー", "正常系", "PF4 で明細行をクリアする",
    "F-STD。PU0010 明細入力中。", "1. 明細入力中に PF4 を押す", "-", "「Line cleared」を表示する。", "§3.23-? / §4.23-3", "低")
for nm, tm, dt, msg, rk in [
    ("仕入先コード未入力はエラー", "1. 仕入先を空で ENTER", "仕入先=空", "Supplier code required", "§3.23-2"),
    ("仕入先が仕入先マスタに無いとエラー", "1. 仕入先999999 を入力し ENTER", "仕入先=999999", "Supplier not found", "§3.23-3"),
    ("仕入先が論理削除済みだとエラー", "1. 削除済み仕入先200002 を入力し ENTER", "仕入先=200002（削除済）", "Supplier is deleted", "§3.23-4"),
    ("商品コード未入力はエラー", "1. 商品コードを空で ENTER", "商品=空", "Product code required", "§3.23-6"),
    ("商品が商品マスタに無いとエラー", "1. 商品99999999 を入力し ENTER", "商品=99999999", "Product not found", "§3.23-7"),
    ("商品が論理削除済みだとエラー", "1. 削除済み商品10000002 を入力し ENTER", "商品=10000002（削除済）", "Product is deleted", "§3.23-8"),
    ("倉庫未入力はエラー", "1. 倉庫を空で ENTER", "倉庫=空", "Warehouse required", "§3.23-9"),
    ("数量が正値でないとエラー", "1. 数量0 を入力し ENTER", "数量=0", "Quantity must be positive", "§3.23-10"),
    ("単価が採れないときはエラー", "1. 既定原価も無い商品で単価を空にし ENTER", "単価=空", "Unit cost required", "§3.23-11"),
    ("採番に失敗するとエラー", "1. 登録時に採番が失敗 *(要実データ)*", "*(要実データ)*", "Number assignment failed", "§3.23-15"),
    ("発注ヘッダ記帳に失敗するとエラー", "1. 登録時にヘッダ記帳が失敗 *(要実データ)*", "*(要実データ)*", "Header write failed", "§3.23-16"),
]:
    add(A, M, "回復・リラン" if ("write" in msg or "assignment" in msg) else "入力チェック", "異常系", nm, "F-STD。PU0010 入力中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "機能・業務フロー", "正常系", "明細を入力せず PF3 で締めると発注を破棄する",
    "F-STD。PU0010 ヘッダのみ。", "1. 明細0 行で PF3", "明細=0", "「No lines entered - PO discarded」を表示する。", "§3.23-13", "中")
add(A, M, "境界値", "境界値", "発注明細 200 行目まで追加できる",
    "F-STD。発注明細199 行入力済み。", "1. 200 行目を追加", "明細=199→200",
    "200 行目まで追加でき、「Maximum 200 lines reached」は出ない。", "§3.23-12 上限境界", "低")

# ── PU0020 発注照会（§3.24/§4.24） ──
A = "POINQ"; M = "発注照会（PU0020）"
add(A, M, "画面表示・レイアウト", "正常系", "発注照会の検索画面が表示される",
    "F-STD。メニューから PU0020 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter a PO number, or a supplier to browse」が表示される。", "§3.24-1", "中")
add(A, M, "機能・業務フロー", "正常系", "発注番号で発注を照会できる",
    "F-STD。発注0000000001 登録済み。", "1. 発注番号0000000001 を入力し ENTER", "発注番号=0000000001",
    "発注ヘッダを読み、照会画面へ進む。", "§4.24-4 / POHF", "高")
add(A, M, "機能・業務フロー", "正常系", "仕入先で発注を辿れる",
    "F-STD。仕入先200001 に複数発注。", "1. 発注番号を空にし仕入先200001 を入力し ENTER\n2. PF6 で次発注", "仕入先=200001",
    "最初の該当発注を表示し、PF6 で次発注へ送る。", "§4.24-5 / §3.24-6", "中")
for nm, tm, dt, msg, rk, cat in [
    ("定義外キーでエラー", "1. 定義外キーを押す", "PF10", "Invalid function key", "§3.24-2", "異常系"),
    ("発注番号も仕入先も未入力だとエラー", "1. いずれも空で ENTER", "空", "Enter a PO number or a supplier code", "§3.24-3", "異常系"),
    ("発注番号が見つからないとエラー", "1. 発注番号9999999999 を入力し ENTER", "発注番号=9999999999", "PO number not found", "§3.24-4", "異常系"),
    ("仕入先に発注が無いとエラー", "1. 発注の無い仕入先200009 を入力し ENTER", "仕入先=200009", "No PO found for supplier", "§3.24-5", "異常系"),
    ("これ以上発注が無いとき案内", "1. 最終発注で PF6 を押す", "-", "No more POs for this supplier", "§3.24-6", "正常系"),
    ("明細 10 行以上は先頭 9 行のみ表示", "1. 明細10 行の発注を照会", "明細=10 行", "More lines exist - only first 9 shown", "§3.24-7", "正常系"),
    ("仕入先の発注が 1 件のみのとき案内", "1. 発注1 件の仕入先を照会", "-", "Single PO - PF3 to go back", "§3.24-8", "正常系"),
]:
    add(A, M, "メッセージ・異常系" if cat == "異常系" else "表示条件・活性制御", cat, nm, "F-STD。PU0020 照会中。", tm, dt, f"「{msg}」を表示する。", rk, "中" if cat == "異常系" else "低")

# ── PU0030 仕入入力（§3.25/§4.25） ──
A = "PURENT"; M = "仕入入力（PU0030）"
add(A, M, "画面表示・レイアウト", "正常系", "仕入入力の入荷番号入力画面が表示される",
    "F-STD。メニューから PU0030 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter the receiving number to book as purchase」が表示される。", "§3.25-1", "中")
add(A, M, "機能・業務フロー", "正常系", "入荷から仕入を計上できる",
    "F-STD。入荷0000000001（未計上・明細あり）。",
    "1. 入荷番号0000000001 を入力し ENTER\n2. 仕入日20260901・税区分1 を入力\n3. 確認で「Y」", "入荷番号=0000000001／税区分=1",
    "「Enter purchase date / tax type - PF3 cancel」→「Confirm to book the purchase」を経て仕入番号を採番し「Purchase booked」を表示。仕入ヘッダ・明細・買掛元帳を記帳する。",
    "§4.25 / §5 PURHF/PURDF/APLF・NUMGEN・TAXCAL", "高")
for nm, tm, dt, msg, rk in [
    ("定義外キーでエラー", "1. 定義外キーを押す", "PF10", "Invalid function key", "§3.25-2"),
    ("入荷番号未入力はエラー", "1. 入荷番号を空で ENTER", "入荷番号=空", "Receiving number required", "§3.25-3"),
    ("入荷が見つからないとエラー", "1. 入荷番号9999999999 を入力し ENTER", "入荷番号=9999999999", "Receiving not found", "§3.25-4"),
    ("入荷が論理削除済みだとエラー", "1. 削除済み入荷0000000009 を入力し ENTER", "入荷=0000000009（削除済）", "Receiving is deleted", "§3.25-5"),
    ("入荷が取消済みだとエラー", "1. 取消済み入荷0000000008 を入力し ENTER", "入荷=0000000008（取消）", "Receiving is cancelled", "§3.25-6"),
    ("入荷が既に計上済みだとエラー", "1. 計上済み入荷0000000003 を入力し ENTER", "入荷=0000000003（計上済）", "Receiving already booked", "§3.25-7"),
    ("入荷に明細が無いとエラー", "1. 明細0 行の入荷を入力し ENTER", "明細=0", "No detail lines on this receiving", "§3.25-8"),
    ("仕入番号採番に失敗するとエラー", "1. 確定時に採番が失敗 *(要実データ)*", "*(要実データ)*", "Purchase number assignment failed", "§3.25-13"),
    ("仕入ヘッダ記帳に失敗するとエラー", "1. 確定時にヘッダ記帳が失敗 *(要実データ)*", "*(要実データ)*", "Purchase header write failed", "§3.25-14"),
    ("買掛元帳採番に失敗するとエラー", "1. 確定時に買掛元帳採番が失敗 *(要実データ)*", "*(要実データ)*", "AP ledger number assignment failed", "§3.25-16"),
    ("買掛元帳記帳に失敗するとエラー", "1. 確定時に買掛元帳記帳が失敗 *(要実データ)*", "*(要実データ)*", "AP ledger write failed", "§3.25-17"),
]:
    add(A, M, "回復・リラン" if ("write" in msg or "assignment" in msg) else "入力チェック", "異常系", nm, "F-STD。PU0030 操作中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "機能・業務フロー", "正常系", "確認で否認すると仕入を破棄する",
    "F-STD。PU0030 確認中。", "1. 確認で「N」を入力", "確認=N", "「Purchase discarded」を表示し、計上しない。", "§3.25-12", "中")

# ── PU0040 仕入返品入力（§3.26/§4.26） ──
A = "PURRET"; M = "仕入返品入力（PU0040）"
add(A, M, "画面表示・レイアウト", "正常系", "仕入返品の返品ヘッダ入力画面が表示される",
    "F-STD。メニューから PU0040 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter return header - PF3 to quit」が表示される。", "§3.26-2", "中")
add(A, M, "機能・業務フロー", "正常系", "仕入返品を計上できる",
    "F-STD。仕入先200001、商品10000001、倉庫001、在庫あり。",
    "1. 仕入先200001 を入力し ENTER\n2. 商品10000001・倉庫001・返品数2・単価100 を明細追加\n3. PF3 で締め確認で「Y」", "仕入先=200001／商品=10000001／数量=2",
    "「Header OK - enter return lines」→「Line added」→「Review totals then confirm return」を経て伝票番号を採番し「Purchase return posted」を表示。仕入返品ヘッダ・明細・買掛元帳を作成し在庫を減算、在庫移動履歴を残す。",
    "§4.26-7 / §5 PURHF(VH-KIND=2)/APLF/STOKF/SMOVF・NUMGEN", "高")
add(A, M, "出力・帳票", "異常系", "返品数が在庫残高を超えると警告する",
    "F-STD。商品10000001 の在庫=1。", "1. 返品数に 5 を入力し明細確定", "返品数=5（在庫1）",
    "「Warning: return qty exceeds on-hand stock」を表示する（明細追加は可）。", "§3.26-15 警告", "中")
add(A, M, "操作性・キー", "正常系", "PF4 で返品明細をクリアする",
    "F-STD。PU0040 明細入力中。", "1. 明細入力中に PF4 を押す", "-", "「Line cleared」を表示する。", "§3.26-7", "低")
for nm, tm, dt, msg, rk in [
    ("仕入先コード未入力はエラー", "1. 仕入先を空で ENTER", "仕入先=空", "Supplier code required", "§3.26-3"),
    ("仕入先が見つからないとエラー", "1. 仕入先999999 を入力し ENTER", "仕入先=999999", "Supplier not found", "§3.26-4"),
    ("仕入先が論理削除済みだとエラー", "1. 削除済み仕入先200002 を入力し ENTER", "仕入先=200002（削除済）", "Supplier is deleted", "§3.26-5"),
    ("明細で定義外キーはエラー", "1. 明細入力中に定義外キーを押す", "PF10", "Invalid key", "§3.26-8"),
    ("商品コード未入力はエラー", "1. 商品コードを空で ENTER", "商品=空", "Product code required", "§3.26-9"),
    ("商品が見つからないとエラー", "1. 商品99999999 を入力し ENTER", "商品=99999999", "Product not found", "§3.26-10"),
    ("商品が論理削除済みだとエラー", "1. 削除済み商品10000002 を入力し ENTER", "商品=10000002（削除済）", "Product is deleted", "§3.26-11"),
    ("倉庫未入力はエラー", "1. 倉庫を空で ENTER", "倉庫=空", "Warehouse required", "§3.26-12"),
    ("返品数が正数でないとエラー", "1. 返品数0 を入力し ENTER", "返品数=0", "Return qty must be positive", "§3.26-13"),
    ("単価未入力はエラー", "1. 単価を空で ENTER", "単価=空", "Unit cost required", "§3.26-14"),
    ("採番に失敗するとエラー", "1. 計上時に採番が失敗 *(要実データ)*", "*(要実データ)*", "Number assignment failed", "§3.26-20"),
    ("ヘッダ記帳に失敗するとエラー", "1. 計上時にヘッダ記帳が失敗 *(要実データ)*", "*(要実データ)*", "Header write failed", "§3.26-21"),
    ("買掛元帳採番に失敗するとエラー", "1. 計上時に買掛元帳採番が失敗 *(要実データ)*", "*(要実データ)*", "AP ledger number assignment failed", "§3.26-23"),
    ("買掛元帳記帳に失敗するとエラー", "1. 計上時に買掛元帳記帳が失敗 *(要実データ)*", "*(要実データ)*", "AP ledger write failed", "§3.26-24"),
]:
    add(A, M, "回復・リラン" if ("write" in msg or "assignment" in msg) else "入力チェック", "異常系", nm, "F-STD。PU0040 入力中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "機能・業務フロー", "正常系", "明細を入力せず確定すると返品を破棄する",
    "F-STD。PU0040 ヘッダのみ。", "1. 明細0 行で確定", "明細=0", "「No lines entered - return discarded」を表示する。", "§3.26-1", "中")
add(A, M, "境界値", "境界値", "仕入返品明細 200 行目まで追加できる",
    "F-STD。返品明細199 行入力済み。", "1. 200 行目を追加", "明細=199→200",
    "200 行目まで追加でき、上限エラーは出ない。", "§3.26-16 上限境界", "低")

# ── RC0010 入荷入力（§3.27/§4.27） ──
A = "RECV"; M = "入荷入力（RC0010）"
add(A, M, "画面表示・レイアウト", "正常系", "入荷入力の発注番号入力画面が表示される",
    "F-STD。メニューから RC0010 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter the purchase order number to receive」が表示される。", "§3.27-1", "中")
add(A, M, "機能・業務フロー", "正常系", "発注に対し入荷を計上できる",
    "F-STD。発注0000000001（未入荷明細あり）。",
    "1. 発注番号0000000001 を入力し ENTER\n2. 入荷日を確認\n3. 明細の入荷数5 を入力\n4. 確認で「Y」", "発注番号=0000000001／入荷数=5",
    "「Confirm receiving date - PF3 to cancel」→「Confirm to post receiving」を経て入荷番号を採番し「Receiving posted」を表示。入荷ヘッダ・明細を作成し在庫を加算、在庫移動履歴を残す。",
    "§4.27 / §5 RCVHF/RCVDF/STOKF/SMOVF・NUMGEN", "高")
add(A, M, "操作性・キー", "正常系", "PF4 で当該明細を入荷対象から外す",
    "F-STD。RC0010 明細処理中。", "1. 明細表示中に PF4 を押す", "-", "「Line skipped」を表示し、当該明細を入荷対象から外す。", "§3.27-12 / §4.27-2", "低")
for nm, tm, dt, msg, rk in [
    ("定義外キーでエラー", "1. 定義外キーを押す", "PF10", "Invalid function key", "§3.27-2"),
    ("発注番号未入力はエラー", "1. 発注番号を空で ENTER", "発注番号=空", "PO number required", "§3.27-3"),
    ("発注が見つからないとエラー", "1. 発注番号9999999999 を入力し ENTER", "発注番号=9999999999", "PO not found", "§3.27-4"),
    ("発注が論理削除済みだとエラー", "1. 削除済み発注0000000009 を入力し ENTER", "発注=0000000009（削除済）", "PO is deleted", "§3.27-5"),
    ("発注が取消済みだとエラー", "1. 取消済み発注0000000008 を入力し ENTER", "発注=0000000008（取消）", "PO is cancelled", "§3.27-6"),
    ("発注が全量入荷済みだとエラー", "1. 全量入荷済み発注0000000003 を入力し ENTER", "発注=0000000003（入荷済）", "PO already fully received", "§3.27-7"),
    ("未入荷明細が無いとエラー", "1. 未入荷明細0 の発注を入力し ENTER", "未入荷=0", "No outstanding lines on this PO", "§3.27-8"),
    ("明細で定義外キーはエラー", "1. 明細処理中に定義外キーを押す", "PF10", "Invalid key", "§3.27-13"),
    ("入荷数が負数だとエラー", "1. 入荷数に -1 を入力し ENTER", "入荷数=-1", "Received qty cannot be negative", "§3.27-15"),
    ("入荷数が未入荷数を超えるとエラー", "1. 未入荷5 に対し入荷数10 を入力し ENTER", "入荷数=10（未入荷5）", "Received qty exceeds outstanding", "§3.27-16"),
    ("入荷番号採番に失敗するとエラー", "1. 確定時に採番が失敗 *(要実データ)*", "*(要実データ)*", "Receiving number assignment failed", "§3.27-19"),
    ("入荷ヘッダ記帳に失敗するとエラー", "1. 確定時にヘッダ記帳が失敗 *(要実データ)*", "*(要実データ)*", "Receiving header write failed", "§3.27-20"),
]:
    add(A, M, "回復・リラン" if ("write" in msg or "assignment" in msg) else "入力チェック", "異常系", nm, "F-STD。RC0010 操作中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "機能・業務フロー", "正常系", "入荷数が 0 件だと破棄する",
    "F-STD。RC0010 明細処理中。", "1. 全明細の入荷数を 0 のまま確定", "入荷数=0",
    "「Nothing received - discarded」を表示し、入荷しない。", "§3.27-9", "中")
add(A, M, "境界値", "境界値", "入荷数＝未入荷数（上限）を受け付ける",
    "F-STD。明細の未入荷数=5。", "1. 入荷数に 5 を入力し ENTER", "入荷数=5（未入荷5）",
    "未入荷数に等しいため受け付け、「Received qty exceeds outstanding」は出ない。", "§3.27-16 上限境界", "低")

# ══════════════════════════════════════════════════════════════════════
# 在庫（IV）
# ══════════════════════════════════════════════════════════════════════

# ── IV0010 在庫残高照会（§3.28/§4.28） ──
A = "STKINQ"; M = "在庫残高照会（IV0010）"
add(A, M, "画面表示・レイアウト", "正常系", "在庫残高照会の検索画面が表示される",
    "F-STD。メニューから IV0010 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter search key then ENTER」が表示される。", "§3.28-2", "中")
add(A, M, "機能・業務フロー", "正常系", "検索キーで在庫を 1 件ずつ照会できる",
    "F-STD。商品10000001・倉庫001 に在庫あり。",
    "1. 開始商品10000001・倉庫001 を入力し ENTER\n2. PF6 で次件送り", "商品=10000001／倉庫=001",
    "当該キー以降の在庫残高を在庫・引当・有効数で 1 件表示し、PF6 で次件へ送る。「Record shown - ENTER/PF6 for next」を表示。",
    "§4.28-5 / STOKF", "高")
add(A, M, "メッセージ・異常系", "異常系", "定義外キーでエラー",
    "F-STD。IV0010 表示中。", "1. 定義外キーを押す", "PF10", "「Invalid function key」を表示する。", "§3.28-1", "低")
add(A, M, "機能・業務フロー", "正常系", "該当在庫が無いとき案内を表示する",
    "F-STD。在庫の無い商品を検索。", "1. 在庫の無い商品99999999 を入力し ENTER", "商品=99999999",
    "「No stock records found」を表示する。", "§3.28-4", "中")
add(A, M, "操作性・キー", "正常系", "一覧末尾で案内を表示する",
    "F-STD。IV0010 一覧末尾。", "1. 末尾まで PF6 で送る", "-",
    "「End of list - PF3 to re-enter key」を表示する。", "§3.28-5", "低")

# ── IV0020 在庫調整（§3.29/§4.29） ──
A = "STKADJ"; M = "在庫調整（IV0020）"
add(A, M, "画面表示・レイアウト", "正常系", "在庫調整の商品・倉庫入力画面が表示される",
    "F-STD。メニューから IV0020 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter product and warehouse」が表示される。", "§3.29-2", "中")
add(A, M, "機能・業務フロー", "正常系", "在庫を調整し在庫移動履歴を残せる",
    "F-STD。商品10000001・倉庫001 に在庫10。",
    "1. 商品10000001・倉庫001 を入力し ENTER\n2. 調整数+5・理由「棚卸差異」を入力\n3. 確認で「Y」", "調整数=+5／理由=棚卸差異",
    "「Stock found - enter adjustment」→「Confirm the new on-hand (Y/N)」→「Adjustment posted」を表示。在庫残高を更新し在庫移動履歴を追記する。",
    "§4.29 / §5 STOKF/SMOVF・NUMGEN", "高")
add(A, M, "機能・業務フロー", "正常系", "在庫が無い商品・倉庫は新規作成扱いになる",
    "F-STD。商品10000001・倉庫002 に在庫レコード無し。", "1. 商品10000001・倉庫002 を入力し ENTER", "倉庫=002（在庫なし）",
    "「No stock record - will be created」を表示し、新規作成扱いで調整入力へ進む。", "§3.29-9", "中")
for nm, tm, dt, msg, rk in [
    ("定義外キーでエラー", "1. 定義外キーを押す", "PF10", "Invalid function key", "§3.29-1"),
    ("商品コードが 0 だとエラー", "1. 商品コードに 0 を入力し ENTER", "商品=0", "Product code must not be zero", "§3.29-3"),
    ("商品が見つからないとエラー", "1. 商品99999999 を入力し ENTER", "商品=99999999", "Product not found", "§3.29-4"),
    ("商品が論理削除済みだとエラー", "1. 削除済み商品10000002 を入力し ENTER", "商品=10000002（削除済）", "Product is deleted", "§3.29-5"),
    ("倉庫が見つからないとエラー", "1. 倉庫999 を入力し ENTER", "倉庫=999", "Warehouse not found", "§3.29-6"),
    ("倉庫が論理削除済みだとエラー", "1. 削除済み倉庫008 を入力し ENTER", "倉庫=008（削除済）", "Warehouse is deleted", "§3.29-7"),
    ("調整数が 0 だとエラー", "1. 調整数に 0 を入力し ENTER", "調整数=0", "Adjustment quantity must not be zero", "§3.29-11"),
    ("調整後がマイナスになるとエラー", "1. 在庫10 に対し調整数-20 を入力し ENTER", "調整数=-20（在庫10）", "Result would be negative - not allowed", "§3.29-12"),
    ("理由が未入力だとエラー", "1. 調整理由を空で ENTER", "理由=空", "Reason is required", "§3.29-13"),
    ("在庫記帳に失敗するとエラー", "1. 確定時に在庫記帳が失敗 *(要実データ)*", "*(要実データ)*", "Stock write failed", "§3.29-16"),
    ("在庫更新に失敗するとエラー", "1. 確定時に在庫更新が失敗 *(要実データ)*", "*(要実データ)*", "Stock update failed", "§3.29-17"),
    ("移動採番に失敗するとエラー", "1. 確定時に移動採番が失敗 *(要実データ)*", "*(要実データ)*", "Movement number assignment failed", "§3.29-19"),
    ("移動記帳に失敗するとエラー", "1. 確定時に移動記帳が失敗 *(要実データ)*", "*(要実データ)*", "Movement write failed", "§3.29-20"),
]:
    add(A, M, "回復・リラン" if ("write" in msg or "update failed" in msg or "assignment" in msg) else "入力チェック", "異常系", nm, "F-STD。IV0020 操作中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "データ整合性・冪等性", "正常系", "確認で N を選ぶと調整を取りやめる",
    "F-STD。IV0020 確認中。", "1. 確認で「N」を入力", "確認=N", "「Adjustment cancelled」を表示し、在庫を更新しない。", "§3.29-15", "中")
add(A, M, "境界値", "境界値", "調整後在庫が 0（下限）は許容する",
    "F-STD。商品10000001・倉庫001 在庫10。", "1. 調整数-10 を入力し確認「Y」", "調整数=-10（在庫10）",
    "調整後在庫=0 はマイナスでないため許容し、「Adjustment posted」を表示する。", "§3.29-12 下限境界", "低")

# ── IV0030 棚卸（§3.30/§4.30） ──
A = "STKCNT"; M = "棚卸（IV0030）"
add(A, M, "画面表示・レイアウト", "正常系", "棚卸の倉庫入力画面が表示される",
    "F-STD。メニューから IV0030 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter warehouse to count」が表示される。", "§3.30-2", "中")
add(A, M, "機能・業務フロー", "正常系", "実地棚卸数を入力し計上できる",
    "F-STD。倉庫001 に在庫あり。",
    "1. 倉庫001 を入力し ENTER\n2. 商品ごとに実地棚卸数を入力（ENTER=保存）\n3. PF3 で計上へ進み確認「Y」", "倉庫=001",
    "帳簿在庫と差異を表示し「Review counts then confirm posting」→「Stocktaking posted」を表示。在庫残高を棚卸数へ更新し在庫移動履歴を残す。",
    "§4.30 / §5 STOKF/SMOVF", "高")
add(A, M, "操作性・キー", "正常系", "PF4 で当該商品を保存せず飛ばす",
    "F-STD。IV0030 棚卸中。", "1. 商品表示中に PF4 を押す", "-", "当該商品を保存せず次へ進める（案内「ENTER=save count PF4=skip PF3=finish」）。", "§3.30-8", "低")
for nm, tm, dt, msg, rk in [
    ("定義外キーでエラー", "1. 定義外キーを押す", "PF10", "Invalid function key", "§3.30-1"),
    ("倉庫が見つからないとエラー", "1. 倉庫999 を入力し ENTER", "倉庫=999", "Warehouse not found", "§3.30-3"),
    ("倉庫が論理削除済みだとエラー", "1. 削除済み倉庫008 を入力し ENTER", "倉庫=008（削除済）", "Warehouse is deleted", "§3.30-4"),
    ("倉庫に在庫が無いとエラー", "1. 在庫0 件の倉庫002 を入力し ENTER", "倉庫=002（在庫なし）", "No stock records in this warehouse", "§3.30-5"),
]:
    add(A, M, "入力チェック", "異常系", nm, "F-STD。IV0030 操作中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "境界値", "境界値", "棚卸対象が上限 500 件に達すると停止する",
    "F-STD。倉庫に在庫500 件。", "1. 501 件目を対象にしようとする", "対象=500→501",
    "「Maximum 500 items reached - stop」を表示し、対象を打ち切る。", "§3.30-7 / 上限500", "中")

# ── IV0040 在庫移動履歴照会（§3.31/§4.31） ──
A = "STKMOV"; M = "在庫移動履歴照会（IV0040）"
add(A, M, "画面表示・レイアウト", "正常系", "在庫移動履歴照会の商品入力画面が表示される",
    "F-STD。メニューから IV0040 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter product code then ENTER」が表示される。", "§3.31-2", "中")
add(A, M, "機能・業務フロー", "正常系", "商品の在庫移動履歴を照会できる",
    "F-STD。商品10000001 に移動履歴あり。", "1. 商品10000001 を入力し ENTER\n2. PF6 で次ページ", "商品=10000001",
    "在庫移動履歴を日付順に一覧表示し、PF6 で次ページへ送る。", "§4.31 / SMOVF", "高")
add(A, M, "メッセージ・異常系", "異常系", "定義外キーでエラー",
    "F-STD。IV0040 表示中。", "1. 定義外キーを押す", "PF10", "「Invalid function key」を表示する。", "§3.31-1", "低")
add(A, M, "入力チェック", "異常系", "商品コードが 0 だとエラー",
    "F-STD。IV0040 表示中。", "1. 商品コードに 0 を入力し ENTER", "商品=0", "「Product code must not be zero」を表示する。", "§3.31-3", "中")
add(A, M, "機能・業務フロー", "正常系", "移動履歴が無いとき案内を表示する",
    "F-STD。移動履歴の無い商品。", "1. 移動履歴の無い商品10000009 を入力し ENTER", "商品=10000009",
    "「No movements for that product」を表示する。", "§3.31-4", "中")

# ── IV0050 倉庫間在庫移動（§3.32/§4.32） ──
A = "STKXFER"; M = "倉庫間在庫移動（IV0050）"
add(A, M, "画面表示・レイアウト", "正常系", "倉庫間在庫移動の入力画面が表示される",
    "F-STD。メニューから IV0050 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter product, from/to warehouse and quantity」が表示される。", "§3.32-2", "中")
add(A, M, "機能・業務フロー", "正常系", "倉庫間で在庫を移動できる",
    "F-STD。商品10000001（在庫管理1）、倉庫001 に在庫10、倉庫002。",
    "1. 商品10000001・出庫倉庫001・入庫倉庫002・数量5 を入力し ENTER\n2. 確認で「Y」", "商品=10000001／001→002／数量=5",
    "検証通過後「Confirm the transfer (Y/N)」→出庫倉庫の在庫を減算・入庫倉庫の在庫を加算し在庫移動履歴を記録する。移動番号を採番。",
    "§4.32 / §5 STOKF/SMOVF・NUMGEN", "高")
for nm, tm, dt, msg, rk in [
    ("定義外キーでエラー", "1. 定義外キーを押す", "PF10", "Invalid function key", "§3.32-1"),
    ("商品コードが 0 だとエラー", "1. 商品コードに 0 を入力し ENTER", "商品=0", "Product code must not be zero", "§3.32-3"),
    ("商品が商品マスタに無いとエラー", "1. 商品99999999 を入力し ENTER", "商品=99999999", "Product not found", "§3.32-4"),
    ("商品が論理削除済みだとエラー", "1. 削除済み商品10000002 を入力し ENTER", "商品=10000002（削除済）", "Product is deleted", "§3.32-5"),
    ("在庫管理対象でない商品はエラー", "1. 在庫管理0 の商品を入力し ENTER", "在庫管理=0", "Product is not stock-managed", "§3.32-6"),
    ("出庫・入庫倉庫のいずれか未入力はエラー", "1. 入庫倉庫を空で ENTER", "入庫倉庫=空", "Both warehouses are required", "§3.32-7"),
    ("出庫倉庫と入庫倉庫が同一だとエラー", "1. 出庫001・入庫001 を入力し ENTER", "001→001", "From and to warehouse must differ", "§3.32-8"),
    ("出庫倉庫が見つからないとエラー", "1. 出庫倉庫999 を入力し ENTER", "出庫=999", "From warehouse not found", "§3.32-9"),
    ("出庫倉庫が論理削除済みだとエラー", "1. 削除済み出庫倉庫008 を入力し ENTER", "出庫=008（削除済）", "From warehouse is deleted", "§3.32-10"),
    ("入庫倉庫が見つからないとエラー", "1. 入庫倉庫999 を入力し ENTER", "入庫=999", "To warehouse not found", "§3.32-11"),
    ("入庫倉庫が論理削除済みだとエラー", "1. 削除済み入庫倉庫008 を入力し ENTER", "入庫=008（削除済）", "To warehouse is deleted", "§3.32-12"),
    ("移動数量が正値でないとエラー", "1. 数量0 を入力し ENTER", "数量=0", "Transfer quantity must be positive", "§3.32-13"),
    ("出庫倉庫に在庫が無いとエラー", "1. 在庫0 の出庫倉庫を指定し ENTER", "出庫在庫=0", "No stock at source warehouse", "§3.32-14"),
    ("移動数量が引当可能在庫を超えるとエラー", "1. 引当可能3 に対し数量10 を入力し ENTER", "数量=10（引当可能3）", "Quantity exceeds available at source", "§3.32-15"),
    ("出庫倉庫在庫更新に失敗するとエラー", "1. 記帳時に出庫在庫更新が失敗 *(要実データ)*", "*(要実データ)*", "Source stock update failed", "§3.32-18"),
    ("移動記帳に失敗するとエラー", "1. 記帳時に移動記帳が失敗 *(要実データ)*", "*(要実データ)*", "Movement write failed", "§3.32-19"),
]:
    add(A, M, "回復・リラン" if ("update failed" in msg or "write" in msg) else "入力チェック", "異常系", nm, "F-STD。IV0050 入力中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "データ整合性・冪等性", "正常系", "確認で N を選ぶと移動を取りやめる",
    "F-STD。IV0050 確認中。", "1. 確認で「N」を入力", "確認=N", "「Transfer cancelled」を表示し、在庫を移動しない。", "§3.32-17", "中")

# ══════════════════════════════════════════════════════════════════════
# 債権債務（AR / AP）
# ══════════════════════════════════════════════════════════════════════

# ── AR0010 入金入力（§3.33/§4.33） ──
A = "ARENT"; M = "入金入力（AR0010）"
add(A, M, "画面表示・レイアウト", "正常系", "入金入力画面が初期表示される",
    "F-STD。メニューから AR0010 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter receipt details - PF3 to end」が表示される。", "§3.33-1", "中")
add(A, M, "機能・業務フロー", "正常系", "現金入金を記帳できる",
    "F-STD。得意先100001 有効。",
    "1. 入金日20260901・得意先100001・入金金額50000・入金方法1（現金）を入力し ENTER\n2. 確認で「Y」", "得意先=100001／金額=50000／方法=1",
    "「Details OK - press ENTER to confirm」→「Confirm to post the receipt」→「Receipt posted」を表示。入金番号を採番し入金を記録、売掛元帳へ貸方計上し得意先残高を更新する。",
    "§4.33 / §5 RCPTF/ARLF(AL-CREDIT)/CUSTF(CU-BALANCE)・NUMGEN", "高")
add(A, M, "機能・業務フロー", "正常系", "振込入金で銀行コードを指定し記帳できる",
    "F-STD。得意先100001、銀行0001。",
    "1. 得意先100001・金額50000・入金方法3（振込）・銀行0001 を入力し ENTER\n2. 確認で「Y」", "方法=3／銀行=0001",
    "振込の銀行コードを照合し「Receipt posted」で記帳する。", "§4.33-6 / §3.33-10 分類値（振込）", "中")
add(A, M, "操作性・キー", "正常系", "PF4 で入力内容をクリアする",
    "F-STD。AR0010 入力中。", "1. PF4 を押す", "-", "「Cleared」を表示し、入力内容をクリアする。", "§3.33-2", "低")
for nm, tm, dt, msg, rk in [
    ("入金日が不正だとエラー", "1. 入金日に 20260230 を入力し ENTER", "入金日=20260230", "Receipt date is invalid", "§3.33-3"),
    ("得意先コード未入力はエラー", "1. 得意先を空で ENTER", "得意先=空", "Customer code required", "§3.33-4"),
    ("得意先が得意先マスタに無いとエラー", "1. 得意先999999 を入力し ENTER", "得意先=999999", "Customer not found", "§3.33-5"),
    ("得意先が論理削除済みだとエラー", "1. 削除済み得意先100002 を入力し ENTER", "得意先=100002（削除済）", "Customer is deleted", "§3.33-6"),
    ("入金金額が正値でないとエラー", "1. 入金金額に 0 を入力し ENTER", "金額=0", "Amount must be positive", "§3.33-7"),
    ("入金方法が 1〜4 以外だとエラー", "1. 入金方法に 5 を入力し ENTER", "方法=5", "Method must be 1 to 4", "§3.33-8"),
    ("振込で銀行コード未入力はエラー", "1. 方法3（振込）で銀行コードを空で ENTER", "方法=3／銀行=空", "Bank code required for transfer", "§3.33-10"),
    ("銀行が銀行マスタに無いとエラー", "1. 銀行9999 を入力し ENTER", "銀行=9999", "Bank code not found", "§3.33-11"),
    ("銀行が論理削除済みだとエラー", "1. 削除済み銀行0009 を入力し ENTER", "銀行=0009（削除済）", "Bank is deleted", "§3.33-12"),
    ("入金記帳に失敗するとエラー", "1. 確定時に入金記帳が失敗 *(要実データ)*", "*(要実データ)*", "Receipt write failed", "§3.33-16"),
    ("売掛元帳記帳に失敗するとエラー", "1. 確定時に売掛元帳記帳が失敗 *(要実データ)*", "*(要実データ)*", "Ledger write failed", "§3.33-17"),
]:
    add(A, M, "回復・リラン" if "write" in msg else "入力チェック", "異常系", nm, "F-STD。AR0010 入力中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "データ整合性・冪等性", "正常系", "確認で否認すると入金を破棄する",
    "F-STD。AR0010 確認中。", "1. 確認で記帳を取り止める", "-", "「Receipt discarded」を表示し、記帳しない。", "§3.33-14", "中")
add(A, M, "境界値", "境界値", "入金方法の下限 1・上限 4 を受け付ける",
    "F-STD。得意先100001。", "1. 入金方法に 1 を入力\n2. 別ケースで 4 を入力", "方法=1／方法=4",
    "1（現金）・4 とも範囲内のため受け付け、「Method must be 1 to 4」は出ない。", "§3.33-8 範囲境界", "低")

# ── AR0020 売掛照会・年齢表（§3.34/§4.34） ──
A = "ARINQ"; M = "売掛照会・年齢表（AR0020）"
add(A, M, "画面表示・レイアウト", "正常系", "売掛照会の得意先入力画面が表示される",
    "F-STD。メニューから AR0020 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter customer code then ENTER」が表示される。", "§3.34-2", "中")
add(A, M, "機能・業務フロー", "正常系", "得意先の売掛残高と年齢表を照会できる",
    "F-STD。得意先100001 に売掛あり。", "1. 得意先100001 を入力し ENTER", "得意先=100001",
    "売掛元帳を走査し、現残高と 0-30／31-60／61-90／90 日超の年齢表を表示する。「Ledger scanned - press any key」を表示。",
    "§4.34 / ARLF 年齢バケット", "高")
add(A, M, "メッセージ・異常系", "異常系", "定義外キーでエラー",
    "F-STD。AR0020 表示中。", "1. 定義外キーを押す", "PF10", "「Invalid function key」を表示する。", "§3.34-1", "低")
add(A, M, "入力チェック", "異常系", "得意先コードが 0 だとエラー",
    "F-STD。AR0020 表示中。", "1. 得意先コードに 0 を入力し ENTER", "得意先=0", "「Customer code must not be zero」を表示する。", "§3.34-3", "中")
add(A, M, "入力チェック", "異常系", "得意先が得意先マスタに無いとエラー",
    "F-STD。AR0020 表示中。", "1. 得意先999999 を入力し ENTER", "得意先=999999", "「Customer not found」を表示する。", "§3.34-4", "中")

# ── AP0010 支払入力（§3.35/§4.35） ──
A = "APENT"; M = "支払入力（AP0010）"
add(A, M, "画面表示・レイアウト", "正常系", "支払入力画面が初期表示される",
    "F-STD。メニューから AP0010 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter payment details - PF3 to end」が表示される。", "§3.35-1", "中")
add(A, M, "機能・業務フロー", "正常系", "現金支払を記帳できる",
    "F-STD。仕入先200001 有効。",
    "1. 支払日20260901・仕入先200001・支払金額30000・支払方法1（現金）を入力し ENTER\n2. 確認で「Y」", "仕入先=200001／金額=30000／方法=1",
    "「Details OK - press ENTER to confirm」→「Confirm to post the payment」→「Payment posted」を表示。支払番号を採番し支払を記録、買掛元帳へ借方計上し仕入先残高を更新する。",
    "§4.35 / §5 PAYF/APLF/SUPPF・NUMGEN", "高")
add(A, M, "機能・業務フロー", "正常系", "振込支払で銀行コードを指定し記帳できる",
    "F-STD。仕入先200001、銀行0001。", "1. 支払方法3（振込）・銀行0001 を指定し確定", "方法=3／銀行=0001",
    "振込の銀行コードを照合し「Payment posted」で記帳する。", "§4.35-6 分類値（振込）", "中")
add(A, M, "操作性・キー", "正常系", "PF4 で入力内容をクリアする",
    "F-STD。AP0010 入力中。", "1. PF4 を押す", "-", "「Cleared」を表示する。", "§3.35-2", "低")
for nm, tm, dt, msg, rk in [
    ("支払日が不正だとエラー", "1. 支払日に 20260230 を入力し ENTER", "支払日=20260230", "Payment date is invalid", "§3.35-3"),
    ("仕入先コード未入力はエラー", "1. 仕入先を空で ENTER", "仕入先=空", "Supplier code required", "§3.35-4"),
    ("仕入先が仕入先マスタに無いとエラー", "1. 仕入先999999 を入力し ENTER", "仕入先=999999", "Supplier not found", "§3.35-5"),
    ("仕入先が論理削除済みだとエラー", "1. 削除済み仕入先200002 を入力し ENTER", "仕入先=200002（削除済）", "Supplier is deleted", "§3.35-6"),
    ("支払金額が正値でないとエラー", "1. 支払金額に 0 を入力し ENTER", "金額=0", "Amount must be positive", "§3.35-7"),
    ("支払方法が 1〜4 以外だとエラー", "1. 支払方法に 5 を入力し ENTER", "方法=5", "Method must be 1 to 4", "§3.35-8"),
    ("振込で銀行コード未入力はエラー", "1. 方法3 で銀行コードを空で ENTER", "方法=3／銀行=空", "Bank code required for transfer", "§3.35-10"),
    ("銀行が銀行マスタに無いとエラー", "1. 銀行9999 を入力し ENTER", "銀行=9999", "Bank code not found", "§3.35-11"),
    ("銀行が論理削除済みだとエラー", "1. 削除済み銀行0009 を入力し ENTER", "銀行=0009（削除済）", "Bank is deleted", "§3.35-12"),
    ("支払記帳に失敗するとエラー", "1. 確定時に支払記帳が失敗 *(要実データ)*", "*(要実データ)*", "Payment write failed", "§3.35-16"),
    ("買掛元帳記帳に失敗するとエラー", "1. 確定時に買掛元帳記帳が失敗 *(要実データ)*", "*(要実データ)*", "Ledger write failed", "§3.35-17"),
]:
    add(A, M, "回復・リラン" if "write" in msg else "入力チェック", "異常系", nm, "F-STD。AP0010 入力中。", tm, dt, f"「{msg}」を表示する。", rk, "中")
add(A, M, "データ整合性・冪等性", "正常系", "確認で否認すると支払を破棄する",
    "F-STD。AP0010 確認中。", "1. 確認で記帳を取り止める", "-", "「Payment discarded」を表示し、記帳しない。", "§3.35-14", "中")

# ── AP0020 買掛照会（§3.36/§4.36） ──
A = "APINQ"; M = "買掛照会（AP0020）"
add(A, M, "画面表示・レイアウト", "正常系", "買掛照会の仕入先入力画面が表示される",
    "F-STD。メニューから AP0020 を起動。", "1. 初期表示を確認する", "-",
    "案内「Enter supplier code then ENTER」が表示される。", "§3.36-2", "中")
add(A, M, "機能・業務フロー", "正常系", "仕入先の買掛残高と元帳明細を照会できる",
    "F-STD。仕入先200001 に買掛あり。", "1. 仕入先200001 を入力し ENTER", "仕入先=200001",
    "買掛元帳を走査し、現買掛残高・購入合計・支払合計と直近の元帳明細を表示する。「Ledger scanned - press any key」を表示。",
    "§4.36 / APLF", "高")
add(A, M, "メッセージ・異常系", "異常系", "定義外キーでエラー",
    "F-STD。AP0020 表示中。", "1. 定義外キーを押す", "PF10", "「Invalid function key」を表示する。", "§3.36-1", "低")
add(A, M, "入力チェック", "異常系", "仕入先コードが 0 だとエラー",
    "F-STD。AP0020 表示中。", "1. 仕入先コードに 0 を入力し ENTER", "仕入先=0", "「Supplier code must not be zero」を表示する。", "§3.36-3", "中")
add(A, M, "入力チェック", "異常系", "仕入先が仕入先マスタに無いとエラー",
    "F-STD。AP0020 表示中。", "1. 仕入先999999 を入力し ENTER", "仕入先=999999", "「Supplier not found」を表示する。", "§3.36-4", "中")


if __name__ == "__main__":
    write_out()
