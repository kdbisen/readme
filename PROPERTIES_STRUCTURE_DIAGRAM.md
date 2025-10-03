# Properties Structure Diagram

```
📁 src/main/resources/
├── 📄 application.properties                    # Main configuration file
└── 📁 config/                                  # Organized configuration directory
    ├── 📁 core/                                # Core application configuration
    │   ├── 📄 application-core.properties     # Application metadata and server config
    │   ├── 📄 database.properties             # Database configuration
    │   └── 📄 logging.properties              # Logging configuration
    ├── 📁 external/                            # External services configuration
    │   ├── 📄 fenergo.properties              # Fenergo API configuration
    │   └── 📄 apigee.properties               # Apigee API configuration
    ├── 📁 security/                           # Security configuration
    │   └── 📄 security.properties             # SSL, CORS, auth, validation
    ├── 📁 monitoring/                         # Monitoring and observability
    │   └── 📄 monitoring.properties           # Metrics, health checks, tracing
    ├── 📁 features/                           # Feature-specific configuration
    │   ├── 📄 steps.properties                # Step execution configuration
    │   ├── 📄 http-client.properties          # HTTP client configuration
    │   └── 📄 jackson.properties              # JSON serialization configuration
    └── 📁 environments/                        # Environment-specific overrides
        ├── 📁 dev/
        │   └── 📄 application-dev.properties  # Development environment
        ├── 📁 test/
        │   └── 📄 application-test.properties # Test environment
        ├── 📁 staging/
        │   └── 📄 application-staging.properties # Staging environment
        └── 📁 prod/
            └── 📄 application-prod.properties  # Production environment
```

## Configuration Loading Flow

```
1. Core Configuration (Always loaded)
   ├── application-core.properties
   ├── database.properties
   └── logging.properties

2. External Services Configuration
   ├── fenergo.properties
   └── apigee.properties

3. Security Configuration
   └── security.properties

4. Monitoring Configuration
   └── monitoring.properties

5. Features Configuration
   ├── steps.properties
   ├── http-client.properties
   └── jackson.properties

6. Environment-Specific Configuration (Profile-based)
   └── environments/{active-profile}/application-{active-profile}.properties
```

## Environment Override Hierarchy

```
Base Configuration (config/core/, config/external/, etc.)
           ↓
Environment-Specific Overrides (config/environments/{env}/)
           ↓
Runtime Environment Variables
           ↓
Final Configuration
```

