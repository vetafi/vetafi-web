package services

import play.api.cache.CacheApi

import scala.concurrent.duration.Duration
import scala.reflect.ClassTag

/**
 * Fake cache for testing.
 */
class TestCacheApiImpl extends CacheApi {
  override def set(key: String, value: Any, expiration: Duration): Unit = {

  }

  override def remove(key: String): Unit = {

  }

  override def getOrElse[A](key: String, expiration: Duration)(orElse: => A)(implicit evidence$1: ClassTag[A]): A = {
    orElse
  }

  override def get[T](key: String)(implicit evidence$2: ClassTag[T]): Option[T] = {
    None
  }
}
