<template>
  <div class="count" id="draggle-area">
    <!--客开新增弹窗 逻辑原先保持一致-->
    <el-dialog
      title="选择密级"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :visible.sync="
        this.dialogVisible &&
        this.$store.state.isredNet &&
        this.$store.state.isWriteEamil
      "
      :show-close="false"
      width="30%"
    >
      <el-select
        v-model="msg.secretTypeId"
        placeholder="请选择"
        @change="MailFileSecretVerify"
        @visible-change="cct"
        class="secretArrow"
      >
        <el-option
          v-for="item in secretTypeIdList"
          :key="item.id"
          :label="item.name"
          :value="item.id"
        >
        </el-option>
      </el-select>
      <span slot="footer" class="dialog-footer">
        <el-button
          size="small"
          round
          type="default"
          @click="handleSelectSecretConcel"
          >取消</el-button
        >
        <el-button size="small" round type="primary" @click="selectSecretType"
          >确定</el-button
        >
      </span>
    </el-dialog>

    <div class="editor-main-wrap">
    <div
      v-show="
        !(
          this.dialogVisible &&
          this.$store.state.isredNet &&
          this.$store.state.isWriteEamil
        )
      "
      style="
        border-radius: 4px;
        background: white;
        border: 1px solid #ebeef0;
        margin: 15px;
        padding: 15px;
        height: 92%;
        overflow: auto;
      "
    >
      <div class="flieupload">
        <el-upload
          style="display: none"
          class="avatar-uploader"
          :action="serverUrl"
          ref="upload"
          name="file"
          :headers="header"
          :auto-upload="false"
          :show-file-list="false"
          :multiple="true"
          :http-request="uploadFile"
          :on-success="uploadflieSuccess"
          :on-change="beforeFeedBackExports"
          :on-error="uploadflieError"
          :on-progress="uploadVideoProcess"
          :before-upload="beforeflieUpload"
        >
        </el-upload>
      </div>
      <el-form
        ref="form"
        label-width="5em"
        :model="msg"
        class="demo-form-inline"
        size="mini"
        style="height: 100%; position: relative"
      >
        <div style="padding-bottom: 15px">
          <el-button
            type="danger"
            round
            icon="el-icon-position"
            size="mini"
            @click="send"
            :disabled="disabled"
            >发 送 邮 件</el-button
          >
          <el-button
            type="primary"
            round
            icon="el-icon-folder-add"
            size="mini"
            @click="save"
            >保 存 草 稿
          </el-button>
          <!-- ===== AI助手功能：工具栏AI按钮 START ===== -->
          <el-button
            type="primary"
            round
            icon="el-icon-cpu"
            size="mini"
            @click="aiPanelVisible = !aiPanelVisible"
            :class="{ 'is-active': aiPanelVisible }"
            >AI助手
          </el-button>
          <!-- ===== AI助手功能：工具栏AI按钮 END ===== -->
          <el-button
            round
            class="closeBt"
            size="mini"
            @click="cancelSave"
            icon="el-icon-close"
          >
            <!-- @click="refnav" -->
          </el-button>
        </div>
        <el-row class="the-theme">
          <el-col :span="24">
            <!--主题-->
            <el-form-item label="主题  " required>
              <el-tooltip
                :disabled="msg.subject == '' || msg.subject == null"
                class="item"
                effect="dark"
                :content="msg.subject"
                placement="top-start"
              >
                <el-input
                  v-model="msg.subject"
                  placeholder="邮件主题"
                ></el-input>
              </el-tooltip>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <!-- 收件人-->
          <el-col :span="24" class="the-recipient">
            <el-form-item label="收件人" required class="select-user-form">
              <!-- lyy :disabled="msg.receiversStr == '' || msg.receiversStr == null" -->
              <el-tooltip
                class="item"
                :disabled="true"
                effect="dark"
                :content="msg.receiversStr.toString()"
                placement="bottom"
              >
                <!-- <el-input clearable
                          v-model="msg.receiversStr"
                          placeholder="收件人"
                          @focus="setlepeo($event,'receivers')"
                          suffix-icon="el-icon-user-solid"></el-input> -->

                <el-select
                  ref="receiver"
                  v-model="receiversStr"
                  placeholder="收件人"
                  multiple
                  filterable
                  default-first-option
                  remote

                  :filter-method="selectBlur"
                  @clear="selectClear"
                  @remove-tag="removeReceiverTag"
                  @change="selectChange"
                  class="select-user"
                  icon="el-icon-user-solid"
                >
                <!-- 屏蔽邮件审批 :popper-append-to-body="false" -->
                  <!-- @focus="focusSearch" -->
                  <el-option
                    v-for="(item, index) in receiversOptions"
                    :key="index"
                    :value="item.memberId || item.id"
                    :label="item.value"
                    @click="getOption(item.memberId)"
                  >
                    {{ item.operationName }}
                  </el-option>
                </el-select>
              </el-tooltip>
              <i
                class="el-icon-user-solid position-icon"
                @click.stop="setlepeo($event, 'receivers')"
              ></i>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <!--     抄送人       -->
          <el-col :span="24" class="copy-people">
            <el-form-item label="抄送" class="select-user-form">
              <!-- lyy  :disabled="
                                msg.copyReceiversStr == '' || msg.copyReceiversStr == null
                            "-->
              <el-tooltip
                class="item"
                :disabled="true"
                effect="dark"
                :content="msg.copyReceiversStr.toString()"
                placement="bottom"
              >
                <!-- <el-input clearable
                          v-model="msg.copyReceiversStr"
                          placeholder="抄送人"
                          @focus="setlepeo($event,'copyReceivers')"
                          suffix-icon="el-icon-user-solid"></el-input> -->
                <el-select
                  ref="copyReceiver"
                  v-model="copyReceiversStr"
                  placeholder="抄送人"
                  multiple
                  filterable
                  default-first-option
                  remote
                  :popper-append-to-body="false"
                  :filter-method="selectBlurCopyReceiver"
                  @clear="selectClearCopyReceiver"
                  @remove-tag="removeCopyReceiverTag"
                  @change="selectChangeCopyReceiver"
                  class="select-user"
                  icon="el-icon-user-solid"
                >
                  <!--lyy  @focus="copyReceiversFocusSearch" -->
                  <el-option
                    v-for="(item, index) in copyReceiversOptions"
                    :key="index"
                    :value="item.memberId || item.id"
                    :label="item.value"
                  >
                    {{ item.operationName }}
                  </el-option>
                </el-select>
              </el-tooltip>
              <i
                class="el-icon-user-solid position-icon"
                @click.stop="setlepeo($event, 'copyReceivers')"
              ></i>
            </el-form-item>
          </el-col>
          <!--      密级 -->
          <p v-show="false" class="emailsecretTypeId">{{ msg.secretTypeId }}</p>
          <el-col v-show="this.$store.state.isredNet" :span="4">
            <!-- <el-form-item label="密级">
                            <div id="secretType">
                              <el-select v-model="msg.secretTypeId" placeholder="请选择" @change="MailFileSecretVerify" @visible-change="cct"> -->
            <!--                                <el-option-->
            <!--                                        v-for="item in secretTypeIdList"-->
            <!--                                        :key="item.id"-->
            <!--                                        :label="item.name"-->
            <!--                                        :value="item.id">-->
            <!--                                </el-option>-->
            <!-- <slectli  v-for="item in secretTypeIdList"
                                          :key="item.id"
                                          :label="item.name"
                                          :value="item.id">
                                </slectli>
                            </el-select>
                            </div>
                        </el-form-item> -->
          </el-col>
        </el-row>
        <el-row>
          <!--密级调整位置 modify yangyanhua-->
          <el-col v-show="this.$store.state.isredNet" :span="4">
            <el-form-item label="密级" required>
              <!-- <div id="secretType"> -->
              <el-select
                v-model="msg.secretTypeId"
                placeholder="请选择"
                @change="MailFileSecretVerify"
                @visible-change="cct"
              >
                <slectli
                  v-for="item in secretTypeIdList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                >
                </slectli>
              </el-select>
              <!-- </div> -->
            </el-form-item>
          </el-col>

          <el-col
            :span="8"
            v-if="(msg.kuaWangdis && this.$store.state.isredNet) || msg.kuaWang"
          >
            <el-form-item
              label="审批人"
              class="select-user-form"
              label-width="100px"
              required
            >
              <!-- lyy :disabled="msg.approverStr == '' || msg.approverStr == null" -->
              <el-tooltip
                class="item"
                :disabled="true"
                effect="dark"
                :content="msg.approverStr.toString()"
                placement="bottom"
              >
                <!-- <el-input clearable
                          v-model="msg.approverStr"
                          placeholder="审批人"
                          @focus="selectapprover($event)"
                          suffix-icon="el-icon-user-solid"></el-input> -->
                <el-select
                  ref="approver"
                  v-model="approverStr"
                  placeholder="审批人"
                  multiple
                  filterable
                  remote
                  default-first-option
                  :multiple-limit="3"
                  :filter-method="selectBlurApprover"
                  @clear="selectClearApprover"
                  @change="selectChangeApprover"
                  @remove-tag="removeApproveTag"
                  class="select-user"
                  icon="el-icon-user-solid"
                >
                  <!--lyy @focus="approverFocusSearch"  -->
                  <el-option
                    v-for="(item, index) in approverOptions"
                    :key="index"
                    :value="item.memberId || item.id"
                    :label="item.value"
                  >
                    {{ item.operationName }}
                  </el-option>
                </el-select>
              </el-tooltip>
              <i
                class="el-icon-user-solid position-icon"
                @click.stop="selectapprover($event)"
              ></i>
            </el-form-item>
          </el-col>

          <!--跨网-->
          <!-- v-show="false" -->
          <el-col :span="3" align="left" v-if="isKuaWangShow">
            <el-form-item label-width="0">
              <keep-alive>
                <el-checkbox
                  :disabled="msg.kuaWangdis && this.$store.state.isredNet"
                  v-model="msg.kuaWang"
                  ref="iskw"
                  style="margin-left: 38px"
                  :key="key"
                  >是否跨网
                </el-checkbox>
              </keep-alive>
            </el-form-item>
          </el-col>
          <!--      定时发送      -->
          <el-col :span="8">
            <div style="height: 2em">
              <el-form-item label-width="5">
                <el-checkbox
                  v-model="msg.timedTask"
                  style="margin-left: 38px"
                  :disabled="msg.kuaWang"
                >
                  定时发送</el-checkbox
                >
                <el-date-picker
                  :picker-options="pickerOptions"
                  v-show="msg.timedTask"
                  style="margin-left: 10px; width: 30%; padding: 0 30px 0 8px"
                  v-model="msg.timingDate"
                  type="datetime"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  placeholder="选择发送时间"
                >
                </el-date-picker>
              </el-form-item>
            </div>
          </el-col>
          <!--            -->
        </el-row>
        <div
          class="uploadbox"
          v-loading="loading"
          style="min-height: 40px !important"
          :element-loading-text="percentage"
          element-loading-spinner="el-icon-upload"
          element-loading-background="rgba(255,255,255,0.9 )"
        >
          <div style="margin-top: 5px">
            <i
              class="el-icon-paperclip"
              style="margin-left: 8px; color: #3d3d3d; font-size: 18px"
              ><span style="font-size: 14px; margin-left: 0.5em">
                <el-button type="text" size="mini" @click="uploadfile"
                  >上传文件</el-button
                ><span style="color: #999999; font-size: 12px"></span> </span
            ></i>
          </div>
          <div class="uploadflie">
            <el-row>
              <div>
                <el-col :span="6" v-for="(i, n) in fileList" :key="i.fileUrl">
                  <el-tooltip
                    class="item"
                    effect="dark"
                    :content="i.filename"
                    placement="top"
                  >
                    <div
                      style="
                        width: 97%;
                        margin: auto;
                        height: 65px;
                        background: #efefef;
                        border-radius: 4px;
                        display: flex;
                        margin-bottom: 10px;
                      "
                    >
                      <div
                        style="
                          width: 20%;
                          display: flex;
                          align-items: center;
                          justify-content: center;
                        "
                      >
                        <img
                          style="width: 64%"
                          :src="
                            '/seeyon/common/images/attachmentICON/' + i.icon
                          "
                        />
                      </div>
                      <div
                        style="width: 80%; position: relative; color: #484848"
                      >
                        <i
                          class="el-icon-download hand"
                          style="position: absolute; bottom: 5px; right: 30px"
                          @click="downflie(i)"
                        ></i>
                        <i
                          class="el-icon-delete hand"
                          style="position: absolute; bottom: 5px; right: 5px"
                          @click="delflie(n)"
                        ></i>
                        <p
                          style="
                            margin: 0.5em;
                            overflow: hidden;
                            white-space: nowrap;
                            text-overflow: ellipsis;
                          "
                        >
                          {{ i.filename }}
                        </p>
                        <span
                          style="margin: 0 1em; font-size: 14px; color: #b1b3b6"
                          >{{ i.size }}</span
                        >
                      </div>
                    </div>
                  </el-tooltip>
                </el-col>
              </div>
            </el-row>
          </div>
        </div>

        <div class="editor-container" style="margin-top: 10px; height: 80%; display: flex; position: relative;">
          <!-- 编辑器区域 -->
          <div class="editor-area" :style="{ width: '100%' }">
            <el-upload
              style="display: none"
              class="avatar-uploader"
              :action="serverUrl"
              name="file"
              :headers="header"
              :show-file-list="false"
              list-type="picture"
              :multiple="true"
              :on-success="uploadSuccess"
              :on-error="uploadError"
              :before-upload="beforeUpload"
            >
            </el-upload>
            <!-- 2026-06-02 AI助手 start: 关键依赖——ref="ue"必须保留，不可删除 -->
            <!-- AI助手所有编辑器操作(getEditorTextContent/getSelectedText/handleAiInsert等) -->
            <!-- 都通过this.$refs.ue访问编辑器实例，删除ref="ue"会导致全部功能失效 -->
            <vue-ueditor-wrap
              v-if="isshow"
              ref="ue"
              v-model="msg.content"
              :config="myConfig"
            ></vue-ueditor-wrap>
            <!-- 2026-06-02 AI助手 end -->
          </div>

        </div>
        <!--                <div style="text-align: right;margin-top: 10px">-->
        <!--                    <el-button type="primary" plain size="mini" @click="save" >保 存 草 稿</el-button>-->
        <!--                    <el-button type="danger" plain size="mini" @click="send">发 送 邮 件</el-button>-->

        <!--                </div>-->
      </el-form>
    </div>
    </div><!-- end editor-main-wrap -->

    <!-- ===== AI助手功能：AI面板组件和切换按钮 START ===== -->
    <!-- 2026-06-02 AI助手 start: AI面板+切换按钮——完整模板(6个prop+4个event+2个class)，整段复制覆盖即可 -->
    <!-- 注意：getEditorTextContent依赖上方vue-ueditor-wrap的ref="ue"，两处必须同时存在 -->
    <ai-assistant-panel
      :visible="aiPanelVisible"
      scene="edit"
      :email-subject="originalEmailSubject"
      :email-content="originalEmailContent"
      :original-sender="originalEmailSender"
      :get-editor-content="getEditorTextContent"
      :get-selected-text-fn="getSelectedText"
      :get-original-email-text="getOriginalEmailText"
      @close="aiPanelVisible = false"
      @insert="handleAiInsert"
      @fill-reply="handleAiInsert"
      @generating="onAiGenerating"
    ></ai-assistant-panel>

    <!-- 2026-06-02 AI助手 start: 按钮图标换为a.png背景图 -->
    <!-- 原图标为CSS画的芯片图案(ai-chip-icon + ai-chip-core)，现改用a.png背景图 -->
    <!-- 旧代码注释保留：<span class="ai-chip-icon"><span class="ai-chip-core"></span></span> -->
    <div class="ai-toggle-btn" :class="{ active: aiPanelVisible, generating: aiGenerating }" @click="aiPanelVisible = !aiPanelVisible" :title="aiPanelVisible ? '关闭AI助手' : '打开AI助手'">
    </div>
    <!-- 2026-06-02 AI助手 end -->
    <!-- ===== AI助手功能：AI面板组件和切换按钮 END ===== -->
  </div>
</template>

<script>
import api from "@/api/api";
// ===== AI助手功能：组件导入 START =====
import AiAssistantPanel from "@/components/AiAssistantPanel";
// ===== AI助手功能：组件导入 END =====
import slectli from "../components/slectli";
import VueUeditorWrap from "vue-ueditor-wrap";

export default {
  name: "editor",
  components: { slectli, VueUeditorWrap, AiAssistantPanel }, // AI助手功能：AiAssistantPanel为新增
  beforeRouteLeave(to, from, next) {
    //点击刷新按钮，弹出是否保存草稿，需要将标志重新恢复初值
    if (this.$store.state.isWriteEamil) {
      this.$store.commit("setWriteEamil", false);
    }
    if (this.sendtie == false && !this.dialogVisible) {
      const answer = window.confirm("是否保存草稿？");
      if (answer == true) {
        // 判断当邮件附件正在上传时，不能保存草稿
        if (this.percentage != 0 && !this.ifFileUploaded) {
          next(false);
          document.querySelector(".mid").style.display = "none";
          document.querySelector(".resize").style.display = "none";
          document.querySelector(".right").style.width = "auto";
          return this.$message.warning("文件还在上传中...");
        }
        this.saves();
      } else {
        api
          .cancelAutosaveApi(this.firstAutosaveTime, this.msg.affairId)
          .then((res) => {
            console.log("执行了取消自动保存接口，并且拿到了详情值");
            this.$store.commit("changeCancelSaveDraftRandom");
            next();
          });
      }
    }
    next();
  },
  data() {
    return {
      // 禁止点击按钮
      disabled:false,
      // lyy start
      // 收件人
      receiversStr: [],
      copyReceiversStr: [],
      approverStr: [],
      sendFlag: true,
      //   lyy end
      receiversOptions: [], //收件人
      copyReceiversOptions: [], //抄送人人
      approverOptions: [], //审批人
      //上传文件遮罩层
      loading: false,
      isshow: false,
      // ===== AI助手功能：data属性 START =====
      // 2026-06-01 AI助手 start: 新增aiGenerating(呼吸灯状态)和originalEmailSender(发件人名字)
      aiPanelVisible: false,
      aiGenerating: false,
      originalEmailSubject: '',
      originalEmailContent: '',
      originalEmailTemplate: '',
      originalEmailSender: '',
      // 2026-06-01 AI助手 end
      aiLoading: false,
      polishLoading: false,
      showAiButton: false,
      // ===== AI助手功能：data属性 END =====
      //编辑器的配置项初始化
      myConfig: {
        forceInit: true,
        enableAutoSave: false,
        // 编辑器不自动被内容撑高
        autoHeightEnabled: false,
        // 粘贴只保留标签，去除标签所有属性
        retainOnlyLabelPasted:false,
        // 是否默认为纯文本粘贴
        pasteplain:false,
        // 初始容器高度
        initialFrameHeight: 680,
        // 初始容器宽度
        initialFrameWidth: "100%",
        // 上传文件接口（这个地址是我为了方便各位体验文件上传功能搭建的临时接口，请勿在生产环境使用！！！）
        serverUrl: "",
        // UEditor 资源文件的存放路径，如果你使用的是 vue-cli 生成的项目，通常不需要设置该选项，vue-ueditor-wrap 会自动处理常见的情况，如果需要特殊配置，参考下方的常见问题2

        // '/seeyon/apps_res/plugin/newInternalmail//UE/' 为打包地址
        //  本地实际地址为'/UE/'
        UEDITOR_HOME_URL: "./UE/",
        // UEDITOR_HOME_URL: '/UE/',
        toolbars: [
          [
            "fullscreen",
            "source",
            "|",
            "undo",
            "redo",
            "|",
            "bold",
            "italic",
            "underline",
            "fontborder",
            "strikethrough",
            "superscript",
            "subscript",
            "removeformat",
            "formatmatch",
            "autotypeset",
            "blockquote",
            "pasteplain",
            "|",
            "forecolor",
            "backcolor",
            "insertorderedlist",
            "insertunorderedlist",
            "selectall",
            "cleardoc",
            "|",
            "rowspacingtop",
            "rowspacingbottom",
            "lineheight",
            "|",
            "customstyle",
            "paragraph",
            "fontfamily",
            "fontsize",
            "|",
            "directionalityltr",
            "directionalityrtl",
            "indent",
            "|",
            "justifyleft",
            "justifycenter",
            "justifyright",
            "justifyjustify",
            "|",
            "touppercase",
            "tolowercase",
            "|",
            "link",
            "unlink",
            "anchor",
            "|",
            "imagenone",
            "imageleft",
            "imageright",
            "imagecenter",
            "|",
            "pagebreak",
            "|",
            "horizontal",
            "date",
            "time",
            "spechars",
            "|",
            "inserttable",
            "deletetable",
            "insertparagraphbeforetable",
            "insertrow",
            "deleterow",
            "insertcol",
            "deletecol",
            "mergecells",
            "mergeright",
            "mergedown",
            "splittocells",
            "splittorows",
            "splittocols",
            "charts",
            "|",
          ],
        ],
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 start*/
        virtualIdPlugin:"",
        sourceIdPlugin:"",
        attachmentUrl:api.getAttachmentUrl(),
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 end*/
      },


      //定时发送
      pickerOptions: {
        disabledDate(time) {
          return time.getTime() < Date.now() - 86400000; //禁止选择以前的时间
        },
      },
      percentage: 0,
      maxFileSize: 200, //上传文件的最大值
      secretTypeIdList: [],
      maxFileLen: "", //上传文件列表长度
      files: [], //上传文件列表
      loadProgress: 0, //上传文件进度
      num: 0, //文件第二次上传初始化
      isCrossDisable: "", //是否跨网
      sendtie: false, //是否编辑
      selectmj: true, //选择密级
      key: 0, //工具数据
      secretTypeIdold: "", //选择密级类型的id
      msg: {
        //邮件信息，整个邮件
        approver: [],
        approverStr: [],
        summaryId: "",
        affairId: "",
        to: "",
        toIds: "",
        cc: "",
        timedTask: false,
        ccIds: "",
        receivers: [],
        receiversStr: [],
        copyReceivers: [],
        copyReceiversStr: [],
        subject: "",
        secretTypeId: "", //密级状态默认为空
        kuaWang: false,
        kuaWangdis: true,
        isTiming: "",
        timingDate: "",
        content: null,
        attachments: [],
        type: "",
        editMailSend: "",
        mark: "",
        replyParentSummaryid: "",
        replyMemberId: "",
        replyMember: "",
        replyParentAffairid: "",
        parentformSummaryid: "",
        forwardMemberId: "",
        forwardMember: "",
        parentformAffairid: "",
        recipient: "",
        copyfor: "",
      },
      serverUrl: "/api/attachment/upload", //上传url
      show: false, //是否显示 废弃
      fileList: [], //文件列表
      dialogVisible: true, //选择密级的弹出框是否隐藏
      header: {
        // token: sessionStorage.token
      }, // 有的图片服务器要求请求头需要有token
      timer: null,
      // 第一次定时器timer
      firstTimer: null,
      // 判断是否退出页面且退出发请求
      isResquest: true,
      // 时间戳
      firstAutosaveTime: "",
      // 跨网选项是否展示
      isKuaWangShow: false,
      // 文件是否上传完成
      ifFileUploaded: false,
      // 收件人选中信息
      saveSubmitIds: [],
      // 抄送人选择信息
      copySaveSubmitIds: [],
      // 审核人选择信息
      approveSaveSubmitIds: [],
      // timer: null,
    };
  },
  watch: {
    //邮件内容
    msg: {
      handler(n) {
        // console.log("监听msg改变", n);
        if (!n.receiversStr) {
          //   this.msg.receivers = "";
          //   lyy
          this.msg.receivers = [];
        }
        if (!n.copyReceiversStr) {
          //   this.msg.copyReceivers = "";
          //   lyy
          this.msg.copyReceivers = [];
        }
        if (!n.approverStr) {
          //   lyy
          this.msg.approver = [];
        }
        // 说明跨网选中了，清空定时任务的选中状态
        if (n.kuaWang) {
          this.msg.timedTask = false;
        }
        // 邮件编辑态和浏览态不一致 fuguichuan
        // this.msg.content = this.msg.content.replace(/pre/g,'p')


        /*客开 安全管理员、审计管理员日志改造 20240714 start */
        //主要监听summaryIdStr,如果有值的话，就给富文本传值
        if (n.summaryIdStr){
          this.myConfig.sourceIdPlugin = n.summaryIdStr;
          this.msg.sourceIdPlugin = n.summaryIdStr;
          this.isshow = true; //有值了才显示富文本
        }
        /*客开 安全管理员、审计管理员日志改造 20240714 end */

      },
      deep: true,
      immediate: true,
      searchName: "",
      searchValue: "",
      searchList: [], //
    },
    /**
     * 监听选择密级弹框的状态，点击写信打开的时候默认不会自动发送邮件，点击确定选择密级之后再开始自动保存
     * @Date: 2022-06-23 17:17:17
     * @param {*} val 当前状态值
     */
    dialogVisible(val) {
      if (!val) {
        this.firstTimer = setTimeout(() => {
          this.pushRealTimeData();
        }, 100 * 1000);
      }
    },
  },
  async created() {
    this.firstAutosaveTime = new Date().getTime();
    // console.log(`this.firstAutosaveTime`, this.firstAutosaveTime);
    //获取邮件数量
    // api.emlianum().then((res) => {
    //   console.log(res);
    //   // this.maxFileSize = res.data.uploadMaxSize;
    // });
    // vm = this;

    //是否邮件列表列
    this.lxryc();
    //写信按钮是否弹出密级弹窗
    // console.log(
    //   `编辑页面this.$store.state.isWriteEamil`,
    //   this.$store.state.isWriteEamil
    // );
    if (this.$store.state.isWriteEamil == false) {
      this.$nextTick(() => {
        setTimeout(() => {
          this.dialogVisible = false;
        }, 500);
      });
    }
    // lyy start
    // 初始化收件人,抄送人，审批人
    // await this.focusSearch();
    // await this.copyReceiversFocusSearch();
    // await this.approverFocusSearch();
    // lyy end
    //获取邮件详情
    this.getdata();
  },
  mounted() {
    // this.saves();
    // this.isshow = true; //暂缓显示编辑板
    api.emlianum().then((res) => {
      this.$store.commit("setnum", res.msg || res);
    });
    // this.secretTypeIdold = this.msg.secretTypeId

    /**
     * 1.文件第一次进入拖动区时，触发 dragenter 事件
     * 2.文件在拖动区来回拖拽时，不断触发 dragover 事件
     * 3.文件已经在拖动区，并松开鼠标时，触发 drop 事件
     * 4.文件在拖动区来回拖拽时，不断触发dragleave 事件
     * auth: tangxiangping
     * date: 2022/06/06 start
     */
    var dropbox = document.getElementById("app");
    dropbox.addEventListener("drop", this.enentDrop, false);
    dropbox.addEventListener("dragleave", this.dragleave, false);
    dropbox.addEventListener("dragenter", this.dragenter, false);
    dropbox.addEventListener("dragover", this.dragover, false);
    /**tangxiangping 2022-06-06 end */

    //是红网需要弹出密级弹出框，否之则隐藏
    if (this.$store.state.isredNet) {
      this.dialogVisible = true;
    } else {
      this.dialogVisible = false;
    }
    // 用于判断当前用户是否有跨网权限
    api
      .getKuaWangShowApi()
      .then((res) => {
        if (res.code == "1001") {
          // 说明跨网权限
          this.isKuaWangShow = true;
        } else {
          this.isKuaWangShow = false;
          this.msg.kuaWang = false;
        }
      })
      .catch((err) => {
        this.isKuaWangShow = false;
        this.msg.kuaWang = false;
      });
  },
  beforeDestroy() {
    // console.log("执行了组件销毁生命周期函数");
    this.isResquest = false;
    let dropbox = document.getElementById("app");
    dropbox.removeEventListener("drop", this.enentDrop, false);
    dropbox.removeEventListener("dragleave", this.dragleave, false);
    dropbox.removeEventListener("dragenter", this.dragenter, false);
    dropbox.removeEventListener("dragover", this.dragover, false);
    if (this.firstTimer) {
      clearTimeout(this.firstTimer);
    }
    clearTimeout(this.timer);
  },
  methods: {
        // ===== AI助手功能：methods方法 START =====
    // 2026-06-01 AI助手 start: 新增onAiGenerating——接收AI面板生成状态，用于悬浮按钮呼吸灯
    onAiGenerating: function(val) {
      this.aiGenerating = val
    },
    // 2026-06-01 AI助手 end

    // 2026-06-01 AI助手 start: 重写getEditorTextContent——用HTML定位蓝色虚线再分离，只取上方用户草稿
    // 关键修复：之前用innerText找#1E9FFF，但innerText不含CSS颜色值，永远找不到分隔线
    // 导致全文(含模板)都当草稿发给AI，AI润色的是别人发的邮件正文而非用户写的内容
    // 现改为用innerHTML定位#1E9FFF（它在HTML的style属性中），分离后再各自提取innerText
    getEditorTextContent: function() {
      try {
        var editor = this.$refs.ue && this.$refs.ue.editor;
        if (!editor || !editor.body) return '';
        return this._splitEditorByBlueLine(editor).aboveText
      } catch(e) {}
      return '';
    },
    // 2026-06-01 AI助手 end

    // 2026-06-01 AI助手 start: 新增getOriginalEmailText——获取蓝色虚线下方的原始邮件正文
    getOriginalEmailText: function() {
      try {
        var editor = this.$refs.ue && this.$refs.ue.editor;
        if (!editor || !editor.body) return '';
        return this._splitEditorByBlueLine(editor).belowText
      } catch(e) {}
      return '';
    },
    // 2026-06-01 AI助手 end

    // 2026-06-01 AI助手 start: 新增_splitEditorByBlueLine——用HTML定位蓝色虚线分隔，分别提取上下方文本
    // 原理：蓝色虚线#1E9FFF存在于HTML的style属性中，innerHTML包含它，innerText不包含
    // 找到分隔线HTML标签的起止位置 → 切分HTML → aboveHTML和belowHTML各自转纯文本
    _splitEditorByBlueLine: function(editor) {
      var html = editor.body.innerHTML || ''
      var aboveText = ''
      var belowText = ''
      // 在HTML中找#1E9FFF或#1e9fff（蓝色虚线颜色值）
      var colorIdx = html.indexOf('#1E9FFF')
      if (colorIdx === -1) colorIdx = html.indexOf('#1e9fff')
      if (colorIdx === -1) {
        // 没有蓝色虚线，说明不是回复/转发场景，全文都是用户草稿，无下方原文
        aboveText = (editor.body.innerText || '').trim()
        return { aboveText: aboveText, belowText: '' }
      }
      // 找到包含#1E9FFF的HTML标签的起始位置（往前找'<'）
      var tagStart = html.lastIndexOf('<', colorIdx)
      if (tagStart === -1) {
        aboveText = (editor.body.innerText || '').trim()
        return { aboveText: aboveText, belowText: '' }
      }
      // 找到整个蓝色虚线行的HTML：从tagStart到该行结束（下一个可见内容的开始或</div>等）
      // 蓝色虚线通常是一个<hr>或带border的<div>/<p>，找到标签闭合位置
      var tagEnd = html.indexOf('>', colorIdx)
      if (tagEnd === -1) tagEnd = colorIdx + 1
      // 继续找闭合标签（可能嵌套）
      var closeIdx = html.indexOf('</', tagEnd)
      // 取分隔标签之后的所有HTML作为belowHTML
      var cutPoint = tagEnd + 1
      // 如果分隔线标签后紧跟换行或空格，跳过
      while (cutPoint < html.length && (html.charAt(cutPoint) === '\n' || html.charAt(cutPoint) === '\r' || html.charAt(cutPoint) === ' ')) {
        cutPoint++
      }
      var aboveHTML = html.substring(0, tagStart)
      var belowHTML = html.substring(cutPoint)
      // aboveHTML → 纯文本（用户草稿）
      var tmpDiv = document.createElement('div')
      tmpDiv.innerHTML = aboveHTML
      aboveText = (tmpDiv.textContent || tmpDiv.innerText || '').trim()
      // belowHTML → 纯文本（原始邮件）
      tmpDiv.innerHTML = belowHTML
      belowText = (tmpDiv.textContent || tmpDiv.innerText || '').trim()
      return { aboveText: aboveText, belowText: belowText }
    },
    // 2026-06-01 AI助手 end
    getSelectedText: function() {
      try {
        var editor = this.$refs.ue && this.$refs.ue.editor;
        if (editor && editor.selection) {
          return editor.selection.getText() || '';
        }
      } catch(e) {}
      return '';
    },
    handleAiInsert: function(content) {
      if (!content) return;
      try {
        var cleanedHtml = this.cleanAiContentToHtml(content);
        // 空内容保护：如果清理后为空，回退到原始内容
        if (!cleanedHtml || cleanedHtml === '<p></p>' || cleanedHtml.replace(/\s/g, '') === '<p></p>') {
          cleanedHtml = '<p>' + content.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;') + '</p>';
        }
        // 2026-06-01 AI助手 start: 改用UEditor execCommand插入，自动替换选中内容或插到光标处
        // 之前的逻辑是直接覆盖整个编辑器草稿并拼模板，会丢失用户已写内容和光标位置
        var editor = this.$refs.ue && this.$refs.ue.editor;
        if (editor) {
          try {
            // execCommand('inserthtml') 会自动处理：有选中则替换选中，无选中则插到光标位置
            editor.execCommand('inserthtml', cleanedHtml);
            // 同步v-model，确保数据一致性
            this.msg.content = editor.getContent();
            this.$message.success('已插入');
            return;
          } catch(e2) {}
        }
        // 2026-06-01 AI助手 end
        // execCommand失败时的兜底方案：替换整个草稿并保留模板
        var template = this.originalEmailTemplate || '';
        if (!template) {
          var currentContent = this.msg.content || '';
          var split = this.splitByBlueLine(currentContent);
          if (split.hasSeparator) {
            template = split.template;
          }
        }
        var newContent = cleanedHtml + template;
        this.msg.content = newContent;
        if (editor && editor.setContent) {
          try { editor.setContent(newContent); } catch(e3) {}
        }
        this.$message.success('已填入编辑器');
        return;
      } catch(e) {
        console.error('AI insert error:', e);
      }
      this.$message.warning('编辑器未就绪，请重试');
    },
    // 按蓝色虚线分割邮件内容（#1E9FFF 为致远OA蓝色虚线标记色）
    splitByBlueLine: function(content) {
      if (!content) return { body: '', template: '', hasSeparator: false };
      var idx = content.indexOf('#1E9FFF');
      if (idx === -1) idx = content.indexOf('#1e9fff');
      if (idx === -1) return { body: content, template: '', hasSeparator: false };
      var tagStart = content.lastIndexOf('<', idx);
      if (tagStart === -1) return { body: content, template: '', hasSeparator: false };
      return { body: content.substring(0, tagStart), template: content.substring(tagStart), hasSeparator: true };
    },
    // 2026-06-01 AI助手 start: 新增_extractOriginalSender——从原始邮件HTML模板中提取发件人名字
    // 模板区域格式: "发件人: 张三"、"寄件者: Zhang San"、"From: xxx"等
    // 提取的名字传给AiAssistantPanel，AI回复时能正确称呼对方而非用户自己
    _extractOriginalSender: function(html) {
      if (!html) return ''
      var text = html.replace(/<[^>]+>/g, '').replace(/&nbsp;/g, ' ').replace(/&amp;/g, '&')
      var lines = text.split('\n')
      for (var i = 0; i < lines.length; i++) {
        var line = lines[i].trim()
        var match = line.match(/(?:发件人|寄件者|发送人|From|Sender)[：:]\s*(.+)/i)
        if (match && match[1]) {
          var name = match[1].trim()
          var emailIdx = name.indexOf('<')
          if (emailIdx > 0) { name = name.substring(0, emailIdx).trim() }
          if (name.indexOf('@') !== -1 && !/[一-龥]/.test(name)) {
            name = name.split('@')[0]
          }
          return name
        }
      }
      return ''
    },
    // 2026-06-01 AI助手 end
    // 将AI输出的文本清理为适合插入编辑器的HTML
    cleanAiContentToHtml: function(text) {
      if (!text) return '';
      var result = text;
      // 去除可能残留的think/thinking标签及内容
      result = result.replace(/<think[\s\S]*?<\/think>/gi, '');
      result = result.replace(/<thinking[\s\S]*?<\/thinking>/gi, '');
      // 去除蓝色虚线（#1E9FFF）及其后的模板内容
      var blueIdx = result.indexOf('#1E9FFF');
      if (blueIdx === -1) blueIdx = result.indexOf('#1e9fff');
      if (blueIdx > 0) {
        var tagStart = result.lastIndexOf('<', blueIdx);
        if (tagStart > 0) {
          result = result.substring(0, tagStart).trim();
        }
      }
      // 检测AI输出是否包含HTML结构标签
      var isHtml = /<(?:p|br|div|span|strong|em|ul|ol|li|h[1-6])\b/i.test(result);
      if (isHtml) {
        // HTML模式：先转为纯文本，再做清理，最后重新分段
        // 将<br>和</p>转为换行
        result = result.replace(/<br\s*\/?>/gi, '\n');
        result = result.replace(/<hr\s*\/?>/gi, '\n');
        result = result.replace(/<\/p>/gi, '\n');
        result = result.replace(/<p[^>]*>/gi, '');
        // 去除所有HTML标签
        result = result.replace(/<[^>]+>/g, '');
        // 转换HTML实体
        result = result.replace(/&nbsp;/g, ' ').replace(/&amp;/g, '&').replace(/&lt;/g, '<').replace(/&gt;/g, '>').replace(/&quot;/g, '"');
        // 合并多个空行
        result = result.replace(/\n{3,}/g, '\n\n');
        result = result.trim();
      }
      // 纯文本阶段（无论原本是HTML还是纯文本，到这里都是纯文本）
      // 截断分隔线（多个---）及其后的内容
      var sepMatch = result.match(/\n\s*-{3,}\s*\n/);
      if (sepMatch) {
        result = result.substring(0, sepMatch.index).trim();
      }
      // 去除markdown标记
      result = result.replace(/\*\*\*(.+?)\*\*\*/g, '$1');
      result = result.replace(/\*\*(.+?)\*\*/g, '$1');
      result = result.replace(/\*(.+?)\*/g, '$1');
      result = result.replace(/^#{1,6}\s+/gm, '');
      result = result.replace(/```[\s\S]*?```/g, function(match) {
        return match.replace(/```\w*\n?/g, '').replace(/```/g, '');
      });
      result = result.replace(/`([^`]+)`/g, '$1');
      result = result.replace(/\[([^\]]+)\]\([^)]+\)/g, '$1');
      // 去除引导语首行
      result = result.replace(/^(?:好的[，,]?\s*)?(?:以下是|总结如下|修改如下|润色如下|回复如下|这是一封|这是一篇|这是为您|下面是一封|下面是一篇|为您撰写|为您起草|为您准备)[^\n]*\n?/i, '');
      result = result.replace(/^(?:好的[，,])\s*(?:这|以下|下面|为您)[^\n]*\n?/i, '');
      // 去除末尾括号说明
      result = result.replace(/[（(][^）)]*(?:适配|根据|参考|按照|原始邮件)[^）)]*[）)]/g, '');
      result = result.trim();
      // 将文本段落转为HTML
      var paragraphs = result.split(/\n\s*\n/);
      var html = '';
      for (var i = 0; i < paragraphs.length; i++) {
        var para = paragraphs[i].trim();
        if (!para) continue;
        var lines = para.split('\n');
        var inner = '';
        for (var j = 0; j < lines.length; j++) {
          var line = lines[j];
          line = line.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
          if (j > 0) inner += '<br>';
          inner += line;
        }
        html += '<p>' + inner + '</p>';
      }
      return html || '<p>' + result.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;') + '</p>';
    },
    // ===== AI助手功能：methods方法 END =====

    /**
     * 选择密级弹框取消点击事件
     * @Date: 2022-06-23 17:19:03
     */
    handleSelectSecretConcel() {
      document.querySelector(".mid").style.display = "flex";
      document.querySelector(".resize").style.display = "flex";
      document.querySelector(".right").style.width = "auto";
      this.$router.back();
    },
    /**
     * 取消自动保存并退出
     * @Date: 2022-06-29 11:30:10
     * @return {*}
     */
    async cancelSave() {
      // console.log('this.firstAutosaveTime', this.firstAutosaveTime)
      // console.log('this.msg.affairId', this.msg)
      this.sendtie = true;
      const res = await api.cancelAutosaveApi(
        this.firstAutosaveTime,
        this.msg.affairId
      );
      this.$store.commit("changeCancelSaveDraftRandom");
      this.getnum();
    },

    /**
     * 监听收件人移除项
     */
    removeReceiverTag() {
      console.log(this.saveSubmitIds)
      if (Array.isArray(this.saveSubmitIds) && this.saveSubmitIds.length != 0) {
        this.msg.receivers = this.saveSubmitIds.map((item) => item.value);
      }
      if (this.msg.receivers.length == 0) {
        // this.focusSearch();
      }
      else{

      }
      //  <!-- 屏蔽邮件审批  -->
      // this.emailApproval(false);
    },

    /**
     * 监听抄送人移除项
     */
    removeCopyReceiverTag() {
      //   lyy====================
      if (
        Array.isArray(this.copySaveSubmitIds) &&
        this.copySaveSubmitIds.length != 0
      )
        this.msg.copyReceivers = this.copySaveSubmitIds.map(
          (item) => item.value
        );
      if (this.msg.copyReceivers && this.msg.copyReceivers.length == 0) {
        // this.copyReceiversFocusSearch();
      }
      //  <!-- 屏蔽邮件审批  -->
      // this.emailApproval(false);
    },

    /**
     * 监听审批人人移除项
     */
    removeApproveTag() {
      //   lyy
      if (
        Array.isArray(this.approveSaveSubmitIds) &&
        this.approveSaveSubmitIds.length != 0
      )
        this.msg.approver = this.approveSaveSubmitIds.map((item) => item.value);
      if (this.msg.approver && this.msg.approver.length == 0) {
        // this.approverFocusSearch();
      }
    },

    /**
     * 收件人 输入发请求
     */
    focusSearch() {
      this.receiversOptions = [];
      let obj = {
        key: "",
        isS: "2",
      };
      // loadingInstance.close();
      api.searchQueryStaff(obj).then((res) => {
        const list = res.msg || res.data || res;
        if (Array.isArray(list)) {
          this.searchList = list;
          list.forEach((item) => {
            this.receiversOptions.push({
              ...item,
              operationName: `【${item.miji||''}】 ${item.name} (${item.loginName}-${item.department})`,
              value: item.name + `(${item.department})`,
            });
          });
        }
      });
    },
    /**
     * lyy
     * 抄送人 输入发请求
     */
    copyReceiversFocusSearch() {
      this.copyReceiversOptions = [];
      let obj = {
        key: "",
        isS: "2",
      };
      api.searchQueryStaff(obj).then((res) => {
        
          // loading.close()
          // console.log("搜索人员", res.data);
          (res.msg || res.data || []).forEach((item) => {
            this.copyReceiversOptions.push({
              ...item,
              operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
              value: item.name + `(${item.department})`,
            });
          });
        }
      });
    },
    /**
     * lyy
     * 审批人 输入发请求
     */
    approverFocusSearch() {
      this.approverOptions = [];
      let obj = {
        key: "",
        isS: "1",
      };
      api.searchQueryStaff(obj).then((res) => {
        
          // loading.close()
          // console.log("搜索人员", res);
          (res.msg || res.data || []).forEach((item) => {
            this.searchName = item.name;
            this.searchValue = `Member|${item.memberId}`;
            this.approverOptions.push({
              ...item,
              operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
              value: item.name + `(${item.department})`,
            });
          });
        }
      });
    },

    selectBlur(e) {
      //   console.log(111111111111);
      //this.flag 防抖
      this.receiversOptions = [];
      // console.log(e, "e的值");
      if (e != "") {
        let obj = {
          key: e + "",
          isS: "2",
        };
        // if (this.flag) return;
        // this.flag = true;
        if (this.timer) {
          clearTimeout(this.timer);
        }
        if (e) {
          this.timer = setTimeout(() => {
            api.searchQueryStaff(obj).then((res) => {
              // console.log("搜索人员", res);
              
                this.searchList = res.msg || res.data;
                (res.msg || res.data || []).forEach((item) => {
                  this.receiversOptions.push({
                    ...item,
                    operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
                    value: item.name + `(${item.department})`,
                  });
                });
              }
            });
          }, 600);
        } else {
          api.searchQueryStaff(obj).then((res) => {
            // console.log("搜索人员", res);
            
              this.searchList = res.msg || res.data;
              (res.msg || res.data || []).forEach((item) => {
                this.receiversOptions.push({
                  ...item,
                  operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
                  value: item.name + `(${item.department})`,
                });
              });
            }
          });
        }
      } else {
        // lyy
        // this.focusSearch();
      }
    },
    /**
     * 组件选中项触发的change事件
     * @param {Object} val 选中数据的集合
     */
    selectChange(val) {
      if (val.length !== 0 && val.length >= this.msg.receivers.length) {
        // console.log("新增", this.receiversOptions);
        let receiverObj = this.receiversOptions.find(
          (receiverItem) => receiverItem.memberId == val[val.length - 1]
        );
        if (receiverObj) {
          this.saveSubmitIds.push({
            textName: receiverObj.value,
            text: receiverObj.name,
            value: `Member|${receiverObj.memberId}`,
          });
          this.msg.receivers.push(`Member|${receiverObj.memberId}`);
          this.msg.receiversStr.push(receiverObj.value);
        }
        // this.$nextTick(() => {
        //   this.$refs.receiver.blur();
        // });
      } else {
        if (val.length > 0) {
          const newArr = [];
          val.forEach((item) => {
            this.saveSubmitIds.forEach((e) => {
              if (item == e.value.split("|")[1]) {
                newArr.push(e);
              }
            });
          });
          this.saveSubmitIds = [...newArr];
        } else {
          this.msg.receiversStr = [];
          this.msg.receivers = [];
          this.saveSubmitIds = [];
        }
      }
      if (
        Array.isArray(this.saveSubmitIds) &&
        this.saveSubmitIds.length != 0 &&
        this.msg.receiversStr.length > 0
      ) {
        this.msg.receivers = this.saveSubmitIds.map((item) => item.value);
        this.msg.receiversStr = this.saveSubmitIds.map(
          (item) => item.textName || item.text
        );
      }
      if (this.msg.receiversStr.length == 0) {
        this.msg.receivers = [];
        this.msg.receiversStr = [];
      }
    },

    selectClear() {
      this.msg.receiversStr = "";
    },
    /***
     * 抄送人
     * * e 的值为  输入的值
     */
    selectBlurCopyReceiver(e) {
      this.copyReceiversOptions = [];
      if (e != "") {
        let obj = {
          key: e + "",
          isS: "2",
        };
        if (this.timer) {
          clearTimeout(this.timer);
        }
        if (e) {
          this.timer = setTimeout(() => {
            //  // 开始加载
            //     let loading = this.$loading({
            //       lock: true,//lock的修改符--默认是false
            //       text: "加载中，请稍候...",//显示在加载图标下方的加载文案
            //       background: "rgba(0,0,0,0.8)",//遮罩层颜色
            //       spinner: "el-icon-loading",//自定义加载图标类名
            //     });
            api.searchQueryStaff(obj).then((res) => {
              
                // loading.close()
                // console.log("搜索人员", res.data);
                (res.msg || res.data || []).forEach((item) => {
                  this.copyReceiversOptions.push({
                    ...item,
                    operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
                    value: item.name + `(${item.department})`,
                  });
                });
              }
            });
          }, 600);
        } else {
          api.searchQueryStaff(obj).then((res) => {
            
              // console.log("搜索人员", res.data);
              (res.msg || res.data || []).forEach((item) => {
                this.copyReceiversOptions.push({
                  ...item,
                  operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
                  value: item.name + `(${item.department})`,
                });
                this.flag = true;
              });
            }
          });
        }
      } else {
        //   lyy
        // this.copyReceiversFocusSearch();
      }
    },

    selectChangeCopyReceiver(val) {
      //   console.log("修改内容", val);
      if (val.length !== 0 && val.length >= this.msg.copyReceivers.length) {
        let receiverObj = this.copyReceiversOptions.find(
          (receiverItem) => receiverItem.memberId == val[val.length - 1]
        );
        if (receiverObj) {
          this.copySaveSubmitIds.push({
            textName: receiverObj.value,
            text: receiverObj.name,
            value: `Member|${receiverObj.memberId}`,
          });
          this.msg.copyReceivers.push(`Member|${receiverObj.memberId}`);
          this.msg.copyReceiversStr.push(receiverObj.value);
        }
        // this.$nextTick(() => {
        //   this.$refs.copyReceiver.blur();
        // });
      } else {
        // console.log(this.copySaveSubmitIds);
        if (val.length > 0) {
          const newArr = [];
          val.forEach((item) => {
            this.copySaveSubmitIds.forEach((e) => {
              if (item == e.value.split("|")[1]) {
                newArr.push(e);
              }
            });
          });
          this.copySaveSubmitIds = [...newArr];
        } else {
          this.copySaveSubmitIds = [];
          this.msg.copyReceivers = [];
          this.msg.copyReceiversStr = [];
        }
      }

      //   // lyy
      if (
        Array.isArray(this.copySaveSubmitIds) &&
        this.copySaveSubmitIds.length != 0 &&
        this.msg.copyReceiversStr.length > 0
      ) {
        this.msg.copyReceivers = this.copySaveSubmitIds.map(
          (item) => item.value
        );
        this.msg.copyReceiversStr = this.copySaveSubmitIds.map(
          (item) => item.textName || item.text
        );
      }
      if (this.msg.copyReceiversStr.length == 0) {
        this.msg.copyReceivers = [];
        this.msg.copyReceiversStr = [];
      }

      //   console.log("this.copySaveSubmitIds", this.copySaveSubmitIds);
      //   console.log(
      //     "this.copyReceivers",
      //     this.msg.copyReceivers,
      //     this.msg.copyReceiversStr
      //   );
    },

    selectClearCopyReceiver() {
      this.msg.copyReceiversStr = "";
    },
    /***
     * 审批人
     * * e 的值为  输入的值
     */
    selectBlurApprover(e) {
      this.approverOptions = [];
      //   console.log("发请求");
      if (e != "") {
        let obj = {
          key: e + "",
          isS: "1",
        };
        if (this.timer) {
          clearTimeout(this.timer);
        }
        if (e) {
          this.timer = setTimeout(() => {
            //  // 开始加载
            //   let loading = this.$loading({
            //     lock: true,//lock的修改符--默认是false
            //     text: "加载中，请稍候...",//显示在加载图标下方的加载文案
            //     background: "rgba(0,0,0,0.8)",//遮罩层颜色
            //     spinner: "el-icon-loading",//自定义加载图标类名
            //   });
            api.searchQueryStaff(obj).then((res) => {
              
                // loading.close()
                // console.log("搜索人员", res);
                (res.msg || res.data || []).forEach((item) => {
                  this.searchName = item.name;
                  this.searchValue = `Member|${item.memberId}`;
                  this.approverOptions.push({
                    ...item,
                    operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
                    value: item.name + `(${item.department})`,
                  });
                });
              }
            });
          }, 600);
        } else {
          api.searchQueryStaff(obj).then((res) => {
            
              //   console.log("搜索人员", res);
              (res.msg || res.data || []).forEach((item) => {
                this.searchName = item.name;
                this.searchValue = `Member|${item.memberId}`;
                this.approverOptions.push({
                  ...item,
                  operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
                  value: item.name + `(${item.department})`,
                });
              });
            }
          });
        }
      } else {
        //   lyy
        // this.approverFocusSearch();
      }
    },

    selectChangeApprover(val) {
      //   console.log("修改内容", val);
      //  新增
      if (val.length !== 0 && val.length >= this.msg.approver.length) {
        let receiverObj = this.approverOptions.find(
          (receiverItem) => receiverItem.memberId == val[val.length - 1]
        );
        if (receiverObj) {
          this.approveSaveSubmitIds.push({
            textName: receiverObj.value,
            text: receiverObj.name,
            value: `Member|${receiverObj.memberId}`,
          });
          this.msg.approver.push(`Member|${receiverObj.memberId}`);
          this.msg.approverStr.push(receiverObj.value);
        }
        // this.$nextTick(() => {
        //   this.$refs.approver.blur();
        // });
      } else {
        if (val.length > 0) {
          const newArr = [];
          val.forEach((item) => {
            this.approveSaveSubmitIds.forEach((e) => {
              if (item == e.value.split("|")[1]) {
                newArr.push(e);
              }
            });
          });
          this.approveSaveSubmitIds = [...newArr];
        } else {
          this.msg.approver = [];
          this.msg.approverStr = [];
          this.approveSaveSubmitIds = [];
        }
      }
      if (
        Array.isArray(this.approveSaveSubmitIds) &&
        this.approveSaveSubmitIds.length != 0 &&
        this.msg.approverStr.length > 0
      ) {
        this.msg.approver = this.approveSaveSubmitIds.map((item) => item.value);
        this.msg.approverStr = this.approveSaveSubmitIds.map(
          (item) => item.textName || item.text
        );
      }
      if (this.msg.approverStr.length == 0) {
        this.msg.approver = [];
        this.msg.approverStr = [];
      }

      //   console.log("this.approve/r", this.msg.approver, this.msg.approverStr);
    },

    selectClearApprover() {
      this.msg.approverStr = [];
    },

    //弹出框选择密级类型点击确定
    selectSecretType() {
      if (!this.msg.secretTypeId) {
        // console.log("请选择密级！");
        this.$alert("请选择密级!", "提示", {
          confirmButtonText: "确定",
          type: "warning",
        })
          .then(() => {})
          .catch((_) => {});
      } else {
        this.dialogVisible = false;
      }
    },

    /**
     * @function void:pushRealTimeData 通过axios轮询像后端发送请求去保存邮件的临时信息
     */
    pushRealTimeData() {
      let self = this;
      self.msg.type = "draft";
      let arr = [];
      self.fileList.forEach((d) => {
        arr.push(d.fileUrl);
      });
      self.msg.attachments = arr.toString();
      self.msg.autosave = "1";
      self.msg.firstAutosaveTime = self.firstAutosaveTime;

      let sendData = {
        ...this.msg,
        copyReceiversStr: this.msg.copyReceiversStr.toString(),
        receiversStr: this.msg.receiversStr.toString(),
        approverStr: this.msg.approverStr.toString(),
        approver: this.msg.approver.toString(),
        receivers: this.msg.receivers.toString(),
        copyReceivers: this.msg.copyReceivers.toString(),

        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 start*/
        sourceIdPlugin : this.myConfig.sourceIdPlugin,
        virtualIdPlugin : this.myConfig.virtualIdPlugin,
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 end*/

      };
      api.sendemail(sendData).then((res) => {
        if (self.timer) {
          clearTimeout(self.timer);
        }
        self.timer = setTimeout(() => {
          if (self.isResquest) {
            self.pushRealTimeData();
          } else {
            return;
          }
        }, 100 * 1000);
      });
    },
    //密级类型的change事件 ,重新切换密级类型，保存原先的选中密级
    cct(n) {
      if (n) {
        this.secretTypeIdold = this.msg.secretTypeId;
      }
    },
    //下载文件
    downflie(file) {
      /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240711 start*/
      let sourceIdPluginTemp = "";
      let virtualIdPluginTemp = "";
      if (this.myConfig.sourceIdPlugin){
        sourceIdPluginTemp = this.myConfig.sourceIdPlugin;
      }
      if (this.myConfig.virtualIdPlugin){
        virtualIdPluginTemp = this.myConfig.virtualIdPlugin;
      }

      window.location.href = encodeURI(
        `/api/attachment/download/${encodeURI(file.fileUrl)}?createDate=${
          file.createdate
        }&fileName=${encodeURIComponent(file.filename)}&moduleTypePlugin=12&sourceIdPlugin=${sourceIdPluginTemp}&virtualIdPlugin=${virtualIdPluginTemp}`
      );
      /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240711 end*/
    },
    //校验邮件内容是否必填
    jycs() {
      // if (
      //   this.msg.subject == "" ||
      //   this.msg.receiversStr == "" ||
      //   this.msg.subject == null ||
      //   this.msg.receiversStr == null
      // ) {
      //   this.$message.warning("请填入必要信息");
      //   return false;
      // } else if (
      //   this.msg.timedTask == true &&
      //   (this.msg.timingDate == undefined || this.msg.timingDate == "")
      // ) {
      //   this.$message.warning("请填入定时信息");
      //   return false;
      // } else if (
      //   ((this.msg.kuaWangdis && this.$store.state.isredNet) ||
      //     this.msg.kuaWang) &&
      //   !this.msg.approverStr
      // ) {
      //   this.$message.warning("请选择审批人");
      //   return false;
      // } else {
      //   console.log(this.msg.timedTask == true && this.msg.timingDate == "");
      //   return true;
      // }
      if (
        this.msg.subject == "" ||
        this.msg.receiversStr == "" ||
        this.msg.subject == null ||
        this.msg.receiversStr == null
      ) {
        this.$message.warning("请填入必要信息");
        return false;
      } else if (
        this.msg.timedTask == true &&
        (this.msg.timingDate == undefined || this.msg.timingDate == "")
      ) {
        this.$message.warning("请填入定时信息");
        return false;
      } else if (
        ((this.msg.kuaWangdis && this.$store.state.isredNet) ||
          this.msg.kuaWang) &&
        this.msg.approver.length == 0
      ) {
        this.$message.warning("请选择审批人");
        return false;
      } else {
        // console.log(this.msg.timedTask == true && this.msg.timingDate == "");
        return true;
      }
    },
    //刷新事件
    refnav() {
      this.$emit("refleftnav");
    },
    //删除文件
    delflie(index) {
      this.fileList.splice(index, 1);
    },
    //上传文件
    uploadFile(file) {
      this.ifFileUploaded = false;
      let break_on = true;
      this.maxFileLen = 0;
      this.num++;
      if (this.num == this.files.length) {
        // console.log("文件上传",this.files);
        this.num = 0;
        let from = new FormData();
        this.files.forEach((d) => {
          let size = (d.raw.size / 1024 / 1024).toFixed(2);
          if (size >= this.maxFileSize) {
            break_on = false;
          } else {
            from.append("file", d.raw);
          }
        });

        //wxt.万云龙 注释邮件附件分区 20230728 start
        from.append("useMyPathAppType", "mail");
        //wxt.万云龙 注释邮件附件分区 20230728 end

        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240711 start*/
        //1
        from.append("moduleTypePlugin", "12");
        if (this.myConfig.sourceIdPlugin){
          from.append("sourceIdPlugin", this.myConfig.sourceIdPlugin);
        }
        if ( this.myConfig.virtualIdPlugin){
          from.append("virtualIdPlugin", this.myConfig.virtualIdPlugin);
        }
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240711 end*/

        if (break_on) {
          this.loading = true;
          const onUploadProgress = (progressEvent) => {
            let percent =
              ((progressEvent.loaded / progressEvent.total) * 100) | 0;
            this.percentage = `上传进度：${percent}%`;
          };
          api
            .loadflie(from, onUploadProgress)
            .then((res) => {
              //   console.log(res);
              // lyy  没有主题就将文件名替换为主题
              if (!this.msg.subject) {
                this.msg.subject = res.atts[0].filename.split(".")[0];
              }
              this.$refs.upload.clearFiles();
              this.files = [];
              if (res.code == "1") {
                this.$message.error(res.message);
                this.loading = false;
                this.percentage = 0;
                return;
              }
              res.atts.forEach((i) => {
                i.size = (i.size / 1024 / 1024).toFixed(2) + "M";
                this.fileList.push(i);
              });
              this.loading = false;
              this.ifFileUploaded = true;
            })
            .catch((err) => {
              // 当上传文件出错时，将进度清空，进度条隐藏，给出提示
              //   console.log(err);
              this.$message.error("上传超时");
              this.percentage = 0;
              this.loading = false;
            });
        } else {
          this.$message.error("文件大小超出限制");
        }
      }
    },
    //切换密级类型的change事件
    MailFileSecretVerify() {
      this.msg.kuaWangdis = true;
      this.msg.kuaWang = false;
      // 切换密级，审批人清空 fuguichuan
      this.approveSaveSubmitIds=[]
      this.approverOptions=[]
      this.msg.approverStr=[]
      this.approverStr=[]
      //切换密级信息不需要弹出提示信息供用户感知  20210908 10:28 modify yangyanhua  Demander:dengyawen
      // this.$confirm('密级变更后需要重新编辑当前邮件，您确定要改变密级吗?', '提示', {
      //   confirmButtonText: '确定',
      //   cancelButtonText: '取消',
      //   showClose:false,
      //   type: 'warning'
      // }).then(() => {
      //重新选择密级，不清空收件人，选了不符合密级状态的人员，再点击发送时需要给出提示信息，让客户自己清空不符合要求的人员信息
      // this.msg.receiversStr = ""
      // this.msg.copyReceiversStr = ""//不清空抄送
      //外层，跨网是否需要禁用
      api
        .MailFileSecretVerify({ secret: this.msg.secretTypeId })
        .then((res) => {
          //   console.log(res);
          if (res.code != 10001) {
            this.$nextTick().then(() => {
              this.msg.secretTypeId = this.secretTypeIdold;
              this.msg.kuaWangdis = true; //是否跨网禁用
              this.$forceUpdate();
            });
            this.$message.error(res.msg);

            api
              .MailFileSecretVerify({ secret: this.msg.secretTypeId })
              .then((res) => {
                // this.fileList = []
                let obj = {
                  secretTypeId: this.msg.secretTypeId,
                  fileNames: [],
                };
                this.fileList.forEach((d) => {
                  obj.fileNames.push(d.filename);
                });
                obj.fileNames = obj.fileNames.toString();

                api.isSecret(obj).then((res) => {
                  this.$nextTick().then(() => {
                    this.msg.kuaWangdis = res.msg.isSecret;
                    this.$forceUpdate();
                  });

                  if (res.code == 10001) {
                    if (res.msg.checkFileIsClear == true) {
                      this.$confirm(
                        "文件列表中文件密级超出, 是否清空文件列表?",
                        "提示",
                        {
                          confirmButtonText: "确定",
                          cancelButtonText: "取消",
                          showClose: false,
                          type: "warning",
                        }
                      )
                        .then(() => {
                          this.$nextTick().then(() => {
                            this.fileList = [];
                            this.secretTypeIdold = this.msg.secretTypeId;
                            this.msg.kuaWangdis = res.msg.isSecret;
                          });
                        })
                        .catch(() => {
                          this.$nextTick().then(() => {
                            this.msg.secretTypeId = this.secretTypeIdold;
                            // console.log(this.secretTypeIdold);
                            this.$forceUpdate();
                          });
                        });
                    }
                    if (res.msg.isSecret == true) {
                      // this.$message.warning("涉密邮件不可跨网")
                      this.msg.kuaWang = false;
                      this.msg.kuaWangdis = true;
                      // this.secretTypeIdold = this.msg.secretTypeId
                    }
                  } else {
                    this.$message.warn(res.msg);
                  }

                  this.key += 1;
                });
              });
          } else {
            // this.fileList = []
            let obj = {
              secretTypeId: this.msg.secretTypeId,
              fileNames: [],
            };
            this.fileList.forEach((d) => {
              obj.fileNames.push(d.filename);
            });
            obj.fileNames = obj.fileNames.toString();

            api.isSecret(obj).then((res) => {
              this.$nextTick().then(() => {
                this.msg.kuaWangdis = res.msg.isSecret;
                // console.log(
                //   "---------------------------------------------------"
                // );
                // console.log(this.msg.kuaWangdis);
                this.$forceUpdate();
              });

              if (res.code == 10001) {
                if (res.msg.checkFileIsClear == true) {
                  this.$confirm(
                    "文件列表中文件密级超出, 是否清空文件列表?",
                    "提示",
                    {
                      confirmButtonText: "确定",
                      cancelButtonText: "取消",
                      showClose: false,
                      type: "warning",
                    }
                  )
                    .then(() => {
                      this.$nextTick().then(() => {
                        this.fileList = [];
                        this.secretTypeIdold = this.msg.secretTypeId;
                        this.msg.kuaWangdis = res.msg.isSecret;
                      });
                    })
                    .catch(() => {
                      this.$nextTick().then(() => {
                        this.msg.secretTypeId = this.secretTypeIdold;
                        // console.log(this.secretTypeIdold);
                        this.$forceUpdate();
                      });
                    });
                }
                if (res.msg.isSecret == true) {
                  // this.$message.warning("涉密邮件不可跨网")
                  this.msg.kuaWang = false;
                  this.msg.kuaWangdis = true;
                  // this.secretTypeIdold = this.msg.secretTypeId
                }
              } else {
                this.$message.warn(res.msg);
              }

              this.key += 1;
            });
            // if (this.msg.secretTypeId == -637209554414910365){
            //     this.$message.warning("涉密邮件不可跨网")
            //     this.msg.kuaWang = false
            // }
          }
        });

      this.$forceUpdate();
      // }).catch(() => {
      //   this.msg.secretTypeId = this.secretTypeIdold

      // });
      //   console.log(this.msg.secretTypeId);
    },
    // 上传进度
    uploadVideoProcess(event, file, fileList) {
      this.loadProgress = parseInt(event.percent); // 动态获取文件上传进度
      if (this.loadProgress >= 100) {
        this.loadProgress = 100;
        // setTimeout( () => {this.progressFlag = false}, 1000) // 一秒后关闭进度条
      }
    },

    beforeFeedBackExports(file, fileList, isDragUpload) {
      let obj = {
        fileNames: [],
        fslId: this.msg.secretTypeId,
      };
      console.log('this.msg.secretTypeId',this.msg.secretTypeId)
      fileList.forEach((d) => {
        obj.fileNames.push(d.name);
      });
      obj.fileNames = obj.fileNames.toString();
      let currLength = fileList.length;
      this.maxFileLen = Math.max(currLength, this.maxFileLen);
      // this.files = fileList
      setTimeout(() => {
        if (currLength == this.maxFileLen) {
          this.files = fileList;
          if (this.$store.state.isredNet) {
            console.log(fileList);
            api.MailFileSecret(obj).then((res) => {
              
                this.$message.success("文件密级校验成功");
                // 兼容匹配拖拽上传
                if (isDragUpload === "dragUpload") {
                  if (this.files.length > 0 && this.num === 0) {
                    this.num = this.files.length - 1;
                  }
                  this.uploadFile();
                }
                this.$refs.upload.submit();
              } else {
                if (res.msg == "附件密级不能大于文件密级！") {
                  this.$message.error("附件密级不能大于邮件密级！");
                } else {
                  this.$message.error(res.msg);
                }
                this.maxFileLen = 0;
              }
              this.$refs.upload.clearFiles();
            });
          } else {
            // 兼容匹配拖拽上传
            if ((isDragUpload = "dragUpload")) {
              if (this.files.length > 0 && this.num === 0) {
                this.num = this.files.length - 1;
              }
              this.uploadFile();
            }
            this.$refs.upload.submit();
          }
        }
      });
    },

    // 校验
    jy() {
      let obj = {
        fileNames: [],
        fslId: this.msg.secretTypeId,
      };
      this.files.forEach((d) => {
        obj.fileNames.push(d.name);
      });
      obj.fileNames = obj.fileNames.toString();
      api.MailFileSecret(obj).then((res) => {
        // console.log(res);
        
          this.$message.success("文件密级校验成功");
          // this.$refs.upload.submit();
        } else {
          this.$message.error(res.msg);
          this.$refs.upload.handleRemove(file);
          this.$refs.upload.handleRemove(file);
        }
      });
    },

    beforeFeedBackExport(file, fileList) {
      let obj = {
        fileNames: file.name,
        fslId: this.msg.secretTypeId,
      };
      // fileList.forEach( d =>{
      //     obj.fileNames.push(d.name)
      // })
      // obj.fileNames =  obj.fileNames.toString()
      //
      // let currLength = fileList.length
      // this.maxFileLen = Math.max(currLength,this.maxFileLen)
      //

      api.MailFileSecret(obj).then((res) => {
        // console.log(res);
        
          this.$message.success("文件密级校验成功");
          this.$refs.upload.submit();
        } else {
          this.$message.error(res.msg);
          this.$refs.upload.handleRemove(file);
        }
      });
      // setTimeout(() =>{
      //     if (currLength != this.maxFileLen) return
      //     api.MailFileSecret(obj).then(res => {
      //         console.log(res)
      //         
      //             this.$message.success('文件密级校验成功');
      //             this.$refs.upload.submit();
      //         } else {
      //             this.$message.error(res.msg);
      //             this.$refs.upload.handleRemove(file);
      //
      //         }
      //     })
      // })

      // fileList.forEach( d =>{
      //     obj.fileNames.push(d.name)
      // })
      // let currLength = fileList.length

      // obj.fileNames = obj.fileNames.toString()
      // this.maxFileLen = Math.max (currLength,this.maxFileLen)
      // setTimeout(() =>{
      //     if(currLength == this.maxFileLen) {
      //     api.MailFileSecret(obj).then(res =>{
      //         console.log(res)
      //         
      //             this.$refs.upload.submit();
      //         }else {
      //             this.$message.error(res.msg);
      //             this.$refs.upload.abort();
      //
      //         }
      //     })
      //     }
      // },0)

      // const isLt2M = (file.size / 1024 / 1024 < 10);
      // if (!extension) {
      //     this.$message({
      //         message: '上传文件只能是xls/xlsx!',
      //         type: 'warning'
      //     });
      //     this.fileUploadList = []
      //     return false;
      // }
      // if (!isLt2M) {
      //     this.$message({
      //         message: "文件大小不可以超过10M",
      //         type: 'warning'
      //     });
      //     this.fileUploadList = []
      //     return false;
      // }
      // return (extension) && isLt2M
    },

    async save() {
      //在点击保存草稿 ，需要将标志重新恢复初值
      if (this.$store.state.isWriteEamil) {
        this.$store.commit("setWriteEamil", false);
      }
      this.msg.type = "draft";
      let arr = [];
      if (this.percentage != 0 && !this.ifFileUploaded) {
        return this.$message.warning("文件还在上传中...");
      }
      this.fileList.forEach((d) => {
        arr.push(d.fileUrl);
      });
      //   console.log("arr :>> ", arr);
      this.msg.attachments = arr.toString();

      //   console.log(this.msg);
      this.msg.timedTask = false;
      this.msg.autosave = "0";
      // 将收件人，抄送人，审批人转为字符串
      let sendData = {
        ...this.msg,
        copyReceiversStr: this.msg.copyReceiversStr.toString(),
        receiversStr: this.msg.receiversStr.toString(),
        approverStr: this.msg.approverStr.toString(),
        approver: this.msg.approver.toString(),
        receivers: this.msg.receivers.toString(),
        copyReceivers: this.msg.copyReceivers.toString(),
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 start*/
        sourceIdPlugin : this.myConfig.sourceIdPlugin,
        virtualIdPlugin : this.myConfig.virtualIdPlugin,
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 end*/
      };
      await api.sendemail(sendData).then((res) => {
        // console.log("执行了保存草稿1");
        if (res.code == "10001") {
          this.sendtie = true;
          this.$message.success(res.msg);
        } else {
          this.$message.warning(res.msg);
        }
      });
      this.getnum();
    },

    async saves() {
      this.msg.type = "draft";
      let arr = [];
      if (this.percentage != 0 && !this.ifFileUploaded) {
        return this.$message.warning("文件还在上传中...");
      }
      this.fileList.forEach((d) => {
        arr.push(d.fileUrl);
      });
      this.msg.attachments = arr.toString();
      this.msg.autosave = "0";
      this.msg.timedTask = false;
      let sendData = {
        ...this.msg,
        copyReceiversStr: this.msg.copyReceiversStr.toString(),
        receiversStr: this.msg.receiversStr.toString(),
        approverStr: this.msg.approverStr.toString(),
        approver: this.msg.approver.toString(),
        receivers: this.msg.receivers.toString(),
        copyReceivers: this.msg.copyReceivers.toString(),
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 start*/
        sourceIdPlugin : this.myConfig.sourceIdPlugin,
        virtualIdPlugin : this.myConfig.virtualIdPlugin,
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 end*/
      };
      await api.sendemail(sendData).then((res) => {
        // console.log("执行了保存草稿s");
        if (res.code == "10001") {
          this.sendtie = true;
          this.$message.success(res.msg);
          api.emlianum().then((res) => {
            // console.log(res);
              this.num = res.msg || res;
              this.$store.commit("setnum", res.msg || res);
              this.$store.commit("changeCancelSaveDraftRandom");
              this.$message.error(res.message);
            }
          });
          // this.getnum()
        } else {
          // console.log('自动存为草稿失败',res)
          this.$message.warning(res.msg);
        }
      });
      // this.getnum()
    },
    // 调用父元素传递的自定义方法--refleftnav
    getnum() {
      this.$emit("refleftnav");
    },

    uploadfile() {
      // this.$refs['upload'].$el.click()
      document.querySelector(".flieupload .avatar-uploader input").click();
      this.percentage = 0;
    },

    /** 拖拽功能的监听事件 tangxiangping 2022-06-06 start */
    dragenter(e) {
      this.preventDefault(e);
    },
    dragleave(e) {
      this.preventDefault(e);
    },
    dragover(e) {
      this.preventDefault(e);
    },
    enentDrop(e) {
      this.preventDefault(e);
      let fileData = e.dataTransfer.files;
      if (this.percentage != 0 && !this.ifFileUploaded) {
        return this.$message.warning("文件还在上传中...");
      }
      // 填充附件数据
      this.files = [
        ...this.files,
        ...Array.from(fileData).map((item) => {
          return {
            name: item.name,
            size: item.size,
            type: item.type,
            status: "ready",
            uid: item.lastModified,
            raw: item,
            percentage: 0,
          };
        }),
      ];
      // 判断如果文件类型为空并且文件
      const result = this.files.some((item) => {
        if (item.type == "" && item.size === 0) {
          this.$message.error(`文件夹${item.name}类型不支持`);
        }
        return item.raw
          ? item.raw.type === "" && item.raw.size === 0
          : item.type === "" && item.size === 0;
      });
      if (result) return (this.files = []);

      // 调用文件上传校验方法
      this.beforeFeedBackExports(
        fileData,
        Array.from(fileData).map((item) => {
          return {
            name: item.name,
            size: item.size,
            type: item.type,
            status: "ready",
            uid: item.lastModified,
            raw: item,
            percentage: 0,
          };
        }),
        "dragUpload" // 表示拖拽上传
      );
    },
    preventDefault(e) {
      e.stopPropagation();
      e.preventDefault(); //必填字段
    },
    /** 拖拽功能的监听事件 tangxiangping 2022-06-06 end */

    // 发送邮件
    async send() {
      console.log(this.msg.content)
      this.disabled = true
      // 控制点击发送按钮次数限制 fuguichuan
      setTimeout(()=>{
          this.disabled = false
        },100)
      //在点击发送邮件 ，需要将标志重新恢复初值
      if (this.$store.state.isWriteEamil) {
        this.$store.commit("setWriteEamil", false);
      }
      if (!this.jycs()) {
        return false;
      }

      // 是否需要选择 审批人
      if (!this.msg.kuaWangdis) {
        // 邮件发送前置审批
        //  <!-- 屏蔽邮件审批  -->
        // const checked = await this.emailApproval();
        // if (!checked) return;
      }

      this.msg.type = "send";
      let arr = [];
      //   console.log("editor 1146line", this.percentage, this.ifFileUploaded);
      if (this.percentage != 0 && !this.ifFileUploaded) {
        return this.$message.warning("文件还在上传中...");
      }
      if (this.sendFlag) {
        this.fileList.forEach((d) => {
          arr.push(d.fileUrl);
        });
        this.msg.attachments = arr.toString();
        let obj = this.$route.query.obj;
        // 将收件人，抄送人，审批人转为字符串
        const params = {
          ...this.msg,
          copyReceiversStr: this.msg.copyReceiversStr.toString(),
          receiversStr: this.msg.receiversStr.toString(),
          approverStr: this.msg.approverStr.toString(),
          approver: this.msg.approver.toString(),
          receivers: this.msg.receivers.toString(),
          copyReceivers: this.msg.copyReceivers.toString(),
          /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 start*/
          sourceIdPlugin : this.myConfig.sourceIdPlugin,
          virtualIdPlugin : this.myConfig.virtualIdPlugin,
          /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 end*/
        };
        console.log(
          "123object :>> ",
          params.copyReceiversStr,
          params.receiversStr,
          params.approverStr
        );
        // console.log("发送邮件参数", params);
        this.msg.autosave = "0";
        api.sendemail(params).then((res) => {
          // console.log("执行了发送邮件", res);
          if (res.code == 10001) {
            this.$message.success(res.msg);
            this.sendtie = true;
            this.$emit("refleftnav");
          } else {
            // console.log('res.msg2008 :>> ', res.msg);
            this.$message.warning(res.msg);
          }
          this.sendFlag = true;
        });
        this.sendFlag = false;
      }
    },

    /**
     * 邮件审批
     * @desc: 邮件发送前, 需对 收件人、抄送人 进行人员校验
     * @param {boolean} showTip: 是否展示错误提示
     * @return {boolean} 是否允许发送邮件
     */
    async emailApproval(showTip = true) {
      // 收件人、抄送人、密级
      const {
        receivers = [],
        copyReceivers = [],
        secretTypeId,
      } = this.msg || {};
      if (!secretTypeId || secretTypeId == "") return false;

      const loading = this.$loading({
        text: "邮件审批中",
      });

      const result = await api
        .emailApproval({ receivers, copyReceivers, secretTypeId })
        .then((res) => {
          if (res?.code != 10002) {
            if (showTip) this.$message.error(res?.message || "审批失败");
            this.msg.kuaWangdis = true;
            return false;
          }

          this.msg.kuaWangdis = false;
          return true;
        })
        .catch(() => {
          if (showTip) this.$message.error("审批失败");
          this.msg.kuaWangdis = true;
          return false;
        })
        .finally(() => {
          loading.close();
        });

      return result;
    },

    sends() {
      if (!this.jycs()) {
        return false;
      }
      this.msg.type = "send";
      let arr = [];
      if (this.percentage != 0 && !this.ifFileUploaded) {
        return this.$message.warning("文件还在上传中...");
      }
      this.fileList.forEach((d) => {
        arr.push(d.fileUrl);
      });

      let obj = this.$route.query.obj;

      this.msg.attachments = arr.toString();
      this.msg.autosave = "0";
      //   console.log(this.msg);

      let sendData = {
        ...this.msg,
        copyReceiversStr: this.msg.copyReceiversStr.toString(),
        receiversStr: this.msg.receiversStr.toString(),
        approverStr: this.msg.approverStr.toString(),
        approver: this.msg.approver.toString(),
        receivers: this.msg.receivers.toString(),
        copyReceivers: this.msg.copyReceivers.toString(),

        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 start*/
        sourceIdPlugin : this.myConfig.sourceIdPlugin,
        virtualIdPlugin : this.myConfig.virtualIdPlugin,
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 end*/

      };

      api.sendemail(sendData).then((res) => {
        // console.log("执行了发送邮件2");
        // console.log(res);
        if (res.code == 10001) {
          this.$message.success(res.msg);
          this.$emit("refleftnav");
        } else {
          // console.log('res.msg2040 :>> ', res.msg);
          this.$message.warning(res.msg);
        }
      });
    },
    uploadflieError() {
      // loading动画消失
      this.$message.error("文件上传失败");
    },

    beforeflieUpload() {},

    uploadflieSuccess(res, file) {
      // res为图片服务器返回的数据
      // 获取富文本组件实例
      // console.log(res)
      // if (res.atts.length != 0) {
      //     res.atts[0].size = (res.atts[0].size/1024/1024).toFixed(2) + 'M'
      //         this.fileList.push(res.atts)
      // } else {
      //
      // }
    },
    // 选择审批人
    selectapprover(e) {
      // lyy
      //   await this.approverFocusSearch();
      //   lyy end
      let _this = this;
      let peo = {
        text: "",
        value: "",
      };
      peo.text = this.approveSaveSubmitIds.map((item) => item.text).join(",");
      peo.value = this.approveSaveSubmitIds.map((item) => item.value).join(",");
      api.getuserInfo(this.msg.secretTypeId).then((res) => {
        // console.log(res);
        let user = [];
        // 存储部门id
        let departmentIds = [];
        res.msg.forEach((d) => {
          user.push(`Member|${d.id}`);
          // 存储
          departmentIds.push(d.orgDepartmentId);
        });

        // console.log("departmentIds", departmentIds);
        // 对部门id去重
        let setDepartmentIds = [...new Set(departmentIds)];
        // console.log("setDepartmentIds", setDepartmentIds);
        parent.$.selectPeople({
          // 授权时的组织机构面板内容
          type: "selectPeople",
          showMe: false,
          panels: "Member,Department",
          selectType: "Member",
          onlyLoginAccount: true,
          onlyCurrentDepartment: false,
          showDepartmentsOfTree: setDepartmentIds.join(","),
          includeElements: user.toString(),
          // isNeedCheckLevelScope: true,//不受职务级别控制
          hiddenPostOfDepartment: true,
          minSize: 0,
          customMaxSize: 3, //最多选择三人
          maxSize: 3, //最多选择三人
          excludeElements: "",
          params: {
            text: peo.text,
            value: peo.value,
          },
          fillBackData: {},
          callback(result) {
            // _this.$set(_this.basicInfo, "manager", result.text);
            // _this.$set(_this.basicInfo, "managerId", result.value.slice(7));
            // console.log("result", result.obj);
            let receiverArr = [];
            _this.msg.approverStr = [];
            _this.approveSaveSubmitIds = [];
            _this.msg.approver = result.value.split(",");
            _this.approverStr = [];
            let ids = [];
            _this.approverOptions = [];
            if (result.obj.length != 0) {
              result.obj.forEach((e) => {
                if (e.type == "Member") {
                  ids.push(e.id);
                } else {
                  _this.approverOptions.push({
                    ...e,
                    value: e.name,
                    operationName: e.name,
                  });
                }
                receiverArr.push(
                  `${e.name}${
                    e.description && e.description.split("部门:")[1]
                      ? "(" + e.description.split("部门:")[1].trim() + ")"
                      : ""
                  }`
                );
                _this.approveSaveSubmitIds.push({
                  textName: `${e.name}${
                    e.description && e.description.split("部门:")[1]
                      ? "(" + e.description.split("部门:")[1].trim() + ")"
                      : ""
                  }`,
                  text: e.name,
                  value: `${e.type}|${e.id}`,
                });
                _this.approverStr.push(e.id);
              });
              var arr = receiverArr.toString().split(",");
              _this.msg.approverStr.push(...arr);
              //   console.log(
              //     111111111111,
              //     _this.msg.approverStr,
              //     _this.msg.approver
              //   );
              if (ids.length > 0) {
                api.getPeopleMsg({ ids: ids.join(",") }).then((res) => {
                  //   console.log("获取options", res);
                  if (res.code == "1001") {
                    res.msg.forEach((item) => {
                      _this.approverOptions.push({
                        ...item,
                        operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
                        value: item.name + `(${item.department})`,
                      });
                    });
                  }
                });
              }
            } else {
              _this.msg.approver = [];
              _this.msg.approverStr = [];
              _this.approveSaveSubmitIds = [];
              // _this.approverFocusSearch();
            }
            // _this.msg.approver = [];
            // _this.approveSaveSubmitIds = [];
            // // _this.msg.receiversStr = result.text
            // _this.msg.approverStr = [];
            // if (result.obj.length != 0) {
            //   result.obj.forEach((e) => {
            //     receiverArr.push(
            //       `${e.name}${
            //         e.description && e.description.split("部门:")[1]
            //           ? "(" + e.description.split("部门:")[1] + ")"
            //           : ""
            //       }`
            //     );
            //     // _this.approverOptions.push({
            //     //   ...e,
            //     //   operationName: e.name,
            //     //   value: `${e.name}${
            //     //     e.description && e.description.split("部门:")[1]
            //     //       ? "(" + e.description.split("部门:")[1].trim() + ")"
            //     //       : ""
            //     //   }`,
            //     // });
            //     // // 重置审批人信息内容
            //     _this.approveSaveSubmitIds.push({
            //       textName: `${e.name}${
            //         e.description && e.description.split("部门:")[1]
            //           ? "( " + e.description.split("部门:")[1].trim() + ")"
            //           : ""
            //       }`,
            //       text: e.name,
            //       value: `${e.type}|${e.id}`,
            //     });
            //   });
            //   // _this.msg.approverStr = receiverArr.toString();
            //   // _this.msg.approver = result.value;
            //   // 分隔
            //   var arr4 = receiverArr.toString().split(",");
            //   _this.msg.approverStr.push(...arr4);
            //   let arr5 = new Set(_this.msg.approverStr); //去重
            //   _this.msg.approverStr = [...arr5];
            //   _this.msg.approver = result.value.split(",");
            // }
            e.target.blur();
          },
        });

        //   api.getroleId().then(res =>{
        //
        //     let role
        //     console.log(res)
        //
        //
        //   })
      });
    },
    // 选择收件人与抄送人
    setlepeo(e, n) {
      // console.log( Event)

      let _this = this;
      let peo = {
        text: "",
        value: "",
      };
      if (n == "receivers") {
        peo.text = this.saveSubmitIds.map((item) => item.text).join(",");
        peo.value = this.saveSubmitIds.map((item) => item.value).join(",");
      } else if (n == "copyReceivers") {
        // lyy
        // this.copyReceiversFocusSearch();
        //   lyy end
        peo.text = this.copySaveSubmitIds.map((item) => item.text).join(",");
        peo.value = this.copySaveSubmitIds.map((item) => item.value).join(",");
      }

      if (this.searchList) {
        this.searchList.forEach((item, index) => {
          this.msg.receiversStr.forEach((ele) => {
            if (ele.includes(item.name)) {
              this.searchName = item.name;
              this.searchValue = `Member|${item.memberId}`;
            }
          });
        });
      }
      //   console.log("????????????????");
      // peo.text = this.searchName;
      // peo.value = this.searchValue;
      parent.$.selectPeople({
        // 授权时的组织机构面板内容
        type: "selectPeople",
        panels: "Member,Department,Team,Account,Outworker",
        selectType: "Account,Department,Member,Team,Post,Outworker",
        onlyLoginAccount: false,
        // isNeedCheckLevelScope: true,//不受职务级别控制
        minSize: 0,
        maxSize: -1, //
        excludeElements: "",
        hiddenPostOfDepartment: true,
        params: {
          // text:"你好",
          // value:"Member|6428087213914279626"
          text: peo.text,
          value: peo.value,
        },
        fillBackData: {},
        callback(result) {
          // _this.$set(_this.basicInfo, "manager", result.text);
          // _this.$set(_this.basicInfo, "managerId", result.value.slice(7));
          //   console.log(
          //     "选择人回调方法-------------------------------------",
          //     result,
          //     result.obj
          //   );
          let receiverArr = [];
          if (n == "receivers") {
            _this.msg.receiversStr = [];
            _this.saveSubmitIds = [];
            _this.msg.receivers = result.value.split(",");
            _this.receiversStr = [];
            let ids = [];
            _this.receiversOptions = [];
            if (result.obj.length != 0) {
              result.obj.forEach((e) => {
                if (e.type == "Member") {
                  ids.push(e.id);
                } else {
                  // _this.receiversStr.push(e.name);
                  _this.receiversOptions.push({
                    ...e,
                    value: e.name,
                    operationName: e.name,
                  });
                }

                receiverArr.push(
                  `${e.name}${
                    e.description && e.description.split("部门:")[1]
                      ? "(" + e.description.split("部门:")[1].trim() + ")"
                      : ""
                  }`
                );

                _this.saveSubmitIds.push({
                  textName: `${e.name}${
                    e.description && e.description.split("部门:")[1]
                      ? "(" + e.description.split("部门:")[1].trim() + ")"
                      : ""
                  }`,
                  text: e.name,
                  value: `${e.type}|${e.id}`,
                });
                // 选人回显时显示的是id fuguichuan
                 _this.$nextTick(()=>{
                   _this.receiversStr.push(e.id);
                })
                //2022/10/07  _this.receiversStr.push(e.id) 改为name  因为通过选人回显时显示的是id
                // _this.receiversStr.push(e.name);

              });
              var arr = receiverArr.toString().split(",");
              _this.msg.receiversStr.push(...arr);
              //   console.log(
              //     111111111111,
              //     _this.msg.receivers,
              //     _this.msg.receiversStr
              //   );
              if (ids.length > 0) {
                api.getPeopleMsg({ ids: ids.join(",") }).then((res) => {
                  //   console.log("获取options", res);
                  if (res.code == "1001") {
                    if (res.msg.length > 0) {
                      res.msg.forEach((item) => {
                        // _this.receiversStr.push(item.name + `(${item.department})`);
                        _this.receiversOptions.push({
                          ...item,
                          operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
                          value: item.name + `(${item.department})`,
                        });
                      });
                    }
                  }
                });
              }
            } else {
              _this.msg.receivers = [];
              _this.msg.receiversStr = [];
              _this.saveSubmitIds = [];
              // _this.focusSearch();
            }
            // _this.msg.receiversStr = [];
            // _this.saveSubmitIds = [];
            // // _this.msg.receiversStr = result.text
            // if (result.obj.length != 0) {
            //   const newArr = [];
            //   result.obj.forEach((e) => {
            //     receiverArr.push(
            //       `${e.name}${
            //         e.description && e.description.split("部门:")[1]
            //           ? "(" + e.description.split("部门:")[1].trim() + ")"
            //           : ""
            //       }`
            //     );
            //     // 重置收件人信息内容
            //     _this.saveSubmitIds.push({
            //       textName: `${e.name}${
            //         e.description && e.description.split("部门:")[1]
            //           ? "(" + e.description.split("部门:")[1].trim() + ")"
            //           : ""
            //       }`,
            //       text: e.name,
            //       value: `${e.type}|${e.id}`,
            //     });
            //   });
            //   var arr = receiverArr.toString().split(",");
            //   _this.msg.receiversStr.push(...arr);
            //   let arrrr = new Set(_this.msg.receiversStr);
            //   _this.msg.receiversStr = [...arrrr];
            // _this.msg.receivers = result.value.split(",");
            // 选择抄送人
          } else if (n == "copyReceivers") {
            _this.msg.copyReceiversStr = [];
            _this.copySaveSubmitIds = [];
            _this.msg.copyReceivers = result.value.split(",");
            _this.copyReceiversStr = [];
            let ids = [];
            _this.copyReceiversOptions = [];
            if (result.obj.length != 0) {
              result.obj.forEach((e) => {
                if (e.type == "Member") {
                  ids.push(e.id);
                } else {
                  _this.copyReceiversOptions.push({
                    ...e,
                    value: e.name,
                    operationName: e.name,
                  });
                }
                receiverArr.push(
                  `${e.name}${
                    e.description && e.description.split("部门:")[1]
                      ? "(" + e.description.split("部门:")[1].trim() + ")"
                      : ""
                  }`
                );

                _this.copySaveSubmitIds.push({
                  textName: `${e.name}${
                    e.description && e.description.split("部门:")[1]
                      ? "(" + e.description.split("部门:")[1].trim() + ")"
                      : ""
                  }`,
                  text: e.name,
                  value: `${e.type}|${e.id}`,
                });
                // 选人回显时显示的是id fuguichuan
                  _this.$nextTick(()=>{
                    _this.copyReceiversStr.push(e.id);
                  })
              });
              var arr = receiverArr.toString().split(",");
              _this.msg.copyReceiversStr.push(...arr);
              //   console.log(
              //     111111111111,
              //     _this.msg.copyReceivers,
              //     _this.msg.copyReceiversStr,
              //     _this.copyReceiversOptions
              //   );
              if (ids.length > 0) {
                api.getPeopleMsg({ ids: ids.join(",") }).then((res) => {
                  //   console.log("获取options", res);
                  if (res.code == "1001") {
                    res.msg.forEach((item) => {
                      _this.copyReceiversOptions.push({
                        ...item,
                        operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
                        value: item.name + `(${item.department})`,
                      });
                    });
                  }
                });
              }
            } else {
              _this.msg.copyReceivers = [];
              _this.msg.copyReceiversStr = [];
              _this.copySaveSubmitIds = [];
              // _this.copyReceiversFocusSearch();
            }
            // _this.msg.copyReceiversStr = [];
            // _this.copySaveSubmitIds = [];
            // // _this.msg.copyReceiversStr = result.text
            // if (result.obj.length != 0) {
            //   result.obj.forEach((e) => {
            //     // console.log("这是result", e);
            //     receiverArr.push(
            //       `${e.name}${
            //         e.description && e.description.split("部门:")[1]
            //           ? "(" + e.description.split("部门:")[1].trim() + ")"
            //           : ""
            //       }`
            //     );
            //     // _this.copyReceiversOptions.push({
            //     //   ...e,
            //     //   operationName: e.name,
            //     //   value: `${e.name}${
            //     //     e.description && e.description.split("部门:")[1]
            //     //       ? "(" + e.description.split("部门:")[1].trim() + ")"
            //     //       : ""
            //     //   }`,
            //     // });
            //     // 重置收件人信息内容
            //     _this.copySaveSubmitIds.push({
            //       textName: `${e.name}${
            //         e.description && e.description.split("部门:")[1]
            //           ? "(" + e.description.split("部门:")[1].trim() + ")"
            //           : ""
            //       }`,
            //       text: e.name,
            //       value: `${e.type}|${e.id}`,
            //     });
            //   });
            //   var arr2 = receiverArr.toString().split(",");
            //   _this.msg.copyReceiversStr.push(...arr2);
            //   let arr3 = new Set(_this.msg.copyReceiversStr);
            //   _this.msg.copyReceiversStr = [...arr3];
            //   _this.msg.copyReceivers = result.value.split(",");
            //   console.log(
            //     "收件人",
            //     _this.msg.copyReceivers,
            //     _this.msg.copyReceiversStr,
            //     _this.copySaveSubmitIds
            //   );
            // } else {
            //   _this.copySaveSubmitIds = [];
            //   _this.msg.copyReceiversStr = [];
            //   _this.msg.copyReceivers = [];
            // }
          }

          // 收件人、抄送人 审批验证
          //  <!-- 屏蔽邮件审批  -->
          // _this.emailApproval(false);

          e.target.blur();
        },
      })
    },
    // 获取信息
    getCurPeopleMsg(ids) {
      api.getPeopleMsg({ ids: ids }).then((res) => {
        // console.log("获取options", res);
        if (res.code == "1001") {
          res.msg.forEach((item) => {
            this.receiversOptions.push({
              ...item,
              operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
              value: item.name + `(${item.department})`,
            });
          });
        }
      });
    },
    getCopyCurPeopleMsg(ids) {
      api.getPeopleMsg({ ids: ids }).then((res) => {
        // console.log(res);
        if (res.code == "1001") {
          res.msg.forEach((item) => {
            this.copyReceiversOptions.push({
              ...item,
              operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
              value: item.name + `(${item.department})`,
            });
          });
        }
      });
    },
    getApproverCurPeopleMsg(ids) {
      api.getPeopleMsg({ ids: ids }).then((res) => {
        // console.log(res);
        if (res.code == "1001") {
          res.msg.forEach((item) => {
            this.approverOptions.push({
              ...item,
              operationName: `【${item.miji}】 ${item.name} (${item.loginName}-${item.department})`,
              value: item.name + `(${item.department})`,
            });
          });
        }
      });
    },
    // 获取邮件初始数据 xjh
    getdata() {
      let obj = this.$route.query.obj;
      if (obj == undefined) {
        // 获取签名
        api.getmailSignature().then((res) => {
          this.msg.content = res.msg;
          this.isCrossDisable = false;
        });
        // 编辑
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 start*/
        //obj为空，说明是新建邮件,所以生成一个虚拟id，转发也要生成一个虚拟id
        api.getUUID().then((res) => {
          this.myConfig.virtualIdPlugin = res.msg;
          this.isshow = true; //暂缓显示编辑板
        })
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 end*/


      } else if (obj.type == "editMail") {
        api.internalCompile(obj).then((res) => {
          //   console.log("编辑邮件获取的数据", res.msg.bean);
          // console.log(res.msg.attachments[0].icon)
          //   lyy start
          this.msg = res.msg.bean;

          console.log("邮件编辑", this.msg);
          //客开,huanglida,2025-12-4,邮件编辑态增加判断逻辑如果replyParentSummaryid有值的时候则按照回复的逻辑处理，避免转发和回复的草稿密级可以自由选择的的问题,start
          try {
            if (this.msg.replyParentSummaryid&&this.msg.replyParentSummaryid!=""){
              let params = {
                type: "",
                secretId: "",
              };
              params.type = "replyMail";
              params.secretId = this.$route.query.obj.secretTypeId;
              params.replayParentSummaryId = this.msg.replyParentSummaryid;
                console.log(params);
                api.getSecretListByReplyParentSummaryId(params).then((res) => {
                  console.log(res.msg);
                  this.secretTypeIdList = res.msg;
                  console.log("this.secretTypeIdList", this.secretTypeIdList)

                  if (obj == undefined) {
                    // this.msg.secretTypeId = this.secretTypeIdList[0].id
                    // console.log(this.msg.secretTypeId);
                  } else {
                    this.msg.secretTypeId = this.$route.query.obj.secretTypeId;
                  }
                });
            }
          }catch (e) {
            console.log(e);
          }
          //客开,huanglida,2025-12-4,邮件编辑态增加判断逻辑如果replyParentSummaryid有值的时候则按照回复的逻辑处理，避免转发和回复的草稿密级可以自由选择的的问题,end
          // lyy end
          //   lyy 2022 5 30
          let edit = JSON.parse(JSON.stringify(res.msg.bean));
          let receiverIds = [];
          if (edit.receivers) {
            this.receiversOptions = [];
            let curReceivers = edit.receivers.split(",");
            curReceivers.forEach((item, index) => {
              if (item.split("|")[0] == "Member") {
                receiverIds.push(item.split("|")[1]);
              } else {
                // console.log("index", index, edit.receiversStr);
                this.receiversOptions.push({
                  id: item.split("|")[1],
                  value: edit.receiversStr.split(",")[index],
                  operationName: edit.receiversStr.split(",")[index],
                });
              }
            });
            if (receiverIds.length > 0) {
              this.getCurPeopleMsg(receiverIds.join(","));
            }
          } else {
            // this.focusSearch();
          }
          this.receiversStr = edit.receivers
            ? edit.receivers.split(",").map((item) => {
                return item.split("|")[1];
              })
            : [];
          // lyy end
          // 回显收件人数据及id
          this.msg.receiversStr = res.msg.bean.receiversStr
            ? res.msg.bean.receiversStr.split(",")
            : [];
          this.msg.receivers = res.msg.bean.receivers
            ? res.msg.bean.receivers.split(",")
            : [];

          //   console.log(
          //     "this.msg.receivers",
          //     this.msg.receivers,
          //     this.msg.receiversStr,
          //     this.receiversStr
          //   );
          for (
            let receiversIndex = 0, length = this.msg.receiversStr.length;
            receiversIndex < length;
            receiversIndex++
          ) {
            // 收件人信息集合
            this.saveSubmitIds.push({
              text: this.msg.receiversStr[receiversIndex],
              value: this.msg.receivers[receiversIndex],
            });
          }

          //   lyy 2022 5 30
          //   console.log("edit", edit);
          let copyReceiverIds = [];
          this.copyReceiversOptions = [];
          if (edit.copyReceivers) {
            let curCopyReceivers = [...edit.copyReceivers.split(",")];
            // console.log(curCopyReceivers);
            curCopyReceivers.forEach((item, index) => {
              if (item.split("|")[0] == "Member") {
                copyReceiverIds.push(item.split("|")[1]);
              } else {
                // console.log(111, index, edit.copyReceiversStr);
                this.copyReceiversOptions.push({
                  id: item.split("|")[1],
                  value: edit.copyReceiversStr.split(",")[index],
                  operationName: edit.copyReceiversStr.split(",")[index],
                });
                // console.log(111, this.copyReceiversOptions);
              }
            });
            if (copyReceiverIds.length > 0) {
              this.getCopyCurPeopleMsg(copyReceiverIds.join(","));
            }
          } else {
            // this.copyReceiversFocusSearch();
          }
          this.copyReceiversStr = edit.copyReceivers
            ? edit.copyReceivers.split(",").map((item) => {
                return item.split("|")[1];
              })
            : [];
          // lyy end
          // 回显抄送人数据及id
          this.msg.copyReceiversStr = res.msg.bean.copyReceiversStr
            ? res.msg.bean.copyReceiversStr.split(",")
            : [];
          this.msg.copyReceivers = res.msg.bean.copyReceivers
            ? res.msg.bean.copyReceivers.split(",")
            : [];

          for (
            let copyReceiversIndex = 0,
              length = this.msg.copyReceiversStr.length;
            copyReceiversIndex < length;
            copyReceiversIndex++
          ) {
            // 收件人信息集合
            this.copySaveSubmitIds.push({
              text: this.msg.copyReceiversStr[copyReceiversIndex],
              value: this.msg.copyReceivers[copyReceiversIndex],
            });
          }

          //   lyy 2022 5 30
          //   console.log("edit", edit);
          let appoverReceiverIds = [];
          this.approverOptions = [];
          if (edit.approver) {
            let curAppoverReceivers = [...edit.approver.split(",")];
            // console.log(curAppoverReceivers);
            curAppoverReceivers.forEach((item, index) => {
              appoverReceiverIds.push(item.split("|")[1]);
            });
            if (appoverReceiverIds.length > 0) {
              this.getApproverCurPeopleMsg(appoverReceiverIds.join(","));
            }
          } else {
            // this.approverFocusSearch();
          }
          this.approverStr = edit.approver
            ? edit.approver.split(",").map((item) => {
                return item.split("|")[1];
              })
            : [];
          // lyy end
          // 回显审批人数据及id
          this.msg.approverStr = res.msg.bean.approverStr
            ? res.msg.bean.approverStr.split(",")
            : [];
          this.msg.approver = res.msg.bean.approver
            ? res.msg.bean.approver.split(",")
            : [];

          for (
            let approverIndex = 0, length = this.msg.approverStr.length;
            approverIndex < length;
            approverIndex++
          ) {
            // 收件人信息集合
            this.approveSaveSubmitIds.push({
              text: this.msg.approverStr[approverIndex],
              value: this.msg.approver[approverIndex],
            });
          }

          this.msg.kuaWang = res.msg.bean.kuaWang;
          this.msg.kuaWangdis = res.msg.isCrossDisable;
          this.msg.secretTypeId = this.$route.query.obj.secretTypeId;
          this.msg.affairId = res.msg.affairId;
          this.isCrossDisable = res.msg.isCrossDisable;
          this.msg.editMailSend = this.$route.query.obj.editMailSend;
          if (res.msg.attachments.length != 0) {
            // this.fileList = res.msg.attachments;
            res.msg.attachments.forEach((i) => {
              i.size = (i.size / 1024 / 1024).toFixed(2) + "M";
              this.fileList.push(i);
            });
            this.$nextTick();
          }
        });
        // 回复
      } else if (obj.type == "replyMail") {
        let receiverslist;
        let arr = [];
        if (obj.mark == "allReply") {
          api.allreplyMailinfo(obj).then((res) => {
            console.log("res``", res);
            this.msg = res.msg.bean;
            console.log("allreplyMailinfo"  ,this.msg);
            receiverslist = this.msg.receiversDetail.split(",");

            receiverslist.some((d, i) => {
              receiverslist[i] = d.split("-");
            });


            receiverslist.forEach((d) => {
              //   console.log("收件人", d);
              if (
                d[0] === "Department" ||
                d[0] === "Team" ||
                d[0] === "Account"
              ) {
                arr.push(`${d[1]}`);
              } else {
                arr.push(`${d[1]}(${d[3]})`);
              }
            });
            //   lyy 2022 5 30
            // console.log(res.msg.bean.receiversStr);
            let edit = JSON.parse(JSON.stringify(res.msg.bean));
            let receiverIds = [];
            this.receiversOptions = [];
            if (edit.receivers) {
              //   console.log("edit.receivers", edit.receivers);
              let curReceivers = edit.receivers.split(",");
              curReceivers.forEach((item, index) => {
                if (item.split("|")[0] == "Member") {
                  receiverIds.push(item.split("|")[1]);
                } else {
                  //   console.log("index", index, edit.receiversStr);
                  this.receiversOptions.push({
                    id: item.split("|")[1],
                    value: edit.receiversStr.split("、")[index],
                    operationName: edit.receiversStr.split("、")[index],
                  });
                }
              });
              if (receiverIds.length > 0) {
                this.getCurPeopleMsg(receiverIds.join(","));
              }
            } else {
              // this.focusSearch();
            }
            this.receiversStr = edit.receivers
              ? edit.receivers.split(",").map((item) => {
                  return item.split("|")[1];
                })
              : [];
            // lyy end
            this.msg.receiversStr = arr;
            // console.log(arr);

            this.msg.kuaWang = res.msg.bean.kuaWang;
            this.msg.secretTypeId = this.$route.query.obj.secretTypeId;
            this.msg.affairId = res.msg.affairId;
            this.isCrossDisable = res.msg.isCrossDisable;
            // console.log(`res.msg.isCrossDisable`, res.msg.isCrossDisable);
            this.msg.kuaWangdis = res.msg.isCrossDisable;

            if (res.msg.attachments && res.msg.attachments.length != 0) {
              //   this.fileList = res.msg.attachments;
              res.msg.attachments.forEach((i) => {
                i.size = (i.size / 1024 / 1024).toFixed(2) + "M";
                this.fileList.push(i);
              });
            }
            // 回显收件人数据及id
            // this.msg.receiversStr = res.msg.bean.receiversStr
            //   ? res.msg.bean.receiversStr.split(",")
            //   : [];

            this.msg.receivers = res.msg.bean.receivers
              ? res.msg.bean.receivers.split(",")
              : [];
            // console.log("this.msg.receivers", this.msg.receivers);
            for (
              let receiversIndex = 0, length = arr.length;
              receiversIndex < length;
              receiversIndex++
            ) {
              // 收件人信息集合
              this.saveSubmitIds.push({
                text: arr[receiversIndex],
                value: this.msg.receivers[receiversIndex],
              });
            }
            //   lyy 2022 5 30
            //   console.log("edit", edit);
            let copyReceiverIds = [];
            this.copyReceiversOptions = [];
            if (edit.copyReceivers) {
              let curCopyReceivers = [...edit.copyReceivers.split(",")];
              // console.log(curCopyReceivers);
              curCopyReceivers.forEach((item, index) => {
                if (item.split("|")[0] == "Member") {
                  copyReceiverIds.push(item.split("|")[1]);
                } else {
                  // console.log(111, index, edit.copyReceiversStr);
                  this.copyReceiversOptions.push({
                    id: item.split("|")[1],
                    value: edit.copyReceiversStr.split(",")[index],
                    operationName: edit.copyReceiversStr.split(",")[index],
                  });
                  // console.log(111, this.copyReceiversOptions);
                }
              });
              if (copyReceiverIds.length > 0) {
                this.getCopyCurPeopleMsg(copyReceiverIds.join(","));
              }
            } else {
              // this.copyReceiversFocusSearch();
            }
            this.copyReceiversStr = edit.copyReceivers
              ? edit.copyReceivers.split(",").map((item) => {
                  return item.split("|")[1];
                })
              : [];
            // lyy end
            // 回显抄送人数据及id
            this.msg.copyReceiversStr = res.msg.bean.copyReceiversStr
              ? res.msg.bean.copyReceiversStr.split(",")
              : [];
            this.msg.copyReceivers = res.msg.bean.copyReceivers
              ? res.msg.bean.copyReceivers.split(",")
              : [];

            for (
              let copyReceiversIndex = 0,
                length = this.msg.copyReceiversStr.length;
              copyReceiversIndex < length;
              copyReceiversIndex++
            ) {
              // 收件人信息集合
              this.copySaveSubmitIds.push({
                text: this.msg.copyReceiversStr[copyReceiversIndex],
                value: this.msg.copyReceivers[copyReceiversIndex],
              });
            }

            // 回显审批人数据及id
            this.msg.approverStr = res.msg.bean.approverStr
              ? res.msg.bean.approverStr.split(",")
              : [];
            this.msg.approver = res.msg.bean.approver
              ? res.msg.bean.approver.split(",")
              : [];

            for (
              let approverIndex = 0, length = this.msg.approverStr.length;
              approverIndex < length;
              approverIndex++
            ) {
              // 收件人信息集合
              this.approveSaveSubmitIds.push({
                text: this.msg.approverStr[approverIndex],
                value: this.msg.approver[approverIndex],
              });
            }

            // ===== AI助手功能：保存原邮件信息+autoContent回填 START =====
            this.showAiButton = true;
            this.originalEmailSubject = res.msg.bean.subject || '';
            this.originalEmailContent = res.msg.bean.content || '';
            this.originalEmailTemplate = this.splitByBlueLine(this.originalEmailContent).template;
            this.originalEmailSender = this._extractOriginalSender(this.originalEmailTemplate);
            // 2026-06-01 AI助手 start: 优先从sessionStorage读取AI回复内容，兜底URL参数
            // 之前通过URL query传autoContent，长文本可能超URL长度限制；改为sessionStorage传递
            // 同时将箭头函数(()=>)改为普通function以兼容Chrome49
            var autoContent = obj.autoContent
            if (!autoContent) {
              try { autoContent = window.sessionStorage.getItem('aiReplyContent'); if (autoContent) { window.sessionStorage.removeItem('aiReplyContent'); } } catch(e) {}
            }
            if (autoContent) {
              var ac = autoContent
              this.$nextTick(function() {
                const cleanedHtml = this.cleanAiContentToHtml(ac);
                const template = this.originalEmailTemplate || this.splitByBlueLine(this.msg.content || '').template || '';
                this.msg.content = cleanedHtml + template;
              });
            }
            // 2026-06-01 AI助手 end
            // ===== AI助手功能：保存原邮件信息+autoContent回填 END =====
          });
        } else {
          api.replyMailinfo(obj).then((res) => {
            //   lyy start
            // this.msg = res.msg.bean;
            // console.log("res.msg.bean``", res.msg.bean);
            this.msg = {
              ...res.msg.bean,
              copyReceiversStr: res.msg.bean.copyReceiversStr.split(","),
              receiversStr: res.msg.bean.receiversStr.split(","),
              approverStr: res.msg.bean.approverStr.split(","),
              approver: res.msg.bean.approver.split(","),
              receivers: res.msg.bean.receivers.split(","),
              copyReceivers: res.msg.bean.copyReceivers.split(","),
            };
            console.log("replyMailinfo"  ,this.msg);
            // lyy end
            receiverslist = this.msg.receiversDetail.split(",");

            receiverslist.some((d, i) => {
              receiverslist[i] = d.split("-");
            });
            // console.log(res);

            receiverslist.forEach((d) => {
              if (
                d[0] === "Department" ||
                d[0] === "Team" ||
                d[0] === "Account"
              ) {
                arr.push(`${d[1]}`);
              } else {
                arr.push(`${d[1]}(${d[3]})`);
              }
            });
            //   lyy 2022 5 30
            let edit = JSON.parse(JSON.stringify(res.msg.bean));
            let receiverIds = [];
            if (edit.receivers) {
              this.receiversOptions = [];
              let curReceivers = edit.receivers.split(",");
              curReceivers.forEach((item, index) => {
                if (item.split("|")[0] == "Member") {
                  receiverIds.push(item.split("|")[1]);
                } else {
                  // console.log("index", index, edit.receiversStr);
                  this.receiversOptions.push({
                    id: item.split("|")[1],
                    value: edit.receiversStr.split(",")[index],
                    operationName: edit.receiversStr.split(",")[index],
                  });
                }
              });
              this.getCurPeopleMsg(receiverIds.join(","));
            } else {
              // this.focusSearch();
            }
            this.receiversStr = edit.receivers
              ? edit.receivers.split(",").map((item) => {
                  return item.split("|")[1];
                })
              : [];
            // lyy end
            this.msg.receiversStr = arr;
            this.msg.kuaWang = res.msg.bean.kuaWang;
            this.msg.secretTypeId = this.$route.query.obj.secretTypeId;
            this.msg.affairId = res.msg.affairId;
            this.isCrossDisable = res.msg.isCrossDisable;
            // console.log(`res.msg.isCrossDisable`, res.msg.isCrossDisable);
            this.msg.kuaWangdis = res.msg.isCrossDisable;

            if (res.msg.attachments && res.msg.attachments.length != 0) {
              //   this.fileList = res.msg.attachments;
              res.msg.attachments.forEach((i) => {
                i.size = (i.size / 1024 / 1024).toFixed(2) + "M";
                this.fileList.push(i);
              });
            }

            // 回显收件人数据及id
            // this.msg.receiversStr = res.msg.bean.receiversStr
            //   ? res.msg.bean.receiversStr.split(",")
            //   : [];
            this.msg.receivers = res.msg.bean.receivers
              ? res.msg.bean.receivers.split(",")
              : [];
            // console.log("this.msg.receivers", this.msg.receivers);
            for (
              let receiversIndex = 0, length = arr.length;
              receiversIndex < length;
              receiversIndex++
            ) {
              // 收件人信息集合
              this.saveSubmitIds.push({
                text: arr[receiversIndex],
                value: this.msg.receivers[receiversIndex],
              });
            }
            // 回显抄送人数据及id
            this.msg.copyReceiversStr = res.msg.bean.copyReceiversStr
              ? res.msg.bean.copyReceiversStr.split(",")
              : [];
            this.msg.copyReceivers = res.msg.bean.copyReceivers
              ? res.msg.bean.copyReceivers.split(",")
              : [];
            for (
              let copyReceiversIndex = 0,
                length = this.msg.copyReceiversStr.length;
              copyReceiversIndex < length;
              copyReceiversIndex++
            ) {
              // 收件人信息集合
              this.copySaveSubmitIds.push({
                text: this.msg.copyReceiversStr[copyReceiversIndex],
                value: this.msg.copyReceivers[copyReceiversIndex],
              });
            }

            // 回显审批人数据及id
            this.msg.approverStr = res.msg.bean.approverStr
              ? res.msg.bean.approverStr.split(",")
              : [];
            this.msg.approver = res.msg.bean.approver
              ? res.msg.bean.approver.split(",")
              : [];

            for (
              let approverIndex = 0, length = this.msg.approverStr.length;
              approverIndex < length;
              approverIndex++
            ) {
              // 收件人信息集合
              this.approveSaveSubmitIds.push({
                text: this.msg.approverStr[approverIndex],
                value: this.msg.approver[approverIndex],
              });
            }

            // ===== AI助手功能：保存原邮件信息+autoContent回填 START =====
            this.showAiButton = true;
            this.originalEmailSubject = res.msg.bean.subject || '';
            this.originalEmailContent = res.msg.bean.content || '';
            this.originalEmailTemplate = this.splitByBlueLine(this.originalEmailContent).template;
            this.originalEmailSender = this._extractOriginalSender(this.originalEmailTemplate);
            // 2026-06-01 AI助手 start: 优先从sessionStorage读取AI回复内容，兜底URL参数
            var autoContent2 = obj.autoContent
            if (!autoContent2) {
              try { autoContent2 = window.sessionStorage.getItem('aiReplyContent'); if (autoContent2) { window.sessionStorage.removeItem('aiReplyContent'); } } catch(e) {}
            }
            if (autoContent2) {
              var ac2 = autoContent2
              this.$nextTick(function() {
                const cleanedHtml = this.cleanAiContentToHtml(ac2);
                const template = this.originalEmailTemplate || this.splitByBlueLine(this.msg.content || '').template || '';
                this.msg.content = cleanedHtml + template;
              });
            }
            // 2026-06-01 AI助手 end
            // ===== AI助手功能：保存原邮件信息+autoContent回填 END =====
          });
        }
        // 转发
      } else if (obj.type == "forwordMail") {
        // console.log(obj);
        this.selectmj = true;
        api.forwordMail(obj).then((res) => {
          // console.log("转发", res);
          //  lyy start
          //   this.msg = res.msg.bean;

          this.msg = {
            ...res.msg.bean,
            copyReceiversStr: [],
            receiversStr: [],
            approverStr: [],
            approver: [],
            receivers: [],
            copyReceivers: [],
          };
          console.log("forwordMail"  ,this.msg);
          //   lyy end
          this.msg.kuaWang = res.msg.bean.kuaWang;
          this.msg.secretTypeId = this.$route.query.obj.secretTypeId;
          this.msg.affairId = res.msg.affairId;
          this.isCrossDisable = res.msg.isCrossDisable;
          // 转发邮件时，密级类型非公开和内部需要 可跨网，其他类型是禁用掉 modify yangyanhua20210908
          this.msg.kuaWangdis = res.msg.isCrossDisable;

          if (res.msg.attachments && res.msg.attachments.length != 0) {
            //lyy start
            res.msg.attachments.forEach((i) => {
              i.size = (i.size / 1024 / 1024).toFixed(2) + "M";
              this.fileList.push(i);
            });
            // this.fileList = res.msg.attachments;
            // lyy end
          }

          // ===== AI助手功能：保存原邮件信息 START =====
          this.showAiButton = true;
          this.originalEmailSubject = res.msg.bean.subject || '';
          this.originalEmailContent = res.msg.bean.content || '';
          this.originalEmailTemplate = this.splitByBlueLine(this.originalEmailContent).template;
          // 2026-06-01 AI助手 start: 新增提取发件人名字，传给AI面板用于智能回复称呼
          this.originalEmailSender = this._extractOriginalSender(this.originalEmailTemplate);
          // 2026-06-01 AI助手 end
          // ===== AI助手功能：保存原邮件信息 END =====
        });

        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 start*/
        //obj为空，说明是新建邮件,所以生成一个虚拟id，转发也要生成一个虚拟id
        api.getUUID().then((res) => {
          this.myConfig.virtualIdPlugin = res.msg;
          this.isshow = true; //暂缓显示编辑板
        })
        /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 end*/
      }
      let params = {
        type: "",
        secretId: "",
      };
      if (obj == undefined) {
        params.type = "";
        params.secretId = "";
      } else {
        params.type = this.$route.query.obj.type;
        params.secretId = this.$route.query.obj.secretTypeId;
      }
      //   console.log(params);
      if (this.$store.state.isredNet) {
        api.getSecretList(params).then((res) => {
          this.secretTypeIdList = res.msg;
          if (obj == undefined) {
            // this.msg.secretTypeId = this.secretTypeIdList[0].id
            // console.log(this.msg.secretTypeId);
          } else {
            this.msg.secretTypeId = this.$route.query.obj.secretTypeId;
          }
        });
        //先注释MailFileSecretVerify 校验文件密级
        // api.MailFileSecretVerify({secret:this.msg.secretTypeId}).then(res =>{
        //   console.log(res)
        //   if (res.code != 10001){
        //     this.$nextTick().then(() => {
        //       this.msg.secretTypeId = this.secretTypeIdold

        //     });

        //     this.$message.error(res.msg)

        //   }
        //   else {

        //     // this.fileList = []
        //     let obj ={
        //       secretTypeId:this.msg.secretTypeId,
        //       fileNames:[]
        //     }
        //     this.fileList.forEach( d =>{
        //       obj.fileNames.push(d.filename)
        //     })
        //     obj.fileNames=obj.fileNames.toString()

        //     api.isSecret(obj).then(res =>{

        //       this.$nextTick().then(() => {
        //         this.msg.kuaWangdis=res.msg.isSecret
        //         console.log('---------------------------------------------------')
        //         console.log(this.msg.kuaWangdis)
        //         this.$forceUpdate()

        //       });

        //       if (res.code == 10001){
        //         if(res.msg.isSecret == true){
        //           // this.$message.warning("涉密邮件不可跨网")
        //           this.msg.kuaWang = false
        //           this.msg.kuaWangdis = true
        //           // this.secretTypeIdold = this.msg.secretTypeId
        //         }
        //       }else {

        //         this.$message.warn(res.msg)
        //       }

        //     })
        //     // if (this.msg.secretTypeId == -637209554414910365){
        //     //     this.$message.warning("涉密邮件不可跨网")
        //     //     this.msg.kuaWang = false
        //     // }
        //   }

        // })
      }
      //   console.log(this.isCrossDisable);

    },
    //点击邮件转发需要修改面板布局，隐藏邮件列表
    lxryc() {
      this.sendtie = false;
      // this.$store.commit('showlxr', [0, 24],);
      //点击邮件转发需要修改面板布局，隐藏邮件列表
      if (
        document.querySelector(".mid") &&
        document.querySelector(".mid").style.display == ""
      ) {
        document.querySelector(".mid").style.display = "none";
      }
      if (
        document.querySelector(".resize") &&
        document.querySelector(".resize").style.display == ""
      ) {
        document.querySelector(".resize").style.display = "none";
      }
      if (
        document.querySelector(".right") &&
        document.querySelector(".right").style.width == ""
      ) {
        document.querySelector(".right").style.width = "auto";
      }
    },

    //  富文本

    onEditorBlur() {
      //失去焦点事件
    },
    onEditorFocus() {
      //获得焦点事件
    },
    // 开始
    onEditorReady(editor) {},

    onEditorChange() {
      //内容改变事件
      this.$emit("input", this.content);
    },

    // 富文本图片上传前
    beforeUpload() {
      // 显示loading动画
      this.quillUpdateImg = true;
    },

    uploadSuccess(res, file) {
      // res为图片服务器返回的数据
      // 获取富文本组件实例
      let quill = this.$refs.myQuillEditor.quill;
      // 如果上传成功
      if (res.atts.length != 0) {
        // 获取光标所在位置
        let length = quill.getSelection().index;
        // 插入图片  res.url为服务器返回的图片地址  res.result.url为图片地址
        quill.insertEmbed(
          length,
          "image",
          `/api/attachment/download/${encodeURI(
            res.atts[0].fileUrl
          )}&createDate=${res.atts[0].createdate.slice(
            0,
            10
          )}&type=image&showType=big`
        );
        // 调整光标到最后
        quill.setSelection(length + 1);
      } else {
        this.$message.error("图片插入失败");
      }
      // loading动画消失
      this.quillUpdateImg = false;
    },
    // 富文本图片上传失败
    uploadError() {
      // loading动画消失
      this.quillUpdateImg = false;
      this.$message.error("图片插入失败");
    },
  },
};
</script>

<style scoped>
/* *解决多选自动换行
.select-user-form /deep/ .el-select__tags {
   flex-wrap: unset;
   overflow: auto;
   margin-left: 5px;
} */
/* 取消保存改为 X  start*/
.demo-form-inline .closeBt {
  position: absolute;
  right: 0;
  border: none;
  font-size: 13px;
  padding: 2px 5px !important;
}
.demo-form-inline .closeBt:hover {
  background-color: #f78989;
  border-radius: 3px !important;
  color: #fff;
  scale: 1.2;
  transition: all 0.2s;
}
/* 取消保存改为 X  end*/
.the-theme {
  margin-bottom: 10px;
}

.copy-people .the-recipient {
  display: block !important;
}

.secretArrow /deep/ .el-input__suffix {
  display: flex;
  align-items: center;
}

.approver,
.recipient,
.copyReceiver,
.select-user,
.approver {
  display: block !important;
}

.theme /deep/ .el-form-item__label {
  margin-top: 5px !important;
}

.select-user-form {
  position: relative;
  text-align: center;
  min-height: 40px !important;
  line-height: 40px !important;
}

.select-user-form .position-icon {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translate(-50%, -50%);
  cursor: pointer;
  color: #c0c4cc;
}

.count /deep/ .el-select .el-select-dropdown {
  z-index: 1000 !important;
}

.count /deep/ .el-select .el-input .el-input__inner {
  box-sizing: border-box;
}

.select-user /deep/ .el-select__input {
  border: none !important;
}

.el-select /deep/ .el-input:hover {
  background-color: none;
  border: none;
}

/**tangxiangping 📅：2022-06-02 修改elementUI自带display： contents不兼容低版本浏览器的问题  start*/
.el-select /deep/ .el-select__tags > span {
  display: flex;
  flex-wrap: wrap;
}
/**tangxiangping 📅：2022-06-02 修改elementUI自带display： contents不兼容低版本浏览器的问题  end*/

.el-select-dropdown.is-multiple .el-select-dropdown__item.selected {
  font-weight: 400;
}
.el-select-dropdown__list .el-select-dropdown__item{
  text-align: left !important;
}
.item:hover {
  background-color: #ffffff !important;
}

.select-user /deep/ .el-input .el-select__caret::before {
  content: "";
}

.count {
  /*padding: 15px;*/
  background: #f7f8f9;
  /* lyy start */
  height: 100vh;
  overflow: hidden;
  display: flex;
  flex-direction: row;
  position: relative;
}
.editor-main-wrap {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.count /deep/ .el-form-item {
  margin-bottom: 10px;
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



/* ===== AI助手功能：CSS样式 START ===== */
/* --- AI思考过程展示样式 --- */
.ai-thinking-panel {
  margin: 10px 0;
}

.ai-thinking-panel .el-collapse {
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.ai-thinking-panel .el-collapse-item__header {
  font-size: 13px;
  color: #606266;
  padding: 0 15px;
  height: 36px;
  line-height: 36px;
}

.ai-thinking-panel .el-collapse-item__content {
  padding: 10px 15px;
}

.ai-thinking-content {
  background: #f5f7fa;
  padding: 10px;
  border-radius: 4px;
  color: #606266;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
  min-height: 40px;
}

.typing-text {
  font-family: 'Courier New', monospace;
}

.typing-cursor {
  display: inline-block;
  color: #409eff;
  font-weight: bold;
  animation: blink-cursor 0.8s infinite;
}

@keyframes blink-cursor {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}
/* ===== AI思考过程展示样式 END ===== */

/* --- AI助手面板按钮样式 --- */
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
/* ===== AI助手功能：CSS样式 END ===== */
</style>
