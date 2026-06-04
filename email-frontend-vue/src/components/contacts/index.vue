<template>
  <div class="contacts" style="height: 100%">
    <el-dialog
      title="搜索"
      :close-on-click-modal="false"
      :visible.sync="searchdialog"
    >
      <!--            <el-select class="searsle" v-model="searchType" clearable placeholder="请选择" size="mini" style="width: 20%"-->
      <!--                       @change="qksearsh">-->
      <!--                <el-option-->
      <!--                        v-for="item in options"-->
      <!--                        :key="item.value"-->
      <!--                        :label="item.label"-->
      <!--                        :value="item.value">-->
      <!--                </el-option>-->
      <!--            </el-select>-->

      <el-form label-position="left" label-width="80px" :model="formLabelAlign">
        <el-form-item label="标题">
          <el-input
            placeholder="请输入标题"
            v-model="formLabelAlign['1']"
            clearable
            size="mini"
          >
          </el-input>
        </el-form-item>
        <el-form-item label="日期">
          <!--              <el-input v-model="formLabelAlign['2']"></el-input>-->
          <el-date-picker
            value-format="yyyy-MM-dd HH-mm-ss"
            v-model="formLabelAlign['2']"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            clearable
            size="mini"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item label="收件人">
          <el-input
            placeholder="请输入收件人"
            v-model="recipient"
            clearable
            size="mini"
            @focus="setPeople('start')"
          >
          </el-input>
        </el-form-item>
        <el-form-item label="抄送人">
          <el-input
            placeholder="请输入抄送人"
            v-model="copy"
            clearable
            size="mini"
            @focus="setPeople('copy')"
          >
          </el-input>
        </el-form-item>
        <el-form-item label="发起人">
          <el-input
            placeholder="请输入发起人"
            v-model="organizer"
            clearable
            size="mini"
            @focus="setPeople('send')"
          >
          </el-input>
        </el-form-item>
        <el-form-item label="正文查询">
          <el-input
            placeholder="请输入正文"
            v-model="formLabelAlign['6']"
            clearable
            size="mini"
          >
          </el-input>
        </el-form-item>
        <el-form-item label="附件查询">
          <el-input
            placeholder="请输入附件名"
            v-model="formLabelAlign['7']"
            clearable
            size="mini"
          >
          </el-input>
        </el-form-item>
      </el-form>
      <!--          <span @click="log">uujuju</span>-->

      <!--            <el-input-->
      <!--                    v-show="!timeinout"-->
      <!--                    placeholder="请输入内容"-->
      <!--                    v-model="searchWord"-->
      <!--                    clearable size="mini" style="width: 75%">-->
      <!--            </el-input>-->

      <div slot="footer" class="dialog-footer">
        <el-button @click="refsearc">清空</el-button>
        <el-button type="primary" @click="search('advanced')">确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog
      :title="msg"
      :close-on-click-modal="false"
      :show-close="false"
      :destroy-on-close="true"
      :visible.sync="dialogFormVisible"
    >
      <div v-show="msg != '修改密码'">
        <el-input
          placeholder="请输入密码"
          v-model="password"
          show-password
          size="small"
        ></el-input>
        <div style="padding: 5px"></div>
        <el-input
          placeholder="请再次输入密码"
          v-show="!pwd"
          v-model="password1"
          show-password
          size="small"
        ></el-input>
      </div>
      <!--            修改密码-->
      <div v-show="msg == '修改密码'">
        <el-input
          placeholder="请输入旧密码"
          v-model="oldPassword"
          show-password
          size="small"
        ></el-input>
        <div style="padding: 5px"></div>
        <el-input
          placeholder="请输入新密码"
          v-model="password"
          show-password
          size="small"
        ></el-input>
        <div style="padding: 5px"></div>
        <el-input
          placeholder="请再次输入新密码"
          v-model="password1"
          show-password
          size="small"
        ></el-input>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editpwd" type="text">修改密码</el-button>
        <el-button @click="pwdback">取 消</el-button>
        <el-button type="primary" @click="addpassword" v-if="!pwd"
          >确 定</el-button
        >
        <el-button type="primary" @click="login" v-else>确 定</el-button>
      </div>
    </el-dialog>

    <el-dialog title="查看回执" :visible.sync="showhz">
      <div class="tabbbox" style="max-height: 450px; overflow: auto">
        <el-table :data="gridData" stripe header-cell-class-name="tabhead">
          <el-table-column
            align="center"
            property="recUserDept"
            label="查阅部门"
            width="150"
          ></el-table-column>
          <el-table-column
            align="center"
            property="recUserName"
            label="查阅人员"
            width="200"
          ></el-table-column>
          <el-table-column align="center" label="查阅状态">
            <template slot-scope="scope">
              <span>{{ scope.row.readFlag ? "已查看" : "未查看" }}</span>
            </template>
          </el-table-column>
          <el-table-column align="center" label="查阅时间">
            <template slot-scope="scope">
              <span>{{
                (scope.row.browseTime = scope.row.browseTime || "无")
              }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>

    <div class="tip">
      <!-- <i @click="sqss" class="el-icon-search hand" style="margin-right: 0.6em;color: #898989"></i> -->
      <el-tooltip class="item" effect="dark" content="全选" placement="top">
        <el-checkbox v-model="allchecked" @change="allslect"></el-checkbox>
      </el-tooltip>

      <!-- <el-select class="ddts" v-model="groupType" v-show="this.selitem != 2 && this.selitem != 3 && this.selitem != 5 && this.selitem != 4 && this.selitem != 6" placeholder="请选择" size="mini"
                       style="width: 20%;margin-left: 1em" @change="typeqh">
                <el-option
                        v-for="item in MailGroupType"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value">
                </el-option>
            </el-select> -->
      <!--精确搜索-->
      <span class="ddts" style="display: inline">
        <!-- 注释原有的下拉选择框 fuguichaun -->
        <!-- <el-select
          v-show="showSearchBox"
          v-model="searchSel"
          placeholder="请选择"
          size="mini"
          style="width: 25%; margin-left: 1em"
          @change="searchCondition"
        >
          <el-option
            v-for="item in searchConditions"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          >
          </el-option>
        </el-select> -->
        <!--普通搜索-->
        <!-- <span style="display: inline" class="btnClass"  v-show="showSearchBox">
          <el-input
            clearable
            v-show="
              this.searchSel != '3' &&
              this.searchSel != '4' &&
              this.searchSel != '5'
            "
            v-model="searchValue"
            placeholder="快速搜索"
            style="
              width: 65%;
              border-left: 0px solid black;
              border-right: 0px solid black;
            "
            size="mini"
            @keyup.enter.native="search('quick')"
          >
            <el-button
              slot="append"
              icon="el-icon-search hand"
              @click="search('quick')"
            ></el-button>
          </el-input>
          <el-input
            v-show="this.searchSel == '3'"
            placeholder="请输入收件人"
            v-model="searchValue"
            clearable
            size="mini"
            @focus="setPeople('start', 'quick')"
            @keyup.enter.native="search('quick')"
            style="
              width: 58%;
              border-left: 0px solid black;
              border-right: 0px solid black;
            "
          >
            <el-button
              slot="append"
              icon="el-icon-search hand"
              @click="search('quick')"
            ></el-button>
          </el-input>
          <el-input
            v-show="this.searchSel == '4'"
            placeholder="请输入抄送人"
            v-model="searchValue"
            clearable
            size="mini"
            @focus="setPeople('copy', 'quick')"
            @keyup.enter.native="search('quick')"
            style="
              width: 58%;
              border-left: 0px solid black;
              border-right: 0px solid black;
            "
          >
            <el-button
              slot="append"
              icon="el-icon-search hand"
              @click="search('quick')"
            ></el-button>
          </el-input>
          <el-input
            v-show="this.searchSel == '5'"
            placeholder="请输入发起人"
            v-model="searchValue"
            clearable
            size="mini"
            @focus="setPeople('send', 'quick')"
            @keyup.enter.native="search('quick')"
            style="
              width: 58%;
              border-left: 0px solid black;
              border-right: 0px solid black;
            "
          >
            <el-button
              slot="append"
              icon="el-icon-search hand"
              @click="search('quick')"
            ></el-button>
          </el-input>
        </span> -->
        <!-- 新建输入框 fuguichaun -->
      <div class="searchBox">
                <el-select
                class="hhh"
                  v-model="value123"
                  filterable
                  remote
                  reserve-keyword
                  placeholder="请输入关键词"
                  @change="searchCondition"
                  :remote-method="remoteMethod"
                  :loading="loading">
                  <el-option
                    v-for="item in searchConditions"
                    :key="item.value"
                    :label="item.label+':'+ searchValue"
                    :value="item.value">
                    <span style="display:inline-block;width:20%;text-align:right;float:left">{{item.label}}</span><span style="display:inline-block;width:60%;text-align:left;float:right">{{searchValue}}</span>
                  </el-option>
                </el-select>
                  <i @click="search('quick')" class="el-icon-search hand searchIcon" style="color: #898989"></i>
        </div>

      </span>

      <span class="more">
        <i
          @click="moreButtonToggle"
          style="
            font-size: 14px;
            color: skyblue;
            cursor: pointer;
            font-style: normal;
            display: none;
          "
          >{{ showValue }}</i
        >
        <span v-show="showMore">
          <!--高级搜索-->
          <el-tooltip
            class="item"
            effect="dark"
            content="高级搜索"
            placement="top"
          >
            <i
              @click="searchdialog = true"
              class="el-icon-zoom-in hand"
              style="padding-left: 5px"
            ></i>
          </el-tooltip>
          <!--分组-->
          <el-dropdown
            @command="typeqh"
            v-model="groupType"
            v-show=" this.selitem != 2 && this.selitem != 3 && this.selitem != 5 && this.selitem != 4 && this.selitem != 6"
          >
            <span class="el-dropdown-link">
              <el-tooltip
                class="item"
                effect="dark"
                content="分组"
                placement="top"
              >
                <i
                  class="el-icon-set-up hand"
                  style="margin-left: 0.5em; color: #898989"
                ></i>
              </el-tooltip>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-for="item in MailGroupType"
                :key="item.value"
                v-text="item.label"
                :command="item.value"
              >
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <!--下载邮件-->
          <el-tooltip
            class="item"
            effect="dark"
            content="下载邮件"
            placement="top"
          >
            <i
              class="el-icon-download hand"
              style="margin-left: 0.5em; color: #898989"
              @click="downloademalia"
            ></i>
          </el-tooltip>
          <!--撤回-->
          <el-tooltip class="item" effect="dark" content="撤回" placement="top">
            <i
              class="iconfont icon-fanhui hand"
              v-show="this.selitem == '3' || this.selitem == '4'"
              style="margin-left: 0.5em"
              @click="emalirecoverys()"
            ></i>
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            content="标记已办"
            placement="top"
          >
            <i
              class="el-icon-thumb hand"
              v-show="
                this.selitem != '1-3' && this.selitem != 3 && this.selitem != 4
              "
              style="margin-left: 0.5em; color: #898989"
              @click="emailoper('handled')"
            ></i>
          </el-tooltip>

          <el-tooltip
            class="item"
            effect="dark"
            content="标红旗"
            placement="top"
          >
            <i
              class="el-icon-s-flag hand"
              v-show="
                this.selitem != '5' && this.selitem != 4
              "
              style="margin-left: 0.5em; color: #898989"
              @click="emailoper('collection')"
            ></i>
          </el-tooltip>

          <el-tooltip
            class="item"
            effect="dark"
            content="取消红旗"
            placement="top"
          >
            <i
              class="el-icon-magic-stick hand"
              v-show="this.selitem == '5' && this.selitem != 3"
              style="margin-left: 0.5em; color: #898989"
              @click="emailoper('cancelCollection')"
            ></i>
          </el-tooltip>

          <el-tooltip class="item" effect="dark" content="加密" placement="top">
            <i
              class="el-icon-lock hand"
              v-show="this.objtype == 'inBox' && this.selitem != 3"
              style="margin-left: 0.5em; color: #898989"
              @click="emailoper('encryption')"
            ></i>
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            content="取消加密"
            placement="top"
          >
            <i
              class="el-icon-unlock hand"
              v-show="this.selitem == '6' && this.selitem != 3"
              style="margin-left: 0.5em; color: #898989"
              @click="emailoper('cancelEncryption')"
            ></i>
          </el-tooltip>

          <el-tooltip class="item" effect="dark" content="删除" placement="top">
            <el-popconfirm
              @confirm="delemail"
              confirm-button-text="好的"
              cancel-button-text="不用了"
              icon="el-icon-info"
              icon-color="red"
              title="确定删除该邮件？"
            >
              <i
                class="el-icon-delete"
                slot="reference"
                style="margin-left: 0.5em; color: #898989"
              ></i>
            </el-popconfirm>
          </el-tooltip>
          <el-tooltip
            class="item"
            effect="dark"
            content="查看回执"
            placement="top"
          >
            <i
              class="iconfont icon-mail-open hand"
              v-show="this.selitem == '3'"
              style="margin-left: 0.5em"
              @click="gethz"
            ></i>
          </el-tooltip>
          <!--排序-->
          <el-dropdown @command="handleSorting">
            <span class="el-dropdown-link">
              <el-tooltip
                class="item"
                effect="dark"
                content="排序"
                placement="top"
              >
                <i
                  class="el-icon-sort hand"
                  style="margin-left: 0.5em; color: #898989"
                ></i>
              </el-tooltip>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="departmentOrder"
                >部门
                <i
                  :class="
                    isDescOrAsc['departmentOrder']
                      ? 'el-icon-sort-down'
                      : 'el-icon-sort-up'
                  "
                ></i>
              </el-dropdown-item>
              <el-dropdown-item command="memberOrder"
                >姓名
                <i
                  :class="
                    isDescOrAsc['memberOrder']
                      ? 'el-icon-sort-down'
                      : 'el-icon-sort-up'
                  "
                ></i>
              </el-dropdown-item>
              <!--排序不需要按照密级排序-->
              <!-- <el-dropdown-item command="secretOrder" v-show="this.$store.state.isredNet">密级  <i :class="isDescOrAsc['secretOrder']?'el-icon-sort-down' :'el-icon-sort-up'"></i></el-dropdown-item> -->
              <el-dropdown-item command="dateOrder"
                >日期
                <i
                  :class="
                    isDescOrAsc['dateOrder']
                      ? 'el-icon-sort-down'
                      : 'el-icon-sort-up'
                  "
                ></i>
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>

          <!-- 邮件移动 -->
          <span
            class="move-mail"
            v-show="
              $store.state.currentMenuType == 'inBox' ||
              $store.state.currentMenuType == 'sent'
            "
            @click="clickDdropDownInfo"
          >
            <el-tooltip
              class="item"
              effect="dark"
              content="邮件移动"
              placement="top"
            >
              <!-- <i class="el-icon-document-remove" style="margin-left: 0.5em; color: #898989"></i> -->
              <i style="margin-left: 0.5em; position: relative; top: 2.5px">
                <svg
                  t="1652940038030"
                  class="icon"
                  viewBox="0 0 1024 1024"
                  version="1.1"
                  xmlns="http://www.w3.org/2000/svg"
                  p-id="1355"
                  xmlns:xlink="http://www.w3.org/1999/xlink"
                  width="16"
                  height="16"
                >
                  <path
                    d="M895.8 592.1a32.2 32.1 0 1 0 64.4 0 32.2 32.1 0 1 0-64.4 0Z"
                    fill="#898989"
                    p-id="1356"
                  ></path>
                  <path
                    d="M928 687.9c-17.8 0-32.2 14.4-32.2 32.1v80.7c0 35.3-28.7 64-64 64H192.5c-35.3 0-64-28.7-64-64V225c0-35.3 28.7-64 64-64H415l82.7 143.3c6.6 11.4 19.2 17.2 31.5 15.8h302.5c35.3 0 64 28.7 64 64v80c0 17.7 14.4 32.1 32.2 32.1s32.2-14.4 32.2-32.1c0-0.8 0-1.6-0.1-2.4v-77.5c0-70.7-57.3-128-128-128H543.9l-82.5-142.9c-7.4-12.9-18-16.2-27.3-16l-0.1-0.1H192.1c-70.7 0-128 57.3-128 128v574.1c0 70.7 57.3 128 128 128h640c70.7 0 128-57.3 128-128v-76.9c0.1-0.8 0.1-1.6 0.1-2.4 0-17.7-14.4-32.1-32.2-32.1z"
                    fill="#898989"
                    p-id="1357"
                  ></path>
                  <path
                    d="M576.7 766.3c12.5 12.5 32.9 12.5 45.4 0L757.5 631c0.3-0.3 0.6-0.5 0.9-0.8h0.1l0.7-0.7c12.3-12.3 12.3-32.4 0-44.7l-136-136c-12.4-12.4-32.5-12.4-44.8-0.1l-0.7 0.7c-12.3 12.3-12.3 32.4 0 44.7l82 82h-372c-17.3 0-31.5 14.2-31.5 31.5s14.2 31.5 31.5 31.5h370.8l-81.7 81.8c-12.6 12.5-12.6 32.9-0.1 45.4z"
                    fill="#898989"
                    p-id="1358"
                  ></path>
                </svg>
              </i>
            </el-tooltip>
          </span>
        </span>
      </span>
    </div>
    <div style="height: calc(100% - 85px)">
      <!--全部-->
      <div
        class="peolist"
        v-if="groupType == 'all'"
        style="
          height: 95%;
          position: relative;
          overflow: auto;
          max-height: 95vh;
        "
      >
        <div
          :class="i.selec ? 'peocard isclick' : 'peocard'"
          v-for="(i, n) in list"
          :key="i.id"
          @click="slecetitem(n)"
          @contextmenu.prevent="openListMenu(i, $event)"
        >
          <el-checkbox
            v-model="i.isselect"
            :key="i.id"
            @click.stop.native
          ></el-checkbox>
          <div class="imgtx">
            <img
              :src="i.img"
              alt=""
              style="width: 100%; height: 100%; border-radius: 50%"
            />
          </div>
          <div>
            <!--发件箱展示  收件人姓名 选择人员-->
            <p
              class="hand sendto"
              style="
                margin: 0 0 5px 0;
                color: #343434;
                width: 100px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                font-size: 14px;
              "
              :title="
                selitem == '3'
                  ? i.sendto && i.sendto.indexOf('(') == -1
                    ? i.sendto
                    : i.sendto.indexOf(',') !== -1
                    ? i.sendto
                        .split(',')
                        .map(function (item) {
                          return item.indexOf('(') !== -1
                            ? item.substring(0, item.indexOf('('))
                            : item;
                        })
                        .toString()
                    : i.sendto.substring(0, i.sendto.indexOf('('))
                  : i.senderName
              "
            >
              {{
                selitem == "3"
                  ? i.sendto && i.sendto.indexOf("(") == -1
                    ? i.sendto
                    : i.sendto.indexOf(",") !== -1
                    ? i.sendto
                        .split(",")
                        .map(function (item) {
                          return item.indexOf("(") !== -1
                            ? item.substring(0, item.indexOf("("))
                            : item;
                        })
                        .toString()
                    : i.sendto.substring(0, i.sendto.indexOf("("))
                  : i.senderName
              }}
            </p>
            <p
              class="hand subject"
              id="subject"
              style="
                margin: 0;
                font-size: 14px;
                color: #676767;
                width: 195px;
                overflow: hidden;
                text-overflow: ellipsis;
                white-space: nowrap;
                font-size: 13px;
              "
              :title="i.subject"
            >
              {{ i.subject }}
            </p>
          </div>
          <!-- <div v-show="i.sendto && i.sendto.indexOf('(') == -1"> -->
          <!--发件箱展示  收件人姓名 为部门-->
          <!-- <p class="hand" style="margin:0 0 5px 0;color: #343434;width: 7em;overflow: hidden;text-overflow: ellipsis; white-space: nowrap;" :title="i.sendto">{{ selitem =='3' ? i.sendto:i.senderName}}</p>
                        <p class="hand" style="margin: 0;font-size: 14px;color: #676767;width:15em;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;" :title="i.subject">{{i.subject}}</p>
                    </div> -->
          <p
            style="
              margin: 0;
              color: #343434;
              position: absolute;
              top: 12px;
              right: 10px;
              font-size: 13px;
              color: #bbbcbc;
            "
          >
            <i
              class="el-icon-paperclip"
              style="margin-right: 0.5em"
              v-if="i.attachmentsFlag"
            >
            </i>
            <i
              class="el-icon-s-flag"
              style="margin-right: 0.5em; color: #ffb800"
              v-if="i.collect == 1"
            >
            </i>
            <span v-show="objtype == 'sent'" style="color: #f2a518">
              {{ i.passTheAudit == 1 ? "已通过" : "审核中" }}
            </span>
            <!-- 如果当前邮件不涉密那么将密级颜色改为#508ff5 提高对比度 tangxiangping 2022-06-07 start -->
            <span
              v-if="i.secretNameStr === '公开' || i.secretNameStr === '内部'"
              style="color: #508ff5; margin: 0 0.5em"
            >
              {{ i.secretNameStr }}
            </span>
            <span v-else style="color: #ff5722; margin: 0 0.5em">
              {{ i.secretNameStr }}
            </span>
            <!-- 如果当前邮件不涉密那么将密级颜色改为#508ff5 提高对比度 tangxiangping 2022-06-07 end -->
            {{ i.createDate.substring(0, 10) }}
          </p>
          <div style="margin-left: auto; width: 20px" v-if="selitem != '3'">
            <div class="dian" v-if="!i.readFlag"></div>
          </div>
          <div></div>
        </div>
        <div style="height: 2em; width: 100%"></div>
      </div>
      <!--分组-->
      <div
        class="peolist"
        v-else
        style="
          height: 95%;
          position: relative;
          min-height: 500px;
          max-height: 95vh;
          overflow: auto;
        "
      >
        <div v-for="(m, q) in list" :key="m.id">
          <div
            style="
              padding: 5px 0 3px 3px;
              font-size: 14px;
              font-weight: 600;
              font-family: 'Arial', 'Microsoft YaHei', '黑体', '宋体',
                sans-serif;
              color: #c8423c;
            "
          >
            {{ m.groupingName }}
            <div
              style="
                width: 100%;
                height: 1px;
                border-bottom: 2px solid #c8423c;
                margin-top: 5px;
              "
            ></div>
          </div>
          <div
            :class="i.selec ? 'peocard isclick' : 'peocard'"
            v-for="(i, n) in m.groupingData"
            :key="i.id"
            @click="slecetitem(n, q)"
          >
            <el-checkbox
              v-model="i.isselect"
              :key="i.id"
              @click.stop.native
            ></el-checkbox>
            <div class="imgtx">
              <img
                :src="i.img"
                alt=""
                style="width: 100%; height: 100%; border-radius: 50%"
              />
            </div>
            <div>
              <!--发件箱展示  收件人姓名 选择人员-->
              <p
                class="hand sendto"
                style="
                  margin: 0 0 5px 0;
                  color: #343434;
                  width: 100px;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  font-size: 14px;
                "
                :title="
                  selitem == '3'
                    ? i.sendto && i.sendto.indexOf('(') == -1
                      ? i.sendto
                      : i.sendto.indexOf(',') !== -1
                      ? i.sendto
                          .split(',')
                          .map(function (item) {
                            return item.substring(0, item.indexOf('('));
                          })
                          .toString()
                      : i.sendto.substring(0, i.sendto.indexOf('('))
                    : i.senderName
                "
              >
                {{
                  selitem == "3"
                    ? i.sendto && i.sendto.indexOf("(") == -1
                      ? i.sendto
                      : i.sendto.indexOf(",") !== -1
                      ? i.sendto
                          .split(",")
                          .map(function (item) {
                            return item.substring(0, item.indexOf("("));
                          })
                          .toString()
                      : i.sendto.substring(0, i.sendto.indexOf("("))
                    : i.senderName
                }}
              </p>
              <p
                class="hand subject"
                style="
                  margin: 0;
                  font-size: 14px;
                  color: #676767;
                  width: 195px;
                  overflow: hidden;
                  text-overflow: ellipsis;
                  white-space: nowrap;
                  font-size: 13px;
                "
                :title="i.subject"
              >
                {{ i.subject }}
              </p>
            </div>
            <p
              style="
                margin: 0;
                color: #343434;
                position: absolute;
                top: 12px;
                right: 10px;
                font-size: 13px;
                color: #bbbcbc;
              "
            >
              <i
                class="el-icon-paperclip"
                style="margin-right: 0.5em"
                v-if="i.attachmentsFlag"
              ></i>
              <i
                class="el-icon-s-flag"
                style="margin-right: 0.5em; color: #ffb800"
                v-if="i.collect == 1"
              ></i
              >、
              <span style="color: #ff5722; margin-right: 0.5em">{{
                i.secretNameStr
              }}</span>

              {{ i.createDate.substring(0, 10) }}
            </p>
            <div style="margin-left: auto; width: 20px" v-if="selitem != '3'">
              <div class="dian" v-if="!i.readFlag"></div>
            </div>
            <div></div>
          </div>
        </div>

        <div style="height: 2em; width: 100%"></div>
      </div>
      <el-pagination
        v-if="groupType == 'all'"
        small
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page.sync="pageNo"
        :page-size="pageSize"
        layout="slot,total, prev, pager, next"
        :total="total"
        :pager-count="5"
        style="position: relative; bottom: 5px; left: 5px"
      >
        <div
          class="slot"
          style="
            width: 35px;
            display: inline-block;
            margin-right: 75px;
            font-weight: normal;
            font-size: 13px;
          "
        >
          每1页<input
            type="text"
            :value="pageSize"
            @blur="setPageSize()"
            ref="pageSize"
            style="width: 100%; text-align: center"
          />条
        </div>
      </el-pagination>
      <el-pagination
        v-else
        small
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page.sync="pageNo"
        :page-size="pageSize"
        :pager-count="5"
        layout="slot,total, prev, pager, next"
        :total="total"
        style="position: relative; bottom: 5px; left: 5px"
      >
        <div
          class="slot"
          style="
            width: 35px;
            display: inline-block;
            margin-right: 75px;
            font-weight: normal;
            font-size: 13px;
          "
        >
          每页<input
            type="number"
            :value="pageSize"
            @blur="setPageSize()"
            ref="pageSize"
            style="
              width: 100%;
              text-align: center;
              height: 20px;
              line-height: 20px;
            "
          />条
        </div>
      </el-pagination>
    </div>
    <ContextListMenu
      :isContextListMenuShow="isContextListMenuShow"
      :menuPosition="menuPosition"
      :currentListMenuData="currentListMenuData"
      @onCloseContextListMenu="closeContextListShow"
    />
    <!-- 邮件移动 -->
    <MoveMail ref="moveMail" @onMoveSuccess="handleMoveSuccess" />
  </div>
</template>

<script>
import api from "@/api/api";
// 导入 vuex modules layout
import layoutStore from "@/store/modules/layout.js";

/**
 * 导入 组件
 */
// 邮件移动
import MoveMail from "./components/moveMail.vue";
import ContextListMenu from "./components/contextListMenu.vue";

// 导入工具函数
import EventBus from "@/utils/eventBus.js";
import { createNamespacedHelpers } from "vuex";
const { mapState, mapMutations } = createNamespacedHelpers("contextMenu");
export default {
  name: "Contacts",
  components: {
    MoveMail,
    ContextListMenu,
  },
  beforeRouteLeave(to, from, next) {
    this.refsearc();
    next();
  },
  data() {
    return {
      // 输入框绑定值 fuguichaun
      value123:[],
      searchdialog: false,
      objtype: "",
      dialogFormVisible: false,
      islogn: false,
      showhz: false,
      gridData: [],
      password: "",
      oldPassword: "",
      password1: "",
      msg: "",
      pwd: false,
      formLabelAlign: {
        0: "",
        1: "",
        2: "",
        3: "",
        4: "",
        5: "",
        6: "",
        7: "",
      },
      options: [
        {
          value: "1",
          label: "标题",
        },
        {
          value: "2",
          label: "日期",
        },
        {
          value: "3",
          label: "收件人",
        },
        {
          value: "4",
          label: "抄送人",
        },
        {
          value: "5",
          label: "发起人",
        },
      ],
      MailGroupType: [],
      loading: false,
      allchecked: false,
      checked: false,
      groupType: "all",
      searchType: "",
      timeinout: false,
      searchWord: "",
      searchWordtime: [],
      list: [],
      pageNo: 1,
      pageSize: 20,
      total: 0,
      showMore: true, //显示更多，默认不显示更多
      searchValue: "", //搜索值
      showSearchBox: true, //默认显示搜索框
      recipient: "", //收件人显示
      copy: "", //抄送人显示
      organizer: "", //发起人显示
      showValue: "更多", //显示更多或隐藏
      orderType: "", //排序
      orderRule: "", //排序方式，升序或降序
      isDescOrAsc: {
        //排序元素：部门、姓名、密级、日期 默认是降序
        departmentOrder: true,
        memberOrder: true,
        secretOrder: true,
        dateOrder: true,
      },
      searchConditions:[],
      // 注释邮件搜索类型 fuguichaun
      // searchConditions: [
      //   { value: "0", label: "全部" },
      //   { value: "1", label: "标题" },
      //   { value: "3", label: "收件人" },
      //   { value: "4", label: "抄送人" },
      //   { value: "5", label: "发起人" },
      //   { value: "6", label: "正文" },
      //   { value: "7", label: "附件" },
      // ],
      searchSel: "0",
      selitem: "",
      LevelOneMenus: [],
      // 当前点击的邮箱 信息
      menuInfo: {},
      // 邮件项菜单是否展示
      isContextListMenuShow: false,
      // 邮件项菜单位置
      menuPosition: {},
      // 邮件项数据
      currentListMenuData: null,
    };
  },
  created() {
    this.getMailGroupType();
    this.getMenus();
    this.getlist();
  },
  watch: {
    // 监听 fuguichaun
    searchValue:{
       handler(n){
        if(n == ''){
          // 清空搜索内容
            this.searchConditions = []
            this.searchValue = ''
            this.value123 = []
        }
      },
      immediate: true,
      deep:true
    },
    value123:{
      handler(n){
        this.searchSel = n
      }
    },
    searchdialog: {
      handler(n) {
        if (n == true) {
          this.refsearc();
        }
      },
      immediate: true,
    },
    pageSize(val) {
      if (val > 200) {
        this.pageSize = 200;
        this.handleSizeChange(200);
      } else if (val <= 0) {
        this.pageSize = 1;
        this.handleSizeChange(1);
      } else {
        this.handleSizeChange(val - 0);
      }
    },
    "$store.state.cancelSaveDraftRandom": {
      handler(val, oldVal) {
        if (val !== oldVal) {
          this.getlist();
        }
      },
    },
    //lyy 监听邮件列表改变
    list: {
      handler(val) {
        // console.log("监听邮件列表改变", val);
        if (val.length > 0) {
          // 邮件列表控制全选框全选
          this.allchecked = val.every((item) => item.isselect);
        } else {
          this.allchecked = false;
        }
      },
      deep: true,
    },
  },
  computed: {
    ...mapState(["curClick"]),
  },
  mounted() {
    // this.dialogFormVisible = false;
    // 当前组件会存在多层嵌套调用, 会导致多次挂载当前事件, 并重复执行事件代码(使用前先关闭vue原型上注册的实例)
    EventBus.$off("main_navigation_menuClick");
    EventBus.$off("main_navigation_setFilterRule");

    // 添加 自定义事件, 获取 当前点击的菜单信息(主页_邮箱列表)
    EventBus.$on("main_navigation_menuClick", (menuInfo) => {
      this.menuInfo = menuInfo;
      this.searchValue = "";
      this.$store.commit("changeQuickSearchInputValue", "");
      //   lyy start
      this.formLabelAlign = {
        0: "",
        1: "",
        2: "",
        3: "",
        4: "",
        5: "",
        6: "",
        7: "",
      };
      //   lyy end
      this.getlist();
      // 2023-07-05 切换菜单时更新 分页器到第一页
      this.pageNo = 1
      console.log('切换菜单时更新 分页器到第一页:>> ', this.pageNo);
        // 2023-07-05 切换菜单时更新 分页器到第一页
    });

    EventBus.$on("main_navigation_setFilterRule", () => {
      this.menuInfo = this.$store.state.currentMenuInfo;
      this.getlist();
    });
  this.getNewMenusList()//获取第三方菜单
  },
  methods: {
    // 输入框值变化执行 fuguichuan
    remoteMethod(query) {
      this.searchValue = query
      console.log('this.searchValue',this.searchValue)
        if (query !== '') {
          this.loading = true;
          setTimeout(() => {
            this.loading = false;
            this.searchConditions = [
              { value: "0", label: "全部" },
              { value: "1", label: "标题" },
              { value: "3", label: "收件人" },
              { value: "4", label: "抄送人" },
              { value: "5", label: "发起人" },
              { value: "6", label: "正文" },
              { value: "7", label: "附件" },
            ]
          });
        } else {
          this.searchConditions = [];
        }
      },
    handleMoveSuccess() {
      this.menuInfo = this.$store.state.currentMenuInfo;
      this.getlist();
    },
        //第三方待办菜单
    getNewMenusList() {
      this.$store.dispatch("getNewMenus");
    },
    /**
     * 获取下来菜单
     */
    getMenus() {
      this.LevelOneMenus = this.$store.state.menus;
      // 添加默认选中菜单信息
      this.menuInfo = this.LevelOneMenus[0];
    },

    /**
     * 传递多个command值
     */
    composeValue(type, path) {
      return {
        type: type,
        path: path,
      };
    },
    /**
     * 移动功能的下拉菜单
     */
    clickDdropDownInfo() {
      // if (command == "0") {
      //   return;
      // } else {
      //   if (
      //     this.$store.state.moveType == this.$store.state.folderCurrentClicked
      //   ) {
      //     this.$confirm(`是否移动?`, {
      //       confirmButtonText: "确定",
      //       cancelButtonText: "取消",
      //     })
      //       .then(() => {
      //         let data = [];

      //         for (let i = 0; i < this.checkedEmailsId.length; i++) {
      //           let obj = {
      //             affairId: "",
      //             folderPath: command,
      //           };
      //           obj.affairId = this.checkedEmailsId[i] + "";
      //           data.push(obj);
      //         }
      //         let dataobj = { data: [] };
      //         dataobj.data = data;
      //         api.moveFolder(dataobj).then((res) => {
      //           this.$message({
      //             type: "info",
      //             message: "移动成功！",
      //           });
      //         });
      //       })
      //       .catch(() => {
      //         this.$message({
      //           type: "info",
      //           message: "已取消移动",
      //         });
      //         this.$store.state.moveType = "";
      //       });
      //   } else {
      //     alert("请在同级目录下移动邮件！！");
      //     return;
      //   }
      // }

      // 筛选需要移动的数据
      const needMoveMails = this.list.filter((i) => i.isselect);
      if (needMoveMails.length) {
        this.$refs.moveMail.show(needMoveMails);
      } else {
        this.$message.info("请选择需要移动的邮件信息");
      }
    },
    // pageSize的修改失焦事件
    setPageSize() {
      const val = this.$refs.pageSize.value;
      this.pageSize = val - 0;
    },
    //搜索条件
    searchCondition() {
      // this.searchValue = "";
      Object.assign(
        this.$data.formLabelAlign,
        this.$options.data().formLabelAlign
      );
      this.$nextTick(()=>{
        this.search('quick')
      })
    },
    //排序事件
    handleSorting(command) {
      this.orderType = command; //选择的排序(部门，姓名,密级,日期)
      if (this.isDescOrAsc[this.orderType]) {
        //降序
        this.orderRule = "desc";
        this.isDescOrAsc[this.orderType] = false;
      } else {
        this.orderRule = "asc";
        this.isDescOrAsc[this.orderType] = true;
      }
      this.search("sort");
    },
    //更多按钮 点击事件
    moreButtonToggle() {
      this.showMore = !this.showMore;
      if (this.showMore) {
        //点击更多后需要阴藏搜索框
        this.showSearchBox = false;
        this.showValue = "隐藏";
      } else {
        this.showSearchBox = true;
        this.showValue = "更多";
      }
    },
    sqss() {
      this.$emit("sqlist");
    },
    //选人
    setPeople(type, source = 0) {
      let _that = this;
      parent.$.selectPeople({
        // 授权时的组织机构面板内容
        type: "selectPeople",
        panels: "Member,Department,Team,Account,Outworker",
        selectType: "Account,Department,Member,Team,Post,Outworker",
        onlyLoginAccount: false,
        isNeedCheckLevelScope: false, //不受职务级别控制
        minSize: 0,
        maxSize: 1, //
        excludeElements: "",
        hiddenPostOfDepartment: true,
        params: {},
        fillBackData: {},
        callback(result) {
          // _this.$set(_this.basicInfo, "manager", result.text);
          // _this.$set(_this.basicInfo, "managerId", result.value.slice(7));
          switch (
            type //modify20210902 需求13:  优化选择收件人、抄送人、发起人,建议传id来搜索
          ) {
            case "start": //收件人
              _that.formLabelAlign["3"] = result.value;
              _that.recipient = result.text;
              if (source == "quick") {
                //快速搜索需要为输入框赋值
                _that.searchValue = result.text;
              }
              break;
            case "copy": //抄送人
              _that.formLabelAlign["4"] = result.value;
              _that.copy = result.text;
              if (source == "quick") {
                _that.searchValue = result.text;
              }
              break;
            case "send": //发起人
              _that.formLabelAlign["5"] =
                result.value &&
                result.value.substring(
                  result.value.indexOf("|") + 1,
                  result.value.length
                );
              _that.organizer = result.text;
              if (source == "quick") {
                _that.searchValue = result.text;
              }
              break;
          }
        },
      });
    },
    log() {
      // this.formLabelAlign['2']
    },
    editpwd() {
      this.msg = "修改密码";
      this.pwd = false;
    },
    emalirecoverys() {
      let obj = {
        affairIds: [],
        type: "",
      };

      if (this.groupType == "all") {
        this.list.forEach((d) => {
          if (d.isselect == true) {
            obj.affairIds.push(d.idStr);
          }
        });
      } else {
        for (let i = 0; i < this.list.length; i++) {
          for (let d = 0; d < this.list[i].groupingData.length; d++) {
            if (this.list[i].groupingData[d].isselect == true) {
              obj.affairIds.push(this.list[i].groupingData[d].idStr);
            }
          }
        }
      }
      obj.affairIds = obj.affairIds.toString();

      if (obj.affairIds) {
        if (this.selitem == "3") {
          obj.type = "rescindMail";
          api.judgeRescindMail(obj).then((res) => {
            if (res.code == "10001") {
              api.emalirecovery(obj).then((res) => {
                if (res.code == "00010012") {
                  this.$message({
                    message: res.msg,
                    type: "success",
                  });
                  this.refnavs();
                  this.getlist();
                }
              });
            } else {
              this.$message({
                message: res.msg,
                type: "warning",
              });
            }
          });
        } else {
          obj.type = "recovery";
          api.recoverydel(obj).then((res) => {
            if (res.code == "10001") {
              this.$message({
                message: res.msg,
                type: "success",
              });
              this.allchecked = false;
              this.refnavs();
              this.getlist();
            } else {
              this.$message({
                message: res.msg,
                type: "warning",
              });
            }
          });
        }
      } else {
        this.$message.warning("请选择数据");
      }
    },
    //分组事件
    typeqh(command) {
      this.groupType = command; //选中分组的值
      this.pageNo = 1;
      this.getlist();
    },
    getMailGroupType() {
      api.getMailGroupType().then((res) => {
        if (res.code == "1001") {
          this.MailGroupType = res.msg;
        }
      });
    },
    pwdback() {
      this.dialogFormVisible = true;
      // layoutStore.state.isNotOperable = false
      this.$router.go(0);

      // this.$router.back(0);
    },
    addpassword() {
      let obj;
      if (this.msg == "修改密码") {
        obj = {
          isSetPwd: false,
          newPassword: this.password,
          oldPassword: this.oldPassword,
          checkPassword: this.password1,
        };
      } else {
        obj = {
          isSetPwd: true,
          newPassword: this.password,
          checkPassword: this.password1,
        };
      }
      api.updateOrAddPwd(obj).then((res) => {
        if (res.code == "00010001") {
          this.$message({
            message: "密码设置成功",
            type: "success",
          });
          this.getlist();
        } else {
          this.$message({
            message: res.msg,
            type: "warning",
          });
        }
      });
    },
    login() {
      let obj = {
        isAC: false,
        password: this.password,
      };
      api.encryption(obj).then((res) => {
        if (res.code == "00010001") {
          this.$message({
            message: "登录成功",
            type: "success",
          });
          this.dialogFormVisible = false;
          this.islogn = true;
        } else {
          this.$message({
            message: res.msg,
            type: "warning",
          });
        }
      });
    },
    qksearsh() {
      this.searchWord = "";
      this.searchWordtime = [];
      this.searchType == "2"
        ? (this.timeinout = true)
        : (this.timeinout = false);
    },
    //搜索 type参数判断是快速搜索还是高级搜索
    search(type) {
      //   console.log("搜索");
      if (this.formLabelAlign["2"]) {
        this.formLabelAlign["2"] = this.formLabelAlign["2"]
          .toString()
          .replace(",", "#");
      }
      if (type == "quick") {
        //快速搜索
        switch (this.searchSel) {
          case "0":
            this.formLabelAlign["0"] = this.searchValue;
            break;
          case "1":
            this.formLabelAlign["1"] = this.searchValue;
            break;
          case "3" :
              this.formLabelAlign['3'] = this.searchValue;
              break;
           case "4" :
              this.formLabelAlign['4'] = this.searchValue;
              break;
           case "5" :
              this.formLabelAlign['5'] = this.searchValue;
              break;
          case "6":
            this.formLabelAlign["6"] = this.searchValue;
            break;
          case "7":
            this.formLabelAlign["7"] = this.searchValue;
            break;
          default:
            break;
        }
      }
      this.getlist();
      // 搜索之后将正文搜索内容高亮显示 tangxiangping 2022-06-13 start
      const defaultValue = this.$store.state.detailContent;
      // 在点击搜索的时候修改search的值
      this.$store.commit("changeQuickSearchInputValue", this.searchValue);
      // 如果值存在，说明当前对应详情路由
      if (defaultValue) {
        // console.log("defaultValue", defaultValue);
        // 如果是详情页面并且输入框有值，那么将输入框的值在正文中高亮
        if (this.$route.path.indexOf("inboxDet") != -1 && this.searchValue) {
          document
            .querySelector(".edui-default iframe")
            .contentWindow.document.querySelector("html > .view").innerHTML =
            defaultValue.replace(
              new RegExp(this.searchValue, "g"),
              '<span style="background: yellow;" class="custom-style">' +
                this.searchValue +
                "</span>"
            );
        } else if (!this.searchValue) {
          // 如果输入框里面没有值且触发了搜索，那么将正文恢复默认
          document
            .querySelector(".edui-default iframe")
            .contentWindow.document.querySelector("html > .view").innerHTML =
            defaultValue;
        }
      }
      // 搜索之后将正文搜索内容高亮显示 tangxiangping 2022-06-13 end
    },
    refsearc() {
      Object.assign(
        this.$data.formLabelAlign,
        this.$options.data().formLabelAlign
      );
      //需求13 清空收件人，抄送人，发件人显示值
      this.recipient = "";
      this.copy = "";
      this.organizer = "";
      //需要清空排序
      this.orderType = "";
      Object.assign(this.$data.isDescOrAsc, this.$options.data().isDescOrAsc);
      //this.isDescOrAsc ={  "departmentOrder":true, "memberOrder":true, "secretOrder":true, "dateOrder":true}
      //清空搜索框
      this.searchValue = "";
      //清空精确搜索的下拉框
      this.searchSel = "0";
    },
    refnav() {
      this.$emit("refleftnav");
    },
    refnavs() {
      this.$emit("refleftnavs");
    },
    // 全选框
    allslect() {
      if (this.groupType == "all") {
        this.list.forEach((d) => {
          if (this.allchecked) {
            d.isselect = true;
          } else {
            d.isselect = false;
          }
        });
      } else {
        for (let i = 0; i < this.list.length; i++) {
          for (let d = 0; d < this.list[i].groupingData.length; d++) {
            if (this.allchecked) {
              this.list[i].groupingData[d].isselect = true;
            } else {
              this.list[i].groupingData[d].isselect = false;
            }
          }
        }
      }
    },
    // 获取邮件列表
    getlist(n) {
      //   console.log("获取邮件列表");
      // debugger
      //改变面板的布局
      if (
        document.querySelector(".mid") &&
        document.querySelector(".mid").style.display == "none"
      ) {
        document.querySelector(".mid").style.display = "";
      }
      if (
        document.querySelector(".resize") &&
        document.querySelector(".resize").style.display == "none"
      ) {
        document.querySelector(".resize").style.display = "";
      }
      if (
        document.querySelector(".right") &&
        document.querySelector(".right").style.width == "auto"
      ) {
        document.querySelector(".right").style.width = "";
      }
      this.list = [];
      let obj = {
        type: "",
        inBoxType: "",
        path: "", //用于筛选二级菜单
        groupType: this.groupType,
        searchType: this.searchType,
        searchWord: JSON.stringify(this.formLabelAlign),
        orderType: this.orderType, //排序类型
        orderRule: this.orderRule, //降序或升序
        pageSize: this.pageSize,
        pageNo: this.pageNo,
      };
      //   console.log(
      //     "获取邮件列表",
      //     this.$store.state.customFloder,
      //     "this.menuInfo",
      //     this.menuInfo,
      //     "this.$store.state.customFloder.find((item) => item == this.menuInfo)",
      //     this.$store.state.customFloder.find((item) => item == this.menuInfo)
      //   );
      // 验证 vuex中存储的 自定义文件夹 信息中是否存在 当前选中的菜单信息
      if (this.$store.state.customFloder.find((item) => item.folderId == this.menuInfo.folderId)) {
        // 存在, 则表示 当前选中的 菜单为 自定义的文件夹, 添加 请求参数
        obj.path = this.menuInfo.path;
      }
      if (n == "n") {
        obj.type = "inBox";
      } else if (n == "encryption") {
        obj.type = "encryption";
      } else {
        // 更新请求参数
        obj.type = this.menuInfo.type || 'inBox';
        obj.inBoxType = this.menuInfo.inBoxType || "";
        // 当前选中菜单
        this.selitem = this.menuInfo.index ? this.menuInfo.index : this.menuInfo.type === 'sent' ? '3' : '1';
      }
      this.searchdialog = false;
      this.objtype = obj.type;
      if (obj.type == "encryption") {
        api.encryption({ isAC: true }).then((res) => {
          if (res.code == "00010002") {
            this.msg = res.msg;
          } else {
            this.msg = "请输入密码";
            this.pwd = true;
          }
          if (!this.islogn) {
            this.dialogFormVisible = true;
          }
        });
      }
        // console.log("obj", obj);
      // 容错措施，当type不存在时，提示用户

      if (obj.type) {
        api.emlist(obj).then((res) => {
          if (res.code == "00010001") {
            let arr = res.msg.data;
            if (this.groupType == "all") {
              arr.forEach((d) => {
                d.isselect = false;
                if (d.idStr == this.$store.state.emliaId) {
                  // 设置item选中
                  d.selec = true;
                  // 设置红点隐藏
                  d.readFlag = true;
                } else {
                  d.selec = false;
                }
              });

            } else {
              let drr = res.msg.data;

              for (let i = 0; i < drr.length; i++) {
                for (let d = 0; d < drr[i].groupingData.length; d++) {
                  if (
                    drr[i].groupingData[d].idStr == this.$store.state.emliaId
                  ) {
                    drr[i].groupingData[d].selec = true;
                    drr[i].groupingData[d].readFlag = true;
                  } else {
                    drr[i].groupingData[d].selec = false;
                  }
                }
              }
            }

            this.list = arr;
            this.total = parseInt(res.msg.total);
            //   console.log("list", this.list);
          } else {
            this.$message.error(res.msg || "加载失败");
          }
        });
      } else {
        this.$message.error("加载失败，请刷新后重试");
      }
    },
    slecetitem(n,q) {
      if (this.groupType == "all") {
        this.list.forEach((d) => {
          d.selec = false;
        });
        this.list[n].selec = true;
        this.list[n].readFlag = true;
        this.$store.commit("itemslect", this.objtype);
        if (this.$store.state.isredNet) {
          api
            .MailPersonSecretVerify({ fileId: this.list[n].summaryIdStr })
            .then((res) => {
              if (res.code == 10003) {
                this.$router.push({
                  path: `/inboxDet/${this.list[n].idStr}/false`,
                });

              } else {
                this.list[n].selec = false;
                this.list[n].readFlag = false;
                this.$message.error(res.msg);
                this.$router.push({ path: "/" });
              }
            });
        } else {
          this.$router.push({
            path: `/inboxDet/${this.list[n].idStr}/false`,
          });
        }
      } else {
        for (let i = 0; i < this.list.length; i++) {
          for (let d = 0; d < this.list[i].groupingData.length; d++) {
            this.list[i].groupingData[d].selec = false;
          }
        }
        this.list[q].groupingData[n].selec = true;
        this.list[q].groupingData[n].readFlag = true;
        if (this.$store.state.isredNet) {
          api
            .MailPersonSecretVerify({
              fileId: this.list[q].groupingData[n].summaryIdStr,
            })
            .then((res) => {
              if (res.code == "10003") {
                this.$router.push({
                  path: `/inboxDet/${this.list[q].groupingData[n].idStr}/false`,
                });
              } else {
                this.list[n].selec = false;
                this.list[n].readFlag = false;
                this.$message.error(res.msg);
                this.$router.push({ path: "/" });
              }
            });
        } else {
          this.$router.push({
            path: `/inboxDet/${this.list[q].groupingData[n].idStr}/false`,
          });
        }
      }
    },
    delemail() {
      let obj = {
        affairId: [],
        type: "",
      };
      if (this.groupType == "all") {
        this.list.forEach((d) => {
          if (d.isselect == true) {
            obj.affairId.push(d.idStr);
          }
        });
      } else {
        for (let i = 0; i < this.list.length; i++) {
          for (let d = 0; d < this.list[i].groupingData.length; d++) {
            if (this.list[i].groupingData[d].isselect == true) {
              obj.affairId.push(this.list[i].groupingData[d].idStr);
            }
          }
        }
      }
      // this.list.forEach(d => {
      //     if (d.isselect == true) {
      //         obj.affairId.push(d.idStr)
      //     }
      // });
      obj.affairId = obj.affairId.toString();
      if (obj.affairId) {
        if (this.selitem == "4") {
          obj.type = "delete";
        } else {
          obj.type = this.objtype;
        }
        api.delemali(obj).then((res) => {
          if (res.code == "00010001") {
            this.$message({
              message: res.msg,
              type: "success",
            });
            this.allchecked = false;
            this.refnavs();
            this.getlist();
          }
        });
      } else {
        this.$message.warning("请选择数据");
      }
    },
    gethz() {
      this.gridData = [];
      let obj = {
        affairId: [],
        summaryId: [],
        openFrom: "sent",
      };
      if (this.groupType == "all") {
        this.list.forEach((d) => {
          if (d.isselect == true) {
            obj.affairId.push(d.idStr);
            obj.summaryId.push(d.summaryIdStr);
          }
        });
      } else {
        for (let i = 0; i < this.list.length; i++) {
          for (let d = 0; d < this.list[i].groupingData.length; d++) {
            if (this.list[i].groupingData[d].isselect == true) {
              obj.affairId.push(this.list[i].groupingData[d].idStr);
              obj.summaryId.push(this.list[i].groupingData[d].summaryIdStr);
            }
          }
        }
      }

      if (obj.affairId.length > 1 || obj.affairId.length == 0) {
        this.$message({
          message: "请选择一项数据",
          type: "warning",
        });
      } else {
        obj.affairId = obj.affairId.toString();
        obj.summaryId = obj.summaryId.toString();
        api.getcheckStatus(obj).then((res) => {
          if (res.code == "00010011") {
            this.gridData = res.msg;
            this.showhz = true;
          }
        });
      }
    },
    downloademalia() {
      let obj = {
        affairId: [],
      };
      if (this.groupType == "all") {
        this.list.forEach((d) => {
          if (d.isselect == true) {
            obj.affairId.push(d.idStr);
          }
        });
      } else {
        for (let i = 0; i < this.list.length; i++) {
          for (let d = 0; d < this.list[i].groupingData.length; d++) {
            if (this.list[i].groupingData[d].isselect == true) {
              obj.affairId.push(this.list[i].groupingData[d].idStr);
            }
          }
        }
      }
      obj.affairId = obj.affairId.toString();

      if (obj.affairId == "") {
        this.$message.warning("请选择数据");
        return false;
      }
      api.exportEmail(obj).then((res) => {
        if (res.code == 10001) {
          this.allchecked = false;
          /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240711 start*/
          document.getElementById(
            "downloadIframe"
          ).src = `/api/attachment/download/${res.msg.fileId}?fileName=${encodeURI(res.msg.fileName)}`;
          /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240711 end*/
        } else {
          this.$message.warning(res.msg);
        }
      });
    },

    emailoper(n) {
      //   console.log(11111111111);
      let obj = {
        affairId: [],
        flagType: n,
      };
      // 判断在发件箱时标红旗逻辑 fuguichuan
      if(this.selitem == 3){
         let sList = []
         let aList = []
        if (this.groupType == "all") {
        this.list.forEach((d) => {
          if (d.isselect == true) {
              sList.push(d)
              if(d.passTheAudit == 1 && d.collect != 1){
                obj.affairId.push(d.idStr)
              }
              else{
              aList.push(d)
              }
              }
            })
      } else {
        for (let i = 0; i < this.list.length; i++) {
          for (let d = 0; d < this.list[i].groupingData.length; d++) {
            if (this.list[i].groupingData[d].isselect == true) {
              obj.affairId.push(this.list[i].groupingData[d].idStr);
            }
          }
        }
      }
      obj.affairId = obj.affairId.toString();
      if (obj.affairId == '') {
        this.$message.warning("请选择一条已审核或未收藏的数据");
        return false;
      }
      if (aList.length > 0) {
        this.$confirm('选中的数据包含有未审核或已收藏的数据, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          api.operemali(obj).then((res) => {
        if (res.code == "00010001") {
          this.$message({
            message: "操作成功",
            type: "success",
          });
          if (n == "cancelEncryption") {
            let obj = {
              type: "encryption",
              inBoxType: "",
              groupType: this.groupType,
              searchType: this.searchType,
              searchWord: this.searchWord,
              pageSize: this.pageSize,
              pageNo: this.pageNo,
            };
            api.emlist(obj).then((res) => {
              if (res.code == "00010001") {
                let arr = res.msg.data;
                arr.forEach((d) => {
                  d.isselect = false;
                  if (d.id == this.$store.state.emliaId) {
                    d.selec = true;
                    d.readFlag = true;
                  } else {
                    d.selec = false;
                  }
                });
                this.list = arr;
                this.total = parseInt(res.msg.total);
              }
              this.refnavs();
            });
          } else {
            this.refnavs();
            this.getlist();
          }
          this.allchecked = false;
        } else {
          this.$message({
            message: res.msg,
            type: "warning",
          });
        }
      });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消'
          });
        });
      }
      else{
        api.operemali(obj).then((res) => {
        if (res.code == "00010001") {
          this.$message({
            message: "操作成功",
            type: "success",
          });
          if (n == "cancelEncryption") {
            let obj = {
              type: "encryption",
              inBoxType: "",
              groupType: this.groupType,
              searchType: this.searchType,
              searchWord: this.searchWord,
              pageSize: this.pageSize,
              pageNo: this.pageNo,
            };
            api.emlist(obj).then((res) => {
              if (res.code == "00010001") {
                let arr = res.msg.data;
                arr.forEach((d) => {
                  d.isselect = false;
                  if (d.id == this.$store.state.emliaId) {
                    d.selec = true;
                    d.readFlag = true;
                  } else {
                    d.selec = false;
                  }
                });
                this.list = arr;
                this.total = parseInt(res.msg.total);
              }
              this.refnavs();
            });
          } else {
            this.refnavs();
            this.getlist();
          }
          this.allchecked = false;
        } else {
          this.$message({
            message: res.msg,
            type: "warning",
          });
        }
      });
      }
      }
      else{
        if (this.groupType == "all") {
        this.list.forEach((d) => {
          if (d.isselect == true) {
            obj.affairId.push(d.idStr);
          }
        });
      } else {
        for (let i = 0; i < this.list.length; i++) {
          for (let d = 0; d < this.list[i].groupingData.length; d++) {
            if (this.list[i].groupingData[d].isselect == true) {
              obj.affairId.push(this.list[i].groupingData[d].idStr);
            }
          }
        }
      }
      obj.affairId = obj.affairId.toString();
      if (obj.affairId == "") {
        this.$message.warning("请选择数据");
        return false;
      }

      api.operemali(obj).then((res) => {
        if (res.code == "00010001") {
          this.$message({
            message: "操作成功",
            type: "success",
          });
          if (n == "cancelEncryption") {
            let obj = {
              type: "encryption",
              inBoxType: "",
              groupType: this.groupType,
              searchType: this.searchType,
              searchWord: this.searchWord,
              pageSize: this.pageSize,
              pageNo: this.pageNo,
            };
            api.emlist(obj).then((res) => {
              if (res.code == "00010001") {
                let arr = res.msg.data;
                arr.forEach((d) => {
                  d.isselect = false;
                  if (d.id == this.$store.state.emliaId) {
                    d.selec = true;
                    d.readFlag = true;
                  } else {
                    d.selec = false;
                  }
                });
                this.list = arr;
                this.total = parseInt(res.msg.total);
              }
              this.refnavs();
            });
          } else {
            this.refnavs();
            this.getlist();
          }
          this.allchecked = false;
        } else {
          this.$message({
            message: res.msg,
            type: "warning",
          });
        }
      });
      }

    },

    handleSizeChange(val) {
      this.pageSize = val;
      this.getlist();
    },
    handleCurrentChange(val) {
      this.pageNo = val;
      this.getlist();
    },

    /**
     * 鼠标右击打开菜单
     * @Date: 2022-06-09 10:28:30
     * @param {Object} data: 当前右键的对象
     * @param {Object} event: 当前元素的event对象
     */
    openListMenu(data, event) {
      // 鼠标位置
      const { x, y } = event;
      this.menuPosition = {
        mouse_x: x,
        mouse_y: y,
      };
      this.currentListMenuData = data;
      this.isContextListMenuShow = true;
    },
    /**
     * 关闭菜单
     * @Date: 2022-06-09 10:55:58
     */
    closeContextListShow() {
      this.isContextListMenuShow = false;
    },
  },
};
</script>

<style lang="less" scoped>
/deep/.searchBox [data-v-79af5b26] .el-input--suffix .el-input__inner{
  text-indent: 10px;
}
/deep/.searchBox .el-select__tags input[type="text"]{
  border: none;
}
/deep/.searchBox .el-input--suffix .el-input__inner{
  border-radius: 20px;
}
/deep/.el-dialog__body .el-input--suffix .el-input__inner{
  padding-right:0px
}
/deep/.el-select .el-input__inner{
  cursor: text;
}
.searchBox .el-select{
  width: 100%;
}
.searchBox{
    width: 300px !important;
    position:relative;
    display:inline-block;
    width: max-content;
    margin: 0 auto;
    margin-left: 5px;
}
.searchIcon{
    position:absolute;
    top: 50%;
    transform: translateY(-50%);
    right: -12px;
    z-index:999;
}
/deep/.btnClass[data-v-79af5b26] .el-input__inner {
    border-top-left-radius: 14px !important;
    border-bottom-left-radius: 14px !important;
}
.el-pagination {
  position: relative !important;
  bottom: 5px !important;
}

.dian {
  height: 10px;
  width: 10px;
  border-radius: 50%;
  background: #c8423c;
  float: right;
  margin-top: 1.5em;
}

.peocard {
  padding: 10px;
  display: flex;
  align-items: center;
  position: relative;
}

.iconfont {
  color: #898989;
}

.tip {
  padding: 10px 0 0 10px;
  border: 1px solid #e6e9ec;
  border-top: 0;
  border-left: 0;
  /* height: 40px; */
}

.more {
  display: block;
  /* margin: 4px 0 0 156px; */
  padding: 10px 0;
  box-sizing: border-box;
  margin-left: -4px;
  padding-right: 8px;
  text-align: right;
}

.tabbbox /deep/ .tabhead {
  background: #ececec;
}

.imgtx {
  height: 38px;
  width: 38px;

  margin: 0 0.5em;
}
.hhh{
   width: 110% !important;
 }
.isclick {
  padding-left: 6px;
  background: #f7f8f9;
  border-left: 4px solid #c8423c;
}

.peolist /deep/ .el-pager .active {
  color: #c8423c !important;
}

.search /deep/ .searsle .el-input__inner {
}

.btnClass /deep/ .el-input__inner {
  /* border-top-left-radius: 0px !important;
  border-bottom-left-radius: 0px !important;
  border-left: none; */
  text-indent: 20px;
}
.ddts{
  margin-left: 10px;
}
.ddts /deep/ .el-input--mini .el-input__inner {
  border-top-right-radius: 0px !important;
  border-bottom-right-radius: 0px !important;
}

.ddts /deep/ .el-select .el-input__inner:focus {
  border-color: #409eff;
  border-right: 1px solid #dbdee5;
}

.ddts /deep/ .el-select .el-input.is-focus .el-input__inner {
  border-color: #409eff;
  border-right: 1px solid #dbdee5;
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

.btnClass /deep/ .el-input-group .el-input-group__append,
.el-input-group__prepend {
  background-color: white;
  color: #909399;
  vertical-align: middle;
  display: table-cell;
  position: relative;
  border: 1px solid #dcdfe6;
  border-radius: 0px 14px 14px 0px;
  /* padding: 0 0px;
            width: 1px; */
  white-space: nowrap;
  border-left: 0px solid black;
  padding-right: 10px;
}
</style>
