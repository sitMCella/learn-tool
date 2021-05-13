package de.mcella.spring.learntool

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebMvcConfiguration : WebMvcConfigurer {
    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**").allowedOrigins("http://localhost:3000").allowedMethods("*")
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry
                .addResourceHandler("/static/**")
                .addResourceLocations("classpath:static/")
    }
}
