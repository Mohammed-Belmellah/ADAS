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

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class EmotionRecordWebSocketController {

    private final IEmotionRecordService emotionRecordService;
    private final SimpMessagingTemplate messagingTemplate;   // <—

    @MessageMapping("/emotions/batch")
    public void receiveEmotionBatch(@Payload EmotionRecordBatchDTO batchDTO) {
        UUID sessionId = batchDTO.getSessionId();
        if (sessionId == null) {
            throw new IllegalArgumentException("sessionId is required in batch payload");
        }


        // 1) Persist
        var saved = emotionRecordService.saveBatch(batchDTO);

        // 2) Broadcast to the session-scoped topic
        String destination = "/topic/emotions.session." + sessionId; // dot topics for RabbitMQ
        messagingTemplate.convertAndSend(destination, saved);

        log.info("Broadcasted {} records to {}", saved.size(), destination);

        // (optional) also broadcast by driver/company if you want rollups:
        // String driverDest = "topic.emotions.driver." + driverId;
        // messagingTemplate.convertAndSend(driverDest, saved);
    }

}