<template>
  <!-- ===== AI智能体新增：AI对话组件 START ===== -->
  <div class="ai-chat-dialog" v-show="visible">
    <div class="chat-header">
      <div class="chat-title">
        <i class="el-icon-chat-dot-round"></i>
        <span>AI 邮件助手</span>
        <el-tag size="mini" type="success" effect="plain" style="margin-left: 8px">新</el-tag>
      </div>
      <div class="chat-actions">
        <el-button type="text" size="mini" @click="clearChat" title="清空对话">
          <i class="el-icon-delete"></i>
        </el-button>
        <el-button type="text" size="mini" @click="$emit('close')" title="关闭">
          <i class="el-icon-close"></i>
        </el-button>
      </div>
    </div>

    <!-- 快捷指令 -->
    <div class="quick-actions">
      <el-button
        v-for="action in quickActions"
        :key="action.key"
        size="mini"
        plain
        round
        @click="handleQuickAction(action)"
        :loading="loading && currentAction === action.key"
      >
        {{ action.label }}
      </el-button>
    </div>

    <!-- 消息列表 -->
    <div class="chat-messages" ref="messageList">
      <div v-if="messages.length === 0" class="chat-welcome">
        <i class="el-icon-cpu welcome-icon"></i>
        <p class="welcome-title">我是您的 AI 邮件助手</p>
        <p class="welcome-desc">
          可以帮您总结邮件、起草回复、润色内容<br>
          支持连续对话，随时追问调整
        </p>
      </div>

      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message-row', msg.role]"
      >
        <div class="message-bubble">
          <!-- 流式思考过程 -->
          <div v-if="msg.thinking" class="thinking-section">
            <div class="thinking-title">
              思考过程
              <span v-if="msg.streamPhase === 'thinking'" class="chat-streaming-indicator">● 思考中</span>
            </div>
            <div class="thinking-content" v-html="formatContent(msg.thinking)"></div>
            <span class="chat-typing-cursor" v-if="msg.streamPhase === 'thinking'">|</span>
          </div>
          <div class="message-content" v-html="formatContent(msg.content)"></div>
          <span class="chat-typing-cursor" v-if="msg.streamPhase === 'result'">|</span>
          <span v-if="msg.streamPhase === 'result'" class="chat-streaming-indicator">● 生成中</span>
          <!-- 流式中显示停止按钮 -->
          <div v-if="msg.streaming && msg.streamPhase" class="stream-stop-actions">
            <el-button type="text" size="mini" @click="cancelStream">
              <i class="el-icon-video-pause"></i> 停止生成
            </el-button>
          </div>
          <!-- 完成后显示操作按钮 -->
          <div v-if="msg.role === 'assistant' && !msg.streaming" class="message-actions">
            <el-button type="text" size="mini" @click="copyContent(msg.content)">
              <i class="el-icon-document-copy"></i> 复制
            </el-button>
            <el-button type="text" size="mini" @click="$emit('insert', msg.content)">
              <i class="el-icon-download"></i> 插入
            </el-button>
            <el-button type="text" size="mini" @click="regenerate(index)">
              <i class="el-icon-refresh"></i> 重新生成
            </el-button>
          </div>
        </div>
      </div>

      <!-- 初始加载中（无流式内容时显示） -->
      <div v-if="loading && streamingMsgIndex < 0" class="message-row assistant">
        <div class="message-bubble loading-bubble">
          <i class="el-icon-loading"></i>
          <span>AI 正在思考中...</span>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="chat-input-area">
      <el-input
        v-model="inputText"
        type="textarea"
        :rows="2"
        placeholder="输入您的需求，例如：帮我写一封请假邮件..."
        resize="none"
        @keydown.enter.native.prevent="handleEnter"
      ></el-input>
      <div class="input-actions">
        <span class="input-tip">Enter 发送，Shift+Enter 换行</span>
        <el-button
          v-if="!loading"
          type="primary"
          size="mini"
          icon="el-icon-s-promotion"
          @click="sendMessage"
          :disabled="!inputText.trim()"
        >发送</el-button>
        <el-button
          v-if="loading"
          type="warning"
          size="mini"
          icon="el-icon-video-pause"
          @click="cancelStream"
        >停止生成</el-button>
      </div>
    </div>
  </div>
  <!-- ===== AI智能体新增：AI对话组件 END ===== -->
</template>

<!-- ===== AI智能体新增：script逻辑 START ===== -->
<script>
import streamRequest from "@/api/streamRequest";

export default {
  name: 'AiChatDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    context: {
      type: Object,
      default: () => ({})
    }
  },
  data() {
    return {
      messages: [],
      inputText: '',
      loading: false,
      currentAction: '',
      currentStreamRequest: null,
      streamingMsgIndex: -1,
      quickActions: [
        { key: 'summarize', label: '总结邮件', icon: '📋' },
        { key: 'reply', label: '起草回复', icon: '💬' },
        { key: 'todo', label: '提取待办', icon: '✅' },
        { key: 'polish', label: '润色内容', icon: '✨' }
      ]
    };
  },
  watch: {
    messages: {
      deep: true,
      handler: function() {
        this.$nextTick(function() {
          this.scrollToBottom();
        });
      }
    }
  },
  methods: {
    handleQuickAction(action) {
      var prompts = {
        summarize: '请总结这封邮件的核心内容',
        reply: '请帮我起草一封回复邮件',
        todo: '请提取邮件中的待办事项',
        polish: '请润色这段邮件内容'
      };
      this.inputText = prompts[action.key] || action.label;
      this.sendMessage(action.key);
    },

    sendMessage(actionKey) {
      var text = this.inputText.trim();
      if (!text || this.loading) return;

      // 取消之前的流式请求
      this.cancelStream();

      // 添加用户消息
      this.messages.push({ role: 'user', content: text });
      this.inputText = '';
      this.loading = true;
      this.currentAction = actionKey || '';

      // 先添加一个空的助手消息占位（流式填充）
      this.messages.push({
        role: 'assistant',
        content: '',
        thinking: '',
        streaming: true,
        streamPhase: '',
        type: 'text'
      });
      var assistantIdx = this.messages.length - 1;
      this.streamingMsgIndex = assistantIdx;

      var self = this;
      this.currentStreamRequest = streamRequest({
        method: 'streamAgent',
        capability: 'CHAT',
        messages: JSON.stringify(this.messages.filter(function(m) { return m.role === 'user' || (m.role === 'assistant' && !m.streaming); })),
        context: JSON.stringify(this.context)
      }, {
        onThinking: function(chunk) {
          if (self.streamingMsgIndex < 0) return;
          var msg = self.messages[self.streamingMsgIndex];
          self.$set(msg, 'streamPhase', 'thinking');
          self.$set(msg, 'thinking', (msg.thinking || '') + chunk);
        },
        onResult: function(chunk) {
          if (self.streamingMsgIndex < 0) return;
          var msg = self.messages[self.streamingMsgIndex];
          self.$set(msg, 'streamPhase', 'result');
          self.$set(msg, 'content', (msg.content || '') + chunk);
        },
        onDone: function(data) {
          self.loading = false;
          self.currentAction = '';
          self.currentStreamRequest = null;
          if (self.streamingMsgIndex >= 0) {
            var msg = self.messages[self.streamingMsgIndex];
            self.$set(msg, 'streaming', false);
            self.$set(msg, 'streamPhase', '');
            if (data.result) {
              self.$set(msg, 'content', data.result);
            }
            if (data.thinking) {
              self.$set(msg, 'thinking', data.thinking);
            }
          }
          self.streamingMsgIndex = -1;
        },
        onError: function(msg) {
          self.loading = false;
          self.currentAction = '';
          self.currentStreamRequest = null;
          if (self.streamingMsgIndex >= 0) {
            var err = self.messages[self.streamingMsgIndex];
            self.$set(err, 'streaming', false);
            self.$set(err, 'streamPhase', '');
            self.$set(err, 'content', msg || '抱歉，请求发生错误，请检查网络后重试。');
            self.$set(err, 'type', 'error');
          }
          self.streamingMsgIndex = -1;
        }
      });
    },

    // 取消流式请求
    cancelStream() {
      if (this.currentStreamRequest) {
        this.currentStreamRequest.abort();
        this.currentStreamRequest = null;
      }
      if (this.streamingMsgIndex >= 0 && this.messages[this.streamingMsgIndex]) {
        this.$set(this.messages[this.streamingMsgIndex], 'streaming', false);
        this.$set(this.messages[this.streamingMsgIndex], 'streamPhase', '');
      }
      this.streamingMsgIndex = -1;
      this.loading = false;
      this.currentAction = '';
    },

    handleEnter(e) {
      if (!e.shiftKey) {
        this.sendMessage();
      }
    },

    formatContent(content) {
      if (!content) return '';
      // 简单换行转 br，先处理转义的 \\n
      return content
        .replace(/\\n/g, '\n')
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\n/g, '<br>');
    },

    copyContent(content) {
      var textarea = document.createElement('textarea');
      textarea.value = content;
      document.body.appendChild(textarea);
      textarea.select();
      document.execCommand('copy');
      document.body.removeChild(textarea);
      this.$message.success('已复制到剪贴板');
    },

    regenerate(index) {
      // 取消当前流式请求
      this.cancelStream();

      // 找到对应的用户消息（往前找最近的一条 user）
      var userIndex = -1;
      for (var i = index - 1; i >= 0; i--) {
        if (this.messages[i].role === 'user') {
          userIndex = i;
          break;
        }
      }
      if (userIndex === -1) return;

      var userMsg = this.messages[userIndex];
      // 删除 assistant 消息及之后的所有消息
      this.messages.splice(userIndex + 1);
      this.inputText = userMsg.content;
      this.sendMessage();
    },

    clearChat() {
      this.cancelStream();
      this.$confirm('确定清空当前对话吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(function() {
        this.messages = [];
        this.$message.success('对话已清空');
      }.bind(this)).catch(function() {});
    },

    scrollToBottom() {
      var list = this.$refs.messageList;
      if (list) {
        list.scrollTop = list.scrollHeight;
      }
    }
  }
};
</script>
<!-- ===== AI智能体新增：script逻辑 END ===== -->

<!-- ===== AI智能体新增：样式定义 START ===== -->
<style scoped>
.ai-chat-dialog {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
  border-radius: 8px;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #ebeef5;
  flex-shrink: 0;
}

.chat-title {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.chat-title i {
  margin-right: 6px;
  color: #409eff;
  font-size: 16px;
}

.chat-actions {
  display: flex;
  gap: 4px;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  padding: 10px 12px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  background: #f5f7fa;
}

.chat-welcome {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.welcome-icon {
  font-size: 48px;
  color: #409eff;
  margin-bottom: 12px;
}

.welcome-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.welcome-desc {
  font-size: 13px;
  line-height: 1.6;
}

.message-row {
  display: flex;
  margin-bottom: 12px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 85%;
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 13px;
  line-height: 1.6;
  word-break: break-word;
}

.message-row.user .message-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-row.assistant .message-bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #e4e7ed;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.04);
}

.thinking-section {
  background: #f5f7fa;
  border-left: 3px solid #409eff;
  padding: 8px 12px;
  margin-bottom: 10px;
  border-radius: 4px;
}

.thinking-title {
  font-size: 12px;
  color: #409eff;
  font-weight: 600;
  margin-bottom: 4px;
}

.thinking-content {
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
}

.message-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  padding-top: 8px;
  border-top: 1px dashed #ebeef5;
}

.loading-bubble {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #909399;
}

.chat-input-area {
  padding: 10px 12px;
  border-top: 1px solid #ebeef5;
  background: #fff;
  flex-shrink: 0;
}

.input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 6px;
}

.input-tip {
  font-size: 12px;
  color: #c0c4cc;
}

.stream-stop-actions {
  margin-top: 6px;
  padding-top: 6px;
  border-top: 1px dashed #ebeef5;
}

.chat-typing-cursor {
  display: inline-block;
  color: #409eff;
  font-weight: bold;
  animation: chat-blink-cursor 0.8s infinite;
}

@keyframes chat-blink-cursor {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.chat-streaming-indicator {
  display: inline-block;
  font-size: 12px;
  color: #409eff;
  margin-left: 6px;
  animation: chat-stream-pulse 1.2s ease-in-out infinite;
}

@keyframes chat-stream-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.3; }
}
</style>
<!-- ===== AI智能体新增：样式定义 END ===== -->
