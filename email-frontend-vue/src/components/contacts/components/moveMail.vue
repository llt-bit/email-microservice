<!--
 * @LastEditTime : 2022-06-17 17:21:40
 * @FilePath     : /pc_mail/src/components/contacts/components/moveMail.vue
 * @Description  : 邮件移动
-->
<template>
  <div class="move-mail-wrap">
    <el-dialog
      width="30%"
      title="选择移动文件夹"
      :visible="visible"
      :before-close="close"
      :close-on-click-modal="false"
    >
      <el-tree
        :data="menus"
        :highlight-current="true"
        :props="{
          children: 'Menus',
          label: 'name',
        }"
        @node-click="handleNodeClick"
        :default-expand-all="true"
      >
        <span class="custom-tree-node" slot-scope="{ data }">
          <span>{{ data.name || data.fileName }}</span>
        </span>
      </el-tree>
      <div slot="footer" class="dialog-footer">
        <el-button @click="close">取 消</el-button>
        <el-button type="primary" @click="handleConfirm"> 确 定 </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
/**
 * 导入 api
 */
import api from "@/api/api";
export default {
  data() {
    return {
      // 弹窗显示状态
      visible: false,
      // 菜单信息
      menus: [],
      // 需要移动的邮件信息
      needMoveMails: [],
      // 邮件移动地址
      moveMailPath: null,
      //邮件移动类型
      moveMailType: null,
      menu: [],
      //   lyy curMailFolder
    };
  },
  watch: {
    // 监测 vuex 中的 菜单信息变更
    "$store.state.menus": {
      handler(newVal) {
        // console.log(newVal, 1111, this.$store.state.currentMenuType);
        // 更新列表信息
        let arr = newVal.filter(
          (el) => el.type == this.$store.state.currentMenuType
        );
        if (this.$store.state.currentMenuType == "inBox") {
          this.menus = JSON.parse(JSON.stringify(arr));
          let curMenu = this.menus[0].Menus;
          let newMenu = curMenu.filter(
            (item) => !["未读", "未办理", "已办理"].includes(item.fileName)
          );
          let item = { ...this.menus[0], Menus: newMenu };
          // console.log(1111111111111, this.menu[curIndex], item);
          this.menus.splice(0, 1, item);
        } else if (this.$store.state.currentMenuType == "sent") {
          this.menus = JSON.parse(JSON.stringify(arr));
        }
      },
      deep: true,
      immediate: true,
    },
    // lyy 监听当前点击的邮件菜单
    "$store.state.currentMenuType": {
      handler(newVal) {
        // console.log("点击菜单信息改变", newVal);
        // 更新列表信息
        let arr = this.$store.state.menus.filter((el) => el.type == newVal);
        if (newVal == "inBox") {
          this.menus = JSON.parse(JSON.stringify(arr));
          let curMenu = this.menus[0].Menus;
          let newMenu = curMenu.filter(
            (item) => !["未读", "未办理", "已办理"].includes(item.fileName)
          );
          let item = { ...this.menus[0], Menus: newMenu };
          // console.log(1111111111111, this.menu[curIndex], item);
          this.menus.splice(0, 1, item);
        } else if (newVal == "sent") {
          this.menus = JSON.parse(JSON.stringify(arr));
        }
      },
      deep: true,
      immediate: true,
    },
    // 监听弹窗状态
    visible(newVal) {
      // 弹窗关闭
      if (!newVal) {
        // 重置数据
        this.menuInfo = [];
        this.moveMailPath = null;
      }
    },
  },
  methods: {
    /**
     * 弹窗 显示
     * @param {array} needMoveMails: 需要移动的邮件信息
     */
    show(needMoveMails) {
      this.visible = true;
      // 存储邮件信息
      this.needMoveMails = needMoveMails;
    },

    /**
     * 弹窗 关闭
     */
    close() {
      this.visible = false;
    },

    /**
     * node-tree 点击
     * @param {object} nodeInfo: 节点信息
     */
    handleNodeClick(nodeInfo) {
      //   console.log("nodeInfo :>> ", nodeInfo);
      // 更新邮件移动地址 n
      this.moveMailPath = nodeInfo.path;
      this.moveMailType = nodeInfo.type;
    },

    /**
     * 点击 确定
     */
    handleConfirm() {
      // 路径验证
      if (!this.moveMailPath) return this.$message.warning("请选择文件夹");
      //   console.log(
      //     "this.moveMailPath.slice(0) :>> ",
      //     this.moveMailPath.slice(0)
      //   );
      //   console.log(
      //     "this.this.moveMailType.slice(0) :>> ",
      //     this.moveMailType.slice(0)
      //   );
      const folderPath = this.moveMailPath.slice(0);
      //   console.log(
      //     "this.$store.state.currentMenuType",
      //     this.$store.state.currentMenuType
      //   );
      //   console.log("this.needMoveMails", this.needMoveMails);
      //   lyy this.$store.state.currentMenuType
      if (this.$store.state.currentMenuType != this.moveMailType.slice(0)) {
        this.$message.error("请不要跨级移动");
      } else {
        // 提取请求参数
        const params = this.needMoveMails.map((item) => {
          return {
            affairId: item.idStr,
            folderPath: folderPath,
            //lyy 当前邮件的type
            type: this.$store.state.currentMenuType,
          };
        });
        // console.log("params", params);
        // 请求
        api
          .moveFolder({ data: params })
          .then((res) => {
            const { code, loseData, message } = res;

            if (code == 200) {
              this.$message.success("移动成功");
              
              this.$emit('onMoveSuccess')
              this.close();
            } else {
              if (Array.isArray(loseData) && loseData.length) {
                // 拼接 错误提示
                let messageText = "";
                loseData.forEach((item) => {
                  messageText += item.emailName + item.errorReason + "\n";
                });
                this.$message.error(messageText);
              }
            }
            this.$store.dispatch("getMailInfo");
          })
          .catch(() => {
            this.$message.error("移动失败");
          });
      }
    },
  },
};
</script>

<style lang="less" scoped>
.move-mail-wrap {
  /deep/ .el-dialog__wrapper {
    .el-dialog {
      .el-dialog__header {
        border-bottom: 1px solid #ccc;
      }

      .el-dialog__body {
        max-height: 300px;
        overflow-y: scroll;

        ::-webkit-scrollbar {
          width: 5px;
          height: 5px;
          background-color: #f5f5f5;
        }
      }
    }
  }
}
</style>
