package com.email.controller;

import com.email.common.R;
import com.email.entity.EmailFolder;
import com.email.security.UserContextHolder;
import com.email.service.FolderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 自定义文件夹 API（从 OA EmailFolder 相关接口迁移）。
 */
@RestController
@RequestMapping("/folder")
public class EmailFolderController {

    @Resource private FolderService folderService;

    @GetMapping("/list")
    public R<List<EmailFolder>> list() {
        return R.ok(folderService.getFolders(UserContextHolder.getUserId()));
    }

    @PostMapping("/create")
    public R<List<EmailFolder>> create(@RequestBody Map<String, String> params) {
        Long userId = UserContextHolder.getUserId();
        String fileName = params.get("fileName");
        String type = params.get("type");
        String parentPath = params.get("parentPath");
        folderService.create(userId, fileName, type, parentPath);
        return R.ok(folderService.getFolders(userId));
    }

    @PostMapping("/update")
    public R<List<EmailFolder>> update(@RequestBody Map<String, String> params) {
        Long folderId = Long.parseLong(params.get("id"));
        String newName = params.get("fileName");
        folderService.rename(folderId, newName);
        return R.ok(folderService.getFolders(UserContextHolder.getUserId()));
    }

    @PostMapping("/delete")
    public R<List<EmailFolder>> delete(@RequestBody Map<String, String> params) {
        Long folderId = Long.parseLong(params.get("id"));
        folderService.delete(folderId);
        return R.ok(folderService.getFolders(UserContextHolder.getUserId()));
    }

    @PostMapping("/move")
    public R<Void> move(@RequestBody Map<String, Object> params) {
        @SuppressWarnings("unchecked")
        List<Integer> affairIdInts = (List<Integer>) params.get("affairIds");
        Long folderId = Long.parseLong(params.get("folderId").toString());
        List<Long> ids = affairIdInts.stream().map(Long::valueOf).collect(java.util.stream.Collectors.toList());
        folderService.moveMails(ids, folderId);
        return R.ok();
    }

    @PostMapping("/rule")
    public R<Void> updateRule(@RequestBody Map<String, String> params) {
        Long folderId = Long.parseLong(params.get("id"));
        String rule = params.get("rule");
        folderService.updateRule(folderId, rule);
        return R.ok();
    }

    @PostMapping("/applyRule")
    public R<Void> applyRule(@RequestBody Map<String, String> params) {
        Long folderId = Long.parseLong(params.get("id"));
        folderService.applyRuleToHistory(folderId);
        return R.ok();
    }
}
