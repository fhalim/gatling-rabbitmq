package net.fawad.rabbitmqloadgen

import java.io.File
import scala.io.Source

class MessageGenerator(jsonDirectory: File) {
  val reader = new RabbitMQMessageReader()
  val documents = for (fileName <- jsonDirectory.listFiles().filter(_.isFile);
                       contents = Source.fromFile(fileName).mkString;
                       message = reader.load(contents)
  ) yield message

  val docs = documents.array
  val stream: Stream[Message] = Stream.concat(docs).append(stream)

  def iterator = stream.iterator
}
