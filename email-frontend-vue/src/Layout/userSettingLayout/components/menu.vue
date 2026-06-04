<!--
 * @Author       : admin
 * @Date         : 2022-04-27 17:47:37
 * @LastEditors  : admin
 * @LastEditTime : 2022-04-28 15:31:49
 * @FilePath     : /emial-true(not-edit)/src/Layout/userSettingLayout/components/menu.vue
 * @Description  : 用户个人设置 > 菜单组件
-->
<template>
  <el-menu :default-active="$route.path" :default-openeds="[menuData[0].path]" :router="true">
    <MenuItem
      v-for="(groupItem, groupIndex) in menuData"
      :key="groupIndex"
      :data="groupItem"
    />
  </el-menu>
</template>
<script>
/**
 * 导入组件
 */
// 菜单项
import MenuItem from "./menuItem.vue";

// 导入路由信息
import { routes } from "@/router/index.js";

export default {
  name: "Menu",
  components: {
    MenuItem,
  },
  data() {
    return {
      menuData: [],
    };
  },
  beforeMount() {
    // 获取个人设置模块 路由配置信息
    this.menuData = routes.filter(
      (item) => item.path == "/userSetting"
    )[0].children;
  },
};
</script>
<style lang="less" scoped>
.el-menu {
  background-color: #f5f5f5;
}
</style>