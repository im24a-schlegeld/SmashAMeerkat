# Smash A Meerkat

Smash A Meerkat is a browser-based reaction game inspired by Whac-A-Mole. The server owns the game state and pushes updates to connected browsers over WebSockets.

## Gameplay

A target appears in one of five holes mapped to the `A`, `S`, `D`, `J` and `K` keys. Hit a meerkat to increase the score. Hitting an impostor ends the game; deliberately shooting an empty hole removes one point. Targets move after one to two seconds, and a round can be started, paused or restarted.

## Features

- Server-side game state and scoring
- WebSocket state updates for connected clients
- Random target positions without immediate repeats in most rounds
- Meerkat and impostor targets
- Start, pause and restart controls
- Browser UI with game assets and keyboard controls

## Tech Stack

- Java 17
- Spring Boot
- Spring MVC and Thymeleaf
- Spring WebSocket
- Jackson
- Maven

## Running locally

The Maven Wrapper downloads the required Maven version automatically. Java 17 is required.

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bat
mvnw.cmd spring-boot:run
```

On Git Bash in Windows, `run-app.sh` also stops an existing instance before starting the app:

```bash
./run-app.sh
```

Then open http://localhost:8080.

## Project Structure

- `src/main/java/com/meerkat/smashameerkat/` contains the Spring Boot application, game service, controller and WebSocket handler.
- `src/main/resources/templates/` contains the Thymeleaf page.
- `src/main/resources/static/` contains JavaScript, CSS and game assets.
- `src/test/` contains the application context test.
