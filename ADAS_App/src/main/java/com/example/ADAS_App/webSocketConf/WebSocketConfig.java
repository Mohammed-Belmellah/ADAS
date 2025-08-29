package com.example.ADAS_App.webSocketConf;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;


import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtDecoder jwtDecoder;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableStompBrokerRelay("/topic")
                .setRelayHost("localhost")
                .setRelayPort(61613)
                .setClientLogin("guest")
                .setClientPasscode("guest")
                .setSystemLogin("guest")
                .setSystemPasscode("guest")
                .setVirtualHost("/");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Allow all origins for dev
                .withSockJS();
        registry.addEndpoint("/ws-raw")          // plain WS for tools/mocks
                .setAllowedOriginPatterns("*");  // <-- no SockJS here

    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (acc != null && StompCommand.CONNECT.equals(acc.getCommand())) {
                    // Try both casings
                    String auth = firstNonNull(
                            acc.getFirstNativeHeader("Authorization"),
                            acc.getFirstNativeHeader("authorization")
                    );

                    if (auth != null && auth.startsWith("Bearer ")) {
                        String token = auth.substring(7);
                        try {
                            Jwt jwt = jwtDecoder.decode(token);     // inject JwtDecoder
                            Authentication a = new JwtAuthenticationToken(jwt);
                            acc.setUser(a);
                        } catch (JwtException e) {
                            // Don't hard fail CONNECT: log and leave user null (or reject if you want)
                            // throw new IllegalArgumentException("Invalid JWT"); // <- if you *want* to reject
                            System.out.println("STOMP CONNECT invalid JWT: " + e.getMessage());
                        }
                    }
                }
                return message;
            }
            private String firstNonNull(String a, String b) { return a != null ? a : b; }
        });
    }

}

