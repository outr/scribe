package scribe

object Test {
  def main(args: Array[String]): Unit = {
    val now = System.currentTimeMillis()
    println(sf"$now%tY - Wahoo!")
  }
}