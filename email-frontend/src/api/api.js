import http from "../api/http";
// 独立部署：所有 API 指向新邮件微服务
let request = "";  // Nginx 同一域名下 /api/* → 微服务

export default {
  // ===== 邮件核心 =====
  emlist(obj) { return http.post(`${request}/api/email/list`, obj); },
  emliaInfo(id) { return http.get(`${request}/api/email/content/${id}`); },
  emlianum() { return http.get(`${request}/api/email/counts`); },
  sendemail(obj) { return http.post(`${request}/api/email/send`, obj); },
  delemali(obj) { return http.post(`${request}/api/email/delete`, obj); },
  operemali(obj) { return http.post(`${request}/api/email/mark`, obj); },
  emalirecovery(obj) { return http.post(`${request}/api/email/recall`, obj); },
  judgeRescindMail(obj) { return http.post(`${request}/api/email/recall`, obj); },
  recoverydel(obj) { return http.post(`${request}/api/email/recovery`, obj); },
  getcheckStatus(obj) { return http.post(`${request}/api/email/checkStatus`, obj); },
  internalCompile(obj) { return http.post(`${request}/api/email/compile`, obj); },
  forwordMail(obj) { return http.post(`${request}/api/email/forward`, obj); },
  replyMailinfo(obj) { return http.post(`${request}/api/email/reply`, obj); },
  allreplyMailinfo(obj) { return http.post(`${request}/api/email/allReply`, obj); },
  exportEmail(obj) { return http.post(`${request}/api/email/export`, obj); },
  searchQueryStaff(obj) { return http.post(`${request}/api/org/searchMember`, obj); },
  getGroupUserInfoApi(mailSummaryId, entityId) {
    return http.post(`${request}/api/org/groupMembers`, { mailSummaryId, entityId });
  },
  cancelAutosaveApi(firstAutosaveTime, affairId) {
    return http.get(`${request}/api/email/cancelAutosave?firstAutosaveTime=${firstAutosaveTime}&oldAffairId=${affairId}`);
  },
  getKuaWangShowApi() { return http.get(`${request}/api/email/showKuaWang`); },
  getPeopleMsg(obj) { return http.post(`${request}/api/org/searchMember`, obj); },

  // ===== 加密/密级 =====
  encryption(obj) { return http.post(`${request}/api/email/encryption`, obj); },
  updateOrAddPwd(obj) { return http.post(`${request}/api/email/updateOrAddPwd`, obj); },
  getSecretList(obj) { return http.post(`${request}/api/email/getSecretList`, obj); },
  isSecret(obj) { return http.post(`${request}/api/email/isSecret`, obj); },
  MailFileSecret(obj) { return http.post(`${request}/api/email/MailFileSecret`, obj); },
  MailFileSecretVerify(obj) { return http.post(`${request}/api/email/MailFileSecretVerify`, obj); },
  MailPersonSecretVerify(obj) { return http.post(`${request}/api/email/MailPersonSecretVerify`, obj); },

  // ===== 文件夹 =====
  submitNewFolder(obj) { return http.post(`${request}/api/folder/create`, obj); },
  changeFolderName(obj) { return http.post(`${request}/api/folder/update`, obj); },
  delFolder(obj) { return http.post(`${request}/api/folder/delete`, obj); },
  moveFolder(arr) { return http.post(`${request}/api/folder/move`, arr); },
  putfilter(params) { return http.post(`${request}/api/folder/rule`, params); },
  putfilterToHistory(id) { return http.get(`${request}/api/folder/applyRule`, { id }); },

  // ===== 附件 =====
  loadflie(obj, onUploadProgress) { return http.upload(`${request}/api/attachment/upload`, obj, onUploadProgress); },
  downloadAllFile(summaryId) { return http.get(`${request}/api/attachment/list/${summaryId}`); },
  getAttachmentUrl() { return `${request}/api/attachment/upload`; },

  // ===== AI 助手 =====
  // 原 /seeyon/apps/internalmail/lingmaAgent.do → /api/ai/agent
  lingmaAgent(obj) { return http.lingmaStream(`${request}/api/ai/agent`, obj); },

  // ===== 认证 =====
  login(params) { return http.post(`${request}/api/auth/login`, params); },
  getMe() { return http.get(`${request}/api/auth/me`); },
  refreshToken() { return http.post(`${request}/api/auth/refresh`); },

  // ===== 其他 =====
  getmailSignature() { return http.post(`${request}/api/email/mailSignature`); },
  getMailGroupType() { return http.post(`${request}/api/email/MailGroupType`); },
  getImgurl(obj) { return http.post(`${request}/api/email/preview`, obj); },
  getShurl(id) { return http.post(`${request}/api/email/flowMsg`, { summaryId: id }); },
  getShurlis(id) { return http.post(`${request}/api/email/revokeMsg`, { affairIds: id }); },
  getuserInfo(secretTypeId) { return http.get(`${request}/api/email/getMemberByDeptApproverRole?secretTypeId=${secretTypeId}`); },
  getroleId() { return http.get(`${request}/api/email/getMemberByDeptApproverRole`); },
  getUUID() { return http.get(`${request}/api/email/getUUID`); },
};
