package zioeffect

import zio._

object ProducerConsumerQueue extends ZIOAppDefault {

  def getRandomTime: UIO[Duration] =
    Random.nextIntBetween(500, 1001)
      .map(randomTime => Duration.fromMillis(randomTime.toLong))

  def producer(id: Int, queue: Queue[String]): Task[Unit] =
    for {
      time <- getRandomTime
      _ <- ZIO.sleep(time)

      value <- Random.nextString(10)
      _ <- queue.offer(value)
      _ <- zio.Console.printLine(s"producer $id producing $value")

      _ <- producer(id, queue)
    } yield ()

  def consumer(id: Int, queue: Queue[String]): Task[Unit] =
    for {
      time <- getRandomTime
      _ <- ZIO.sleep(time)

      value <- queue.take
      _ <- zio.Console.printLine(s"consumer $id consuming value $value")

      _ <- consumer(id, queue)
    } yield ()

  override def run =
    for {
      queue <- Queue.unbounded[String]
      producers = List.range(1, 11).map(producer(_, queue)) // 10 producers
      consumers = List.range(1, 11).map(consumer(_, queue)) // 10 consumers
      _ <- ZIO.collectAllPar(producers ++ consumers) // Run producers and consumers in parallel until done
    } yield ()

}
