<template>
  <router-view></router-view>
</template>
<script>
// 导入 vuex modules layout
import layoutStore from "@/store/modules/layout.js";

export default {
  name: "APP",
  created() {
    // 监听 vuex中的 状态, 判断页面初始化的进入的页面
    
    if (layoutStore.state.isNotOperable) {
      // 个人设置
      this.$router.push({
        path: "/userSetting",
      });
    }
    // this.$store.commit('setnum')
    // else {
    //   // 首页
    //   this.$router.push({
    //     path: "/",
    //   });
    // }
  },
};
</script>