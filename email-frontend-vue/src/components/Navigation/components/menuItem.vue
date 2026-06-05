<!--
 * @LastEditTime : 2022-06-17 11:42:35
 * @FilePath     : /pc_mail/src/components/Navigation/components/menuItem.vue
 * @Description  : 菜单项
-->
<template>
  <div>
    <el-submenu
      :index="data.index"
      :class="{
        'sub-menu-active': selectedOneLevelMenuIndex == data.index,
      }"
      v-if="!data.appName"
    >
      <!-- data 为一级菜单对象 -->
      <div
        slot="title"
        class="sub-menu-content"
        style="position: relative"
        @click="handleOneLevelMenuClick(data)"
        @contextmenu.prevent="openMenu(data, 1, $event)"
      >
        <span class="info">
          <i class="icontie" :class="data.icon"></i>
          <!-- 一级菜单 -->
          <span> {{ data.name }} </span>
          <span
            class="numtip numtipThird"
            style="
              position: absolute;
              left: 70%;
              font-size: 12px;
              color: #999999;
            "
            v-if="!isNotOperable"
          >
            <!-- 一级菜单邮件数量 -->
         <!-- 屏蔽三方待办功能 thirdToDo1 -->
            <acronym title=""></acronym
            >{{ data.type == "thirdToDo1" ? data.affiarNum : data.num }}
            <span v-if="data.type == 'thirdToDo1'" class="third_to"> <span style="padding:0 1px;position: relative;top: 0px;">/</span> <span class="noreadNum">{{
             data.noReadMsgNum
            }}</span>
            </span>
          </span>
        </span>
        <!-- 隐藏掉菜单的箭头 -->
        <!-- <span class="arrow" v-if="data.Menus.length != 0">
          <i class="el-icon-arrow-up" v-if="isOpen"></i>
          <i class="el-icon-arrow-down" v-else></i>
        </span> -->
      </div>
      <el-menu-item
        style="position: relative"
        v-for="(menu, index) in data.Menus"
        :key="index"
        :index="menu.index || menu.path"
        :path="menu.path"
      >
        <div
          class="menu-item-content"
          @click="handleMenuItemClick(menu.type, menu.path, menu)"
          @contextmenu.prevent="openMenu(menu, 2, $event)"
         v-show="!(isNull && menu.affiarNum==0 && menu.noReadMsgNum == 0)"
        >
          <span class="fileName" :title="menu.fileName">
            <!-- 二级菜单 -->
            {{ menu.fileName }}
          </span>
          <span
            class="numtip numtips"
            style="font-size: 12px; color: #999999"
            v-if="!isNotOperable"
          >
            <!-- 二级菜单的邮件数量 -->
             <!-- 屏蔽三方待办功能 thirdToDo1 -->
            <span >{{ menu.type == "thirdToDo1" ?  menu.affiarNum : menu.number }}</span>           
            <span class="third_to_do" v-if="menu.type == 'thirdToDo1'"><span class="leftNum">/</span> 
            {{menu.noReadMsgNum}}
            </span>
          </span>
        </div>
       
      </el-menu-item>
       <!-- 屏蔽三方待办功能  -->
      <!-- <div v-show="!(JSON.stringify(data.Menus).indexOf('appName') == -1) && this.isShowMore">
        <div v-show="moreButton" @click="changeMoreState(1)" class="moreButton"><span class="moreTriangle">更多</span> </div>
        <div v-show="!moreButton" @click="changeMoreState(2)" class="moreButton"><span class="closeTriangle">点击收起</span></div>
      </div> -->


    </el-submenu>
   
    <ContextMenu :show='showContextMenu' :msg="contextmenuMsg" />
  </div>
</template>
<script>
// 导入 vuex modules layout
import layoutStore from "@/store/modules/layout.js";
// 导入 工具函数
import EventBus from "@/utils/eventBus.js";
import { createNamespacedHelpers } from "vuex";
import { eventBus } from "@/main.js";
// 导入 api
import api from "@/api/api";
import ContextMenu from "./contextMenu.vue";
const { mapState, mapMutations } = createNamespacedHelpers("contextMenu");
export default {
  name: "MenuItem",
  components: { ContextMenu },
  props: {
    // 菜单信息
    data: {
      type: Object,
      require: true,
    },
    // 当前选中的 一级菜单 菜单下标
    selectedOneLevelMenuIndex: {
      type: String,
      require: true,
    },
  },
  data() {
    return {
      moreText:true,
      moreButton:true,
      isShowMore:null,
      // 是否 展开
      isOpen: false,
      isNull:true,
      menuIndex:'',
      flag:false,
      arrs:[],
      menuInfos:{},
      timer:null,
      
      // 当前右键点击得菜单
      contextmenuMsg: {

      },
      showContextMenu: false,
    };
  },
  watch:{
    "$store.state.selectState":{
      handler(newV,oldV){
        console.log('newV,oldV :>> ', newV,oldV);
        // if(this.$store.state.selectState.isToDo){
      //   let id1 = oneLevelMenuInfo.id
      //   let str1 = `?size=10&page=1`
      //   api.getAllThirdToDo(id1,str1).then((res) => {
      //     // console.log("res.datadata.data) :>> ", res.data);
      //     EventBus.$emit("getEmailData", res.data);
      //   });
      // }
      // if(this.$store.state.selectState.isOk){
      //   let id2 = oneLevelMenuInfo.id
      //   let str2 = `?size=10&page=1`
      //   api.cipAppHasPending(id2,str2).then((res) => {
      //     // console.log("res.datadata.data) :>> ", res.data);
      //     EventBus.$emit("getEmailData", res.data);
      //   });
      // }
      // if(this.$store.state.selectState.isNoRead){
      //   let id3 = oneLevelMenuInfo.id
      //   let str3 = `?size=10&page=1`
      //   api.getNotReadList(id3,str3).then((res) => {
      //     // console.log("res.datadata.data) :>> ", res.data);
      //     EventBus.$emit("getEmailData", res.data);
      //   });
      // }
      // if(this.$store.state.selectState.isRead){
      //   let id4 = oneLevelMenuInfo.id
      //   let str4 = `?size=10&page=1`
      //   api.getReadMessageList(id4,str4).then((res) => {
      //     // console.log("res.datadata.data) :>> ", res.data);
      //     EventBus.$emit("getEmailData", res.data);
      //   });
      // }
      },
      // immediate:true,
      deep:true
    }
  },
  created(){
      // 注释 fuguichuan
      //  api.getNewMenu().then((res)=>{
      //    this.$store.commit('changeMenuNumber',res.data[0])
      //     // this.oneNoReadMsgNumber = res.data[0].noReadMsgNum
      //     // this.oneAffiarNumber = res.data[0].affiarNum
      
      // })
  },
  mounted() {
    console.log('data2222222 :>> ', this.data);
      //     EventBus.$on('OneAffiarNoReadNum',(data)=>{
      //     this.oneNoReadMsgNumber = data.noReadMsgNum
      //     this.oneAffiarNumber = data.affiarNum
      // })
      // 每隔5分钟定时刷新
      
   this.timer = setInterval(() => {
     this.getFxItemlist();
   },60000 )

  this.isNullIsShow()
  },
  
 beforeDestroy() {
   clearInterval(this.timer);
 },
  computed: {

    //   lyy
    ...mapState(["msg", "show"]),
    // lyy end
    // 是否不可操作(用户处于个人设置界面,主界面功能不可操作)
    isNotOperable() {
      return layoutStore.state.isNotOperable;
    },
    getSelectState(){
      return this.$store.state.selectState
    },
   
    
  },
  methods: {
    changeMoreState(key){    
      switch (key) {
        case 1:
        this.moreButton = false
        this.isNull = false  
          this.moreText= false
      //     let item =document.getElementsByClassName('menu-item-content')
      // var count = 0
      // for(let i = 0; i < item.length;i++){
      //   if(item[i].style.display == 'none'){
      //     count++
      //   }
      // }
      // console.log('count :>> ', count);
          break;
        case 2:
        this.moreButton = true
        this.isNull = true  
          this.moreText= false
        break;
        default:
          break;
      }

    },
    getFxItemlist() {
      // api.getNewMenu().then((res)=>{
      //   this.$store.commit('changeMenuNumber',res.data[0])
      // })
      },

    //   lyy
    ...mapMutations(["setMsg", "setShow"]),
    // lyy end

    // 判断当前点击的是否是 一级菜单下的已办
    isNullIsShow(){
      // 把待办数 和 未读数为0的应用的id存起来 
    //  <!-- 屏蔽三方待办功能 start -->
      // this.data.Menus.forEach((item)=>{
      //   if(item.affiarNum == 0 && item.noReadMsgNum == 0){
      //    this.arrs.push(item.id)
      //   }
      //   if(this.arrs.length > 0){
      //     this.isShowMore = true
      //   }
      // })
      //  <!-- 屏蔽三方待办功能 end -->
      // 当前点的是在 第三方待办的 已办页面
    //    EventBus.$on('click_have_to_do',(data)=>{      
        
    //     if(data.number == 2 && data.index.length ==1  ){
    //       this.isNull = false  
    //       this.moreText= false
    //     }
    //     else {
    //       // 如果当前点击的二级菜单是 待办数和未读数都为0的应用  那么就不隐藏
          
    //       if(JSON.stringify(this.arrs).indexOf(this.menuInfos.id) > 0 ){
    //         this.isNull = false
    //       }else{
    //         this.isNull = true
    //       }
    //     }
    // })
      

    },
    /**
     * 一级菜单点击事件
     * @param {obect} oneLevelMenuInfo: 一级菜单信息
     */
    handleOneLevelMenuClick(oneLevelMenuInfo) {    
     
      // lyy 保存一级文件夹的type到仓库 start
        // console.log("lyyyyyyyyyyy", oneLevelMenuInfo);
      this.$store.commit("currentMenuType", oneLevelMenuInfo.type);
      //   点击既触发
      //   this.setCurClick();
      //   lyy end
      // 菜单折叠
      this.isOpen = !this.isOpen;

      // 触发自定义事件, 传递选中的 一级菜单信息
      this.$emit("oneLevelMenuClick", oneLevelMenuInfo);

      // 触发 自定义事件, 获取 当前点击的菜单信息(主页_邮箱列表)
      EventBus.$emit("main_navigation_menuClick", oneLevelMenuInfo);

      // 添加自定义事件, 获取当前点击的 一级级菜单信息(个人设置_邮件个人设置_新建文件夹)
      EventBus.$emit(
        "userSetting_mailPersonalSettings_customFolder_getOneLevelMenuInfo",
        oneLevelMenuInfo
      );
       EventBus.$emit(
        "userSetting_mailPersonalSettings_customFolder_getOneLevelMenuInfo",
        oneLevelMenuInfo
      );
      // console.log(oneLevelMenuInfo, "一级菜单");

        //把当前点击的菜单的ID存到VUEX中
      this.$store.commit("changeCurrentID", oneLevelMenuInfo.id);


      //点击菜单获取 待办  已办 已读 未读  数量
      this.$store.dispatch('getAllNumber')
      // 一级菜单的第三方待办的待办数和未读消息数数量
      this.$store.dispatch("getNewMenus");  

      EventBus.$emit('watchBtn',oneLevelMenuInfo.id)
      //判断点击的一级菜单是否是  第三方待办
      if (oneLevelMenuInfo.id) {    
        if( document.getElementsByClassName('third_to_do_info')[0]){
            console.log(' document.getE一级菜单:>> ',  document.getElementsByClassName('third_to_do_info')[0]);
            document.getElementsByClassName('third_to_do_info')[0].style.display = 'block'
          } 
        //默认选中  待办页签  获取数据
        //发请求 获取第三方待办数据
         let id = oneLevelMenuInfo.id
         let str = `?size=10&page=1`
        api.getAllThirdToDo(id,str).then((res) => {
          // console.log("res.datadata.data) :>> ", res.data);
          EventBus.$emit("getEmailData", res.data);
        });
        
        // 获取 待办 已办 未读 已读数
        api.getApplicationNumber(oneLevelMenuInfo.id).then((res) => {
          EventBus.$emit("getApplicateNumber", res.data);
        });
      }
      // EventBus.$on('OneAffiarNoReadNum',(data)=>{
      //     this.oneNoReadMsgNumber = data.oneNoReadMsgNumber
      //     this.oneAffiarNumber = data.oneAffiarNumber
      // })
      this.$store.commit("setCurrentMenuInfo", oneLevelMenuInfo);

      //   lyy 写信时一级菜单弹出保存草稿
      this.$router.push("/");
      // if(this.$store.state.currentPage){
      //   let id = oneLevelMenuInfo.id
      //    let str = `?size=10&page=1`
      //       switch (this.$store.state.currentPage) {
      //         case 'isToDo':

      //           api.getAllThirdToDo(id,str).then((res) => {
      //             // console.log("res.datadata.data) :>> ", res.data);
      //             EventBus.$emit("getEmailData", res.data);
      //           });
      //         break;
      //         case 'isOk':
      //             api.cipAppHasPending(id,str).then((res) => {
      //               // console.log("res.datadata.data) :>> ", res.data);
      //               EventBus.$emit("getEmailData", res.data);
      //             });
      //         break;
      //         case 'isNoRead':

      //           api.getNotReadList(id,str).then((res) => {
      //             // console.log("res.datadata.data) :>> ", res.data);
      //             EventBus.$emit("getEmailData", res.data);
      //           });
      //         break;
      //         case 'isRead':

      //           api.getReadMessageList(id,str).then((res) => {
      //             // console.log("res.datadata.data) :>> ", res.data);
      //             EventBus.$emit("getEmailData", res.data);
      //           });
      //           break;

      //         default:
      //           break;
      //       }
      //       }
      this.isNullIsShow()

    },
    /**
     * lyy 右键菜单
     * @param {object} data
     */
    openMenu(data, symbol, event) {
      console.log('右键菜单',data, symbol, event)
      //   console.log("右键的菜单信息", data, symbol);
      //   ["所有邮件", "未读", "未办理", "已办理"]右键不生效
      if (
        symbol == 2 &&
        ["所有邮件", "未读", "未办理", "已办理"].includes(data.fileName)
      ) {
        return;
      } else {
        if (data.type == "inBox" || data.type == "sent") {
          // 鼠标位置
          const { x, y } = event;
          this.setShow(true);
          this.setMsg({
            ...data,
            symbol,
            mouse_x: x,
            mouse_y: y,
          });
        } else {
          this.showContextMenu = false;
          this.setShow(false);
        }
      }
    },
    /**
     * 二级菜单项 点击事件
     * @param {string} type: 菜单标识(用于区分当前点击的是哪一个菜单类别)
     * @param {string} path: 菜单路径
     * @param {object} menuInfo: 当前点击的菜单所有信息
     */
    handleMenuItemClick(type, path, menuInfo) {
      
      // lyy start
      //   this.setCurClick();
      // lyy end
      // if(window.sessionStorage.getItem('flag') ){
      //   console.log('object :>> ', window.sessionStorage.getItem('flag'));
      //   this.isNull = false
      // }
      this.menuIndex = menuInfo.index
      this.$emit("menuItemClick", type, path, menuInfo);
      this.$store.commit("currentMenuType", type);
      // 触发自定义事件, 传递 当前点击的 二级菜单信息(个人设置_邮件个人设置_新建文件夹)
      EventBus.$emit(
        "userSetting_mailPersonalSettings_customFolder_getSubMenuInfo",
        menuInfo
      );
      // console.log(menuInfo, "二级菜单");
      this.menuInfos =menuInfo   
      this.$store.commit("setCurrentMenuInfo", menuInfo);

      //把当前点击的菜单的ID存到VUEX中
      this.$store.commit("changeCurrentID", menuInfo.id);

     //点击菜单获取 待办  已办 已读 未读  数量
      this.$store.dispatch('getAllNumber')
  // 一级菜单的第三方待办的待办数和未读消息数数量
  this.$store.dispatch("getNewMenus");  
      //判断点击的二级菜单是否 有id
      if (menuInfo.id) {  
        if( document.getElementsByClassName('third_to_do_info')[0]){
            console.log(' document二级菜单 :>> ',  document.getElementsByClassName('third_to_do_info')[0]);
            document.getElementsByClassName('third_to_do_info')[0].style.display = 'block'
          }       
        //发请求 获取二级菜单数据
        let id = menuInfo.id
        let str = `?size=10&page=1`
        api.getAllThirdToDo(id,str).then((res) => {
          EventBus.$emit("getSecondEmailData", res.data);
        });

        // 获取 待办 已办 未读 已读数
        api.getApplicationNumber(menuInfo.id).then((res) => {
          // console.log('res.datawwwww :>> ', res.data);
          EventBus.$emit("getAppNumber", res.data);
        });
      }
      // 验证是否是本地新增数据(新增时添加标识)
      if (menuInfo.isLocallyAddedData) {
        this.$message.info("当前文件夹为本地新增文件夹, 暂无数据");
      } else {
        // 触发 自定义事件, 获取 当前点击的菜单信息(主页_邮箱列表)
        EventBus.$emit("main_navigation_menuClick", menuInfo);
      }
      this.isNullIsShow()
    },
  },
};
</script>

<style lang="less" scoped>
  // 更多  和 点击收起
  .moreButton{
    height: 40px;
    // background-color: red;
    line-height: 40px;
    text-align: center;
    cursor: pointer;
    position: relative;
  }
  .moreButton span{
    position: relative;
    color: #303133;
    font-size: 14px;

  }
  // .moreButton .closeTriangle::after{
  //   content: '▲';
  //   display: block;
  //   font-size: 14px;
  //   position: absolute;
  //   right: -21px;
  //   top: -9px;
  //   color: #303133;

  // }
  // .moreButton .moreTriangle::after{
  //   content: '▲';
  //   display: block;
  //   font-size: 16px;
  //   position: absolute;
  //   right: -21px;
  //   top: -9px;
  //   transform: rotateY(90deg);

  // }
  .moreButton:hover{
    background-color: #f9eceb;
  }
  
.el-submenu {
  // 菜单选中
  &.sub-menu-active {
    // ,&.is-opened
    background-color: #f9eceb !important;

    .el-submenu__title {
      color: #c8423c;

      .sub-menu-content {
        .info,
        i {
          color: #c8423c;
        }
      }
    }
  }

  /deep/ .el-submenu__title {
    width: 100%;
    display: inline-flex;
    align-items: center;
    justify-content: space-between;
    user-select: none;
    // lyy
    padding-left: 0 !important;
    padding-right: 0 !important;

    .sub-menu-content {
      width: 100%;
      display: flex;
      align-items: center;
      justify-content: space-between;
      //   lyy
      padding: 10px 20px;
    }

    & + .el-menu {
      user-select: none;

      .el-menu-item {
        padding: 0 !important;
        // txp 调整menu样式 start
        .menu-item-content {
          // lyy start
          //   width: 109px;
          display: flex;
          width: 100%;
          padding: 10px 0px 10px 20px;
          box-sizing: border-box;
          //   lyy end
          justify-content: space-evenly;
          align-items: center;

          line-height: 24px;
          word-break: break-all !important;
          word-wrap: break-word !important;
          // overflow: hidden;

          .fileName {
            display: inline-block;
            // word-wrap: break-word;
            // width: 83px;
            height: 100%;
            word-break: break-all !important;
            word-wrap: break-word !important;
            flex-shrink: 0;
            flex: 0 0 40%;
            // text-align: center;
            // white-space: nowrap;
            // overflow: hidden;
            // text-overflow: ellipsis;
          }

          .numtip {
            // width: 24px;
            flex: 0 0 30%;
            text-align: center;
          }
        }
        // txp 调整menu样式 end
      }
    }
  }
  .numtips {
    display: flex;
    justify-content: space-evenly;
    align-items: center;
  }
  .leftNum{
   padding:0 7px 0 0;
  }
  .third_to_do {
    display: flex;
    align-items: center;
   
  }
}
span.noreadNum {
    position: relative;
    top: 0.2;
    margin-left: 2px;
}
  //  <!-- 屏蔽三方待办功能  -->
//   .el-menu-vertical-demo div:last-child{
//   display: none !important;
// }
</style>
