package de.proteinevolution.tel

object Implicits {

  class File {

    implicit class FileDecorators(f: better.files.File) {}
  }
}
