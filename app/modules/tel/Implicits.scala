package modules.tel

/**
  * Created by lzimmermann on 09.12.16.
  */
object Implicits {

  class File {

    implicit class FileDecorators(f: better.files.File) {}
  }
}
