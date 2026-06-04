import "core-js/stable";
import "regenerator-runtime/runtime";

import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import "@/assets/CSS/icon.css"
import VueQuillEditor from 'vue-quill-editor'
import 'quill/dist/quill.core.css'
import 'quill/dist/quill.snow.css'
import 'quill/dist/quill.bubble.css'
import 'quill/dist/quill.core.css'
import 'quill/dist/quill.snow.css'
import 'quill/dist/quill.bubble.css'
import $ from 'jquery'
import axios from 'axios';
import 'viewerjs/dist/viewer.css'
import Viewer from 'v-viewer'
import fa from "element-ui/src/locale/lang/fa";

Vue.use(Viewer)
Vue.prototype.$axios = axios
    // 这里是插件的默认设置
Viewer.setDefaults({
    zIndex: 9999,
    inline: false,
    button: true,
    navbar: false,
    title: false,
    toolbar: {
        zoomIn: {
            show: 4,
            size: 'large',
        },
        zoomOut: {
            show: 4,
            size: 'large',
        },
        oneToOne: {
            show: 4,
            size: 'large',
        },
        reset: {
            show: 4,
            size: 'large',
        },
        prev: 0,
        play: {
            show: 4,
            size: 'large',
        },
        next: 0,
        rotateLeft: {
            show: 4,
            size: 'large',
        },
        rotateRight: {
            show: 4,
            size: 'large',
        },
        flipHorizontal: {
            show: 4,
            size: 'large',
        },
        flipVertical: {
            show: 4,
            size: 'large',
        },
    },
    tooltip: false,
    movable: true,
    zoomable: true,
    rotatable: true,
    scalable: false,
    transition: true,
    fullscreen: false,
    keyboard: false
})

//获取第三方待办邮件数据
export const eventBus = new Vue()

Vue.config.productionTip = false
Vue.prototype.jQuery = $;

Vue.use(VueQuillEditor);


Vue.config.productionTip = false
Vue.use(ElementUI);


new Vue({
    router,
    store,
    render: h => h(App)
}).$mount('#app')