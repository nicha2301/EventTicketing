package com.eventticketing.backend.config

import com.eventticketing.backend.interceptor.RateLimitInterceptor
import com.eventticketing.backend.util.Constants.ApiPaths
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.HttpMessageConverter
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.charset.StandardCharsets
import org.springframework.beans.factory.annotation.Value

@Configuration
@EnableConfigurationProperties
class WebConfig(
    private val rateLimitInterceptor: RateLimitInterceptor,
    @Value("\${app.upload.dir:\${user.home}/event-ticketing/uploads}") private val uploadDir: String
) : WebMvcConfigurer {
    
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(rateLimitInterceptor)
            .addPathPatterns("${ApiPaths.AUTH_BASE}/**")
    }

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
    }
    
    override fun configureMessageConverters(converters: MutableList<HttpMessageConverter<*>>) {
        converters.add(StringHttpMessageConverter(StandardCharsets.UTF_8))
        super.configureMessageConverters(converters)
    }
    
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler("/api/files/**")
                .addResourceLocations("file:$uploadDir/")
                .setCachePeriod(3600)
                .resourceChain(true)
    }
    
    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        
        restTemplate.messageConverters.add(0, StringHttpMessageConverter(StandardCharsets.UTF_8))
        
        return restTemplate
    }
} 