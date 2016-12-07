package modules.tel.runscripts


import better.files._

/**
  * Created by lzimmermann on 10/19/16.
  */


/**
  * Represents one particular runscript, specified by the path of the corresponding file.
  * Instances should be created via the companion object.
  *
  * @param path The path to the runscript that belongs to the instance of this class
  */
/*
class Runscript(path : String) {

  private val f = path.toFile

  // Maps each output type to all files that represent this type
  private var outputs : Map[String, Set[String]] = _
  this.load()

  /**
    * Returns the Output types of the runscript as a set of Strings.
    * @return
    */
  def outputTypes = this.outputs.keySet




  // TODO Also support inputs
  /**
    * Reloads the runscripts and specified information
    */
  def load() : Unit = {

    this.outputs = this.f.lineIterator
      .withFilter(_.startsWith("#%OUT"))  //
      .foldLeft(Map.empty[String, Set[String]].withDefaultValue(Set.empty[String])) { (a,b) =>
      val spt = b.split("\\s+")
      a.updated(spt(1), a(spt(1)) + spt(2))
    }
  }
}


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
        currentMap
      }

 */