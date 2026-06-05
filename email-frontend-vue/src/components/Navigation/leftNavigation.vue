<template>

  <div class="left" @contextmenu="contextMenu">
    <div class="button_box">
      <div>
        <!-- <router-link to="/editor"> -->
        <el-button
          type="danger"
          round
          icon="el-icon-edit-outline"
          style="
            box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1);
            width: 100%;
            background: #c63f3d;
            color: white;
            margin-bottom: 10px;
          "
          @click="handleWriteEmail"
          v-show="!isNotOperable"
        >
          写信
        </el-button>
        <!-- </router-link> -->
      </div>
      <div>
        <el-button
          plain
          round
          icon="el-icon-message"
          style="width: 100%; box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1)"
          @click="refnav"
          v-show="!isNotOperable"
        >
          刷新
        </el-button>
      </div>
    </div>
    <div>
      <div class="nav">
        <div>
          <el-menu
            class="el-menu-vertical-demo"
            :default-active="activeSecondLevelMenuIndex"
            :unique-opened="true"
            @select="handleMenuSelect"
            @open="handleMenuOpen"
            @close="handleMenuClose"
          >
          <!-- jianyuan左侧菜单 -->
            <MenuItem
              v-for="muses in LevelOneMenus"
              :key="muses.index"
              :data="muses"
              :selected-one-level-menu-index="selectedOneLevelMenuIndex"
              @menuItemClick="handleMenuItemClick"
              @oneLevelMenuClick="
                (oneLevelMenuInfo) =>
                  (selectedOneLevelMenuIndex = oneLevelMenuInfo.index)
              "
            />
          </el-menu>
        </div>
      </div>
    </div>
    <!-- 过滤规则弹框 tangxiangping 2022-06-06 start -->
    <el-dialog
      title="设置过滤规则"
      :visible.sync="roleDialogVisible"
      width="40%"
      :before-close="handleRoleDialogClose"
      :close-on-click-modal="false"
      >
      <!-- <div slot="title" class="dialog-title">
        <span class="title-text">设置过滤规则</span>
        <div class="use-history-mail">
          <span style="margin-right: 4px">应用到历史邮件</span>
          <el-switch v-model="roleFilterForm.useHistoryMail" size="medium"></el-switch>
        </div>
      </div> -->
      <el-form :model="roleFilterForm">
        <el-form-item label="标题：" :label-width="roleFormLabelWidth">
          <el-input
            v-model="roleFilterForm.subject"
            size="medium"
            autocomplete="off"
          ></el-input>
        </el-form-item>
        <el-form-item label="发起人：" :label-width="roleFormLabelWidth">
          <el-input
            v-model="roleFilterForm.senderName"
            size="medium"
            autocomplete="off"
            @focus="setPeople"
          ></el-input>
        </el-form-item>
        <el-form-item label="" :label-width="roleFormLabelWidth">
          <el-checkbox-group v-model="roleFilterForm.type">
            <el-checkbox label="应用到历史邮件" name="type"></el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="handleRoleDialogClose">取 消</el-button>
        <el-button type="primary" @click="handleRoleDialogConfirm"
          >确 定</el-button
        >
      </span>
    </el-dialog>
    <!-- 过滤规则弹框 tangxiangping 2022-06-06 end -->
  </div>
</template>

<script>
// 导入 api
import api from "@/api/api";
import { createNamespacedHelpers } from "vuex";
import EventBus from '@/utils/eventBus';
/**
 * 导入组件
 */
import MenuItem from "./components/menuItem.vue";
const { mapState, mapMutations } = createNamespacedHelpers("contextMenu");
export default {
  name: "Navigation",
  props: {
    // 是否不可操作(用户处于个人设置界面,主界面功能不可操作)
    isNotOperable: {
      type: Boolean,
      default: false,
    },
  },
  components: {
    MenuItem,
  },
  data() {
    return {
      // 一级菜单Level 1 menu
      LevelOneMenus: [],
      // 当前选中的 一级菜单 菜单下标
      selectedOneLevelMenuIndex: "1",
      // 当前选中的 二级菜单
      currentSubMenu: 0,
      currentSubMenuType: null,

      // 默认选中的二级菜单 index
      activeSecondLevelMenuIndex: "1-1",
      roleFilterForm: {
        id: "",
        subject: "",
        senderName: "",
        useHistoryMail: false,
        senderId: "",
        type: false
      },
      roleFormLabelWidth: "140px",
    };
  },
  watch: {
    // 监测 vuex 中的 菜单信息变更
    "$store.state.menus": {
      handler(newVal) {
        // 更新列表信息
        console.log('newVal菜单更新的值 :>> ', newVal);
        this.LevelOneMenus = newVal;
      },
      deep: true,
      immediate: true,
    },

    
    roleDialogVisible(newVal) {
      if (newVal) {
        this.roleFilterForm.id = this.msg.folderId
        if(this.msg.rule) {
          const filterRuleData = JSON.parse(this.msg.rule)
          this.roleFilterForm.subject = filterRuleData.subject;
          this.roleFilterForm.senderName = filterRuleData.senderName;
          this.roleFilterForm.senderId = filterRuleData.senderId
        }else {
          this.roleFilterForm.subject = "";
          this.roleFilterForm.senderName = "";
          this.roleFilterForm.senderId = "";
        }
       
      }
    },
    // "$store.state.numberLists":{
    //     handler(newV){
    //       if(newV){
    //           this.$store.dispatch("getNewMenus");
    //           console.log('leftLIMIAN :>> ');
    //       }
    //     },
    //     deep:true
    // }
  },
  computed: {
    ...mapState(["roleDialogVisible", "msg"]),
  },
  created() {
    api.emlianum().then((res)=>{
      this.$store.commit('setnum', res.msg || res)
    })
  },
  mounted() {
        // 获取邮件(菜单)数据
        this.getMenuNum();
         // 触发自定义事件, 传递当前生成出的 新版菜单结构
        this.$emit("getMenus", this.LevelOneMenus);

    this.getNewMenusList()//获取第三方菜单
  },
  methods: {
    ...mapMutations(["setRoleDialogVisible"]),
        //第三方待办菜单
    getNewMenusList() {
      this.$store.dispatch("getNewMenus");
    },
    // 右键点击事件
    contextMenu(e) {
      //   console.log(11111111111111);
      e.preventDefault();
    },
    /**
     * 点击 写信
     */
    handleWriteEmail() {
     if( document.getElementsByClassName('third_to_do_info')[0]){
      console.log(' document.getElementsBy :>> ',  document.getElementsByClassName('third_to_do_info')[0]);
      document.getElementsByClassName('third_to_do_info')[0].style.display = 'none'
     }
      let obj = {
        secretTypeId: "",
        summaryId: "",
        affairId: "",
        type: "editMail",
        editMailSend: "1",
        from: "",
        autosave: "1",
      };
      document.querySelector(".mid").style.display = "none";
      document.querySelector(".resize").style.display = "none";
      document.querySelector(".right").style.width = "auto";
      api
        .internalCompile(obj)
        .then((res) => {
          if (res.code == "10001") {
            // 在蓝网环境下不存在密级，所以不需要密级id
            if (
              res.msg.securityDefaultLevel &&
              res.msg.fileSecretLevelList &&
              res.msg.fileSecretLevelList.length != 0
            ) {
              // 红网
              //   console.log("红网进入");
              const result = res.msg.fileSecretLevelList.filter(
                (item) => item.name == res.msg.securityDefaultLevel
              );
              obj.secretTypeId = result[0].id;
            } else {
              //   console.log("蓝网进入");
            }
            this.getTheIncompleteMessage(obj);
          } else {
            this.$router.push("/editor");
            if (!this.$store.state.isWriteEamil) {
              //点击写信，传个标志位弹出密级选择弹框
              this.$store.commit("setWriteEamil", true);
            }
          }
        })
        .catch(() => {
          this.$router.push("/editor");
          if (!this.$store.state.isWriteEamil) {
            //点击写信，传个标志位弹出密级选择弹框
            this.$store.commit("setWriteEamil", true);
          }
        });
    },

    /**
     * 点击 刷新
     */
    refnav() {
      // 触发事件
      this.$emit("refleftnav");
      // 触发菜单点击事件
      this.handleMenuItemClick("1-1");
      // 设置默认选中的菜单
      this.activeSecondLevelMenuIndex = "1-1";
      // 通过 可操作性进行判断 是否需要进行路由切换
      !this.isNotOperable && this.$router.push("/");
      // 重载 页面信息
      this.getMenuNum();
    },
    addMenuNum() {},

    /**
     * 获取菜单列表(邮件分类、分类数量)
     */
    getMenuNum() {
      this.$store.dispatch("getMailInfo");
    },

    /**
     * 一级菜单 点击事件 的回调
     * @param {string} index: 点击的一级菜单 index
     */
    handleMenuOpen(index) {
      this.$emit("changeMenuSelected");
      //   console.log("点击的一级菜单", index);
      // 添加 一级级菜单选中 效果
      this.selectedOneLevelMenuIndex = index;
      // 清除 二级菜单 选中
      this.activeSecondLevelMenuIndex = null;
    },
    handleMenuClose(index) {
      this.$emit("changeMenuSelected");
    },

    /**
     * 菜单激活回调
     * @param {string} index: 选中菜单项的 index
     * @param {string} indexPath: 选中菜单项的 index path
     */
    handleMenuSelect(index, indexPath) {
      this.$emit("changeMenuSelected");
      this.$store.commit("showlxr", [7, 17]);
      this.$store.commit("isslect", "");
      // 通过 可操作性进行判断 是否需要进行路由切换(跳到收件箱)
      !this.isNotOperable && this.$router.push({ path: `/`, });
    },

    /**
     * 当前点击的 二级菜单
     * @param {string} type: 菜单类型标识
     * @param {string} path: 菜单路径
     * @param {object} menuInfo: 当前选中的菜单全部信息
     */
    handleMenuItemClick(type, path, menuInfo) {
      this.currentSubMenu = path;
      //   console.log("二级type :>> ", type);
      this.currentSubMenuType = type;

      // 设置选中的 二级菜单
      this.activeSecondLevelMenuIndex = menuInfo.index;

      // 清除 一级菜单的选中
      this.selectedOneLevelMenuIndex = null;

      // 验证是否是本地新增数据(新增时添加标识)
      if (!menuInfo.isLocallyAddedData) {
        // this.$emit("refleftnav");
      }
    },

    /**
     * 获取当前用户是否有未完成的邮件
     * @param {object} obj: 请求参数
     */
    getTheIncompleteMessage(obj) {
      this.$confirm("您尚有未完成的邮件, 是否继续完成?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        closeOnClickModal: false,
      })
        .then(() => {
          this.$router.push({
            path: "/editor",
            query: {
              obj,
            },
          });
        })
        .catch(() => {
          api.cancelAutosaveApi();
          this.$router.push("/editor");
          if (!this.$store.state.isWriteEamil) {
            //点击写信，传个标志位弹出密级选择弹框
            this.$store.commit("setWriteEamil", true);
          }
        });
    },

    /**
     * 关闭设置过滤规则弹框
     * @Author: tangxiangping
     * @Date: 2022-06-06 10:49:05
     */
    handleRoleDialogClose() {
      this.setRoleDialogVisible(false);
    },

    /**
     * 设置过滤规则弹框点击确定
     * @Author: tangxiangping
     * @Date: 2022-06-06 11:20:46
     */    
    handleRoleDialogConfirm() {
      console.log(this.roleFilterForm)
      const {id, subject, senderId, senderName} = this.roleFilterForm
      api.putfilter({id, subject, senderId, senderName}).then(res=>{
        if(res.code === 1000){
          if(this.roleFilterForm.type) {
            api.putfilterToHistory(this.msg.folderId).then(res=>{
              if(res.code === 1000) {
                this.$message.success(res.message || '应用成功')
              }
            })
          }else {
            this.$message.success(res.message || '设置成功')
          }
          this.setRoleDialogVisible(false)
          this.roleFilterForm = {
              id: "",
              subject: "",
              senderName: "",
              senderId: "",
              type: false
          }
          this.$store.dispatch("getMailInfo");
          EventBus.$emit('main_navigation_setFilterRule')
        }
      })
    },

    /**
     * 打开选人控件
     * @Author: tangxiangping
     * @Date: 2022-06-06 11:35:17
     */    
    setPeople() {
      let _that = this;
      parent.$.selectPeople({
        // 授权时的组织机构面板内容
        type: "selectPeople",
        panels: "Member,Department,Team,Account,Outworker",
        selectType: "Account,Department,Member,Team,Post,Outworker",
        onlyLoginAccount: false,
        isNeedCheckLevelScope: false, //不受职务级别控制
        hiddenPostOfDepartment: true,
        minSize: 0,
        maxSize: -1, //
        excludeElements: "",
        params: {
          text: _that.roleFilterForm.senderName,
          value: _that.roleFilterForm.senderId,
        },
        fillBackData: {},
        callback(result) {
          _that.roleFilterForm.senderName = result.text
          _that.roleFilterForm.senderId = result.value
        },
      });
    },
  },
};
</script>
<style scoped>

.dialog-title {
  display: flex;
  justify-content: space-between;
}

.use-history-mail {
  display: flex;
  align-items: center;
  font-size: 14px;
  margin-right: 32px;
  color: #599cf8;
}

.left {
  border-right: 1px solid #e4e8eb;
  height: 100%;
}

.button_box {
  padding: 20px 20px;
}

.left /deep/ .el-menu-item:hover {
  background: #f9eceb !important;
  color: #c8423c;
}

.left /deep/ .el-menu-item:hover .icontie {
  color: #c8423c;
}

.left /deep/ .el-menu-item:hover .numtip {
  color: #c8423c !important;
}

.left /deep/ .el-submenu__title:hover {
  background: #f9eceb !important;
  color: #c8423c;
}

.left /deep/ .el-submenu__title:hover .icontie {
  color: #c8423c;
}

.left /deep/ .el-submenu__title:hover .numtip {
  color: #c8423c !important;
}

.left /deep/ .el-menu-item.is-active {
  background-color: #f9eceb;
  border-left: 4px solid #c8423c;
  color: #c8423c;
}

.left /deep/ .el-menu-item.is-active .numtip {
  color: #c8423c !important;
}

.left /deep/ .el-submenu .el-menu-item {
  min-width: 150px !important;
}

.left /deep/ .el-menu {
  border-right: 0px !important;
}

.left /deep/ .el-submenu__icon-arrow {
  display: none;
}
</style>
