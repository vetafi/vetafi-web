package utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.DummyAuthenticator
import models.TwilioUser

trait TwilioAuthEnv extends Env {
  type I = TwilioUser
  // We dont use a real authenticator since each
  // request contains the authentication information.
  type A = DummyAuthenticator
}
