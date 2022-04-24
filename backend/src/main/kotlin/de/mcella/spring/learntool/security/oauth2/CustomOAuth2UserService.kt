package de.mcella.spring.learntool.security.oauth2

import de.mcella.spring.learntool.security.UserPrincipal
import de.mcella.spring.learntool.security.oauth2.user.OAuth2UserInfo
import de.mcella.spring.learntool.security.oauth2.user.OAuth2UserInfoFactory
import de.mcella.spring.learntool.security.oauth2.user.exceptions.OAuth2AuthenticationProcessingException
import de.mcella.spring.learntool.user.AuthProvider
import de.mcella.spring.learntool.user.UserEntity
import de.mcella.spring.learntool.user.UserRepository
import org.springframework.security.authentication.InternalAuthenticationServiceException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

@Service
class CustomOAuth2UserService(val userRepository: UserRepository, val oAuth2UserInfoFactory: OAuth2UserInfoFactory) : DefaultOAuth2UserService() {
    override fun loadUser(oAuth2UserRequest: OAuth2UserRequest): OAuth2User {
        val oAuth2User = super.loadUser(oAuth2UserRequest)
        return try {
            processOAuth2User(oAuth2UserRequest, oAuth2User)
        } catch (ex: AuthenticationException) {
            throw ex
        } catch (ex: Exception) {
            throw InternalAuthenticationServiceException(ex.message, ex.cause)
        }
    }

    private fun processOAuth2User(oAuth2UserRequest: OAuth2UserRequest, oAuth2User: OAuth2User): OAuth2User {
        val oAuth2UserInfo: OAuth2UserInfo = oAuth2UserInfoFactory.getOAuth2UserInfo(oAuth2UserRequest.clientRegistration.registrationId, oAuth2User.attributes)
        if (oAuth2UserInfo.getEmail().isEmpty()) {
            throw OAuth2AuthenticationProcessingException("Email not found from OAuth2 provider")
        }
        val userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail())
        var user: UserEntity
        if (userOptional.isPresent) {
            user = userOptional.get()
            if (user.authProvider != AuthProvider.valueOf(oAuth2UserRequest.clientRegistration.registrationId)) {
                throw OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                        user.authProvider.toString() + " account. Please use your " + user.authProvider.toString() +
                        " account to login.")
            }
            user = updateExistingUser(user, oAuth2UserInfo)
        } else {
            user = registerNewUser(oAuth2UserRequest, oAuth2UserInfo)
        }
        val attributes = oAuth2User.attributes
        return UserPrincipal.create(user, attributes)
    }

    private fun registerNewUser(oAuth2UserRequest: OAuth2UserRequest, oAuth2UserInfo: OAuth2UserInfo): UserEntity {
        val user = UserEntity(
                null,
                oAuth2UserInfo.getName(),
                oAuth2UserInfo.getEmail(),
                oAuth2UserInfo.getImageUrl(),
                true,
                "",
                AuthProvider.valueOf(oAuth2UserRequest.clientRegistration.registrationId),
                oAuth2UserInfo.getId()
        )
        return userRepository.save(user)
    }

    private fun updateExistingUser(existingUser: UserEntity, oAuth2UserInfo: OAuth2UserInfo): UserEntity {
        val updatedUser = UserEntity(
                existingUser.id,
                oAuth2UserInfo.getName(),
                existingUser.email,
                oAuth2UserInfo.getImageUrl(),
                existingUser.emailVerified,
                existingUser.password,
                existingUser.authProvider,
                existingUser.authProviderId
        )
        return userRepository.save(updatedUser)
    }
}
