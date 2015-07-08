import java.util.concurrent.CountDownLatch

import scala.concurrent.duration._

import akka.actor._

import scala.language.postfixOps

case object Run

case object Msg


class Destination extends Actor {
  def receive = {
    case Msg ⇒ sender ! Msg
  }
}


class UpTheHeatClient extends Actor {

  val testSize = 100000000L
  val initialMessages = 200

  val act = context.actorOf(Props[Destination])

  var sent = 0L
  var received = 0L

  import context.dispatcher

  context.system.scheduler.schedule(0 seconds, 1 second, self, "go")

  val timeStart = System.currentTimeMillis()

  var lastTime = timeStart
  var lastReceived = 0L

  for (i <- 0L until initialMessages) {
    act ! Msg
    sent += 1
  }

  def receive = {
    case Msg =>
      received += 1
      if (sent < testSize) {
        act ! Msg
        sent += 1
      } else context.system.terminate()

    case "go" =>
      val now = System.currentTimeMillis()
      val timeTot = (now - timeStart) / 1e3
      val timeSeg = (now - lastTime) / 1e3
      lastTime = now
      println(f"${2 * received} - ${2 * received / timeTot / 1e06}%5.2f - ${2 * (received - lastReceived) / timeSeg / 1e06}%5.2f")
      lastReceived = received
  }
}
