package catseffect

import cats.effect.{Deferred, ExitCode, IO, IOApp, Ref}
import cats.instances.list._
import cats.syntax.all._
import org.apache.commons.lang3.RandomStringUtils

import scala.collection.immutable.Queue
import scala.concurrent.duration._
import scala.util.Random

object ProducerConsumer extends IOApp {

  case class State(queue: Queue[String], takers: Queue[Deferred[IO, String]])

  def getRandomTime: FiniteDuration = Random.between(500, 1001).millis

  def producer(id: Int, stateR: Ref[IO, State]): IO[Unit] =
    def offer(): IO[String] =
      for {
        _ <- IO.sleep(getRandomTime)
        value = RandomStringUtils.randomAlphanumeric(10)
        _ <- stateR.modify(state => modifyState(value, state))
      } yield value

    def modifyState(value: String, state: State): (State, String) =
      if (state.takers.nonEmpty) {
        val (taker, remainingTakers) = state.takers.dequeue
        taker.complete(value).void // notifies/completes the waiting taker

        State(state.queue, remainingTakers) -> value
      } else {
        State(state.queue.enqueue(value), state.takers) -> value
      }

    for {
      value <- offer()
      _ <- IO.println(s"producer $id producing $value")
      _ <- producer(id, stateR)
    } yield ()

  def consumer(id: Int, stateR: Ref[IO, State]): IO[Unit] =
    def take(): IO[String] =
      for {
        _ <- IO.sleep(getRandomTime)
        signal <- Deferred[IO, String]
        value <- stateR.modify(state => modifyState(signal, state)).flatten
      } yield value

    def modifyState(signal: Deferred[IO, String], state: State): (State, IO[String]) =
      if (state.queue.nonEmpty) {
        val (value, remaining) = state.queue.dequeue
        State(remaining, state.takers) -> IO.pure(value)
      } else {
        State(state.queue, state.takers.enqueue(signal)) -> signal.get // the taker will wait until signal completion
      }

    for {
      value <- take()
      _ <- IO.println(s"consumer $id consuming value $value")
      _ <- consumer(id, stateR)
    } yield ()

  override def run(args: List[String]): IO[ExitCode] =
    for {
      state <- Ref.of[IO, State](State(Queue.empty, Queue.empty))
      producers = List.range(1, 11).map(producer(_, state)) // 10 producers
      consumers = List.range(1, 11).map(consumer(_, state)) // 10 consumers
      res <-
        (producers ++ consumers)
          .parSequence
          .as(ExitCode.Success) // Run producers and consumers in parallel until done
    } yield res

}
