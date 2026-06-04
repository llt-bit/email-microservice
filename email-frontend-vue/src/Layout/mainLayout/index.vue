<!--
 * @LastEditTime : 2022-04-30 12:30:27
 * @FilePath     : /emial-true(not-edit)/src/Layout/mainLayout/index.vue
 * @Description  : 页面主要布局结构
-->
<template>
  <div id="app">
    <el-container>
      <ContextMenu style="display:none;" />
      <el-header v-if="!isTrue" height="45px">
        <div class="title" @click="getinfo">内部 电子邮箱</div>
        <span class="setting-page-entrance">
          <el-tooltip
            effect="dark"
            content="点击进入个人设置界面"
            placement="bottom"
            v-if="!isNotOperable"
          >
            <i class="el-icon-setting" @click="handleSettingEntrance"></i>
          </el-tooltip>
        </span>
      </el-header>
      <el-container class="box" ref="box" style="height: calc(100% - 45px)">
        <el-aside
          class="leftMenu"
          width="201px"
          style="height: 100%"
          v-show="!isTrue && this.$store.state.isall"
        >
          <lettNav
            :is-not-operable="isNotOperable"
            @refleftnav="sx"
            @changeMenuSelected="changeMenuSelected"
            ref="leftnav"
          ></lettNav>
        </el-aside>
        <el-main style="padding: 0; height: 100%; display: flex">
          <div v-show="!isTrue && ishiden" style="height: 100%">
            <div class="mid">
              <contacts
                @sqlist="listsq"
                ref="cont"
                @gettypeitem="getinfo"
                @refleftnav="cct"
                @refleftnavs="refnav"
              >
              </contacts>
            </div>
            <div class="resize" title="收缩中间侧边栏"></div>
          </div>
          <div class="right">
            <router-view
              :ishidenbox="ishiden"
              @isallshowmid="isallshowmidbox"
              @gettypeitem="getinfo"
              @refleftnav="refnavs"
              @refleftnavs="refnav"
              :key="gettime()"
              ref="view"
              @addnum="addnumc"
            />
          </div>
          <!-- 不可操作时, 内容蒙层 -->
          <div class="not-operable-mask" v-if="isNotOperable"></div>
          <!-- 第三方待办 邮件展示 jianyuan isThirdToDo -->
          <!-- 屏蔽三方待办功能 thirdToDo1 -->
          <div class="third_to_do_info" v-if="this.oneType == 'thirdToDo1'">
            <div class="top_part">
              <div class="message_btn">
                <el-button
                  class="info_btn isActived"
                  icon="el-icon-document"
                  @click="selected(1)"
                  >待办事项
                  <span class="info_number">{{
                    this.$store.state.numberLists.affiarNum
                  }}</span></el-button
                >
                <el-button
                  class="info_btn"
                  icon="el-icon-document-checked"
                  @click="selected(2)"
                  >已办事项
                  <span class="info_number">{{ this.$store.state.numberLists.hasPendingCount }}</span></el-button
                >
                <el-button
                  class="info_btn"
                  icon="el-icon-folder"
                  @click="selected(3)"
                  >待阅消息
                  <span class="info_number">{{
                    this.$store.state.numberLists.noReadMsgNum
                  }}</span></el-button
                >
                <el-button
                  class="info_btn"
                  icon="el-icon-folder-opened"
                  @click="selected(4)"
                  >已阅消息
                  <span class="info_number">{{ this.$store.state.numberLists.readMsgNum}}</span></el-button
                >
              </div>
            </div>

            <div class="mid_part">
              <div class="search_part">
                <div class="search_input">
                  <span class="serach_title">标题：</span>
                  <el-input
                    placeholder="请输入内容"
                    prefix-icon="el-icon-search"
                    v-model="input1"
                    class="search_text"
                    @keyup.enter.native="search"
                  >
                  </el-input>
                </div>

                <div class="search_input">
                  <span class="serach_title">{{
                    this.noReadBtn == true || this.readBtn == true
                      ? " 摘要："
                      : "发起人："
                  }}</span>
                  <el-input
                    placeholder="请输入内容"
                    prefix-icon="el-icon-search"
                    v-model="input2"
                    class="search_text"
                    @keyup.enter.native="search"
                  >
                  </el-input>
                </div>
                <div class="search_input date_search">
                  <!-- value-format="yyyy-MM-dd hh:mm:ss" -->
                  <span class="serach_title">发起时间：</span>
                  <el-date-picker
                    v-model="value2"
                    type="datetimerange"
                    :picker-options="pickerOptions"
                    value-format="yyyy-MM-dd hh:mm:ss"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    @keyup.enter.native="search"
                    align="right"
                  >
                  </el-date-picker>
                </div>
              </div>

              <div class="btn_part">
                <button class="search_btn" @click="search">搜索</button>
                <button class="search_btn" @click="clear">重置</button>
              </div>
            </div>

            <div class="table">
              <div class="foot_part">
                <div class="filtrate">
                  <div class="left_part">
                    <el-button
                      v-show="isShow"
                      type="danger"
                      class="del_btn"
                      icon="el-icon-delete"
                      @click="open(1)"
                      >批量删除</el-button
                    >
                    <el-button
                      v-show="isShowAllRead"
                      type="success"
                      class="del_btn"
                      icon="el-icon-folder-opened"
                       @click="open(4)"
                      >全部标记已读</el-button
                    >
                    <el-button
                      v-show="isShowRead"
                      type="danger"
                      class="del_btn"
                      icon="el-icon-folder-opened"
                      @click="open(5)"
                      >标记已读</el-button
                    >
                    <el-button
                      v-show="isShowAllDel"
                      type="danger"
                      class="del_btn"
                      icon="el-icon-delete"
                      @click="open(2)"
                      >批量删除</el-button
                    >
                    <!-- <el-button
                      v-show="isShowDel"
                      type="danger"
                      class="del_btn"
                      icon="el-icon-delete"
                      @click="open(3)"
                      >清空消息（批量删除）</el-button -->
                    
                  </div>
                </div>
              </div>
              <el-table
                v-if="isShow"
                :data="tableData"
                height="80%"
                border
                style="width: 100%"
                @selection-change="handleSelectionChange"
                 @row-click="editCurrentApplicationApp"
                :header-cell-style="{ background: '#eef1f6', color: '#606266' }"
              >
                
                <el-table-column type="selection" width="55"> </el-table-column>
                <el-table-column prop="title" label="标题"> </el-table-column>
                <el-table-column prop="senderName" label="发起人" width="180">
                </el-table-column>
                <el-table-column
                  prop="creationDate"
                  label="发起时间"
                  width="180"
                >
                </el-table-column>
                <el-table-column
                  v-if="this.currentIndex.length == '1'"
                  prop="registerName"
                  label="应用名称"
                  width="180"
                >
                </el-table-column>

                <!-- <el-table-column label="操作" width="100">
                  <template slot-scope="scope">
                    <el-button
                      class="eidt_btn"
                      size="mini"
                      @click.native.stop="handleEdit(scope.$index, scope.row)"
                      >编辑</el-button
                    >
                    <el-button
                      class="table_del_btn"
                      size="mini"
                      type="danger"
                      @click.native.stop="handleDelete(scope.$index, scope.row)"
                      >删除</el-button
                    >
                  </template>
                </el-table-column> -->
              </el-table>
              <el-table
                v-if="isShowAllDel"
                :data="tableData"
                height="80%"
                border
                style="width: 100%"
                @selection-change="handleSelectionChange"
                @row-click="editCurrentApplicationApproval"
                :header-cell-style="{ background: '#eef1f6', color: '#606266' }"
              >
                
                <el-table-column type="selection" width="55"> </el-table-column>
                <el-table-column prop="messageTitle" label="标题"> </el-table-column>
                <el-table-column prop="messageDigest" label="摘要" width="180">
                </el-table-column>
                <el-table-column
                  prop="creationDate"
                  label="接收时间"
                  width="180"
                >
                </el-table-column>
                <el-table-column
                  v-if="this.currentIndex.length == '1'"
                  prop="appName"
                  label="应用名称"
                  width="180"
                >
                </el-table-column>
                <!-- <el-table-column label="操作" width="100">
                  <template slot-scope="scope">
                    <el-button
                      class="eidt_btn"
                      size="mini"
                      @click.native.stop="handleEdit(scope.$index, scope.row)"
                      >编辑</el-button
                    >
                    <el-button
                      class="table_del_btn"
                      size="mini"
                      type="danger"
                      @click.native.stop="handleDelete(scope.$index, scope.row)"
                      >删除</el-button
                    >
                  </template>
                </el-table-column> -->


              </el-table>
              <div class="pagination jian">
                <el-pagination
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                  
                  :page-sizes="[5, 10, 15, 20]"
                  :page-size="this.pagesize"
                  layout="total, prev,sizes, pager, next"
                  :total="this.total"
                  :current-page.sync="currentPage"
                >
                </el-pagination>
              </div>

                <!-- <div id="wangeditor" v-show="wangeditorIsShow" >
                    <WangEditor  />
                </div> -->
              
            </div>
          </div>
        </el-main>
      </el-container>
    </el-container>

    <!-- 遮罩层 readMessageDielog -->
    <div class="readMessageDielog" ref="reference" v-show = "contentHtmlMessageBox" @click="closeMessageBox">
      </div>
       <!-- 装消息盒子的容器 -->
       <div class="containerBox" v-show = "contentHtmlMessageBox"> 
        <div class="topPart">
         <div class="btn">
          <el-button  
          round
            class="closeX"
            size="mini"
            @click="closeMessageBox"
            icon="el-icon-close"></el-button>
         </div>
          
            <div class="infomation">
          <p class="fromRegion">来源系统：{{this.messageInfomation.appname}}</p>
          <p>标题：{{this.messageInfomation.messageTitle}}</p>
          <p>时间：{{this.messageInfomation.creationDate}}</p>
        <p>摘要：{{ this.messageInfomation.messageDigest }}</p>
        </div>
        </div>
      <!-- 消息盒子 -->
      
      <div class="infoBox" >
        <!-- 富文本消息 -->
        
        <div class="messageInfoBox" v-show = "htmlOrText">

            <div class="contentHtml"  v-html ="contentHtml" >
          </div>
        </div> 
              
        <!-- 文本消息 -->
        <div class="messageText" v-show = "!htmlOrText" >
          <div class="mainContent">
            <div class="contentText">         
              {{this.messageInfo.contentText}}
            </div>
          </div>
        </div>
      </div>
      <div class="bottomPart">
          <button class="bottomClose" @click="closeMessageBox">关闭</button>
        </div>
      </div>
  </div>
</template>

<script>
// 导入 vuex modules layout
import layoutStore from "@/store/modules/layout.js";
import Vue from "vue";
// 导入 api
import api from "@/api/api";
/**
 * 导入组件
 */

import lettNav from "@/components/Navigation/leftNavigation";
import contacts from "@/components/contacts/index";
import axios from "axios";
// 导入EventBus
import EventBus from "@/utils/eventBus.js";
// import E from "wangeditor";//导入富文本组件
// import WangEditor from "./components/WangEditor.vue"
import { eventBus } from "@/main.js";
// 右键弹框
import ContextMenu from "../../components/Navigation/components/contextMenu";
export default {
  name: "MainLayout",
  data() {
    return {
      // editor: null,//富文本
      // wangeditorIsShow:false,
      ishiden: true,
      type: "",
      // 默认隐藏左侧窗格
      hideLeftPane: false,
      isTrue: false,
      // 当前点击的一级文件夹的type
      oneType: null,
      multipleSelection: [],
      checked: true,
      input2: "", // 摘要 和发起人
      input1: "", //标题
      isShow: true, //删除按钮
      noReadBtn: false, //当前是否是  未读消息标签
      readBtn: false, //当前是否是  已读消息标签
      isShowAllRead: false, //全部标记已读
      isShowRead: false, //标记已读
      isShowAllDel: false, //全部情空
      isShowDel: false, //清空
      isMessageTable: false, //是否是已读和未读
      affiarNum: 0, //待办
      hasPendingCount: 0, //已办
      noReadMsgNum: 0, //未读
      readMsgNum: 0, //已读
      tableData: [],
      total:0,
      pagesize:5,
      currentPage: 1,
      currentIndex: "0", //当前点击的菜单的index
      currentId: "0", //当前点击的ID
      selectData:[],//选中的列表数据的ID
      value2: "", //日期选择
      pickerOptions: {
        shortcuts: [
          {
            text: "最近一周",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 7);
              picker.$emit("pick", [start, end]);
            },
          },
          {
            text: "最近一个月",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 30);
              picker.$emit("pick", [start, end]);
            },
          },
          {
            text: "最近三个月",
            onClick(picker) {
              const end = new Date();
              const start = new Date();
              start.setTime(start.getTime() - 3600 * 1000 * 24 * 90);
              picker.$emit("pick", [start, end]);
            },
          },
        ],
      },
      notReadLis:[] , //未读消息的 id集合
      notReadBtn:true,

// 用于分页标识
      isToDo:true,//待办、
      isOk:false,//已办
      isNotRead:false,//未读
      isRead:false,//已读
      contentHtml:null,//富文本
      contentHtmlMessageBox:false,
      messageInfo:{
        contentText:'', //文本内容
        contentTitle:'',
        createTime:''
      },
      htmlOrText:true,
      timers:null,

      // 消息弹框的数据
      messageInfomation:{}
      
      // datas: {
      //   id: 0,
      //   value: {
      //     affiarNumber: 0, //待办
      //     done: 1, //已办
      //     noReadMsgNumber: 2, //未读
      //     readed: 3, //已读
      //   },
      // },
    };
  },
  watch: {
    //监听路由变化 控制邮件详情的显隐
    $route: {
      handler(newUrl) {
        if (newUrl.path.indexOf("inboxDet") != -1) {
          const urlList = window.location.href.split("/");
          this.isTrue = urlList[urlList.length - 1] == "true" ? true : false;
        }
      },
      immediate: true,
      deep: true,
    },
    //监听当前菜单的切换
    "$store.state.currentMenuInfo": {
      handler(newVal, oldVal) {
        if (newVal.index) {
          newVal.index !== oldVal.index && this.selected(1);
          newVal.index == oldVal.index && this.selected(1);
        }
      },
      immediate: true,
      deep: true,
    },
    "pagesize":{
      handler(newV,oldV){
       
        if(newV){
          // console.log('this.pagesize发生改变 :>> ');
          this.currentPage = 1
          // console.log('this.currentPage :>> ', this.currentPage);
        }
      },
      deep:true,
    },
    "tableData":{
      handler(newV){
        if(newV){
          // if(this.currentIndex.length == 1){
          //  this.getNumberToOne()
          this.currentPage = 1
          // }else{
          //   this.getNumberToTow()
          // }
        }
      }
    },

    //监听当前点击的菜单的ID  更新  待办 已办等的数量
    "$store.state.currentID":{
      handler(newV){ 
         this.$store.dispatch('getAllNumber')
    
      },
       deep: true,
    }
  },
  components: { lettNav, contacts, ContextMenu},
  // created() {
  //   const urlList = window.location.href.split("/");
  //   this.isTrue = urlList[urlList.length - 1] == "true" ? true : false;

  // },
  computed: {
    // 是否不可操作(用户处于个人设置界面,主界面功能不可操作)
    isNotOperable() {
      return layoutStore.state.isNotOperable;
    },
  },
  methods: {
    // 第三方待办 jianyuan start、
    // 刷新
    getFxItemlist() {
     
      this.$store.dispatch("getMailInfo"); 
      this.$store.dispatch("getNewMenus");  
      this.$store.dispatch('getAllNumber');
      // this.getLeveDataNotRead(1)
      // this.getOneLeveListData(1)
      let id = this.currentId
      let str = `?size=${this.pagesize}&page=${page}`
      if(this.isTodo){
      api.getAllThirdToDo(id,str).then((res)=>{
        this.tableData = res.data.data;
         this.total =res.data.total
         this.pagesize = res.data.pageSize
      })
      }
      if(this.isOk){

      api.cipAppHasPending(id,str).then((res)=>{
        this.tableData = res.data.data;
         this.total =res.data.total
         this.pagesize = res.data.pageSize
      })
      }
      if(this.isNotRead){
        api.getNotReadList(id,str).then((res)=>{
        // console.log('获取菜单未读数据 :>> ', res);
        this.tableData = res.data.data;
         this.total =res.data.total
         this.pagesize = res.data.pageSize
         res.data.data.forEach(item => {
           this.notReadLis.push(item.id)
         });
      })
      }
      if(this.isRead){
        api.getReadMessageList(id,str).then((res)=>{
        // console.log('获取菜单已读数据 :>> ', res);
        this.tableData = res.data.data;
         this.total =res.data.total;
         this.pagesize = res.data.pageSize;
      })
      }
      },
    //功能按钮交互 提示
    open(type) {
      let msg = "";
      let successMsg = "";
      let cancelMsg = "";     
      if(this.selectData.length == 0 && (type == 1 || type == 3)){
            alert('请选择要删除的数据')
          return
      }
        if(this.selectData.length == 0 && type == 5 ){
            alert('请选择要标记数据')
          return
      }
      switch (type) {
        case 1:
          msg = "此操作将永久删除该文件, 是否继续?";
          successMsg = "删除成功！";
          cancelMsg = "已取消删除";
         
          break;
        case 2:
          msg = "此操作将删除所选择的文件, 是否继续?";
          successMsg = "已删除所选择的文件！";
          cancelMsg = "已取消删除所选择的文件";
          break;
        case 3:
          msg = "此操作将清空已选择的文件, 是否继续?";
          successMsg = "已清空已选择的文件！";
          cancelMsg = "已取消当前操作";
          break;
        case 4:
           msg = "此操作将标记当前所有消息为已读, 是否继续?";
          successMsg = "当前所有消息已标记为已读！";
          cancelMsg = "已取消当前操作";
          break;
        case 5:
           msg = "此操作将标记当前选择的消息为已读, 是否继续?";
          successMsg = "当前选择的消息已标记为已读！";
          cancelMsg = "已取消当前操作";
          break;
        default:
          break;
      }
      this.$confirm(msg, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        center: true,
      })
        .then(() => {
           let arr = this.selectData
           let str = this.currentId
           console.log('this.selectData',this.selectData);
           if(type == 1 ){
            api.cipAppAffiarHasPendingDel(arr).then((res)=>{                       
              if(res.data.deleteState == true ){
                if( this.isToDo == true){
                  this.getOneLeveListData(1)
                  this.$store.dispatch('getAllNumber')
                  this.$store.dispatch("getNewMenus");  
                }else{
                  this.getLeveDataHasPending(1)
                  this.$store.dispatch('getAllNumber')
                  this.$store.dispatch("getNewMenus");  
                }      
              }            
            })
           }
           if(type == 2){     
             
             api.cipAppSelectDel(arr).then((res)=>{                       
              if(res.data.deleteState == true){
                console.log('this.isNoRead',this.isNoRead);
                if(this.isNoRead){
                  this.getLeveDataNotRead(1)                
                }else{
                  this.getLeveDataReadMessage(1)                
                }
                this.$store.dispatch('getAllNumber')
                this.$store.dispatch("getNewMenus"); 
                          
              }            
            })
           }
           if(type == 4){
             api.MarkReadAll(str).then((res)=>{                    
              if(res.data.deleteState == true){
               this.getLeveDataReadMessage(1)    
              }      
                this.getLeveDataNotRead(1)
                this.$store.dispatch('getAllNumber')
                this.$store.dispatch("getNewMenus");        
            })
           }
          if(type == 3){
             api.cipAppSelectDel(arr).then((res)=>{                    
              if(res.data.deleteState == true){
               this.getLeveDataReadMessage(1)   
               this.$store.dispatch('getAllNumber')
                this.$store.dispatch("getNewMenus");   
              }                                          
            })
           }
           if(type == 5){
             api.selectMarkRead(arr).then((res)=>{   
              if(res.data.deleteState == true){
                this.getLeveDataNotRead(1)
              }    
              this.getLeveDataNotRead(1)
                this.$store.dispatch('getAllNumber')
                this.$store.dispatch("getNewMenus");          
            })
           }         
          this.$message({
            type: "success",
            message: successMsg,
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: cancelMsg,
          });
        });
    },

    // 消息列表 点击消息展示详情或者富文本结构
    editCurrentApplicationApproval(row) {     
      this.messageInfomation.appname = row.appName
      this.messageInfomation.creationDate= row.creationDate
      this.messageInfomation.messageTitle = row.messageTitle
      this.messageInfomation.messageDigest = row.messageDigest
      this.contentHtmlMessageBox = true
      this.contentHtml = row.messageContent
      
      if(this.isNoRead){
          api.selectMarkRead(row.id).then((res)=>{  
            
          if(res.data.deleteState == true){
            this.getLeveDataNotRead(1)
          }    
            this.getLeveDataNotRead(1)
            this.$store.dispatch('getAllNumber')
            this.$store.dispatch("getNewMenus");           
        })
        
      }
    },
    closeMessageBox(){
        this.contentHtmlMessageBox = false;

      
    },
    //待办点击穿透
    editCurrentApplicationApp(row){
      window.open(row.url, '_blank', 'width=1500,height=800,toolbar=no,scrollbars=no,menubar=no,screenX=100,screenY=100');
    },
    //当前点的是那个应用菜单

    getPagenation(page){
      let id = this.currentId
      let str = `?size=${this.pagesize}&page=${page}`
      if(this.isShowAllDel){

        //消息分页
          api.getAllThirdToDo(id,str).then((res)=>{
          this.tableData = res.data.data
          this.total = res.data.total
      })
      }else{
        api.getAllThirdToDo(id,str).then((res)=>{
          this.tableData = res.data.data
          this.total = res.data.total
      })
      }

},
    //获取一级菜单待办数据
    getOneLeveData() {
      EventBus.$on("getEmailData", (data) => {
        this.tableData = data.data;
        this.total =data.total
        this.pagesize = data.pageSize
       
      });

    },

    // 获取二级菜待办单数据
    getSecendLeveData() {
      
      EventBus.$on("getSecondEmailData", (data) => {
        // console.log('获取二级菜待办单数据 :>> ', data);
        this.tableData = data.data;
         this.total =data.total
         this.pagesize = data.pageSize
   
      });
    },
    //获取待办 数据
    getOneLeveListData(page){
      let id = this.currentId
      let str = `?size=${this.pagesize}&page=${page}`
      api.getAllThirdToDo(id,str).then((res)=>{
        this.tableData = res.data.data;
         this.total =res.data.total
         this.pagesize = res.data.pageSize
      })
    },
     //获取菜单已办数据
    getLeveDataHasPending(page){
       let id = this.currentId
      let str = `?size=${this.pagesize}&page=${page}`
      api.cipAppHasPending(id,str).then((res)=>{
        this.tableData = res.data.data;
         this.total =res.data.total
         this.pagesize = res.data.pageSize
      })
      
      

    },
    //获取菜单未读消息数据
    getLeveDataNotRead(page){
       let id = this.currentId
      let str = `?size=${this.pagesize}&page=${page}`
      api.getNotReadList(id,str).then((res)=>{
        // console.log('获取菜单未读数据 :>> ', res);
        this.tableData = res.data.data;
         this.total =res.data.total
         this.pagesize = res.data.pageSize
         res.data.data.forEach(item => {
           this.notReadLis.push(item.id)
         });
      })
    },
        //获取菜单已读消息数据
    getLeveDataReadMessage(page){
       let id = this.currentId
      let str = `?size=${this.pagesize}&page=${page}`
      api.getReadMessageList(id,str).then((res)=>{
        // console.log('获取菜单已读数据 :>> ', res);
        this.tableData = res.data.data;
         this.total =res.data.total;
         this.pagesize = res.data.pageSize;
      })
    },

    getStated(){
      
    },
    // 获取二级菜已办单数据
    getSecendLeveDataHasPending(){},
    //当前点击的是 待办  已办  未读  已读
   
    selected(type) {
      let info_btn = document.getElementsByClassName("info_btn");
      let info_btn0 = document.getElementsByClassName("info_btn")[0];
      let info_btn1 = document.getElementsByClassName("info_btn")[1];
      let info_btn2 = document.getElementsByClassName("info_btn")[2];
      let info_btn3 = document.getElementsByClassName("info_btn")[3];
      this.input1 = '';
      this.input2 = '';
      this.value2 = ''
      // 用于传递 控制待办数和未读数为0的应用 的 数据
      let datasInfo = {
        index:this.currentIndex,
        number:type
      }
        EventBus.$emit("click_have_to_do", datasInfo);
      
      
      if (type == 1) {
        info_btn0.classList.add("isActived");
        info_btn1.classList.remove("isActived");
        info_btn2.classList.remove("isActived");
        info_btn3.classList.remove("isActived");
        this.isShow = true;
        this.isShowAllRead = false;
        this.isShowRead = false;
        this.isShowAllDel = false;
        this.isShowDel = false;
        this.isMessageTable = true; //加了  ！不显示
        this.readBtn = false;
        this.noReadBtn = false;

        this.isToDo = true
        this.isOk = false
        this.isNoRead = false
        this.isRead = false
        let  selectState= {
              isToDo: true,
              isOk: false,
              isNoRead: false,
              isRead: false,
          }
          this.$store.commit('stateSelect',selectState)
          console.log(this.$store.state.selectState);
        // console.log("待办页");
        EventBus.$emit('watchBtn','isToDo')

        //发起数据请求  请求当前菜单下的待办数据 展示在表格中
        //如果是一级菜单
        // console.log("this.currentIndex.length :>> ", this.currentIndex.length);
        if (this.currentIndex.length == 1) {
          // console.log("一级菜单 ");
          this.getOneLeveListData(1);
          // console.log('this.table :>> ', this.tableData);
        } else {
          // console.log("二级菜单");

          this.getOneLeveListData(1);
        }
        // 如果是二级菜单
      } else if (type == 2) {
        info_btn0.classList.remove("isActived");
        info_btn1.classList.add("isActived");
        info_btn2.classList.remove("isActived");
        info_btn3.classList.remove("isActived");
        this.readBtn = false;
        this.noReadBtn = false;
        this.isShow = true;
        this.isShowAllRead = false;
        this.isShowRead = false;
        this.isShowAllDel = false;
        this.isShowDel = false;
        this.isMessageTable = true; //加了  ！不显示
        // console.log("已办页");
         this.isToDo = false
        this.isOk = true
        this.isNoRead = false
        this.isRead = false
        let  selectState= {
              isToDo: false,
              isOk: true,
              isNoRead: false,
              isRead: false,
          }
          // this.$store.commit('stateSelect',selectState)
          console.log('2',this.$store.state.selectState);
          this.$store.commit('stateSelected','isOk')
         
        // if (this.currentIndex.length == 1) {
        //   console.log("一级菜单 ");
        //   //代办数据
        //   // this.getOneLeveData();
        //   // 已办数据
        //   // this.getLeveDataHasPending(1)
        // } else {
        //   console.log("二级菜单");
        //   this.getSecendLeveData();
          this.getLeveDataHasPending(1)
        // }

        
      } else if (type == 3) {
        info_btn0.classList.remove("isActived");
        info_btn1.classList.remove("isActived");
        info_btn2.classList.add("isActived");
        info_btn3.classList.remove("isActived");
        this.readBtn = false;
        this.noReadBtn = true;
        this.isShow = false;
        this.isShowAllRead = true;
        this.isShowRead = true;
        this.isShowAllDel = true;
        this.isShowDel = false;
        // console.log("未读消息页");
        this.notReadBtn = true //判断是未读消息页还是已读  用于搜索判定
        this.isMessageTable = true;

        this.isToDo = false
        this.isOk = false
        this.isNoRead = true
        this.isRead = false
        let  selectState= {
              isToDo: false,
              isOk: false,
              isNoRead: true,
              isRead: false,
          }
          this.$store.commit('stateSelected','isNoRead')
        // console.log('状态',this.$store.state.selectState)
        // console.log('3',this.$store.state.selectState);
        EventBus.$emit('watchBtn',selectState)
        if (this.currentIndex.length == 1) {
          // console.log("一级菜单 ");
          this.getLeveDataNotRead(1)
          this.getOneLeveData();
        } else {
          // console.log("二级菜单");
          this.getSecendLeveData();
          this.getLeveDataNotRead(1)

        }


        //弹框
      } else {
        info_btn0.classList.remove("isActived");
        info_btn1.classList.remove("isActived");
        info_btn2.classList.remove("isActived");
        info_btn3.classList.add("isActived");
        this.readBtn = true;
        this.noReadBtn = false;
        this.isShow = false;
        this.isShowAllRead = false;
        this.isShowRead = false;
        this.isShowAllDel = true;
        this.isShowDel = true;
        this.isMessageTable = true; //加了  ！不显示
        // console.log("已读消息页");
        this.notReadBtn = false //判断是未读消息页还是已读  用于搜索判定

        this.isToDo = false
        this.isOk = false
        this.isNoRead = false
        this.isRead = true
        let  selectState= {
              isToDo: false,
              isOk: false,
              isNoRead: false,
              isRead: true,
          }
          // this.$store.commit('stateSelect',selectState)
          this.$store.commit('stateSelected','isRead')
          console.log('4',this.$store.state.selectState);
        EventBus.$emit('watchBtn','isRead')

        if (this.currentIndex.length == 1) {
          // console.log("一级菜单 ");
          this.getLeveDataReadMessage(1)
          this.getOneLeveData();
        } else {
          // console.log("二级菜单");
          this.getLeveDataReadMessage(1)
          this.getSecendLeveData();
        }


      }
    },
    //表格中选中的项
    handleSelectionChange(val) {
      this.multipleSelection = val;
      let arr = []
      for(let i = 0;i < this.multipleSelection.length; i++){
        arr.push(this.multipleSelection[i].id)
      }
      this.selectData = arr
      // console.log("this.multipleSelection  :>> ", this.multipleSelection);

    },
    // 编辑当前行数据
    handleEdit(index, row) {
      // console.log(index, row, "编辑的内容");
    },
    // 删除当前行数据
    handleDelete(index, row) {
      // console.log(index, row, "删除的内容");
      this.$confirm("此操作将永久删除该文件, 是否继续?", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        center: true,
      })
        .then(() => {
          this.$message({
            type: "success",
            message: "删除成功!",
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消删除",
          });
        });
    },
        // 当前第几页
    handleCurrentChange(val) {
      // console.log(`当前页: ${val}`);
      if(this.isToDo){
        this.daiban(val)
      }else if(this.isOk){
        this.HasPending(val)
      }else if(this.isNoRead){
        this.messageNoRead(val)   
       
      }else{
       this.messageRead(val)
       
      }
    },
    // 每页多少条
    handleSizeChange(val) {
      // console.log(`每页 ${val} 条`);
      this.pagesize = val
      if(this.isToDo){
        this.daiban(1)
      }

      else if(this.isOk){
        this.HasPending(1)
      }else if(this.isNoRead){
        this.messageNoRead(1)   
       
      }else{
       this.messageRead(1)
      }

      // this.getPagenation(1)
    },

    //待办分页
    daiban(page){
      let id = this.currentId
      let str = `?size=${this.pagesize}&page=${page}`
      api.getAllThirdToDo(id,str).then((res)=>{
        this.tableData = res.data.data
        this.total = res.data.total
      })
    },

    // 已办分页
    HasPending(page){
        // 已办分页
       let id = this.currentId
       let str = `?size=${this.pagesize}&page=${page}`
        api.cipAppHasPending(id,str).then((res)=>{
          this.tableData = res.data.data
          this.total = res.data.total
      })
    },

    // 未读消息分页
    messageNoRead(page){
      let id = this.currentId
       let str = `?size=${this.pagesize}&page=${page}`
        api.getNotReadList(id,str).then((res)=>{
          this.tableData = res.data.data
          this.total = res.data.total
      })
    },

    // 已读消息分页
    messageRead(page){
      // 已读消息分页
       let id = this.currentId
       let str = `?size=${this.pagesize}&page=${page}`
        api.getReadMessageList(id,str).then((res)=>{
          this.tableData = res.data.data
          this.total = res.data.total
      })
    },

    //第三方待办菜单
    getNewMenusList() {
      this.$store.dispatch("getNewMenus");
    },
    //搜索按钮点击事件
    search() {
      let searchStr = "";
      let startDate = ''
       let endDate = ''
       let id = this.currentId
       console.log(this.value2,'3123');
      if (this.value2) {
          startDate = this.value2[0];
         endDate = this.value2[1];  
      }      
      searchStr = `?v=${Math.random()}`
      if(this.isShowAllDel == false){
        
        if(this.input1){
          searchStr += "&title=" + this.input1;
        }
        if(this.input2){
          searchStr += "&senderName=" + this.input2;
        }
        if(this.value2){
          searchStr += "&creationDate=" + startDate + "&creationDate=" + endDate;
        }
         //待办搜索
          if(this.isToDo){
            api.searchInfo(searchStr, id).then((res) => {
              this.tableData = res.data.data
              this.total = res.data.total
            });
          }
          // 已办搜索
          if(this.isOk){
            api.searchInfoHasPend(searchStr, id).then((res) => {
              this.tableData = res.data.data
              this.total = res.data.total
            });
          }
      }else{
       
        if(this.input1){
          searchStr += "&title=" + this.input1;
        }
        if(this.input2){
          searchStr += "&digest=" + this.input2;
        }
        if(this.value2){
          searchStr += "&creationDate=" + startDate + "&creationDate=" + endDate;
        }
        //  if (this.input1 == "" && this.input2 == "" && this.value2 == "") {
        //         alert("请输入要搜索的值！！！");
        //       } else if (this.input1 != "" && this.input2 == "" && this.value2 == "") {
                
        //       } else if (this.input1 == "" && this.input2 != "" && this.value2 == "") {
                
        //       } else if (this.input1 == "" && this.input2 == "" && this.value2 != "") {
                
        //       } else if (this.input1 == "" && this.input2 != "" && this.value2 != "") {
        //         searchStr =
        //           "?digest=" +
        //           this.input2 +
        //           "&creationDate=" +
        //           startDate +
        //           "&creationDate=" +
        //           endDate;
        //       } else if (this.input1 != "" && this.input2 == "" && this.value2 != "") {
        //         searchStr =
        //           "?title=" +
        //           this.input1 +
        //           "&creationDate=" +
        //           startDate +
        //           "&creationDate=" +
        //           endDate;
        //       } else if (this.input1 != "" && this.input2 != "" && this.value2 == "") {
        //         searchStr ="?title=" + this.input1 + "&digest=" + this.input2;
        //       } else {
        //         // searchStr =`?title=${this.input1}&digest=${this.input2}&creationDate=${startDate}&creationDate=${endDate}`;
        //         searchStr ="?title="+this.input1+"&digest="+this.input2+"&creationDate="+startDate+"&creationDate="+endDate;
        //       }
        if(this.notReadBtn){
          
          // 未读
        
          api.messageNoReadSearchInfo(searchStr, id).then((res) => {
  
          this.tableData = res.data.data
          this.total = res.data.total
        });
        }else{
          // 已读
          
          api.messageReadSearchInfo(searchStr, id).then((res) => {
       
          this.tableData = res.data.data
          this.total = res.data.total
        });
        }
          
      }

    },
    //重置按钮 清空输入内容
    clear() {
      this.input2 = "";
      this.input1 = "";
      this.value2 = "";
    },
    // jianyuan 第三方待办  end
    changeMenuSelected() {
      this.ishiden = true;
    },

    /**
     * 点击个人设置入口按钮
     */
    handleSettingEntrance() {
      this.$store.dispatch("toggleLayoutType");
    },

    /**
     * 拖动控制器
     */
    dragControllerDiv() {
      // 获取 dom信息
      var resize = document.getElementsByClassName("resize");
      var left = document.getElementsByClassName("leftMenu");
      var mid = document.getElementsByClassName("mid");
      var right = document.getElementsByClassName("right");
      var box = document.getElementsByClassName("box");
      for (let i = 0; i < resize.length; i++) {
        // 鼠标按下事件
        resize[i].onmousedown = function (e) {
          //   console.log("鼠标按下");
          var startX = e.clientX;
          resize[i].left = resize[i].offsetLeft;
          // 鼠标拖动事件
          document.onmousemove = function (e) {
            // console.log("鼠标在移动");
            var endX = e.clientX;
            var leftW = left[i].offsetWidth;
            var moveLen = resize[i].left + (endX - startX) - leftW; // （endx-startx）=移动的距离。resize[i].left+移动的距离=左边区域最后的宽度
            var maxT = box[i].clientWidth - resize[i].offsetWidth; // 容器宽度 - 左边区域的宽度 = 右边区域的宽度

            if (moveLen < 390) moveLen = 390; // 左边区域的最小宽度为400
            if (moveLen > maxT - leftW - 300) moveLen = maxT - leftW - 300; //右边区域最小宽度为900

            resize[i].style.left = moveLen; // 设置左侧区域的宽度

            for (let j = 0; j < right.length; j++) {
              mid[j].style.width = moveLen + "px";
              right[j].style.width =
                box[i].clientWidth - moveLen - leftW - 10 + "px";
            }
            var dragDistance = moveLen - 390; //移动距离
            if (document.getElementsByClassName("subject")) {
              //主题
              //  document.getElementsByClassName('subject').style.width = (195 +  dragDistance)+'px';
              // document.getElementsByClassName("subject").forEach((element) => {
              //     element.style.width = 195 + dragDistance + "px";
              // });
              // lyy------------------------------------------------------------
              for (let [, element] of Object.entries(
                document.getElementsByClassName("subject")
              )) {
                element.style.width = 195 + dragDistance + "px";
              }
            }
            if (document.getElementsByClassName("sendto")) {
              //收件人
              //  document.getElementsByClassName('sendto').style.width = (100 +  dragDistance)+'px';
              // document.getElementsByClassName("sendto").forEach((element) => {
              //     element.style.width = 195 + dragDistance + "px";
              // });
              // lyy------------------------------------------------------------
              for (let [, element] of Object.entries(
                document.getElementsByClassName("sendto")
              )) {
                element.style.width = 195 + dragDistance + "px";
              }
            }
          };
          // 鼠标松开事件
          document.onmouseup = function () {
            // console.log("鼠标松开");
            //颜色恢复
            // resize[i].style.background = '#d6d6d6';
            document.onmousemove = null;
            document.onmouseup = null;
            resize[i].releaseCapture && resize[i].releaseCapture(); //当你不在需要继续获得鼠标消息就要应该调用ReleaseCapture()释放掉
          };
          resize[i].setCapture && resize[i].setCapture(); //该函数在属于当前线程的指定窗口里设置鼠标捕获
          return false;
        };
      }
    },

    /**
     * 是否显示 中间的容器
     */
    isallshowmidbox() {
      this.ishiden = !this.ishiden;
    },

    // //切换左侧窗格
    // switchLeftPane(){
    //   if(!this.hideLeftPane){
    //     document.querySelector("#listArea").style.display="none";
    //     this.$store.commit('showlxr', [0,24]);
    //     this.hideLeftPane = true;
    //   }else{
    //      document.querySelector("#listArea").style.display="";
    //      this.$store.commit('showlxr', [7,17]);
    //      this.hideLeftPane = false;
    //   }

    // },

    /**
     * 列表平方
     */
    listsq() {
      // document.querySelector(".mid").style.display="none";
      this.$store.commit("showlxr", [0, 24]);
    },

    /**
     * 获取信息
     */
    getinfo() {
      this.type =
        this.$refs.leftnav.$data.type != ""
          ? this.$refs.leftnav.$data.type
          : "inBox";
      this.$store.commit("itemslect", this.type);
    },

    /**
     * 添加 菜单信息
     */
    addnumc() {
      this.$refs.leftnav.addMenuNum();
    },

    /**
     * 刷新侧边导航
     */
    refnav() {
      this.$refs.leftnav.getMenuNum();
    },

    /**
     * 刷新？
     */
    sx() {
      this.$refs.cont.refsearc();
      this.$refs.cont.getlist();
      this.$refs.leftnav.getMenuNum();
    },

    cct() {
      this.refnav();
      this.sx();
    },
    refnavs() {
      //邮件发送成功需要修改面板布局
      document.querySelector(".mid").style.display = "";
      document.querySelector(".resize").style.display = "";
      document.querySelector(".right").style.width = "auto";
      this.$refs.leftnav.getMenuNum();
      this.$refs.cont.getlist();
      this.$router.push({
        path: `/`,
      });
    },

    gettime() {
      return new Date().getTime();
    },
    //一级菜单 已办待办已读未读数量
    getNumberToOne(){
      EventBus.$on("getApplicateNumber", (data) => {
      // console.log("dataeeee :>> ", data);
      let affiarNum = data.reduce((pre, cur) => {
        return pre + cur.affiarNum;
      }, 0);
      let hasPendingCount = data.reduce((pre, cur) => {
        return pre + cur.hasPendingCount;
      }, 0);
      let noReadMsgNum = data.reduce((pre, cur) => {
        return pre + cur.noReadMsgNum;
      }, 0);
      let readMsgNum = data.reduce((pre, cur) => {
        return pre + cur.readMsgNum;
      }, 0);
      this.affiarNum = affiarNum;
      this.hasPendingCount = hasPendingCount;
      this.noReadMsgNum = noReadMsgNum;
      this.readMsgNum = readMsgNum;
    });
    },
     //二级菜单 已办待办已读未读数量
    getNumberToTow(){
        EventBus.$on("getAppNumber", (data) => {
          this.affiarNum = data.affiarNum;
          this.hasPendingCount = data.hasPendingCount;
          this.noReadMsgNum = data.noReadMsgNum;
          this.readMsgNum = data.readMsgNum;
       });
    },
    
    // 获取一级菜单的 类型 index  id
    getOneinfo(){
      EventBus.$on(
          "userSetting_mailPersonalSettings_customFolder_getOneLevelMenuInfo",
          (oneLevelMenuInfo) => {
            this.oneType = oneLevelMenuInfo.type;
            this.currentIndex = oneLevelMenuInfo.index;
            this.currentId = oneLevelMenuInfo.id;
            // console.log("oneLevelMenuInfo.type :>> ", oneLevelMenuInfo);
          }
        );
    },
     
      // 获取二级菜单的 类型 index  id
       getTowinfo(){
          EventBus.$on(
      "userSetting_mailPersonalSettings_customFolder_getSubMenuInfo",
      (menuInfo) => {
        this.currentIndex = menuInfo.index;
        this.currentId = menuInfo.id;
      }
    );
       },

    // 一级菜单的 待办和未读 数量
    getOneAffiarNoReadNum(){
     
    },
  },
  

  beforeDestroy() {
   clearInterval(this.timers);
 },
  mounted() {

          // 每隔5分钟定时刷新
      
   this.timers = setInterval(() => {
     this.getFxItemlist();
   },300000 )
    // if(this.$store.state.currentID.length > 1){
     
    // }else{

    // }
    this.getOneAffiarNoReadNum()

    this.dragControllerDiv();
    this.dialogFormVisible = false;


    // 获取一级菜单的 类型 index  id
    this.getOneinfo()

    // 获取二级菜单的 类型 index  id
    this.getTowinfo()
   
    //获取一级菜单 已办待办已读未读数量
    this.getNumberToOne()

    //获取二级菜单 已办待办已读未读数量
    this.getNumberToTow()

    //获取第三方待办 菜单
    // this.getNewMenusList();
    //获取第三方待办 数据
    this.getSecendLeveData();
    this.getOneLeveData();

  },
};
</script>
<style>
@import "~@/../public/css/font.css";
/* 消息弹框的样式 start */
/* .topPart{
  width: 100%;
  background-color: #fff;
  height: 20%;
  display: flex;
  justify-content: end;
  border-radius: 9px 9px 0 0;
  position: relative;
 
} */
.topPart{
  width: 100%;
    background-color: #fff;
    height: 15%;
    border-radius: 9px 9px 0 0;
    position: relative;
    padding-left: 42px;
    box-sizing: border-box;
    margin-bottom: 20px;
}
.topPart .btn{
  height: 19%;
}
.topPart span{
  font-size: 1.66vh;
  cursor: pointer;
  width: 2.08vw;
  height: 3.7vh;
  line-height: 40px;
  text-align: center;
}
.topPart span:hover{
  scale: 1.5;
  transition: all 0.3s;
}
.closeX{
  font-size: 1.66vh;
  cursor: pointer;
  width: 2.08vw;
  height: 3.7vh;
  line-height: 40px;
  text-align: center;
  border: none !important;
  font-size: 16px !important;
  padding: 2px 5px !important;
  position: absolute;
  right: 0;

}
.closeX:hover{
  background-color: #f78989 !important;
  border-radius: 0 !important;
  color: #fff !important;
 
}
.infomation{
  font-size: 14px;
  position: relative; 
  /* margin-bottom: 30px; */
}
.infomation::after{
  /* content: ''; */
  display: block;
  position: absolute;
  bottom: -15px;
  margin-left: 5px;
  width: 99%;
  border-bottom: 1px solid #dcdfe6;
}
.infomation p{
  margin: 5px;
  
}
.fromRegion{
  font-size: 15px;
  font-weight: bold;
}
.bottomPart{
  width: 100%;
  background-color: #fff;
  height: 7%;
  display: flex;
  justify-content: center;
  align-items: center;
  border-radius: 0 0 9px 9px;


}
.bottomPart button{
  width: 5.2vw;
  height: 3.7vh;
  border: 1px solid #dcdfe6;
  background-color: #ffffff;
  border-radius: 4px;
  cursor: pointer;
  
}
.bottomPart button:hover{
  background-color: #f78989;
  color: #fff;
  border: none;
}

.contentHtml  p{
  margin: 5px !important;
}
/* 消息弹框的样式 end */
.readMessageDielog{
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.7);
  position: absolute;
  top: 0;
  left: 0;
  z-index: 99;
  /* pointer-events: none; */
}
.containerBox{
  z-index: 999;
  position: absolute;
  width: 80%;
  height: 80%;
  background-color: #fff;
  margin: auto;
  margin-top: 3%;
  top: 0;
  left: 10%;
  border-radius: 9px;
  /* overflow: auto ; */
  /* padding: 20px; */
  
}

.infoBox{
  background-color: #fff;
    border: 1px solid #dadada;
    overflow: auto;
    border-radius: 6px;
    padding: 8px;
    height: 75%;
    width: 93%;
    color: #000000;
    font-size: 16px;
    font-family: sans-serif;
    margin-left: 2.7%;
}
.infoBox::-webkit-scrollbar{
  width: 5px;
  height: 10px;
}
.containerBox::-webkit-scrollbar{
  width: 5px;
  height: 10px;
}
.contentTitle{
  padding-top: 20px;
  font-size: 20px;
  font-weight: bold;
  margin-left: 20px;

}
.time{
  margin-left: 20px;
}

.contentText{
  font-size: 16px;
  /* margin-top: 40px; */
  /* margin-left: 40px; */
  background-color: #fff;
  height: 600px;
  width: 1470px;
  border-radius: 10px;
  padding: 10px;
}
.contentText p{
  font-size: 16px;
  font-weight: bold;

  
}
#wangeditor{
  /* width:400px; */
  /* height:600px; */
  position:absolute;
  top:0;
  z-index:55;
}

/* 拖拽相关样式 */
/*包围div样式*/
.box {
  width: 100%;
  height: 100%;
  /* margin: 1% 0px; */
  /* overflow: hidden;
        box-shadow: -1px 9px 10px 3px rgba(0, 0, 0, 0.11); */
}

/*左侧div样式*/
.leftMenu {
  width: 201px;
  /*左侧初始化宽度width: calc(18% - 10px);    */
  height: calc(100% - 45px);
  /* background:skyblue; */
  float: left;
}

/*拖拽区div样式*/
.resize {
  cursor: col-resize;
  float: left;
  position: relative;
  top: 45%;
  background-color: #d6d6d6;
  border-radius: 5px;
  margin-top: -10px;
  width: 0px;
  height: 0px;
  background-size: cover;
  background-position: center;
  /*z-index: 99999;*/
  font-size: 32px;
  color: white;
}

/*拖拽区鼠标悬停样式*/
.resize:hover {
  color: #444444;
}

/*左侧div'样式*/
.mid {
  float: left;
  /* width: 30%;   左侧初始化宽度 先屏蔽*/
  width: 390px;
  height: 100%;
  background: #fff;
  box-shadow: -1px 4px 5px 3px rgba(0, 0, 0, 0.11);
}

.right {
  float: left;
  width: 70%;
  height: 100%;
  flex: 1;
  /**先试试 */
  /* background: #f00;; */
}

.el-main {
  position: relative;
}

/* 不可操作 蒙层 */
.el-main .not-operable-mask {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  background-color: transparent;
  z-index: 100;
}

.el-submenu .el-menu-item {
  height: auto !important;
  padding: 10px 45px !important;
  line-height: 40px !important;
  white-space: normal !important;
}

.el-header {
  width: 100%;
  padding: 0 40px !important;
  display: flex;
  justify-content: space-between;
  align-items: center;
  display: none;
}

.el-header .setting-page-entrance {
  cursor: pointer;
}

.title {
  padding: 10px 15px;
  font-size: 18px;
  height: 24px;
  border-bottom: 1px solid #e4e8eb;
}
/* jianyuan 第三方待办 start */
.el-main {
  overflow: hidden !important;
}
.el-checkbox__input.is-checked + .el-checkbox__label {
  color: #c63f3d !important;
}
.el-button--mini,
.el-button--mini.is-round {
  margin-left: 12px;
}
.serach_title {
  display: inline-block;
  width: 80px;
  text-align: center;
}
.search_input {
  display: flex;
  align-items: center;
  width: 434px;
}
.search_part {
  display: flex;
  margin-right: 20px;
}
.search_btn {
  display: inline-block;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #606266;
  -webkit-appearance: none;
  text-align: center;
  box-sizing: border-box;
  outline: 0;
  margin: 0;
  transition: 0.1s;
  font-weight: 500;
  padding: 12px 20px;
  font-size: 16px;
  border-radius: 4px;
  width: 112px;
  margin-right: 20px;
}
.search_btn:hover {
  background-color: #c63f3d !important;
  border-color: #c63f3d !important;
  color: #fff !important;
}
.table_del_btn {
  background-color: #c63f3d !important;
  border-color: #c63f3d !important;
  margin-left: 12px !important;
}
.el-input.is-active .el-input__inner,
.el-input__inner:focus {
  border: #c63f3d 1px solid !important;
}
.el-pager li.active,
.el-select-dropdown__item.selected {
  color: #c63f3d !important;
}
.el-range-editor.is-active,
.el-range-editor.is-active:hover,
.el-select .el-input.is-focus .el-input__inner {
  border: #c63f3d 1px solid !important;
}
.el-pager li:hover {
  color: #c63f3d !important;
}
.eidt_btn:hover {
  border: #c63f3d 1px solid !important;
  color: #c63f3d !important;
  background-color: #f5f7fa !important;
}

.info_btn {
  border: none !important;
  color: #3a3a3a !important;
  height: 60px !important;
  font-size: 20px !important;
  margin-right: 10px !important;
  border-radius: 8px !important;
  min-width: 200px !important;
}
.info_btn:hover {
  background-color: #c63f3d !important;
  color: #fff !important;
}
.isActived {
  background-color: #c63f3d !important;
  color: #fff !important;
}
.el-checkbox__inner:hover {
  color: #c63f3d !important;
  border-color: #c63f3d !important;
}
.third_to_do_info {
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  z-index: 99;
  background-color: #f2f5f8;
  padding: 15px;
}
.top_part {
  padding: 20px 0;
  margin-bottom: 10px;
  border-radius: 8px;
  width: 96%;
}
.info_number {
  margin-left: 20px;
  font-size: 16px;
  font-weight: bold;
}
.mid_part {
  display: flex;
  background-color: #fff;
  padding: 20px;
  margin-bottom: 25px;
  border-radius: 8px;
  width: 96%;
  justify-content: space-between;
}
.foot_part {
  margin-bottom: 20px;
}

.filtrate {
  height: 39px;
}
.left_part{
  float: right;
}
.right_patr {
  display: flex;
}
/* .el-input--prefix .el-input__inner {
  padding-left: 30px !important;
} */
.search_text {
  margin-right: 20px;
  width: 350px !important;
}
.table {
  padding: 20px;
  background-color: #fff;
  border-radius: 8px;
  width: 96%;
  position: relative;
  height: 68%;
}
.table .el-table__body-wrapper.is-scrolling-none {
  height: 91% !important;
}
.table   /deep/ .el-table .el-table__cell.gutter{
  background-color: #eef1f6;
  border: none !important;
}
.pagination {
  position: absolute;
  bottom: 30px;
  right: 30px;
}
.del_btn {
  margin-right: 30px !important;
  margin-left: 0px !important;
  background-color: #c63f3d !important;
  border-color: #c63f3d !important;
}
.el-checkbox__input.is-checked .el-checkbox__inner,
.el-checkbox__input.is-indeterminate .el-checkbox__inner {
  background: #c63f3d !important;
  border-color: #c63f3d !important;
}
.el-checkbox__input.is-checked .el-checkbox__inner,
.el-checkbox__input.is-indeterminate .el-checkbox__inner:hover {
  border-color: #c63f3d !important;
}
/* .table el-table /deep/.el-table__header-wrapper */
/* jianyuan 第三方待办 end */

html,
body {
  margin: 0;
  padding: 0;
}

html,
body,
#app,
.el-container {
  /*设置内部填充为0，几个布局元素之间没有间距*/
  padding: 0px;
  /*外部间距也是如此设置*/
  margin: 0px;
  /*统一设置高度为100%*/
  height: 100%;
  position: relative;
}

.search_part > .el-input__inner {
  /* box-sizing: border-box !important; */
  /* padding: 0 30px 0 15px !important; */
  width: 350px !important;
}
.date_search .el-input__inner {
  width: 354px !important;
}
/* ul,
li {
  padding: 0;
  margin: 0;
  list-style: none;
} */

a {
  text-decoration: none;
}

.editor {
  height: 100%;
}

.ql-editor {
  /*height: 52vh !important*/
  /*height: 70vh !important*/
}

.editor {
  line-height: normal !important;
}

.ql-container {
  height: 95% !important;
}

.ql-snow .ql-tooltip[data-mode="link"]::before {
  content: "请输入链接地址:";
}

.ql-snow .ql-tooltip.ql-editing a.ql-action::after {
  border-right: 0px;
  content: "保存";
  padding-right: 0px;
}

.el-dialog__wrapper {
  z-index: 1000 !important;
}

.v-modal {
  z-index: 999 !important;
}

.ql-snow .ql-tooltip[data-mode="video"]::before {
  content: "请输入视频地址:";
}

.ql-snow .ql-picker.ql-size .ql-picker-label::before,
.ql-snow .ql-picker.ql-size .ql-picker-item::before {
  content: "14px";
}

.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="small"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="small"]::before {
  content: "10px";
}

.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="large"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="large"]::before {
  content: "18px";
}

.ql-snow .ql-picker.ql-size .ql-picker-label[data-value="huge"]::before,
.ql-snow .ql-picker.ql-size .ql-picker-item[data-value="huge"]::before {
  content: "32px";
}

.ql-snow .ql-picker.ql-header .ql-picker-label::before,
.ql-snow .ql-picker.ql-header .ql-picker-item::before {
  content: "文本";
}

.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="1"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="1"]::before {
  content: "标题1";
}

.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="2"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="2"]::before {
  content: "标题2";
}

.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="3"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="3"]::before {
  content: "标题3";
}

.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="4"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="4"]::before {
  content: "标题4";
}

.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="5"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="5"]::before {
  content: "标题5";
}

.ql-snow .ql-picker.ql-header .ql-picker-label[data-value="6"]::before,
.ql-snow .ql-picker.ql-header .ql-picker-item[data-value="6"]::before {
  content: "标题6";
}

.ql-snow .ql-picker.ql-font .ql-picker-label::before,
.ql-snow .ql-picker.ql-font .ql-picker-item::before {
  content: "标准字体";
}

.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="serif"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="serif"]::before {
  content: "衬线字体";
}

.ql-snow .ql-picker.ql-font .ql-picker-label[data-value="monospace"]::before,
.ql-snow .ql-picker.ql-font .ql-picker-item[data-value="monospace"]::before {
  content: "等宽字体";
}

.hand {
  cursor: pointer;
}
</style>
