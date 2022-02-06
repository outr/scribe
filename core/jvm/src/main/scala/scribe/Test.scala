package scribe

object Test {
  def main(args: Array[String]): Unit = {
    scribe.info("This is a test", new Throwable("Argh!"), "Aha!")
  }
}
