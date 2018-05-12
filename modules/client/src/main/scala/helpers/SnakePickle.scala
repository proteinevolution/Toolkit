package helpers

object SnakePickle extends upickle.AttributeTagged {
  
  def camelToSnake(s: String): String = {
    s.split("(?=[A-Z])", -1).map(_.toLowerCase).mkString("_")
  }
  def snakeToCamel(s: String): String = {
    val res = s.split("_", -1).map(x => x(0).toUpper + x.drop(1)).mkString
    s(0).toLower + res.drop(1)
  }

  override def objectAttributeKeyReadMap(s: CharSequence): String =
    snakeToCamel(s.toString)
  override def objectAttributeKeyWriteMap(s: CharSequence): String =
    camelToSnake(s.toString)

  override def objectTypeKeyReadMap(s: CharSequence): String =
    snakeToCamel(s.toString)
  override def objectTypeKeyWriteMap(s: CharSequence): String =
    camelToSnake(s.toString)

}
