package controllers

import org.scalatestplus.play._
import play.api.test.Helpers._
import play.api.test.FakeRequest

class UptimeSpec extends PlaySpec {

  "UptimeController#uptime" should {
    "return a valid result with action" in {
      val controller = new UptimeController(stubControllerComponents())
      val result     = controller.uptime(FakeRequest())
      status(result) mustBe OK
      contentAsString(result) must include("ms")
    }
  }

}
