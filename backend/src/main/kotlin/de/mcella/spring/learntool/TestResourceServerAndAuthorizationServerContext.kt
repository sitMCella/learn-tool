package de.mcella.spring.learntool

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer

@Configuration
@EnableResourceServer
@EnableAuthorizationServer
@EnableWebSecurity
@ConditionalOnProperty(prefix = "service", name = ["mock"], havingValue = "true")
class TestResourceServerAndAuthorizationServerContext : AuthorizationServerConfigurerAdapter() {
    override fun configure(clients: ClientDetailsServiceConfigurer) {
        clients.inMemory().withClient("1").secret("secret").scopes("scope")
    }
}
