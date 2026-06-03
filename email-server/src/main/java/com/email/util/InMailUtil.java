package com.email.util;

import com.email.entity.InMailAffair;
import com.email.entity.InMailSummary;
import com.email.entity.OrgDepartment;
import com.email.entity.OrgMember;
import com.email.mapper.OrgDepartmentMapper;
import com.email.mapper.OrgMemberMapper;
import com.email.security.UserContextHolder;
import com.email.security.UserInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 邮件组装工具 —— 从 OA InMailUtil 原样复制。
 * 只替换了：AppContext → UserContextHolder, OrgManager → Mapper, SecurityHelper/MessageEncoder → 本地实现。
 */
public class InMailUtil {

    private static final char[] hex = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    // 延迟注入（非 Spring 管理的工具类通过 AppContextHolder 获取）
    private static OrgMemberMapper orgMemberMapper;
    private static OrgDepartmentMapper orgDepartmentMapper;

    public static void setMappers(OrgMemberMapper m, OrgDepartmentMapper d) {
        orgMemberMapper = m;
        orgDepartmentMapper = d;
    }

    public static String removeRpeat(String sendToIds) {
        if (sendToIds == null || sendToIds.isEmpty()) return sendToIds;
        String[] str = sendToIds.split(",");
        Set<String> strSet = new LinkedHashSet<>(Arrays.asList(str));
        return String.join(",", strSet);
    }

    public static void webAlertAndClose(HttpServletResponse response, String msg) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<script>");
        out.println("alert('" + msg.replace("'", "\\'") + "');");
        out.println("if(window.dialogArguments){ window.close();");
        out.println("}else if (window.opener) { window.close();");
        out.println("}else{ parent.getA8Top().reFlesh(); }");
        out.println("</script>");
    }

    /**
     * 组装内部邮件对象 —— 从 OA InMailUtil.makeInMailSummary 原样复制。
     * 唯一变化: 参数 User → UserInfo。
     */
    public static void makeInMailSummary(InMailSummary bean, UserInfo user, String subject,
                                          String to, String toIds, String cc, String ccIds,
                                          String text, String shortMsg) {
        bean.setIdIfNew();
        bean.setSubject(subject);
        bean.setState(0); // InMailSummaryState.run.ordinal() = 0
        bean.setStartMemberId(user.getId());
        bean.setOrgDepartmentId(user.getDepartmentId());
        bean.setOrgAccountId(user.getLoginAccount());
        bean.setCreateDate(new Timestamp(System.currentTimeMillis()));
        bean.setSendTo(to);
        bean.setSendToIds(toIds);
        bean.setCopyTo(cc);
        bean.setCopyToIds(ccIds);
        try {
            text = EmailDESUtil.getEncryptString(text);
        } catch (Exception e) {
            // 加密失败不影响发送
        }
        bean.setContent(text);
        bean.setShortMsgContent(shortMsg);
    }

    /**
     * 组装内部邮件事务对象 —— 从 OA InMailUtil.makeInMailAffair 原样复制。
     */
    public static void makeInMailAffair(InMailAffair affair, InMailSummary bean, UserInfo user,
                                         String subject, int state, Long memberId) {
        affair.setIdIfNew();
        affair.setMemberId(memberId);
        affair.setSenderId(user.getId());
        affair.setSubject(subject);
        affair.setObjectId(bean.getId());
        affair.setState(state);
        affair.setCreateDate(new Timestamp(System.currentTimeMillis()));
        affair.setOrgDepartmentId(user.getDepartmentId());
        affair.setOrgAccountId(user.getLoginAccount());
        affair.setAttachmentsFlag(bean.getAttachmentsFlag());
        affair.setIsHandled(false);
    }

    /**
     * 邮件签名 —— 从 OA InMailUtil.mailSignature 原样复制。
     */
    public static String mailSignature() {
        try {
            UserInfo user = UserInfo.fromCurrentUser();
            Long unitId = UserContextHolder.get().getAccountId();
            Long departmentId = user.getDepartmentId();
            Long postId = user.getPostId();

            OrgMember deptOrg = orgDepartmentMapper != null ? orgDepartmentMapper.selectById(departmentId) : null;
            String unitName = ""; // TODO: 查 org_account 表
            String deptName = deptOrg != null ? deptOrg.getName() : "";
            String postName = ""; // 岗位名称
            String userName = user.getName();

            String content = "<br/><br/><br/><br/>";
            String star = "<p style='color:#1E9FFF;'>--------------------------------------------------------------------------------------------------------------------------</p><br/><br/>";
            String signature = "<strong><span style='color:rgb(0, 102, 204);' class=\"ql-size-large\">"
                    + unitName + "-" + deptName + "-" + postName + "-" + userName
                    + "</span></strong>";
            return content + star + signature;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 邮件密码加密 —— 从 OA InMailUtil.secretPwd 迁移。
     * 原方法用 SM3/SHA 加密，这里简化为 SHA-256。
     */
    public static String secretPwd(String loginName, String pwd) {
        if (pwd != null && pwd.trim().length() > 0) {
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] digest = md.digest((loginName + ":" + pwd).getBytes("UTF-8"));
                StringBuilder sb = new StringBuilder();
                for (byte b : digest) {
                    sb.append(hex[(b >> 4) & 0x0f]);
                    sb.append(hex[b & 0x0f]);
                }
                return sb.toString();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}
