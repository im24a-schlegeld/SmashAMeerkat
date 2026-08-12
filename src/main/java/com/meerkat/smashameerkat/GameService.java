package com.meerkat.smashameerkat;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

@Service
public class GameService {
    // Control how long a spawned target stays visible before the board advances.
    private static final long MIN_TARGET_VISIBLE_MS = 1000;
    private static final long MAX_TARGET_VISIBLE_MS = 2000;

    private final Random random = new Random();

    // Map each keyboard key to the target currently shown in that hole.
    private final String[] keys = {"A", "S", "D", "J", "K"};
    private final Map<String, String> holes = new HashMap<>();
    private String previousSpawnKey;

    // Mutable state for the current round.
    private int score = 0;
    private String status = "START";
    private long nextAdvanceAt = Long.MAX_VALUE;
    private long pausedRemainingMs;

    public GameService() {
        clearBoard();
    }

    // Reset every hole so nothing is visible on the board.
    private void clearBoard() {
        for (String key : keys) {
            holes.put(key, "");
        }
    }

    /**
     * Starts a fresh game and spawns the first target.
     */
    public synchronized void startGame() {
        score = 0;
        status = "RUNNING";
        clearBoard();
        spawnTarget();
    }

    /**
     * Restarts by delegating to the normal game start flow.
     */
    public synchronized void restartGame() {
        startGame();
    }

    /** Freezes or resumes the current target timer. */
    public synchronized void togglePause() {
        if ("RUNNING".equals(status)) {
            pausedRemainingMs = Math.max(0, nextAdvanceAt - System.currentTimeMillis());
            status = "PAUSED";
            nextAdvanceAt = Long.MAX_VALUE;
        } else if ("PAUSED".equals(status)) {
            status = "RUNNING";
            nextAdvanceAt = System.currentTimeMillis() + pausedRemainingMs;
        }
    }

    // Pick a random hole, decide the target type, and schedule the next board change.
    private void spawnTarget() {
        clearBoard();

        String selectedKey;
        if (previousSpawnKey != null && random.nextDouble() >= 0.01) {
            // In 99% of spawns, choose evenly from the other four holes.
            do {
                selectedKey = keys[random.nextInt(keys.length)];
            } while (selectedKey.equals(previousSpawnKey));
        } else if (previousSpawnKey != null) {
            // Allow an immediate repeat only 1% of the time.
            selectedKey = previousSpawnKey;
        } else {
            selectedKey = keys[random.nextInt(keys.length)];
        }
        previousSpawnKey = selectedKey;

        boolean impostor = random.nextDouble() < 0.2;
        holes.put(selectedKey, impostor ? "IMPOSTOR" : "MEERKAT");
        
        long visibleDuration = random.nextLong(MIN_TARGET_VISIBLE_MS, MAX_TARGET_VISIBLE_MS + 1);
        nextAdvanceAt = System.currentTimeMillis() + visibleDuration;
    }

    /**
     * Advances the board only when the current target has expired.
     */
    public synchronized boolean advanceGameIfDue() {
        if (!"RUNNING".equals(status)) {
            return false;
        }

        if (System.currentTimeMillis() < nextAdvanceAt) {
            return false;
        }

        spawnTarget();
        return true;
    }

    /**
     * Applies a player key press to the current board state.
     */
    public synchronized void hitKey(String key) {
        if (!"RUNNING".equals(status)) {
            return;
        }

        if (key == null) {
            return;
        }

        String upperKey = key.toUpperCase(Locale.ROOT);

        if (!holes.containsKey(upperKey)) {
            return;
        }

        String target = holes.get(upperKey);

        if ("".equals(target)) {
            return;
        }

        // Hitting a real meerkat scores and immediately spawns the next target.
        if ("MEERKAT".equals(target)) {
            score++;
            holes.put(upperKey, "");
            spawnTarget();
            return;
        }

        // Hitting an impostor ends the game and clears the board.
        if ("IMPOSTOR".equals(target)) {
            status = "GAME_OVER";
            nextAdvanceAt = Long.MAX_VALUE;
            clearBoard();
        }
    }

    /**
     * Applies the fixed one-point penalty for deliberately shooting an empty hole.
     */
    public synchronized void missKey(String key) {
        if (!"RUNNING".equals(status) || key == null) {
            return;
        }

        String upperKey = key.toUpperCase(Locale.ROOT);
        if (holes.containsKey(upperKey)) {
            score = Math.max(0, score - 1);
        }
    }
    
    /**
     * Returns a snapshot of the current holes so callers cannot mutate internal state.
     */
    public synchronized Map<String, String> getHoles() {
        return new HashMap<>(holes);
    }

    /**
     * Returns the player's current score.
     */
    public synchronized int getScore() {
        return score;
    }

    /**
     * Returns the current lifecycle state of the game.
     */
    public synchronized String getStatus() {
        return status;
    }
}
