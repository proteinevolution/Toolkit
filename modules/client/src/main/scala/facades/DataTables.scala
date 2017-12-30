import scala.scalajs.js

trait DataTablesOptions extends js.Object {
  val bProcessing: Boolean = js.native
  val bServerSide: Boolean = js.native
  val sAjaxSource: String = js.native
  val autoWidth: Boolean = js.native
  val lengthMenu: js.Array[Int] = js.native
  val iDisplayLength: Int = js.native
}

object DataTablesOptions {
  def apply(bProcessing: Boolean, bServerSide: Boolean, sAjaxSource: String): DataTablesOptions = {
    js.Dynamic.literal(bProcessing = bProcessing, bServerSide = bServerSide).asInstanceOf[DataTablesOptions]
  }
}
