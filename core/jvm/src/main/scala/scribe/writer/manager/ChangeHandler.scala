package scribe.writer.manager

trait ChangeHandler {
  def change(): Unit
}
