import http from "../api/http";
// 独立部署：所有 API 路由改为空，nginx 代理 /api → 后端
let resquest = "";
export default {
  //   /*
  //   * 实列
  //   * */
  //   //用户登录
  //   userlogin(params){
  //     return http.post(`${resquest}/auth/login`,params)
  //   },
  // //  用户权限查看
  //   getpermissions(params){
  //     return http.get(`${resquest}/auth/getUserInfo`)
  //   },
  //
  //
  // //  删除菜单
  //   delmenu(params){
  //     return http.delete(`${resquest}/system/menu/${params}`)
  //   },
  // //  修改菜单
  //   putmenu(params){
  //     return http.put(`${resquest}/system/menu/edit`,params)
  //   },

  /**
   * 新建/修改过滤规则
   * @Date: 2022-06-08 10:21:04
   * @Author: tangxiangping
   * @param {*} params { id: string, subject: string, senderId: string, senderName: string}
   * @return {*} Promise
   */
  putfilter(params) {
    return http.post(`${resquest}/api/folder/updateRule`, params);
  },

  /**
   * 将过滤规则应用到历史邮件
   * @Date: 2022-06-08 11:46:09
   * @Author: tangxiangping
   * @param {*} id 自定义文件夹id
   * @return {*} Promise
   */
  putfilterToHistory(id) {
    return http.get(`${resquest}/api/folder/applyRule?id=${id}`);
  },

  //jian start
  //提交新建文件夹
  submitNewFolder(obj) {
    return http.post(`${resquest}/api/folder/create`, obj);
  },
  //修改自定义菜单名称
  changeFolderName(obj) {
    return http.post(`${resquest}/api/folder/update`, obj);
  },
  //删除自定义菜单
  delFolder(obj) {
    return http.post(`${resquest}/api/folder/delete`, obj);
  },
  //移动邮件到自定义菜单
  moveFolder(arr) {
    return http.post(`${resquest}/api/folder/move`, arr);
  },
  //搜索查询人员
  searchQueryStaff(obj) {
    return http.post(`${resquest}/api/email/seaechName`, obj);
  },
  //jy end

  // 获取图片连接
  getImgurl(obj) {
    return http.post(`${resquest}/api/email/preview`, obj);
  },
  // 获取审核连接
  getShurl(id) {
    return http.post(`${resquest}/api/email/flowMsg`, {
      summaryId: id,
    });
  },
  // 查询邮件是否撤回
  getShurlis(id) {
    return http.post(`${resquest}/api/email/revokeMsg`, {
      affairIds: id,
    });
  },

  //  邮件详情
  emliaInfo(id) {
    return http.get(`${resquest}/api/email/content/${id}`);
  },
  //  邮箱数量
  emlianum() {
    return http.get(`${resquest}/api/email/counts`);
  },
  //  邮件列表
  emlist(obj) {
    return http.post(`${resquest}/api/email/list`, obj);
  },
  //  删除邮件列表
  delemali(obj) {
    return http.post(`${resquest}/api/email/delete`, obj);
  },
  //  操作邮件
  operemali(obj) {
    return http.post(`${resquest}/api/email/mark`, obj);
  },
  //  加密箱验证
  encryption(obj) {
    return http.post(`${resquest}/api/email/encryption`, obj);
  },
  //  加密箱新增密码
  updateOrAddPwd(obj) {
    return http.post(`${resquest}/api/email/updateOrAddPwd`, obj);
  },
  //  获取分组列表
  getMailGroupType() {
    return http.post(`${resquest}/api/email/MailGroupType`);
  },
  //  查看回执
  getcheckStatus(obj) {
    return http.post(`${resquest}/api/email/checkStatus`, obj);
  },
  //  撤销
  emalirecovery(obj) {
    return http.post(`${resquest}/api/email/recall`, obj);
  },
  //  撤销校验
  judgeRescindMail(obj) {
    return http.post(`${resquest}/api/email/recall`, obj);
  },
  //  撤销校验
  recoverydel(obj) {
    return http.post(`${resquest}/api/email/recovery`, obj);
  },

  /*
   * 发件
   * */

  //  获取密级
  getSecretList(obj) {
    return http.post(`${resquest}/api/email/getSecretList`, obj);
  },
  //客开,huanglida,2025-12-4,邮件编辑态增加判断逻辑如果replyParentSummaryid有值的时候则按照回复的逻辑处理，避免转发和回复的草稿密级可以自由选择的的问题,start
  getSecretListByReplyParentSummaryId(obj) {
    return http.post(`${resquest}/api/email/getSecretList`, obj);
  },
  //客开,huanglida,2025-12-4,邮件编辑态增加判断逻辑如果replyParentSummaryid有值的时候则按照回复的逻辑处理，避免转发和回复的草稿密级可以自由选择的的问题,end
  //  获取密级
  isSecret(obj) {
    return http.post(`${resquest}/api/email/isSecret`, obj);
  },
  //  获取签名
  getmailSignature() {
    return http.post(`${resquest}/api/email/mailSignature`);
  },
  /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 start*/
  //获取uuid
  getUUID(){
    return http.get(`${resquest}/api/email/getUUID`);
  },

  getAttachmentUrl(){
    return `${resquest}/api/attachment`;
  },

  /*客开 wxt.万云龙 安全管理员、审计管理员日志改造 20240715 end*/
  //  转发
  forwordMail(obj) {
    return http.post(`${resquest}/api/email/forward`, obj);
  },

  //  检验文件密级
  MailFileSecret(obj) {
    return http.post(`${resquest}/api/email/MailFileSecret`, obj);
  },
  //  编辑获取内容
  internalCompile(obj) {
    return http.post(`${resquest}/api/email/compile`, obj);
  },
  //  编辑获取内容
  replyMailinfo(obj) {
    return http.post(`${resquest}/api/email/reply`, obj);
  },
  //  编辑获取内容
  allreplyMailinfo(obj) {
    return http.post(`${resquest}/api/email/allReply`, obj);
  },

  /**
   * 邮件审批
   * @desc: 根据选中密级对 收件人、抄送人 进行验证
   * @param {object} params: 请求参数
   * @param {string} params.secretTypeId: 密级id
   * @param {Array<string>} params.copyReceivers: 抄送人
   * @param {Array<string>} params.receivers: 收件人
   * @return {Promise<{ code:number, data:boolean, message:string }>} 验证结果
   */
  // 屏蔽邮件审批功能
  // emailApproval(params) {
  //   const url = `${resquest}/rest/emailapproval/emailappr`;
  //   return http.post(url, params);
  // },

  //  发送邮件
  sendemail(obj) {
    return http.post(`${resquest}/api/email/send`, obj);
  },

  MailFileSecretVerify(obj) {
    return http.post(`${resquest}/api/email/MailFileSecretVerify`, obj);
  },

  //  文件上传
  loadflie(obj, onUploadProgress) {
    return http.upload(`${resquest}/api/attachment`, obj, onUploadProgress);
  },
  /**
   * @function downloadAllFile: 下载全部附件// (核心)通知后端将所有附件进行打包
   */
  downloadAllFile(summaryId) {
    return http.get(`${resquest}/api/email/packFile/${summaryId}`);
  },

  // 校验查看密级
  MailPersonSecretVerify(obj) {
    return http.post(`${resquest}/api/email/MailPersonSecretVerify`, obj);
  },
  // 下载邮件
  exportEmail(obj) {
    return http.post(`${resquest}/api/email/export`, obj);
  },
  //获取用户信息 增加一个参数secretTypeId fuguichuan
  getuserInfo(secretTypeId) {
    return http.get(`${resquest}/api/email/getMemberByDeptApproverRole?secretTypeId=${secretTypeId}`);
  },
  //获取角色
  getroleId() {
    return http.get(`${resquest}/api/email/getMemberByDeptApproverRole`);
  },

  /*********************************************************************************** */
  // 取消继续编辑暂存的邮件
  cancelAutosaveApi(firstAutosaveTime, affairId) {
    return http.get(
      `${resquest}/api/email/cancelAutosave?firstAutosaveTime=${firstAutosaveTime}&oldAffairId=${affairId}`
    );
  },

  // 获取组或者部门的成员信息
  getGroupUserInfoApi(mailSummaryId , entityId) {
    return http.post(`${resquest}/api/org/groupMembers`, {
      mailSummaryId,
      entityId
    });
  },

  // 获取当前跨网权限
  getKuaWangShowApi() {
    return http.get(`${resquest}/api/email/showKuaWang`);
  },

  allpro(arr) {
    return http.all();
  },
  // 根据选人组件获取人员信息
  getPeopleMsg(obj) {
    return http.post(`${resquest}/api/org/groupMembers`, obj);
  },
  // 注释 fuguichuan
  // 第三方待办菜单  jianyuan
  // getNewMenu() {
  //   return http.get(`${resquest}/api/cipAppAffiar/cipAppAffiarMenu`);
  // },

  // 待办数和未读数接口
  getAffiarNoReadNum(id) {
    return http.get(
      `${resquest}/api/cipAppAffiar/findMsgNumAndPendingNum/${id}`
    );
  },
  //所有第三方待办数据   分页
  getAllThirdToDo(id, str) {
    return http.get(
      `${resquest}/api/cipAppAffiar/cipAppPendingList/${id}` + str
    );
  },
  //获取当前菜单应用的已办待办未读已读数据
  getApplicationNumber(id) {
    return http.get(`${resquest}/api/cipAppAffiar/findNum/${id}`);
  },

  //未读消息数据
  getNotReadList(id, str) {
    return http.get(
      `${resquest}/api/cipAppMessage/noReadMessageList/${id}` + str
    );
  },
  //已读消息数据
  getReadMessageList(id, str) {
    return http.get(
      `${resquest}/api/cipAppMessage/readMessageList/${id}` + str
    );
  },

  //待办搜索
  searchInfo(str, id) {
    return http.get(
      `${resquest}/rest/cipAppAffiar/searchCipAppPending/${id}` + str
    );
  },
  //已办搜索
  searchInfoHasPend(str, id) {
    return http.get(
      `${resquest}/rest/cipAppAffiar/cipAppHasPendingList/${id}` + str
    );
  },
  //已读搜索
  messageReadSearchInfo(str, id) {
    return http.get(
      `${resquest}/rest/cipAppMessage/searchReadMessage/${id}` + str
    );
  },

  //未读搜索
  messageNoReadSearchInfo(str, id) {
    return http.get(
      `${resquest}/rest/cipAppMessage/searchNoReadMessage/${id}` + str
    );
  },
  //已办数据
  cipAppHasPending(id, str) {
    return http.get(
      `${resquest}/rest/cipAppAffiar/cipAppHasPendingList/${id}` + str
    );
  },
  // 全部标记已读
  MarkReadAll(id) {
    return http.put(`${resquest}/rest/cipAppMessage/updateAllReadState/${id}`);
  },

  // 选择的标记已读
  selectMarkRead(array) {
    return http.put(`${resquest}/rest/cipAppMessage/updateReadState/${array}`);
  },

  //已办 待办 的删除
  cipAppAffiarHasPendingDel(array) {
    return http.delete(
      `${resquest}/rest/cipAppAffiar/deleteCipAppPending/${array}`
    );
  },
  //已读 未读 的全部清空
  cipAppReadAndNotReadDel(str) {
    return http.delete(
      `${resquest}/rest/cipAppMessage/deleteAllMessage/` + str
    );
  },
  //已读 未读 的清空已选择的
  cipAppSelectDel(array) {
    return http.delete(`${resquest}/rest/cipAppMessage/deleteMessage/${array}`);
  },
};
