package com.email.controller;

import com.email.common.R;
import com.email.service.AttachmentService;
import com.email.stub.OaCompat.Attachment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.*;

/** 附件 REST API - 上传/下载/列举/删除 */
@RestController
@RequestMapping("/attachment")
public class AttachmentController {

    @Resource private AttachmentService attachmentService;

    /** 上传附件 */
    @PostMapping("/upload")
    public R<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return R.fail("文件为空");
        Attachment att = attachmentService.upload(file);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", att.getId());
        data.put("fileName", att.getFilename());
        data.put("fileSize", att.getSize());
        data.put("mimeType", att.getMimeType());
        return R.ok(data);
    }

    /** 下载附件 (302重定向到MinIO预签名URL) */
    @GetMapping("/download/{id}")
    public ResponseEntity<Void> download(@PathVariable Long id) {
        String url = attachmentService.getDownloadUrl(id);
        if (url == null) return ResponseEntity.notFound().build();
        return ResponseEntity.status(302).header(HttpHeaders.LOCATION, url).build();
    }

    /** 获取邮件附件列表 */
    @GetMapping("/list/{summaryId}")
    public R<List<Attachment>> list(@PathVariable Long summaryId) {
        return R.ok(attachmentService.listBySummaryId(summaryId));
    }

    /** 删除附件 */
    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        attachmentService.deleteById(id);
        return R.ok();
    }
}
