# QueueCTL

QueueCTL is a CLI-based background job queue system built using **Java**, **Spring Boot**, and **Picocli**. It provides reliable background job execution with support for multiple workers, persistent storage, automatic retries using exponential backoff, and a Dead Letter Queue (DLQ).

---

## Features

- CLI-based job management
- Multiple concurrent workers
- Persistent job storage using H2 Database
- Automatic retry with exponential backoff
- Dead Letter Queue (DLQ)
- Worker locking to prevent duplicate processing
- Graceful worker shutdown
- Runtime configuration management
- Modular Spring Boot architecture

---

# Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Data JPA
- H2 Database
- Picocli
- Maven

---

# Project Structure

```
src
├── main
│   ├── java
│   │   ├── commands
│   │   ├── config
│   │   ├── controller
│   │   ├── model
│   │   ├── repository
│   │   ├── service
│   │   └── QueueCtlApplication.java
│   └── resources
│       ├── application.properties
│       └── static
└── test
```

---

# Prerequisites

- Java 17+
- Maven

Verify installation

```bash
java --version
mvn --version
```

---

# Clone Repository

```bash
git clone https://github.com/Aditya-Bhardwaj022/flam.git

cd flam
```

---

# Build the Project

Linux / macOS

```bash
./mvnw clean package
```

Windows

```bash
mvnw.cmd clean package
```

or

```bash
mvn clean package
```

After a successful build, Maven creates

```
target/queuectl-0.0.1-SNAPSHOT.jar
```

---

# Run the Application

Run the executable jar

```bash
java -jar target/queuectl-0.0.1-SNAPSHOT.jar
```

You can optionally create an alias

Linux

```bash
alias queuectl="java -jar target/queuectl-0.0.1-SNAPSHOT.jar"
```

Windows PowerShell

```powershell
function queuectl {
    java -jar target/queuectl-0.0.1-SNAPSHOT.jar $args
}
```

---

# CLI Commands

## Enqueue Job

```bash
queuectl enqueue '{"id":"job1","command":"echo Hello World"}'
```

Example

```bash
queuectl enqueue '{"id":"job2","command":"sleep 2"}'
```

Failed Job

```bash
queuectl enqueue '{"id":"job3","command":"exit 1"}'
```

---

## Start Workers

Single Worker

```bash
queuectl worker start
```

Multiple Workers

```bash
queuectl worker start --count 3
```

---

## Stop Workers

```bash
queuectl worker stop
```

Workers finish the current job before shutting down.

---

## View Status

```bash
queuectl status
```

Shows

- Pending jobs
- Processing jobs
- Completed jobs
- Failed jobs
- Dead jobs
- Active workers

---

## List Jobs

```bash
queuectl list
```

Filter by state

```bash
queuectl list --state pending
```

```bash
queuectl list --state processing
```

```bash
queuectl list --state completed
```

```bash
queuectl list --state failed
```

```bash
queuectl list --state dead
```

---

## Dead Letter Queue

List DLQ

```bash
queuectl dlq list
```

Retry a job

```bash
queuectl dlq retry job3
```

---

## Configuration

Maximum retries

```bash
queuectl config set max-retries 5
```

Backoff base

```bash
queuectl config set backoff-base 3
```

---

# Job Lifecycle

```
Pending
    │
    ▼
Processing
    │
 ┌──┴────────────┐
 │               │
 ▼               ▼
Completed      Failed
                  │
                  ▼
           Retry Available
                  │
                  ▼
            Exponential
             Backoff
                  │
                  ▼
            Processing
                  │
                  ▼
        Max Retries Exceeded
                  │
                  ▼
          Dead Letter Queue
```

---

# Architecture

```
CLI (Picocli)
      │
      ▼
Command Layer
      │
      ▼
Service Layer
      │
      ▼
Repository Layer
      │
      ▼
 H2 Database
```

---

# Worker Execution Flow

1. Worker polls pending jobs.

2. Claims a job using pessimistic locking.

3. Executes the command.

4. Marks the job completed on success.

5. On failure

- Increment attempts
- Apply exponential backoff
- Retry

6. If retries exceed the configured limit

- Move job to DLQ

---

# Persistence

QueueCTL stores

- Jobs
- Worker configuration
- Retry configuration

using an embedded H2 database.

All data survives application restarts.

---

# Assumptions

- Java 17 is installed.
- H2 database is stored locally.
- Commands are executed using the host operating system shell.
- Worker stop uses a database flag for graceful shutdown.

---

# Testing

Run tests

```bash
mvn test
```

or

```bash
./mvnw test
```

The project contains unit tests covering

- Job enqueue
- Worker execution
- Retry logic
- Dead Letter Queue

---

# Demo

A short CLI demonstration video is available here:

```
<Add Google Drive Link Here>
```

---

# Future Improvements

- Job priorities
- Scheduled jobs
- Job timeout support
- Metrics dashboard
- REST API
- Docker support

---

# Author

**Aditya Bhardwaj**

GitHub

https://github.com/Aditya-Bhardwaj022

---
