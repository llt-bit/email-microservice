<!--
 * @LastEditTime : 2022-04-28 15:30:30
 * @FilePath     : /emial-true(not-edit)/src/Layout/userSettingLayout/index.vue
 * @Description  : 布局文件(个人设置)
-->
<template>
    <el-container class="user-setting-layout">
        <el-aside width="200px">
            <Menu />
        </el-aside>
        <el-container class="layout-body-wrap">
            <div class="leave-page-wrap">
                <LeavePage />
            </div>
            <router-view></router-view>
        </el-container>
    </el-container>
</template>

<script>
/**
 * 导入组件
 */
// 导入菜单
import Menu from "./components/menu.vue";
// 离开界面
import LeavePage from "./components/leavePage.vue";
import layoutStore from "@/store/modules/layout.js";

export default {
    name: "UserSettingLayout",
    components: {
        Menu,
        LeavePage,
    },
    created() {
        // 判断，如果进入用户自定义设置页面的时候，isNotOperable状态为false(刷新导致的状态丢失)，手动去设置一次isNotOperable的状态值
        if (!layoutStore.state.isNotOperable) {
            layoutStore.mutations.SET_TYPE(layoutStore.state)
        }
    }
};
</script>

<style lang="less" scope>
.user-setting-layout {
    width: 100%;
    height: 100%;

    &>.el-aside {
        background-color: #f2f2f2;
        border-right: 1px solid #e4e8eb;
        box-sizing: border-box;
        padding-top: 80px;
    }

    &>.layout-body-wrap {
        flex-direction: column;

        .leave-page-wrap {
            position: fixed;
            top: 20px;
            right: 20px;
        }
    }
}
</style>

