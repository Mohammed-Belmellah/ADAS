package com.example.ADAS_App.webSocketConf;

import com.example.ADAS_App.DTOs.EmotionRecordBatchDTO;
import com.example.ADAS_App.DTOs.EmotionRecordDTO;
import com.example.ADAS_App.service.IEmotionRecordService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;

import org.springframework.stereotype.Controller;


import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class EmotionRecordWebSocketController {

    private final IEmotionRecordService emotionRecordService;
    private final ObjectMapper objectMapper;

    @MessageMapping("/emotions/batch")
    @SendTo("/topic/emotions")
    public List<EmotionRecordDTO> receiveEmotionBatch(@Payload EmotionRecordBatchDTO batchDTO) throws Exception {
        log.info("Parsed batch DTO: sessionId={}, recordCount={}",
                batchDTO.getSessionId(), batchDTO.getRecords().size());

        List<EmotionRecordDTO> result = emotionRecordService.saveBatch(batchDTO);
        log.info("Successfully saved {} emotion records", result.size());

        return result;
}}


