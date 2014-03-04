package net.fawad.rabbitmqloadgen

import java.io.File
import scala.io.Source

class MessageGenerator(jsonDirectory: File) extends Iterator[Message] {
  val reader = new RabbitMQMessageReader()
  val documents = for (fileName <- jsonDirectory.listFiles().filter(_.isFile);
                       contents = Source.fromFile(fileName).mkString;
                       message = reader.load(contents)
  ) yield message

  val stream: Stream[Message] = Stream.concat(documents).append(stream)

  override def hasNext = true

  override def next() = stream.head
}
