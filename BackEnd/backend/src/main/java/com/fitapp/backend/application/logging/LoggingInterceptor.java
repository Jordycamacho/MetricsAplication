package com.fitapp.backend.application.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {
    
    private final SportServiceLogger sportLogger;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId != null) {
            sportLogger.setCorrelationId(correlationId);
        }
        
        log.info("HTTP_REQUEST | method={} | uri={} | query={} | correlationId={}",
                request.getMethod(), 
                request.getRequestURI(),
                request.getQueryString(),
                sportLogger.getCorrelationId());
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        log.info("HTTP_RESPONSE | status={} | uri={} | correlationId={}",
                response.getStatus(),
                request.getRequestURI(),
                sportLogger.getCorrelationId());
        
        if (ex != null) {
            log.error("HTTP_REQUEST_ERROR | uri={} | error={} | correlationId={}",
                    request.getRequestURI(),
                    ex.getMessage(),
                    sportLogger.getCorrelationId(),
                    ex);
        }
        
        sportLogger.clearCorrelationId();
    }
}