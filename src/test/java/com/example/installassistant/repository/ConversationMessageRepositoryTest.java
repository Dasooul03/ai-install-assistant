package com.example.installassistant.repository;

import com.example.installassistant.model.ConversationMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ConversationMessageRepositoryTest {

    @Autowired
    private ConversationMessageRepository messageRepository;

    @Test
    void shouldAppendAndRetrieveMessages() {
        ConversationMessage msg1 = ConversationMessage.builder()
                .sessionId(1L).role("USER").content("你好").build();
        ConversationMessage msg2 = ConversationMessage.builder()
                .sessionId(1L).role("ASSISTANT").content("你好！有什么可以帮助你的？").build();
        messageRepository.save(msg1);
        messageRepository.save(msg2);

        List<ConversationMessage> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(1L);

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getRole()).isEqualTo("USER");
        assertThat(messages.get(1).getRole()).isEqualTo("ASSISTANT");
    }
}
