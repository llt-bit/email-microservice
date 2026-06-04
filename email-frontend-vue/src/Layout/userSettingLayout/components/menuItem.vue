<!--
 * @Author       : admin
 * @Date         : 2022-04-27 18:17:20
 * @LastEditors  : admin
 * @LastEditTime : 2022-04-28 15:33:48
 * @FilePath     : /emial-true(not-edit)/src/Layout/userSettingLayout/components/menuItem.vue
 * @Description  : 用户个人设置 > 菜单 > 菜单项
-->
<template>
  <div class="menu-item">
    <el-submenu v-if="data.meta.isGroup" :index="data.path">
      <template slot="title">
        <span>{{ data.meta.title }}</span>
      </template>
      <el-menu-item
        v-for="(item, index) in data.children"
        :key="index"
        :index="item.path"
      >
        {{ item.meta.title }}
      </el-menu-item>
    </el-submenu>
    <el-menu-item v-else :index="data.path" class="menu-title">
      {{ data.meta.title }}
    </el-menu-item>
  </div>
</template>
<script>
export default {
  name: "MenuItem",
  props: {
    // 菜单信息
    data: {
      type: Object,
      require: true,
    },
  },
};
</script>
<style lang="less" scoped>
.menu-item {
  /deep/ .el-submenu {
    .el-submenu__title,
    .el-menu {
      background-color: transparent;
      user-select: none;
    }
    .el-menu {
      .is-active {
        background-color: #f9eceb !important;
        border-left: 4px solid #c8423c;
        color: #c8423c;

      }
    }
  }
}
</style>