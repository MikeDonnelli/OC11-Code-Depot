# hospital-service (POC)

Service Spring Boot minimal pour gérer les hôpitaux et la disponibilité par spécialité.

## Endpoints

- POST /api/hospitals : créer un hôpital (JSON body)
- GET  /api/hospitals : lister tous les hôpitaux (sans paramètres) ou filtrer avec `?specialty=<s>&minBeds=<n>`
- GET  /api/hospitals/{id} : récupérer un hôpital par son ID
- GET  /api/hospitals/specialties : obtenir la liste de toutes les spécialités disponibles
- POST /api/hospitals/{id}/reserve?specialty=<s> : réserver un lit (POC — décrémente le compteur)
- POST /api/hospitals/nearest : retourner l'hôpital le plus proche parmi une liste fournie. Body JSON: { "from": {"lat":.., "lon":..}, "hospitals": [ {"id":.., "name":.., "lat":.., "lon":..}, ... ] } — retourne `{ "hospital": <HospitalDTO>, "distanceKm": <double>, "duration": "Hh Mm Ss" }`

## Run (local)

1. Avec Maven (pour développement) :

   mvn spring-boot:run

2. Avec le jar :

   mvn -DskipTests package
   java -jar target/hospital-service-0.0.1-SNAPSHOT.jar

L'application démarre sur : https://localhost:8444
La console H2 est accessible sur : https://localhost:8444/h2-console (url JDBC : jdbc:h2:mem:hospitaldb)

## Tests & Coverage

- Run tests: mvn test
- Generate coverage report: mvn jacoco:report
- View report: target/site/jacoco/index.html

## Docker

docker build -t hospital-service:local .
docker run -p 8444:8444 hospital-service:local

## Remarques

- Base H2 initialisée via `src/main/resources/data.sql` pour faciliter les tests manuels.
- Ce service est volontairement simple pour la POC. Nous ajouterons prochainement : validations, DTOs dédiés, tests unitaires, et sécurisation des endpoints.
