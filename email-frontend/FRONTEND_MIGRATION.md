# 前端迁移指南

## 步骤

1. 把 `D:\chenlq\Email_strip\vue\` 整个目录拷贝过来（不要拷贝 node_modules/ 和 dist/）
2. 用本目录下的以下文件覆盖对应位置：
   - `vue.config.js` → 去掉 OA V5PcPlugin，改 devServer
   - `src/api/request.js` → 加 JWT Token，改 AI agent URL
   - `src/api/api.js` → `/seeyon` 改为 `/api`
   - `src/api/auth.js` → 新增：登录 API
   - `src/router/index.js` → history 模式
   - `src/views/Login.vue` → 新增：登录页
   - 删除 `config/plugin/V5PcPlugin.js`
3. `npm install`，然后 `npm run serve` 启动开发模式

## JWT 认证流程

1. OA 点击邮件菜单 → OA 后端签发 JWT → 重定向到 `http://mail-host?token=xxx`
2. 前端存储 token 到 localStorage
3. 所有 API 请求带 `Authorization: Bearer <token>`
4. 401 响应 → 跳转登录页
