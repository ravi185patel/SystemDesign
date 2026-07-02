package com.chatapplication.websocketgatewayservice.receiver;

import com.chatapplication.websocketgatewayservice.handler.ChatWebSocketHandler;
import com.chatapplication.websocketgatewayservice.service.AsyncDeliveryService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Component
public class RedisMessageReceiver {

    private final ChatWebSocketHandler socketHandler;
    private final AsyncDeliveryService deliveryService;

    public RedisMessageReceiver(@Lazy ChatWebSocketHandler socketHandler, AsyncDeliveryService deliveryService) {
        this.socketHandler = socketHandler;
        this.deliveryService = deliveryService;
    }

    public void receiveBroadcastPayload(String payload) {
        // Wire contract layout format -> "targetRecipientUserId:TextFrameStringContent"
        String[] segments = payload.split(":", 2);
        String targetRecipientUserId = segments[0];
        String textFrameStringContent = segments[1];

        WebSocketSession session = socketHandler.getSessionByUserId(targetRecipientUserId);
        if (session != null) {
            // Hand off execution immediately to prevent network thread starvation
            deliveryService.dispatchFrameToSocket(session, textFrameStringContent);
        }
    }
}