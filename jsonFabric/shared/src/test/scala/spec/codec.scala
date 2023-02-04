package spec

import fabric._
import fabric.rw.ReaderWriter.stringRW

import scala.util.Try

// TODO should push either and list reader/writer down to fabric library
object codec {

  import fabric.rw._
  import scribe.json.event._

  implicit def eitherReaderWriter[A, B](implicit rwA: RW[A], rwB: RW[B]): ReaderWriter[Either[A, B]] =
    ReaderWriter.apply(_.fold(_.toValue, _.toValue), v => {
      Try(rwA.write(v)).toEither.left.map(_ => rwB.write(v)).swap
    })

  implicit def listRW[A](implicit rwA: RW[A]): RW[List[A]] = ReaderWriter.apply(xs => Arr(xs.map(_.toValue).toVector), _.asVector.toList.map(rwA.write))

  implicit val te: RW[TraceElement] = ccRW[TraceElement]
  implicit val trw: RW[Trace] = ccRW[Trace]

  implicit val e1 = eitherReaderWriter[String, List[String]]
  implicit val e2 = eitherReaderWriter[Trace, List[Trace]]
  implicit val drw: Reader[DataDogRecord] = ccR[DataDogRecord]


}
