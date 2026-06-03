package com.email.controller;

import com.email.common.R;
import com.email.entity.EmailAttachment;
import com.email.exception.BusinessException;
import com.email.service.AttachmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 附件 API（替代 OA /seeyon/rest/attachment）。
 *
 * <p>附件存储在 MinIO，下载通过预签名 URL 重定向。</p>
 */
@Slf4j
@RestController
@RequestMapping("/attachment")
@Tag(name = "附件管理", description = "附件上传下载")
public class AttachmentController {

    @Resource private AttachmentService attachmentService;

    @PostMapping("/upload")
    @Operation(summary = "上传附件")
    public R<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "summaryId", required = false) Long summaryId) {
        if (file.isEmpty()) return R.fail("文件为空");

        EmailAttachment att = attachmentService.upload(file, summaryId);
        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("id", att.getId());
        data.put("fileName", att.getFileName());
        data.put("fileSize", att.getFileSize());
        data.put("mimeType", att.getMimeType());
        return R.ok(data);
    }

    @GetMapping("/download/{attachmentId}")
    @Operation(summary = "下载附件（重定向到 MinIO 预签名 URL）")
    public ResponseEntity<Void> download(@PathVariable Long attachmentId) {
        String url = attachmentService.getDownloadUrl(attachmentId);
        return ResponseEntity.status(302).header(HttpHeaders.LOCATION, url).build();
    }

    @GetMapping("/download/direct/{attachmentId}")
    @Operation(summary = "直接下载附件（服务端中转，适用于需要权限控制的场景）")
    public ResponseEntity<byte[]> downloadDirect(@PathVariable Long attachmentId) throws IOException {
        EmailAttachment att = attachmentService.getById(attachmentId);
        if (att == null) return ResponseEntity.notFound().build();

        InputStream is = attachmentService.download(attachmentId);
        byte[] bytes = is.readAllBytes();
        is.close();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + att.getFileName() + "\"")
                .body(bytes);
    }

    @GetMapping("/list/{summaryId}")
    @Operation(summary = "获取某封邮件的附件列表")
    public R<List<EmailAttachment>> list(@PathVariable Long summaryId) {
        return R.ok(attachmentService.listBySummaryId(summaryId));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除附件")
    public R<Void> delete(@RequestBody Map<String, List<Long>> params) {
        List<Long> ids = params.get("ids");
        if (ids == null || ids.isEmpty()) return R.fail("参数缺失");
        attachmentService.deleteByIds(ids);
        return R.ok();
    }
}
