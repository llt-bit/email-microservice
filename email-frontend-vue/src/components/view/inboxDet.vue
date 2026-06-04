<template>
  <!-- AI助手功能：START 根节点class绑定 -->
  <div class="inboxDet" :class="{ 'has-ai-panel': aiPanelVisible }">
  <!-- AI助手功能：END 根节点class绑定 -->
    <!-- <div class="scllorBox">
      <div class="scllorBoxSon"></div>
    </div> -->
    <div class="inboxDet-main">
    <viewer :images="imglist" ref="viewer" v-show="false">
      <img v-for="(src, index) in imglist" :src="src.src" :key="index" />
    </viewer>
    <div v-if="!isTrue" class="tlite" style="position: relative">
      <span
        style="position: absolute; left: 0em"
        @click="isall"
        class="hand"
        :title="ishidenbox ? '展开' : '收起'"
      >
        <i class="el-icon-full-screen" v-if="ishidenbox"></i>
        <i class="el-icon-copy-document" v-else></i>
      </span>
      {{ data.subject }}
      <span style="margin-left: auto; padding-right: 2em" v-show="isshowtip">
        <el-tooltip
          class="item"
          effect="dark"
          content="审批进度"
          placement="top"
        >
          <!--            <i class="iconfont hand "  @click="hf" v-show="itemum == 'inBox'">回复</i>-->
          <el-button
            type="info"
            icon="el-icon-chat-dot-round"
            v-show="itemum == 'sent'"
            @click="geturl"
            size="mini"
            >审批进度</el-button
          >
        </el-tooltip>
        <el-tooltip class="item" effect="dark" content="回复" placement="top">
          <!--            <i class="iconfont hand "  @click="hf" v-show="itemum == 'inBox'">回复</i>-->
          <!-- 发件箱中审核通过展示回复、回复全部按钮 && $store.state.passTheAudit == 1 fuguichaun-->
          <el-button
            type="info"
            icon="el-icon-chat-dot-round"
            @click="hf"
            v-show="itemum == 'inBox' || itemum == 'collection' || itemum == 'sent' && $store.state.passTheAudit == 1"
            size="mini"
            >回复</el-button
          >
        </el-tooltip>
        <el-tooltip
          class="item"
          effect="dark"
          content="回复全部"
          placement="top"
        >
          <!--            <i class="iconfont hand " style="margin-left: 0.5em"  @click="allhf" v-show="itemum == 'inBox'">回复全部</i>-->
          <!-- 发件箱中审核通过展示回复、回复全部按钮 && $store.state.passTheAudit == 1 fuguichaun-->
          <el-button
            type="success"
            icon="el-icon-chat-line-round"
            size="mini"
            @click="allhf"
            v-show="itemum == 'inBox' || itemum == 'collection' || itemum == 'sent' && $store.state.passTheAudit == 1"
            >回复全部</el-button
          >
        </el-tooltip>
        <el-tooltip class="item" effect="dark" content="编辑" placement="top">
          <!--            <i class="iconfont hand " @click="tobj" v-show="itemum == 'draft'">编辑</i>-->
          <el-button
            type="primary"
            icon="el-icon-edit"
            @click="tobj"
            v-show="itemum == 'draft'"
            size="mini"
            >编辑</el-button
          >
        </el-tooltip>
        <el-tooltip
          class="item"
          effect="dark"
          content="再次编辑"
          placement="top"
        >
          <!--            <i class="iconfont hand " @click="tozcbj" v-show="itemum == 'sent'">再次编辑</i>-->
          <el-button
            type="warning"
            icon="el-icon-edit"
            @click="tozcbj"
            v-show="itemum == 'sent'"
            size="mini"
            >再次编辑</el-button
          >
        </el-tooltip>
        <el-tooltip class="item" effect="dark" content="转发" placement="top">
          <!--         <i class="iconfont hand " v-show="itemum != 'draft'"  style="margin-left: 0.5em" @click="topoth">转发</i>-->
          <el-button
            type="Success"
            icon="el-icon-position"
            size="mini"
            v-show="itemum != 'draft'"
            @click="topoth"
            >转发</el-button
          >
        </el-tooltip>
      </span>
    </div>

    <div style="height: calc(100vh - 75px)">
      <div
      class="container"
        style="
          border-radius: 4px;
          background: white;
          border: 1px solid #ebeef0;
          padding: 10px;
          height: calc(100vh - 75px);
          overflow: scroll;
          margin: 20px 20px 0;
          box-sizing: border-box;
        "
      >
        <div class="msghead">
          <div class="imgtx">
            <img
              :src="data.headImg"
              @click="sqlxr"
              alt=""
              style="width: 100%; height: 100%; border-radius: 50%"
            />
          </div>

          <p style="font-size: 14px; font-weight: 600">
            {{
              data.startDetail[1] +
              " (" +
              data.startDetail[data.startDetail.length - 2] +
              " " +
              data.startDetail[data.startDetail.length - 1] +
              ")"
            }}
          </p>

          <span style="margin-left: auto; font-size: 12px; color: #999999" >
            <!-- 如果当前邮件不涉密那么将密级颜色改为#508ff5 提高对比度 tangxiangping 2022-06-07 start -->
            <span v-if="data.secretNameStr === '公开' || data.secretNameStr === '内部'" style="color: #508ff5; margin-right: 0.5em">{{ data.secretNameStr }}</span >
            <span v-else style="color: #ff5722; margin-right: 0.5em">{{ data.secretNameStr }}</span >
            <!-- 如果当前邮件不涉密那么将密级颜色改为#508ff5 提高对比度 tangxiangping 2022-06-07 start -->
            {{ data.createDate }}
          </span >
        </div>
        <div style="padding-left: 4.5em; font-size: 13px; color: #999999">
          <div style="margin: 0 0 10px 0">
            收件人：
            <!-- lyy 原:display: inline-block" -->
            <div style="color: #343434; display: inline">
              <template v-for="(item, index) in data.send">
                <!-- 遍历收件人进行分支操作
                  如果字段里面有部门信息 则对部门信息进行操作
                  如果没有则是Team组，直接对组进行操作
                -->
                <span :key="item.id" v-if="item.department">
                  {{ item.text
                  }}{{
                    data.send.length - 1 === index
                      ? "(" + item.department + ")"
                      : "(" + item.department + "),"
                  }}
                </span>
                <span :key="item.id" v-else>
                  <el-popover
                    placement="bottom"
                    width="500"
                    trigger="click"
                    :ref="`popover-${item.id}`"
                  >
                    <!-- 对逗号进行判断 -->
                    <div>
                      <div class="title" v-html="popoverText"></div>
                      <el-table
                        :data="popoverTableData"
                        stripe
                        header-cell-class-name="tabhead"
                        height="300px"
                        style="width: 100%"
                      >
                        <el-table-column
                          align="center"
                          property="deptName"
                          label="部门"
                          width="160"
                        ></el-table-column>
                        <el-table-column
                          align="center"
                          property="name"
                          label="姓名"
                          width="160"
                        ></el-table-column>
                        <el-table-column
                          align="center"
                          property="loginName"
                          label="登录名"
                          width="160"
                        ></el-table-column>
                      </el-table>
                    </div>
                    <div
                      slot="reference"
                      style="
                        cursor: pointer;
                        user-select: none;
                        text-decoration: underline;
                        display: inline-block;
                      "
                      @click="getGroupUserInfo(summaryId , item.id, item.text)"
                    >
                      {{
                        data.send.length - 1 === index
                          ? item.text
                          : item.text + ","
                      }}
                    </div>
                  </el-popover>
                </span>
              </template>
            </div>
          </div>
          <div style="margin: 0 0 10px 0">
            抄送人：
            <!-- lyy 原:display: inline-block" -->
            <div style="color: #343434; display: inline">
              <template v-for="(item, index) in data.csr">
                <span :key="item.id" v-if="item.department">
                  {{ item.text
                  }}{{
                    data.csr.length - 1 === index
                      ? "(" + item.department + ")"
                      : "(" + item.department + "),"
                  }}
                </span>
                <span :key="item.id" v-else>
                  <el-popover
                    placement="bottom"
                    width="500"
                    trigger="click"
                    :ref="`popover-${item.id}`"
                  >
                    <!-- 对逗号进行判断 -->
                    <div>
                      <div class="title" v-html="popoverText"></div>
                      <el-table
                        :data="popoverTableData"
                        stripe
                        header-cell-class-name="tabhead"
                        height="300px"
                        style="width: 100%"
                      >
                        <el-table-column
                          align="center"
                          property="deptName"
                          label="部门"
                          width="160"
                        ></el-table-column>
                        <el-table-column
                          align="center"
                          property="name"
                          label="姓名"
                          width="160"
                        ></el-table-column>
                        <el-table-column
                          align="center"
                          property="loginName"
                          label="登录名"
                          width="160"
                        ></el-table-column>
                      </el-table>
                    </div>
                    <div
                      slot="reference"
                      style="
                        cursor: pointer;
                        user-select: none;
                        text-decoration: underline;
                        display: inline-block;
                      "
                      @click="getGroupUserInfo(summaryId  , item.id, item.text)"
                    >
                      {{
                        data.csr.length - 1 === index
                          ? item.text
                          : item.text + ","
                      }}
                    </div>
                  </el-popover>
                </span>
              </template>
            </div>
            <p style="margin: 10px 0 0" v-if="data.attachs.length > 0">
              {{ "共" + data.attachs.length + "个附件：" }}
              <span
                style="color: #4a8af5; cursor: pointer; user-select: none"
                @click="downloadAllFiles(data.attachs)"
                v-if="data.attachs.length > 1"
                >全部下载</span
              >
              <span class="hand"
                >（
                <span
                  v-for="(i, index) in data.attachs"
                  :key="index"
                  style="position: relative"
                >
                  <span @click="downflie(i)">
                    <img
                      style="position: absolute"
                      :src="'/seeyon/common/images/attachmentICON/' + i.icon"
                    />
                    <span style="padding-left: 1em"> {{ i.filename }}</span>
                  </span>
                  <span
                    style="padding: 0 5px"
                    v-show="!(data.attachs.length == index + 1)"
                    >,</span
                  >
                  <span
                    style="padding-left: 1em; color: #4a8af5"
                    @click="_previewType(i)"
                    v-show="
                      [
                        'doc',
                        'docx',
                        'xls',
                        'xlsx',
                        'ppt',
                        'pptx',
                        'pdf',
                        'jpg',
                        'jpeg',
                        'png',
                      ].includes(i.extension)
                    "
                    >预览附件</span
                  >
                </span> </span
              >）
            </p>
          </div>
          <!-- 下载介质(表单自定义控件都在用)预览 -->
          <iframe
            ref="myFormIframe"
            id="myFormIframe"
            style="display: none"
          ></iframe>
          <el-divider></el-divider>

          <div class="vhtmlBoxScllor" >
            <div class="info"
                        v-html="data.content">

                    </div>

          </div>
          <!-- <vue-ueditor-wrap
            v-model="data.content"
            :config="myConfig"
            @click="clearPopover"
          ></vue-ueditor-wrap> -->

          <div style="display: none">
            <el-divider></el-divider>
            <div class="msginfo" style="padding: 0 20px; margin-bottom: 1em">
              <el-row :gutter="15">
                <div style="font-size: 14px; margin-bottom: 10px">
                  <i class="el-icon-paperclip" style="color: #4e4e4e"></i>
                  <span style="font-size: 13px">附件1个（8MB）</span>
                </div>
                <el-col :span="8">
                  <div
                    style="
                      width: 100%;
                      background: #efefef;
                      border-radius: 4px;
                      height: 3.5em;
                    "
                  ></div>
                </el-col>
              </el-row>
            </div>
          </div>
        </div>
      </div>
    </div>
    </div><!-- end inboxDet-main -->

    <!-- AI助手功能：START AI面板组件和切换按钮 -->
    <!-- 2026-06-01 AI助手 start: 模板prop改动——传函数替代传值+新增generating事件和class绑定 -->
    <!-- email-content从(:email-content="fn()")改为(:get-email-content="fn") -->
    <!-- 新增@generating事件收发AI面板生成状态，toggle按钮新增generating class绑定呼吸灯 -->
    <ai-assistant-panel
      :visible="aiPanelVisible"
      scene="detail"
      :email-subject="data.subject"
      :get-email-content="getEmailTextContent"
      @close="aiPanelVisible = false"
      @insert="handleAiInsert"
      @fill-reply="handleAiFillReply"
      @generating="onAiGenerating"
    ></ai-assistant-panel>

    <!-- 2026-06-02 AI助手 start: 按钮图标换为a.png背景图，旧芯片图标span已移除 -->
    <!-- 原代码：<span class="ai-chip-icon"><span class="ai-chip-core"></span></span> -->
    <div class="ai-toggle-btn" :class="{ active: aiPanelVisible, generating: aiGenerating }" @click="aiPanelVisible = !aiPanelVisible" :title="aiPanelVisible ? '关闭AI助手' : '打开AI助手'">
    </div>
    <!-- 2026-06-02 AI助手 end -->
    <!-- AI助手功能：END AI面板组件和切换按钮 -->
  </div>
</template>

<script>
import api from "@/api/api";
// AI助手功能：START 组件导入
import AiAssistantPanel from "@/components/AiAssistantPanel";
// AI助手功能：END 组件导入
// import VueUeditorWrap from "vue-ueditor-wrap";

export default {
  name: "inboxDet",
  props: {
    ishidenbox: {
      required: false,
    },
  },
  components: {
    // AI助手功能：START 组件注册
    AiAssistantPanel,
    // AI助手功能：END 组件注册
    // VueUeditorWrap,
  },
  data() {
    return {

      imglist: [],
      id: "",
      itemum: "",
      isshowtip: "",

      data: {
        content: "",
        subject: "",
        attachs: [],
        //抄送
        copyToDetail: [],
        // //收件
        sendToDetail: [],
        // //发送
        startDetail: [],
      },
      isTrue: false,
      // summaryId
      summaryId: "",
      myConfig: {
        forceInit: true,
        enableAutoSave: false,
        // 编辑器不自动被内容撑高
        autoHeightEnabled: true,
        // 初始容器高度
        initialFrameHeight: 700,
        // 初始容器宽度
        initialFrameWidth: "100%",
        readonly: true,
        // 上传文件接口（这个地址是我为了方便各位体验文件上传功能搭建的临时接口，请勿在生产环境使用！！！）
        serverUrl: "",
        // UEditor 资源文件的存放路径，如果你使用的是 vue-cli 生成的项目，通常不需要设置该选项，vue-ueditor-wrap 会自动处理常见的情况，如果需要特殊配置，参考下方的常见问题2

        // '/seeyon/apps_res/plugin/newInternalmail//UE/' 为打包地址
        //  本地实际地址为'/UE/'
        UEDITOR_HOME_URL: "./UE/",
        // UEDITOR_HOME_URL: '/UE/',
        toolbars: [],
      },
      // popover展示的标题
      popoverText: "",
      // popover展示的表格信息
      popoverTableData: [],
      editor: null,
        html: '',
        toolbarConfig: {
          /* 显示哪些菜单，如何排序、分组 */
          toolbarKeys: [
            'headerSelect',
            // '|',
            'bold',
            'underline',
            'italic',
            'color',
            'bgColor',
            // '|',
            'indent',  // 增加缩进
            'delIndent',  // 减少缩进
            'justifyLeft',  // 左对齐
            'justifyRight',  // 右对齐
            'justifyCenter',  // 居中
            'justifyJustify',  // 两端对齐
            // '|',
            'fontSize',
            'fontFamily',
            'lineHeight',
            // '|',
            'bulletedList',
            'numberedList',
            'todo',
            'insertLink',
            'insertTable',
            // '|',
            'codeBlock',
            'divider',
            'uploadImage',
            'undo',
            'redo',
          ],
          // excludeKeys: [ ], /* 隐藏哪些菜单 */
        },
        editorConfig: {
          placeholder: "请输入内容",
          // autoFocus: false,
          // readOnly: true, // 只读、不可编辑
          // 所有的菜单配置，都要在 MENU_CONF 属性下
          MENU_CONF: {
            // 配置上传图片
            uploadImage: {
              customUpload: this.uploaadImg
            },
          },
        },

      // AI助手功能：START 数据属性
      // 2026-06-01 AI助手 start: 新增aiGenerating——AI生成状态标记，控制悬浮按钮呼吸灯
      aiPanelVisible: false,
      aiGenerating: false
      // 2026-06-01 AI助手 end
      // AI助手功能：END 数据属性

    };
  },

  created() {
    this.getid();
    this.getinfo();
    this.getNewMenusList()//获取第三方菜单
    this.itemum = this.$store.state.itemnum || "inBox";
    const urlList = window.location.href.split("/");
    this.isTrue = urlList[urlList.length - 1] == "true" ? true : false;
  },
  mounted() {
    this.gettypeshow();

    //初始化v3x
    // window.v3x = new V3X();
  },
  // updated() {
  //   this.setTableBorder();
  // },
  methods: {
    //第三方待办菜单
    getNewMenusList() {
      this.$store.dispatch("getNewMenus");
    },
    // 全屏展示邮件
    isall() {
      this.$emit("isallshowmid", !this.isallshow);
      this.isallshow = !this.isallshow;
      // 查看邮件 放大-缩小 界面显示右上角按钮不一致  fuguichuan
      this.$store.commit('itemslect',this.itemum)
    },
    //预览文件
    _previewType(file) {
      //文件类型
      if (["png", "jpg", "jpeg", "gif", "bmp"].includes(file.extension)) {
        const datas = [];
        datas.push({
          src: `/api/attachment/download/${file.fileUrl}&createDate=${file.createdate}&type=image&showType=big`,
          originalsrc: `/api/attachment/download/${file.fileUrl}&createDate=${file.createdate}&type=image`,
        });

        // window.$.touch({
        //   id: new Date().getTime(),
        //   datas,
        //   currentIndex:  0
        // })
        // parent.$.touch({
        //   id: new Date().getTime(),
        //   datas,
        //   currentIndex:  0
        // })
        //oa 原生图片展示
        // parent.$.touch({
        //   id: new Date().getTime(),
        //   datas,
        //   currentIndex:  0
        // })
        //
        this.imglist = datas;
        const viewer = this.$refs["viewer"].$viewer;
        this.$nextTick(() => {
          viewer.show();
        });
      } else if (file.extension === "pdf") {
        const url = `/seeyon/fileDownload.do?method=doDownload4Office&type=Pdf&isOpenFile=true&fileId=${
          file.fileUrl
        }&createDate=${file.createdate}&filename=${encodeURIComponent(
          file.filename
        )}&v=${file.v}${window._mine_getUrlSurffix}`;
        // window.addattachDialog = null;
        // window.addattachDialog = window.$.dialog({
        //     title: '查看',
        //     transParams: { parentWin: window },
        //     url,
        //     width: 1280,
        //     height: 800
        // });
        parent.addattachDialog = null;
        parent.addattachDialog = parent.$.dialog({
          title: "查看",
          transParams: { parentWin: parent },
          url,
          width: 1280,
          height: 800,
        });
      } else {
        const viewUrl = `/seeyon/officeTrans.do?method=view&fileId=${
          file.fileUrl
        }&createDate=${file.createdate}&v=${file.v}&filename=${encodeURI(
          file.filename
        )}`;
        this.$refs.myFormIframe.src = viewUrl;
      }
    },
    geturl() {
      api.getShurl(this.id).then((res) => {
        if (res.msg) {
          window.open(
            res.msg,
            "_blank",
            "width=" +
              (window.screen.availWidth - 10) +
              ",height=" +
              (window.screen.availHeight - 70) +
              ",top=0,left=0,toolbar=no,menubar=no,scrollbars=no, resizable=yes,location=no, status=no"
          );
        } else {
          this.$message.warning("此任务无需审批");
        }
      });
    },
    sqlxr() {
      this.$store.commit("showlxr", [0, 24]);
    },
    gettypeshow() {
      this.$emit("gettypeitem");
    },
    topoth() {
      this.$router.push({
        path: "/editor",
        query: {
          obj: {
            secretTypeId: this.data.secretIdStr,
            summaryId: this.data.summaryId,
            affairId: this.data.affairId,
            type: "forwordMail",
            mark: "forwordMai",
            from: "",
          },
        },
      });
    },
    hf() {
      this.$router.push({
        path: "/editor",
        query: {
          obj: {
            secretTypeId: this.data.secretIdStr,
            summaryId: this.data.summaryId,
            affairId: this.data.affairId,
            type: "replyMail",
            mark: "reply",
            from: "",
          },
        },
      });
    },
    allhf() {
      this.$router.push({
        path: "/editor",
        query: {
          obj: {
            secretTypeId: this.data.secretIdStr,
            summaryId: this.data.summaryId,
            affairId: this.data.affairId,
            type: "replyMail",
            mark: "allReply",
            from: "",
          },
        },
      });
    },
    tobj() {
      this.$router.push({
        path: "/editor",
        query: {
          obj: {
            secretTypeId: this.data.secretIdStr,
            summaryId: this.data.summaryId,
            affairId: this.data.affairId,
            type: "editMail",
            editMailSend: "1",
            from: "",
          },
        },
      });
    },
    tozcbj() {
      this.$router.push({
        path: "/editor",
        query: {
          obj: {
            secretTypeId: this.data.secretIdStr,
            summaryId: this.data.summaryId,
            affairId: this.data.affairId,
            type: "editMail",
            editMailSend: "yes",
            from: "",
          },
        },
      });
    },
    getinfo() {
      this.isshowtip = true;
      // 做全展示预留代码，已失效
      api.emliaInfo(this.id).then((res) => {
        if (res.code == "00010001") {
          // 设置summaryId
          this.$store.commit('setPassTheAudit',res.msg.passTheAudit)
          this.summaryId = res.msg.summaryId || "";
          // 处理数据
          res.msg.copyToNames = res.msg.copyToNames || "无";
          res.msg.copyToDetail == null ? (res.msg.copyToDetail = []) : (res.msg.copyToDetail = res.msg.copyToDetail.split(","));
          res.msg.startDetail = res.msg.startDetail.split("-");
          res.msg.sendToDetail == null ? (res.msg.sendToDetail = []) : (res.msg.sendToDetail = res.msg.sendToDetail.split(","));
          this.$emit("refleftnavs");

          this.data = res.msg;
          // 将搜索内容高亮显示 start
          this.$store.commit('setDetailContent', res.msg.content)
          const quickSearchInputValue =  this.$store.state.quickSearchInputValue;
          if (quickSearchInputValue) {
            this.data.content = res.msg.content.replace(new RegExp(quickSearchInputValue, 'g'), `<span style="background: yellow;" class="custom-style">${quickSearchInputValue}</span>`);
          }
          // 将搜索内容高亮显示 end
          // 收件人
          let arrsend = [];
          // 存放收件人信息的id集合
          if (res.msg.receivers && this.data.sendToDetail) {
            let sendIds = res.msg.receivers.split(",");
            this.data.sendToDetail.forEach((e, index) => {
              e = e.split("-");
              if (e[0] == "Member") {
                // arrsend.push(e[1] + "(" + e[3] + " " + e[4] + ")");
                // 构建数组信息
                arrsend.push({
                  text: e[1],
                  department: e[3] + " " + e[4],
                  id: sendIds[index],
                });
              } else {
                arrsend.push({
                  text: e[1],
                  id: sendIds[index],
                });
              }
            });
          }
          //抄送人
          let arr = [];
          // 存放抄送人信息的id集合
          if (res.msg.copyReceivers && this.data.copyToDetail) {
            let copyIds = res.msg.copyReceivers.split(",");
            this.data.copyToDetail.forEach((e, index) => {
              e = e.split("-");
              if (e[0] == "Member") {
                // arr.push(e[1] + "(" + e[3] + " " + e[4] + ")");
                // 构建数组信息
                arr.push({
                  text: e[1],
                  department: e[3] + " " + e[4],
                  id: copyIds[index],
                });
              } else {
                // arr.push(e[1]);
                arr.push({
                  text: e[1],
                  id: copyIds[index],
                });
              }
            });
          }

          // 收件人
          // this.data["send"] = arrsend.toString();
          this.data["send"] = arrsend;
          // 抄送人
          // this.data["csr"] = arr.toString();
          this.data["csr"] = arr;
        } else {
          this.$message.error(res.msg);
          this.$router.push("/");
          this.$emit("refleftnavs");
        }
      });
    },

    // 处理表格信息
    setTableBorder() {
      const tableList = [...document.querySelectorAll("table")];
      if (tableList.length != 0) {
        tableList.forEach((table) => {
          table.removeAttribute("border");
          table.style.margin = "10px 0";
          table.style.border = "1px solid #ccc";
          const list = [...table.querySelectorAll("td")];
          list.forEach((item) => {
            if (
              !item.getAttribute("style") ||
              item.getAttribute("style").indexOf("padding: 5px") == -1
            ) {
              item.setAttribute( "style", "border-bottom: 1px solid rgb(221, 221, 221); border-right: 1px solid rgb(221, 221, 221); padding: 5px" );
            }
          });
        });
      }
    },
    // 附件批量下载
    downloadAllFiles() {
      document.getElementById( "downloadIframe" ).src = `/api/email/packFile/${this.summaryId}`;
      // window.location.href = encodeURI(
      //   `/api/email/packFile/${this.summaryId}`
      // );
      // api.downloadAllFile(this.summaryId).then((res) => {
      //   if (res.code=="10001") {
      //     this.downflie({
      //       fileUrl:res.msg.fileId,
      //       filename:res.msg.fileName
      //     })

      //   }
      // });
    },
    downflie(file) {
      /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240711 start*/
      document.getElementById("downloadIframe").src = encodeURI( `/api/attachment/download/${encodeURI(file.fileUrl)}?createDate=${ file.createdate }&fileName=${encodeURIComponent(file.filename)}&moduleTypePlugin=12&sourceIdPlugin=${this.summaryId}` );
      /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240711 end*/
      // window.location.href = encodeURI(
      //   `/api/attachment/download/${encodeURI(
      //     file.fileUrl
      //   )}?fileName=${encodeURI(file.filename)}`
      // );
    },
    getid() {
      this.id = this.$route.params.id;
      if ( this.$route.params.isall == "true" || this.$route.params.isall == true ) {
        api.getShurlis(this.id).then((data) => {
          if (data.msg) {
            this.$message.warning("此邮件撤回");
            this.$alert("此邮件撤回，确定以关闭此窗口", "提示", {
              confirmButtonText: "确定",
              callback: (action) => {
                window.opener = null;
                window.open("", "_self");
                window.close();
              },
            });
          }
        });
      }

      this.$store.commit("isslect", this.id);
    },
    /**
     * 根据id获取部门或组的成员信息
     * @date 2021-12-22
     * @param {any} id
     * @param {any} text 标题文本
     */
    getGroupUserInfo(mailSummaryId , entityId, text) {
      this.popoverTableData = []; //清空数组
      this.popoverText = "成员信息加载中...";
      api.getGroupUserInfoApi(mailSummaryId , entityId).then((res) => {
        if (res.code === "1000" && res.msg.length != 0) {
          this.popoverTableData = res.msg;
          this.popoverText = `<h4 style="margin:5px 0">${text}<span style="color:red;margin:0 5px">(${res.msg.length})</span><h4>`;
          // // 将成员信息切成数组
          // const membersList = res.msg.split(',')
          // // 将成员遍历为html格式的元素并转换为字符串
          // let memberListStr = membersList.map(item=>`<div style="font-weight:normal">${item}</div>`).join('');
          // // 拼接上标题
          // // 并将数组成员构建成html代码
          // this.popoverText = `<h4 style="margin:5px 0">${text}<span style="color:red;margin:0 5px">(${membersList.length})</span><h4>` + memberListStr
        } else {
          this.popoverText = "没有获取到成员信息";
        }
      });
    },
    /**
     * 关闭所有popover框
     * @date 2021-12-22
     * @returns {any}
     */
    clearPopover() {
      for (const key in this.$refs) {
        if (key.indexOf("popover-") !== -1) {
          this.$refs[key].doClose();
        }
      }
    },

    // AI助手功能：START 方法（getEmailTextContent, handleAiInsert, handleAiFillReply, onAiGenerating）
    // 2026-06-01 AI助手 start: 新增onAiGenerating——接收AI面板生成状态，控制悬浮按钮呼吸灯
    onAiGenerating: function (val) {
      this.aiGenerating = val
    },
    // 2026-06-01 AI助手 end
    getEmailTextContent: function () {
      if (!this.data.content) return '';
      var tmp = document.createElement('div');
      tmp.innerHTML = this.data.content;
      return tmp.textContent || tmp.innerText || '';
    },
    handleAiInsert: function (content) {
      if (content) {
        var textarea = document.createElement('textarea');
        textarea.value = content;
        textarea.style.position = 'fixed';
        textarea.style.opacity = '0';
        document.body.appendChild(textarea);
        textarea.select();
        document.execCommand('copy');
        document.body.removeChild(textarea);
        this.$message.success('已复制到剪贴板');
      }
    },
    handleAiFillReply: function (content) {
      if (!content) {
        this.$message.warning('没有可填充的内容');
        return;
      }
      // 2026-06-01 AI助手 start: 改用sessionStorage传AI回复内容，避免长文本URL传递+移除autoContent字段
      // 之前通过URL query传autoContent，AI回复可能500+字超URL长度限制
      // 改为写入sessionStorage，editor.vue加载时读取并自动回填到编辑器
      try {
        window.sessionStorage.setItem('aiReplyContent', content)
      } catch (e) {}
      this.$router.push({
        path: "/editor",
        query: {
          obj: {
            secretTypeId: this.data.secretIdStr,
            summaryId: this.data.summaryId,
            affairId: this.data.affairId,
            type: "replyMail",
            mark: "reply",
            from: ""
          },
        },
      });
      // 2026-06-01 AI助手 end
    }
    // AI助手功能：END 方法
  },
};
</script>
<style scoped>
.vhtmlBoxScllor{
  min-height: 76vh;
  padding-left: 7px;
  /* overflow-x: auto; */
  border: 1px solid #dadada;
  border-radius: 5px;
  color: #000000;
  font-size: 16px;
  font-family: sans-serif;
}
.vhtmlBoxScllor /deep/ p{
  margin: 5px 0 !important;
}
.scorllbox /deep/ .edui-default .edui-editor{
  border: none !important;
}
/* .vhtmlBoxScllor /deep/ ol li {
  list-style-type:decimal;
} */
/* .vhtmlBoxScllor::-webkit-scrollbar{
  width: 10px;
  height: 10px;
  border-radius: 0 !important;
} */
.container{
  overflow: auto !important;
}
.container::-webkit-scrollbar{
  width: 5px;
  height: 10px;
}
  /* .scllorBox{
    width: 600px;
    height: 10px;
    overflow-x: scroll;
    z-index: 9999;
    background-color: #898989;
    position: absolute;
    bottom: 50px;
  }
  .scllorBoxSon{
    width: 1000px;
    height: 10px;
    z-index: 9999;
    background-color: #fff;
  } */
p {
  margin: 0 0 10px 0;
}
.inboxDet {
  background: #f7f8f9;
  height: 100%;
  position: relative;
  display: flex;
  flex-direction: row;

}
.iconfont {
  color: #898989;
}
.tlite {
  background: white;
  padding-left: 2.5em;
  display: flex;
  align-items: center;
  border: 1px solid #e6e9ec;
  border-top: 0;
  min-height: 54px;
  border-left: 0;
  font-weight: 600;
}

.imgtx {
  height: 38px;
  width: 38px;

  margin: 0 0.5em;
}
.msghead {
  width: 100%;
  display: flex;
  align-items: center;
}
.info::-webkit-scrollbar {
  width: 10px !important;
  height: 10px  !important;
}
.inboxDet /deep/ .el-divider--horizontal {
  margin: 12px 0;
}

.tabbbox /deep/ .tabhead {
  background: #ececec;
}

/*定义滚动条高宽及背景 高宽分别对应横竖滚动条的尺寸*/
::-webkit-scrollbar {
  width: 5px;
  height: 5px;
  background-color: #f5f5f5;
}

/*定义滚动条轨道 内阴影+圆角*/
::-webkit-scrollbar-track {
  box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
  -webkit-box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
  border-radius: 10px;
  background-color: #f5f5f5;
}

/*定义滑块 内阴影+圆角*/
::-webkit-scrollbar-thumb {
  border-radius: 10px;
  box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.1);
  -webkit-box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.1);
  background-color: #c8c8c8;
}

/* AI助手功能：START 布局变更（注意：以下是合并到 .inboxDet 原有样式的，不要覆盖原有样式） */
/* 在原有的 .inboxDet 后面追加: display: flex; flex-direction: row; */
/* 新增 .inboxDet-main 规则 */
.inboxDet-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
  overflow: hidden;
}
.inboxDet.has-ai-panel > .ai-panel {
  flex-shrink: 0;
}
/* AI助手功能：END 布局变更 */

/* AI助手功能：START 组件样式（切换按钮、图标等，直接新增即可） */
.ai-toggle-btn {
  position: fixed;
  right: 20px;
  bottom: 60px;
  width: 76px;
  height: 76px;
  border-radius: 14px;
  /* 2026-06-02 AI助手 start: 只显示机器人图标——去掉背景色、渐变、阴影、圆角边框 */
  /* 原样式已注释: background-color、gradient、box-shadow、border-radius */
  border: none;
  cursor: pointer;
  z-index: 9999;
  transition: all 0.3s;
  background-color: transparent;
  background-image: url('../../assets/ai-btn.png');
  background-size: contain;
  background-position: center center;
  background-repeat: no-repeat;
  /* 2026-06-02 AI助手 end */
}
.ai-toggle-btn:hover {
  /* 2026-06-02 AI助手: 悬停放大效果替代原阴影+上移 */
  transform: scale(1.1);
}
.ai-toggle-btn.active {
  background-color: transparent;
  background-image: url('../../assets/ai-btn.png');
}
/* 2026-06-02 AI助手 start: 生成中脉冲动画——图标缩放跳动，生成结束自动消失 */
.ai-toggle-btn.generating {
  animation: ai-pulse-btn 1.2s ease-in-out infinite;
}
@keyframes ai-pulse-btn {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.15); opacity: 0.8; }
}
/* 2026-06-02 AI助手 end */
/* 2026-06-02 AI助手 start: 旧CSS芯片图标注释保留，原代码参考 */
/*
.ai-chip-icon {
  width: 22px;
  height: 22px;
  border: 2px solid rgba(255,255,255,0.9);
  border-radius: 4px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-chip-icon::before,
.ai-chip-icon::after {
  content: '';
  position: absolute;
  background: rgba(255,255,255,0.9);
}
.ai-chip-icon::before {
  width: 10px;
  height: 2px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.ai-chip-icon::after {
  width: 2px;
  height: 10px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.ai-chip-core {
  width: 6px;
  height: 6px;
  background: rgba(255,255,255,0.9);
  border-radius: 1px;
  position: relative;
  z-index: 1;
}
*/
/* 2026-06-02 AI助手 end */
/* AI助手功能：END 组件样式 */

</style>
<style>
.el-popover {
  max-height: 400px;
  overflow: auto;
}
/*定义滚动条高宽及背景 高宽分别对应横竖滚动条的尺寸*/
::-webkit-scrollbar {
  width: 5px;
  height: 5px;
  background-color: #f5f5f5;
}

/*定义滚动条轨道 内阴影+圆角*/
::-webkit-scrollbar-track {
  box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
  -webkit-box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.3);
  border-radius: 10px;
  background-color: #f5f5f5;
}

/*定义滑块 内阴影+圆角*/
::-webkit-scrollbar-thumb {
  border-radius: 10px;
  box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.1);
  -webkit-box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.1);
  background-color: #c8c8c8;
}
</style>
