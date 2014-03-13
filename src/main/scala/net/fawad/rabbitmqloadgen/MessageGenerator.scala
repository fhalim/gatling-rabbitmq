package net.fawad.rabbitmqloadgen

import java.io.File
import scala.io.Source
import org.w3c.dom.Document

class MessageGenerator(jsonDirectory: File) {
  val reader = new RabbitMQMessageReader()
  val documents = for (fileName <- jsonDirectory.listFiles().filter(_.getName.endsWith(".json"));
                       contents = Source.fromFile(fileName).mkString;
                       message = reader.load(contents)
  ) yield message

  val xmlDocuments = documents.map(d => (d, MessageTransformers.parseXML(d)))

  val stream: Stream[Message] = Stream.concat(documents).append(stream)
  val xml: Stream[(Message, Document)] = Stream.concat(xmlDocuments).append(xml)

  def iterator = stream.iterator
  def xmlIterator = xml.iterator

}
