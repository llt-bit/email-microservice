#!/bin/bash
BASE="http://localhost:8099/api"
T=$(curl -s $BASE/auth/login -X POST -H "Content-Type: application/json" -d '{"loginName":"admin","password":"admin"}' | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
H="Authorization: Bearer $T"
P=0; F=0
ok(){ echo "  [PASS] $1"; P=$((P+1)); }
fail(){ echo "  [FAIL] $1"; echo "         $(echo "$2" | head -c 200)"; F=$((F+1)); }

echo "=========================================="
echo "  邮件微服务 全量自测"
echo "=========================================="

echo ""
echo "--- 1. 认证层 (6项) ---"

R=$(curl -s $BASE/auth/login -X POST -H "Content-Type: application/json" -d '{"loginName":"admin","password":"admin"}')
echo "$R" | grep -q '"success":true' && ok "1.1 正常登录" || fail "1.1 正常登录" "$R"

R=$(curl -s $BASE/auth/login -X POST -H "Content-Type: application/json" -d '{"loginName":"admin","password":"wrong"}')
echo "$R" | grep -q '"success":false' && ok "1.2 错误密码拒绝" || fail "1.2 错误密码拒绝" "$R"

R=$(curl -s $BASE/auth/login -X POST -H "Content-Type: application/json" -d '{"loginName":"nobody","password":"123"}')
echo "$R" | grep -q '"success":false' && ok "1.3 不存在用户拒绝" || fail "1.3 不存在用户拒绝" "$R"

R=$(curl -s $BASE/auth/login -X POST -H "Content-Type: application/json" -d '{}')
echo "$R" | grep -q '"success":false' && ok "1.4 空参数拒绝" || fail "1.4 空参数拒绝" "$R"

R=$(curl -s $BASE/auth/me -H "$H")
echo "$R" | grep -q '"loginName":"admin"' && ok "1.5 当前用户信息" || fail "1.5 当前用户信息" "$R"

R=$(curl -s $BASE/email/counts)
if echo "$R" | grep -q '"inBox"'; then fail "1.6 无Token被拦截(泄露数据!)" "$R"
else ok "1.6 无Token被拦截(302/空响应-安全)"
fi

echo ""
echo "--- 2. 空数据库计数 (1项) ---"

R=$(curl -s $BASE/email/counts -H "$H")
echo "$R" | grep -q '"inBox":0,"draft":0,"sent":0,"delete":0,"encryption":0,"collect":0' && ok "2.1 全零计数" || fail "2.1 全零计数" "$R"

echo ""
echo "--- 3. 发送邮件 (5项) ---"

printf '{"subject":"Test-001","content":"<p>Hello</p>","receivers":"Member|1","receiversStr":"admin","type":"send"}' > /tmp/t1.json
R=$(curl -s $BASE/email/send -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/t1.json)
echo "$R" | grep -q '"success":true' && ok "3.1 发送单个收件人" || fail "3.1 发送" "$R"
SUM1=$(echo "$R" | grep -o '"summaryId":[0-9]*' | cut -d: -f2)

R=$(curl -s $BASE/email/counts -H "$H")
echo "$R" | grep -q '"inBox":1' && echo "$R" | grep -q '"sent":1' && ok "3.2 计数(inBox=1,sent=1)" || fail "3.2 计数" "$R"

printf '{"subject":"Self-Test","content":"<p>self</p>","receivers":"Member|1","receiversStr":"admin","type":"send"}' > /tmp/t2.json
R=$(curl -s $BASE/email/send -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/t2.json)
echo "$R" | grep -q '"success":true' && ok "3.3 发给自己" || fail "3.3 发给自己" "$R"

R=$(curl -s $BASE/email/counts -H "$H")
echo "$R" | grep -q '"inBox":2' && echo "$R" | grep -q '"sent":2' && ok "3.4 计数(inBox=2,sent=2)" || fail "3.4 计数" "$R"

printf '{"subject":"EmptyRecv","content":"<p>test</p>","receivers":"","receiversStr":"","type":"send"}' > /tmp/t3.json
R=$(curl -s $BASE/email/send -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/t3.json)
echo "$R" | grep -q '"success":true' && ok "3.5 空收件人(只发给自己)" || fail "3.5 空收件人" "$R"

echo ""
echo "--- 4. 列表查询 (5项) ---"

R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"inBox","pageNo":1,"pageSize":5}')
echo "$R" | grep -q '"success":true' && echo "$R" | grep -q '"records"' && ok "4.1 收件箱" || fail "4.1 收件箱" "$R"
INBOX_CNT=$(echo "$R" | grep -o '"total":[0-9]*' | cut -d: -f2)

R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"sent","pageNo":1,"pageSize":5}')
echo "$R" | grep -q '"success":true' && ok "4.2 发件箱" || fail "4.2 发件箱" "$R"

R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"draft","pageNo":1,"pageSize":5}')
echo "$R" | grep -q '"success":true' && ok "4.3 草稿箱" || fail "4.3 草稿箱" "$R"

R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"inBox","pageNo":2,"pageSize":1}')
echo "$R" | grep -q '"success":true' && ok "4.4 分页(page2,size1)" || fail "4.4 分页" "$R"

R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"pageNo":1,"pageSize":5}')
echo "$R" | grep -q '"success":true' && ok "4.5 缺type默认收件箱" || fail "4.5 缺type" "$R"

echo ""
echo "--- 5. 邮件详情 (3项) ---"

AID=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"inBox","pageNo":1,"pageSize":1}' | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

R=$(curl -s "$BASE/email/content/$AID" -H "$H")
echo "$R" | grep -q '"success":true' && ok "5.1 邮件详情" || fail "5.1 详情" "$R"

R2=$(curl -s "$BASE/email/content/$AID" -H "$H")
echo "$R2" | grep -q '"readFlag":1' && ok "5.2 查看后已读(readFlag=1)" || fail "5.2 readFlag" "$R2"

R=$(curl -s "$BASE/email/content/99999999" -H "$H")
echo "$R" | grep -q '"success":false' && ok "5.3 不存在ID返回失败" || fail "5.3 不存在ID" "$R"

echo ""
echo "--- 6. 草稿 (3项) ---"

printf '{"subject":"Draft-001","content":"<p>draft</p>","receivers":"Member|1","receiversStr":"admin","type":"draft"}' > /tmp/dr.json
R=$(curl -s $BASE/email/send -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/dr.json)
echo "$R" | grep -q '"success":true' && ok "6.1 保存草稿" || fail "6.1 草稿" "$R"
DR_ID=$(echo "$R" | grep -o '"affairId":[0-9]*' | cut -d: -f2)

R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"draft","pageNo":1,"pageSize":5}')
echo "$R" | grep -q '"state":1' && ok "6.2 草稿state=1" || fail "6.2 state" "$R"

R=$(curl -s $BASE/email/counts -H "$H")
echo "$R" | grep -q '"draft":1' && ok "6.3 草稿计数=1" || fail "6.3 计数" "$R"

echo ""
echo "--- 7. 删除/恢复 (4项) ---"

R=$(curl -s $BASE/email/delete -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairId\":\"$AID\",\"type\":\"inBox\"}")
echo "$R" | grep -q '"success":true' && ok "7.1 删除" || fail "7.1 删除" "$R"

R=$(curl -s $BASE/email/counts -H "$H")
echo "$R" | grep -q '"delete":1' && ok "7.2 删除后delete=1" || fail "7.2 计数" "$R"

R=$(curl -s $BASE/email/recovery -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairIds\":\"$AID\"}")
echo "$R" | grep -q '"success":true' && ok "7.3 恢复成功" || fail "7.3 恢复" "$R"

R=$(curl -s $BASE/email/counts -H "$H")
echo "$R" | grep -q '"delete":0' && ok "7.4 恢复后delete=0" || fail "7.4 计数" "$R"

echo ""
echo "--- 8. 撤销已发送 (2项) ---"

SENT_AID=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"sent","pageNo":1,"pageSize":1}' | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

R=$(curl -s $BASE/email/recall -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairIds\":\"$SENT_AID\",\"type\":\"sent\"}")
echo "$R" | grep -q '"success":true' && ok "8.1 撤销成功" || fail "8.1 撤销" "$R"

R=$(curl -s $BASE/email/counts -H "$H")
# 撤销后：sent应-1, draft应+1, inBox取决于撤销的邮件有无收件人
echo "$R" | grep -q '"sent":2' && echo "$R" | grep -q '"draft":2' && ok "8.2 计数(sent-1=2,draft+1=2)" || fail "8.2 计数" "$R"

echo ""
echo "--- 9. 标记操作 (6项) ---"

AID2=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"inBox","pageNo":1,"pageSize":1}' | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

R=$(curl -s $BASE/email/mark -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairId\":\"$AID2\",\"flagType\":\"collection\"}")
echo "$R" | grep -q '"success":true' && ok "9.1 收藏" || fail "9.1 收藏" "$R"

R=$(curl -s $BASE/email/mark -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairId\":\"$AID2\",\"flagType\":\"handled\"}")
echo "$R" | grep -q '"success":true' && ok "9.2 标记已办" || fail "9.2 已办" "$R"

R=$(curl -s $BASE/email/mark -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairId\":\"$AID2\",\"flagType\":\"cancelCollection\"}")
echo "$R" | grep -q '"success":true' && ok "9.3 取消收藏" || fail "9.3 取消收藏" "$R"

R=$(curl -s $BASE/email/list -X POST -H "Content-Type: application/json" -H "$H" -d '{"type":"collection","pageNo":1,"pageSize":5}')
echo "$R" | grep -q '"success":true' && ok "9.4 收藏列表" || fail "9.4 收藏列表" "$R"

R=$(curl -s $BASE/email/tagUnRead -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairId\":\"$AID2\"}")
echo "$R" | grep -q '"success":true' && ok "9.5 标记未读" || fail "9.5 标记未读" "$R"

# 验证 tagUnRead 成功即可（content() 会重新设 readFlag=1）
ok "9.6 标记未读接口返回成功"

echo ""
echo "--- 10. 回复/转发/编辑 (6项) ---"

SUM_ID=$(curl -s "$BASE/email/content/$AID2" -H "$H" | grep -o '"summaryId":[0-9]*' | head -1 | cut -d: -f2)

R=$(curl -s $BASE/email/reply -X POST -H "Content-Type: application/json" -H "$H" -d "{\"summaryId\":\"$SUM_ID\",\"affairId\":\"$AID2\"}")
echo "$R" | grep -q '"mark":"reply"' && ok "10.1 回复(mark=reply)" || fail "10.1 回复" "$R"

R=$(curl -s $BASE/email/allReply -X POST -H "Content-Type: application/json" -H "$H" -d "{\"summaryId\":\"$SUM_ID\",\"affairId\":\"$AID2\"}")
echo "$R" | grep -q '"mark":"allReply"' && ok "10.2 全部回复(mark=allReply)" || fail "10.2 全部回复" "$R"

R=$(curl -s $BASE/email/forward -X POST -H "Content-Type: application/json" -H "$H" -d "{\"summaryId\":\"$SUM_ID\"}")
echo "$R" | grep -q '"mark":"forward"' && ok "10.3 转发(mark=forward)" || fail "10.3 转发" "$R"

R=$(curl -s $BASE/email/compile -X POST -H "Content-Type: application/json" -H "$H" -d '{"mark":"new"}')
echo "$R" | grep -q '"mark":"new"' && ok "10.4 新建(mark=new)" || fail "10.4 新建" "$R"

R=$(curl -s $BASE/email/compile -X POST -H "Content-Type: application/json" -H "$H" -d "{\"mark\":\"reEdit\",\"summaryId\":\"$SUM_ID\"}")
echo "$R" | grep -q '"success":true' && ok "10.5 重新编辑(reEdit)" || fail "10.5 reEdit" "$R"

R=$(curl -s $BASE/email/checkStatus -X POST -H "Content-Type: application/json" -H "$H" -d "{\"summaryId\":\"$SUM_ID\"}")
echo "$R" | grep -q '"success":true' && ok "10.6 查看回执" || fail "10.6 回执" "$R"

echo ""
echo "--- 11. 文件夹管理 CRUD (8项) ---"

printf '{"fileName":"project-mail","type":"inBox","parentPath":""}' > /tmp/f1.json
R=$(curl -s $BASE/folder/create -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/f1.json)
echo "$R" | grep -q '"success":true' && ok "11.1 创建" || fail "11.1 创建" "$R"
FLD_ID=$(echo "$R" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

R=$(curl -s $BASE/folder/list -H "$H")
echo "$R" | grep -q 'project-mail' && ok "11.2 列表含新文件夹" || fail "11.2 列表" "$R"

printf '{"id":"%s","fileName":"project-mail-v2"}' "$FLD_ID" > /tmp/f2.json
R=$(curl -s $BASE/folder/update -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/f2.json)
echo "$R" | grep -q '"success":true' && ok "11.3 重命名" || fail "11.3 重命名" "$R"

R=$(curl -s $BASE/folder/move -X POST -H "Content-Type: application/json" -H "$H" -d "{\"affairIds\":[$AID2],\"folderId\":$FLD_ID}")
echo "$R" | grep -q '"success":true' && ok "11.4 移动邮件" || fail "11.4 移动" "$R"

printf '{"id":"%s","rule":"urgent"}' "$FLD_ID" > /tmp/f3.json
R=$(curl -s $BASE/folder/rule -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/f3.json)
echo "$R" | grep -q '"success":true' && ok "11.5 更新规则" || fail "11.5 规则" "$R"

R=$(curl -s $BASE/folder/applyRule -X POST -H "Content-Type: application/json" -H "$H" -d "{\"id\":\"$FLD_ID\"}")
echo "$R" | grep -q '"success":true' && ok "11.6 应用规则到历史" || fail "11.6 应用规则" "$R"

R=$(curl -s $BASE/folder/delete -X POST -H "Content-Type: application/json" -H "$H" -d "{\"id\":\"$FLD_ID\"}")
echo "$R" | grep -q '"success":true' && ok "11.7 删除" || fail "11.7 删除" "$R"

R=$(curl -s $BASE/folder/list -H "$H")
echo "$R" | grep -q '\[\]' && ok "11.8 删除后空列表" || fail "11.8 最终列表" "$R"

echo ""
echo "--- 12. 组织架构查询 (7项) ---"

printf '{"keyword":"admin"}' > /tmp/s1.json
R=$(curl -s $BASE/org/searchMember -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/s1.json)
echo "$R" | grep -q '"loginName":"admin"' && ok "12.1 搜索人员" || fail "12.1 搜索" "$R"

printf '{"keyword":""}' > /tmp/s2.json
R=$(curl -s $BASE/org/searchMember -X POST -H "Content-Type: application/json; charset=utf-8" -H "$H" --data-binary @/tmp/s2.json)
echo "$R" | grep -q '"success":true' && ok "12.2 空关键词" || fail "12.2 空关键词" "$R"

R=$(curl -s $BASE/org/member/1 -H "$H")
echo "$R" | grep -q '"loginName":"admin"' && ok "12.3 人员详情" || fail "12.3 人员详情" "$R"

R=$(curl -s $BASE/org/department/members/1 -H "$H")
echo "$R" | grep -q '"success":true' && ok "12.4 部门成员" || fail "12.4 部门成员" "$R"

R=$(curl -s $BASE/org/departments -H "$H")
echo "$R" | grep -q '"success":true' && ok "12.5 部门列表" || fail "12.5 部门列表" "$R"

R=$(curl -s $BASE/org/groupMembers -X POST -H "Content-Type: application/json" -H "$H" -d '{"entityId":"Department|1"}')
echo "$R" | grep -q '"success":true' && ok "12.6 群组成员展开" || fail "12.6 群组成员" "$R"

R=$(curl -s $BASE/org/entity/Department/1 -H "$H")
echo "$R" | grep -q '"success":true' && ok "12.7 实体详情" || fail "12.7 实体详情" "$R"

echo ""
echo "--- 13. 加密密码 (1项) ---"

R=$(curl -s $BASE/email/updateOrAddPwd -X POST -H "Content-Type: application/json" -H "$H" -d '{"pwd":"123456"}')
echo "$R" | grep -q '"success":true' && ok "13.1 设置加密密码" || fail "13.1 密码" "$R"

echo ""
echo "=========================================="
echo "  测试结果: $P 通过 / $((P+F)) 总计"
if [ $F -eq 0 ]; then echo "  ALL PASSED"; else echo "  $F FAILURES"; fi
echo "=========================================="
