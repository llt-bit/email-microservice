package com.email.controller;

import com.alibaba.fastjson.JSON;
import com.email.service.AiService;
import com.email.service.AiService.AiResponse;
import com.email.service.AiService.Capability;
import com.email.service.AiService.StreamCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 助手 SSE 流式 API（从 OA LingmaAgentController 迁移）。
 *
 * <p>统一 SSE 入口，通过 capability 参数区分功能类型。
 * 原路径: /seeyon/apps/internalmail/lingmaAgent.do → /api/ai/agent</p>
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource private AiService aiService;

    @RequestMapping(value = "/agent", produces = MediaType.TEXT_EVENT_STREAM_VALUE,
                    method = {RequestMethod.GET, RequestMethod.POST})
    public SseEmitter agent(@RequestParam String capability,
                             @RequestParam(required = false) String subject,
                             @RequestParam(required = false) String content,
                             @RequestParam(required = false) String replyTo,
                             @RequestParam(required = false) String tone,
                             @RequestParam(required = false) String prompt,
                             @RequestParam(required = false) String targetLanguage,
                             @RequestParam(required = false) String messages,
                             @RequestParam(required = false) String context) {

        SseEmitter emitter = new SseEmitter(120000L); // 2分钟超时

        StreamCallback cb = new StreamCallback() {
            @Override
            public void onThinking(String chunk) {
                send(emitter, "thinking", chunk, null);
            }
            @Override
            public void onResult(String chunk) {
                send(emitter, "result", chunk, null);
            }
            @Override
            public void onError(String message) {
                send(emitter, "error", null, message);
                emitter.complete();
            }
            @Override
            public void onComplete(AiResponse resp) {
                Map<String, String> extra = new HashMap<>();
                extra.put("thinking", resp.getThinking());
                extra.put("result", resp.getResult());
                send(emitter, "done", null, null, extra);
                emitter.complete();
            }
        };

        // 异步执行避免阻塞 Tomcat 线程
        new Thread(() -> {
            try {
                String cap = capability.toUpperCase();

                if ("CHAT".equals(cap)) {
                    if (messages == null || messages.trim().isEmpty()) {
                        cb.onError("对话消息不能为空"); return;
                    }
                    @SuppressWarnings("unchecked")
                    List<Map<String, String>> msgs = JSON.parseObject(messages, List.class);
                    Map<String, Object> ctx = new HashMap<>();
                    if (context != null && !context.trim().isEmpty()) {
                        ctx = JSON.parseObject(context, Map.class);
                    }
                    aiService.chatStream(msgs, ctx, cb);
                } else {
                    Capability c = Capability.valueOf(cap);
                    Map<String, Object> ctx = new HashMap<>();
                    if (subject != null) ctx.put("subject", subject);
                    if (content != null) ctx.put("content", content);
                    if (replyTo != null) ctx.put("sender", replyTo);
                    if (tone != null) ctx.put("tone", tone);
                    if (prompt != null) ctx.put("requirement", prompt);
                    if (targetLanguage != null) ctx.put("targetLanguage", targetLanguage);
                    aiService.executeStream(c, ctx, cb);
                }
            } catch (IllegalArgumentException e) {
                cb.onError("不支持的capability: " + capability);
            } catch (Exception e) {
                log.error("AI接口异常", e);
                cb.onError("系统内部错误: " + e.getMessage());
            }
        }).start();

        return emitter;
    }

    private void send(SseEmitter emitter, String type, String content, String message) {
        send(emitter, type, content, message, null);
    }

    private void send(SseEmitter emitter, String type, String content, String message, Map<String, String> extra) {
        Map<String, Object> data = new HashMap<>();
        data.put("type", type);
        if (content != null) data.put("content", content);
        if (message != null) data.put("message", message);
        if (extra != null) data.putAll(extra);
        try {
            emitter.send(SseEmitter.event().data(JSON.toJSONString(data)));
        } catch (IOException e) {
            log.debug("SSE send failed: {}", e.getMessage());
        }
    }
}
