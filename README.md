# Smash A Meerkat

Smash A Meerkat ist ein kleines Reaktionsspiel, das als Gruppenprojekt im Java-Unterricht entstanden ist.

Auf dem Spielfeld erscheinen Erdmännchen an fünf verschiedenen Positionen. Die Positionen sind den Tasten `A`, `S`, `D`, `J` und `K` zugeordnet.

Ein getroffenes Erdmännchen erhöht den Punktestand. Wird ein Impostor getroffen, endet die Runde. Ein Schuss auf ein leeres Feld zieht einen Punkt ab.

<p align="center">
  <img src="docs/images/smash-a-meerkat-game.png" alt="Smash A Meerkat – laufendes Spiel" width="900">
</p>

## Umsetzung

Der Spielzustand wird im Spring-Boot-Backend verwaltet.

Die Eingaben aus dem Browser werden über eine WebSocket-Verbindung an den Server gesendet. Der Server verarbeitet die Eingaben und sendet den aktuellen Spielzustand wieder an die verbundenen Browser.

Aktuell verwenden alle verbundenen Clients denselben Spielzustand. Es gibt keine getrennte Spielrunde pro Spieler.

## Technik

- Java 17
- Spring Boot
- Spring MVC
- Thymeleaf
- Spring WebSocket
- Jackson
- Maven

## Lokal starten

Java 17 wird benötigt.

Linux / macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```bat
mvnw.cmd spring-boot:run
```

Danach:

```text
http://localhost:8080
```

## Tests

```bash
./mvnw test
```
