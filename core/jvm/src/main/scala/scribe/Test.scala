package scribe

object Test {
  def main(args: Array[String]): Unit = {
    val now = System.currentTimeMillis()
    scribe.info(sf"$now%tY.$now%tm.$now%td - Wahoo!")
  }
}