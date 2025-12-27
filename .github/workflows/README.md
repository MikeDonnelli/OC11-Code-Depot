# CI/CD Pipeline Documentation

## Vue d'ensemble

Cette pipeline GitHub Actions assure la qualité, la sécurité et la fiabilité du projet Hospital POC.

## 🔄 Déclencheurs

- **Push** sur branche `dev-main`
- **Pull Requests** vers `dev-main`

## 📋 Jobs de la Pipeline

### 1. **test-backend** (parallèle)
- **Durée estimée** : ~2-3 min par service
- **Stratégie** : Matrix build (distance-service, hospital-service)
- **Actions** :
  - ✅ Compilation Maven
  - ✅ Exécution des tests unitaires et d'intégration
  - ✅ Génération de rapports de couverture (JaCoCo)
  - ✅ Upload des artefacts (test results, coverage reports)
  - 🚫 Upload Codecov (désactivé - rapports disponibles localement)

### 2. **test-frontend**
- **Durée estimée** : ~2-3 min
- **Actions** :
  - ✅ Installation des dépendances npm
  - ✅ Exécution des tests Vitest avec couverture de code
  - ✅ Génération de rapports de couverture
  - ✅ Build de production
  - ✅ Upload des artefacts (coverage, build)

### 3. **code-quality**
- **Dépend de** : test-backend, test-frontend
- **Durée estimée** : ~3-5 min
- **Actions** :
  - ✅ Analyse statique du code
  - ⚠️ SonarCloud (décommenter si configuré)

### 4. **build-docker**
- **Dépend de** : test-backend, test-frontend
- **Durée estimée** : ~5-8 min
- **Actions** :
  - ✅ Génération des certificats SSL
  - ✅ Build des 3 images Docker
  - ✅ Cache GitHub Actions pour optimisation

### 5. **integration-tests**
- **Dépend de** : build-docker
- **Durée estimée** : ~3-5 min
- **Actions** :
  - ✅ Démarrage de l'architecture complète (docker compose v2)
  - ✅ Health checks sur tous les services HTTPS
  - ✅ Smoke test k6 (30 secondes) via réseau Docker
  - ✅ Upload des résultats k6 (JSON, summary, output)
  - ✅ Upload des logs Docker en cas d'échec

### 6. **security-scan**
- **Dépend de** : test-backend
- **Durée estimée** : ~2-3 min
- **Actions** :
  - ✅ Scan de vulnérabilités avec Trivy
  - ✅ Upload des résultats vers GitHub Security

### 7. **dependency-check**
- ⚠️ **Temporairement désactivé** (en attente de clé API NVD)
- **Durée estimée** : ~2-3 min (avec clé API) ou 20-30 min (sans clé)
- **Actions** :
  - ✅ OWASP Dependency Check
  - ✅ Détection de CVE dans les dépendances
  - ⚠️ Décommenter le job dans ci.yml après obtention de NVD_API_KEY

## ⏱️ Durée Totale Estimée

**~6-9 minutes** (grâce à la parallélisation)
- ⚠️ dependency-check désactivé temporairement (gagnerait 20-30 min sans clé API NVD)

## 🎯 Critères de Succès

La pipeline échoue si :
- ❌ Tests unitaires/intégration échouent
- ❌ Build Docker échoue
- ❌ Services ne démarrent pas correctement
- ❌ Smoke test k6 échoue
- ❌ Vulnérabilités critiques détectées

## 🔧 Configuration Requise

### Secrets GitHub

#### Pour activation future (optionnels)
- **`NVD_API_KEY`** : Clé API pour National Vulnerability Database (OWASP Dependency Check)
  - 📝 **Comment obtenir** : 
    1. Créer un compte gratuit sur [NVD](https://nvd.nist.gov/developers/request-an-api-key)
    2. Demander une API key (délai ~2 heures)
    3. Ajouter dans GitHub : Settings → Secrets and variables → Actions → New repository secret
    4. Décommenter le job `dependency-check` dans ci.yml
  - ⚠️ **Actuellement désactivé** : Job commenté en attendant l'obtention de la clé

- **`CODECOV_TOKEN`** : Pour upload automatique de couverture vers Codecov.io
  - ⚠️ **Actuellement désactivé** : Upload commenté, rapports générés localement
  - Les rapports sont disponibles dans :
    - Backend : `target/site/jacoco/index.html` après chaque build
    - Frontend : `ui/coverage/index.html` après `npm run test:coverage`

- **`SONAR_TOKEN`** : Pour analyse SonarCloud (qualité de code)

## 📊 Artefacts Générés

Tous les artefacts sont versionnés avec le format : `[nom]-[id-du-run]-[numéro de run]`

### Backend (par service)
- **test-results-[service]-[id-du-run]-[run]** : Résultats des tests unitaires (XML, TXT) - 30 jours
- **coverage-report-[service]-[id-du-run]-[run]** : Rapports de couverture JaCoCo (HTML, XML, CSV) - 30 jours

### Frontend
- **frontend-test-results-[id-du-run]-[run]** : Rapports de couverture Vitest - 30 jours
- **frontend-dist-[id-du-run]-[run]** : Build de production - 7 jours

### Tests d'intégration
- **k6-smoke-test-results-[id-du-run]-[run]** : Résultats k6 (JSON, summary, output) - 30 jours
- **docker-logs-[id-du-run]-[run]** : Logs Docker (en cas d'échec uniquement) - 7 jours

### Sécurité
- **trivy-security-report-[id-du-run]-[run]** : Scan de vulnérabilités (SARIF) - 30 jours

## 📊 Optimisations

- ✅ **Cache Maven** : Accélère les builds Java
- ✅ **Cache npm** : Accélère les builds Node.js
- ✅ **Cache Docker** : Réutilise les layers
- ✅ **Matrix builds** : Parallélise les services backend

## 🚀 Améliorations Futures

### Court terme
- [ ] Ajouter tests de performance (k6 stress test limité)
- [ ] Configurer SonarCloud pour métriques de qualité
- [ ] Ajouter linting (ESLint pour frontend, Checkstyle pour backend)

### Moyen terme
- [ ] Déploiement automatique sur environnement de staging
- [ ] Tests end-to-end avec Playwright/Cypress
- [ ] Publication d'images Docker sur GitHub Container Registry

### Long terme
- [ ] Déploiement Kubernetes avec Helm
- [ ] Tests de charge complets (800 req/s)
- [ ] Monitoring et alerting (Prometheus + Grafana)

## 🛠️ Dépannage

### "Services not healthy"
Vérifier les logs : `docker-compose logs`

### "Maven build failed"
Vérifier Java version et dépendances Maven

### "k6 smoke test failed"
Vérifier que les certificats SSL sont générés correctement

## 📖 Documentation Complémentaire

- [GitHub Actions Docs](https://docs.github.com/en/actions)
- [Docker Build Push Action](https://github.com/docker/build-push-action)
- [k6 Load Testing](https://k6.io/docs/)
