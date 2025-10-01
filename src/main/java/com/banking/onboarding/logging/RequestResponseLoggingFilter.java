package com.banking.onboarding.logging;

import com.banking.onboarding.service.CorrelationIdService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Comprehensive Request/Response Logging Filter
 * Logs all inbound requests and outbound responses with correlation IDs
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class RequestResponseLoggingFilter implements Filter {

    private final LoggingEventService loggingEventService;
    private final CorrelationIdService correlationIdService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Generate correlation ID and trace ID
        String correlationId = correlationIdService.getCurrentCorrelationId();
        String traceId = generateTraceId();

        // Set correlation ID in response headers
        httpResponse.setHeader("X-Correlation-ID", correlationId);
        httpResponse.setHeader("X-Trace-ID", traceId);

        // Create request wrapper to capture request body
        RequestWrapper requestWrapper = new RequestWrapper(httpRequest);
        
        // Create response wrapper to capture response body
        ResponseWrapper responseWrapper = new ResponseWrapper(httpResponse);

        long startTime = System.currentTimeMillis();

        try {
            // Log inbound request
            logInboundRequest(requestWrapper, correlationId, traceId);

            // Process the request
            chain.doFilter(requestWrapper, responseWrapper);

        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log outbound response
            logOutboundResponse(requestWrapper, responseWrapper, correlationId, traceId, duration);
        }
    }

    private void logInboundRequest(RequestWrapper requestWrapper, String correlationId, String traceId) {
        try {
            Map<String, String> headers = new HashMap<>();
            requestWrapper.getHeaderNames().asIterator().forEachRemaining(headerName -> {
                headers.put(headerName, requestWrapper.getHeader(headerName));
            });

            String requestBody = requestWrapper.getBody();
            
            loggingEventService.logInboundRequest(
                requestWrapper.getMethod(),
                requestWrapper.getRequestURI(),
                headers,
                requestBody,
                correlationId,
                traceId
            );

        } catch (Exception e) {
            log.error("Failed to log inbound request: {}", e.getMessage(), e);
        }
    }

    private void logOutboundResponse(RequestWrapper requestWrapper, ResponseWrapper responseWrapper, 
                                   String correlationId, String traceId, long duration) {
        try {
            Map<String, String> headers = new HashMap<>();
            responseWrapper.getHeaderNames().forEach(headerName -> {
                headers.put(headerName, responseWrapper.getHeader(headerName));
            });

            String responseBody = responseWrapper.getBody();
            
            loggingEventService.logOutboundResponse(
                requestWrapper.getMethod(),
                requestWrapper.getRequestURI(),
                responseWrapper.getStatus(),
                headers,
                responseBody,
                correlationId,
                traceId,
                duration
            );

        } catch (Exception e) {
            log.error("Failed to log outbound response: {}", e.getMessage(), e);
        }
    }

    private String generateTraceId() {
        return "TRACE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * Request wrapper to capture request body
     */
    private static class RequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        private byte[] body;

        public RequestWrapper(HttpServletRequest request) throws IOException {
            super(request);
            this.body = StreamUtils.copyToByteArray(request.getInputStream());
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new ServletInputStream() {
                private int index = 0;

                @Override
                public boolean isFinished() {
                    return index >= body.length;
                }

                @Override
                public boolean isReady() {
                    return true;
                }

                @Override
                public void setReadListener(ReadListener readListener) {
                    // Not implemented
                }

                @Override
                public int read() throws IOException {
                    if (index >= body.length) {
                        return -1;
                    }
                    return body[index++] & 0xFF;
                }
            };
        }

        public String getBody() {
            return new String(body, StandardCharsets.UTF_8);
        }
    }

    /**
     * Response wrapper to capture response body
     */
    private static class ResponseWrapper extends jakarta.servlet.http.HttpServletResponseWrapper {
        private ByteArrayServletOutputStream outputStream;
        private PrintWriter writer;

        public ResponseWrapper(HttpServletResponse response) {
            super(response);
            this.outputStream = new ByteArrayServletOutputStream();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            return outputStream;
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            if (writer == null) {
                writer = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            }
            return writer;
        }

        @Override
        public void flushBuffer() throws IOException {
            if (writer != null) {
                writer.flush();
            }
            outputStream.flush();
        }

        public String getBody() {
            try {
                flushBuffer();
                return outputStream.toString();
            } catch (IOException e) {
                return "Error reading body: " + e.getMessage();
            }
        }
    }

    /**
     * Custom ServletOutputStream to capture response body
     */
    private static class ByteArrayServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // Not implemented
        }

        @Override
        public void write(int b) throws IOException {
            byteArrayOutputStream.write(b);
        }

        @Override
        public void flush() throws IOException {
            byteArrayOutputStream.flush();
        }

        @Override
        public void close() throws IOException {
            byteArrayOutputStream.close();
        }

        @Override
        public String toString() {
            return byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        }
    }
}
