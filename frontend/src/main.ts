import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'


const app = createApp(App)
const pinia = createPinia()

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
    app.component(key, component)
}

import axios from 'axios'

axios.defaults.withCredentials = true
axios.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('userRole')
            localStorage.removeItem('username')
            router.push('/login')
        }
        return Promise.reject(error)
    }
)

app.use(pinia)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
