package modules.common

/**
  * Object contains methods to create random strings
  * Created by astephens on 02.03.16.
  */
object RandomString {
  val numbers      = '0' to '9'                          // List of numbers
  val lowerLetters = 'a' to 'z'                          // List of lower letters
  val upperLetters = 'A' to 'Z'                          // List of upper letters
  val specialChar  = ('!', '§', '$', '_', '#', '~', '*') // Special characters which should be save to use
  val umlLetters   = ('ä', 'ö', 'ü', 'ß')                // German special vowels

  val random = scala.util.Random // random seed class

  /**
    * Returns a String with random letters
    * @param length Length of the String
    * @return
    */
  def randomAlphaString(length: Int): String = {
    randomStringFromCharList(length, lowerLetters ++ upperLetters)
  }

  /**
    * Returns a String with random numbers with starting zeros
    * @param length Length of the String
    * @return
    */
  def randomNumString(length: Int): String = {
    randomStringFromCharList(length, numbers)
  }

  /**
    * Returns a String with random Alphanumerical characters
    * @param length Length of the String
    * @return
    */
  def randomAlphaNumString(length: Int): String = {
    randomStringFromCharList(length, numbers ++ lowerLetters ++ upperLetters)
  }

  /**
    * Returns a String with random characters from the list
    * @param length Length of the String
    * @param chars  List of the Chars to choose from
    * @return
    */
  def randomStringFromCharList(length: Int, chars: Seq[Char]): String = {
    val sb = new StringBuilder
    for (i <- 1 to length) {
      val randomNum = util.Random.nextInt(chars.length)
      sb.append(chars(randomNum))
    }
    sb.toString
  }
}
