<template>
  <ul v-show="show" class="contextMenu" @click="fileOperate" :style="style">
    <li
      v-for="item in menu"
      :key="item.id"
      data-symbol="contextMenu"
      :data-index="item.id"
    >
      {{ item.name }}
    </li>
  </ul>
</template>

<script>
import { createNamespacedHelpers } from "vuex";
const { mapState, mapMutations } = createNamespacedHelpers("contextMenu");
// 导入api
import api from "@/api/api";
export default {
  // props: {
  //     msg: {
  //         type: Object,
  //         default: () => ({})
  //     }
  // },
  data() {
    return {
      menus: [
        {
          name: "新建文件夹",
          type: 1,
          id: 0,
        },
        {
          name: "修改文件夹",
          type: 2,
          id: 1,
        },
        {
          name: "删除文件夹",
          type: 2,
          id: 2,
        },
        
      ],
      style: {
        top: 0,
        left: 0,
      },
      // 当前的操作信息
      operationCustomFloder: {},
    };
  },
  computed: {
    ...mapState(["msg", "show"]),
    // 根据类型筛选出要展示的menu
    menu() {
      const menus = this.menus.filter((item) => item.type == this.msg.symbol)
      if(this.msg.type === 'inBox' && this.msg.symbol === 2) {
        return [...menus,{
          name: "设置过滤规则",
          type: 2,
          id: 3,
        },
        // {
        //   name: "将规则应用到历史邮件",
        //   type: 2,
        //   id: 4,
        // }
        ];
      }else {
        return menus;
      }
      
    },
  },
  watch: {
    // 监听内容改变
    msg: {
      handler(val) {
        // 修改样式
        this.style = {
          top: val.mouse_y + 10 + "px",
          left: val.mouse_x + 10 + "px",
        };
      },
      immediate: true,
      deep: true,
    },
  },
  mounted() {
    this.style = { top: this.msg.mouse_y - 100 + "px" };
    // 监听点击事件
    document.addEventListener( "click", (e) => {
        const mySymbol = e.target.getAttribute("data-symbol");
        // console.log("mySymbol", mySymbol);
        if (mySymbol != "contextMenu") {
          this.setShow(false);
        }
      },
      true
    );
    // 监听鼠标滚轮
    document.addEventListener( "scroll", () => {
        // console.log(1111);
        // 隐藏右键弹框
        this.setShow(false);
      },
      true
    );
    // document.addEventListener("contextmenu", (e) => {
    //   e.preventDefault();
    // });
  },
  methods: {
    ...mapMutations(["setShow", "setRoleDialogVisible"]),
    // 操作文件
    fileOperate(e) {
      const type = e.target.getAttribute("data-index");
      //   console.log(type, typeof type);
      switch (type) {
        // 新建文件夹
        case "0":
          this.handleCreate();
          break;
        // 修改文件夹
        case "1":
          this.handleEdit();
          break;
        // 删除文件夹
        case "2":
          this.handleDelete();
          break;
        // 新建过滤规则
        case "3":
          this.handleRule();
          break;
        // 将过滤规则应用到历史邮件
        case "4":
          this.handleHistoryRule();
          break;
      }
    },
    /**
     * 点击 新建文件夹
     */
    handleCreate() {
      this.setShow(false);
      this.$prompt("请输入新建文件夹名", {
        confirmButtonClass: "确定",
        //显示输入框
        showInput: true,
        inputValidator: function (value) {
          if (!value) {
            return "请输入文件夹名称";
          }
          // else if (2 <= value.length && value.length <= 10) {
          //   return true;
          // }
          // else {
          //   return "请输入2-8个字符的名称";
          // }
        },
      }).then(({ value }) => {
        // console.log(value);
        const find = this.msg.Menus.find((item) => item.fileName == value);
        if (find) {
          this.$message.warning(`已存在文件夹${value}`);
          return;
        } else {
          // 请求
          api
            .submitNewFolder({
              type: this.msg.type,
              floderName: value,
            })
            .then((res) => {
              if (res.code == 200) {
                // this.$store.dispatch("toggleLayoutType");
                // this.$router.push({ path: "/" });
                this.$store.dispatch("getMailInfo");
              } else {
                this.$message.error(res.message || "保存失败");
              }
            });
        }

        // console.log(
        //     "this.operationCustomFloder :>> ",
        //     this.operationCustomFloder
        // );
      });
    },
    /**
     * 点击 删除文件夹
     */
    handleDelete() {
      this.setShow(false);
      if (
        ["所有邮件", "未读", "已办理", "未办理"].includes(this.msg.fileName)
      ) {
        this.$message.warning("不允许删除当前文件夹");
        return;
      }
      this.$confirm("是否删除当前文件夹,删除后不可恢复?", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        // 请求
        api
          .delFolder({ id: this.msg.folderId, folderPath: this.msg.path, type: this.msg.type })
          .then((res) => {
            // console.log("删除邮件", res);
            if (res.code == 200) {
              this.$store.dispatch("getMailInfo");
              this.$message.success("删除成功");
            } else {
              //   this.$message.error(res.message || "删除失败");
              // lyy
              this.$message.error("删除自定义文件夹前，请确保改文件下无文件");
            }
          });
      });
    },
    /**
     * 点击 修改文件夹
     */
    handleEdit() {
      this.setShow(false);
      if ( ["所有邮件", "未读", "已办理", "未办理"].includes(this.msg.fileName) ) {
        this.$message.warning("不允许修改当前文件夹");
        return;
      }
      this.$prompt("请输入变更后的文件夹名称", {
        confirmButtonClass: "确定",
        showInput: true,
        closeOnClickModal: false,
        inputValidator: function (value) {
          if (!value) {
            return "请输入文件夹名称";
          }
          // else if (2 <= value.length && value.length <= 6) {
          //   return true;
          // } else {
          //   return "请输入2-6个字符的名称";
          // }
        },
      }).then(({ value }) => {
        // 请求
        api
          .changeFolderName({
            id: this.msg.folderId,
            floderName: value,
          })
          .then((res) => {
            if (res.code == 200) {
              this.$store.dispatch("getMailInfo");
            } else {
              this.$message.error(res.message || "更新失败");
            }
          });
      });
    },

    /**
     * 点击 新建过滤规则
     * @Author: tangxiangping
     * @Date: 2022-06-06 10:35:46
     */    
    handleRule() {
      this.setShow(false);
      this.setRoleDialogVisible(true)
    },

    /**
     * 将过滤规则应用到历史邮件
     * @Date: 2022-06-08 11:55:38
     */    
    handleHistoryRule() {
      this.setShow(false);
      if(!this.msg.rule || this.msg.rule === '') {
        return this.$message.warning('请先添加过滤规则');
      }else {
        const rule = JSON.parse(this.msg.rule)
        if(Object.keys(rule).every(key => rule[key] === '')) {
          return this.$message.warning('请先添加过滤规则')
        }
      }
      api.putfilterToHistory(this.msg.folderId).then(res=>{
        if(res.code === 1000) {
          this.$message.success(res.message || '应用成功')
          this.$store.dispatch("getMailInfo");
        }
      })
    }
  },
};
</script>
<style scoped lang="less">
.contextMenu {
  position: fixed;
  // top: 0;
  // left: 200px;
  // border: 1px solid black;
  min-width: 150px;

  background-color: white;
  z-index: 999999999999999;
  border-radius: 3px;
  background-color: #fbfbfb;
  box-shadow: 0 0 5px 1px rgb(183, 181, 181);

  li {
    line-height: 30px;
    // color: black;
    height: 30px;
    color: black;
    font-size: 12px;
    padding: 0 20px;
    cursor: pointer;
    user-select: none;
  }

  li:hover {
    background-color: #f9eceb;
    // background-color: #cbcbcc;
    color: #c8423c;
  }
}
</style>
