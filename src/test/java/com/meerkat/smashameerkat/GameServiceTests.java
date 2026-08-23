package com.meerkat.smashameerkat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.Test;

class GameServiceTests {

    @Test
    void startingAGameResetsTheScore() throws Exception {
        GameService service = new GameService();
        setTarget(service, "A", "MEERKAT");
        service.hitKey("A");

        service.startGame();

        assertEquals("RUNNING", service.getStatus());
        assertEquals(0, service.getScore());
    }

    @Test
    void invalidKeysDoNotChangeTheGame() throws Exception {
        GameService service = new GameService();
        setTarget(service, "A", "MEERKAT");

        service.hitKey("X");
        service.missKey("X");

        assertEquals("RUNNING", service.getStatus());
        assertEquals(0, service.getScore());
        assertEquals("MEERKAT", service.getHoles().get("A"));
    }

    @Test
    void hittingAMeerkatIncreasesTheScore() throws Exception {
        GameService service = new GameService();
        setTarget(service, "A", "MEERKAT");

        service.hitKey("A");

        assertEquals(1, service.getScore());
        assertEquals("RUNNING", service.getStatus());
    }

    @Test
    void hittingAnImpostorEndsTheGame() throws Exception {
        GameService service = new GameService();
        setTarget(service, "A", "IMPOSTOR");

        service.hitKey("A");

        assertEquals("GAME_OVER", service.getStatus());
        assertEquals("", service.getHoles().get("A"));
    }

    @Test
    void missesReduceTheScoreButNeverBelowZero() throws Exception {
        GameService service = new GameService();
        setTarget(service, "A", "MEERKAT");
        service.hitKey("A");

        service.missKey("S");
        service.missKey("S");

        assertEquals(0, service.getScore());
    }

    @Test
    void pausePreventsHitsUntilTheGameResumes() throws Exception {
        GameService service = new GameService();
        setTarget(service, "A", "MEERKAT");

        service.togglePause();
        service.hitKey("A");
        assertEquals("PAUSED", service.getStatus());
        assertEquals(0, service.getScore());

        service.togglePause();
        assertEquals("RUNNING", service.getStatus());
    }

    @Test
    void holeSnapshotsCannotMutateTheGameState() throws Exception {
        GameService service = new GameService();
        setTarget(service, "A", "MEERKAT");

        Map<String, String> snapshot = service.getHoles();
        snapshot.put("A", "IMPOSTOR");

        assertNotEquals("IMPOSTOR", service.getHoles().get("A"));
    }

    @SuppressWarnings("unchecked")
    private static void setTarget(GameService service, String key, String target) throws Exception {
        Field holesField = GameService.class.getDeclaredField("holes");
        holesField.setAccessible(true);
        Map<String, String> holes = (Map<String, String>) holesField.get(service);
        holes.replaceAll((ignored, value) -> "");
        holes.put(key, target);

        Field statusField = GameService.class.getDeclaredField("status");
        statusField.setAccessible(true);
        statusField.set(service, "RUNNING");
    }
}
