package com.veggieshop.config;

import com.veggieshop.core.exception.ErrorResponseFactory;
import com.veggieshop.core.exception.ExceptionMappingService;
import com.veggieshop.core.exception.DefaultExceptionMappingService;
import com.veggieshop.core.exception.ProblemDetails;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the exception-mapping stack:
 * <ul>
 *   <li>{@link ErrorProps} for runtime customization (typeBase, defaultFormat).</li>
 *   <li>{@link DefaultExceptionMappingService} as the central Throwable→shape mapper.</li>
 *   <li>{@link ProblemDetails} builder for RFC7807 responses.</li>
 *   <li>{@link ErrorResponseFactory} builder for ApiResponse-based errors.</li>
 * </ul>
 *
 * Beans are {@code @ConditionalOnMissingBean} to remain override-friendly.
 */
@Configuration
@EnableConfigurationProperties(ErrorProps.class)
public class ExceptionMappingConfig {

    @Bean
    @ConditionalOnMissingBean(ExceptionMappingService.class)
    public ExceptionMappingService exceptionMappingService(ErrorProps errorProps) {
        // Preferred constructor – mapper derives "type" links from ErrorProps.typeBase()
        return new DefaultExceptionMappingService(errorProps);
    }

    @Bean
    @ConditionalOnMissingBean(ProblemDetails.class)
    public ProblemDetails problemDetails(ExceptionMappingService mappingService,
                                         ErrorProps errorProps) {
        // ProblemDetails uses mappingService; may consult ErrorProps internally if needed
        return new ProblemDetails(mappingService, errorProps);
    }

    @Bean
    @ConditionalOnMissingBean(ErrorResponseFactory.class)
    public ErrorResponseFactory errorResponseFactory(ExceptionMappingService mappingService,
                                                     ErrorProps errorProps) {
        // ApiResponse error builder; uses ErrorProps for consistent "type" URLs in messages/docs
        return new ErrorResponseFactory(mappingService, errorProps);
    }
}
