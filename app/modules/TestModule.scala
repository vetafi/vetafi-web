package modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.api.cache.CacheApi
import play.modules.reactivemongo.ReactiveMongoApi
import services.{ TestCacheApiImpl, TestMongoApi }

class TestModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bind[ReactiveMongoApi].to[TestMongoApi]
  }
}
