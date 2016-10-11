package models.tel.param

import models.tel.Subject

import scala.sys.process.Process

/**
  * Created by lzimmermann on 10/11/16.
  */
abstract class Param(name : String)

abstract class PredicativeParam(name : String) extends Param(name) {


  /**
    * Decides whether or not the value for this parameter is allowed
    * @param value The value that should be tested for its validity
    * @return Whether the provided value is valid for this parameter
    */
  def validate(value : String) : Boolean
}

abstract class GenerativeParam(name : String) extends PredicativeParam(name) {

  // Sequence of allowed values with respective clear text name
  def validate(value : String) = this.generate.contains(value)
  def generate : Set[String]
}



/*
 * Parameters obtained from files
 */
abstract class GenerativeParamFile(name: String, path : String)
  extends GenerativeParam(name)  with Subject[GenerativeParamFile] {

  /* Load the parameters from the file */
  def load() : Unit
}


class ExecGenParamFile(name : String,  path : String) extends GenerativeParamFile(name, path) {

  // Load file upon instantiation
  this.load()

  // Remembers parameter values that are allowed to be used
  private var allowed : Set[String] = _
  private var clearTextNames : Map[String, String] = _

  def load() : Unit = {
    clearTextNames = Map.empty

    this.allowed = Process(path).!!.split('\n').map { param =>
      val spt = param.split(' ')
      clearTextNames = clearTextNames + (spt(0) -> spt(1))
      spt(0)
    }.toSet
  }

  def generate = allowed.map(x => x)
  def decode(value : String) = this.clearTextNames(value)
}









