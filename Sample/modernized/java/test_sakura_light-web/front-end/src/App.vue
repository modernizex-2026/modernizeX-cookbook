<script setup lang="ts">
import { ref, computed, nextTick, type Component } from 'vue';
import TerminalScreen from './components/TerminalScreen.vue';
import AP0010ConfirmScreen from './programs/ap0010/AP0010ConfirmScreen.vue';
import AP0010HeadScreen from './programs/ap0010/AP0010HeadScreen.vue';
import AR0020KeyScreen from './programs/ar0020/AR0020KeyScreen.vue';
import AR0020AgeScreen from './programs/ar0020/AR0020AgeScreen.vue';
import AP0020KeyScreen from './programs/ap0020/AP0020KeyScreen.vue';
import AP0020ViewScreen from './programs/ap0020/AP0020ViewScreen.vue';
import AR0010HeadScreen from './programs/ar0010/AR0010HeadScreen.vue';
import AR0010ConfirmScreen from './programs/ar0010/AR0010ConfirmScreen.vue';
import IV0010KeyScreen from './programs/iv0010/IV0010KeyScreen.vue';
import IV0010DetailScreen from './programs/iv0010/IV0010DetailScreen.vue';
import IV0020KeyScreen from './programs/iv0020/IV0020KeyScreen.vue';
import IV0020AdjScreen from './programs/iv0020/IV0020AdjScreen.vue';
import IV0020ConfirmScreen from './programs/iv0020/IV0020ConfirmScreen.vue';
import IV0030WhScreen from './programs/iv0030/IV0030WhScreen.vue';
import IV0030CountScreen from './programs/iv0030/IV0030CountScreen.vue';
import IV0030SummaryScreen from './programs/iv0030/IV0030SummaryScreen.vue';
import IV0030ReviewScreen from './programs/iv0030/IV0030ReviewScreen.vue';
import IV0040KeyScreen from './programs/iv0040/IV0040KeyScreen.vue';
import IV0040ListScreen from './programs/iv0040/IV0040ListScreen.vue';
import IV0050KeyScreen from './programs/iv0050/IV0050KeyScreen.vue';
import IV0050ShowScreen from './programs/iv0050/IV0050ShowScreen.vue';
import IV0050ConfirmScreen from './programs/iv0050/IV0050ConfirmScreen.vue';
import MENU00LoginScreen from './programs/menu00/MENU00LoginScreen.vue';
import MENU00MainScreen from './programs/menu00/MENU00MainScreen.vue';
import MENU00MenuMstScreen from './programs/menu00/MENU00MenuMstScreen.vue';
import MENU00MenuOrdScreen from './programs/menu00/MENU00MenuOrdScreen.vue';
import MENU00MenuPurScreen from './programs/menu00/MENU00MenuPurScreen.vue';
import MENU00MenuInvScreen from './programs/menu00/MENU00MenuInvScreen.vue';
import MENU00MenuRptScreen from './programs/menu00/MENU00MenuRptScreen.vue';
import MENU00MenuBatScreen from './programs/menu00/MENU00MenuBatScreen.vue';
import MS0010KeyScreen from './programs/ms0010/MS0010KeyScreen.vue';
import MS0010BodyScreen from './programs/ms0010/MS0010BodyScreen.vue';
import MS0010ConfirmScreen from './programs/ms0010/MS0010ConfirmScreen.vue';
import MS0020KeyScreen from './programs/ms0020/MS0020KeyScreen.vue';
import MS0020BodyScreen from './programs/ms0020/MS0020BodyScreen.vue';
import MS0020ConfirmScreen from './programs/ms0020/MS0020ConfirmScreen.vue';
import MS0030KeyScreen from './programs/ms0030/MS0030KeyScreen.vue';
import MS0030BodyScreen from './programs/ms0030/MS0030BodyScreen.vue';
import MS0030ConfirmScreen from './programs/ms0030/MS0030ConfirmScreen.vue';
import MS0040KeyScreen from './programs/ms0040/MS0040KeyScreen.vue';
import MS0040BodyScreen from './programs/ms0040/MS0040BodyScreen.vue';
import MS0040ConfirmScreen from './programs/ms0040/MS0040ConfirmScreen.vue';
import MS0050KeyScreen from './programs/ms0050/MS0050KeyScreen.vue';
import MS0050BodyScreen from './programs/ms0050/MS0050BodyScreen.vue';
import MS0050ConfirmScreen from './programs/ms0050/MS0050ConfirmScreen.vue';
import MS0060KeyScreen from './programs/ms0060/MS0060KeyScreen.vue';
import MS0060BodyScreen from './programs/ms0060/MS0060BodyScreen.vue';
import MS0060ConfirmScreen from './programs/ms0060/MS0060ConfirmScreen.vue';
import MS0070KeyScreen from './programs/ms0070/MS0070KeyScreen.vue';
import MS0070BodyScreen from './programs/ms0070/MS0070BodyScreen.vue';
import MS0070ConfirmScreen from './programs/ms0070/MS0070ConfirmScreen.vue';
import MS0080KeyScreen from './programs/ms0080/MS0080KeyScreen.vue';
import MS0080BodyScreen from './programs/ms0080/MS0080BodyScreen.vue';
import MS0080ConfirmScreen from './programs/ms0080/MS0080ConfirmScreen.vue';
import MS0090KeyScreen from './programs/ms0090/MS0090KeyScreen.vue';
import MS0090BodyScreen from './programs/ms0090/MS0090BodyScreen.vue';
import MS0090ConfirmScreen from './programs/ms0090/MS0090ConfirmScreen.vue';
import MS0100KeyScreen from './programs/ms0100/MS0100KeyScreen.vue';
import MS0100BodyScreen from './programs/ms0100/MS0100BodyScreen.vue';
import MS0100ConfirmScreen from './programs/ms0100/MS0100ConfirmScreen.vue';
import MS0110KeyScreen from './programs/ms0110/MS0110KeyScreen.vue';
import MS0110BodyScreen from './programs/ms0110/MS0110BodyScreen.vue';
import MS0110ConfirmScreen from './programs/ms0110/MS0110ConfirmScreen.vue';
import MS0120KeyScreen from './programs/ms0120/MS0120KeyScreen.vue';
import MS0120ConfirmScreen from './programs/ms0120/MS0120ConfirmScreen.vue';
import MS0120BodyScreen from './programs/ms0120/MS0120BodyScreen.vue';
import OE0010DetailScreen from './programs/oe0010/OE0010DetailScreen.vue';
import OE0010StatusScreen from './programs/oe0010/OE0010StatusScreen.vue';
import OE0010ConfirmScreen from './programs/oe0010/OE0010ConfirmScreen.vue';
import OE0010HeadScreen from './programs/oe0010/OE0010HeadScreen.vue';
import OE0020KeyScreen from './programs/oe0020/OE0020KeyScreen.vue';
import OE0020StatusScreen from './programs/oe0020/OE0020StatusScreen.vue';
import OE0020BrowseScreen from './programs/oe0020/OE0020BrowseScreen.vue';
import OE0020OrderScreen from './programs/oe0020/OE0020OrderScreen.vue';
import OE0030KeyScreen from './programs/oe0030/OE0030KeyScreen.vue';
import OE0030PageScreen from './programs/oe0030/OE0030PageScreen.vue';
import OE0030ConfirmScreen from './programs/oe0030/OE0030ConfirmScreen.vue';
import OE0030BrowseScreen from './programs/oe0030/OE0030BrowseScreen.vue';
import OE0030OrderScreen from './programs/oe0030/OE0030OrderScreen.vue';
import OE0040KeyScreen from './programs/oe0040/OE0040KeyScreen.vue';
import OE0040TotalScreen from './programs/oe0040/OE0040TotalScreen.vue';
import OE0040CmdScreen from './programs/oe0040/OE0040CmdScreen.vue';
import OE0040DetailScreen from './programs/oe0040/OE0040DetailScreen.vue';
import OE0040ChglineScreen from './programs/oe0040/OE0040ChglineScreen.vue';
import OE0040ConfirmScreen from './programs/oe0040/OE0040ConfirmScreen.vue';
import OE0040InfoScreen from './programs/oe0040/OE0040InfoScreen.vue';
import OE0040ListScreen from './programs/oe0040/OE0040ListScreen.vue';
import OE0040HdrScreen from './programs/oe0040/OE0040HdrScreen.vue';
import PU0010DetailScreen from './programs/pu0010/PU0010DetailScreen.vue';
import PU0010StatusScreen from './programs/pu0010/PU0010StatusScreen.vue';
import PU0010ConfirmScreen from './programs/pu0010/PU0010ConfirmScreen.vue';
import PU0010HeadScreen from './programs/pu0010/PU0010HeadScreen.vue';
import PU0020SearchScreen from './programs/pu0020/PU0020SearchScreen.vue';
import PU0020HdrviewScreen from './programs/pu0020/PU0020HdrviewScreen.vue';
import PU0020ColhdrScreen from './programs/pu0020/PU0020ColhdrScreen.vue';
import PU0020RowsScreen from './programs/pu0020/PU0020RowsScreen.vue';
import PU0020NavScreen from './programs/pu0020/PU0020NavScreen.vue';
import PU0030KeyScreen from './programs/pu0030/PU0030KeyScreen.vue';
import PU0030RinfoScreen from './programs/pu0030/PU0030RinfoScreen.vue';
import PU0030VheadScreen from './programs/pu0030/PU0030VheadScreen.vue';
import PU0030ColhdrScreen from './programs/pu0030/PU0030ColhdrScreen.vue';
import PU0030RowsScreen from './programs/pu0030/PU0030RowsScreen.vue';
import PU0030TotalScreen from './programs/pu0030/PU0030TotalScreen.vue';
import PU0030ConfirmScreen from './programs/pu0030/PU0030ConfirmScreen.vue';
import PU0040HeadScreen from './programs/pu0040/PU0040HeadScreen.vue';
import PU0040DetailScreen from './programs/pu0040/PU0040DetailScreen.vue';
import PU0040StatusScreen from './programs/pu0040/PU0040StatusScreen.vue';
import PU0040ConfirmScreen from './programs/pu0040/PU0040ConfirmScreen.vue';
import RC0010KeyScreen from './programs/rc0010/RC0010KeyScreen.vue';
import RC0010PinfoScreen from './programs/rc0010/RC0010PinfoScreen.vue';
import RC0010RdateScreen from './programs/rc0010/RC0010RdateScreen.vue';
import RC0010LineScreen from './programs/rc0010/RC0010LineScreen.vue';
import RC0010ConfirmScreen from './programs/rc0010/RC0010ConfirmScreen.vue';
import SH0010KeyScreen from './programs/sh0010/SH0010KeyScreen.vue';
import SH0010StatusScreen from './programs/sh0010/SH0010StatusScreen.vue';
import SH0010EditScreen from './programs/sh0010/SH0010EditScreen.vue';
import SH0010ConfirmScreen from './programs/sh0010/SH0010ConfirmScreen.vue';
import SH0010OrderScreen from './programs/sh0010/SH0010OrderScreen.vue';
import SL0010KeyScreen from './programs/sl0010/SL0010KeyScreen.vue';
import SL0010DheadScreen from './programs/sl0010/SL0010DheadScreen.vue';
import SL0010DetailScreen from './programs/sl0010/SL0010DetailScreen.vue';
import SL0010EstatScreen from './programs/sl0010/SL0010EstatScreen.vue';
import SL0010TotalScreen from './programs/sl0010/SL0010TotalScreen.vue';
import SL0010ConfirmScreen from './programs/sl0010/SL0010ConfirmScreen.vue';
import SL0010BrowseScreen from './programs/sl0010/SL0010BrowseScreen.vue';
import SL0010InvScreen from './programs/sl0010/SL0010InvScreen.vue';
import SL0020KeyScreen from './programs/sl0020/SL0020KeyScreen.vue';
import SL0020StatusScreen from './programs/sl0020/SL0020StatusScreen.vue';
import SL0020BrowseScreen from './programs/sl0020/SL0020BrowseScreen.vue';
import SL0020InvScreen from './programs/sl0020/SL0020InvScreen.vue';
import SL0030KeyScreen from './programs/sl0030/SL0030KeyScreen.vue';
import SL0030DheadScreen from './programs/sl0030/SL0030DheadScreen.vue';
import SL0030DetailScreen from './programs/sl0030/SL0030DetailScreen.vue';
import SL0030EstatScreen from './programs/sl0030/SL0030EstatScreen.vue';
import SL0030TotalScreen from './programs/sl0030/SL0030TotalScreen.vue';
import SL0030ConfirmScreen from './programs/sl0030/SL0030ConfirmScreen.vue';
import SL0030BrowseScreen from './programs/sl0030/SL0030BrowseScreen.vue';
import SL0030InvScreen from './programs/sl0030/SL0030InvScreen.vue';
import SL0040KeyScreen from './programs/sl0040/SL0040KeyScreen.vue';
import SL0040PstatScreen from './programs/sl0040/SL0040PstatScreen.vue';
import SL0040PickScreen from './programs/sl0040/SL0040PickScreen.vue';
import SL0040ConfirmScreen from './programs/sl0040/SL0040ConfirmScreen.vue';
import SL0040InfoScreen from './programs/sl0040/SL0040InfoScreen.vue';
import SL0040ListScreen from './programs/sl0040/SL0040ListScreen.vue';

// modernize-screens.js --init populates this lookup with
// entries like: programId === '<program>' ? PROGRAM_OVERRIDES : ...
const AP0010_OVERRIDES = {
  'DS-CONFIRM': AP0010ConfirmScreen,
  'FIELD:SC-CONF': AP0010ConfirmScreen,
  'DS-HEAD': AP0010HeadScreen,
  'FIELD:PY-DATE': AP0010HeadScreen,
  'FIELD:PY-SUPP': AP0010HeadScreen,
  'FIELD:PY-METHOD': AP0010HeadScreen,
  'FIELD:PY-AMOUNT': AP0010HeadScreen,
  'FIELD:PY-BANK-CODE': AP0010HeadScreen,
  'FIELD:PY-REMARK': AP0010HeadScreen,
} as const;

const AR0010_OVERRIDES = {
  'DS-HEAD': AR0010HeadScreen,
  'FIELD:RE-DATE': AR0010HeadScreen,
  'FIELD:RE-CUST': AR0010HeadScreen,
  'FIELD:RE-METHOD': AR0010HeadScreen,
  'FIELD:RE-AMOUNT': AR0010HeadScreen,
  'FIELD:RE-BANK-CODE': AR0010HeadScreen,
  'FIELD:RE-REMARK': AR0010HeadScreen,
  'DS-CONFIRM': AR0010ConfirmScreen,
  'FIELD:SC-CONF': AR0010ConfirmScreen,
} as const;

const AR0020_OVERRIDES = {
  'DS-KEY': AR0020KeyScreen,
  'FIELD:WK-KEY-CUST': AR0020KeyScreen,
  'DS-AGE': AR0020AgeScreen,
} as const;

const AP0020_OVERRIDES = {
  'DS-KEY': AP0020KeyScreen,
  'FIELD:WK-KEY-SUPP': AP0020KeyScreen,
  'DS-VIEW': AP0020ViewScreen,
} as const;

const IV0010_OVERRIDES = {
  'DS-KEY': IV0010KeyScreen,
  'FIELD:WK-ORDER': IV0010KeyScreen,
  'FIELD:WK-KEY-PROD': IV0010KeyScreen,
  'FIELD:WK-KEY-WHSE': IV0010KeyScreen,
  'DS-DETAIL': IV0010DetailScreen,
} as const;

const IV0020_OVERRIDES = {
  'DS-KEY': IV0020KeyScreen,
  'FIELD:WK-KEY-PROD': IV0020KeyScreen,
  'FIELD:WK-KEY-WHSE': IV0020KeyScreen,
  'DS-ADJ': IV0020AdjScreen,
  'FIELD:WK-ADJ-QTY': IV0020AdjScreen,
  'FIELD:WK-REASON': IV0020AdjScreen,
  'DS-CONFIRM': IV0020ConfirmScreen,
  'FIELD:SC-CONF': IV0020ConfirmScreen,
} as const;

const IV0030_OVERRIDES = {
  'DS-WH': IV0030WhScreen,
  'FIELD:WK-WHSE-IN': IV0030WhScreen,
  'DS-COUNT': IV0030CountScreen,
  'FIELD:WK-COUNTED': IV0030CountScreen,
  'DS-SUMMARY': IV0030SummaryScreen,
  'FIELD:SC-CONF': IV0030SummaryScreen,
  'DS-REVIEW': IV0030ReviewScreen,
} as const;

const IV0040_OVERRIDES = {
  'DS-KEY': IV0040KeyScreen,
  'FIELD:WK-KEY-PROD': IV0040KeyScreen,
  'FIELD:WK-KEY-DATE': IV0040KeyScreen,
  'DS-LIST': IV0040ListScreen,
} as const;

const IV0050_OVERRIDES = {
  'DS-KEY': IV0050KeyScreen,
  'FIELD:WK-KEY-PROD': IV0050KeyScreen,
  'FIELD:WK-FROM-WHSE': IV0050KeyScreen,
  'FIELD:WK-TO-WHSE': IV0050KeyScreen,
  'FIELD:WK-TR-QTY': IV0050KeyScreen,
  'DS-SHOW': IV0050ShowScreen,
  'DS-CONFIRM': IV0050ConfirmScreen,
  'FIELD:SC-CONF': IV0050ConfirmScreen,
} as const;

const MENU00_OVERRIDES = {
  'DS-LOGIN': MENU00LoginScreen,
  'FIELD:KL-LOGIN': MENU00LoginScreen,
  'FIELD:KL-PASSWORD': MENU00LoginScreen,
  'DS-MAIN': MENU00MainScreen,
  'FIELD:WK-CHOICE': MENU00MainScreen,
  'DS-MENU-MST': MENU00MenuMstScreen,
  'DS-MENU-ORD': MENU00MenuOrdScreen,
  'DS-MENU-PUR': MENU00MenuPurScreen,
  'DS-MENU-INV': MENU00MenuInvScreen,
  'DS-MENU-RPT': MENU00MenuRptScreen,
  'DS-MENU-BAT': MENU00MenuBatScreen,
} as const;

const MS0010_OVERRIDES = {
  'DS-KEY': MS0010KeyScreen,
  'FIELD:SC-KEY': MS0010KeyScreen,
  'DS-BODY': MS0010BodyScreen,
  'FIELD:CU-NAME': MS0010BodyScreen,
  'FIELD:CU-KANA': MS0010BodyScreen,
  'FIELD:CU-ZIP': MS0010BodyScreen,
  'FIELD:CU-ADDR1': MS0010BodyScreen,
  'FIELD:CU-ADDR2': MS0010BodyScreen,
  'FIELD:CU-TEL': MS0010BodyScreen,
  'FIELD:CU-FAX': MS0010BodyScreen,
  'FIELD:CU-REGION': MS0010BodyScreen,
  'FIELD:CU-STAFF': MS0010BodyScreen,
  'FIELD:CU-CLOSE-DAY': MS0010BodyScreen,
  'FIELD:CU-PAY-METHOD': MS0010BodyScreen,
  'FIELD:CU-TAX-TYPE': MS0010BodyScreen,
  'FIELD:CU-CREDIT-LIMIT': MS0010BodyScreen,
  'FIELD:CU-PRICE-RANK': MS0010BodyScreen,
  'FIELD:CU-BANK-CODE': MS0010BodyScreen,
  'DS-CONFIRM': MS0010ConfirmScreen,
  'FIELD:SC-CONF': MS0010ConfirmScreen,
} as const;

const MS0020_OVERRIDES = {
  'DS-KEY': MS0020KeyScreen,
  'FIELD:SC-KEY': MS0020KeyScreen,
  'DS-BODY': MS0020BodyScreen,
  'FIELD:SP-NAME': MS0020BodyScreen,
  'FIELD:SP-KANA': MS0020BodyScreen,
  'FIELD:SP-ZIP': MS0020BodyScreen,
  'FIELD:SP-ADDR1': MS0020BodyScreen,
  'FIELD:SP-ADDR2': MS0020BodyScreen,
  'FIELD:SP-TEL': MS0020BodyScreen,
  'FIELD:SP-FAX': MS0020BodyScreen,
  'FIELD:SP-CLOSE-DAY': MS0020BodyScreen,
  'FIELD:SP-PAY-MONTH': MS0020BodyScreen,
  'FIELD:SP-PAY-DAY': MS0020BodyScreen,
  'FIELD:SP-PAY-METHOD': MS0020BodyScreen,
  'FIELD:SP-TAX-TYPE': MS0020BodyScreen,
  'FIELD:SP-BANK-CODE': MS0020BodyScreen,
  'FIELD:SP-BANK-ACCT': MS0020BodyScreen,
  'DS-CONFIRM': MS0020ConfirmScreen,
  'FIELD:SC-CONF': MS0020ConfirmScreen,
} as const;

const MS0030_OVERRIDES = {
  'DS-KEY': MS0030KeyScreen,
  'FIELD:SC-KEY': MS0030KeyScreen,
  'DS-BODY': MS0030BodyScreen,
  'FIELD:PR-NAME': MS0030BodyScreen,
  'FIELD:PR-KANA': MS0030BodyScreen,
  'FIELD:PR-SPEC': MS0030BodyScreen,
  'FIELD:PR-CATEGORY': MS0030BodyScreen,
  'FIELD:PR-BARCODE': MS0030BodyScreen,
  'FIELD:PR-UNIT': MS0030BodyScreen,
  'FIELD:PR-STD-COST': MS0030BodyScreen,
  'FIELD:PR-LIST-PRICE': MS0030BodyScreen,
  'FIELD:PR-LAST-COST': MS0030BodyScreen,
  'FIELD:PR-TAX-CATEGORY': MS0030BodyScreen,
  'FIELD:PR-RANK-PRICE(1)': MS0030BodyScreen,
  'FIELD:PR-RANK-PRICE(2)': MS0030BodyScreen,
  'FIELD:PR-RANK-PRICE(3)': MS0030BodyScreen,
  'FIELD:PR-RANK-PRICE(4)': MS0030BodyScreen,
  'FIELD:PR-RANK-PRICE(5)': MS0030BodyScreen,
  'FIELD:PR-SAFETY-STOCK': MS0030BodyScreen,
  'FIELD:PR-REORDER-POINT': MS0030BodyScreen,
  'FIELD:PR-REORDER-QTY': MS0030BodyScreen,
  'FIELD:PR-LEAD-DAYS': MS0030BodyScreen,
  'FIELD:PR-DFLT-SUPP': MS0030BodyScreen,
  'FIELD:PR-DFLT-WHSE': MS0030BodyScreen,
  'FIELD:PR-STOCK-MNG': MS0030BodyScreen,
  'DS-CONFIRM': MS0030ConfirmScreen,
  'FIELD:SC-CONF': MS0030ConfirmScreen,
} as const;

const MS0040_OVERRIDES = {
  'DS-KEY': MS0040KeyScreen,
  'FIELD:SC-KEY': MS0040KeyScreen,
  'DS-BODY': MS0040BodyScreen,
  'FIELD:WH-NAME': MS0040BodyScreen,
  'FIELD:WH-ZIP': MS0040BodyScreen,
  'FIELD:WH-ADDR': MS0040BodyScreen,
  'FIELD:WH-TEL': MS0040BodyScreen,
  'FIELD:WH-TYPE': MS0040BodyScreen,
  'FIELD:WH-MANAGER': MS0040BodyScreen,
  'DS-CONFIRM': MS0040ConfirmScreen,
  'FIELD:SC-CONF': MS0040ConfirmScreen,
} as const;

const MS0050_OVERRIDES = {
  'DS-KEY': MS0050KeyScreen,
  'FIELD:SC-KEY': MS0050KeyScreen,
  'DS-BODY': MS0050BodyScreen,
  'FIELD:SF-NAME': MS0050BodyScreen,
  'FIELD:SF-KANA': MS0050BodyScreen,
  'FIELD:SF-DEPT': MS0050BodyScreen,
  'FIELD:SF-TEL': MS0050BodyScreen,
  'FIELD:SF-EMAIL': MS0050BodyScreen,
  'FIELD:SF-TITLE': MS0050BodyScreen,
  'DS-CONFIRM': MS0050ConfirmScreen,
  'FIELD:SC-CONF': MS0050ConfirmScreen,
} as const;

const MS0060_OVERRIDES = {
  'DS-KEY': MS0060KeyScreen,
  'FIELD:SC-KEY': MS0060KeyScreen,
  'DS-BODY': MS0060BodyScreen,
  'FIELD:CT-NAME': MS0060BodyScreen,
  'FIELD:CT-PARENT': MS0060BodyScreen,
  'FIELD:CT-LEVEL': MS0060BodyScreen,
  'DS-CONFIRM': MS0060ConfirmScreen,
  'FIELD:SC-CONF': MS0060ConfirmScreen,
} as const;

const MS0070_OVERRIDES = {
  'DS-KEY': MS0070KeyScreen,
  'FIELD:CP-CUST': MS0070KeyScreen,
  'FIELD:CP-PROD': MS0070KeyScreen,
  'DS-BODY': MS0070BodyScreen,
  'FIELD:CP-PRICE': MS0070BodyScreen,
  'FIELD:CP-START-DATE': MS0070BodyScreen,
  'FIELD:CP-END-DATE': MS0070BodyScreen,
  'DS-CONFIRM': MS0070ConfirmScreen,
  'FIELD:SC-CONF': MS0070ConfirmScreen,
} as const;

const MS0080_OVERRIDES = {
  'DS-KEY': MS0080KeyScreen,
  'FIELD:SC-KEY': MS0080KeyScreen,
  'DS-BODY': MS0080BodyScreen,
  'FIELD:DP-NAME': MS0080BodyScreen,
  'FIELD:DP-PARENT': MS0080BodyScreen,
  'DS-CONFIRM': MS0080ConfirmScreen,
  'FIELD:SC-CONF': MS0080ConfirmScreen,
} as const;

const MS0090_OVERRIDES = {
  'DS-KEY': MS0090KeyScreen,
  'FIELD:TX-CODE': MS0090KeyScreen,
  'FIELD:TX-START-DATE': MS0090KeyScreen,
  'DS-BODY': MS0090BodyScreen,
  'FIELD:TX-RATE': MS0090BodyScreen,
  'FIELD:TX-NAME': MS0090BodyScreen,
  'DS-CONFIRM': MS0090ConfirmScreen,
  'FIELD:SC-CONF': MS0090ConfirmScreen,
} as const;

const MS0100_OVERRIDES = {
  'DS-KEY': MS0100KeyScreen,
  'FIELD:SC-KEY': MS0100KeyScreen,
  'DS-BODY': MS0100BodyScreen,
  'FIELD:BK-NAME': MS0100BodyScreen,
  'FIELD:BK-BRANCH': MS0100BodyScreen,
  'DS-CONFIRM': MS0100ConfirmScreen,
  'FIELD:SC-CONF': MS0100ConfirmScreen,
} as const;

const MS0110_OVERRIDES = {
  'DS-KEY': MS0110KeyScreen,
  'FIELD:SC-KEY': MS0110KeyScreen,
  'DS-BODY': MS0110BodyScreen,
  'FIELD:RG-NAME': MS0110BodyScreen,
  'DS-CONFIRM': MS0110ConfirmScreen,
  'FIELD:SC-CONF': MS0110ConfirmScreen,
} as const;

const MS0120_OVERRIDES = {
  'DS-KEY': MS0120KeyScreen,
  'FIELD:SC-KEY': MS0120KeyScreen,
  'DS-CONFIRM': MS0120ConfirmScreen,
  'FIELD:SC-CONF': MS0120ConfirmScreen,
  'DS-BODY': MS0120BodyScreen,
  'FIELD:US-LOGIN': MS0120BodyScreen,
  'FIELD:US-PASSWORD': MS0120BodyScreen,
  'FIELD:US-NAME': MS0120BodyScreen,
  'FIELD:US-ROLE': MS0120BodyScreen,
  'FIELD:US-AUTH-MASTER': MS0120BodyScreen,
  'FIELD:US-AUTH-ORDER': MS0120BodyScreen,
  'FIELD:US-AUTH-SALES': MS0120BodyScreen,
  'FIELD:US-AUTH-PURCH': MS0120BodyScreen,
  'FIELD:US-AUTH-CLOSE': MS0120BodyScreen,
} as const;

const OE0010_OVERRIDES = {
  'DS-DETAIL': OE0010DetailScreen,
  'FIELD:WK-D-PROD': OE0010DetailScreen,
  'FIELD:WK-D-WHSE': OE0010DetailScreen,
  'FIELD:WK-D-QTY': OE0010DetailScreen,
  'FIELD:WK-D-PRICE': OE0010DetailScreen,
  'DS-STATUS': OE0010StatusScreen,
  'DS-CONFIRM': OE0010ConfirmScreen,
  'FIELD:WK-CONFIRM': OE0010ConfirmScreen,
  'DS-HEAD': OE0010HeadScreen,
  'FIELD:OH-DATE': OE0010HeadScreen,
  'FIELD:OH-CUST': OE0010HeadScreen,
  'FIELD:OH-STAFF': OE0010HeadScreen,
  'FIELD:OH-WHSE': OE0010HeadScreen,
  'FIELD:OH-DUE-DATE': OE0010HeadScreen,
  'FIELD:OH-CUST-PO': OE0010HeadScreen,
  'FIELD:OH-TAX-TYPE': OE0010HeadScreen,
} as const;

const OE0020_OVERRIDES = {
  'DS-KEY': OE0020KeyScreen,
  'FIELD:WK-SEL-NO': OE0020KeyScreen,
  'FIELD:WK-SEL-CUST': OE0020KeyScreen,
  'FIELD:WK-SEL-DATE': OE0020KeyScreen,
  'DS-STATUS': OE0020StatusScreen,
  'DS-BROWSE': OE0020BrowseScreen,
  'FIELD:WK-DUMMY': OE0020BrowseScreen,
  'DS-ORDER': OE0020OrderScreen,
} as const;

const OE0030_OVERRIDES = {
  'DS-KEY': OE0030KeyScreen,
  'FIELD:WK-SEL-NO': OE0030KeyScreen,
  'DS-PAGE': OE0030PageScreen,
  'DS-CONFIRM': OE0030ConfirmScreen,
  'FIELD:WK-CONFIRM': OE0030ConfirmScreen,
  'DS-BROWSE': OE0030BrowseScreen,
  'FIELD:WK-DUMMY': OE0030BrowseScreen,
  'DS-ORDER': OE0030OrderScreen,
} as const;

const OE0040_OVERRIDES = {
  'DS-KEY': OE0040KeyScreen,
  'FIELD:WK-SEL-ORD': OE0040KeyScreen,
  'DS-TOTAL': OE0040TotalScreen,
  'DS-CMD': OE0040CmdScreen,
  'FIELD:WK-CMD': OE0040CmdScreen,
  'FIELD:WK-CMD-ARG': OE0040CmdScreen,
  'DS-DETAIL': OE0040DetailScreen,
  'FIELD:WK-D-PROD': OE0040DetailScreen,
  'FIELD:WK-D-WHSE': OE0040DetailScreen,
  'FIELD:WK-D-QTY': OE0040DetailScreen,
  'FIELD:WK-D-PRICE': OE0040DetailScreen,
  'DS-CHGLINE': OE0040ChglineScreen,
  'DS-CONFIRM': OE0040ConfirmScreen,
  'FIELD:SC-CONF': OE0040ConfirmScreen,
  'DS-INFO': OE0040InfoScreen,
  'DS-LIST': OE0040ListScreen,
  'DS-HDR': OE0040HdrScreen,
  'FIELD:OH-DATE': OE0040HdrScreen,
  'FIELD:OH-CUST': OE0040HdrScreen,
  'FIELD:OH-STAFF': OE0040HdrScreen,
  'FIELD:OH-WHSE': OE0040HdrScreen,
  'FIELD:OH-DUE-DATE': OE0040HdrScreen,
  'FIELD:OH-CUST-PO': OE0040HdrScreen,
  'FIELD:OH-TAX-TYPE': OE0040HdrScreen,
  'FIELD:OH-REMARK': OE0040HdrScreen,
} as const;

const PU0010_OVERRIDES = {
  'DS-DETAIL': PU0010DetailScreen,
  'FIELD:WK-D-PROD': PU0010DetailScreen,
  'FIELD:WK-D-WHSE': PU0010DetailScreen,
  'FIELD:WK-D-QTY': PU0010DetailScreen,
  'FIELD:WK-D-COST': PU0010DetailScreen,
  'DS-STATUS': PU0010StatusScreen,
  'DS-CONFIRM': PU0010ConfirmScreen,
  'FIELD:WK-CONFIRM': PU0010ConfirmScreen,
  'DS-HEAD': PU0010HeadScreen,
  'FIELD:PH-DATE': PU0010HeadScreen,
  'FIELD:PH-SUPP': PU0010HeadScreen,
  'FIELD:PH-WHSE': PU0010HeadScreen,
  'FIELD:PH-STAFF': PU0010HeadScreen,
  'FIELD:PH-DUE-DATE': PU0010HeadScreen,
  'FIELD:PH-TAX-TYPE': PU0010HeadScreen,
  'FIELD:PH-REMARK': PU0010HeadScreen,
} as const;

const PU0020_OVERRIDES = {
  'DS-SEARCH': PU0020SearchScreen,
  'FIELD:WK-SRCH-NO': PU0020SearchScreen,
  'FIELD:WK-SRCH-SUPP': PU0020SearchScreen,
  'FIELD:WK-SRCH-DATE': PU0020SearchScreen,
  'DS-HDRVIEW': PU0020HdrviewScreen,
  'DS-COLHDR': PU0020ColhdrScreen,
  'DS-ROWS': PU0020RowsScreen,
  'DS-NAV': PU0020NavScreen,
  'FIELD:WK-NAV': PU0020NavScreen,
} as const;

const PU0030_OVERRIDES = {
  'DS-KEY': PU0030KeyScreen,
  'FIELD:WK-RECV-KEY': PU0030KeyScreen,
  'DS-RINFO': PU0030RinfoScreen,
  'DS-VHEAD': PU0030VheadScreen,
  'FIELD:VH-DATE': PU0030VheadScreen,
  'FIELD:VH-TAX-TYPE': PU0030VheadScreen,
  'DS-COLHDR': PU0030ColhdrScreen,
  'DS-ROWS': PU0030RowsScreen,
  'DS-TOTAL': PU0030TotalScreen,
  'DS-CONFIRM': PU0030ConfirmScreen,
  'FIELD:WK-CONFIRM': PU0030ConfirmScreen,
} as const;

const PU0040_OVERRIDES = {
  'DS-HEAD': PU0040HeadScreen,
  'FIELD:VH-DATE': PU0040HeadScreen,
  'FIELD:VH-SUPP': PU0040HeadScreen,
  'FIELD:VH-TAX-TYPE': PU0040HeadScreen,
  'FIELD:VH-REMARK': PU0040HeadScreen,
  'DS-DETAIL': PU0040DetailScreen,
  'FIELD:WK-D-PROD': PU0040DetailScreen,
  'FIELD:WK-D-WHSE': PU0040DetailScreen,
  'FIELD:WK-D-QTY': PU0040DetailScreen,
  'FIELD:WK-D-COST': PU0040DetailScreen,
  'DS-STATUS': PU0040StatusScreen,
  'DS-CONFIRM': PU0040ConfirmScreen,
  'FIELD:WK-CONFIRM': PU0040ConfirmScreen,
} as const;

const RC0010_OVERRIDES = {
  'DS-KEY': RC0010KeyScreen,
  'FIELD:WK-PO-KEY': RC0010KeyScreen,
  'DS-PINFO': RC0010PinfoScreen,
  'DS-RDATE': RC0010RdateScreen,
  'FIELD:RH-DATE': RC0010RdateScreen,
  'DS-LINE': RC0010LineScreen,
  'FIELD:WK-D-RECV': RC0010LineScreen,
  'DS-CONFIRM': RC0010ConfirmScreen,
  'FIELD:WK-CONFIRM': RC0010ConfirmScreen,
} as const;

const SH0010_OVERRIDES = {
  'DS-KEY': SH0010KeyScreen,
  'FIELD:WK-SEL-NO': SH0010KeyScreen,
  'DS-STATUS': SH0010StatusScreen,
  'DS-EDIT': SH0010EditScreen,
  'FIELD:WK-EDIT-LN': SH0010EditScreen,
  'FIELD:WK-EDIT-QTY': SH0010EditScreen,
  'DS-CONFIRM': SH0010ConfirmScreen,
  'FIELD:WK-CONFIRM': SH0010ConfirmScreen,
  'DS-ORDER': SH0010OrderScreen,
} as const;

const SL0010_OVERRIDES = {
  'DS-KEY': SL0010KeyScreen,
  'FIELD:WK-SEL-SHIP': SL0010KeyScreen,
  'FIELD:WK-SEL-CUST': SL0010KeyScreen,
  'DS-DHEAD': SL0010DheadScreen,
  'FIELD:WK-STAFF-IN': SL0010DheadScreen,
  'FIELD:WK-TAXTYPE-IN': SL0010DheadScreen,
  'DS-DETAIL': SL0010DetailScreen,
  'FIELD:WK-D-PROD': SL0010DetailScreen,
  'FIELD:WK-D-WHSE': SL0010DetailScreen,
  'FIELD:WK-D-QTY': SL0010DetailScreen,
  'FIELD:WK-D-PRICE': SL0010DetailScreen,
  'DS-ESTAT': SL0010EstatScreen,
  'DS-TOTAL': SL0010TotalScreen,
  'DS-CONFIRM': SL0010ConfirmScreen,
  'FIELD:WK-CONFIRM': SL0010ConfirmScreen,
  'DS-BROWSE': SL0010BrowseScreen,
  'FIELD:WK-DUMMY': SL0010BrowseScreen,
  'DS-INV': SL0010InvScreen,
} as const;

const SL0020_OVERRIDES = {
  'DS-KEY': SL0020KeyScreen,
  'FIELD:WK-SEL-NO': SL0020KeyScreen,
  'FIELD:WK-SEL-CUST': SL0020KeyScreen,
  'FIELD:WK-SEL-DATE': SL0020KeyScreen,
  'DS-STATUS': SL0020StatusScreen,
  'DS-BROWSE': SL0020BrowseScreen,
  'FIELD:WK-DUMMY': SL0020BrowseScreen,
  'DS-INV': SL0020InvScreen,
} as const;

const SL0030_OVERRIDES = {
  'DS-KEY': SL0030KeyScreen,
  'FIELD:WK-SEL-CUST': SL0030KeyScreen,
  'FIELD:WK-ORIG-INV': SL0030KeyScreen,
  'DS-DHEAD': SL0030DheadScreen,
  'FIELD:WK-STAFF-IN': SL0030DheadScreen,
  'FIELD:WK-TAXTYPE-IN': SL0030DheadScreen,
  'DS-DETAIL': SL0030DetailScreen,
  'FIELD:WK-D-PROD': SL0030DetailScreen,
  'FIELD:WK-D-WHSE': SL0030DetailScreen,
  'FIELD:WK-D-QTY': SL0030DetailScreen,
  'FIELD:WK-D-PRICE': SL0030DetailScreen,
  'DS-ESTAT': SL0030EstatScreen,
  'DS-TOTAL': SL0030TotalScreen,
  'DS-CONFIRM': SL0030ConfirmScreen,
  'FIELD:WK-CONFIRM': SL0030ConfirmScreen,
  'DS-BROWSE': SL0030BrowseScreen,
  'FIELD:WK-DUMMY': SL0030BrowseScreen,
  'DS-INV': SL0030InvScreen,
} as const;

const SL0040_OVERRIDES = {
  'DS-KEY': SL0040KeyScreen,
  'FIELD:WK-ORIG-INV': SL0040KeyScreen,
  'DS-PSTAT': SL0040PstatScreen,
  'DS-PICK': SL0040PickScreen,
  'FIELD:WK-PICK-LINE': SL0040PickScreen,
  'FIELD:WK-PICK-QTY': SL0040PickScreen,
  'DS-CONFIRM': SL0040ConfirmScreen,
  'FIELD:SC-CONF': SL0040ConfirmScreen,
  'DS-INFO': SL0040InfoScreen,
  'DS-LIST': SL0040ListScreen,
} as const;

const screenOverrides = computed<Record<string, Component> | undefined>(() =>
    programId.value === 'sl0040' ? SL0040_OVERRIDES :
    programId.value === 'sl0030' ? SL0030_OVERRIDES :
    programId.value === 'sl0020' ? SL0020_OVERRIDES :
    programId.value === 'sl0010' ? SL0010_OVERRIDES :
    programId.value === 'sh0010' ? SH0010_OVERRIDES :
    programId.value === 'rc0010' ? RC0010_OVERRIDES :
    programId.value === 'pu0040' ? PU0040_OVERRIDES :
    programId.value === 'pu0030' ? PU0030_OVERRIDES :
    programId.value === 'pu0020' ? PU0020_OVERRIDES :
    programId.value === 'pu0010' ? PU0010_OVERRIDES :
    programId.value === 'oe0040' ? OE0040_OVERRIDES :
    programId.value === 'oe0030' ? OE0030_OVERRIDES :
    programId.value === 'oe0020' ? OE0020_OVERRIDES :
    programId.value === 'oe0010' ? OE0010_OVERRIDES :
    programId.value === 'ms0120' ? MS0120_OVERRIDES :
    programId.value === 'ms0110' ? MS0110_OVERRIDES :
    programId.value === 'ms0100' ? MS0100_OVERRIDES :
    programId.value === 'ms0090' ? MS0090_OVERRIDES :
    programId.value === 'ms0080' ? MS0080_OVERRIDES :
    programId.value === 'ms0070' ? MS0070_OVERRIDES :
    programId.value === 'ms0060' ? MS0060_OVERRIDES :
    programId.value === 'ms0050' ? MS0050_OVERRIDES :
    programId.value === 'ms0040' ? MS0040_OVERRIDES :
    programId.value === 'ms0030' ? MS0030_OVERRIDES :
    programId.value === 'ms0020' ? MS0020_OVERRIDES :
    programId.value === 'ms0010' ? MS0010_OVERRIDES :
    programId.value === 'menu00' ? MENU00_OVERRIDES :
    programId.value === 'iv0050' ? IV0050_OVERRIDES :
    programId.value === 'iv0040' ? IV0040_OVERRIDES :
    programId.value === 'iv0030' ? IV0030_OVERRIDES :
    programId.value === 'iv0020' ? IV0020_OVERRIDES :
    programId.value === 'iv0010' ? IV0010_OVERRIDES :
    programId.value === 'ap0020' ? AP0020_OVERRIDES :
    programId.value === 'ar0020' ? AR0020_OVERRIDES :
    programId.value === 'ar0010' ? AR0010_OVERRIDES :
    programId.value === 'ap0010' ? AP0010_OVERRIDES :
    undefined);

// Tile-based program selector.
// modernize-screens.js --init also appends entries here so each modernized
// program shows up as a clickable tile. When the array is empty (single-program
// mode or before any --init has run), the UI falls back to the free-text input.
interface ProgramTile { id: string; label: string }
const currentPrograms = ref<ProgramTile[]>([{ id: 'ap0010', label: 'AP0010' }, { id: 'ar0010', label: 'AR0010' }, { id: 'ar0020', label: 'AR0020' }, { id: 'ap0020', label: 'AP0020' }, { id: 'iv0010', label: 'IV0010' }, { id: 'iv0020', label: 'IV0020' }, { id: 'iv0030', label: 'IV0030' }, { id: 'iv0040', label: 'IV0040' }, { id: 'iv0050', label: 'IV0050' }, { id: 'menu00', label: 'MENU00' }, { id: 'ms0010', label: 'MS0010' }, { id: 'ms0020', label: 'MS0020' }, { id: 'ms0030', label: 'MS0030' }, { id: 'ms0040', label: 'MS0040' }, { id: 'ms0050', label: 'MS0050' }, { id: 'ms0060', label: 'MS0060' }, { id: 'ms0070', label: 'MS0070' }, { id: 'ms0080', label: 'MS0080' }, { id: 'ms0090', label: 'MS0090' }, { id: 'ms0100', label: 'MS0100' }, { id: 'ms0110', label: 'MS0110' }, { id: 'ms0120', label: 'MS0120' }, { id: 'oe0010', label: 'OE0010' }, { id: 'oe0020', label: 'OE0020' }, { id: 'oe0030', label: 'OE0030' }, { id: 'oe0040', label: 'OE0040' }, { id: 'pu0010', label: 'PU0010' }, { id: 'pu0020', label: 'PU0020' }, { id: 'pu0030', label: 'PU0030' }, { id: 'pu0040', label: 'PU0040' }, { id: 'rc0010', label: 'RC0010' }, { id: 'sh0010', label: 'SH0010' }, { id: 'sl0010', label: 'SL0010' }, { id: 'sl0020', label: 'SL0020' }, { id: 'sl0030', label: 'SL0030' }, { id: 'sl0040', label: 'SL0040' }]);

const programId = ref('');
const connected = ref(false);
const selectedTileIdx = ref(0);
const tileRefs = ref<Array<HTMLDivElement | null>>([]);

function clampTileIdx(idx: number): number {
  const len = currentPrograms.value.length;
  if (len === 0) return -1;
  if (idx < 0) return 0;
  if (idx >= len) return len - 1;
  return idx;
}
function setTileRef(el: Element | null, idx: number) {
  tileRefs.value[idx] = el as HTMLDivElement | null;
}
function selectTile(idx: number, focus = false) {
  const i = clampTileIdx(idx);
  if (i < 0) return;
  selectedTileIdx.value = i;
  if (focus) nextTick(() => tileRefs.value[i]?.focus());
}
function activateTile(idx: number) {
  const i = clampTileIdx(idx);
  if (i < 0) return;
  selectedTileIdx.value = i;
  programId.value = currentPrograms.value[i].id;
  connected.value = true;
}
function onTileKeyDown(e: KeyboardEvent, idx: number) {
  if (e.key === 'ArrowDown' || e.key === 'ArrowRight') { e.preventDefault(); selectTile(idx + 1, true); return; }
  if (e.key === 'ArrowUp'   || e.key === 'ArrowLeft')  { e.preventDefault(); selectTile(idx - 1, true); return; }
  if (e.key === 'Home') { e.preventDefault(); selectTile(0, true); return; }
  if (e.key === 'End')  { e.preventDefault(); selectTile(currentPrograms.value.length - 1, true); return; }
  if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); activateTile(idx); }
}

function connect() {
  if (programId.value) connected.value = true;
}
function onKeyDown(e: KeyboardEvent) {
  if (e.key === 'Enter' && programId.value) connected.value = true;
}
function onDisconnect() {
  connected.value = false;
  if (currentPrograms.value.length > 0) selectTile(selectedTileIdx.value, true);
}
</script>

<template>
  <div v-if="!connected" class="scr-login-page">
    <div class="scr-login-card">
      <div class="scr-login-header">
        <h2>COBOL Screen Terminal</h2>
        <p v-if="currentPrograms.length === 0">Connect to a program by ID</p>
        <p v-else>Pick a program to launch</p>
      </div>
      <div class="scr-login-body">
        <!-- Tile UI: appears when modernize-screens.js --init has registered ≥1 program. -->
        <div
          v-if="currentPrograms.length > 0"
          class="scr-program-grid"
          role="listbox"
          aria-label="Available programs"
        >
          <div
            v-for="(item, idx) in currentPrograms"
            :key="item.id"
            :ref="(el) => setTileRef(el as Element | null, idx)"
            class="scr-program-tile"
            :class="{ 'is-active': selectedTileIdx === idx }"
            role="option"
            :aria-selected="selectedTileIdx === idx"
            :tabindex="selectedTileIdx === idx ? 0 : -1"
            @focus="selectTile(idx)"
            @mouseenter="selectTile(idx)"
            @keydown="onTileKeyDown($event, idx)"
            @click="activateTile(idx)"
          >
            <div class="scr-program-tile__id">{{ item.id.toUpperCase() }}</div>
            <!-- label falls back to the upper-case id when no program title is
                 configured — showing it then just duplicates the id line. -->
            <div v-if="item.label && item.label !== item.id.toUpperCase()" class="scr-program-tile__label">{{ item.label }}</div>
          </div>
        </div>
        <!-- Free-text fallback: empty array OR power user wanting to connect by ID directly. -->
        <div v-else class="scr-input-group">
          <label class="scr-label">Program ID</label>
          <input
            class="scr-input scr-numeric"
            :value="programId"
            @input="(e) => programId = (e.target as HTMLInputElement).value.toLowerCase()"
            @keydown="onKeyDown"
            placeholder="program id"
            autofocus
          />
        </div>
        <div v-if="currentPrograms.length === 0" class="scr-dialog-actions">
          <button
            class="btn btn-primary btn-sm"
            :disabled="!programId"
            @click="connect"
          >
            Connect <kbd>Enter</kbd>
          </button>
        </div>
      </div>
    </div>
  </div>
  <TerminalScreen
    v-else
    :program-id="programId"
    :screen-overrides="screenOverrides"
    @disconnect="onDisconnect"
  />
</template>
