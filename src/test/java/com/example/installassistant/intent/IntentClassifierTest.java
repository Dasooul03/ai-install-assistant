package com.example.installassistant.intent;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.model.chat.ChatModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntentClassifierTest {

    @Mock
    private ChatModel chatModel;

    private IntentClassifier intentClassifier;

    @BeforeEach
    void setUp() {
        intentClassifier = new IntentClassifier(chatModel, new ObjectMapper());
    }

    @Test
    void shouldClassifyKnowledgeQA() {
        when(chatModel.chat(anyString())).thenReturn(
                "{\"type\":\"KNOWLEDGE_QA\",\"confidence\":0.92,\"parameters\":{}}");

        IntentResult result = intentClassifier.classify("如何安装 Docker？");

        assertThat(result.type()).isEqualTo(IntentType.KNOWLEDGE_QA);
        assertThat(result.confidence()).isEqualTo(0.92);
    }

    @Test
    void shouldClassifyCreateCluster() {
        when(chatModel.chat(anyString())).thenReturn(
                "{\"type\":\"CREATE_CLUSTER\",\"confidence\":0.95,\"parameters\":{\"nodeCount\":\"3\"}}");

        IntentResult result = intentClassifier.classify("创建一个3节点的集群");

        assertThat(result.type()).isEqualTo(IntentType.CREATE_CLUSTER);
        assertThat(result.parameters()).containsEntry("nodeCount", "3");
    }

    @Test
    void shouldClassifyAddInstance() {
        when(chatModel.chat(anyString())).thenReturn(
                "{\"type\":\"ADD_INSTANCE\",\"confidence\":0.90,\"parameters\":{\"serviceName\":\"user-service\",\"count\":\"5\"}}");

        IntentResult result = intentClassifier.classify("给user-service加5个实例");

        assertThat(result.type()).isEqualTo(IntentType.ADD_INSTANCE);
        assertThat(result.parameters()).containsEntry("serviceName", "user-service");
        assertThat(result.parameters()).containsEntry("count", "5");
    }

    @Test
    void shouldClassifyServiceLifecycle() {
        when(chatModel.chat(anyString())).thenReturn(
                "{\"type\":\"SERVICE_LIFECYCLE\",\"confidence\":0.88,\"parameters\":{\"serviceName\":\"order-service\",\"action\":\"restart\"}}");

        IntentResult result = intentClassifier.classify("重启order-service");

        assertThat(result.type()).isEqualTo(IntentType.SERVICE_LIFECYCLE);
        assertThat(result.parameters()).containsEntry("action", "restart");
    }

    @Test
    void shouldClassifyChitchat() {
        when(chatModel.chat(anyString())).thenReturn(
                "{\"type\":\"CHITCHAT\",\"confidence\":0.99,\"parameters\":{}}");

        IntentResult result = intentClassifier.classify("你好");

        assertThat(result.type()).isEqualTo(IntentType.CHITCHAT);
    }

    @Test
    void shouldFallbackToChitchatOnInvalidJson() {
        when(chatModel.chat(anyString())).thenReturn("invalid response that is not json");

        IntentResult result = intentClassifier.classify("something");

        assertThat(result.type()).isEqualTo(IntentType.CHITCHAT);
    }

    @Test
    void shouldFallbackToChitchatOnUnknownIntent() {
        when(chatModel.chat(anyString())).thenReturn(
                "{\"type\":\"UNKNOWN_TYPE\",\"confidence\":0.5,\"parameters\":{}}");

        IntentResult result = intentClassifier.classify("test");

        assertThat(result.type()).isEqualTo(IntentType.CHITCHAT);
    }

    @Test
    void shouldExtractJsonFromMarkdownCodeBlock() {
        when(chatModel.chat(anyString())).thenReturn(
                "```json\n{\"type\":\"DIAGNOSTIC\",\"confidence\":0.87,\"parameters\":{}}\n```");

        IntentResult result = intentClassifier.classify("启动报错了");

        assertThat(result.type()).isEqualTo(IntentType.DIAGNOSTIC);
    }
}
