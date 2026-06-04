package com.email.controller;

import com.email.common.R;
import com.email.entity.InMailFolder;
import com.email.platform.*;
import com.email.security.UserContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 文件夹 REST API —— 从 OA EmailFolder 相关接口迁移。
 */
@RestController
@RequestMapping("/folder")
public class FolderController {

    @PostMapping("/create")
    public R<List<InMailFolder>> create(@RequestBody Map<String, String> params) {
        Long userId = UserContextHolder.getUserId();
        String fileName = params.get("fileName");
        String type = params.get("type");

        InMailFolder f = new InMailFolder();
        f.setIdIfNew();
        f.setMemberId(userId);
        f.setFileName(fileName);
        f.setPath(fileName);
        f.setType(type);
        f.setIsDelete(0);
        f.setCreateDate(new java.sql.Timestamp(System.currentTimeMillis()));
        DBAgent.save(f);

        return listFolders(userId);
    }

    @PostMapping("/update")
    public R<List<InMailFolder>> update(@RequestBody Map<String, String> params) {
        Long folderId = Long.parseLong(params.get("id"));
        String newName = params.get("fileName");
        InMailFolder f = DBAgent.get(InMailFolder.class, folderId);
        if (f != null) { f.setFileName(newName); DBAgent.update(f); }
        return listFolders(UserContextHolder.getUserId());
    }

    @PostMapping("/delete")
    public R<List<InMailFolder>> delete(@RequestBody Map<String, String> params) {
        Long folderId = Long.parseLong(params.get("id"));
        InMailFolder f = DBAgent.get(InMailFolder.class, folderId);
        if (f != null) { f.setIsDelete(1); DBAgent.update(f); }
        return listFolders(UserContextHolder.getUserId());
    }

    @GetMapping("/list")
    public R<List<InMailFolder>> list() {
        return listFolders(UserContextHolder.getUserId());
    }

    @PostMapping("/move")
    public R<Void> move(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Object> ids = (List<Object>) params.get("affairIds");
        Long folderId = Long.parseLong(params.get("folderId").toString());
        InMailFolder f = DBAgent.get(InMailFolder.class, folderId);
        if (f == null) return R.fail("文件夹不存在");

        for (Object o : ids) {
            Long aid = Long.parseLong(o.toString());
            Map<String, Object> p = new HashMap<>(); p.put("id", aid); p.put("path", f.getPath());
            DBAgent.bulkUpdate("UPDATE InMailAffair SET path=:path WHERE id=:id", p);
        }
        return R.ok();
    }

    @PostMapping("/rule")
    public R<Void> updateRule(@RequestBody Map<String, String> params) {
        Long folderId = Long.parseLong(params.get("id"));
        String rule = params.get("rule");
        InMailFolder f = DBAgent.get(InMailFolder.class, folderId);
        if (f != null) { f.setRule(rule); DBAgent.update(f); }
        return R.ok();
    }

    @PostMapping("/applyRule")
    public R<Void> applyRule(@RequestBody Map<String, String> params) {
        Long folderId = Long.parseLong(params.get("id"));
        InMailFolder f = DBAgent.get(InMailFolder.class, folderId);
        if (f == null || f.getRule() == null) return R.ok();

        Long userId = UserContextHolder.getUserId();
        String rule = f.getRule();
        Map<String, Object> p = new HashMap<>();
        p.put("mid", userId);
        List<Object> affairs = DBAgent.find("FROM InMailAffair WHERE memberId=:mid AND path IS NULL AND delFlag=0", p);
        for (Object o : affairs) {
            com.email.entity.InMailAffair a = (com.email.entity.InMailAffair) o;
            if (a.getSubject() != null && a.getSubject().contains(rule)) {
                a.setPath(f.getPath());
                DBAgent.update(a);
            }
        }
        return R.ok();
    }

    private R<List<InMailFolder>> listFolders(Long memberId) {
        Map<String, Object> p = new HashMap<>();
        p.put("mid", memberId);
        p.put("del", 0);
        List<InMailFolder> list = DBAgent.find("FROM InMailFolder WHERE memberId=:mid AND isDelete=:del", p);
        return R.ok(list != null ? list : Collections.emptyList());
    }
}
