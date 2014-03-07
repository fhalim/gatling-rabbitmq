package net.fawad.rabbitmqloadgen

object Utilities {
  def time[T](action: => T) = {
    val start = System.currentTimeMillis()
    action
    System.currentTimeMillis() - start
  }
}
