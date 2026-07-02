package com.chatapplication.websocketgatewayservice.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;

@Service
public class AsyncDeliveryService {

    @Async("wsDeliveryExecutor")
    public void dispatchFrameToSocket(WebSocketSession session, String textMessage) {
        try {
            if (session != null && session.isOpen()) {
                session.sendMessage(new TextMessage(textMessage));
                System.out.println("[" + Thread.currentThread().getName() + "] Asynchronously delivered packet payload.");
            }
        } catch (IOException e) {
            System.err.println("Async dispatch crashed: " + e.getMessage());
        }
    }
}