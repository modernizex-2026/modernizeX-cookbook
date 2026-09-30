import { createApp } from 'vue'
import App from './App.vue'
import './styles/tokens/base.css'   // lớp token DÙNG CHUNG 2 nhánh (R9.5) — kit dùng --scr-*
import './styles/data-table.css'   // style của DataTable trong UI kit dùng chung
import './styles/tokens.css'   // design tokens (:root) — override để đổi theme/dark-mode
import './styles.css'

createApp(App).mount('#app')
