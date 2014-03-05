package net.fawad.rabbitmqloadgen

import java.io.File
import scala.io.Source

class MessageGenerator(jsonDirectory: File) {
  val reader = new RabbitMQMessageReader()
  val documents = for (fileName <- jsonDirectory.listFiles().filter(_.getName.endsWith(".json"));
                       contents = Source.fromFile(fileName).mkString;
                       message = reader.load(contents)
  ) yield message

  val stream: Stream[Message] = Stream.concat(documents).append(stream)

  def iterator = stream.iterator
}
