const holes = Array.from(document.querySelectorAll(".hole"));
const statusText = document.getElementById("status");
const scoreText = document.getElementById("score");
const pistol = document.getElementById("pistol");
const effects = document.getElementById("effects");
const gameOver = document.getElementById("game-over");
const pauseButton = document.getElementById("pause-button");

let currentGameState = { status: "START", score: 0, holes: {} };
let displayedScore = 0;
let shotInProgress = false;

const shotArt = [
    { pistol: "/images/pistol-right-strong.png", patron: "/images/patron-right-strong.png", mirror: true },
    { pistol: "/images/pistol-right-light.png", patron: "/images/patron-right-light.png", mirror: true },
    { pistol: "/images/pistol-straight.png", patron: "/images/patron-straight.png", mirror: false },
    { pistol: "/images/pistol-right-light.png", patron: "/images/patron-right-light.png", mirror: false },
    { pistol: "/images/pistol-right-strong.png", patron: "/images/patron-right-strong.png", mirror: false }
];

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

    if (key === "P" && !shotInProgress && ["RUNNING", "PAUSED"].includes(currentGameState.status)) {
        socket.send("PAUSE");
        return;
    }

    if (["A", "S", "D", "J", "K"].includes(key)) {
        const holeIndex = holes.findIndex((hole) => hole.dataset.key === key);
        const target = String(currentGameState.holes?.[key] || "").toUpperCase();

        if (currentGameState.status !== "RUNNING" || shotInProgress) {
            return;
        }
        playShot(holeIndex, target);
        return;
    }
});

pauseButton.addEventListener("click", () => {
    if (socket.readyState === WebSocket.OPEN && !shotInProgress) {
        socket.send("PAUSE");
    }
});

function updateUI(gameState) {
    currentGameState = gameState;
    statusText.textContent = formatStatus(gameState.status);
    scoreText.textContent = `Score: ${gameState.score}`;
    if (gameState.score > displayedScore) {
        restartAnimation(scoreText, "score-pop");
    }
    displayedScore = gameState.score;
    gameOver.hidden = gameState.status !== "GAME_OVER";
    pauseButton.disabled = !["RUNNING", "PAUSED"].includes(gameState.status);
    pauseButton.textContent = gameState.status === "PAUSED" ? "Resume" : "Pause";
    updateHoles(gameState.holes ?? {});
}

function playShot(holeIndex, target) {
    const hole = holes[holeIndex];
    const art = shotArt[holeIndex];
    if (!hole || !art) return;

    shotInProgress = true;

    pistol.src = art.pistol;
    pistol.classList.toggle("mirrored", art.mirror);
    restartAnimation(pistol, "pistol-recoil");

    const sceneRect = effects.getBoundingClientRect();
    const targetRect = hole.getBoundingClientRect();
    const endX = targetRect.left + targetRect.width / 2 - sceneRect.left;
    const endY = targetRect.top + targetRect.height / 2 - sceneRect.top;
    const startX = sceneRect.width / 2;
    const startY = sceneRect.height * 0.9;

    const patron = document.createElement("img");
    patron.className = `patron${art.mirror ? " mirrored" : ""}`;
    patron.src = art.patron;
    patron.style.left = `${startX}px`;
    patron.style.top = `${startY}px`;
    effects.appendChild(patron);

    const horizontalScale = art.mirror ? -1 : 1;
    const flight = patron.animate([
        // Patron flight size: raise these scales for an even larger projectile.
        { transform: `translate(-50%, -50%) scale(${horizontalScale * 0.9}, .9)`, opacity: 1 },
        { transform: `translate(calc(-50% + ${endX - startX}px), calc(-50% + ${endY - startY}px)) scale(${horizontalScale * 0.48}, .48)`, opacity: 1 }
    ], { duration: 210, easing: "cubic-bezier(.2,.75,.25,1)", fill: "forwards" });

    flight.finished.then(() => {
        patron.remove();
        if (target === "MEERKAT") {
            // Replace the meerkat at the exact moment the patron reaches it.
            hole.dataset.state = "";
            showHitEffect(hole, target);
        }
        if (!target) {
            showMissEffect(hole);
            socket.send(`MISS:${hole.dataset.key}`);
        } else {
            socket.send(hole.dataset.key);
        }
        window.setTimeout(() => { shotInProgress = false; }, 120);
    });
}

function showMissEffect(hole) {
    const penalty = document.createElement("img");
    const rect = hole.getBoundingClientRect();
    penalty.className = "miss-penalty";
    penalty.src = "/images/minus-one.png";
    penalty.style.left = `${rect.left + rect.width / 2}px`;
    penalty.style.top = `${rect.top - 12}px`;
    effects.appendChild(penalty);
    penalty.addEventListener("animationend", () => penalty.remove(), { once: true });
}

function showHitEffect(hole, target) {
    if (target !== "MEERKAT") return;

    const steam = document.createElement("img");
    const rect = hole.getBoundingClientRect();
    steam.className = "steam-effect";
    steam.src = "/images/steam.png";
    // Match the steam width to the meerkat artwork in this particular hole.
    steam.style.width = `${rect.width * 0.9}px`;
    steam.style.left = `${rect.left + rect.width / 2}px`;
    // Use the same bottom anchor as the meerkat artwork instead of the hole center.
    steam.style.top = `${rect.bottom + rect.height * 0.1 - 30}px`;
    effects.appendChild(steam);
    steam.addEventListener("animationend", () => steam.remove(), { once: true });
}

function restartAnimation(element, className) {
    element.classList.remove(className);
    void element.offsetWidth;
    element.classList.add(className);
    element.addEventListener("animationend", () => element.classList.remove(className), { once: true });
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
