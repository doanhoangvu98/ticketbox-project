# learning-demo

Small standalone examples used for learning sessions and presentations.
Nothing here depends on the TicketBox code in `backend/`.

## testing-demo

JUnit 5, Mockito, TDD and integration testing, shown on one simple example: **user registration**.

| Test class | Topic | What to look at |
|---|---|---|
| `PasswordValidatorTest` | JUnit 5 + TDD | parameterized tests, test-first cycles (see comments in `PasswordValidator`) |
| `UserServiceTest` | Mockito | `@Mock`, `when`, `verify`, `never`, `ArgumentCaptor`, `InOrder` |
| `RegistrationFlowIntegrationTest` | Integration | real objects, no mocks, a `FakeEmailSender` |

Run (Java 21 required):

```bash
cd learning-demo/testing-demo
mvn test
```

## java-core-demo

One small example per Java Core topic, on TicketBox-flavoured data (seats, events). Every package has its own test.

| Package | Topic | What to look at |
|---|---|---|
| `model` | OOP | `Seat` record, compact constructor, implements `Identifiable` |
| `generic` | Generics | bounded type `<T extends Identifiable<ID>, ID>`, `? extends` / `? super`, default methods |
| `collection` | Collections + Lambda + Stream | `EnumMap`, `TreeMap`, `TreeSet`, `groupingBy`, `partitioningBy`, composed `Predicate` |
| `io` | I/O + Exceptions | `Files` + buffered reader/writer, try-with-resources, a custom checked exception |
| `thread` | Threads | race condition, `synchronized`, `putIfAbsent`, `ExecutorService`, `CountDownLatch`, `AtomicInteger` |
| `jdbc` | Plain JDBC | `DriverManager`, `PreparedStatement`, `ResultSet`, transaction with commit/rollback, batch |
| `network` | Networking | TCP `ServerSocket`/`Socket` echo server, `java.net.http.HttpClient` |
| `annotation` | Annotations + reflection | custom `@Required` / `@MaxLength`, a mini validator that reads them at runtime |

The JDBC tests run on **H2**, an in-memory database, so `mvn test` needs no Docker.
The JDBC code itself only uses `java.sql`; against the real PostgreSQL only the URL changes
(see the comment in `EventJdbcRepositoryTest`).

Run (Java 21 required):

```bash
cd learning-demo/java-core-demo
mvn test
```
