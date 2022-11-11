# Banno Interview Exercise

## Description
Interview exercise for Banno (Jack Henry) designed to provide an endpoint for getting a forecast on a lat/lon pair

## Usage
```bash
sbt run
```

## Testing

### Prerequisites
- scala and sbt installed
- docker and docker-compose installed

### How to run unit tests

- You will first need to get our docker container with postgres running. From the root project directory run:
   ```bash
   ./docker-build.sh
   ```

- Then run the tests
   ```bash
   sbt test
   ```

### How to run integration tests
 ```bash
   ./run-integration-tests.sh
   ```

## Follow-on Work
 - CI/CD
 - Deploy to Kubernetes
 - Metrics and Monitoring 

## Troubleshooting