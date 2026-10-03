package com.myeshopping.orderservice.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(1)
public class CorrelationIdFilter implements jakarta.servlet.Filter {
    
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String correlationId = (request instanceof HttpServletRequest httpRequest)
                ? httpRequest.getHeader(CORRELATION_ID_HEADER)
                : null;
        
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        
        long start = System.currentTimeMillis();

        try {
            MDC.put(MDC_KEY, correlationId);
            MDC.put("traceId", correlationId);
            chain.doFilter(request, response);
        } finally {
            if (request instanceof HttpServletRequest req 
                    && response instanceof HttpServletResponse res 
                    && !req.getRequestURI().startsWith("/actuator") 
                ) {

                log.info("{} {} -> {} ({} ms)", req.getMethod(), req.getRequestURI(), res.getStatus(), System.currentTimeMillis() - start);
            }
            
            MDC.remove(MDC_KEY);
            MDC.remove("traceId");
        }

    }
}

