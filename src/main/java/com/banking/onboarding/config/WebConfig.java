package com.banking.onboarding.config;

import com.banking.onboarding.interceptor.CorrelationIdInterceptor;
import com.banking.onboarding.interceptor.RequestResponseLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web configuration for interceptors
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final CorrelationIdInterceptor correlationIdInterceptor;
    private final RequestResponseLoggingInterceptor requestResponseLoggingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add correlation ID interceptor first
        registry.addInterceptor(correlationIdInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**");
        
        // Add request/response logging interceptor
        registry.addInterceptor(requestResponseLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**");
    }
}
