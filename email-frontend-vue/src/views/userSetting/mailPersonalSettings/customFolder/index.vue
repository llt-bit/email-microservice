<!--
 * @LastEditTime : 2022-04-30 19:57:00
 * @FilePath     : /emial-true(not-edit)/src/views/userSetting/mailPersonalSettings/customFolder/index.vue
 * @Description  : 个人设置 > 个人邮箱设置 > 自定义文件夹
-->
<template>
    <el-container class="newFolder-wrap">
        <!-- <el-header>
            <el-button round :disabled="isUseFolderOperationBtn" @click="handleCreate">
                新建文件夹
            </el-button>
            <el-button round :disabled="isUseFolderOperationBtn" @click="handleDelete">
                删除文件夹
            </el-button>
            <el-button round :disabled="isUseFolderOperationBtn" @click="handleEdit">
                修改文件夹
            </el-button>
            <el-button round :disabled="isUseFolderOperationBtn" @click="handleSubmit">
                保存
            </el-button>
            <el-button round :disabled="isUseFolderOperationBtn" @click="handleCancel">
                取消
            </el-button>
        </el-header> -->
        <el-main>
            <MainLayout />
        </el-main>
    </el-container>
</template>
<script>
// 导入主要布局
import MainLayout from "@/Layout/mainLayout/index.vue";
// 导入api
import api from "@/api/api";

// 导入 eventbus
import EventBus from "@/utils/eventBus.js";

export default {
    name: "NewFolder",
    components: {
        MainLayout,
    },
    data() {
        return {
            // 当前点击的一级菜单 信息
            oneLevelMenuInfo: {},
            // 判断新建文件夹等按钮是否 不可用
            isUseFolderOperationBtn: true,
            // 当前选中的二级菜单信息
            selectSubMenuInfo: {},

            // 操作的 自定义文件夹信息
            operationCustomFloder: [
                // {
                //   ...
                //   自定义的 操作标识
                //   customOperationType: 'add' | 'update' | 'delete'
                // }
            ],
        };
    },
    mounted() {
        // 添加自定义事件, 获取当前点击的 一级级菜单信息(个人设置_邮件个人设置_新建文件夹)
        EventBus.$on(
            "userSetting_mailPersonalSettings_customFolder_getOneLevelMenuInfo",
            (oneLevelMenuInfo) => {
                // 更新本地菜单信息
                this.oneLevelMenuInfo = oneLevelMenuInfo;
                // 新建文件夹 按钮 操作状态
                this.isUseFolderOperationBtn = false;
            }
        );

        // 添加自定义事件, 获取当前点击的 二级菜单信息(个人设置_邮件个人设置_新建文件夹)
        EventBus.$on(
            "userSetting_mailPersonalSettings_customFolder_getSubMenuInfo",
            (subMenuInfo) => {
                // 记录 当前点击 二级菜单信息
                this.selectSubMenuInfo = subMenuInfo;
            }
        );
    },
    watch: {
        // // 监测
        // operationCustomFloder: {
        //     handler(newVal) {
        //         // 从本地收集的关于文件操作的记录, 根据自定义标识 剔除已删除的 文件夹
        //         // 剩余数据为 新增、修改 后的数据
        //         const needCustomFloder = newVal.filter(
        //             (item) => item.customOperationType != "delete"
        //         );
        //         // 根据结果 生成新的 菜单结构
        //         this.$store.dispatch("generateMenuInfo", needCustomFloder);
        //     },
        //     deep: true,
        // },
    },
    computed: {
        // 获取 vuex 中的 菜单信息
        storeMenus() {
            return this.$store.state.menus;
        },
    },
    methods: {
        /**
         * 点击 新建文件夹
         */
        // handleCreate() {
        //     this.$prompt("请输入新建文件夹名", {
        //         confirmButtonClass: "确定",
        //         //显示输入框
        //         showInput: true,
        //         inputValidator: function (value) {
        //             if (!value) {
        //                 return "请输入文件夹名称";
        //             }
        //             // else if (2 <= value.length && value.length <= 10) {
        //             //   return true;
        //             // } 
        //             // else {
        //             //   return "请输入2-8个字符的名称";
        //             // }
        //         },
        //     }).then(({ value }) => {
        //         if (this.operationCustomFloder.indexOf(value) > 0) {
        //             alert(`已存在文件夹${value}`);
        //             return;
        //         }
        //         // 收集 创建的文件夹信息
        //         this.operationCustomFloder.push({
        //             fileName: value,
        //             // 一级菜单 标识
        //             type: this.oneLevelMenuInfo.type,
        //             // 添加 自定义的 index
        //             index:
        //                 this.oneLevelMenuInfo.index +
        //                 "-" +
        //                 (Array.isArray(this.oneLevelMenuInfo.Menus) &&
        //                     this.oneLevelMenuInfo.Menus.length + 1) || "1",
        //             // 本地新增的数据标识
        //             isLocallyAddedData: true,
        //             // 操作标识
        //             customOperationType: "add",
        //         });
        //         console.log(
        //             "this.operationCustomFloder :>> ",
        //             this.operationCustomFloder
        //         );
        //     });
        // },

        // /**
        //  * 点击 删除文件夹
        //  */
        // handleDelete() {
        //     if (!this.selectSubMenuInfo.fileName) {
        //         this.$message.warning("请选择需要删除的 文件夹");
        //         return;
        //     }
        //     if (
        //         this.selectSubMenuInfo.fileName == "所有邮件" ||
        //         this.selectSubMenuInfo.fileName == "未读" ||
        //         this.selectSubMenuInfo.fileName == "已办理" ||
        //         this.selectSubMenuInfo.fileName == "未办理"
        //     ) {
        //         this.$message.warning("不允许删除当前文件夹");
        //         return;
        //     }
        //     this.$confirm("是否删除当前文件夹,删除后不可恢复?", {
        //         confirmButtonText: "确定",
        //         cancelButtonText: "取消",
        //         type: "warning",
        //     }).then(() => {
        //         console.log('this.storeMenus', this.storeMenus);
        //         // 遍历菜单信息
        //         for (const item of this.storeMenus) {
        //             // 遍历 二级菜单
        //             const { folderId, type, path, index } = this.selectSubMenuInfo || {};
        //             // 一级菜单匹配
        //             if (item.type == type) {
        //                 const needMenuIndex = item.Menus.findIndex(
        //                     (i) =>
        //                         // 后台数据

        //                         i.folderId == folderId ||
        //                         (i.type == type && i.path == path) ||
        //                         // 本地新增 自定义标识数据
        //                         // i.index == index
        //                         i.path == path
        //                 );

        //                 // 二级菜单中存在 当前点击的菜单信息
        //                 console.log('当前点击的菜单信息', needMenuIndex)
        //                 if (needMenuIndex > -1) {
        //                     // 删除当前记录
        //                     item.Menus.splice(needMenuIndex, 1);

        //                     // 通过 folderId 进行判断, 当前记录是否是 后端数据, 是: 标识 删除, 否: 无操作
        //                     if (this.selectSubMenuInfo.folderId) {
        //                         // 收集需要删除的 自定义文件夹信息
        //                         this.operationCustomFloder.push({
        //                             ...this.selectSubMenuInfo,
        //                             customOperationType: "delete",
        //                         });
        //                     } else {
        //                         // 删除本地记录
        //                         this.operationCustomFloder.splice(
        //                             this.operationCustomFloder.indexOf(this.selectSubMenuInfo),
        //                             1
        //                         );
        //                     }

        //                     // 重置当前选中的二级菜单信息
        //                     this.selectSubMenuInfo = {};
        //                 }
        //             }
        //         }
        //     });
        // },

        // /**
        //  * 点击 修改文件夹
        //  */
        // handleEdit() {
        //     if (!this.selectSubMenuInfo.fileName) {
        //         this.$message.warning("请选择需要修改的 文件夹");
        //         return;
        //     }
        //     if (
        //         this.selectSubMenuInfo.fileName == "所有邮件" ||
        //         this.selectSubMenuInfo.fileName == "未读" ||
        //         this.selectSubMenuInfo.fileName == "已办理" ||
        //         this.selectSubMenuInfo.fileName == "未办理"
        //     ) {
        //         console.log('this.selectSubMenuInfo.fileName', this.selectSubMenuInfo.fileName);
        //         this.$message.warning("不允许修改当前文件夹");
        //         return;
        //     }
        //     this.$prompt("请输入变更后的文件夹名称", {
        //         confirmButtonClass: "确定",
        //         showInput: true,
        //         inputValue: this.selectSubMenuInfo.fileName,
        //         closeOnClickModal: false,
        //         inputValidator: function (value) {
        //             if (!value) {
        //                 return "请输入文件夹名称";
        //             }
        //             // else if (2 <= value.length && value.length <= 6) {
        //             //   return true;
        //             // } else {
        //             //   return "请输入2-6个字符的名称";
        //             // }
        //         },
        //     }).then(({ value }) => {
        //         // 更新 收集的 新建文件夹信息
        //         const index = this.operationCustomFloder.findIndex(
        //             (item) => item == this.selectSubMenuInfo
        //         );

        //         // 生成新的 文件夹信息
        //         const newFolderInfo = {
        //             ...this.selectSubMenuInfo,
        //             fileName: value,
        //             // 通过 folderId 进行判断, 当前记录是否是 后端数据, 是: 标识更新, 否: 标识创建
        //             customOperationType: this.selectSubMenuInfo.folderId ? "edit" : "add",
        //         };

        //         // 验证是否已存在 当前选中的 文件夹信息
        //         if (index > -1) {
        //             // 更新 信息
        //             this.operationCustomFloder.splice(index, 1, newFolderInfo);
        //         } else {
        //             // 存储当前记录
        //             this.operationCustomFloder.push(newFolderInfo);
        //         }

        //         // 重置当前选中的二级菜单信息
        //         this.selectSubMenuInfo = {};
        //     });
        // },

        // /**
        //  * 点击 保存(提交新建文件夹到数据库)
        //  */
        // handleSubmit() {
        //     this.$confirm("是否保存?", {
        //         confirmButtonText: "确定",
        //         cancelButtonText: "取消",
        //     }).then(() => {
        //         this.submitAction(this.operationCustomFloder);
        //     });
        // },

        // /**
        //  * 点击 取消(刷新页面)
        //  */
        // handleCancel() {
        //     this.$confirm("请确认是否取消", {
        //         confirmButtonText: "确定",
        //         cancelButtonText: "取消",
        //         type: "warning",
        //     }).then(() => {
        //         this.$store.dispatch("toggleLayoutType");
        //     });
        // },

        // /**
        //  * 保存操作
        //  * @param {Array} customFloder: 收集的 自定义文件夹操作数据
        //  */
        // async submitAction(customFloder) {
        //     /** 新建文件夹
        //      ************************************************/
        //     customFloder
        //         .filter((item) => item.customOperationType == "add")
        //         .map((item) => {
        //             return {
        //                 type: item.type,
        //                 floderName: item.fileName,
        //             };
        //         })
        //         .forEach((item) => {
        //             // 请求
        //             api.submitNewFolder(item).then((res) => {
        //                 if (res.code == 200) {
        //                     this.$store.dispatch("toggleLayoutType");
        //                     // this.$router.push({ path: "/" });
        //                 } else {
        //                     this.$message.error(res.message || "保存失败");
        //                 }
        //             });
        //         });

        //     /**  更新文件夹
        //      **********************************************/
        //     customFloder
        //         .filter((item) => item.customOperationType == "edit")
        //         .map((item) => {
        //             return {
        //                 id: item.folderId,
        //                 floderName: item.fileName,
        //             };
        //         })
        //         .forEach((item) => {
        //             // 请求
        //             api.changeFolderName(item).then((res) => {
        //                 if (res.code == 200) {
        //                     // this.$store.dispatch("getMailInfo");
        //                 } else {
        //                     this.$message.error(res.message || "更新失败");
        //                 }
        //             });
        //         });

        //     /** 删除文件夹
        //      ************************************************/
        //     customFloder
        //         .filter((item) => item.customOperationType == "delete")
        //         .map((item) => {
        //             return {
        //                 id: item.folderId,
        //                 folderPath: item.path,
        //             };
        //         })
        //         .forEach((item) => {
        //             // 请求
        //             api.delFolder(item).then((res) => {
        //                 if (res.code == 200) {
        //                     // this.$store.dispatch("getMailInfo");
        //                 } else {
        //                     this.$message.error(res.message || "删除失败");
        //                 }
        //             });
        //         });

        //     this.$nextTick(() => {
        //         this.$store.dispatch("toggleLayoutType");
        //         this.$router.push({ path: "/" });
        //     });
        // },
    },
};
</script>
<style lang="less" scope>
.newFolder-wrap {
    &>.el-header {
        justify-content: flex-end;
        box-sizing: border-box;
        padding-right: 100px !important;
        border-bottom: 1px solid #e4e8eb;
    }
}
</style>