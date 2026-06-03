package com.email.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.email.exception.BusinessException;
import com.email.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Pattern;

/**
 * AI 助手服务实现（从 OA LingmaAgentService 完整迁移，962行 → 精简为 ~400行）。
 *
 * <p>核心能力：智能回复、邮件总结、待办提取、撰写/润色/翻译邮件、语气调整、扩缩写。
 * 通过阿里云 DashScope API (通义千问) 实现，支持 SSE 流式输出。</p>
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Value("${email.ai.api-url:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}")
    private String apiUrl;

    @Value("${email.ai.api-key:sk-11fa0019a3874de78354a1d85e0b78cb}")
    private String apiKey;

    @Value("${email.ai.model:qwen-plus}")
    private String model;

    @Value("${email.ai.timeout:60000}")
    private int timeout;

    // 信任所有 SSL（DashScope 兼容）
    static {
        try {
            TrustManager[] tm = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] c, String a) {}
                public void checkServerTrusted(X509Certificate[] c, String a) {}
            }};
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, tm, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((h, s) -> true);
        } catch (Exception e) {
            log.error("SSL init failed", e);
        }
    }

    // ===== 智能标签解析器（完整保留原逻辑）=====

    static class SmartParser {
        enum State { INIT, IN_THINKING, BETWEEN, IN_RESULT }
        private State state = State.INIT;
        private final StringBuilder buf = new StringBuilder();
        private final StringBuilder thinking = new StringBuilder();
        private final StringBuilder result = new StringBuilder();
        private final StringBuilder raw = new StringBuilder();
        private boolean inTag;
        private boolean hasReasoning;
        private static final int MAX_TAG = 12;

        void appendReasoning(String text, StreamCallback cb) {
            if (text == null || text.isEmpty()) return;
            hasReasoning = true;
            if (state == State.INIT) state = State.IN_THINKING;
            thinking.append(text);
            if (cb != null) cb.onThinking(text);
        }

        void feed(String chunk, StreamCallback cb) {
            if (chunk == null || chunk.isEmpty()) return;
            for (int i = 0; i < chunk.length(); i++) {
                char c = chunk.charAt(i);
                raw.append(c);
                if (inTag) {
                    buf.append(c);
                    String t = buf.toString();
                    if (t.equals("think>")) { state = State.IN_THINKING; inTag = false; buf.setLength(0); }
                    else if (t.equals("/think>")) { state = State.BETWEEN; inTag = false; buf.setLength(0); }
                    else if (t.equals("thinking>")) { state = State.IN_THINKING; inTag = false; buf.setLength(0); }
                    else if (t.equals("/thinking>")) { state = State.BETWEEN; inTag = false; buf.setLength(0); }
                    else if (t.equals("result>")) { state = State.IN_RESULT; inTag = false; buf.setLength(0); }
                    else if (t.equals("/result>")) { inTag = false; buf.setLength(0); }
                    else if (t.length() >= MAX_TAG) {
                        output("<\"" + buf.toString(), cb);
                        buf.setLength(0); inTag = false;
                    }
                } else if (c == '<') {
                    inTag = true; buf.setLength(0);
                } else {
                    output(String.valueOf(c), cb);
                }
            }
        }

        private void output(String text, StreamCallback cb) {
            switch (state) {
                case IN_THINKING: thinking.append(text); if (cb != null) cb.onThinking(text); break;
                case IN_RESULT:
                case BETWEEN: result.append(text); if (cb != null) cb.onResult(text); break;
                case INIT:
                    if (hasReasoning) { result.append(text); if (cb != null) cb.onResult(text); }
                    else { thinking.append(text); if (cb != null) cb.onThinking(text); }
                    break;
            }
        }

        void finish() {
            String r = result.toString()
                .replaceAll("(?is)<think>.*?</think>", "")
                .replaceAll("(?is)<thinking>.*?</thinking>", "")
                .replaceAll("(?i)</?(?:think|thinking|result)>", "").trim();
            result.setLength(0); result.append(r);
        }

        String getThinking() { return thinking.toString().trim(); }
        String getResult() { return result.toString().trim(); }
        String getRaw() { return raw.toString(); }
    }

    // ===== 流式 API 调用 =====

    private void doStreamCall(String requestBody, StreamCallback callback) {
        HttpURLConnection conn = null;
        try {
            URL u = new URL(apiUrl);
            conn = (HttpURLConnection) u.openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(timeout);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            if (apiKey != null && !apiKey.isEmpty()) {
                conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            }
            conn.setRequestProperty("Accept", "text/event-stream");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(requestBody.getBytes("UTF-8"));
                os.flush();
            }

            int code = conn.getResponseCode();
            if (code != 200) {
                StringBuilder err = new StringBuilder();
                try (BufferedReader r = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"))) {
                    String l; while ((l = r.readLine()) != null) err.append(l);
                }
                callback.onError("AI服务错误(" + code + "): " + err.substring(0, Math.min(200, err.length())));
                return;
            }

            SmartParser parser = new SmartParser();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = in.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring(6).trim();
                        if ("[DONE]".equals(data)) break;
                        try {
                            JSONObject json = JSON.parseObject(data);
                            JSONObject choices0 = json.getJSONArray("choices") != null
                                && !json.getJSONArray("choices").isEmpty()
                                ? json.getJSONArray("choices").getJSONObject(0) : null;
                            if (choices0 != null) {
                                JSONObject delta = choices0.getJSONObject("delta");
                                if (delta == null) delta = choices0.getJSONObject("message");
                                if (delta != null) {
                                    String reasoning = delta.getString("reasoning_content");
                                    if (reasoning != null && !reasoning.isEmpty()) parser.appendReasoning(reasoning, callback);
                                    String content = delta.getString("content");
                                    if (content != null && !content.isEmpty()) parser.feed(content, callback);
                                }
                            }
                        } catch (Exception pe) { log.debug("SSE parse: {}", pe.getMessage()); }
                    }
                }
            }

            parser.finish();
            String ft = parser.getThinking(), fr = parser.getResult();
            if (fr.isEmpty()) { fr = ft; ft = ""; }
            if (!fr.isEmpty()) fr = cleanOutput(fr);
            callback.onComplete(new AiResponse(ft, fr, parser.getRaw()));

        } catch (java.net.SocketTimeoutException e) {
            callback.onError("AI服务响应超时");
        } catch (Exception e) {
            log.error("AI调用异常", e);
            callback.onError("AI服务异常: " + e.getMessage());
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // ===== 公开方法 =====

    @Override
    public void executeStream(Capability cap, Map<String, Object> ctx, StreamCallback cb) {
        String prompt = buildPrompt(cap, ctx);
        if (prompt == null || prompt.isEmpty()) {
            cb.onError("构建提示词失败"); return;
        }
        int maxTokens;
        switch (cap) {
            case EXPAND_CONTENT: maxTokens = 8192; break;
            case AUTO_REPLY:
            case WRITE_EMAIL:    maxTokens = 4096; break;
            default:             maxTokens = 2048; break;
        }
        String body = "{" +
            "\"model\":\"" + model + "\"," +
            "\"messages\":[{\"role\":\"system\",\"content\":\"你是致远OA内部邮件系统的AI助手，为企业员工处理工作邮件。" +
            "回复时语气和篇幅匹配原邮件；总结时有几说几不凑数；提取待办只取真正行动项；润色时好的保留只改问题部分。所有输出直接可用，不要引导废话。\"}," +
            "{\"role\":\"user\",\"content\":\"" + escapeJson(prompt) + "\"}]," +
            "\"temperature\":0.3,\"max_tokens\":" + maxTokens + ",\"stream\":true}";
        doStreamCall(body, cb);
    }

    @Override
    public void chatStream(List<Map<String, String>> messages, Map<String, Object> ctx, StreamCallback cb) {
        if (messages == null || messages.isEmpty()) { cb.onError("对话消息不能为空"); return; }

        StringBuilder msgs = new StringBuilder("\"messages\":[");
        msgs.append("{\"role\":\"system\",\"content\":\"").append(escapeJson(buildChatSystem(ctx))).append("\"}");
        int start = Math.max(0, messages.size() - 10);
        for (int i = start; i < messages.size(); i++) {
            Map<String, String> m = messages.get(i);
            if (m.get("role") == null || m.get("content") == null) continue;
            msgs.append(",{\"role\":\"").append(m.get("role"))
                .append("\",\"content\":\"").append(escapeJson(m.get("content"))).append("\"}");
        }
        msgs.append("]");
        String body = "{\"model\":\"" + model + "\"," + msgs + ",\"temperature\":0.3,\"max_tokens\":1024,\"stream\":true}";
        doStreamCall(body, cb);
    }

    // ===== Prompt 构建（完整保留原逻辑）=====

    private String buildPrompt(Capability cap, Map<String, Object> ctx) {
        StringBuilder p = new StringBuilder();
        switch (cap) {
            case AUTO_REPLY:
                String sender = str(ctx, "sender"), subj = str(ctx, "subject");
                p.append("你正在帮用户起草一封邮件回复。请先判断原邮件的类型和语气，然后用匹配的风格回复。\n\n");
                if (!sender.isEmpty()) p.append("【重要】回复「").append(sender).append("」，称呼对方用此名。\n\n");
                p.append("【原则】\n1.语气匹配原邮件\n2.篇幅匹配原邮件\n3.对方问的问题逐一回答\n");
                p.append("4.涉及多议题分段回复\n5.请示/审批给出明确态度\n6.直接输出回复正文\n\n");
                if (!subj.isEmpty()) p.append("【主题】").append(subj).append("\n");
                p.append("【原文】\n").append(str(ctx, "content"));
                break;
            case SUMMARIZE_EMAIL:
                p.append("请对以下邮件智能总结，有几条实质内容就总结几条，不要凑数。\n\n");
                p.append("【原则】\n1.每点20-30字直击核心\n2.不要换说法重复\n3.有截止/责任人/需回复用⏰👤📩标注\n");
                p.append("4.纯通知无行动项标注\"（仅供参考）\"\n5.直接列要点\n\n");
                p.append("【主题】").append(str(ctx, "subject")).append("\n【正文】\n").append(str(ctx, "content"));
                break;
            case EXTRACT_TODO:
                p.append("请从以下邮件提取待办事项，只提取真正需要执行的。\n\n");
                p.append("【规则】\n1.有就列，没有回复\"无待办事项\"\n2.不要一条拆多条\n");
                p.append("3.每条含：谁做什么、截止时间（如有）\n\n");
                p.append("【主题】").append(str(ctx, "subject")).append("\n【正文】\n").append(str(ctx, "content"));
                break;
            case POLISH_EMAIL:
                p.append("请润色优化以下邮件草稿，保持原意不变。\n\n【规则】\n1.缺称呼自动补\n2.缺结束语自动补\n");
                p.append("3.修正错别字和不通顺句子\n4.合理分段\n5.口语转书面但不过度\n6.好的保留不瞎改\n");
                p.append("7.保留所有关键信息\n8.直接输出润色后正文\n\n【待润色】\n").append(str(ctx, "content"));
                break;
            case WRITE_EMAIL:
                p.append("请根据以下要求撰写邮件：\n\n").append(str(ctx, "requirement"));
                break;
            case ADJUST_TONE: {
                String tone = str(ctx, "tone"), desc;
                switch (tone) {
                    case "friendly": desc = "友好亲切语气"; break;
                    case "concise":  desc = "简洁明了语气"; break;
                    case "polite":   desc = "礼貌得体语气"; break;
                    default:         desc = "正式商务语气"; break;
                }
                p.append("请将以下内容调整为").append(desc).append("，保持原意，直接返回正文。\n\n").append(str(ctx, "content"));
                break;
            }
            case EXPAND_CONTENT:
                p.append("扩写以下内容，保持原意、补充细节、语言流畅。直接返回。\n\n").append(str(ctx, "content"));
                break;
            case SHRINK_CONTENT:
                p.append("精简缩写以下内容，保留所有关键信息点，删除冗余。直接返回。\n\n").append(str(ctx, "content"));
                break;
            case TRANSLATE_EMAIL:
                p.append("翻译为").append(str(ctx, "targetLanguage")).append("：\n\n").append(str(ctx, "content"));
                break;
            default: return null;
        }
        return p.toString();
    }

    private String buildChatSystem(Map<String, Object> ctx) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是致远OA内部邮件AI助手。\\n\\n");
        String scene = str(ctx, "scene");
        if ("detail".equals(scene)) sb.append("【当前】用户在看邮件详情页\\n");
        else if ("edit".equals(scene)) sb.append("【当前】用户在编辑邮件\\n");
        if (!str(ctx, "originalSender").isEmpty()) sb.append("【发件人】").append(str(ctx, "originalSender")).append("\\n");
        if (!str(ctx, "emailSubject").isEmpty()) sb.append("【主题】").append(str(ctx, "emailSubject")).append("\\n");
        String ec = str(ctx, "emailContent");
        if (!ec.isEmpty()) sb.append("【正文】\\n").append(truncate(extractEmailBody(ec), 3000)).append("\\n");
        String ed = str(ctx, "editorContent");
        if (!ed.isEmpty()) sb.append("【草稿】\\n").append(truncate(extractEmailBody(ed), 3000)).append("\\n");
        if (!str(ctx, "selectedText").isEmpty()) sb.append("【选中文字】\\n").append(str(ctx, "selectedText")).append("\\n");
        sb.append("\\n【原则】\\n理解自然语言指令灵活执行邮件任务。如涉及回复，用户是收件人。直接给结果不解释过程。\\n");
        return sb.toString();
    }

    // ===== HTML/文本处理 =====

    private String stripHtml(String html) {
        if (html == null || html.isEmpty()) return "";
        String t = html.replaceAll("(?i)<br\\s*/?>", "\n")
            .replaceAll("(?i)<hr\\s*/?>", "\n").replaceAll("(?i)</p>", "\n")
            .replaceAll("(?i)<p[^>]*>", "").replaceAll("<[^>]+>", "")
            .replaceAll("&nbsp;", " ").replaceAll("&amp;", "&")
            .replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"");
        return t.replaceAll("\n{3,}", "\n\n").trim();
    }

    private String extractEmailBody(String html) {
        if (html == null || html.isEmpty()) return "";
        int idx = html.indexOf("#1E9FFF");
        if (idx == -1) idx = html.indexOf("#1e9fff");
        if (idx > 0) {
            int tagEnd = html.indexOf('>', idx);
            if (tagEnd > 0 && tagEnd + 1 < html.length()) {
                String below = stripHtml(html.substring(tagEnd + 1)).trim();
                if (!below.isEmpty()) return below;
            }
        }
        return stripHtml(html).trim();
    }

    private String stripEmailTemplate(String html) {
        if (html == null || html.isEmpty()) return "";
        int idx = html.indexOf("#1E9FFF");
        if (idx == -1) idx = html.indexOf("#1e9fff");
        if (idx == -1) {
            idx = html.indexOf("---");
            if (idx > 0) {
                int end = idx;
                while (end < html.length() && html.charAt(end) == '-') end++;
                if (end - idx < 3) idx = -1;
                else { int ts = html.lastIndexOf('<', idx); if (ts >= 0) idx = ts; }
            }
        } else { int ts = html.lastIndexOf('<', idx); if (ts >= 0) idx = ts; }
        return (idx > 0) ? stripHtml(html.substring(0, idx)).trim() : stripHtml(html).trim();
    }

    // ===== 输出清理（完整保留原逻辑）=====

    private String cleanOutput(String content) {
        if (content == null) return "";
        String r = content.trim();
        r = r.replaceAll("(?is)<think>.*?</think>", "").replaceAll("(?is)<thinking>.*?</thinking>", "")
             .replaceAll("(?i)</?(?:think|thinking)>", "");
        int bi = r.indexOf("#1E9FFF");
        if (bi == -1) bi = r.indexOf("#1e9fff");
        if (bi > 0) { int ts = r.lastIndexOf('<', bi); if (ts > 0) r = r.substring(0, ts).trim(); }

        boolean isHtml = r.matches("(?s).*<(?:p|br|div|span|strong|em|ul|ol|li|h[1-6])\\b.*");
        if (isHtml) r = stripHtml(r);

        int si = r.indexOf("---");
        if (si > 0) { int e = si; while (e < r.length() && r.charAt(e) == '-') e++;
            if (e - si >= 3) { int ls = r.lastIndexOf('\n', si); r = r.substring(0, ls >= 0 ? ls : si).trim(); } }

        String[] prefixes = {"以下是","总结如下","修改如下","润色如下","回复如下",
            "好的，这是一封","好的，这是一篇","好的，这是","好的，以下是","好的，为您",
            "这是一封","这是一篇","下面是一封","为您撰写","为您起草",
            "Here is","Here are","Below is","思考过程","分析过程"};
        for (String pf : prefixes) {
            if (r.startsWith(pf)) {
                int nl = r.indexOf('\n');
                if (nl > 0) r = r.substring(nl + 1).trim();
                else { int ci = r.indexOf('：'); if (ci == -1) ci = r.indexOf(':'); r = ci > 0 ? r.substring(ci + 1).trim() : ""; }
                break;
            }
        }
        r = r.replaceAll("(?m)^\\s*\\d+\\.\\s*.*$", "").trim();
        return r;
    }

    // ===== 工具方法 =====

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t");
    }

    private String str(Map<String, Object> m, String key) {
        Object v = m.getOrDefault(key, "");
        return v != null ? v.toString() : "";
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "...（已截断）" : s;
    }
}
