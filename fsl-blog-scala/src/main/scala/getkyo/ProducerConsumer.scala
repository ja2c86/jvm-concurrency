package getkyo

import kyo.*
import org.apache.commons.lang3.RandomStringUtils

import scala.concurrent.duration.*

object ProducerConsumer extends KyoApp {

  def getRandomTime: Int < IOs = Randoms.nextInt(1001)

  def producer(id: Int, queue: Queues.Unbounded[String]): Unit < Fibers =
    for {
      waitingTime <- getRandomTime
      _ <- Fibers.sleep(waitingTime.millis)

      value = RandomStringUtils.randomAlphanumeric(10)
      _ <- queue.offer(value)
      _ <- Consoles.println(s"producer $id producing $value")

      _ <- producer(id, queue)
    } yield ()

  def consumer(id: Int, queue: Queues.Unbounded[String]): Unit < Fibers =
    for {
      waitingTime <- getRandomTime
      _ <- Fibers.sleep(waitingTime.millis)

      value <- queue.poll
      _ <- IOs(value.foreach(str => println(s"consumer $id consuming value $str")))

      _ <- consumer(id, queue)
    } yield ()

  run {
    for {
      queue <- Queues.initUnbounded[String](access = Access.Spsc) // Access Policy: Single Producer, Single Consumer
      producers = List.range(1, 11).map(producer(_, queue)) // 10 producers
      consumers = List.range(1, 11).map(consumer(_, queue)) // 10 consumers
      _ <- Fibers.parallel(producers ++ consumers) // Run producers and consumers in parallel until done
    } yield ()
  }

}
