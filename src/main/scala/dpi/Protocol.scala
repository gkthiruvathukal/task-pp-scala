package dpi

import upickle.default.{ReadWriter, macroRW}

case class Work(points: Long)

case class Result(inside: Long)

object Work:
  given ReadWriter[Work] = macroRW

object Result:
  given ReadWriter[Result] = macroRW
