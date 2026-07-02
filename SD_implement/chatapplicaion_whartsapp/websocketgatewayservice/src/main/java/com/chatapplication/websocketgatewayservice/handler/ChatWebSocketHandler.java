package com.chatapplication.websocketgatewayservice.handler;

import com.chatapplication.websocketgatewayservice.service.AsyncDeliveryService;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    // Physically tracks open sockets on THIS physical node instance memory
    private final Map<String, WebSocketSession> localSessionRegistry = new ConcurrentHashMap<>();

    // Tracks active dynamic subscriptions to prevent duplicate topic registration leaks
    private final Map<String, MessageListener> activeListenersRegistry = new ConcurrentHashMap<>();

    private final RedisMessageListenerContainer redisContainer;
    private final AsyncDeliveryService asyncDeliveryService;

    public ChatWebSocketHandler(RedisMessageListenerContainer redisContainer,
                                AsyncDeliveryService asyncDeliveryService) {
        this.redisContainer = redisContainer;
        this.asyncDeliveryService = asyncDeliveryService;
    }

    // =========================================================================
    // ⭐ THE DYNAMIC TOPIC SUBSCRIPTION CREATION HOOK ⭐
    // =========================================================================
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();
        if (query == null || !query.contains("=")) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        String userId = query.split("=")[1]; // Extracts clean id (e.g. "pooja")
        localSessionRegistry.put(userId, session);

        // Formulate explicit topic format matching the stateless chat tier
        String userWiseTopic = "user:" + userId;

        // 1. Create a clean, isolated runtime message listener for this user
        MessageListener dynamicTopicListener = new MessageListener() {
            @Override
            public void onMessage(Message message, byte[] pattern) {
                // Convert bytes arriving from the Redis cluster into a string framework
                String receivedBody = new String(message.getBody());

                System.out.println("[Redis Channel Triggered] Target: " + userWiseTopic + " | Data: " + receivedBody);

                // Instantly hand off delivery execution onto the @Async Thread Pool task executor
                asyncDeliveryService.dispatchFrameToSocket(session, receivedBody);
            }
        };

        // 2. Instruct the runtime Redis container engine to dynamically allocate the channel subscription
        redisContainer.addMessageListener(dynamicTopicListener, new ChannelTopic(userWiseTopic));

        // Save listener context reference to clear memory allocations cleanly on disconnect
        activeListenersRegistry.put(userId, dynamicTopicListener);

        System.out.println(">>> SUCCESS: Created dynamic subscriber channel line in Redis for topic -> " + userWiseTopic);
    }

    // =========================================================================
    // 🧹 CLEANUP DE-ALLOCATION LOOP ON DROPPED SOCKET CONNECTION
    // =========================================================================
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String query = session.getUri().getQuery();
        if (query != null && query.contains("=")) {
            String userId = query.split("=")[1];

            localSessionRegistry.remove(userId);

            // Remove active topic listener from Redis memory allocation tracking rings
            MessageListener targetedListener = activeListenersRegistry.remove(userId);
            if (targetedListener != null) {
                String userWiseTopic = "user:" + userId;
                redisContainer.removeMessageListener(targetedListener, new ChannelTopic(userWiseTopic));
                System.out.println("<<< Purged dynamic subscriber thread for topic -> " + userWiseTopic);
            }
        }
    }

    public WebSocketSession getSessionByUserId(String userId) {
        return localSessionRegistry.get(userId);
    }
}