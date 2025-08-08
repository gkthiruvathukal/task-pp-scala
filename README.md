# Featherweight Parallel Processing in Scala

This project demonstrates how to build lightweight, type-safe parallel applications in Scala for high-performance computing (HPC) environments — including traditional clusters and cloud-based deployments.

The Monte Carlo Pi calculation is used to demonstrate the potential for this approach, given that it has been used to show the effectiveness of other classical parallel computing frameworks!

Certainly — here’s a clear and friendly **Disclaimer** section you can include in your `README.md`:

## ⚠️  Disclaimer

This project is an early-stage **prototype** — a **Minimum Viable Application (MVA)** — intended to demonstrate basic patterns in lightweight parallel computing using Scala and ZeroMQ.

It is **not yet production-ready**.

We welcome feedback, but kindly ask that you wait for a formal release before opening issues or requesting features.

If you're interested in contributing or collaborating, feel free to reach out via [my GitHub profile](https://github.com/gkthiruvathukal). I'm happy to connect and discuss ideas for how this work can grow.

## 🎧 Motivation: In Defense of the Embarrassingly Parallel

In the HPC field, the term *"embarrassingly parallel"* is often used pejoratively, as if simple parallelism is somehow beneath serious computing.
But this is both misguided and wrong, especially when it comes to data science applications.

Much like popular music — which dominates what most people listen to — *simple task-based parallelism* has profound expressive potential. Even those of us who primarily enjoy jazz and classical music know how much artistry can come from straightforward songwriting forms. Similarly, basic forms of parallelism can be expressive, scalable, and surprisingly powerful.

This project is our first step toward **embracing simplicity with intention**: demonstrating how easily structured parallel programs can be built using modern, type-safe tools like Scala.

Let's take a look!

## 🧱 Architecture Overview

This system models computation as **work distribution** using structured messaging between:

* A **work supplier** (`dpi.WorkSupplier`) that partitions a task and sends it to remote workers.
* Multiple **work consumers** (`dpi.WorkConsumer`) that receive and compute on assigned tasks.

Communication is done over **message queues**, allowing for buffered and decoupled processing — conceptually simpler than MPI but semantically rich enough to support real distributed work.

## 🧪 Current Prototype: Monte Carlo Estimation of π

We use the **Monte Carlo method** to estimate π — a classic in parallel computing due to its simplicity and effectiveness.

### How It Works:

1. The `WorkSupplier` accepts a total number of points to simulate.
2. It partitions this number evenly across a list of consumer nodes.
3. Each `WorkConsumer` receives its workload, simulates random points, and returns a partial count of points that fall within the unit circle.
4. The supplier collects the results and computes π using the formula:

   $$
   \pi \approx 4 \times \frac{\text{points inside circle}}{\text{total points}}
   $$

## 🔧 Key Scala Features and Dependencies

This project uses modern Scala 3 features and lightweight functional libraries:

| Feature           | Description                                                            |
| ----------------- | ---------------------------------------------------------------------- |
| `case class Work` | Represents the message exchanged between supplier and consumer         |
| `uPickle`         | JSON-based serialization of case classes (pure Scala, minimal config)  |
| `JeroMQ`          | A 100% Java implementation of ZeroMQ (no native libs or root required) |
| `sbt-assembly`    | Builds a self-contained (fat) JAR file for easy deployment             |

The full project is organized under the `dpi` package.

## 🚀 How to Run

### Build

```bash
sbt assembly
```

This creates a self-contained JAR in `target/scala-3.3.5/work-supplier.jar`.

### Launch Workers (Consumers)

On each **worker node**:

```bash
java -cp target/scala-3.3.5/work-supplier.jar dpi.WorkConsumer
```

Each consumer binds to TCP port 5555 and waits for work.

### Launch Controller (Supplier)

On the **controller node**:

```bash
java -cp target/scala-3.3.5/work-supplier.jar dpi.WorkSupplier 1000000 node1 node2 node3
```

Replace `1000000` with the total number of points to simulate, and `node1` etc. with the hostnames or IPs of the worker nodes.

## 📜 Using This in a PBS Job Script

If you're using a PBS-based cluster (like many traditional HPC environments), you can use `$PBS_NODEFILE` to launch workers and the supplier.

### Example `job.pbs`:

```bash
#!/bin/bash
#PBS -l nodes=4:ppn=1
#PBS -N distributed-pi
#PBS -j oe

cd $PBS_O_WORKDIR

# Read nodes into an array
NODES=($(uniq $PBS_NODEFILE))
SUPPLIER_NODE=${NODES[0]}
WORKER_NODES=("${NODES[@]:1}")

# Start workers on remote nodes (in background)
for node in "${WORKER_NODES[@]}"; do
  ssh "$node" "java -cp work-supplier.jar dpi.WorkConsumer" &
done

# Start supplier on current node
java -jar work-supplier.jar 1000000 "${WORKER_NODES[@]}"
```

You must ensure that:

* The JAR is accessible on all nodes (via shared filesystem or pre-staged)
* SSH access is configured between nodes (if launching via `ssh`)
* Port 5555 is open or adjusted accordingly

## 🔭 Future Work

This is a **hard-wired computation**: the supplier and consumers know exactly what to do and when. But this simplicity is a strength — and a foundation for what’s next.

We plan to:

* Generalize this design into an **event-driven framework**
* Decouple computation logic from communication patterns
* Explore structured messaging using higher-level APIs (e.g. Akka, FS2, or ZIO)

The larger goal is to **reintroduce high-level, type-safe functional programming into the HPC toolbox**, bridging the gap between expressive software engineering and scalable scientific computing.

## 🧠 Final Thoughts

This project is intentionally simple — but the ideas are scalable.
It’s a **musical sketch**, not a symphony.
But even sketches can inspire great works.

We hope this project inspires new ways of thinking about HPC — not just as a domain of low-level speed, but as one where **clarity, structure, and safety** also belong.

Let me know if you'd like this in `README.md` format or included directly in the downloadable project.


## Reproducibility

I will be adding my chat transcript soon. I've written systems like this in the past.
Thanks to modern LLMs, I can now communicate architectural and design ideas (starting from requirements) to build these systems.

## What about other languages?

I've already been asked to create a Python version using `pyzmq`. I will also create a Go version.
