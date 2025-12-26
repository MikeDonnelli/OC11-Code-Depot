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
  - ✅ Upload vers Codecov (optionnel)

### 2. **test-frontend**
- **Durée estimée** : ~2-3 min
- **Actions** :
  - ✅ Installation des dépendances npm
  - ✅ Exécution des tests Vitest
  - ✅ Build de production

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
  - ✅ Démarrage de l'architecture complète (docker-compose)
  - ✅ Health checks sur tous les services
  - ✅ Smoke test k6 (30 secondes)

### 6. **security-scan**
- **Dépend de** : test-backend
- **Durée estimée** : ~2-3 min
- **Actions** :
  - ✅ Scan de vulnérabilités avec Trivy
  - ✅ Upload des résultats vers GitHub Security

### 7. **dependency-check**
- **Durée estimée** : ~5-10 min (première exécution, ensuite plus rapide)
- **Actions** :
  - ✅ OWASP Dependency Check
  - ✅ Détection de CVE dans les dépendances

## ⏱️ Durée Totale Estimée

**~8-12 minutes** (grâce à la parallélisation)

## 🎯 Critères de Succès

La pipeline échoue si :
- ❌ Tests unitaires/intégration échouent
- ❌ Build Docker échoue
- ❌ Services ne démarrent pas correctement
- ❌ Smoke test k6 échoue
- ❌ Vulnérabilités critiques détectées

## 🔧 Configuration Requise

### Secrets GitHub (optionnels)
- `CODECOV_TOKEN` : Pour upload de couverture
- `SONAR_TOKEN` : Pour analyse SonarCloud

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
