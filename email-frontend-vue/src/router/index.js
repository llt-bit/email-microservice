import Vue from "vue";
import Router from "vue-router";
import emailLnbox from "@/components/view/emailLnbox";
import inboxDet from "@/components/view/inboxDet";
import editor from "@/components/view/editor";
import tippage from "@/components/tippage/index";
import { MessageBox } from "element-ui";
import { Message } from "element-ui";

// 导入主要布局
import MainLayout from "@/Layout/mainLayout/index.vue";
// 导入 空白布局
import EmptyLayout from "@/Layout/emptyLayout/index.vue";
// 个人设置界面
import UserSettingLayout from "@/Layout/userSettingLayout/index.vue";

Vue.use(Router);

//获取原型对象上的push函数
const originalPush = Router.prototype.push;
//修改原型对象中的push方法
Router.prototype.push = function push(location) {
    return originalPush.call(this, location).catch((err) => err);
};

export const routes = [{
        path: "/login",
        name: "登录",
        component: () => import("@/views/Login.vue"),
    },{
        path: "/",
        name: "收件箱",
        component: MainLayout,
        children: [{
            path: "",
            component: tippage,
        }, ],
    },
    {
        path: "/inboxDet/:id/:isall",
        name: "收件箱",
        component: MainLayout,
        children: [{
            path: "",
            component: inboxDet,
        }, ],
    },
    {
        path: "/editor",
        name: "发件",
        component: MainLayout,
        children: [{
            path: "",
            component: editor,
        }, ],
    },
    {
        path: "/userSetting",
        name: "userSetting",
        redirect: "/userSetting/mailPersonalSettings",
        component: UserSettingLayout,
        meta: {
            title: "个人设置中心",
        },
        children: [{
            name: "MailPersonalSettings",
            component: EmptyLayout,
            path: "/userSetting/mailPersonalSettings",
            redirect: "/userSetting/mailPersonalSettings/customFolder",
            meta: {
                title: "邮件个人设置",
                isGroup: true,
            },
            children: [{
                name: "CustomFolder",
                path: "/userSetting/mailPersonalSettings/customFolder",
                component: () =>
                    import (
                        "@/views/userSetting/mailPersonalSettings/customFolder/index.vue"
                    ),
                meta: {
                    title: "自定义文件夹",
                },
            }, ],
        }, ],
    },
];

const router = new Router({ routes });

// 路由守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
    if (to.path === "/login") {
        next();
        return;
    }
    const token = localStorage.getItem("email_token");
    if (!token) {
        const urlToken = new URLSearchParams(window.location.search).get("token");
        if (urlToken) {
            localStorage.setItem("email_token", urlToken);
            window.history.replaceState({}, "", window.location.pathname);
            next();
        } else {
            next("/login");
        }
    } else {
        next();
    }
});

export default router;
