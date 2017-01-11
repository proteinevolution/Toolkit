

/**
  * Created by snam on 05.01.17.
  */


import co.technius.scalajs.mithril._
import org.scalajs.dom
import org.scalajs.dom.html

import scala.scalajs.js


object Main extends Component {

  override val controller: js.Function = () => new Controller

  val view: js.Function = (ctrl: Controller) => js.Array(

  )

  class Controller {
    val static : String = m.route.param("static")
  }
}




object Toolkit extends js.JSApp {
  import js.Dynamic.{ global => g }


  def main(): Unit = {
    //m.mount(dom.document.getElementById("test"), Main)

    //m.route.mode = "hash"
    //m.route(dom.document.getElementById("main-content"), "/",
    //  "/" : )
    //'/tools/:toolname': m Toolkit, {isJob: false}
    //'/jobs/:jobID': m Toolkit, {isJob: true}

  }
}


/*

StaticRoute =
  controller: ->
    { static: m.route.param('static') }
  view: (controller) ->
    $.ajax(
      type: "GET"
      url: "/static/get/" + controller.static ).done (data) ->
        if [
          'sitemap'
          'reformat'
          'alnvizfrontend'
          'patSearch'
        ].indexOf(controller['static']) >= 0
          $('#content').empty().prepend data
        else
          $('body').empty().prepend data
        $(document).foundation()
        $("html, body").animate({ scrollTop: 0 }, "fast")


#setup routes to start w/ the `#` symbol
m.route.mode = 'hash'

m.route document.getElementById('main-content'), '/',
  '/' : Index
  '/tools/:toolname': m Toolkit, {isJob: false}
  '/jobs/:jobID': m Toolkit, {isJob: true}




# Miscellaneous code that is present across the whole web application
window.onfocus = ->
  titlenotifier.reset();
window.onclick = ->
  titlenotifier.reset();

 */