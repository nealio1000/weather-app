# Weather app

## Description
Weather app designed to provide an endpoint for getting a forecast on a lat/lon pair

## Usage
```bash
sbt run
```

## Example Queries
```
http://localhost:8081/weather?lat=39.7456&lon=-97.0892
http://localhost:8081/weather?lat=40.2268&lon=-108.9805
http://localhost:8081/weather?lat=27.8168&lon=-81.3828
```

## Testing

### Prerequisites
- scala and sbt installed

### How to run unit tests
   ```bash
   sbt test
   ```

## Follow-on Work
 - Dockerize Server
 - Use docker compose to set up integration tests
 - CI/CD
 - Deploy to Kubernetes
 - Metrics and Monitoring
 - standard result format
 - publish json schema defining standard result format contract
 - mock client for testing api functions
 - clean up error handling
 - use generators for domain objects
 - test encoding/decoding domain objects in circe

## Troubleshooting