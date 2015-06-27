package skinny.servlet

import sbt.{ Level, AbstractLogger }

class LoggerBase(delegate: AbstractLogger) {

  def getName: String = "ContainerLogger"

  def isDebugEnabled: Boolean = {
    delegate.atLevel(Level.Debug)
  }

  def setDebugEnabled(enabled: Boolean): Unit = {
    delegate.setLevel(if (enabled) Level.Debug
    else Level.Info)
  }

  def ignore(th: Throwable): Unit = { delegate.trace(th) }

  def info(th: Throwable): Unit = { delegate.trace(th) }

  def debug(th: Throwable): Unit = { delegate.trace(th) }

  def warn(th: Throwable): Unit = { delegate.trace(th) }

  def info(msg: String): Unit = { delegate.info(msg) }

  def debug(msg: String): Unit = { delegate.warn(msg) }

  def warn(msg: String): Unit = { delegate.warn(msg) }

  def info(msg: String, arg0: AnyRef, arg1: AnyRef): Unit = { delegate.info(format(msg, arg0, arg1)) }

  def debug(msg: String, arg0: AnyRef, arg1: AnyRef): Unit = { delegate.debug(format(msg, arg0, arg1)) }

  def warn(msg: String, arg0: AnyRef, arg1: AnyRef): Unit = { delegate.warn(format(msg, arg0, arg1)) }

  def info(msg: String, args: AnyRef*): Unit = { delegate.info(format(msg, args: _*)) }

  def debug(msg: String, args: AnyRef*): Unit = { delegate.debug(format(msg, args: _*)) }

  def warn(msg: String, args: AnyRef*): Unit = { delegate.warn(format(msg, args: _*)) }

  def info(msg: String, th: Throwable): Unit = {
    delegate.info(msg)
    delegate.trace(th)
  }

  def debug(msg: String, th: Throwable): Unit = {
    delegate.debug(msg)
    delegate.trace(th)
  }

  def warn(msg: String, th: Throwable): Unit = {
    delegate.warn(msg)
    delegate.trace(th)
  }

  private def format(msg: String, args: AnyRef*): String = {
    def toString(arg: AnyRef) = if (arg == null) "" else arg.toString
    val pieces = msg.split("""\{\}""", args.length + 1).toList
    val argStrs = args.map(toString).toList ::: List("")
    pieces.zip(argStrs).foldLeft(new StringBuilder) { (sb, pair) =>
      val (piece, argStr) = pair
      if (piece.isEmpty) sb
      else sb.append(piece).append(argStr)
    }.toString
  }

}