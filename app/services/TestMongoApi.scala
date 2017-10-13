package services

import play.modules.reactivemongo.ReactiveMongoApi

class TestMongoApi extends ReactiveMongoApi {
  override def driver = ???

  override def connection = ???

  override def database = ???

  override def asyncGridFS = ???

  override def db = ???

  override def gridFS = ???
}
