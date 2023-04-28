package futures

import org.apache.commons.lang3.RandomStringUtils

import java.util.concurrent.{BlockingQueue, Executors, LinkedBlockingQueue}
import scala.annotation.tailrec
import scala.concurrent.ExecutionContext
import scala.util.Random
import scala.concurrent.Future

object ProducerConsumer {

  implicit val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(20))

  def getRandomTime: Int = Random.between(500, 1001)

  def producer(id: Long, queue: BlockingQueue[String]): Future[Unit] = Future {
    produce(id, queue)
  }

  @tailrec
  def produce(id: Long, queue: BlockingQueue[String]): Unit = {
    Thread.sleep(getRandomTime)

    val value = RandomStringUtils.randomAlphanumeric(10)
    queue.put(value)
    println("producer " + id + " producing " + value)

    produce(id, queue)
  }

  def consumer(id: Long, queue: BlockingQueue[String]): Future[Unit] = Future {
    consume(id, queue)
  }

  @tailrec
  def consume(id: Long, queue: BlockingQueue[String]): Unit = {
    Thread.sleep(getRandomTime)

    val value = queue.take()
    println("consumer " + id + " consuming value " + value)

    consume(id, queue)
  }

  def main(args: Array[String]): Unit = {
    val queue =  new LinkedBlockingQueue[String]()

    List.range(1, 11).foreach(producer(_, queue))

    List.range(1, 11).foreach(consumer(_, queue))
  }

}
