package catseffect

import cats.effect._
import cats.effect.std.Queue
import cats.instances.list._
import cats.syntax.all._
import org.apache.commons.lang3.RandomStringUtils

import scala.concurrent.duration._
import scala.util.Random

object ProducerConsumer extends IOApp {

  def getRandomTime: FiniteDuration = Random.between(500, 1001).millis

  def producer(id: Int, queue: Queue[IO, String]): IO[Unit] = {
    for {
      _ <- IO.sleep(getRandomTime)

      value = RandomStringUtils.randomAlphanumeric(10)
      _ <- queue.offer(value)
      _ <- IO.println(s"producer $id producing $value")

      _ <- producer(id, queue)
    } yield ()
  }

  def consumer(id: Int, queue: Queue[IO, String]): IO[Unit] = {
    for {
      _ <- IO.sleep(getRandomTime)

      value <- queue.take
      _ <- IO.println(s"consumer $id consuming value $value")

      _ <- consumer(id, queue)
    } yield ()
  }

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      queue <- Queue.unbounded[IO, String]
      producers = List.range(1, 11).map(producer(_, queue))
      consumers = List.range(1, 11).map(consumer(_, queue))
      res <-
        (producers ++ consumers)
          .parSequence
          .as(ExitCode.Success)
    } yield res
  }

}
