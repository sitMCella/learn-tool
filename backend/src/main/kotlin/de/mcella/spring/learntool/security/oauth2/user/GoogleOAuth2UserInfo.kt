package de.mcella.spring.learntool.security.oauth2.user

import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull

data class GoogleOAuth2UserInfo(
    @field:NotNull @field:NotEmpty val attributes: Map<String, Any>
) : OAuth2UserInfo {
    override fun getId(): String {
        return attributes["sub"] as String
    }

    override fun getName(): String {
        return attributes["name"] as String
    }

    override fun getEmail(): String {
        return attributes["email"] as String
    }

    override fun getImageUrl(): String {
        return attributes["picture"] as String
    }
}
