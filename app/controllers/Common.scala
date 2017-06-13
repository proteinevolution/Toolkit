package controllers

import java.nio.file.attribute.PosixFilePermission

import modules.CommonModule
import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import play.modules.reactivemongo.ReactiveMongoComponents
import play.api.mvc.Controller
import play.api.http.ContentTypes
import reactivemongo.bson.BSONDocument
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ Await, Future }

/**
  *
  * Created by snam on 12.11.16.
  */
private[controllers] trait Common extends Controller with ContentTypes with ReactiveMongoComponents with CommonModule {

  var loggedOut = true

  protected def CheckBackendPath(implicit request: RequestHeader): Boolean = {
    request.headers.get("referer").getOrElse("").matches("http://" + request.host + "/@/backend.*")
  }

  protected def NoCache(res: Result): Result = res.withHeaders(
    CACHE_CONTROL -> "no-cache, no-store, must-revalidate",
    EXPIRES       -> "0"
  )

  protected val filePermissions = Set(
    PosixFilePermission.OWNER_EXECUTE,
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE,
    PosixFilePermission.GROUP_EXECUTE,
    PosixFilePermission.GROUP_READ,
    PosixFilePermission.GROUP_WRITE
  )
  val forwardForm = Form(
    mapping(
      "checkboxes" -> seq(number)
    )(ForwardForm.apply)(ForwardForm.unapply)
  )

  def matchSuperUserToPW(username: String, password: String): Future[Boolean] = {

    findUser(BSONDocument("userData.nameLogin" -> username)).map {

      case Some(user) if user.checkPassword(password) && user.isSuperuser => true
      case None                                                           => false

    }

  }

  def MaintenanceSecured[A]()(action: Action[A]): Action[A] = Action.async(action.parser) { request =>
    request.headers
      .get("Authorization")
      .flatMap { authorization =>
        authorization.split(" ").drop(1).headOption.filter { encoded =>
          new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
            case u :: p :: Nil if Await.result(matchSuperUserToPW(u, p), scala.concurrent.duration.Duration.Inf) =>
              true
            case _ => false
          }
        }
      }
      .map(_ => action(request))
      .getOrElse {
        Future.successful(Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Secured Area""""))
        //Future.successful(BadRequest())
      }
  }
}
// Exceptions
case class FileException(message: String) extends Exception(message)

case class ForwardForm(checkboxes: Seq[Int])
