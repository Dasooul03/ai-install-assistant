package com.example.installassistant.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentLoaderTest {

    private final DocumentLoader documentLoader = new DocumentLoader();

    @Test
    void shouldSplitMarkdownByHeadings() throws IOException {
        Path tempFile = Files.createTempFile("test", ".md");
        String content = """
                ## 第一节 安装准备
                这是第一节的内容。需要安装 Docker 和 Docker Compose。
                
                ## 第二节 启动服务
                运行 docker-compose up -d 即可启动全部服务。
                
                ## 第三节 常见问题
                如果启动失败，请检查端口是否被占用。
                """;
        Files.writeString(tempFile, content);

        List<Document> documents = documentLoader.load(tempFile);

        assertThat(documents).hasSize(3);
        assertThat(documents.get(0).getText()).contains("安装准备");
        assertThat(documents.get(1).getText()).contains("启动服务");
        assertThat(documents.get(2).getText()).contains("常见问题");

        // 验证元数据
        assertThat(documents.get(0).getMetadata()).containsEntry("source", tempFile.getFileName().toString());
        assertThat(documents.get(0).getMetadata()).containsEntry("docType", "MANUAL");

        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldSplitLongTextBySlidingWindow() throws IOException {
        Path tempFile = Files.createTempFile("test", ".txt");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sb.append("这是一段很长的测试文本内容用于验证滑动窗口分段功能。");
        }
        Files.writeString(tempFile, sb.toString());

        List<Document> documents = documentLoader.load(tempFile);

        assertThat(documents.size()).isGreaterThan(1);
        for (Document doc : documents) {
            assertThat(doc.getText().length()).isLessThanOrEqualTo(512);
        }

        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldLoadFromText() {
        String text = """
                ## 安装手册
                准备工作：安装 Java 17 和 Docker。
                
                ## 配置说明
                修改 application.yml 中的数据库连接信息。
                """;

        List<Document> docs = documentLoader.loadFromText(text, "manual.md", "MANUAL");
        assertThat(docs).hasSize(2);
        assertThat(docs.get(0).getMetadata()).containsEntry("source", "manual.md");
        assertThat(docs.get(0).getMetadata()).containsEntry("docType", "MANUAL");
    }

    @Test
    void shouldDetectDocType() throws IOException {
        Path manual = Files.createTempFile("install-guide", ".md");
        Files.writeString(manual, "# 安装手册");
        List<Document> docs = documentLoader.load(manual);
        assertThat(docs.get(0).getMetadata().get("docType")).isEqualTo("MANUAL");
        Files.deleteIfExists(manual);
    }
}
