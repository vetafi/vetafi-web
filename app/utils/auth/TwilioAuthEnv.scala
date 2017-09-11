package utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.BasicAuthProvider
import models.User

trait TwilioAuthEnv extends Env {
  type I = User
  type A = BasicAuthProvider
}
