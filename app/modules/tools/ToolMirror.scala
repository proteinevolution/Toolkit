package modules.tools

import java.io.File
import models.tools._
import org.clapper.classutil.ClassFinder
import reflect.runtime.universe
import reflect.macros.Universe


/**
 *
 * Created by snam on 27.08.16.
 */

final class ToolMirror {

  // this method could be used in the Toolmatcher but unfortunately the inputForm is not inherent in the ToolModel trait
  // TODO either abstract over tuple arity and get the inputForm somehow into the trait or find another solution
  // TODO 2 besides the ToolMatcher, we wish to generate the navigation with the template engine from a dynamic list of tools

  def invokeToolName(tool :String) {

    val capitalName = tool.capitalize
    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    val module = runtimeMirror.staticModule(s"models.tools.ToolModel.$capitalName")
    val obj = runtimeMirror.reflectModule(module)

    println("Tool: " + obj.instance.getClass.toString)

    val someTrait: ToolModel = obj.instance.asInstanceOf[ToolModel]
    println(someTrait.toolNameAbbreviation)

  }


  def listToolModels() = {

    val tpe = universe.typeOf[ToolModel]
    val clazz = tpe.typeSymbol.asClass
    clazz.knownDirectSubclasses.foreach(println)
    println(clazz.knownDirectSubclasses)

  }


  /**
   * gets methods from class or trait
   */

  def getMembers = {

    val typ = universe.typeOf[ToolModel]

    typ.members.collect{
     case m => println("Membertest: " + m)
    }
  }


  // This uses the clapper library and gets a dynamic list of all objects which implement ToolModel
  // this seems to be impossible with the current version of Scala's reflection API: http://stackoverflow.com/questions/28500804/get-all-classes-of-a-package


  // This is really slow at the moment. Try to tweak this somehow.

  // A possible alternative could be sealed traits with Scala macros

  def findInstances() = {

    val classpath = List("./").map(new File(_))
    val finder = ClassFinder(classpath)
    val classes = finder.getClasses().filter(_.implements("models.tools.ToolModel"))
    classes.foreach(println)

    println(classes(0).name)
    val runtimeMirror = universe.runtimeMirror(getClass.getClassLoader)
    val module = runtimeMirror.staticModule(classes(0).name)
    val obj = runtimeMirror.reflectModule(module)
    val someTrait2: ToolModel = obj.instance.asInstanceOf[ToolModel]
    println(someTrait2.toolNameAbbreviation)

  }


}
