package akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.routing.RoundRobinPool
import org.apache.commons.lang3.RandomStringUtils

import scala.collection.immutable.Queue
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

object ProducerConsumer {
  val system: ActorSystem = ActorSystem("actorSystem")
  implicit val executionContext: ExecutionContext = system.dispatcher

  def getRandomTime: Int = Random.between(500, 1001)

  object QueueActor {
    case class Init()
    case class Add(value: String)
    case class Retrieve()
  }

  class QueueActor() extends Actor {
    import QueueActor._
    import Consumer._

    override def receive: Receive = {
      case Init => context.become(withInnerQueue(Queue.empty[String]))
    }

    def withInnerQueue(inner: Queue[String]): Receive = {
      case Add(value) =>
        context.become(withInnerQueue(inner.enqueue(value)))

      case Retrieve =>
        if (inner.nonEmpty) {
          val (value, remaining) = inner.dequeue
          context.become(withInnerQueue(remaining))

          sender ! Obtained(Some(value))
        } else {
          sender ! Obtained(None)
        }
    }
  }

  object Producer {
    case class Produce()

    def props(queue: ActorRef) = Props(new Producer(queue))
  }

  class Producer(queue: ActorRef) extends Actor {
    import Producer._
    import QueueActor._

    override def receive: Receive = {
      case Produce() =>
        val value = RandomStringUtils.randomAlphanumeric(10)
        println(s"producer ${self.path.name} producing $value")
        queue ! Add(value)
    }
  }

  object Consumer {
    case class Consume()
    case class Obtained(valueOpt: Option[String])

    def props(queue: ActorRef) = Props(new Consumer(queue))
  }

  class Consumer(queue: ActorRef) extends Actor {
    import Consumer._
    import QueueActor._

    override def receive: Receive = {
      case Consume() =>
        queue ! Retrieve

      case Obtained(valueOpt) =>
        valueOpt match {
          case Some(value) => println(s"consumer ${self.path.name} consuming value $value")
          case None => println(s"consumer ${self.path.name} no value obtained")
        }
    }
  }

  def main(args: Array[String]): Unit = {
    import Consumer._
    import Producer._
    import QueueActor._

    val queue = system.actorOf(Props[QueueActor])
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
  }
}
