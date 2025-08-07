package dpi

import org.zeromq.{ZContext, ZMQ}
import scala.util.Random
import upickle.default._
import dpi.Work._
import dpi.Result._

object WorkConsumer:
  def main(args: Array[String]): Unit =
    val context = new ZContext()
    val socket = context.createSocket(ZMQ.REP)
    socket.bind("tcp://*:5555")
    println("[WorkConsumer] Ready and waiting...")

    while true do
      val msg = socket.recvStr()
      val work = read[Work](msg)
      val rand = new Random()

      val inside = (1L to work.points).count { _ =>
        val x = rand.nextDouble()
        val y = rand.nextDouble()
        x * x + y * y <= 1.0
      }

      val result = Result(inside)
      socket.send(write(result))
