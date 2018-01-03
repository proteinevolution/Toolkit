package de.proteinevolution.sys

import cats.data.NonEmptyList
import de.proteinevolution.sys.ConstructR.Script
import de.proteinevolution.sys.ConstructR.Script.Commands

class ConstructR[T <: Script](cmds: Map[String, String] = Map.empty[String, String]) {

  def addCmd(key: String, cmd: String): ConstructR[T] = {
    new ConstructR[T](cmds + (key -> cmd))
  }

  def run(): Int = new XeqtR(Commands(cmds.values.toList: _*).asInstanceOf[T]).run()

}

object ConstructR {

  sealed trait Script {
    def params: NonEmptyList[String]
  }

  object Script {

    sealed trait ForwardingScript extends Script

    sealed trait TemplateAlignmentScript extends Script

    case class Commands(params: String*) extends Script

  }

}
