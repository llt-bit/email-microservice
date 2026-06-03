package com.email.stub;

import java.util.*;

/**
 * OA 平台 Stub 合集 —— 让邮件业务代码原样编译通过。
 * 这些类在剥离后会被逐步替换为真实实现。
 */
public class OaStubs {

    // ========== 协作/审批 (com.seeyon.apps.collaboration.*) ==========
    public static class ColManager {
        public Object getColSummaryById(Long id) { return null; }
        public Object getColSummaryById(Long id, boolean b) { return null; }
    }
    public static class ColSummary {
        private Long id; private Integer state;
        public Long getId() { return id; } public void setId(Long id) { this.id = id; }
        public Integer getState() { return state; } public void setState(Integer state) { this.state = state; }
    }

    // ========== CAP4 表单 (com.seeyon.cap4.form.*) ==========
    public static class FormApi4Cap4 {
        public Map<String, Object> getFormDataByColSummaryId(Long summaryId) { return Collections.emptyMap(); }
        public Object getFormmainBySummaryId(Long summaryId, int code) { return null; }
    }
    public static class FormBean {
        private Long summaryId; private String subject;
        public Long getSummaryId() { return summaryId; } public void setSummaryId(Long v) { this.summaryId = v; }
        public String getSubject() { return subject; } public void setSubject(String v) { this.subject = v; }
    }
    public static class FormTableBean {
        private List<List<String[]>> rowData = new ArrayList<>();
        public List<List<String[]>> getRowData() { return rowData; }
        public void setRowData(List<List<String[]>> v) { this.rowData = v; }
    }
    public static class FormDataMasterBean {
        private Long id; private Integer state;
        public Long getId() { return id; } public void setId(Long v) { this.id = v; }
        public Integer getState() { return state; } public void setState(Integer v) { this.state = v; }
    }

    // ========== 数据摆渡 (com.seeyon.apps.mic.datafusion.*) ==========
    public static class DataFeffryLogManager {
        public void addLog(Map<String, Object> log) {}
        public Object getBySourceTargetId(Long id, String type) { return null; }
    }
    public static class FileFerry {
        public boolean ferryFile(Map<String, Object> params) { return true; }
    }
    public static class FileOperationUtil {
        public static void copyFile(String from, String to) {}
    }
    public static class NetworkSegmentUtil {
        public static boolean isSameNetwork(String ip1, String ip2) { return true; }
    }

    // ========== 事件 (com.seeyon.ctp.event.*) ==========
    public static class EventDispatcher {
        public void fireEvent(Object event) {}
    }

    // ========== 定时任务 (com.seeyon.ctp.common.quartz.*) ==========
    public static class QuartzHolder {
        public static Object newJob(Class<?> clazz) { return null; }
        public static void scheduleJob(Object job, String cron) {}
    }

    // ========== 日志/工具 (com.seeyon.ctp.*) ==========
    public static class NewEmailLogUtils {
        public static void addLog(Map<String, Object> logMap) {}
    }
    public static class CurrentUser {
        public static com.email.security.UserInfo get() {
            return com.email.platform.AppContext.getCurrentUser();
        }
    }
    public static class Functions {
        public static String getAvatarImageUrl(Long memberId) { return ""; }
    }
    public static class SystemEnvironment {
        public static String getSystemTempFolder() { return System.getProperty("java.io.tmpdir"); }
    }
    public static class ApplicationCategoryEnum {
        public static final ApplicationCategoryEnum mail = new ApplicationCategoryEnum();
        public int key() { return 12; }
    }

    // ========== 文件管理适配 (bean名: attachmentManager/fileManager) ==========
    public static class AttachmentManagerAdapter {
        public void deleteByReference(Long ref1, Long ref2) {}
        public void create(Long[] fileIds, ApplicationCategoryEnum cat, Long ref1, Long ref2) {}
        public List<Object> getByReference(Long ref) { return Collections.emptyList(); }
    }

    // ========== 用户消息 (bean名: userMessageManager) ==========
    public static class UserMessageManagerStub {
        public void sendSystemMessage(Object content, ApplicationCategoryEnum cat, Long userId, Object result) {}
        public void sendSystemMessage(Object content, ApplicationCategoryEnum cat, Object result) {}
    }

    // ========== 密级管理 (com.seeyon.ctp.securitymanage.*) ==========
    public static class FileSecretManager {
        public Object getDefault() { return null; }
        public Object getById(Long id) { return null; }
        public Object getByLevel(Integer level, Long accountId) { return null; }
        public String getNameById(Long id) { return ""; }
    }
    public static class FileSecretMapManager {
        public Object getFileSecretByFileIdAndFileType(Long fileId, String type) { return null; }
        public void saveFileMapInfo(Object map) {}
    }
    public static class CheckSecretManager {
        public Map<Long, Boolean> checkMemberFileRight(Object secretId, String[] memberIds) { return Collections.emptyMap(); }
        public Map<Long, Boolean> checkMemberFileRight(String secretId, String id) { return Collections.emptyMap(); }
    }
    public static class FileSecretLevel {
        private Long id; private String name; private Integer level; private Integer isFileSecret;
        public Long getId() { return id; } public void setId(Long v) { this.id = v; }
        public String getName() { return name; } public void setName(String v) { this.name = v; }
        public Integer getLevel() { return level; } public void setLevel(Integer v) { this.level = v; }
        public Integer getIsFileSecret() { return isFileSecret; } public void setIsFileSecret(Integer v) { this.isFileSecret = v; }
    }
    public static class FileSecretMap {
        private Long fileSecretLevelId; private String fileType; private Long fileId;
        public Long getFileSecretLevelId() { return fileSecretLevelId; } public void setFileSecretLevelId(Long v) { this.fileSecretLevelId = v; }
        public String getFileType() { return fileType; } public void setFileType(String v) { this.fileType = v; }
        public Long getFileId() { return fileId; } public void setFileId(Long v) { this.fileId = v; }
    }

    // ========== 消息内容 (com.seeyon.ctp.common.usermessage.*) ==========
    public static class MessageContent {
        private String resource; private Object[] args;
        public static MessageContent get(String key, Object... args) { MessageContent mc = new MessageContent(); mc.args = args; return mc; }
        public String getResource() { return resource; }
        public void setResource(String r) { this.resource = r; }
    }
    public static class MessageReceiver {
        private Long memberId; private String memberName;
        public Long getMemberId() { return memberId; } public void setMemberId(Long v) { this.memberId = v; }
        public String getMemberName() { return memberName; } public void setMemberName(String v) { this.memberName = v; }
    }

    // ========== 工作流 (com.seeyon.ctp.workflow.*) ==========
    public static class WorkflowAjaxManager {
        public Object getFlowMsg(String summaryId) { return null; }
    }
}
