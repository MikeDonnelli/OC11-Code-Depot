# Hospital Routing System

## 📋 Présentation du projet

Système de gestion d'hôpitaux permettant la recherche et la réservation de lits par spécialité avec calcul de distance en temps réel.

### Architecture

Le projet est composé de **3 microservices** communiquant en HTTPS :

- **`distance-service`** (Spring Boot - Port 8443) : Calcul de distances routières via OSRM
- **`hospital-service`** (Spring Boot - Port 8444) : Gestion des hôpitaux, spécialités et réservations
- **`hospital-ui`** (Vue 3 + Vite + Nginx - Port 443) : Interface utilisateur web

### Technologies

- **Backend** : Spring Boot 3.1.4, Java 21, H2 Database, JPA
- **Frontend** : Vue 3, Vite, Vitest
- **Infrastructure** : Docker Compose, Nginx, SSL/TLS
- **Tests** : JUnit, Vitest, k6 (load testing)
- **CI/CD** : GitHub Actions, JaCoCo, Trivy, Maven

### Fonctionnalités principales

- ✅ Liste et recherche d'hôpitaux par spécialité
- ✅ Calcul de distance et temps de trajet entre points
- ✅ Recherche de l'hôpital le plus proche avec disponibilité
- ✅ Réservation de lits par spécialité
- ✅ Communication sécurisée HTTPS entre services
- ✅ Tests de charge validant 800+ req/s

---

## 🚀 Démarrage et Tests

### Lancement de l'application

**Option 1 : API publique OSRM (recommandé pour CI/tests)**
```bash
# Générer les certificats SSL (première fois uniquement)
cd certs
docker run --rm -v $(pwd):/certs -w /certs --entrypoint sh alpine/openssl /certs/generate-certs-san.sh

# Démarrer tous les services (utilise l'API publique OSRM par défaut)
docker compose up --build -d

# Accéder à l'application
# https://localhost (accepter le certificat auto-signé)
```

**Option 2 : OSRM local (recommandé pour dev/load tests)**
```bash
# 1. Configurer OSRM local (voir osrm-data/README.md pour détails)
cd osrm-data
# Télécharger et traiter les données Île-de-France (~10 min, voir README.md)

# 2. Démarrer avec profil OSRM local
cd ..
docker compose --profile local-osrm up --build -d

# Performance : 5-50ms au lieu de 200-1000ms avec API publique
```

### Tests

#### Tests unitaires et de couverture

```bash
# Backend (distance-service et hospital-service)
cd distance-service  # ou hospital-service
mvn test
mvn jacoco:report
# Rapport : target/site/jacoco/index.html

# Frontend
cd ui
npm install
npm run test:coverage
# Rapport : ui/coverage/index.html
```

#### Tests de charge (k6)

```bash
# Smoke test (rapide - 30s)
docker run --rm \
  -v ${PWD}/load-tests:/scripts \
  --network oc11-code-depot_hospital-network \
  grafana/k6:latest run /scripts/smoke-test.js

# Stress test (validation POC - 1.5 min)
# Note : Pour tests optimaux, utiliser OSRM local (--profile local-osrm)
docker run --rm --cpus=4 --memory=2g \
  -v ${PWD}/load-tests:/scripts \
  --network oc11-code-depot_hospital-network \
  grafana/k6:latest run /scripts/stress-test.js
```

📚 **Documentation complète** : [DOCKER.md](DOCKER.md)

---

## 🔄 Pipeline CI/CD

### Déclencheurs

- Push sur `dev-main`
- Pull Requests vers `dev-main`

### Jobs de la pipeline

1. **test-backend** (matrix: distance-service, hospital-service)
   - Tests unitaires Maven
   - Rapports de couverture JaCoCo
   - Upload des artefacts versionnés

2. **test-frontend**
   - Tests Vitest avec couverture
   - Build de production
   - Upload des artefacts

3. **code-quality**
   - Analyse statique du code

4. **build-docker**
   - Build des 3 images Docker
   - Génération des certificats SSL
   - Cache GitHub Actions

5. **integration-tests**
   - Démarrage complet avec docker compose
   - Health checks HTTPS
   - Smoke test k6
   - Upload résultats et logs

6. **security-scan**
   - Scan Trivy des vulnérabilités
   - Upload vers GitHub Security

### Artefacts générés

Tous les artefacts sont versionnés : `[nom]-[id-du-run]-[numéro-de-run]`

- Test results (backend × 2, frontend)
- Coverage reports (backend × 2, frontend)
- Frontend build
- k6 test results
- Docker logs (échec uniquement)
- Security reports

### Durée estimée

**~6-9 minutes** (grâce à la parallélisation)

📚 **Documentation complète** : [.github/workflows/README.md](.github/workflows/README.md)

---

## 🌿 Workflow Git

**Résumé**

- `main` est la branche de production *stable*. 
- `dev-main` est la branche de préproduction et l'origine de toutes les branches de développement (`feature/*`, `bugfix/*`, ...).
- Les branches de développement partent de `dev-main`, sont testées puis fusionnées dans `dev-main`.
- Après validation sur `dev-main`, un merge vers `main` peut être réalisé.
- Les branches `main` et `dev-main` restent ouvertes ; les branches de développement sont supprimées après leur fusion.

---

### 1) Branches principales

- **`main`** — Production stable. Branche protégée : pas de push direct, PR requises, checks obligatoires.
- **`dev-main`** — Préproduction. Base pour toutes les branches de développement. Branche protégée et testée après chaque merge.

### 2) Création d’une branche de développement

- Créer la branche depuis `dev-main` :
  - `git checkout -b feature/ma-fonctionnalite dev-main`
  - `git checkout -b bugfix/ID-corrige dev-main`
- Conventions de nommage recommandées : `feature/<description>`, `bugfix/<issue>-<courte-description>`.

### 3) Développement et tests locaux

- Effectuer des commits atomiques et clairs.
- Lancer les tests unitaires et d’intégration localement avant d'ouvrir une PR.
- Mettre à jour la documentation ou les tests si nécessaire.

### 4) Pull Request vers `dev-main` (revue & CI)

- Ouvrir une PR vers `dev-main` lorsqu'une fonctionnalité est prête.
- Conditions minimales pour merger :
  - CI verte (tests, linter, build).
  - Revue par au moins une personne (ou plus selon le repo).
  - Respect des règles de sécurité et des politiques de commit.
- Après approbation et CI verte : merger la PR dans `dev-main` et supprimer la branche de développement.

### 5) Préproduction (`dev-main`) — Validation intégrée

- À chaque merge dans `dev-main`, exécuter les pipelines complets (tests, scans, build, déploiement sur staging si applicable).
- Procéder aux tests d'intégration et validations manuelles nécessaires.
- Si des problèmes sont détectés, corriger via de nouvelles branches issues de `dev-main`.

### 6) Passage en production (`dev-main` → `main`)

- Lorsque `dev-main` est stable et validée : ouvrir une PR de `dev-main` vers `main`.
- Exiger CI verte et approbation pour fusionner dans `main`.
- Après merge, tagguer la release (ex. `v1.2.3`) si souhaité.

### 7) Règles et pratiques recommandées

- **Ne pas supprimer** `main` ni `dev-main` : ces branches restent actives.
- **Supprimer** automatiquement les branches `feature/*` et `bugfix/*` après fusion.
- Protéger `main` et `dev-main` : interdire push direct, exiger PR, définir checks obligatoires et reviewers.
- Stratégie de merge recommandée : `Squash and merge` pour un historique propre ou `Merge commit` si vous souhaitez garder l’historique complet des merges.
- Automatiser au maximum (CI, tests, scans de sécurité, déploiement sur staging).
