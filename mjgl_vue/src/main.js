import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

const app = createApp(App)

// 长页面返回顶部：绑定到可滚动容器上，滚动时派发事件供 App 显示/隐藏返回顶部按钮
app.directive('back-to-top', {
  mounted(el) {
    el._onScroll = () => {
      el.dispatchEvent(
        new CustomEvent('back-to-top-scroll', {
          detail: { scrollTop: el.scrollTop },
          bubbles: true,
        })
      )
    }
    el.addEventListener('scroll', el._onScroll, { passive: true })
  },
  unmounted(el) {
    el.removeEventListener('scroll', el._onScroll)
  },
})

app.use(createPinia())
app.use(router)

app.mount('#app')
