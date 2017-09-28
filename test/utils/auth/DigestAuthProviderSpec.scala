package utils.auth

import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.PasswordHasherRegistry
import org.mockito.Mockito
import play.api.test.PlaySpecification

class DigestAuthProviderSpec  extends PlaySpecification {
  "the digest auth provider" should {
    "authenticate correct requests" in {
      val mockAuthInfoRepository = Mockito.mock(classOf[AuthInfoRepository])
      val mockPasswordHasherRegistry = Mockito.mock(classOf[PasswordHasherRegistry])
      new DigestAuthProvider(mockAuthInfoRepository, mockPasswordHasherRegistry)
    }
  }
}
