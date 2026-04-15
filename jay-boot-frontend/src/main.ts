import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import './styles/user-theme.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { setupAuthExpiredHandler } from './config/authEventHandler'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

// 初始化认证过期事件监听
setupAuthExpiredHandler(router)

app.mount('#app')
