package scribe

trait MinimumLevel {
  def logger: Logger

  def minimumLevel: Level
}

object MinimumLevel {
  implicit class FromString(in: (String, Level)) extends MinimumLevel {
    override def logger: Logger = Logger(in._1)

    override def minimumLevel: Level = in._2
  }

  implicit class FromClass(in: (Class[_], Level)) extends MinimumLevel {
    override def logger: Logger = Logger(in._1.getName)

    override def minimumLevel: Level = in._2
  }

  implicit class FromLogger(in: (Logger, Level)) extends MinimumLevel {
    override def logger: Logger = in._1

    override def minimumLevel: Level = in._2
  }

  implicit class FromId(in: (Long, Level)) extends MinimumLevel {
    override def logger: Logger = Logger(in._1)

    override def minimumLevel: Level = in._2
  }
}