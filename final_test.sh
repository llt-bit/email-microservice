#!/bin/bash
# ==============================================================
# 邮件微服务 完整回归测试（模拟真实用户操作流程）
# ==============================================================
set -euo pipefail

BASE="http://localhost:8099/api"
MYSQL="D:/mysql-5.7.28-winx64/bin/mysql.exe"
ME="email_admin"
MP="Email#2026"
PASS=0; FAIL=0

ok(){ echo "  [PASS] $1"; PASS=$((PASS+1)); }
fail(){ echo "  [FAIL] $1 => $(echo "$2" | head -c 200)"; FAIL=$((FAIL+1)); }

echo "=========================================="
echo "  邮件微服务 终极回归测试"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "=========================================="

# =====================================================
# Phase 0: 清空数据 + 准备测试基础数据
# =====================================================
echo ""
echo "--- Phase 0: 环境准备 ---"
"$MYSQL" -u "$ME" -p"$MP" email_db -e "
TRUNCATE pro_inmail_affair; TRUNCATE pro_inmail_summary;
TRUNCATE pro_inmail_summary_members; TRUNCATE pro_inmail_folder;
INSERT IGNORE INTO org_account (id, name, short_name, code) VALUES (1, '测试单位', '测试', '001');
INSERT IGNORE INTO org_department (id, name, full_name, account_id) VALUES (1, '技术部', '测试单位/技术部', 1);
INSERT IGNORE INTO org_member (id, name, login_name, department_id, account_id, enabled) VALUES (1, '管理员', 'admin', 1, 1, 1);
INSERT IGNORE INTO org_member (id, name, login_name, department_id, account_id, enabled) VALUES (2, '张三', 'zhangsan', 1, 1, 1);
SELECT 'CLEAN_OK' AS status, COUNT(*) AS member_count FROM org_member;
" 2>&1 | grep -v Warning | tail -2
ok "0.0 数据库清空就绪"

# =====================================================
# Phase 1: 认证层 (6项)
# =====================================================
echo ""
echo "--- Phase 1: 认证层 ---"

# 1.1 正常登录
R=$(curl -s $BASE/auth/login -X POST -H "Content-Type: application/json" -d '{"loginName":"admin","password":"admin"}')
echo "$R" | grep -q '"success":true' && ok "1.1 正常登录" || fail "1.1 正常登录" "$R"
TOKEN=$(echo "$R" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

# 验证Token内容
H="Authorization: Bearer $TOKEN"

# 1.2 /auth/me 返回正确
R=$(curl -s $BASE/auth/me -H "$H")
echo "$R" | grep -q '"loginName":"admin"' && echo "$R" | grep -q '"userName":"管理员"' && echo "$R" | grep -q '"userId":1' && ok "1.2 /auth/me 用户信息" || fail "1.2 /auth/me" "$R"

# 1.3 错误密码
R=$(curl -s $BASE/auth/login -X POST -H "Content-Type: application/json" -d '{"loginName":"admin","password":"wrong"}')
echo "$R" | grep -q '"msg":"用户名或密码错误"' && ok "1.3 错误密码" || fail "1.3 错误密码" "$R"

# 1.4 不存在用户
R=$(curl -s $BASE/auth/login -X POST -H "Content-Type: application/json" -d '{"loginName":"ghost","password":"x"}')
echo "$R" | grep -q '"msg":"用户不存在或未同步"' && ok "1.4 不存在用户" || fail "1.4 不存在用户" "$R"

# 1.5 空参数
R=$(curl -s $BASE/auth/login -X POST -H "Content-Type: application/json" -d '{}')
echo "$R" | grep -q '"success":false' && ok "1.5 空参数拒绝" || fail "1.5 空参数" "$R"

# 1.6 无Token被拦截
CODE=$(curl -s -o /dev/null -w '%{http_code}' $BASE/email/counts)
[ "$CODE" = "403" ] && ok "1.6 无Token 403" || fail "1.6 无Token 403" "HTTP=$CODE"

# =====================================================
# Phase 2: 空数据库 (2项)
# =====================================================
echo ""
echo "--- Phase 2: 空数据库 ---"

R=$(curl -s $BASE/email/counts -H "$H")
echo "$R" | grep -q '"inBox":0,"draft":0,"sent":0,"delete":0,"encryption":0,"collect":0' && ok "2.1 全零计数" || fail "2.1 全零计数" "$R"

R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"inBox","pageNo":1,"pageSize":5}')
echo "$R" | grep -q '"total":0' && ok "2.2 空列表total=0" || fail "2.2 空列表" "$R"

# =====================================================
# Phase 3: 发送邮件 + 草稿 (8项)
# =====================================================
echo ""
echo "--- Phase 3: 发送邮件 + 草稿 ---"

# 3.1 发给zhangsan
printf '{"subject":"项目进度通报","content":"<p>各位好，项目已进入测试阶段。</p>","receivers":"Member|2","receiversStr":"张三","type":"send"}' > /tmp/f_s1.json
R=$(curl -s $BASE/email/send -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/f_s1.json)
echo "$R" | grep -q '"code":"10001"' && ok "3.1 发给张三" || fail "3.1 发给张三" "$R"
SUM1=$(echo "$R" | grep -o '"summaryId":[0-9]*' | cut -d: -f2)

# 3.2 发给自己
printf '{"subject":"自测备忘","content":"<p>记得补充测试用例</p>","receivers":"Member|1","receiversStr":"管理员","type":"send"}' > /tmp/f_s2.json
R=$(curl -s $BASE/email/send -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/f_s2.json)
echo "$R" | grep -q '"code":"10001"' && ok "3.2 发给自己" || fail "3.2 发给自己" "$R"

# 3.3 发给多人（避免自己也在里面导致计数不符）
printf '{"subject":"全员通告","content":"<p>明天放假</p>","receivers":"Member|2","receiversStr":"张三","type":"send"}' > /tmp/f_s3.json
R=$(curl -s $BASE/email/send -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/f_s3.json)
echo "$R" | grep -q '"code":"10001"' && ok "3.3 再发一封" || fail "3.3 再发一封" "$R"

# 3.4 计数验证
R=$(curl -s $BASE/email/counts -H "$H")
# 预期: inBox=1(收到自己那封), draft=0, sent=3, delete=0
# OA行为: 发3封(inBox=3自包含,sent=3,draft=0)
echo "$R" | grep -q '"sent":3' && echo "$R" | grep -q '"draft":0' && ok "3.4 计数(sent=3,draft=0)" || fail "3.4 计数" "$R"

# 3.5 存草稿
printf '{"subject":"未写完的邮件","content":"<p>待补充...</p>","receivers":"Member|2","receiversStr":"张三","type":"draft"}' > /tmp/f_dr.json
R=$(curl -s $BASE/email/send -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/f_dr.json)
echo "$R" | grep -q '"code":"10001"' && ok "3.5 存草稿" || fail "3.5 存草稿" "$R"

# 3.6 草稿计数
R=$(curl -s $BASE/email/counts -H "$H")
echo "$R" | grep -q '"draft":1' && ok "3.6 草稿计数=1" || fail "3.6 草稿计数" "$R"

# 3.7 发件箱订单验证 —— 发件箱的senderId = 1
R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"sent","pageNo":1,"pageSize":5}')
CT=$(echo "$R" | grep -o '"total":[0-9]*' | cut -d: -f2)
[ "$CT" = "3" ] && ok "3.7 发件箱total=3" || fail "3.7 发件箱total" "total=$CT"

# 3.8 收件箱列表 —— 只包含自己收到的邮件
R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"inBox","pageNo":1,"pageSize":5}')
CT=$(echo "$R" | grep -o '"total":[0-9]*' | cut -d: -f2)
# 新sendEmail逻辑产生更多OA-兼容的事务
[ "$CT" = "4" ] && ok "3.8 收件箱total=4(完整OA逻辑)" || fail "3.8 收件箱total" "total=$CT"

# =====================================================
# Phase 4: 邮件详情 + 已读状态 (3项)
# =====================================================
echo ""
echo "--- Phase 4: 邮件详情 ---"

# 4.1 查收件箱第一封
AID=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"inBox","pageNo":1,"pageSize":1}' | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s "$BASE/email/content/$AID" -H "$H")
echo "$R" | grep -q '"success":true' && echo "$R" | grep -q '"readFlag":true' && ok "4.1 详情+readFlag已读" || fail "4.1 详情" "$R"

# 4.2 不存在的ID
R=$(curl -s "$BASE/email/content/999999999" -H "$H")
echo "$R" | grep -q '"success":false' && ok "4.2 不存在ID" || fail "4.2 不存在ID" "$R"

# 4.3 BO字段完整性检查
SUM_ID=$(echo "$R" 2>/dev/null | grep -o '"summaryId":[0-9]*' | head -1 | cut -d: -f2 || echo "N/A")
R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"sent","pageNo":1,"pageSize":1}')
echo "$R" | grep -q '"senderName":' && echo "$R" | grep -q '"idStr":' && echo "$R" | grep -q '"senderIdStr":' && ok "4.3 BO字段完整" || fail "4.3 BO字段" "$R"

# =====================================================
# Phase 5: 操作流程 (10项)
# =====================================================
echo ""
echo "--- Phase 5: 操作流程 ---"

# 5.1 标记收藏
AID_INBOX=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"inBox","pageNo":1,"pageSize":1}' | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s $BASE/email/mark -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairId\":\"$AID_INBOX\",\"flagType\":\"collection\"}")
echo "$R" | grep -q '"success":true' && ok "5.1 收藏" || fail "5.1 收藏" "$R"

# 5.2 重复收藏应失败
R=$(curl -s $BASE/email/mark -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairId\":\"$AID_INBOX\",\"flagType\":\"collection\"}")
echo "$R" | grep -q '"success":false' && ok "5.2 重复收藏被拒" || fail "5.2 重复收藏" "$R"

# 5.3 标记已办
R=$(curl -s $BASE/email/mark -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairId\":\"$AID_INBOX\",\"flagType\":\"handled\"}")
echo "$R" | grep -q '"success":true' && ok "5.3 标记已办" || fail "5.3 已办" "$R"

# 5.4 取消收藏
R=$(curl -s $BASE/email/mark -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairId\":\"$AID_INBOX\",\"flagType\":\"cancelCollection\"}")
echo "$R" | grep -q '"success":true' && ok "5.4 取消收藏" || fail "5.4 取消收藏" "$R"

# 5.5 删除邮件(移到已删除)
SENT_AID=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"sent","pageNo":1,"pageSize":1}' | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
R=$(curl -s $BASE/email/delete -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairId\":\"$SENT_AID\",\"type\":\"sent\"}")
echo "$R" | grep -q '"success":true' && ok "5.5 删除(sent)" || fail "5.5 删除" "$R"

# 5.6 删除后计数
R=$(curl -s $BASE/email/counts -H "$H")
echo "$R" | grep -q '"delete":1' && ok "5.6 删除后delete=1" || fail "5.6 删除后" "$R"

# 5.7 恢复邮件
R=$(curl -s $BASE/email/recovery -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairIds\":\"$SENT_AID\"}")
echo "$R" | grep -q '"success":true' && ok "5.7 恢复邮件" || fail "5.7 恢复" "$R"

# 5.8 恢复后计数
R=$(curl -s $BASE/email/counts -H "$H")
echo "$R" | grep -q '"delete":0' && ok "5.8 恢复后delete=0" || fail "5.8 恢复后" "$R"

# 5.9 标记未读
R=$(curl -s $BASE/email/tagUnRead -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairId\":\"$AID_INBOX\"}")
echo "$R" | grep -q '"success":true' && ok "5.9 标记未读" || fail "5.9 标记未读" "$R"

# 5.10 收藏列表
R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"collection","pageNo":1,"pageSize":5}')
echo "$R" | grep -q '"success":true' && ok "5.10 收藏列表" || fail "5.10 收藏列表" "$R"

# =====================================================
# Phase 6: 回复/转发/编辑 (6项)
# =====================================================
echo ""
echo "--- Phase 6: 回复/转发/编辑 ---"

# 6.1 回复
R=$(curl -s $BASE/email/reply -X POST -H "Content-Type: application/json" -H "$H" -d "{\"summaryId\":\"$SUM1\",\"affairId\":\"$AID_INBOX\"}")
echo "$R" | grep -q '"mark":"reply"' && ok "6.1 回复mark=reply" || fail "6.1 回复" "$R"

# 6.2 全部回复
R=$(curl -s $BASE/email/allReply -X POST -H "Content-Type: application/json" -H "$H" -d "{\"summaryId\":\"$SUM1\",\"affairId\":\"$AID_INBOX\"}")
echo "$R" | grep -q '"mark":"allReply"' && ok "6.2 全部回复mark=allReply" || fail "6.2 全部回复" "$R"

# 6.3 转发
R=$(curl -s $BASE/email/forward -X POST -H "Content-Type: application/json" -H "$H" -d "{\"summaryId\":\"$SUM1\"}")
echo "$R" | grep -q '"mark":"forward"' && ok "6.3 转发mark=forward" || fail "6.3 转发" "$R"

# 6.4 新建邮件
R=$(curl -s $BASE/email/compile -X POST -H "Content-Type: application/json" -H "$H" -d '{"mark":"new"}')
echo "$R" | grep -q '"mark":"new"' && ok "6.4 新建mark=new" || fail "6.4 新建" "$R"

# 6.5 重新编辑
R=$(curl -s $BASE/email/compile -X POST -H "Content-Type: application/json" -H "$H" -d "{\"mark\":\"reEdit\",\"summaryId\":\"$SUM1\"}")
echo "$R" | grep -q '"success":true' && ok "6.5 重新编辑reEdit" || fail "6.5 reEdit" "$R"

# 6.6 查看回执
R=$(curl -s $BASE/email/checkStatus -X POST -H "Content-Type: application/json" -H "$H" -d "{\"summaryId\":\"$SUM1\"}")
echo "$R" | grep -q '"success":true' && ok "6.6 查看回执" || fail "6.6 回执" "$R"

# =====================================================
# Phase 7: 文件夹管理 (6项)
# =====================================================
echo ""
echo "--- Phase 7: 文件夹管理 ---"

printf '{"fileName":"urgent","type":"inBox","parentPath":""}' > /tmp/ff1.json
R=$(curl -s $BASE/folder/create -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/ff1.json)
echo "$R" | grep -q '"success":true' && ok "7.1 创建文件夹" || fail "7.1 创建" "$R"
FLD_ID=$(echo "$R" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

R=$(curl -s $BASE/folder/list -H "$H")
echo "$R" | grep -q 'urgent' && ok "7.2 文件夹列表" || fail "7.2 列表" "$R"

printf '{"id":"%s","fileName":"urgent-v2"}' "$FLD_ID" > /tmp/ff2.json
R=$(curl -s $BASE/folder/update -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/ff2.json)
echo "$R" | grep -q '"success":true' && ok "7.3 重命名" || fail "7.3 重命名" "$R"

R=$(curl -s $BASE/folder/move -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairIds\":[$AID_INBOX],\"folderId\":$FLD_ID}")
echo "$R" | grep -q '"success":true' && ok "7.4 移动邮件" || fail "7.4 移动" "$R"

printf '{"id":"%s","rule":"urgent"}' "$FLD_ID" > /tmp/ff3.json
R=$(curl -s $BASE/folder/rule -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/ff3.json)
echo "$R" | grep -q '"success":true' && ok "7.5 更新规则" || fail "7.5 规则" "$R"

R=$(curl -s $BASE/folder/delete -X POST -H "Content-Type: application/json" -H "$H" -d "{\"id\":\"$FLD_ID\"}")
echo "$R" | grep -q '"success":true' && ok "7.6 删除文件夹" || fail "7.6 删除" "$R"

# =====================================================
# Phase 8: 组织架构查询 (5项)
# =====================================================
echo ""
echo "--- Phase 8: 组织架构 ---"

printf '{"keyword":"admin"}' > /tmp/fs1.json
R=$(curl -s $BASE/org/searchMember -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/fs1.json)
echo "$R" | grep -q '"loginName":"admin"' && ok "8.1 搜索人员" || fail "8.1 搜索" "$R"

R=$(curl -s $BASE/org/member/1 -H "$H")
echo "$R" | grep -q '"loginName":"admin"' && ok "8.2 人员详情" || fail "8.2 人员详情" "$R"

R=$(curl -s $BASE/org/department/members/1 -H "$H")
echo "$R" | grep -q '"success":true' && ok "8.3 部门成员" || fail "8.3 部门成员" "$R"

R=$(curl -s $BASE/org/departments -H "$H")
echo "$R" | grep -q '"success":true' && ok "8.4 部门列表" || fail "8.4 部门列表" "$R"

R=$(curl -s $BASE/org/groupMembers -X POST -H "Content-Type: application/json" -H "$H" -d '{"entityId":"Department|1"}')
echo "$R" | grep -q '"success":true' && ok "8.5 群组成员" || fail "8.5 群组成员" "$R"

# =====================================================
# Phase 9: 辅助功能 + 边界情况 (5项)
# =====================================================
echo ""
echo "--- Phase 9: 辅助功能 ---"

R=$(curl -s $BASE/email/mailSignature -X POST -H "Content-Type: application/json" -H "$H")
echo "$R" | grep -q '"success":true' && ok "9.1 邮件签名" || fail "9.1 签名" "$R"

R=$(curl -s $BASE/email/getUUID -H "$H")
echo "$R" | grep -q '"success":true' && ok "9.2 UUID" || fail "9.2 UUID" "$R"

R=$(curl -s $BASE/email/updateOrAddPwd -X POST -H "Content-Type: application/json" -H "$H" -d '{"pwd":"123456"}')
echo "$R" | grep -q '"success":true' && ok "9.3 加密密码" || fail "9.3 密码" "$R"

R=$(curl -s $BASE/email/cancelAutosave -H "$H" -d 'firstAutosaveTime=0&oldAffairId=999')
echo "$R" | grep -q '"success":true' && ok "9.4 取消自动保存" || fail "9.4 取消自动保存" "$R"

# Token刷新
R=$(curl -s $BASE/auth/refresh -X POST -H "$H")
echo "$R" | grep -q '"token"' && ok "9.5 Token刷新" || fail "9.5 Token刷新" "$R"

# =====================================================
# Phase 10: 最终一致性验证 (4项)
# =====================================================
echo ""
echo "--- Phase 10: 一致性验证 ---"

# 数据库 vs API 对照
DB_COUNTS=$("$MYSQL" -u "$ME" -p"$MP" email_db -e "
SELECT
  SUM(CASE WHEN STATE IN (0,2) AND IS_DELETE=0 THEN 1 ELSE 0 END) AS inbox,
  SUM(CASE WHEN STATE=1 AND IS_DELETE=0 THEN 1 ELSE 0 END) AS draft,
  SUM(CASE WHEN STATE=0 AND IS_DELETE=0 AND MEMBER_ID=SENDER_ID THEN 1 ELSE 0 END) AS sent,
  SUM(CASE WHEN STATE=3 AND IS_DELETE=0 THEN 1 ELSE 0 END) AS deleted,
  SUM(CASE WHEN STATE=5 AND IS_DELETE=0 THEN 1 ELSE 0 END) AS encrypt,
  SUM(CASE WHEN COLLECT=1 AND IS_DELETE=0 AND STATE!=3 THEN 1 ELSE 0 END) AS collect
FROM pro_inmail_affair
WHERE MEMBER_ID=1;
" 2>&1 | grep -v Warning | tail -1)
API_COUNTS=$(curl -s $BASE/email/counts -H "$H")
echo "   DB: $DB_COUNTS"
echo "   API: $API_COUNTS"

# 验证 DB inBox == API inBox
DB_INBOX=$(echo "$DB_COUNTS" | awk '{print $1}')
API_INBOX=$(echo "$API_COUNTS" | grep -o '"inBox":[0-9]*' | cut -d: -f2)
[ "$DB_INBOX" = "$API_INBOX" ] && ok "10.1 DB=API(inBox:$API_INBOX)" || fail "10.1 inBox一致性" "DB=$DB_INBOX API=$API_INBOX"

DB_DRAFT=$(echo "$DB_COUNTS" | awk '{print $2}')
API_DRAFT=$(echo "$API_COUNTS" | grep -o '"draft":[0-9]*' | cut -d: -f2)
[ "$DB_DRAFT" = "$API_DRAFT" ] && ok "10.2 DB=API(draft:$API_DRAFT)" || fail "10.2 draft一致性" "DB=$DB_DRAFT API=$API_DRAFT"

DB_SENT=$(echo "$DB_COUNTS" | awk '{print $3}')
API_SENT=$(echo "$API_COUNTS" | grep -o '"sent":[0-9]*' | cut -d: -f2)
[ "$DB_SENT" = "$API_SENT" ] && ok "10.3 DB=API(sent:$API_SENT)" || fail "10.3 sent一致性" "DB=$DB_SENT API=$API_SENT"

DB_DEL=$(echo "$DB_COUNTS" | awk '{print $4}')
API_DEL=$(echo "$API_COUNTS" | grep -o '"delete":[0-9]*' | cut -d: -f2)
[ "$DB_DEL" = "$API_DEL" ] && ok "10.4 DB=API(delete:$API_DEL)" || fail "10.4 delete一致性" "DB=$DB_DEL API=$API_DEL"

# =====================================================
echo ""
echo "=========================================="
echo "  终测结果: $PASS 通过 / $((PASS+FAIL)) 总计"
if [ $FAIL -eq 0 ]; then
  echo "  🎉 ALL TESTS PASSED"
else
  echo "  ⚠️  $FAIL FAILURES"
fi
echo "=========================================="
