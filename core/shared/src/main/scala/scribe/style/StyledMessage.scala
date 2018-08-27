package scribe.style

case class StyledMessage(blocks: List[StyledBlock]) extends ContainerBlock

trait StyledBlock {
  def plainText: String
}

case class StringBlock(value: String) extends StyledBlock {
  override def plainText: String = value
}

trait ContainerBlock extends StyledBlock {
  def blocks: List[StyledBlock]

  override lazy val plainText: String = blocks.map(_.plainText).mkString
}

case class ColoredBlock(color: SimpleColor, blocks: List[StyledBlock]) extends ContainerBlock

trait SimpleColor