package services

import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.{ DB, DefaultDB }

import scala.concurrent.Future

class TestMongoApi extends ReactiveMongoApi {
  override def driver = ???

  override def connection = ???

  override def database: Future[DefaultDB] = {
    Future.successful(DefaultDB("testDB", null))
  }

  override def asyncGridFS = ???

  override def db = ???

  override def gridFS = ???
}
