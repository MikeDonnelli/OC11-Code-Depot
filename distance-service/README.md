# distance-service (POC)

Service Spring Boot minimal pour calculer la distance routière entre deux points en utilisant OSRM.

## Endpoints

- POST /api/distance/route
  - body: { "from": {"lat":..., "lon":...}, "to": {"lat":..., "lon":...} }
  - response: { "distanceMeters": 1234, "durationSeconds": 300 }

## Configuration
- `application.yml` contient `osrm.baseUrl` (par défaut `https://router.project-osrm.org`)

## Run
- mvn spring-boot:run
- Service listens on port 8443 (HTTPS)
- Endpoint: https://localhost:8443

## Tests & Coverage
- Run tests: mvn test
- Generate coverage report: mvn jacoco:report
- View report: target/site/jacoco/index.html

## Docker
- docker build -t distance-service:local .
- docker run -p 8443:8443 distance-service:local