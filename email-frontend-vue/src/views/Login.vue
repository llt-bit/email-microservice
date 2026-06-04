<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 style="text-align:center;margin-bottom:24px;color:#303133;">内部邮件系统</h2>
      <el-form ref="form" :model="form" :rules="rules">
        <el-form-item prop="loginName">
          <el-input v-model="form.loginName" placeholder="用户名" prefix-icon="el-icon-user" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="el-icon-lock"
                    @keyup.enter.native="doLogin" />
        </el-form-item>
        <el-button type="primary" @click="doLogin" :loading="loading" style="width:100%">登录</el-button>
        <div v-if="error" style="color:#f56c6c;margin-top:10px;text-align:center;">{{ error }}</div>
        <div style="margin-top:16px;text-align:center;color:#999;font-size:13px;">
          从 OA 进入：自动登录，无需输入密码
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      form: { loginName: "", password: "" },
      loading: false,
      error: "",
      rules: {
        loginName: [{ required: true, message: "请输入用户名", trigger: "blur" }],
        password: [{ required: true, message: "请输入密码", trigger: "blur" }],
      },
    };
  },
  created() {
    // SSO Token 从 OA 跳转过来，直接从 URL 取
    const urlToken = this.$route.query.token || new URLSearchParams(window.location.search).get("token");
    if (urlToken) {
      localStorage.setItem("email_token", urlToken);
      this.$router.replace("/");
    }
  },
  methods: {
    async doLogin() {
      try { await this.$refs.form.validate(); } catch { return; }
      this.loading = true; this.error = "";
      try {
        const axios = (await import("axios")).default;
        const res = await axios.post("/api/auth/login", {
          loginName: this.form.loginName,
          password: this.form.password,
        });
        if (res.data && res.data.success) {
          localStorage.setItem("email_token", res.data.data.token);
          this.$router.replace("/");
        } else {
          this.error = (res.data && res.data.msg) || "登录失败";
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
.login-container { display: flex; justify-content: center; align-items: center; height: 100vh; background: #f0f2f5; }
.login-card { width: 400px; }
</style>
