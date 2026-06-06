package com.example.installassistant.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * AI 模型配置 — 当 Embedding/Milvus 不可用时提供 Stub
 * 聊天功能由 LangChain4j 提供, 不受影响
 */
@Slf4j
@Configuration
public class AiConfig {

    public AiConfig() {
        log.info("[AiConfig] Initialized — stub mode for embedding/vector-store");
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(EmbeddingModel.class)
    public EmbeddingModel stubEmbeddingModel() {
        log.warn("[AiConfig] Using STUB EmbeddingModel — RAG disabled, chat-only mode");
        return new EmbeddingModel() {
            @Override
            public EmbeddingResponse call(EmbeddingRequest request) {
                return new EmbeddingResponse(List.of());
            }

            @Override
            public float[] embed(Document document) {
                return new float[0];
            }
        };
    }

    @Bean
    @Primary
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore stubVectorStore() {
        log.warn("[AiConfig] Using STUB VectorStore — RAG retrieval disabled");
        return new VectorStore() {

            @Override
            public void add(List<Document> documents) {
                log.debug("[StubVectorStore] add({} docs)", documents.size());
            }

            @Override
            public void delete(List<String> idList) {
                log.debug("[StubVectorStore] delete({} ids)", idList.size());
            }

            @Override
            public void delete(org.springframework.ai.vectorstore.filter.Filter.Expression filterExpression) {
                log.debug("[StubVectorStore] delete by filter");
            }

            @Override
            public List<Document> similaritySearch(String query) {
                return List.of();
            }

            @Override
            public List<Document> similaritySearch(org.springframework.ai.vectorstore.SearchRequest request) {
                return List.of();
            }
        };
    }
}
