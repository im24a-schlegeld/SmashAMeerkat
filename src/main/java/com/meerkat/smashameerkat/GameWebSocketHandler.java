package com.meerkat.smashameerkat;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    private final GameService gameService;

    public GameWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sendGameState(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        if ("START".equalsIgnoreCase(payload)) {
            gameService.startGame();
        } else if ("RESTART".equalsIgnoreCase(payload)) {
            gameService.restartGame();
        } else {
            gameService.hitKey(payload);
        }

        sendGameState(session);
    }

    private void sendGameState(WebSocketSession session) throws Exception {
        String gameState =
            "status=" + gameService.getStatus() +
            ";score=" + gameService.getScore() +
            ";holes=" + gameService.getHoles();
        
        session.sendMessage(new TextMessage(gameState));
    }
}
