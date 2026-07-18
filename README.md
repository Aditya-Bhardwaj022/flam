# QueueCTL

QueueCTL is a CLI-based background job queue system implemented in Java using Spring Boot and Picocli.

## Features
- Job execution in the background
- Multiple worker threads
- Persistent job storage using embedded H2 database
- Retry mechanism with exponential backoff
- Dead Letter Queue (DLQ)
- Configuration management for retries and backoff

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven

### Build locally
From the project root, run:

```bash
./mvnw clean package -DskipTests
```

On Windows, use:

```bash
mvnw.cmd clean package -DskipTests
```

### Run locally
After the build completes, run:

```bash
java -jar target/queuectl-0.0.1-SNAPSHOT.jar
```

If the JAR name is different, use the exact file name inside `target/`.

### Optional alias
For convenience, you can create an alias:

```bash
alias queuectl="java -jar target/queuectl-0.0.1-SNAPSHOT.jar"
```

On Windows PowerShell, you can run the JAR directly using `java -jar ...`.

## Usage Examples

### Enqueue a job
```bash
queuectl enqueue '{"id":"job1","command":"echo Hello World"}'
queuectl enqueue '{"id":"job2","command":"sleep 2"}'
queuectl enqueue '{"id":"job3","command":"exit 1"}'
```

### Manage workers
Start 3 workers:
```bash
queuectl worker start --count 3
```

Stop workers gracefully:
```bash
queuectl worker stop
```

### Check status and list jobs
```bash
queuectl status
queuectl list
queuectl list --state pending
queuectl list --state failed
```

### DLQ commands
```bash
queuectl dlq list
queuectl dlq retry job3
```

### Configuration
```bash
queuectl config set max-retries 5
queuectl config set backoff-base 3
```

## Architecture Overview
- Picocli handles the CLI layer.
- Spring Boot provides dependency injection and application wiring.
- H2 stores jobs and configuration persistently.
- Workers claim jobs using locking to avoid duplicate processing.
- Failed jobs are retried with exponential backoff and moved to DLQ after max retries.

## Testing Instructions
To manually verify the core flow locally:
1. Enqueue a successful job.
2. Enqueue a failing job.
3. Start one or more workers.
4. Check `status`, `list`, and `dlq list`.
5. Retry a DLQ job with `dlq retry`.

## Notes
- This project is a CLI tool, not a web application.
- A GUI is not required for the assignment.
