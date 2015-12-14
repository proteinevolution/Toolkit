package models

/**
 * Created by lzimmermann on 14.12.15.
 */
package models

case class Task(id: Long, label: String)

object Task {

  def all(): List[Task] = Nil

  def create(label: String) {}

  def delete(id: Long) {}

}