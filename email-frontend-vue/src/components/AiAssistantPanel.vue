<template>
  <div v-show="visible" class="ai-panel">
    <!-- 头部 -->
    <div class="ai-panel-header">
      <div class="ai-panel-title">
        <span class="ai-panel-icon ai-chip-icon-sm"><span class="ai-chip-core-sm"></span></span>
        <span>AI 助手</span>
      </div>
      <i class="el-icon-close ai-panel-close" @click="$emit('close')"></i>
    </div>

    <!-- 快捷操作 -->

    <!-- 2026-06-01 AI助手 start: 按场景拆分按钮，loading期间加disabled态 -->
    <div class="ai-panel-actions">
      <!-- 详情页按钮：总结、待办、回复、润色 -->
      <template v-if="scene === 'detail'">
        <div class="ai-action-btn" :class="{ active: currentAction === 'SUMMARIZE_EMAIL', disabled: streaming }" @click="doAction('SUMMARIZE_EMAIL')">
          <i class="el-icon-document"></i> 总结
        </div>
        <div class="ai-action-btn" :class="{ active: currentAction === 'EXTRACT_TODO', disabled: streaming }" @click="doAction('EXTRACT_TODO')">
          <i class="el-icon-s-check"></i> 待办
        </div>
        <div class="ai-action-btn" :class="{ active: currentAction === 'AUTO_REPLY', disabled: streaming }" @click="doAction('AUTO_REPLY')">
          <i class="el-icon-chat-line-round"></i> 回复
        </div>
        <div class="ai-action-btn" :class="{ active: currentAction === 'POLISH_EMAIL', disabled: streaming }" @click="doAction('POLISH_EMAIL')">
          <i class="el-icon-edit"></i> 润色
        </div>
      </template>
      <!-- 编辑页按钮：润色、语气、扩写、缩写 -->
      <template v-else>
        <div class="ai-action-btn" :class="{ active: currentAction === 'POLISH_EMAIL', disabled: streaming }" @click="doAction('POLISH_EMAIL')">
          <i class="el-icon-edit"></i> 润色
        </div>
        <div class="ai-action-btn" :class="{ disabled: streaming }" @click="showToneMenu = !showToneMenu">
          <i class="el-icon-s-opportunity"></i> 语气
        </div>
        <div class="ai-action-btn" :class="{ active: currentAction === 'EXPAND_CONTENT', disabled: streaming }" @click="doAction('EXPAND_CONTENT')">
          <i class="el-icon-zoom-in"></i> 扩写
        </div>
        <div class="ai-action-btn" :class="{ active: currentAction === 'SHRINK_CONTENT', disabled: streaming }" @click="doAction('SHRINK_CONTENT')">
          <i class="el-icon-zoom-out"></i> 缩写
        </div>
      </template>
    </div>
    <!-- 2026-06-01 AI助手 end -->

    <!-- 语气子菜单 -->
    <div v-if="showToneMenu" class="ai-tone-menu">
      <span class="ai-tone-item" @click="doTone('formal')">正式</span>
      <span class="ai-tone-item" @click="doTone('friendly')">友好</span>
      <span class="ai-tone-item" @click="doTone('concise')">简洁</span>
      <span class="ai-tone-item" @click="doTone('polite')">礼貌</span>
    </div>

    <!-- 内容区域 -->
    <div class="ai-panel-body" ref="panelBody">
      <!-- 欢迎状态 -->
      <div v-if="messages.length === 0 && !streaming && !resultContent" class="ai-panel-welcome">
        <div class="ai-welcome-icon">&#x1F916;</div>
        <div class="ai-welcome-title">你好，我是AI助手</div>
        <div class="ai-welcome-desc">点击上方快捷按钮，或在下方输入你的需求</div>
        <!-- 2026-06-01 AI助手 start: 欢迎提示按场景区分 -->
        <div class="ai-welcome-tips" v-if="scene === 'detail'">
          <div class="ai-tip-item" @click="sendQuickChat('帮我总结这封邮件的要点')">"帮我总结这封邮件的要点"</div>
          <div class="ai-tip-item" @click="sendQuickChat('提取邮件中的待办事项')">"提取邮件中的待办事项"</div>
        </div>
        <div class="ai-welcome-tips" v-else>
          <div class="ai-tip-item" @click="sendQuickChat('帮我把这段话写得更正式一些')">"帮我把这段话写得更正式一些"</div>
          <div class="ai-tip-item" @click="sendQuickChat('帮我扩写邮件内容')">"帮我扩写邮件内容"</div>
        </div>
        <!-- 2026-06-01 AI助手 end -->
      </div>

      <!-- 消息列表 -->
      <div v-for="(msg, idx) in messages" :key="idx" class="ai-msg">
        <div v-if="msg.role === 'user'" class="ai-msg-user">
          <div class="ai-msg-user-bubble">{{ msg.content }}</div>
        </div>
        <div v-else class="ai-msg-assistant">
          <!-- 思考过程（可折叠） -->
          <div v-if="msg.thinking" class="ai-thinking-block" :class="{ expanded: msg.thinkingExpanded }">
            <div class="ai-thinking-header" @click="toggleThinking(idx)">
              <i :class="msg.thinkingExpanded ? 'el-icon-arrow-down' : 'el-icon-arrow-right'"></i>
              <span>思考过程</span>
              <span class="ai-thinking-badge" v-if="!msg.thinkingExpanded">{{ msg.thinking.length }}字</span>
            </div>
            <div v-show="msg.thinkingExpanded" class="ai-thinking-content">{{ msg.thinking }}</div>
          </div>
          <!-- 结果内容 -->
          <!-- 2026-06-01 AI助手 start: 长内容默认折叠，超过500字显示展开按钮 -->
          <div class="ai-msg-result" v-html="getDisplayContent(msg)"></div>
          <div v-if="msg.content && msg.content.length > 500 && !msg.showFull" class="ai-expand-bar">
            <span class="ai-expand-link" @click="toggleExpand(msg)">展开全部 ↓（共{{ msg.content.length }}字）</span>
          </div>
          <div v-if="msg.showFull && msg.content && msg.content.length > 500" class="ai-expand-bar">
            <span class="ai-expand-link" @click="toggleExpand(msg)">收起 ↑</span>
          </div>
          <!-- 2026-06-01 AI助手 end -->
          <!-- 操作按钮 -->
          <div v-if="msg.done" class="ai-msg-actions">
            <span class="ai-action-link" @click="copyText(msg.content)"><i class="el-icon-document-copy"></i> 复制</span>
            <!-- 2026-06-01 AI助手: 编辑页显示插入，详情页不显示 -->
            <span class="ai-action-link" v-if="scene === 'edit'" @click="$emit('insert', msg.content)"><i class="el-icon-edit-outline"></i> 插入</span>
            <span class="ai-action-link" v-if="scene === 'detail' && currentAction === 'AUTO_REPLY'" @click="$emit('fill-reply', msg.content)"><i class="el-icon-chat-dot-round"></i> 填入回复</span>
            <span class="ai-action-link" @click="regenerate(idx)"><i class="el-icon-refresh"></i> 重新生成</span>
          </div>
        </div>
      </div>

      <!-- 当前流式输出 -->
      <div v-if="streaming || streamThinking" class="ai-msg">
        <div class="ai-msg-assistant">
          <!-- 流式思考 -->
          <div v-if="streamThinking" class="ai-thinking-block expanded">
            <div class="ai-thinking-header">
              <i class="el-icon-arrow-down"></i>
              <span>思考中</span>
              <span class="ai-thinking-dot">
                <span class="ai-dot-anim"></span>
              </span>
            </div>
            <div v-show="true" class="ai-thinking-content">
              {{ streamThinking }}<span class="ai-cursor">|</span>
            </div>
          </div>
          <!-- 流式结果 -->
          <div v-if="resultContent" class="ai-msg-result">
            <span v-html="formatContent(resultContent)"></span><span class="ai-cursor">|</span>
          </div>
          <!-- 加载中占位 -->
          <div v-if="!streamThinking && !resultContent" class="ai-loading-state">
            <span class="ai-dot-anim"></span> 正在思考...
          </div>
        </div>
      </div>

      <!-- 停止生成按钮 -->
      <div v-if="streaming" class="ai-stop-bar">
        <div class="ai-stop-btn" @click="cancelStream">
          <i class="el-icon-video-pause"></i> 停止生成
        </div>
      </div>
    </div>

    <!-- 底部输入 -->
    <div class="ai-panel-footer">
      <div class="ai-input-wrap">
        <textarea
          ref="chatInput"
          v-model="inputText"
          class="ai-chat-input"
          placeholder="输入你的需求..."
          rows="2"
          @keydown.enter.exact.prevent="sendChat"
        ></textarea>
        <div class="ai-send-btn" :class="{ disabled: !inputText.trim() || streaming }" @click="sendChat">
          <i class="el-icon-s-promotion"></i>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import streamRequest from '../api/streamRequest'

export default {
  name: 'AiAssistantPanel',
  props: {
    visible: { type: Boolean, default: false },
    scene: { type: String, default: 'detail' },
    emailSubject: { type: String, default: '' },
    emailContent: { type: String, default: '' },
    editorContent: { type: String, default: '' },
    selectedText: { type: String, default: '' },
    // 2026-06-01 AI助手: 原始发件人名字，帮助AI知道该称呼谁
    originalSender: { type: String, default: '' },
    // 2026-06-01 AI助手: 函数类型prop，实时获取编辑器内容
    getEmailContent: { type: Function, default: null },
    getEditorContent: { type: Function, default: null },
    getSelectedTextFn: { type: Function, default: null },
    // 2026-06-01 AI助手: 获取原始邮件正文（蓝线下方的原文，用于智能回复场景）
    getOriginalEmailText: { type: Function, default: null }
  },
  data: function () {
    return {
      inputText: '',
      messages: [],
      streaming: false,
      streamThinking: '',
      resultContent: '',
      currentAction: '',
      showToneMenu: false,
      currentStreamRequest: null,
      lastActionParams: null,
      // 2026-06-01 AI助手: 标记当前会话是否已发送过邮件上下文，避免每轮都带6000字全文
      contextSent: false
    }
  },
  // 2026-06-01 AI助手 start: watch监听——关闭面板取消流+切换邮件清空对话
  watch: {
    // 2026-06-01 AI助手: 面板关闭时取消正在进行的流式请求，防止后台请求堆积
    visible: function (newVal) {
      if (!newVal && this.streaming) {
        this.cancelStream()
      }
    },
    // 2026-06-01 AI助手: 切换邮件时清空对话历史和上下文，避免上下文错乱
    // 用emailSubject替代emailContent——详情页只传get-email-content函数不传email-content值
    // emailSubject在详情页和编辑页都绑定了，两个场景都能触发
    emailSubject: function (newVal, oldVal) {
      if (newVal !== oldVal && this.messages.length > 0) {
        this.clearChat()
      }
    }
  },
  // 2026-06-01 AI助手 end
  methods: {
    doAction: function (capability) {
      if (this.streaming) return
      this.showToneMenu = false
      this.currentAction = capability

      var labelMap = {
        'SUMMARIZE_EMAIL': '总结邮件',
        'EXTRACT_TODO': '提取待办',
        'AUTO_REPLY': '智能回复',
        'POLISH_EMAIL': '润色内容',
        'EXPAND_CONTENT': '扩写内容',
        'SHRINK_CONTENT': '精简缩写'
      }
      var label = labelMap[capability] || capability

      // 确定使用的文本内容
      var content = this.getRelevantContent(capability)
      if (!content) {
        this.$message.warning('请先选择或输入邮件内容')
        return
      }

      this.messages.push({ role: 'user', content: label })

      var params = {
        method: 'streamAgent',
        capability: capability,
        subject: this.emailSubject || '',
        content: content
      }
      // 2026-06-01 AI助手: 智能回复时传入原始发件人名字，帮助AI正确称呼
      if (capability === 'AUTO_REPLY' && this.originalSender) {
        params.replyTo = this.originalSender
      }
      this.lastActionParams = params
      this.startStream(params)
    },

    doTone: function (tone) {
      if (this.streaming) return
      this.showToneMenu = false
      this.currentAction = 'ADJUST_TONE'

      var toneMap = { formal: '正式', friendly: '友好', concise: '简洁', polite: '礼貌' }
      var label = '调整为' + (toneMap[tone] || tone) + '语气'

      // 2026-06-01 AI助手: 改用实时获取方法替代prop快照，解决UEditor内容获取为空的问题
      var content = this._resolveSelectedText() || this._resolveEditorContent()
      if (!content) {
        this.$message.warning('请先选择或输入需要调整的内容')
        return
      }

      this.messages.push({ role: 'user', content: label })

      var params = {
        method: 'streamAgent',
        capability: 'ADJUST_TONE',
        content: content,
        tone: tone
      }
      this.lastActionParams = params
      this.startStream(params)
    },

    // 2026-06-01 AI助手 start: 新增实时获取内容的方法，解决UEditor富文本编辑器非响应式导致的prop快照为空问题
    // 原理：Vue无法感知UEditor的DOM变化，传prop值是快照。改为传函数，在按钮点击时实时调用，拿到最新内容
    _resolveEmailContent: function () {
      if (this.getEmailContent) {
        return this.getEmailContent()
      }
      return this.emailContent
    },
    _resolveEditorContent: function () {
      if (this.getEditorContent) {
        return this.getEditorContent()
      }
      return this.editorContent
    },
    _resolveSelectedText: function () {
      if (this.getSelectedTextFn) {
        return this.getSelectedTextFn()
      }
      return this.selectedText
    },
    // 2026-06-01 AI助手: 获取蓝色虚线下方的原始邮件正文（用于智能回复）
    _resolveOriginalEmailText: function () {
      if (this.getOriginalEmailText) {
        return this.getOriginalEmailText()
      }
      return ''
    },
    // 2026-06-01 AI助手 end

    // 2026-06-01 AI助手: 按场景和能力精确区分内容来源
    // 润色/扩写/缩写/语气 → 只取用户草稿（蓝线上方），避免原始邮件模板混淆AI
    // 智能回复 → 取原始邮件正文（蓝线下方），需要理解对方说了什么
    // 总结/待办 → 详情页用邮件全文，编辑页无意义（已按场景隐藏按钮）
    getRelevantContent: function (capability) {
      if (this.scene === 'edit') {
        if (capability === 'AUTO_REPLY') {
          // 智能回复：需要理解原始邮件内容
          var originalText = this._resolveOriginalEmailText()
          if (originalText) return originalText
          // 兜底：用整个emailContent（已剥离模板的原文）
          return this.emailContent || ''
        }
        // 润色、扩写、缩写、语气：只用用户草稿（蓝线上方）
        if (capability === 'POLISH_EMAIL' || capability === 'EXPAND_CONTENT' || capability === 'SHRINK_CONTENT' || capability === 'ADJUST_TONE') {
          return this._resolveSelectedText() || this._resolveEditorContent() || ''
        }
        return this._resolveSelectedText() || this._resolveEditorContent() || ''
      }
      // 详情页：总结、待办用邮件全文
      return this._resolveEmailContent() || ''
    },

    sendChat: function () {
      if (this.streaming) return
      var text = this.inputText.trim()
      if (!text) return
      this.inputText = ''
      this.currentAction = 'CHAT'

      this.messages.push({ role: 'user', content: text })

      // 构建多轮对话消息
      var chatMessages = []
      for (var i = 0; i < this.messages.length; i++) {
        var msg = this.messages[i]
        if (msg.role === 'user') {
          chatMessages.push({ role: 'user', content: msg.content })
        } else if (msg.content) {
          chatMessages.push({ role: 'assistant', content: msg.content })
        }
      }

      // 2026-06-01 AI助手 start: 首轮带完整上下文给AI理解邮件，后续轮次只带主题节省token
      // contextSent标记防止每轮聊天都带6000字全文，clearChat时重置该标记
      var context = {
        scene: this.scene,
        emailSubject: this.emailSubject || '',
        originalSender: this.originalSender || ''
      }
      if (!this.contextSent) {
        context.emailContent = (this._resolveEmailContent() || '').substring(0, 3000)
        context.editorContent = (this._resolveEditorContent() || '').substring(0, 3000)
        context.selectedText = this._resolveSelectedText() || ''
        this.contextSent = true
      }
      // 2026-06-01 AI助手 end

      var params = {
        method: 'streamAgent',
        capability: 'CHAT',
        messages: JSON.stringify(chatMessages),
        context: JSON.stringify(context)
      }
      this.lastActionParams = params
      this.startStream(params)
    },

    sendQuickChat: function (text) {
      this.inputText = text
      this.sendChat()
    },

    // 2026-06-01 AI助手 start: startStream签名新增insertIndex和savedMsg参数+generating事件+原位插入+错误恢复+toast
    // insertIndex: 指定消息插入位置，regenerate时在原始位置替换而非追加末尾
    // savedMsg: 备份被替换的消息，请求失败时恢复，防止丢失数据
    // generating事件: 通知父组件更新悬浮按钮呼吸灯状态
    startStream: function (params, insertIndex, savedMsg) {
      var self = this
      self.streaming = true
      self.streamThinking = ''
      self.resultContent = ''
      self.scrollToBottom(true)
      self.$emit('generating', true)

      var pendingThinking = ''
      var pendingResult = ''

      self.currentStreamRequest = streamRequest(params, {
        onThinking: function (chunk) {
          pendingThinking += chunk
          self.streamThinking = pendingThinking
          self.scrollToBottom()
        },
        onResult: function (chunk) {
          pendingResult += chunk
          self.resultContent = pendingResult
          self.scrollToBottom()
        },
        onDone: function () {
          self.streaming = false
          self.currentStreamRequest = null
          self.$emit('generating', false)

          var msg = {
            role: 'assistant',
            content: pendingResult || pendingThinking,
            thinking: pendingThinking,
            thinkingExpanded: false,
            showFull: false,
            done: true,
            params: params
          }
          if (insertIndex !== undefined && insertIndex !== null) {
            self.messages.splice(insertIndex, 0, msg)
          } else {
            self.messages.push(msg)
          }
          self.streamThinking = ''
          self.resultContent = ''
          self.scrollToBottom(true)
        },
        onError: function (errMsg) {
          self.streaming = false
          self.currentStreamRequest = null
          self.$emit('generating', false)
          self.streamThinking = ''
          self.resultContent = ''
          if (savedMsg) {
            self.messages.splice(insertIndex, 0, savedMsg)
          }
          self.$message.error('AI请求失败：' + errMsg)
          self.messages.push({
            role: 'assistant',
            content: '出错了：' + errMsg + '，可点击右上角重新生成',
            done: true
          })
        }
      })
    },
    // 2026-06-01 AI助手 end

    cancelStream: function () {
      if (this.currentStreamRequest) {
        this.currentStreamRequest.abort()
        this.currentStreamRequest = null
      }
      this.streaming = false
      // 2026-06-01 AI助手: 通知父组件生成结束，关闭呼吸灯
      this.$emit('generating', false)

      // 保留已收到的内容
      if (this.resultContent || this.streamThinking) {
        this.messages.push({
          role: 'assistant',
          content: this.resultContent || '(已取消)',
          thinking: this.streamThinking,
          thinkingExpanded: false,
          done: true
        })
      }
      this.streamThinking = ''
      this.resultContent = ''
    },

    regenerate: function (idx) {
      if (this.streaming) return
      var msg = this.messages[idx]
      if (!msg || !msg.params) return
      // 2026-06-01 AI助手 start: 备份旧结果+传入insertIndex实现原位替换+失败时恢复
      // 之前splice删除后push到末尾，多轮对话中重新生成第一轮结果会跑到最下面
      // 现在传入idx让startStream在删除位置splice插入，失败时savedMsg恢复原结果
      var savedMsg = this.messages[idx]
      this.messages.splice(idx, 1)
      this.startStream(msg.params, idx, savedMsg)
      // 2026-06-01 AI助手 end
    },

    toggleThinking: function (idx) {
      var msg = this.messages[idx]
      if (msg) {
        this.$set(msg, 'thinkingExpanded', !msg.thinkingExpanded)
      }
    },

    // 2026-06-01 AI助手 start: 增强版markdown渲染，支持编号列表、无序列表、分隔线、引用、行内代码
    formatContent: function (text) {
      if (!text) return ''
      var escaped = text
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')

      // 逐行处理，识别块级结构
      var lines = escaped.split('\n')
      var result = ''
      var inOrderedList = false
      var inUnorderedList = false
      var inBlockquote = false

      for (var i = 0; i < lines.length; i++) {
        var line = lines[i]
        var trimmed = line.trim()

        // 检测水平分隔线 --- 或 ***
        if (/^-{3,}$/.test(trimmed) || /^\*{3,}$/.test(trimmed)) {
          if (inOrderedList) { result += '</ol>'; inOrderedList = false }
          if (inUnorderedList) { result += '</ul>'; inUnorderedList = false }
          if (inBlockquote) { result += '</blockquote>'; inBlockquote = false }
          result += '<hr class="ai-hr">'
          continue
        }

        // 检测引用 >
        if (/^&gt;\s?/.test(trimmed)) {
          if (inOrderedList) { result += '</ol>'; inOrderedList = false }
          if (inUnorderedList) { result += '</ul>'; inUnorderedList = false }
          if (!inBlockquote) { result += '<blockquote class="ai-bq">'; inBlockquote = true }
          var quoteContent = trimmed.replace(/^&gt;\s?/, '')
          result += this._formatInline(quoteContent) + '<br>'
          continue
        }
        if (inBlockquote) { result += '</blockquote>'; inBlockquote = false }

        // 检测编号列表 1. 或 1)
        var olMatch = trimmed.match(/^(\d+)[.\)]\s*(.+)/)
        if (olMatch) {
          if (!inOrderedList) {
            if (inUnorderedList) { result += '</ul>'; inUnorderedList = false }
            result += '<ol class="ai-ol">'
            inOrderedList = true
          }
          result += '<li>' + this._formatInline(olMatch[2]) + '</li>'
          continue
        }
        if (inOrderedList) { result += '</ol>'; inOrderedList = false }

        // 检测无序列表 - 或 *
        var ulMatch = trimmed.match(/^[*-]\s+(.+)/)
        if (ulMatch) {
          if (!inUnorderedList) {
            if (inOrderedList) { result += '</ol>'; inOrderedList = false }
            result += '<ul class="ai-ul">'
            inUnorderedList = true
          }
          result += '<li>' + this._formatInline(ulMatch[1]) + '</li>'
          continue
        }
        if (inUnorderedList) { result += '</ul>'; inUnorderedList = false }

        // 空行：关闭所有块
        if (trimmed === '') {
          result += '<br>'
          continue
        }

        // 普通行
        result += this._formatInline(trimmed) + '<br>'
      }

      // 关闭未闭合的标签
      if (inOrderedList) { result += '</ol>' }
      if (inUnorderedList) { result += '</ul>' }
      if (inBlockquote) { result += '</blockquote>' }

      return result
    },
    // 行内格式化：粗体、代码
    _formatInline: function (text) {
      if (!text) return ''
      // 行内代码 `code`
      text = text.replace(/`([^`]+)`/g, '<code class="ai-code">$1</code>')
      // 粗体 **text**
      text = text.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
      return text
    },
    // 2026-06-01 AI助手 end

    // 2026-06-01 AI助手: 获取显示内容，支持长内容折叠
    getDisplayContent: function (msg) {
      if (!msg.content) return ''
      if (msg.showFull || msg.content.length <= 500) {
        return this.formatContent(msg.content)
      }
      // 截取前500字符的纯文本展示
      var plainText = msg.content.substring(0, 500)
      return this.formatContent(plainText) + '...'
    },
    toggleExpand: function (msg) {
      this.$set(msg, 'showFull', !msg.showFull)
    },

    copyText: function (text) {
      var self = this
      if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(text).then(function () {
          self.$message.success('已复制')
        })
      } else {
        var textarea = document.createElement('textarea')
        textarea.value = text
        textarea.style.position = 'fixed'
        textarea.style.opacity = '0'
        document.body.appendChild(textarea)
        textarea.select()
        document.execCommand('copy')
        document.body.removeChild(textarea)
        self.$message.success('已复制')
      }
    },

    // 2026-06-01 AI助手 start: 自动滚动优化——用户向上翻看历史时不强制拽回底部
    // force=true（新对话/完成时）强制滚底；否则只在用户本来接近底部80px内才自动滚
    scrollToBottom: function (force) {
      var self = this
      this.$nextTick(function () {
        var body = self.$refs.panelBody
        if (!body) return
        var atBottom = body.scrollTop + body.clientHeight >= body.scrollHeight - 80
        if (force || atBottom) {
          body.scrollTop = body.scrollHeight
        }
      })
    },
    // 2026-06-01 AI助手 end

    focusInput: function () {
      var self = this
      this.$nextTick(function () {
        if (self.$refs.chatInput) {
          self.$refs.chatInput.focus()
        }
      })
    },

    clearChat: function () {
      if (this.streaming) {
        this.cancelStream()
      }
      this.messages = []
      this.currentAction = ''
      this.contextSent = false
      // 2026-06-01 AI助手 start: 重置语气菜单状态，避免切换邮件后菜单残留
      this.showToneMenu = false
      // 2026-06-01 AI助手 end
    }
  }
}
</script>

<style scoped>
.ai-panel {
  width: 360px;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #f8f9fb;
  border-left: 1px solid #e4e7ed;
  font-size: 13px;
  color: #333;
}

/* 头部 */
.ai-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}
.ai-panel-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.ai-panel-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}
.ai-chip-icon-sm {
  width: 18px;
  height: 18px;
  border: 1.5px solid #667eea;
  border-radius: 3px;
  position: relative;
}
.ai-chip-icon-sm::before,
.ai-chip-icon-sm::after {
  content: '';
  position: absolute;
  background: #667eea;
}
.ai-chip-icon-sm::before {
  width: 8px;
  height: 1.5px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.ai-chip-icon-sm::after {
  width: 1.5px;
  height: 8px;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}
.ai-chip-core-sm {
  width: 5px;
  height: 5px;
  background: #667eea;
  border-radius: 1px;
  position: relative;
  z-index: 1;
  display: block;
  margin: auto;
}
.ai-panel-close {
  cursor: pointer;
  font-size: 16px;
  color: #909399;
  transition: color 0.2s;
}
.ai-panel-close:hover {
  color: #409eff;
}

/* 快捷操作 */
.ai-panel-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px 12px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}
.ai-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  padding: 4px 10px;
  border-radius: 14px;
  background: #f4f4f5;
  color: #606266;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.ai-action-btn:hover {
  background: #ecf5ff;
  color: #409eff;
}
.ai-action-btn.active {
  background: #409eff;
  color: #fff;
}
/* 2026-06-01 AI助手: loading期间按钮disabled态 */
.ai-action-btn.disabled {
  opacity: 0.45;
  cursor: not-allowed;
  pointer-events: none;
}
.ai-action-btn i {
  font-size: 13px;
}

/* 语气子菜单 */
.ai-tone-menu {
  display: flex;
  gap: 8px;
  padding: 6px 12px;
  background: #fafafa;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}
.ai-tone-item {
  padding: 3px 12px;
  border-radius: 12px;
  background: #fff;
  border: 1px solid #dcdfe6;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.ai-tone-item:hover {
  border-color: #409eff;
  color: #409eff;
}

/* 内容区域 */
.ai-panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

/* 欢迎状态 */
.ai-panel-welcome {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
}
.ai-welcome-icon {
  font-size: 48px;
  margin-bottom: 12px;
}
.ai-welcome-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}
.ai-welcome-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 20px;
}
.ai-welcome-tips {
  width: 100%;
}
.ai-tip-item {
  padding: 8px 12px;
  margin-bottom: 6px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  font-size: 12px;
  color: #606266;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s;
}
.ai-tip-item:hover {
  border-color: #409eff;
  color: #409eff;
  background: #ecf5ff;
}

/* 用户消息 */
.ai-msg {
  margin-bottom: 12px;
}
.ai-msg-user {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 8px;
}
.ai-msg-user-bubble {
  max-width: 80%;
  padding: 8px 12px;
  background: #409eff;
  color: #fff;
  border-radius: 12px 12px 2px 12px;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}

/* AI消息 */
.ai-msg-assistant {
  background: #fff;
  border-radius: 2px 12px 12px 12px;
  padding: 10px 12px;
  border: 1px solid #ebeef5;
  word-break: break-word;
}

/* 思考过程（DeepSeek风格折叠） */
.ai-thinking-block {
  margin-bottom: 8px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f7fa;
}
.ai-thinking-header {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 8px;
  font-size: 12px;
  color: #909399;
  cursor: pointer;
  user-select: none;
}
.ai-thinking-header:hover {
  color: #606266;
}
.ai-thinking-header i {
  font-size: 12px;
  width: 14px;
}
.ai-thinking-badge {
  margin-left: auto;
  font-size: 11px;
  color: #c0c4cc;
}
.ai-thinking-content {
  padding: 4px 8px 8px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
  white-space: pre-wrap;
  max-height: 200px;
  overflow-y: auto;
}

/* 结果内容 */
.ai-msg-result {
  font-size: 13px;
  line-height: 1.7;
  color: #303133;
}

/* 操作按钮 */
.ai-msg-actions {
  display: flex;
  gap: 10px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
  flex-wrap: wrap;
}
.ai-action-link {
  font-size: 12px;
  color: #909399;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 2px;
  transition: color 0.2s;
}
.ai-action-link:hover {
  color: #409eff;
}
.ai-action-link i {
  font-size: 13px;
}

/* 流式光标 */
.ai-cursor {
  display: inline-block;
  color: #409eff;
  animation: ai-blink 0.8s infinite;
  font-weight: bold;
}
@keyframes ai-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* 加载动画点 */
.ai-dot-anim {
  display: inline-block;
  width: 6px;
  height: 6px;
  background: #409eff;
  border-radius: 50%;
  animation: ai-pulse 1.2s infinite;
}
@keyframes ai-pulse {
  0%, 100% { opacity: 0.3; transform: scale(0.8); }
  50% { opacity: 1; transform: scale(1.2); }
}
.ai-thinking-dot {
  display: inline-flex;
  margin-left: 4px;
}

/* 加载状态 */
.ai-loading-state {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 0;
  font-size: 13px;
  color: #909399;
}

/* 停止按钮 */
.ai-stop-bar {
  display: flex;
  justify-content: center;
  padding: 6px 0;
}
.ai-stop-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 16px;
  border-radius: 14px;
  background: #fff;
  border: 1px solid #dcdfe6;
  color: #909399;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s;
}
.ai-stop-btn:hover {
  border-color: #f56c6c;
  color: #f56c6c;
}

/* 底部输入 */
.ai-panel-footer {
  padding: 10px 12px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
}
.ai-input-wrap {
  display: flex;
  align-items: flex-end;
  gap: 8px;
}
.ai-chat-input {
  flex: 1;
  resize: none;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 8px 10px;
  font-size: 13px;
  line-height: 1.4;
  outline: none;
  font-family: inherit;
  transition: border-color 0.2s;
}
.ai-chat-input:focus {
  border-color: #409eff;
}
.ai-send-btn {
  width: 34px;
  height: 34px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: #409eff;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  flex-shrink: 0;
  transition: all 0.2s;
}
.ai-send-btn:hover {
  background: #66b1ff;
}
.ai-send-btn.disabled {
  background: #c0c4cc;
  cursor: not-allowed;
}

/* 2026-06-01 AI助手 start: 新增样式——长内容折叠、markdown块级渲染 */
/* 折叠/展开 */
.ai-expand-bar {
  text-align: center;
  margin-top: 4px;
}
.ai-expand-link {
  font-size: 12px;
  color: #409eff;
  cursor: pointer;
}
.ai-expand-link:hover {
  text-decoration: underline;
}

/* markdown渲染：编号列表 */
.ai-msg-result .ai-ol,
.ai-msg-result .ai-ul {
  margin: 4px 0;
  padding-left: 20px;
}
.ai-msg-result .ai-ol li,
.ai-msg-result .ai-ul li {
  margin-bottom: 2px;
}

/* markdown渲染：分隔线 */
.ai-msg-result .ai-hr {
  border: none;
  border-top: 1px dashed #dcdfe6;
  margin: 8px 0;
}

/* markdown渲染：引用 */
.ai-msg-result .ai-bq {
  margin: 4px 0;
  padding: 4px 10px;
  border-left: 3px solid #dcdfe6;
  color: #909399;
  background: #fafafa;
}

/* markdown渲染：行内代码 */
.ai-msg-result .ai-code {
  background: #f0f2f5;
  padding: 1px 4px;
  border-radius: 3px;
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
}
/* 2026-06-01 AI助手 end */
</style>
