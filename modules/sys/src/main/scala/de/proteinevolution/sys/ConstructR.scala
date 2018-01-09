package de.proteinevolution.sys

import cats.data.NonEmptyList
import de.proteinevolution.sys.ConstructR.Script
import de.proteinevolution.sys.ConstructR.Script.Commands

import scala.concurrent.Future

class ConstructR[T <: Script](cmds: Map[String, String] = Map.empty[String, String]) {

  def addCmd(key: String, cmd: String): ConstructR[T] = {
    new ConstructR[T](cmds + (key -> cmd))
  }

  def run(): List[Future[Int]] =
    new XeqtR(Commands(NonEmptyList.fromListUnsafe(cmds.values.toList)).asInstanceOf[T]).run()

}

object ConstructR {

  sealed trait Script {
    def params: NonEmptyList[String]
  }

  object Script {

    sealed trait ForwardingScript extends Script

    sealed trait TemplateAlignmentScript extends Script

    case class Commands(params: NonEmptyList[String]) extends Script

  }

}
