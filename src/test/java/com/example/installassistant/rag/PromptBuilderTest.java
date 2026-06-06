package com.example.installassistant.rag;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PromptBuilderTest {

    private final PromptBuilder promptBuilder = new PromptBuilder();

    @Test
    void shouldBuildPromptWithContext() {
        String context = "这是检索到的文档内容片段1\n这是检索到的文档内容片段2";
        String prompt = promptBuilder.buildSystemPrompt(context);

        assertThat(prompt).contains("智能安装助手");
        assertThat(prompt).contains(context);
        assertThat(prompt).doesNotContain("{context}"); // 模板变量已被替换
    }

    @Test
    void shouldHandleEmptyContext() {
        String prompt = promptBuilder.buildSystemPrompt("");

        assertThat(prompt).contains("智能安装助手");
        assertThat(prompt).doesNotContain("{context}");
    }
}
