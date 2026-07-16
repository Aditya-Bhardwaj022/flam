# QueueCTL

QueueCTL is a CLI-based background job queue system implemented in Java using the Spring Boot framework and Picocli.

## Features
- **Job Execution:** Runs commands in the background.
- **Multiple Workers:** Supports running multiple worker threads concurrently.
- **Persistence:** Uses an embedded H2 database to persist jobs and configurations across restarts.
- **Retry Mechanism:** Automatically retries failed jobs using exponential backoff.
- **Dead Letter Queue (DLQ):** Moves permanently failed jobs to a DLQ after exhausting retries.
- **Configuration Management:** Configure maximum retries and exponential backoff base via CLI.

## Setup Instructions

Ensure you have Java 17+ installed.

1. Clone or navigate to the repository directory.
2. Build the application:
   ```bash
   ./mvnw clean package -DskipTests
   ```
   *Note: On Windows, use `.\mvnw.cmd`.*

3. The executable JAR will be available at `target/queuectl-0.0.1-SNAPSHOT.jar`.
   To make it easier to run, you can create a simple alias or run script:
   ```bash
   alias queuectl="java -jar target/queuectl-0.0.1-SNAPSHOT.jar"
   ```

## Usage Examples

### 1. Enqueue a Job
```bash
queuectl enqueue '{"id":"job1","command":"echo Hello World"}'
queuectl enqueue '{"id":"job2","command":"sleep 2"}'
queuectl enqueue '{"id":"job3","command":"exit 1"}' # A job that will fail
```

### 2. Manage Workers
Start 3 concurrent workers in the foreground:
```bash
queuectl worker start --count 3
```

In a separate terminal, you can stop running workers gracefully:
```bash
queuectl worker stop
```

### 3. Check Status & List Jobs
View system summary:
```bash
queuectl status
```

List jobs (optionally filtered by state):
```bash
queuectl list
queuectl list --state pending
queuectl list --state failed
```

### 4. Manage Dead Letter Queue (DLQ)
List permanently failed jobs:
```bash
queuectl dlq list
```

Retry a job from the DLQ:
```bash
queuectl dlq retry job3
```

### 5. Configuration
Set the maximum number of retries (default is 3):
```bash
queuectl config set max-retries 5
```

Set the exponential backoff base (default is 2):
```bash
queuectl config set backoff-base 3
```

## Architecture Overview
- **CLI Framework:** Picocli handles subcommands and options efficiently.
- **Spring Boot:** Provides dependency injection, transaction management, and robust structure.
- **Persistence:** Spring Data JPA with an embedded H2 database (persisted to `./data/queuectl.db`). This ensures job data and config survive application restarts.
- **Worker Logic:** Workers run in a fixed thread pool. A worker claims a pending job using a pessimistic write lock to avoid race conditions. If a job fails, the attempts counter is incremented, and an exponential backoff (`delay = base ^ attempts` seconds) is applied before it becomes ready again.

## Assumptions & Trade-offs
- The H2 database file is created in `./data` directory relative to the execution path.
- The `queuectl worker stop` command uses a simple database flag to signal the running worker process to shutdown gracefully. This avoids the complexity of cross-process OS signals in Java.
- The command execution (`ProcessBuilder`) relies on the host OS shell (`cmd.exe` on Windows, `sh` on Unix). Complex quotes in commands might need escaping.

## Testing Instructions
To manually verify core flows:
1. Enqueue a successful job: `java -jar target/queuectl-0.0.1-SNAPSHOT.jar enqueue '{"id":"success-job","command":"echo Success"}'`
2. Enqueue a failing job: `java -jar target/queuectl-0.0.1-SNAPSHOT.jar enqueue '{"id":"fail-job","command":"exit 1"}'`
3. Start a worker: `java -jar target/queuectl-0.0.1-SNAPSHOT.jar worker start`
4. In another terminal, observe `status` and `list` commands as the fail-job retries and eventually enters DLQ.
5. Stop the worker process (`Ctrl+C` or `queuectl worker stop`).
6. Retry the DLQ job: `queuectl dlq retry fail-job` and start a worker again.
=======
# flam
>>>>>>> 61482cd1c2f9880fb93642438677c7184be860dd
