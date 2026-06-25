# Discussion: Potential Improvements and Alternative Design Approaches

---

## 1. N+1 Problem

In Session and 

---

## 2. Service Startup Ordering

The six services must start in a strict sequence: config-server → eureka-server → game-engine → game-session → ui-service → gateway. The current setup relies on developers following a documented checklist. If a service starts before its dependencies are ready, it may fail to register with Eureka or fail to fetch its configuration and exit.

**Potential approaches:**

- **Docker Compose with health checks.** Each service declares a `healthcheck` and downstream services use `depends_on: condition: service_healthy`. Compose then enforces the ordering automatically and restarts failed containers. This is the most practical improvement for local development.

- **Kubernetes readiness probes.** In a production deployment, each pod exposes a `/actuator/health` readiness probe. Kubernetes holds traffic away from pods that are not yet ready, and service mesh routing prevents calls reaching unready instances.

- **Spring Boot retry configuration.** Spring Cloud Config and Eureka both support retry on startup via `spring.cloud.config.retry.*` and `eureka.client.initial-instance-info-replication-interval-seconds`. Increasing retry attempts with exponential backoff makes services more tolerant of transient ordering issues without requiring orchestration changes.

---

## 3. `simulate()` Method Complexity

`SessionService.simulate()` currently combines three distinct concerns in a single method:

1. **Database transaction** — claim the session as `SIMULATING` with optimistic locking (`@Transactional` + `saveAndFlush`).
2. **Background execution** — run the simulation loop on a virtual thread via `Executor`.
3. **HTTP streaming** — write SSE events to the client through `SseEmitter`.

This coupling makes the method hard to unit-test in isolation. Testing the simulation logic requires either a real `SseEmitter` or careful mocking of the emitter internals. The integration test currently covers the full flow end-to-end, which works but is slower and harder to diagnose when it fails.

**Potential refactoring:**

- **Extract the status claim.** Move the `findById` + status check + `saveAndFlush` into a separate `@Transactional` method (or a dedicated `SessionClaimService` component). The controller could call this first — getting a 409 early if the session is already claimed — and then call a `runSimulation()` method that only deals with the executor and emitter. Each piece becomes independently testable.

- **Extract the simulation loop.** The loop that calls `gameEngineClient.getGame()` and `makeMove()` repeatedly is pure orchestration logic with no SSE or JPA dependency. Extracting it into a method that accepts a `gameId` and returns a `List<String>` moveHistory makes it trivially unit-testable with a mocked `GameEngineClient` and no async infrastructure.

- **Use Spring `@Async`.** Replacing the manual `Executor` with `@Async` on a dedicated `SimulationRunner` bean would let Spring manage the thread pool and make the async boundary explicit at the method signature level. Combined with virtual threads (`spring.threads.virtual.enabled=true` already configured), this changes nothing at runtime but improves readability and testability.

A rough target design:

```
SessionController
  └─ SessionService.simulate()          — orchestrates: claim → spawn → return emitter
       ├─ SessionClaimService.claim()   — @Transactional, throws 409 on conflict
       └─ SimulationRunner.run()        — @Async, pure loop + SSE writes, no JPA
            └─ SessionCompletionService.complete() — @Transactional, saves COMPLETED + history
```

Each layer is testable in isolation. The integration test remains valuable as a smoke test for the assembled flow but is no longer the only way to verify the simulation logic.
