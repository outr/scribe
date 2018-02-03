package scribe

object Test {
  def main(args: Array[String]): Unit = {
    val count = 1000000
    val now = System.currentTimeMillis()
    val d = 12.3456

//    val start1 = System.currentTimeMillis()
//    (0 until count).toList.map(_ => f"$now%tY.$now%tm.$now%td $now%tH:$now%tM - Wahoo! $d%2.2f")
//    println(s"F Interpolation took: ${System.currentTimeMillis() - start1}ms")
//    val start2 = System.currentTimeMillis()
//    (0 until count).toList.map(_ => sf"$now{tY}.$now{tm}.$now{td} $now{tH}:$now{tM} - Wahoo! $d{##.##}")
//    println(s"SF Interpolation took: ${System.currentTimeMillis() - start2}ms")


//    println(f"$now%tY.$now%tm.$now%td - Wahoo! $d%2.2f")
    scribe.info(sf"$now{tY}.$now{tm}.$now{td} $now{tH}:$now{tM}:$now{tS} $now{tc} - Wahoo! $$$d{##.##}")
  }
}