package com.yupi.template.service.impl;

import com.yupi.template.service.AIScoringService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class AIScoringServiceImplTest {

    @Resource
    private AIScoringService aiScoringService;

    @Test
    void selectModel() {
        List<String> selectModels = aiScoringService.selectJudgeModels("OpenAI: GPT-4o Search Preview");

        log.info("selectModels: {}", selectModels);
    }
}