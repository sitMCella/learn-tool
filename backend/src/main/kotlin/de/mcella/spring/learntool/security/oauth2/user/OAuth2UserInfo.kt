package de.mcella.spring.learntool.security.oauth2.user

interface OAuth2UserInfo {
    fun getId(): String

    fun getName(): String

    fun getEmail(): String

    fun getImageUrl(): String
}
