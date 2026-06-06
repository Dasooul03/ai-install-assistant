package com.example.installassistant;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        properties = {
                "spring.ai.vectorstore.milvus.initialize-schema=false",
                "spring.autoconfigure.exclude=" +
                        "org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreAutoConfiguration",
                // 测试环境使用 H2 内存数据库
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect"
        }
)
@ActiveProfiles("test")
class AiInstallAssistantApplicationTests {

    @MockBean
    private VectorStore vectorStore;

    @Test
    void contextLoads() {
        // 验证 Spring 上下文能正常加载
    }
}
