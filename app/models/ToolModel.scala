package models

/**
  * Created by lukas on 1/16/16.
  */
abstract class ToolModel {

  // Specifies the Command line invocation of the tool
  // TODO Of course, this needs to be generalized
  val exec: Vector[CallComponent]

}
