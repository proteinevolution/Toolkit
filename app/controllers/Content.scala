package controllers

import javax.inject.Inject

import play.api.i18n.MessagesApi
import play.api.mvc.Controller
import play.modules.reactivemongo.{ReactiveMongoComponents, MongoController, ReactiveMongoApi}

/**
  * Created by zin on 01.08.16.
  */
class Content @Inject() (val messagesApi: MessagesApi,
                         val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents {



}
