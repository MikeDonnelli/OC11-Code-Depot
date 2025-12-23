# hospital-service (POC)

Service Spring Boot minimal pour gérer les hôpitaux et la disponibilité par spécialité.

## Endpoints

- POST /api/hospitals : créer un hôpital (JSON body)
- GET  /api/hospitals?specialty=<s>&minBeds=<n> : lister hôpitaux avec >= n lits pour une spécialité
- POST /api/hospitals/{id}/reserve?specialty=<s> : réserver un lit (POC — décrémente le compteur)
- POST /api/hospitals/nearest : retourner l'hôpital le plus proche parmi une liste fournie. Body JSON: { "from": {"lat":.., "lon":..}, "hospitals": [ {"id":.., "name":.., "lat":.., "lon":..}, ... ] } — retourne `{ "hospital": <HospitalDTO>, "distanceKm": <double>, "duration": "Hh Mm Ss" }`

## Run (local)

1. Avec Maven (pour développement) :

   mvn spring-boot:run

2. Avec le jar :

   mvn -DskipTests package
   java -jar target/hospital-service-0.0.1-SNAPSHOT.jar

L'application démarre sur : http://localhost:8081
La console H2 est accessible sur : http://localhost:8081/h2-console (url JDBC : jdbc:h2:mem:hospitaldb)

## Docker

docker build -t hospital-service:local .
docker run -p 8081:8081 hospital-service:local

## Remarques

- Base H2 initialisée via `src/main/resources/data.sql` pour faciliter les tests manuels.
- Ce service est volontairement simple pour la POC. Nous ajouterons prochainement : validations, DTOs dédiés, tests unitaires, et sécurisation des endpoints.
