package utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.DummyAuthenticator
import com.mohiva.play.silhouette.impl.providers.BasicAuthProvider
import models.User

trait TwilioAuthEnv extends Env {
  type I = User
  // We dont use a real authenticator since each
  // request contains the authentication information.
  type A = DummyAuthenticator
}
