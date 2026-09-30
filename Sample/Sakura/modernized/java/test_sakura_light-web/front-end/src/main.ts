import { createApp } from 'vue';
import App from './App.vue';
import './styles/tokens/base.css';   // lớp token DÙNG CHUNG 2 nhánh (R9.5)
import './styles/terminal-screen.css';
import './styles/design-tokens.css';
import './styles/data-table.css';   // style của DataTable trong UI kit dùng chung (R9.7)

createApp(App).mount('#root');
