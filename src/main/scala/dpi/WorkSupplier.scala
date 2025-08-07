package dpi

import org.zeromq.{ZContext, ZMQ}
import upickle.default._
import dpi.Work._
import dpi.Result._

object WorkSupplier:
  def main(args: Array[String]): Unit =
    if args.length < 2 then
      println("Usage: WorkSupplier <totalPoints> <workerHost1> <workerHost2> ...")
      sys.exit(1)

    val totalPoints = args.head.toLong
    val workerHosts = args.tail
    val pointsPerWorker = totalPoints / workerHosts.length

    val context = new ZContext()
    val sockets = workerHosts.map { host =>
      val socket = context.createSocket(ZMQ.REQ)
      socket.connect(s"tcp://$host:5555")
      socket
    }

    println(s"[WorkSupplier] Sending work to ${sockets.size} workers...")

    sockets.foreach { socket =>
      val work = Work(pointsPerWorker)
      socket.send(write(work))
    }

    var insideTotal = 0L
    sockets.foreach { socket =>
      val reply = socket.recvStr()
      val result = read[Result](reply)
      insideTotal += result.inside
    }

    val pi = 4.0 * insideTotal / totalPoints
    println(f"[WorkSupplier] Pi estimate: $pi%.6f")

    context.close()
