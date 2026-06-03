package com.email.service;

import java.util.List;
import java.util.Map;

/**
 * AI 助手服务接口（从 OA LingmaAgentService 迁移）。
 */
public interface AiService {

    /** AI 能力枚举 */
    enum Capability {
        AUTO_REPLY, SUMMARIZE_EMAIL, EXTRACT_TODO, WRITE_EMAIL,
        POLISH_EMAIL, TRANSLATE_EMAIL, ADJUST_TONE,
        EXPAND_CONTENT, SHRINK_CONTENT, CHAT
    }

    /** AI 响应结果 */
    class AiResponse {
        private String thinking;
        private String result;
        private String raw;
        public AiResponse(String thinking, String result, String raw) {
            this.thinking = thinking; this.result = result; this.raw = raw;
        }
        public String getThinking() { return thinking; }
        public String getResult() { return result; }
        public String getRaw() { return raw; }
    }

    /** 流式回调 */
    interface StreamCallback {
        void onThinking(String chunk);
        void onResult(String chunk);
        void onError(String message);
        void onComplete(AiResponse resp);
    }

    /** 执行流式 AI 调用 */
    void executeStream(Capability cap, Map<String, Object> context, StreamCallback callback);

    /** 多轮对话 */
    void chatStream(List<Map<String, String>> messages, Map<String, Object> context, StreamCallback callback);
}
