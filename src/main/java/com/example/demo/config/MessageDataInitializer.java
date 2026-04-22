package com.example.demo.config;

import com.example.demo.mapper.MessageBoardMapper;
import com.example.demo.model.entity.MessageEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class MessageDataInitializer implements ApplicationRunner {

    private final MessageBoardMapper messageBoardMapper;

    @Override
    public void run(ApplicationArguments args) {
        if (messageBoardMapper.countMessages() > 0) {
            return;
        }

        List<MessageEntity> seedMessages = List.of(
                MessageEntity.builder()
                        .nickname("Alice")
                        .contact("alice@example.com")
                        .content("页面很清爽，期待后续支持置顶和筛选。")
                        .build(),
                MessageEntity.builder()
                        .nickname("Bob")
                        .contact("bob@example.com")
                        .content("接口字段命名很规范，前后端对接会轻松很多。")
                        .build(),
                MessageEntity.builder()
                        .nickname("Carol")
                        .contact("carol@example.com")
                        .content("建议列表页默认按时间倒序，便于查看最新留言。")
                        .build()
        );

        seedMessages.forEach(messageBoardMapper::insertMessage);
    }
}
