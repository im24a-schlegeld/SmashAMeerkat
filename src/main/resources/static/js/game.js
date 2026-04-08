const holes = Array.from(document.querySelectorAll(".hole"));
const statusText = document.getElementById("status");
const scoreText = document.getElementById("score");

// Match the page protocol so local HTTP and deployed HTTPS both connect cleanly.
const socketProtocol = window.location.protocol === "https:" ? "wss:" : "ws:";
const socket = new WebSocket(`${socketProtocol}//${window.location.host}/game`);

socket.addEventListener("open", () => {
    statusText.textContent = "Press R To Start";
});

socket.addEventListener("message", (event) => {
    const gameState = parseGameState(event.data);
    updateUI(gameState);
});

socket.addEventListener("close", () => {
    statusText.textContent = "Disconnected";
});

socket.addEventListener("error", () => {
    statusText.textContent = "Connection error";
});

// Forward only the controls the backend understands.
document.addEventListener("keydown", (event) => {
    if (socket.readyState !== WebSocket.OPEN) {
        return;
    }

    const key = event.key.toUpperCase();

    if (key === "R") {
        socket.send("START");
        return;
    }

    if (["A", "S", "D", "J", "K"].includes(key)) {
        socket.send(key);
    }
});

function updateUI(gameState) {
    statusText.textContent = formatStatus(gameState.status);
    scoreText.textContent = `Score: ${gameState.score}`;
    updateHoles(gameState.holes ?? {});
}

function parseGameState(payload) {
    try {
        // Prefer the current JSON payload format.
        return JSON.parse(payload);
    } catch {
        // Fall back to the older semicolon-separated format for compatibility.
        const parts = payload.split(";");
        return {
            status: parts[0]?.replace("status=", "") ?? "START",
            score: Number(parts[1]?.replace("score=", "") ?? 0),
            holes: parseLegacyHoles(parts[2]?.replace("holes=", "") ?? "")
        };
    }
}

function parseLegacyHoles(holesPart) {
    const holesState = {};
    const clean = holesPart.replace("{", "").replace("}", "");

    // Convert key=value pairs into the hole state map used by the UI.
    for (const entry of clean.split(",")) {
        const [key, value = ""] = entry.trim().split("=");
        if (key) {
            holesState[key] = value;
        }
    }

    return holesState;
}

function formatStatus(status) {
    if (!status) {
        return "Start";
    }

    return status
        .toLowerCase()
        .replace(/_/g, " ")
        .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function updateHoles(holesState) {
    // Keep DOM state and accessibility labels in sync with the server state.
    for (const hole of holes) {
        const key = hole.dataset.key;
        const value = String(holesState[key] || "").toLowerCase();
        hole.dataset.state = value;
        hole.setAttribute("aria-label", value ? `${key} hole ${value}` : `${key} hole empty`);
    }
}
