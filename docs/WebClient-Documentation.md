# WebClient Documentation - Banking Onboarding Service

## 🎯 Overview

WebClient is Spring's modern, non-blocking HTTP client that provides reactive programming capabilities. It's the recommended replacement for RestTemplate and offers superior performance, especially under high load scenarios typical in banking applications.

## 🏗️ Architecture

### WebClient in Our System

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   API Bridge    │───▶│   WebClient      │───▶│  External APIs  │
│   Service       │    │   (Non-blocking) │    │  (Fenergo, etc) │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │
         ▼                       ▼
┌─────────────────┐    ┌──────────────────┐
│ JWT Token       │    │ Reactive Streams │
│ Management      │    │ (Mono/Flux)      │
└─────────────────┘    └──────────────────┘
```

## 🔧 Configuration

### 1. Basic WebClient Configuration

```java
@Configuration
public class WebClientConfig {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> {
                    // Configure codecs
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10MB
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                })
                .baseUrl("http://localhost:8081") // Default base URL
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "Banking-Onboarding-Service/1.0")
                .build();
    }
}
```

### 2. Advanced WebClient Configuration

```java
@Configuration
public class AdvancedWebClientConfig {
    
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> {
                    // Memory configuration
                    configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024);
                    
                    // Enable logging for debugging
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                    
                    // Custom JSON codec
                    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder());
                    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder());
                })
                
                // Connection configuration
                .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                        .responseTimeout(Duration.ofSeconds(30))
                        .doOnConnected(conn -> 
                            conn.addHandlerLast(new ReadTimeoutHandler(30))
                                .addHandlerLast(new WriteTimeoutHandler(30))
                        )
                ))
                
                // Default headers
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "Banking-Onboarding-Service/1.0")
                
                // Filters for cross-cutting concerns
                .filter(ExchangeFilterFunctions.ofRequestProcessor(clientRequest -> {
                    log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
                    return Mono.just(clientRequest);
                }))
                .filter(ExchangeFilterFunctions.ofResponseProcessor(clientResponse -> {
                    log.info("Response: {}", clientResponse.statusCode());
                    return Mono.just(clientResponse);
                }))
                
                .build();
    }
}
```

### 3. Multiple WebClient Instances

```java
@Configuration
public class MultipleWebClientConfig {
    
    @Bean("fenergoWebClient")
    public WebClient fenergoWebClient() {
        return WebClient.builder()
                .baseUrl("http://fenergo-service:8080")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
    
    @Bean("authWebClient")
    public WebClient authWebClient() {
        return WebClient.builder()
                .baseUrl("http://auth-service:8080")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }
    
    @Bean("genericWebClient")
    public WebClient genericWebClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
    }
}
```

## 🚀 Basic Usage

### 1. Simple GET Request

```java
@Service
public class SimpleWebClientService {
    
    private final WebClient webClient;
    
    public SimpleWebClientService(WebClient webClient) {
        this.webClient = webClient;
    }
    
    // Synchronous GET
    public String getData(String id) {
        return webClient
                .get()
                .uri("/api/data/{id}", id)
                .retrieve()
                .bodyToMono(String.class)
                .block(); // Convert to blocking
    }
    
    // Asynchronous GET
    public Mono<String> getDataAsync(String id) {
        return webClient
                .get()
                .uri("/api/data/{id}", id)
                .retrieve()
                .bodyToMono(String.class);
    }
}
```

### 2. POST Request with Body

```java
@Service
public class PostWebClientService {
    
    private final WebClient webClient;
    
    // POST with JSON body
    public Mono<ApiResponse> postData(Object payload) {
        return webClient
                .post()
                .uri("/api/submit")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ApiResponse.class);
    }
    
    // POST with form data
    public Mono<String> postFormData(Map<String, String> formData) {
        return webClient
                .post()
                .uri("/api/form")
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(String.class);
    }
}
```

### 3. Request with Headers

```java
@Service
public class HeaderWebClientService {
    
    private final WebClient webClient;
    
    public Mono<ApiResponse> requestWithHeaders(String token, Object payload) {
        return webClient
                .post()
                .uri("/api/secure")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header("X-Correlation-ID", UUID.randomUUID().toString())
                .header("X-Request-Source", "banking-onboarding")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ApiResponse.class);
    }
}
```

## 🔄 Advanced Features

### 1. Error Handling

```java
@Service
public class ErrorHandlingWebClientService {
    
    private final WebClient webClient;
    
    public Mono<ApiResponse> requestWithErrorHandling(Object payload) {
        return webClient
                .post()
                .uri("/api/submit")
                .bodyValue(payload)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, response -> {
                    log.error("Client error: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new ClientException("Client error: " + body)));
                })
                .onStatus(HttpStatus::is5xxServerError, response -> {
                    log.error("Server error: {}", response.statusCode());
                    return response.bodyToMono(String.class)
                            .flatMap(body -> Mono.error(new ServerException("Server error: " + body)));
                })
                .bodyToMono(ApiResponse.class)
                .doOnError(throwable -> log.error("Request failed", throwable));
    }
}
```

### 2. Retry Mechanism

```java
@Service
public class RetryWebClientService {
    
    private final WebClient webClient;
    
    public Mono<ApiResponse> requestWithRetry(Object payload) {
        return webClient
                .post()
                .uri("/api/submit")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
                        .filter(throwable -> throwable instanceof WebClientResponseException)
                        .doBeforeRetry(retrySignal -> 
                            log.warn("Retrying request, attempt: {}", retrySignal.totalRetries() + 1)
                        ));
    }
    
    // Exponential backoff retry
    public Mono<ApiResponse> requestWithExponentialBackoff(Object payload) {
        return webClient
                .post()
                .uri("/api/submit")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .maxBackoff(Duration.ofSeconds(10))
                        .filter(throwable -> throwable instanceof WebClientResponseException));
    }
}
```

### 3. Timeout Configuration

```java
@Service
public class TimeoutWebClientService {
    
    private final WebClient webClient;
    
    public Mono<ApiResponse> requestWithTimeout(Object payload) {
        return webClient
                .post()
                .uri("/api/submit")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(Duration.ofSeconds(30))
                .doOnError(TimeoutException.class, throwable -> 
                    log.error("Request timed out after 30 seconds")
                );
    }
    
    // Different timeouts for different operations
    public Mono<ApiResponse> quickRequest(Object payload) {
        return webClient
                .post()
                .uri("/api/quick")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(Duration.ofSeconds(5));
    }
    
    public Mono<ApiResponse> longRunningRequest(Object payload) {
        return webClient
                .post()
                .uri("/api/long-running")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .timeout(Duration.ofMinutes(5));
    }
}
```

### 4. Circuit Breaker Pattern

```java
@Service
public class CircuitBreakerWebClientService {
    
    private final WebClient webClient;
    private final CircuitBreaker circuitBreaker;
    
    public CircuitBreakerWebClientService(WebClient webClient) {
        this.webClient = webClient;
        this.circuitBreaker = CircuitBreaker.ofDefaults("webclient-circuit-breaker");
    }
    
    public Mono<ApiResponse> requestWithCircuitBreaker(Object payload) {
        return circuitBreaker.executeSupplier(() -> 
            webClient
                .post()
                .uri("/api/submit")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .block() // Convert to blocking for circuit breaker
        ).map(response -> response);
    }
}
```

## 🔐 Authentication Integration

### 1. JWT Token Integration

```java
@Service
public class AuthenticatedWebClientService {
    
    private final WebClient webClient;
    private final JwtTokenService jwtTokenService;
    
    public Mono<ApiResponse> authenticatedRequest(Object payload, String scope) {
        return jwtTokenService.getToken(scope)
                .cast(JwtToken.class)
                .flatMap(token -> {
                    if (token.isValid()) {
                        return webClient
                                .post()
                                .uri("/api/secure")
                                .header(HttpHeaders.AUTHORIZATION, 
                                       token.getTokenType() + " " + token.getAccessToken())
                                .bodyValue(payload)
                                .retrieve()
                                .bodyToMono(ApiResponse.class);
                    } else {
                        return Mono.error(new AuthenticationException("Invalid token"));
                    }
                });
    }
}
```

### 2. Automatic Token Refresh

```java
@Service
public class AutoRefreshWebClientService {
    
    private final WebClient webClient;
    private final JwtTokenService jwtTokenService;
    
    public Mono<ApiResponse> requestWithAutoRefresh(Object payload, String scope) {
        return jwtTokenService.getToken(scope)
                .cast(JwtToken.class)
                .flatMap(token -> makeAuthenticatedRequest(payload, token))
                .onErrorResume(AuthenticationException.class, ex -> {
                    // Token expired, refresh and retry
                    return jwtTokenService.refreshToken(scope)
                            .cast(JwtToken.class)
                            .flatMap(newToken -> makeAuthenticatedRequest(payload, newToken));
                });
    }
    
    private Mono<ApiResponse> makeAuthenticatedRequest(Object payload, JwtToken token) {
        return webClient
                .post()
                .uri("/api/secure")
                .header(HttpHeaders.AUTHORIZATION, 
                       token.getTokenType() + " " + token.getAccessToken())
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ApiResponse.class);
    }
}
```

## 📊 Monitoring and Metrics

### 1. Request Metrics

```java
@Service
public class MetricsWebClientService {
    
    private final WebClient webClient;
    private final MeterRegistry meterRegistry;
    
    public Mono<ApiResponse> requestWithMetrics(Object payload) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        return webClient
                .post()
                .uri("/api/submit")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(ApiResponse.class)
                .doOnSuccess(response -> {
                    sample.stop(Timer.builder("webclient.request.duration")
                            .tag("status", "success")
                            .register(meterRegistry));
                })
                .doOnError(throwable -> {
                    sample.stop(Timer.builder("webclient.request.duration")
                            .tag("status", "error")
                            .tag("error", throwable.getClass().getSimpleName())
                            .register(meterRegistry));
                });
    }
}
```

### 2. Custom Filters for Monitoring

```java
@Component
public class MonitoringWebClientFilter implements ExchangeFilterFunction {
    
    private final MeterRegistry meterRegistry;
    
    @Override
    public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        return next.exchange(request)
                .doOnSuccess(response -> {
                    sample.stop(Timer.builder("webclient.request.duration")
                            .tag("method", request.method().name())
                            .tag("uri", request.url().getPath())
                            .tag("status", response.statusCode().toString())
                            .register(meterRegistry));
                })
                .doOnError(throwable -> {
                    sample.stop(Timer.builder("webclient.request.duration")
                            .tag("method", request.method().name())
                            .tag("uri", request.url().getPath())
                            .tag("status", "error")
                            .register(meterRegistry));
                });
    }
}
```

## 🔄 Reactive Programming Patterns

### 1. Parallel Requests

```java
@Service
public class ParallelWebClientService {
    
    private final WebClient webClient;
    
    public Mono<CombinedResponse> parallelRequests(String id) {
        Mono<EntityData> entityData = webClient
                .get()
                .uri("/api/entity/{id}", id)
                .retrieve()
                .bodyToMono(EntityData.class);
        
        Mono<ComplianceData> complianceData = webClient
                .get()
                .uri("/api/compliance/{id}", id)
                .retrieve()
                .bodyToMono(ComplianceData.class);
        
        Mono<RiskData> riskData = webClient
                .get()
                .uri("/api/risk/{id}", id)
                .retrieve()
                .bodyToMono(RiskData.class);
        
        return Mono.zip(entityData, complianceData, riskData)
                .map(tuple -> CombinedResponse.builder()
                        .entityData(tuple.getT1())
                        .complianceData(tuple.getT2())
                        .riskData(tuple.getT3())
                        .build());
    }
}
```

### 2. Sequential Requests with Dependencies

```java
@Service
public class SequentialWebClientService {
    
    private final WebClient webClient;
    
    public Mono<ProcessedData> sequentialRequests(String id) {
        return webClient
                .get()
                .uri("/api/entity/{id}", id)
                .retrieve()
                .bodyToMono(EntityData.class)
                .flatMap(entityData -> 
                    webClient
                        .post()
                        .uri("/api/validate")
                        .bodyValue(entityData)
                        .retrieve()
                        .bodyToMono(ValidationResult.class)
                )
                .flatMap(validationResult -> 
                    webClient
                        .post()
                        .uri("/api/process")
                        .bodyValue(validationResult)
                        .retrieve()
                        .bodyToMono(ProcessedData.class)
                );
    }
}
```

### 3. Batch Processing

```java
@Service
public class BatchWebClientService {
    
    private final WebClient webClient;
    
    public Flux<ApiResponse> batchRequests(List<Object> payloads) {
        return Flux.fromIterable(payloads)
                .flatMap(payload -> 
                    webClient
                        .post()
                        .uri("/api/submit")
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(ApiResponse.class)
                        .onErrorResume(throwable -> 
                            Mono.just(ApiResponse.error(throwable.getMessage(), 500))
                        )
                )
                .buffer(10) // Process in batches of 10
                .flatMap(batch -> 
                    Flux.fromIterable(batch)
                        .delayElements(Duration.ofMillis(100)) // Rate limiting
                );
    }
}
```

## 🧪 Testing

### 1. Unit Testing with MockWebServer

```java
@ExtendWith(MockitoExtension.class)
class WebClientServiceTest {
    
    private MockWebServer mockWebServer;
    private WebClient webClient;
    private WebClientService webClientService;
    
    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        
        webClientService = new WebClientService(webClient);
    }
    
    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }
    
    @Test
    void testGetData() {
        // Given
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"id\":\"123\",\"name\":\"Test\"}"));
        
        // When
        String result = webClientService.getData("123");
        
        // Then
        assertThat(result).isEqualTo("{\"id\":\"123\",\"name\":\"Test\"}");
    }
}
```

### 2. Integration Testing

```java
@SpringBootTest
@TestPropertySource(properties = {
    "webclient.base-url=http://localhost:8080"
})
class WebClientIntegrationTest {
    
    @Autowired
    private WebClientService webClientService;
    
    @Test
    void testRealApiCall() {
        // Given
        Object payload = Map.of("entityId", "TEST-123");
        
        // When
        Mono<ApiResponse> result = webClientService.postData(payload);
        
        // Then
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.isSuccess()).isTrue();
                    assertThat(response.getStatusCode()).isEqualTo(200);
                })
                .verifyComplete();
    }
}
```

## 🚀 Performance Optimization

### 1. Connection Pooling

```java
@Configuration
public class OptimizedWebClientConfig {
    
    @Bean
    public WebClient optimizedWebClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(
                    HttpClient.create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                        .responseTimeout(Duration.ofSeconds(30))
                        .option(ChannelOption.SO_KEEPALIVE, true)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .connectionProvider(ConnectionProvider.builder("webclient-pool")
                                .maxConnections(100)
                                .maxIdleTime(Duration.ofSeconds(30))
                                .maxLifeTime(Duration.ofMinutes(5))
                                .pendingAcquireTimeout(Duration.ofSeconds(10))
                                .evictInBackground(Duration.ofSeconds(120))
                                .build())
                ))
                .build();
    }
}
```

### 2. Caching

```java
@Service
public class CachedWebClientService {
    
    private final WebClient webClient;
    private final Cache<String, String> cache;
    
    public CachedWebClientService(WebClient webClient) {
        this.webClient = webClient;
        this.cache = Caffeine.newBuilder()
                .maximumSize(1000)
                .expireAfterWrite(Duration.ofMinutes(5))
                .build();
    }
    
    public Mono<String> getCachedData(String id) {
        String cached = cache.getIfPresent(id);
        if (cached != null) {
            return Mono.just(cached);
        }
        
        return webClient
                .get()
                .uri("/api/data/{id}", id)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(data -> cache.put(id, data));
    }
}
```

## 🔧 Configuration Properties

### application.properties

```properties
# WebClient Configuration
webclient.base-url=http://localhost:8080
webclient.timeout=30000
webclient.max-in-memory-size=10485760
webclient.max-connections=100
webclient.max-connections-per-route=20
webclient.connection-timeout=5000
webclient.read-timeout=30000
webclient.write-timeout=30000

# Retry Configuration
webclient.retry.max-attempts=3
webclient.retry.delay=1000
webclient.retry.max-delay=10000

# Circuit Breaker Configuration
webclient.circuit-breaker.failure-threshold=5
webclient.circuit-breaker.timeout=60000
webclient.circuit-breaker.retry-timeout=30000
```

## 📋 Best Practices

### 1. Always Use Timeouts

```java
// Good
return webClient
    .get()
    .uri("/api/data")
    .retrieve()
    .bodyToMono(String.class)
    .timeout(Duration.ofSeconds(30));

// Bad - no timeout
return webClient
    .get()
    .uri("/api/data")
    .retrieve()
    .bodyToMono(String.class);
```

### 2. Handle Errors Gracefully

```java
// Good
return webClient
    .get()
    .uri("/api/data")
    .retrieve()
    .onStatus(HttpStatus::isError, response -> 
        Mono.error(new ApiException("API call failed"))
    )
    .bodyToMono(String.class)
    .onErrorResume(throwable -> 
        Mono.just("fallback-data")
    );

// Bad - no error handling
return webClient
    .get()
    .uri("/api/data")
    .retrieve()
    .bodyToMono(String.class);
```

### 3. Use Appropriate HTTP Methods

```java
// GET for retrieving data
webClient.get().uri("/api/entities/{id}", id)

// POST for creating/submitting data
webClient.post().uri("/api/entities").bodyValue(entityData)

// PUT for updating data
webClient.put().uri("/api/entities/{id}", id).bodyValue(entityData)

// DELETE for removing data
webClient.delete().uri("/api/entities/{id}", id)
```

### 4. Configure Connection Pooling

```java
// Good - configured connection pool
WebClient.builder()
    .clientConnector(new ReactorClientHttpConnector(
        HttpClient.create()
            .connectionProvider(ConnectionProvider.builder("pool")
                .maxConnections(100)
                .maxIdleTime(Duration.ofSeconds(30))
                .build())
    ))

// Bad - default connection pool
WebClient.builder()
```

## 🐛 Troubleshooting

### Common Issues and Solutions

#### 1. Connection Timeout

**Problem**: `java.net.ConnectException: Connection timed out`

**Solution**:
```java
WebClient.builder()
    .clientConnector(new ReactorClientHttpConnector(
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
    ))
```

#### 2. Memory Issues

**Problem**: `OutOfMemoryError` with large responses

**Solution**:
```java
WebClient.builder()
    .codecs(configurer -> 
        configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024) // 50MB
    )
```

#### 3. Blocking in Reactive Context

**Problem**: `block()/blockFirst()/blockLast()` in reactive context

**Solution**:
```java
// Bad
return webClient.get().uri("/api/data").retrieve().bodyToMono(String.class).block();

// Good
return webClient.get().uri("/api/data").retrieve().bodyToMono(String.class);
```

## 📚 Additional Resources

- [Spring WebClient Documentation](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client)
- [Reactor Core Documentation](https://projectreactor.io/docs/core/release/reference/)
- [Netty HttpClient Documentation](https://netty.io/4.1/api/io/netty/handler/codec/http/HttpClientCodec.html)

---

This documentation provides comprehensive guidance for using WebClient in your banking onboarding service. WebClient offers superior performance and modern reactive programming capabilities that are essential for high-volume banking applications.
