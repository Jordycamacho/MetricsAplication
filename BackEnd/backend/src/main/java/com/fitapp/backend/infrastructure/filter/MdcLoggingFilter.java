package com.fitapp.backend.infrastructure.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(1)
@Slf4j
public class MdcLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String correlationId = Optional
            .ofNullable(request.getHeader("X-Correlation-ID"))
            .orElseGet(() -> UUID.randomUUID().toString());

        MDC.put("correlationId", correlationId);
        MDC.put("uri", request.getRequestURI());
        MDC.put("method", request.getMethod());
        response.setHeader("X-Correlation-ID", correlationId);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.put("responseMs", String.valueOf(System.currentTimeMillis() - start));
            MDC.put("status", String.valueOf(response.getStatus()));
            log.info("REQUEST_COMPLETE");
            MDC.clear(); 
        }
    }
}