package coroutines

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import org.apache.commons.lang3.RandomStringUtils

fun getRandomTime(): Long = (500..1000).random().toLong()

suspend fun producer(id: Int, queue: SendChannel<String>) {
  while (true) {
    delay(getRandomTime())

    val value = RandomStringUtils.randomAlphanumeric(10)
    println("producer $id producing $value")
    queue.send(value)
  }
}

suspend fun consumer(id: Int, queue: ReceiveChannel<String>) {
  while (true) {
    delay(getRandomTime())

    val value = queue.receive()
    println("consumer $id consuming value $value")
  }
}

suspend fun main(args: Array<String>) {
  coroutineScope {
    val queue = Channel<String>(UNLIMITED)

    repeat(10) { i ->
      launch { producer(i + 1, queue) }
    }

    repeat(10) { i ->
      launch { consumer(i + 1, queue) }
    }
  }
}
