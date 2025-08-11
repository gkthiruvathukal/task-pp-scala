# Distributed Pi in Scala

This project demonstrates how to build lightweight, type-safe parallel applications in Scala for high-performance computing (HPC) environments — including traditional clusters and cloud-based deployments.

- It runs without MPI.  
- It runs without root access.  
- It is designed to be easy to read and extend.

---

## Why

In the HPC field, the term *"embarrassingly parallel"* is often used in a dismissive way.  
The implication is that these problems are too simple to be interesting.

But simple patterns can be powerful.
Think of basic songwriting — most people listen to popular music, and while I tend to listen to jazz or classical, I still enjoy the craft and structure of a good pop song.

This project applies that same idea to parallel computing.  
We can write simple, structured programs in a high-level language like Scala and run them in serious computing environments.

---

## Overview

This prototype models computation as **work distribution** using structured messages between:

- **WorkSupplier** (`dpi.WorkSupplier`) — partitions a task and sends work to consumers.
- **WorkConsumer** (`dpi.WorkConsumer`) — receives a task, computes results, and sends them back.

The programs communicate over message queues provided by [JeroMQ](https://github.com/zeromq/jeromq), a pure Java implementation of ZeroMQ.  
Work is described as a Scala `case class` and serialized/deserialized to JSON using [uPickle](https://github.com/com-lihaoyi/upickle).

---

## Example: Monte Carlo Estimation of π

The code uses the Monte Carlo method to estimate π — a common example for testing parallel systems.

1. The **WorkSupplier** takes a total number of points to simulate.
2. It divides this number evenly across the given list of worker nodes.
3. Each **WorkConsumer**:
   - Receives a `Work` message specifying how many points to process.
   - Generates random points inside the unit square.
   - Counts how many points land inside the unit circle.
   - Sends the count back to the supplier.
4. The supplier collects the counts, computes the ratio, and multiplies by 4 to estimate π.

---

## How it works in code

### Work messages

We define a single case class in `dpi.Work`:

```scala
case class Work(points: Long)
```

uPickle’s `macroRW` automatically provides the JSON reader/writer for this case class.

The supplier serializes a `Work` instance to JSON with `write(work)`.  
The consumer receives a JSON string and deserializes it with `read[Work](msg)`.

---

### Communication

- **Supplier** uses a ZeroMQ **REQ** socket for each worker.  
  It connects to `tcp://<hostname>:<port>`, sends the JSON-encoded `Work` message, and waits for a reply.
- **Consumer** uses a ZeroMQ **REP** socket.  
  It binds to `tcp://*:<port>`, receives the `Work` message, processes it, and replies with the count.

This REQ/REP pattern ensures that for every request sent, there is exactly one reply.

---

### CLI parsing

We use [mainargs](https://github.com/com-lihaoyi/mainargs) for command-line parsing:

- **WorkSupplier** accepts:
  - `--points, -p` — total number of points (required)
  - `--workers, -w` — worker hostnames (repeatable or comma-separated)
  - `--workers-file` — file with one hostname per line
  - `--port, -P` — TCP port (default 5555)
- **WorkConsumer** accepts:
  - `--port, -P` — TCP port to bind (default 5555)
  - `--runs, -n` — number of jobs to process before exiting (omit for infinite loop)

---

### Scripts

Three shell scripts wrap the Java commands:

- `run-consumer.sh` — runs a consumer with the given port and optional run limit.
- `run-supplier.sh` — runs the supplier with points, workers (list or file), and port.
- `pbs-launch.sh` — reads `$PBS_NODEFILE`, starts consumers on all but the first node via SSH, and runs the supplier.

---

## Running the code

### Build

```bash
sbt assembly
```

This produces:

```
target/scala-3.3.5/work-supplier.jar
```

---

### Start consumers

On each worker node:

```bash
./run-consumer.sh -P 5555
```

Limit to a set number of jobs:

```bash
./run-consumer.sh -P 5555 -n 10
```

---

### Run supplier

On the control node:

```bash
./run-supplier.sh -p 1000000 -w node1,node2
```

Or use a workers file:

```bash
./run-supplier.sh -p 1000000 --workers-file workers.txt
```

Where `workers.txt` contains:

```
node1
node2
```

---

### PBS cluster example

If using PBS with `$PBS_NODEFILE`:

```bash
./pbs-launch.sh
```

Environment variables:
- `POINTS` — total points (default 1,000,000)
- `PORT` — TCP port (default 5555)
- `EXTRA_CONSUMER_ARGS` — additional args for consumer
- `EXTRA_SUPPLIER_ARGS` — additional args for supplier

Example:

```bash
POINTS=5000000 PORT=6000 ./pbs-launch.sh
```

---

## Future work

Right now, the code is hard-wired to the π calculation.  
The next step is to generalize this into an event-driven framework that separates:

- Communication layer (message passing)
- Computation layer (what each worker does)

This would make it easy to swap in different algorithms while reusing the communication infrastructure.

---

## Disclaimer

This is a prototype.  
It’s a Minimum Viable Application.  
It is meant to demonstrate a pattern, not to serve as a production-ready framework.

---

## AI Disclosure

The design, code, and explanations in this project are my own work.  
I also interact with large language models (LLMs) — specifically **GPT‑4o**, **GPT‑5**, and LLAMA 3.x — to explore ideas, check details, and refine wording.  
The final decisions, structure, and content are mine.
