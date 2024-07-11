package oxconcurreny

import org.apache.commons.lang3.RandomStringUtils
import ox._
import ox.channels._

import scala.annotation.tailrec
import scala.util.Random

object ProducerConsumer {

  def getRandomTime: Int = Random.between(500, 1001)

  def producer(id: Long, queue: Sink[String])(using Ox): Fork[Unit] = fork {
    produce(id, queue)
  }

  @tailrec
  def produce(id: Long, queue: Sink[String]): Unit =
    Thread.sleep(getRandomTime)

    val value = RandomStringUtils.randomAlphanumeric(10)
    queue.send(value)
    println("producer " + id + " producing " + value)

    produce(id, queue)

  def consumer(id: Long, queue: Source[String])(using Ox): Fork[Unit] = fork {
    consume(id, queue)
  }

  @tailrec
  def consume(id: Long, queue: Source[String]): Unit =
    Thread.sleep(getRandomTime)

    val value = queue.receive()
    println("consumer " + id + " consuming value " + value)

    consume(id, queue)

  @main def main(): Unit =
    supervised {
      val queue = Channel.unlimited[String]

      val producers = List.range(1, 11).map(producer(_, queue))
      val consumers = List.range(1, 11).map(consumer(_, queue))

      (producers ++ consumers).foreach(_.join())
    }

}
