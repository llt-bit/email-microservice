import Vue from "vue";
import Router from "vue-router";

Vue.use(Router);

const originalPush = Router.prototype.push;
Router.prototype.push = function push(location) {
  return originalPush.call(this, location).catch((err) => err);
};

export const routes = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/Login.vue"),
  },
  {
    path: "/",
    name: "收件箱",
    component: () => import("@/Layout/mainLayout/index.vue"),
    meta: { requireAuth: true },
    children: [
      { path: "", component: () => import("@/components/tippage/index.vue") },
      {
        path: "inboxDet/:id/:isall",
        component: () => import("@/components/view/inboxDet.vue"),
      },
      {
        path: "editor",
        component: () => import("@/components/view/editor.vue"),
      },
    ],
  },
  {
    path: "/userSetting",
    component: () => import("@/Layout/userSettingLayout/index.vue"),
    meta: { requireAuth: true },
    children: [
      {
        path: "mailPersonalSettings/customFolder",
        component: () => import("@/views/userSetting/mailPersonalSettings/customFolder/index.vue"),
      },
    ],
  },
];

const router = new Router({
  mode: "history",
  routes,
});

// 路由守卫：未登录跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem("email_token");
  if (to.path === "/login") {
    next();
  } else if (!token) {
    // 尝试从 URL 参数获取 SSO token
    const urlToken = new URLSearchParams(window.location.search).get("token");
    if (urlToken) {
      localStorage.setItem("email_token", urlToken);
      // 清除 URL 中的 token
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
