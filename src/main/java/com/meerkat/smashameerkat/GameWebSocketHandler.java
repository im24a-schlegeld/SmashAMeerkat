package com.meerkat.smashameerkat;

import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Bridges WebSocket messages from the browser to the game service.
 */
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    private final GameService gameService;
    // Track connected clients so updates can be broadcast to everyone.
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public GameWebSocketHandler(GameService gameService) {
        this.gameService = gameService;
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Add the new client and immediately send the current game state.
        sessions.add(session);
        sendGameState(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // Control messages manage the game; everything else is treated as a key hit.
        if ("START".equalsIgnoreCase(payload)) {
            gameService.startGame();
        } else if ("RESTART".equalsIgnoreCase(payload)) {
            gameService.restartGame();
        } else if ("PAUSE".equalsIgnoreCase(payload)) {
            gameService.togglePause();
        } else if (payload.toUpperCase().startsWith("MISS:")) {
            gameService.missKey(payload.substring(5));
        } else {
            gameService.hitKey(payload);
        }

        broadcastGameState();
    }

    /**
     * Polls the game timer and pushes updates whenever a target expires.
     */
    @Scheduled(fixedRate = 100)
    public void gameTick() throws Exception {
        if (gameService.advanceGameIfDue()) {
            broadcastGameState();
        }
    }

    // Build the JSON payload expected by the frontend and send it to one client.
    private void sendGameState(WebSocketSession session) throws Exception {
        Map<String, Object> gameState = new HashMap<>();
        gameState.put("status", gameService.getStatus());
        gameState.put("score", gameService.getScore());
        gameState.put("holes", gameService.getHoles());

        String json = objectMapper.writeValueAsString(gameState);
        session.sendMessage(new TextMessage(json));
    }

    // Push the latest game snapshot to every connected and still-open session.
    private void broadcastGameState() throws Exception {
        for (WebSocketSession session : sessions) {
            if (!session.isOpen()) {
                sessions.remove(session);
                continue;
            }

            sendGameState(session);
        }
    }
}
