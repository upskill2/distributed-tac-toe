# Distributed Tic Tac Toe

A distributed Tic Tac Toe application built as microservices using Spring Boot 4.1.0 and Spring Cloud 2025.1.2.
The game runs as an automated simulation — a session service drives random moves through the game engine
until the game ends, then the UI replays the board move-by-move.

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Browser (UI)                      │
└─────────────────────┬───────────────────────────────┘
                      │ HTTP :8080
┌─────────────────────▼───────────────────────────────┐
│               Gateway  (:8080)                       │
│   /api/games/**  → game-engine-service-app           │
│   /api/sessions/** → game-session-service-app        │
│   /**            → ui-service-app                    │
└────────┬─────────────────┬────────────────┬─────────┘
         │                 │                │
┌────────▼──────┐ ┌────────▼──────┐ ┌──────▼────────┐
│ game-engine   │ │ game-session  │ │  ui-service   │
│   (:8081)     │ │   (:8082)     │ │   (:8083)     │
└───────────────┘ └───────────────┘ └───────────────┘
         ▲                 │
         └─────────────────┘
           lb:// (Eureka)

┌──────────────────┐   ┌──────────────────┐
│  config-server   │   │  eureka-server   │
│    (:8888)       │   │    (:8761)       │
└──────────────────┘   └──────────────────┘
```

| Module                   | Port | Role                                           |
|--------------------------|------|------------------------------------------------|
| `config-server`          | 8888 | Spring Cloud Config Server (git backend)       |
| `eureka-server`          | 8761 | Netflix Eureka service registry                |
| `gateway`                | 8080 | Spring Cloud Gateway MVC — routes all traffic  |
| `game-engine-service-app`| 8081 | Core game logic: board, moves, win detection   |
| `game-session-service-app`| 8082| Session management + simulation loop           |
| `ui-service-app`         | 8083 | Serves static `index.html`                     |
| `configuration`          | —    | YAML config files read by config-server        |

---

## Prerequisites

- Java 25
- Maven 3.9+

---

## Running the Application

Services must start in this order (each depends on the previous):

```bash
# 1. Config Server — other services fetch their config from here
mvn spring-boot:run -pl config-server

# 2. Eureka Server — service registry
mvn spring-boot:run -pl eureka-server

# 3. Game Engine — core game logic
mvn spring-boot:run -pl game-engine-service-app

# 4. Game Session — simulation orchestration
mvn spring-boot:run -pl game-session-service-app

# 5. UI Service — static frontend
mvn spring-boot:run -pl ui-service-app

# 6. Gateway — reverse proxy (start last)
mvn spring-boot:run -pl gateway
```

### Verify startup

- Config Server: http://localhost:8888/game-engine-service-app/default — returns JSON with port 8081
- Eureka Dashboard: http://localhost:8761 — shows all 4 client services registered
- Application UI: http://localhost:8080 — shows the game board

---

## How It Works

1. Open http://localhost:8080 and click **▶ Start Simulation**
2. The browser POSTs to `/api/sessions` — the session service creates a game via the engine and persists a session
3. The browser POSTs to `/api/sessions/{id}/simulate` — the session service:
   - Fetches game state from the engine
   - Picks a random available position
   - Calls the engine to make the move
   - Repeats until the game ends (win or draw)
4. The board animates move-by-move (500 ms per cell); the winning line is highlighted in yellow

---

## API Reference

### game-engine-service-app (via gateway as `/api/games/...`)

| Method | Path                     | Description        | Response        |
|--------|--------------------------|--------------------|-----------------|
| POST   | `/games`                 | Create a new game  | 201 + game JSON |
| POST   | `/games/{gameId}/move`   | Make a move        | 200 + game JSON |
| GET    | `/games/{gameId}`        | Get game state     | 200 + game JSON |

### game-session-service-app (via gateway as `/api/sessions/...`)

| Method | Path                             | Description            | Response           |
|--------|----------------------------------|------------------------|--------------------|
| POST   | `/sessions`                      | Create a new session   | 201 + session JSON |
| POST   | `/sessions/{sessionId}/simulate` | Run the simulation     | 200 + result JSON  |
| GET    | `/sessions/{sessionId}`          | Get session state      | 200 + session JSON |

---

## Configuration

Config files are served from the `configuration/` directory of this repository via Spring Cloud Config Server
(git backend, `develop` branch). Each service fetches its config on startup.

Default profile (H2 in-memory) config files:

```
configuration/
  game-engine-service-app.yaml     # port 8081, H2 in-memory
  game-session-service-app.yaml    # port 8082, H2 in-memory
  ui-service-app.yaml              # port 8083
  gateway.yaml                     # port 8080
```

---

## Enhancements

### Data Persistence

By default, both `game-engine-service-app` and `game-session-service-app` use H2 in-memory database. This is for local run and testing.
In order to address persistence enhancement, i added support for PostgresQL.
This is done via SpringBoot profiling, related dependency in pom.xml and config files in configuration module.
In order to run:
    - make sure postgres is running in your env (docker or self hosted)
    - run backend applications with env var: SPRING_PROFILES_ACTIVE=prod 

```
configuration/
  game-engine-service-app-prod.yaml
  game-session-service-app-prod.yaml
```

### Real-Time UI with Server-Sent Events (SSE)

The simulate endpoint (`POST /sessions/{sessionId}/simulate`) returns a `SseEmitter` instead of a plain JSON response.
The endpoint is called **once per simulation** — Spring holds the HTTP connection open and the session service pushes individual move events down that single connection as the simulation loop progresses.
The browser consumes these events incrementally using `fetch` + `ReadableStream` (rather than `EventSource`, which only supports GET).

Each `move` event carries the full game state (board, status, current player) as JSON.
A final `done` event signals that the simulation is complete, after which the connection closes.

This means the board fills in cell-by-cell in real time as moves are computed — no client-side replay timer needed.
CORS is handled centrally at the gateway; the session service itself has no `@CrossOrigin` configuration.

### Concurrency Safeguards

Two concurrency issues are addressed in the game engine:

**Optimistic locking on `Game`** — the `Game` entity carries a `@Version Long version` field managed by Hibernate.
On every `makeMove()` call, the UPDATE includes a `WHERE version = ?` clause.
If two requests read the same game state and both try to write, the second write finds a stale version and Hibernate throws `OptimisticLockException`, which the exception handler maps to **409 Conflict**.
This prevents two concurrent moves from silently corrupting the board.

**Transactional boundary on `makeMove()`** — `@Transactional` ensures the read (`findById`) and the write (`save`) are part of a single transaction, making the version check meaningful.
Without it the two operations would run in separate persistence contexts and the optimistic lock would offer no protection.

### Integration and Unit Tests

Each backend service has two layers of automated tests.

**Unit tests** use `@ExtendWith(MockitoExtension.class)` with no Spring context — all collaborators are mocked via `@Mock` / `@InjectMocks`. They run fast and verify pure service logic in isolation:

| Test class | Service | Scenarios covered |
|---|---|---|
| `GameServiceTest` | `game-engine-service-app` | create game, make move (happy path, cell taken, game over, not found), get game |
| `SessionServiceTest` | `game-session-service-app` | create session, get session (happy path, not found) |

**Integration tests** load the full Spring context (`@SpringBootTest`) with H2 in-memory database and exercise the HTTP layer via `MockMvc`. Eureka and Config Server are disabled for the test run via inline properties (`spring.config.import=optional:`, `eureka.client.enabled=false`); H2 is auto-configured by Spring Boot when no datasource URL is set.

| Test class | Service | Scenarios covered |
|---|---|---|
| `GameControllerIT` | `game-engine-service-app` | 201 create, 200 move, 400 cell-taken / out-of-range / game-over, 404 not found, full X-wins flow, full O-wins flow |
| `SessionControllerIT` | `game-session-service-app` | 201 create session, full SSE simulation flow (5 moves → X wins), session persisted as COMPLETED with move history, 404 not found |

The `SessionControllerIT` uses `@MockitoBean GameEngineClient` to replace the HTTP client with a Mockito mock. The simulate endpoint returns a `SseEmitter`; MockMvc's async dispatch (`request().asyncStarted()` → `asyncDispatch()`) blocks until the virtual thread calls `emitter.complete()` and then asserts the accumulated SSE body.

To run all tests:

```bash
mvn test -pl game-engine-service-app
mvn test -pl game-session-service-app
```


---

## Running with Docker Compose

Docker Compose uses Cloud Native Buildpacks (via `spring-boot:build-image`) — no Dockerfiles needed.

### Prerequisites

- Docker Desktop (or Docker Engine + Compose plugin)
- Maven 3.9+
- JDK 25 (only needed to build images)

### 1. Build all images

Run from the project root. Each module produces an OCI image via the Paketo buildpack:

```bash
mvn spring-boot:build-image -f config-server/pom.xml
mvn spring-boot:build-image -f eureka-server/pom.xml
mvn spring-boot:build-image -f game-engine-service-app/pom.xml
mvn spring-boot:build-image -f game-session-service-app/pom.xml
mvn spring-boot:build-image -f ui-service-app/pom.xml
mvn spring-boot:build-image -f gateway/pom.xml
```

Images are tagged `distributed-tac-toe/<module>:latest` and stored in the local Docker daemon.

### 2. Start all services

```bash
docker compose up
```

Compose enforces the correct startup order via `depends_on: condition: service_healthy`.
Each service waits until its upstream dependencies pass `/actuator/health` before starting.

### 3. Verify and use

| Check | URL |
|---|---|
| Config server | `http://localhost:8888/game-engine-service-app/default` |
| Eureka dashboard | `http://localhost:8761` |
| UI (via gateway) | `http://localhost:8080/` |

### How config works in Docker

The `config-server` image mounts `./configuration` as a volume and switches to the `native`
profile so it reads YAML files from the local directory rather than the git backend:

```
environment:
  SPRING_PROFILES_ACTIVE=native
  SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS=file:/workspace/config
volumes:
  ./configuration:/workspace/config
```

All other services override their config-server and Eureka URLs for Docker networking:

```
SPRING_CONFIG_IMPORT=optional:configserver:http://config-server:8888
EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka/
```

### Stopping

```bash
docker compose down
```