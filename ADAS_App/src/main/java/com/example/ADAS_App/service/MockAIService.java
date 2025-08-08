//package com.example.ADAS_App.service;
/*
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
public class MockAIService {

    private static final String DRIVER_UUID = "b52c7dc2-dc7e-4c55-9d6f-d9b9cd0128bd";
    private static final String BASE_URL = "http://localhost:8080";
    private static final String SESSION_URL = BASE_URL + "/api/sessions";
    private static final String WS_URL = BASE_URL + "/ws";

    private final RestTemplate restTemplate = new RestTemplate();
    private WebSocketStompClient stompClient;

    @PostConstruct
    public void init() {
        createSession();
        connectWebSocket();
    }

    /**
     * Create a driving session for the mock driver.

    private void createSession() {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("driverUuid", DRIVER_UUID);
            body.put("status", "ACTIVE");

            String response = restTemplate.postForObject(SESSION_URL, body, String.class);
            log.info("✅ Session created for driver {} : {}", DRIVER_UUID, response);
        } catch (Exception e) {
            log.error("❌ Failed to create session: {}", e.getMessage(), e);
        }
    }

    /**
     * Connect to WebSocket and subscribe to emotion updates.

    @Async
    public void connectWebSocket() {
        try {
            Transport webSocketTransport = new WebSocketTransport(new StandardWebSocketClient());
            this.stompClient = new WebSocketStompClient(new SockJsClient(Collections.singletonList(webSocketTransport)));

            StompSession stompSession = stompClient.connect(WS_URL, new StompSessionHandlerAdapter() {
                @Override
                public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                    log.info("✅ Connected to WebSocket server.");
                }

                @Override
                public void handleTransportError(StompSession session, Throwable exception) {
                    log.error("❌ WebSocket transport error", exception);
                }
            }).get();

            // Subscribe to emotion updates
            stompSession.subscribe("/topic/emotions", new StompFrameHandler() {
                @Override
                public Type getPayloadType(StompHeaders headers) {
                    return String.class; // Or DTO if your backend sends JSON
                }

                @Override
                public void handleFrame(StompHeaders headers, Object payload) {
                    log.info("📡 Emotion received: {}", payload);
                }
            });

            log.info("📡 Subscribed to /topic/emotions");

        } catch (InterruptedException | ExecutionException e) {
            log.error("❌ Failed to connect to WebSocket: {}", e.getMessage(), e);
        }
    }
}
*/
