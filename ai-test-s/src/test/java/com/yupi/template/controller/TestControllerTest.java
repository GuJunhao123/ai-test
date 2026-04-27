package com.yupi.template.controller;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class TestControllerTest {

    @Resource
    private ChatClient chatClient;

    @Test
    void testModel() {
        String prompt = "前辈，请和我交往吧！";

        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        log.info(response);
    }
}