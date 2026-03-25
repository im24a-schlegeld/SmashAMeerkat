package com.meerkat.smashameerkat;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class GameService {
    private final Random random = new Random();

    private final String[] keys = {"A", "S", "D", "J", "K"};
    private final Map<String, String> holes = new HashMap<>();

    private int score = 0;
    private String status = "START";

    public GameService() {
        clearBoard();
    }

    private void clearBoard() {
        for (String key : keys) {
            holes.put(key, "");
        }
    }

    public void startGame() {
        score = 0;
        status = "RUNNING";
        clearBoard();
        spawnTarget();
    }

    public void restartGame() {
        startGame();
    }

    public void spawnTarget() {
        clearBoard();

        int randomIndex = random.nextInt(keys.length);
        String selectedKey = keys[randomIndex];

        boolean impostor = random.nextDouble() < 0.2;
        holes.put(selectedKey, impostor ? "IMPOSTOR" : "MEERKAT");
    }

    public void hitKey(String key) {
        if (!"RUNNING".equals(status)) {
            return;
        }

        String upperKey = key.toUpperCase();

        if (!holes.containsKey(upperKey)) {
            return;
        }

        String target = holes.get(upperKey);

        if ("".equals(target)) {
            return;
        }

        if ("MEERKAT".equals(target)) {
            score++;
            holes.put(upperKey, "");
            spawnTarget();
            return;
        }

        if ("IMPOSTOR".equals(target)) {
            status = "GAME_OVER";
            clearBoard();
        }
    }
    
    public Map<String, String> getHoles() {
        return new HashMap<>(holes);
    }

    public int getScore() {
        return score;
    }

    public String getStatus() {
        return status;
    }
}
