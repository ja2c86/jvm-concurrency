package akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.routing.RoundRobinPool
import akka.util.Timeout
import org.apache.commons.lang3.RandomStringUtils

import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Random, Try}

object ProducerConsumerAskPattern {
  val system: ActorSystem = ActorSystem("actorSystem")
  given executionContext: ExecutionContext = system.dispatcher
  given askTimeout: Timeout = Timeout(30.seconds)

  def getRandomTime: Int = Random.between(500, 1001)

  object Queue {
    case class Init()
    case class Add(value: String)
    case class Retrieve()
  }

  class Queue() extends Actor {
    import Queue._

    override def receive: Receive =
      case Init => context.become(withQueue(List()))

    def withQueue(innerQueue: List[String]): Receive =
      case Add(value) =>
        context.become(withQueue(innerQueue :+ value))

      case Retrieve =>
        val value = innerQueue.headOption
        val remaining = if (innerQueue.nonEmpty) innerQueue.tail else innerQueue
        context.become(withQueue(remaining))

        sender() ! value
  }

  object Producer {
    case class Produce()

    def props(queue: ActorRef) = Props(new Producer(queue))
  }

  class Producer(queue: ActorRef) extends Actor {
    import Producer._
    import Queue._

    override def receive: Receive =
      case Produce() =>
        val value = RandomStringUtils.randomAlphanumeric(10)
        println(s"producer ${self.path.name} producing $value")
        queue ! Add(value)
  }

  object Consumer {
    case class Consume()

    def props(queue: ActorRef) = Props(new Consumer(queue))
  }

  class Consumer(queue: ActorRef) extends Actor {
    import Consumer._
    import Queue._

    override def receive: Receive =
      case Consume() =>
        Try {
          Await.result(queue ? Retrieve, askTimeout.duration) match { // ask pattern
            case Some(value) => println(s"consumer ${self.path.name} consuming value $value")
            case None => println(s"consumer ${self.path.name} no value obtained")
          }
        }.recover { _ =>
          println(s"consumer ${self.path.name} no value obtained")
        }
  }

  def main(args: Array[String]): Unit =
    import Queue._
    import Producer._
    import Consumer._

    val queue = system.actorOf(Props[Queue]())
    queue ! Init

    Future {
      val producers = system.actorOf(RoundRobinPool(10).props(Producer.props(queue)))
      LazyList.from(1).foreach { _ =>
        Thread.sleep(getRandomTime)
        producers ! Produce()
      }
    }

    Future {
      val consumers = system.actorOf(RoundRobinPool(10).props(Consumer.props(queue)))
      LazyList.from(1).foreach { _ =>
        Thread.sleep(getRandomTime)
        consumers ! Consume()
      }
    }
  end main

}
