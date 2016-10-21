package models.tel.runscripts

import javax.inject.{Inject, Named, Singleton}

import better.files._
import play.api.Logger


/**
  *
  * Created by lzimmermann on 10/19/16.
  */
@Singleton
class Types @Inject() (@Named("typesPath") path : String) {

  private val f  = path.toFile

  // Maps each type in the type file to its corresponding superclasses
  // Each type can have multiple superclasses
  private var typeClass : Map[String, Set[String]] = _
  this.load()

  def getTypeclass(t : String) = this.typeClass(t)


  /**
    * Reloads the type file. Can be invoked from outside the class when the type file changes its content
  */
  def load() : Unit = {
    this.typeClass = this.f.lineIterator
        .map(_.split('#')(0)) // Trim comment lines
        .withFilter(!_.trim().isEmpty) // Remove empty lines
        .foldLeft(Map.empty[String, Set[String]].withDefaultValue(Set.empty[String])) { (a,b) =>
        val spt = b.split(':')
        var currentMap : Map[String, Set[String]] = a

        // Traverse words after the colon
        spt(1).split("\\s+").withFilter(!_.trim().isEmpty).foreach { word =>
          currentMap = currentMap.updated(word, currentMap(word) + spt(0))
        }
        Logger.info("We have the following types" + currentMap.mkString)
        currentMap
      }
  }
}
