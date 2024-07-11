package getkyo

import kyo.*
import kyo.direct.*
import org.apache.commons.lang3.RandomStringUtils

import scala.concurrent.duration.*

object ProducerConsumerDirect extends KyoApp {

  def getRandomTime: Int < IOs = Randoms.nextInt(1001)

  def producer(id: Int, queue: Queues.Unbounded[String]): Unit < Fibers = defer {
    val waitingTime = await(getRandomTime)
    await(Fibers.sleep(waitingTime.millis))

    val value = RandomStringUtils.randomAlphanumeric(10)

    await(queue.offer(value))
    await(Consoles.println(s"producer $id producing $value"))

    await(producer(id, queue))
  }

  def consumer(id: Int, queue: Queues.Unbounded[String]): Unit < Fibers = defer {
    val waitingTime = await(getRandomTime)
    await(Fibers.sleep(waitingTime.millis))

    val value = await(queue.poll)
    value.foreach(str => println(s"consumer $id consuming value $str"))

    await(consumer(id, queue))
  }

  run {
    defer {
      val queue = await(Queues.initUnbounded[String](access = Access.Spsc)) // Access Policy: Single Producer, Single Consumer

      val producers = List.range(1, 11).map(await(producer(_, queue))) // 10 producers
      val consumers = List.range(1, 11).map(await(consumer(_, queue))) // 10 consumers

      await(Fibers.parallel(producers ++ consumers)) // Run producers and consumers in parallel until done
    }
  }

}
