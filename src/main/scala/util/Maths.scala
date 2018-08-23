package util

object Maths {
  def recSum[T : Numeric](s : Iterable[Iterable[T]]) : List[T] = {
    val goodVecs = s.filterNot(_.isEmpty)

    if(goodVecs.isEmpty)
      List.empty[T]
    else
      goodVecs.map(_.head).sum :: recSum(goodVecs.map(_.tail))
  }

  def sum(xs: List[Int]): Int = {
    xs match {
      case x :: tail => x + sum(tail) // if there is an element, add it to the sum of the tail
      case Nil => 0 // if there are no elements, then the sum is 0
    }
  }

  def median(s: Array[Double]): Double = {
    val (lower, upper) = s.sortWith(_<_).splitAt(s.size / 2)
    if (s.size % 2 == 0) (lower.last + upper.head) / 2.0 else upper.head
  }
}