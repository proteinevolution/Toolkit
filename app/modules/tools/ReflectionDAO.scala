package modules.tools

import models.tools._
import org.clapper.classutil.ClassFinder
import reflect.runtime.universe



/**
 *
 * Created by snam on 27.08.16.
 */

class ReflectionDAO {

  // this method could be used in the Toolmatcher but unfortunately the inputForm is not inherent in the ToolModel trait
  // TODO either abstract over tuple arity and get the inputForm somehow into the trait or find another solution
  // TODO 2 besides the ToolMatcher, we wish to generate the navigation with the template engine from a dynamic list of tools

  def invokeToolName(tool :String) {

    val capitalName = tool.capitalize
    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    val module = runtimeMirror.staticModule(s"models.tools.$capitalName")
    val obj = runtimeMirror.reflectModule(module)

    println("Tool: " + obj.instance.getClass.toString)

    val someTrait: ToolModel = obj.instance.asInstanceOf[ToolModel]
    println(someTrait.toolNameAbbreviation)

  }


  // This uses the clapper library but does not work so far TODO get a dynamic list of all objects which implement ToolModel
  def printTest() = {

    val finder = ClassFinder()
    val found = finder.getClasses().filter(_.isConcrete).filter(_.implements("models.tools.ToolModel"))

    found.foreach(println)

  }

}
