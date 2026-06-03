<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>内部邮件系统</h2>
      <el-form ref="form" :model="form" :rules="rules">
        <el-form-item prop="loginName">
          <el-input v-model="form.loginName" placeholder="用户名" prefix-icon="el-icon-user" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="el-icon-lock"
                    @keyup.enter.native="login" />
        </el-form-item>
        <el-button type="primary" @click="login" :loading="loading" style="width:100%">登 录</el-button>
        <div v-if="error" style="color:red;margin-top:10px;text-align:center">{{ error }}</div>
        <div style="margin-top:16px;text-align:center;color:#999;font-size:13px">
          或通过 OA 系统点击邮件菜单自动登录
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
import api from "@/api/api";
export default {
  data() {
    return { form: { loginName: "", password: "" }, loading: false, error: "",
      rules: { loginName: [{ required: true, message: "请输入用户名" }],
               password: [{ required: true, message: "请输入密码" }] } };
  },
  created() {
    // SSO 跳转：从 URL 取 token
    const token = this.$route.query.token || new URLSearchParams(window.location.search).get("token");
    if (token) {
      localStorage.setItem("email_token", token);
      this.$router.replace("/");
    }
  },
  methods: {
    async login() {
      try {
        await this.$refs.form.validate();
      } catch { return; }
      this.loading = true; this.error = "";
      try {
        const res = await api.login(this.form);
        if (res.code === 0) {
          localStorage.setItem("email_token", res.data.token);
          this.$router.replace("/");
        } else {
          this.error = res.msg || "登录失败";
        }
      } catch (e) {
        this.error = "登录失败，请检查用户名密码或网络连接";
      }
      this.loading = false;
    },
  },
};
</script>

<style scoped>
.login-container { display:flex; justify-content:center; align-items:center; height:100vh; background:#f0f2f5; }
.login-card { width:400px; }
.login-card h2 { text-align:center; margin-bottom:24px; color:#303133; }
</style>
