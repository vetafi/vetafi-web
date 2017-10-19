package modules

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import play.modules.reactivemongo.ReactiveMongoApi
import services.BiscuitPasswordMongoApi

class ProdModule extends AbstractModule with ScalaModule {
  override def configure(): Unit = {
    bind[ReactiveMongoApi].to[BiscuitPasswordMongoApi]
  }
}
