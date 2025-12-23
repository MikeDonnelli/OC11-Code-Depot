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
- Service listens on port 8082

## Docker
- docker build -t distance-service:local .
- docker run -p 8082:8082 distance-service:local