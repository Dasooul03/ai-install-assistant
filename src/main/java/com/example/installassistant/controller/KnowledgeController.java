package com.example.installassistant.controller;

import com.example.installassistant.model.KnowledgeDocument;
import com.example.installassistant.rag.DocumentLoader;
import com.example.installassistant.rag.KnowledgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 知识库管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    private final KnowledgeService knowledgeService;
    private final DocumentLoader documentLoader;

    public KnowledgeController(KnowledgeService knowledgeService, DocumentLoader documentLoader) {
        this.knowledgeService = knowledgeService;
        this.documentLoader = documentLoader;
    }

    /**
     * 上传文本内容
     */
    @PostMapping(value = "/upload/text", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> uploadText(@RequestBody UploadTextRequest request) {
        log.info("[KnowledgeController] Upload text: fileName={}, docType={}",
                request.fileName(), request.docType());
        KnowledgeDocument doc = knowledgeService.uploadText(
                request.content(), request.fileName(), request.docType());
        return Map.of("success", true, "document", doc);
    }

    /**
     * 上传文件（支持 .md / .txt）
     */
    @PostMapping(value = "/upload/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> uploadFile(@RequestParam("file") MultipartFile file,
                                          @RequestParam(value = "docType", defaultValue = "OTHER") String docType) {
        log.info("[KnowledgeController] Upload file: name={}, size={}", file.getOriginalFilename(), file.getSize());

        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "uploaded_file";

            KnowledgeDocument doc = knowledgeService.uploadText(content, fileName, docType);
            return Map.of("success", true, "document", doc);
        } catch (IOException e) {
            log.error("[KnowledgeController] Failed to read uploaded file", e);
            return Map.of("success", false, "error", "文件读取失败: " + e.getMessage());
        }
    }

    /**
     * 删除知识文档
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> delete(@PathVariable Long id) {
        log.info("[KnowledgeController] Delete document: id={}", id);
        try {
            knowledgeService.delete(id);
            return Map.of("success", true, "message", "文档已删除");
        } catch (Exception e) {
            log.error("[KnowledgeController] Failed to delete document: id={}", id, e);
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    /**
     * 获取知识文档列表
     */
    @GetMapping("/list")
    public List<KnowledgeDocument> list() {
        log.info("[KnowledgeController] Listing knowledge documents");
        return knowledgeService.listAll();
    }

    // ===== DTOs =====

    public record UploadTextRequest(String content, String fileName, String docType) {}
}
