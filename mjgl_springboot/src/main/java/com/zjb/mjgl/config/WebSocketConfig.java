package com.zjb.mjgl.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类（基于 STOMP）
 * 作用：注册连接端点、配置消息代理，前端通过 /ws 建立连接后，可订阅 /topic/xxx 或 /user/queue/xxx 接收服务端推送。
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单内存消息代理，客户端可订阅以 /topic 或 /queue 开头的 destination
        config.enableSimpleBroker("/topic", "/queue");
        // 客户端发送消息时，以 /app 开头的 destination 会路由到 @MessageMapping 方法
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 前端通过 ws://host:port/ws 建立 SockJS+STOMP 连接（支持降级，兼容性好）
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
