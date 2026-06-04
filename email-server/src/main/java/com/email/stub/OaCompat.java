package com.email.stub;

import com.email.constants.InMailConstant;
import com.email.platform.*;
import com.email.platform.entity.OrgMember;
import com.email.security.UserContextHolder;
import com.email.security.UserInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;

/**
 * OA 类型兼容映射 —— 让 OA 代码的 import 只需更改包路径，类名保持不变。
 *
 * <p>用法：在 OA 原 Java 文件中执行以下全局替换后直接使用：
 * <pre>
 *   package com.seeyon.ctp.rest.resources.util → package com.email.service
 *   import com.seeyon.ctp.common.AppContext → import com.email.platform.AppContext
 *   import com.seeyon.ctp.util.* → import com.email.platform.*
 *   import com.seeyon.ctp.organization.bo.* → import com.email.stub.OaCompat.* (manual)
 * </pre></p>
 */

// ====== 对应 OA com.seeyon.ctp.rest.resources.result.JsonResult ======
@SuppressWarnings("rawtypes")
public class OaCompat {
    public static class JsonResult {
        private String code = "10001"; private String msg; private Object listType; private Map data;
        public JsonResult() {}
        public String getCode() { return code; } public void setCode(String v) { this.code = v; }
        public String getMsg() { return msg; } public void setMsg(String v) { this.msg = v; }
        public Object getListType() { return listType; } public void setListType(Object v) { this.listType = v; }
        public Map getData() { return data; } public void setData(Map v) { this.data = v; }
    }

    // ====== 对应 OA com.seeyon.ctp.login.UserUtil ======
    public static class UserUtil {
        public static UserInfo build(String loginName, String pwd, Object obj) {
            UserInfo u = new UserInfo(); u.setLoginName(loginName); return u;
        }
    }

    // ====== 对应 OA com.seeyon.ctp.common.usermessage.MessageReceiver ======
    public static class MessageReceiver {
        private Object id1; private Long memberId; private String linkType; private Object[] args;
        public MessageReceiver() {}
        public MessageReceiver(Object id1, Long memberId, String linkType, Object[] args) {
            this.id1 = id1; this.memberId = memberId; this.linkType = linkType; this.args = args;
        }
        public Long getMemberId() { return memberId; }
    }

    // ====== 对应 OA com.seeyon.ctp.common.usermessage.MessageContent ======
    public static class MessageContent {
        private String resource;
        public static MessageContent get(String key, Object... args) { return new MessageContent(); }
        public String getResource() { return resource; }
        public void setResource(String v) { this.resource = v; }
    }

    // ====== 对应 OA com.seeyon.ctp.common.usermessage.UserMessageManager ======
    public static class UserMessageManagerStub {
        public void sendSystemMessage(MessageContent content, Object category, Long uid, Collection coll, Object[] args) {}
    }

    // ====== 对应 OA com.seeyon.ctp.securitymanage.manager.* ======
    public static class FileSecretManager {
        public Object getDefault() {
            com.email.stub.OaCompat.FileSecretLevel l = new com.email.stub.OaCompat.FileSecretLevel();
            l.setId(1L); l.setName("普通"); l.setIsFileSecret(0);
            return l;
        }
        public Object getById(Long id) {
            com.email.stub.OaCompat.FileSecretLevel l = new com.email.stub.OaCompat.FileSecretLevel();
            l.setId(id); l.setName("普通"); l.setIsFileSecret(0); return l;
        }
        public Object getByLevel(Integer level) {
            com.email.stub.OaCompat.FileSecretLevel l = new com.email.stub.OaCompat.FileSecretLevel();
            l.setId(1L); l.setName("普通"); l.setIsFileSecret(0); return l;
        }
        public String getNameById(Long id) { return "普通"; }
        public java.util.List getAll() {
            com.email.stub.OaCompat.FileSecretLevel l = new com.email.stub.OaCompat.FileSecretLevel();
            l.setId(1L); l.setName("普通"); l.setIsFileSecret(0);
            return java.util.Collections.singletonList(l);
        }
    }

    public static class FileSecretMapManager {
        public Object getFileSecretByFileIdAndFileType(Long fileId, String type) {
            com.email.stub.OaCompat.FileSecretMap m = new com.email.stub.OaCompat.FileSecretMap();
            m.setFileSecretLevelId(1L); m.setFileType("email"); m.setFileId(fileId); m.setAccountId(1L);
            return m;
        }
        public void saveFileMapInfo(Object map) {}
    }

    public static class FileSecretLevel {
        private Long id; private String name; private Integer isFileSecret; private Integer level;
        public Long getId() { return id; } public void setId(Long v) { this.id = v; }
        public String getName() { return name; } public void setName(String v) { this.name = v; }
        public Integer getIsFileSecret() { return isFileSecret; } public void setIsFileSecret(Integer v) { this.isFileSecret = v; }
        public Integer getLevel() { return level; } public void setLevel(Integer v) { this.level = v; }
    }

    public static class FileSecretMap {
        private Long fileSecretLevelId; private String fileType; private Long fileId; private Long accountId;
        public Long getFileSecretLevelId() { return fileSecretLevelId; } public void setFileSecretLevelId(Long v) { this.fileSecretLevelId = v; }
        public String getFileType() { return fileType; } public void setFileType(String v) { this.fileType = v; }
        public Long getFileId() { return fileId; } public void setFileId(Long v) { this.fileId = v; }
        public Long getAccountId() { return accountId; } public void setAccountId(Long v) { this.accountId = v; }
    }

    public static class CheckSecretManager {
        public String getByFileName(Long id) { return ""; }
        public Map<Long,Boolean> checkMemberFileRight(Object secretId, String[] ids) { return Collections.emptyMap(); }
    }

    public static class SecretManagerPlugin {
        public static boolean isSecretManagerPluginStart() { return false; }
    }

    public static class PluginAuthorUtil {
        public boolean pluginAuthorityUtils() { return true; }
    }

    // ====== 对应 OA com.seeyon.ctp.common.constants.ApplicationCategoryEnum ======
    public static class ApplicationCategoryEnum {
        public static final ApplicationCategoryEnum mail = new ApplicationCategoryEnum();
        public int key() { return 12; }
    }

    // ====== 对应 OA com.seeyon.ctp.common.SystemEnvironment ======
    public static class SystemEnvironment {
        public static String getSystemTempFolder() { return System.getProperty("java.io.tmpdir"); }
    }

    // ====== 对应 OA com.seeyon.ctp.common.po.filemanager.Attachment ======
    public static class Attachment {
        private Long id; private Long size; private String filename; private String mimeType;
        private Long fileUrl; private java.util.Date createdate; private Long category; private Long type;
        private String description; private Long reference;
        public Long getId() { return id; } public void setId(Long v) { this.id = v; }
        public Long getSize() { return size; } public void setSize(Long v) { this.size = v; }
        public String getFilename() { return filename; } public void setFilename(String v) { this.filename = v; }
        public String getMimeType() { return mimeType; } public void setMimeType(String v) { this.mimeType = v; }
        public Long getFileUrl() { return fileUrl; } public void setFileUrl(Long v) { this.fileUrl = v; }
        public java.util.Date getCreatedate() { return createdate; } public void setCreatedate(java.util.Date v) { this.createdate = v; }
        public Long getCategory() { return category; } public void setCategory(Long v) { this.category = v; }
        public Long getType() { return type; } public void setType(Long v) { this.type = v; }
        public String getDescription() { return description; } public void setDescription(String v) { this.description = v; }
        public Long getReference() { return reference; } public void setReference(Long v) { this.reference = v; }
    }

    // ====== 对应 OA com.seeyon.ctp.common.po.filemanager.V3XFile ======
    public static class V3XFile {
        private Long id; private Long category; private Long type; private String filename;
        private String mimeType; private java.util.Date createDate; private Long size;
        private String description; private Long accountId; private Long createMember;
        public Long getId() { return id; } public void setId(Long v) { this.id = v; }
        public Long getCategory() { return category; } public void setCategory(Long v) { this.category = v; }
        public Long getType() { return type; } public void setType(Long v) { this.type = v; }
        public String getFilename() { return filename; } public void setFilename(String v) { this.filename = v; }
        public String getMimeType() { return mimeType; } public void setMimeType(String v) { this.mimeType = v; }
        public java.util.Date getCreateDate() { return createDate; } public void setCreateDate(java.util.Date v) { this.createDate = v; }
        public Long getSize() { return size; } public void setSize(Long v) { this.size = v; }
        public String getDescription() { return description; } public void setDescription(String v) { this.description = v; }
        public Long getAccountId() { return accountId; } public void setAccountId(Long v) { this.accountId = v; }
        public Long getCreateMember() { return createMember; } public void setCreateMember(Long v) { this.createMember = v; }
    }

    // ====== 对应 OA com.seeyon.ctp.common.filemanager.manager.AttachmentManager ======
    public static class AttachmentManagerAdapter {
        private static final Log log = LogFactory.getLog(AttachmentManagerAdapter.class);
        public void deleteByReference(Long ref1, Long ref2) {}
        public void create(Long[] fileIds, ApplicationCategoryEnum cat, Long ref1, Long ref2) {}
        public void create(java.util.List<Long> fileIds, ApplicationCategoryEnum cat, Long ref1, Long ref2) {}
        public java.util.List<Attachment> getByReference(Long ref) { return getByReference(ref, ref); }
        public java.util.List<Attachment> getByReference(Long ref1, Long ref2) { return Collections.emptyList(); }
        public java.util.List<Long> getBySubReference(Long id) { return Collections.emptyList(); }
        public void deleteByIds(java.util.List<Long> ids) {}
        public Attachment getAttachmentByFileURL(Long id) { return null; }
    }

    // ====== 对应 OA com.seeyon.ctp.common.filemanager.manager.FileManager ======
    public static class FileManagerAdapter {
        public com.email.stub.OaCompat.V3XFile save(FileManagerAdapter self, com.email.stub.OaCompat.V3XFile f) { return f; }
        public void save(com.email.stub.OaCompat.V3XFile f) {}
        public com.email.stub.OaCompat.V3XFile getV3XFile(Long id) { return null; }
        public java.io.File getFile(Long id, java.util.Date date) { return null; }
        public java.io.File getFileDecryption(Long id) { return null; }
        public String getFolder(java.util.Date date, boolean b) { return ""; }
    }

    // ====== 对应 OA com.seeyon.ctp.common.filemanager.manager.PartitionManager ======
    public static class PartitionManagerAdapter {
        public String getPartitionPath(Object obj, boolean b) { return System.getProperty("java.io.tmpdir"); }
    }

    // ====== 对应 OA com.seeyon.ctp.common.quartz.QuartzHolder ======
    public static class QuartzHolderCompat {
        public static void newQuartzJob(String name, java.util.Date date, String bean, Map params) {}
    }

    // ====== 对应 OA com.seeyon.ctp.event.EventDispatcher ======
    public static class EventDispatcherCompat {
        public static void fireEvent(Object event) {}
    }

    // ====== 对应 OA com.seeyon.ctp.securitymanage.po.PoEnum ======
    public static class PoEnum {
        public enum FileType { email }
    }

    // ====== 对应 OA com.seeyon.apps.internalmail.even.MailSaveEventPlugin ======
    public static class MailSaveEventPlugin {
        private java.util.List list;
        public MailSaveEventPlugin(java.util.List l) { this.list = l; }
        public void setInMailSummary(Object s) {}
        public void setRequestAttributesMap(Map m) {}
    }

    // ====== 对应 OA com.seeyon.apps.internalmail.even.MailSendEventPlugin ======
    public static class MailSendEventPlugin {
        private java.util.List list;
        public MailSendEventPlugin(java.util.List l) { this.list = l; }
        public void setInMailSummary(Object s) {}
        public void setRequestAttributesMap(Map m) {}
    }

    // ====== 对应 OA com.seeyon.apps.internalmail.listener.MyCollaborationEventListener ======
    public static class MyCollaborationEventListenerStub {
        public void flowAudit(Map m, String code, String title) {}
    }

    // ====== 对应 OA com.seeyon.apps.internalmail.manager.InMailOfficeTransManager ======
    public static class InMailOfficeTransManagerStub {
        public void officeTransGenerate(java.util.List attList, Map param, UserInfo user) {}
    }

    // ====== 对应 OA com.seeyon.ctp.rest.resources.util.NewEmailLogUtils ======
    public static class NewEmailLogUtilsStub {
        public static void emailLog(Object bean) {}
        public static void emailLog(Object bean, String msg) {}
    }

    // ====== 对应 OA com.seeyon.v3x.bulletin.util.BulletinUtils ======
    public static class BulletinUtilsCompat {
        public static void saveBulletinLog(Object... args) {}
    }

    // ====== 对应 OA com.seeyon.apps.filehandle.even.BulletinSaveEventPlugin ======
    public static class BulletinSaveEventPluginStub {}

    // ====== 对应 OA com.seeyon.v3x.common.web.login.CurrentUser ======
    public static class CurrentUser {
        public static UserInfo get() { return com.email.platform.AppContext.getCurrentUser(); }
    }

    private static final Log log = LogFactory.getLog(OaCompat.class);
}
