package com.yupi.template.controller;

import com.yupi.template.job.SyncModelJob;
import com.yupi.template.model.vo.ModelVO;
import com.yupi.template.service.ModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class ModelControllerTest {

    @Resource
    private ModelService modelService;

    @Resource
    private SyncModelJob syncModelJob;

    @Test
    void getAllModels() {
        syncModelJob.syncModels();

        List<ModelVO> models = modelService.getAllModels(3L);

        log.info(models.toString());
    }
}