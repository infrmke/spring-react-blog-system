package br.com.spring_react.blog.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class RequestResponseLoggingFilter implements Filter {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // request e response copiados com wrapper
        ContentCachingRequestWrapper requestWrapper =
                new ContentCachingRequestWrapper((HttpServletRequest) request, 64000);
        ContentCachingResponseWrapper responseWrapper =
                new ContentCachingResponseWrapper((HttpServletResponse) response);

        // o tempo começa a contar
        long startTime = System.currentTimeMillis();
        chain.doFilter(requestWrapper, responseWrapper);
        long duration = System.currentTimeMillis() - startTime;

        // exibe method, URI, status e timestamp (estilo Morgan)
        String logMessage = String.format("METHOD=%s; URI=%s; STATUS=%d; DURATION=%dms",
                requestWrapper.getMethod(),
                requestWrapper.getRequestURI(),
                responseWrapper.getStatus(),
                duration);

        LOGGER.info(logMessage);

        // entrega a resposta ao cliente
        responseWrapper.copyBodyToResponse();
    }
}
