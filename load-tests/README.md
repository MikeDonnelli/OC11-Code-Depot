# k6 Load Testing - Hospital POC

Tests de charge pour l'application Hospital POC utilisant [k6](https://k6.io/).

## 🚀 Lancement rapide

### Prérequis
Les services principaux doivent être lancés :
```bash
cd ..
docker-compose up -d
```

### Exécuter le stress test
```bash
cd load-tests
docker-compose up
```

Le test se lance automatiquement avec :
- **4 CPU** alloués au conteneur k6
- **2 GB RAM** (réservé minimum 1 GB)
- **HTTPS sécurisé** avec certificats SAN
- Configuration optimisée pour atteindre **800+ req/s**

## 🎯 Critères de validation POC

**Objectif principal** : Le système doit gérer **800 requêtes/seconde par instance** avec un temps de réponse de **moins de 200ms**.

### Seuils de validation

| Métrique | Objectif POC | Status |
|----------|--------------|--------|
| **p(95) response time** | < 200ms | ✅ CRITIQUE |
| **p(99) response time** | < 500ms | ✅ Important |
| **Moyenne** | < 150ms | ✅ Important |
| **Throughput** | > 700 req/s | ✅ CRITIQUE |
| **Taux d'erreur** | < 2% | ✅ CRITIQUE |

Le **stress test** utilise l'exécuteur `ramping-vus` pour maximiser le débit tout en respectant les seuils de latence.

## 📋 Scripts disponibles

| Script | Description | Charge testée | Durée | Objectif |
|--------|-------------|---------------|-------|----------|
| `smoke-test.js` | Test rapide de santé | 3 VUs | 30s | Vérifier que tous les endpoints répondent correctement |
| `stress-test.js` | **Validation POC** | **10→20 VUs** | **3.5min** | **Maximiser le débit avec p(95)<200ms** |

### Détails des scénarios

**smoke-test.js** :
- 3 VUs constants pendant 30 secondes
- Teste 2 endpoints : GET `/api/hospitals` et GET `/api/hospitals?latitude=X&longitude=Y`
- Seuils : p(95) < 1000ms, erreurs < 1%
- **HTTPS** avec `insecureSkipTLSVerify: true`

**stress-test.js** :
- Montée progressive : 10→15→20 VUs (ramping-vus)
- Mix de 4 scénarios :
  - 40% : Liste complète des hôpitaux
  Test par défaut : `stress-test.js`
- Réseau Docker : `oc11-code-depot_hospital-networkyon)
  - 10% : Récupération d'un hôpital par ID
- Seuils POC stricts : p(95)<200ms, p(99)<500ms, avg<150ms, erreurs<2%, débit>700 req/s
- **HTTPS** avec certificats SAN
- IDs testés : [1, 2] uniquement (correspond à la base de données H2)

## 🚀 Utilisation

### Option 1: Avec docker-compose (Recommandé)

Lance automatiquement le stress test avec les ressources optimisées :
```moke test (rapide - 30 secondes)
docker run --rm \
  -v ${PWD}:/scripts \
  --network oc11-code-depot_hospital-network \
  grafana/k6 run /scripts/smoke-test.js

# Stress test (validation POC - 3.5 minutes)
docker run --rm --cpus=4 --memory=2g \
  -v ${PWD}:/scripts \
  --network oc11-code-depot_hospital-network \
  grafana/k6 run /scripts/stress-test.js
```
```

### Option 2: Avec docker run (tests manuels)

#### Lancer un test unique
```bash
# Stress test (validation POC)
docker run --rm --cpus=4 --memory=2g \
  -moke test (rapide - 30 secondes)
docker run --rm \
  -v ${PWD}:/scripts \
  --network oc11-code-depot_hospital-network \
  grafana/k6 run /scripts/smoke-test.js

# Stress test (validation POC - 3.5 minutes)
docker run --rm --cpus=4 --memory=2g \
  -v ${PWD}:/scripts \
  --network oc11-code-depot_hospital-network \
  grafana/k6 run /scripts/stress-test.js
```

### Option 3: Installation locale de k6

#### Installation

**Windows (Chocolatey):**
```bash
choco install k6
```

**Linux:**
```bash
sudo gpg -k
sudo gpg --no-default-keyring --keyring /usr/share/keyrings/k6-archive-keyring.gpg --keyserver hkp://keyserver.ubuntu.com:80 --recv-keys C5AD17C747E3415A3642D57D77C6C491D6AC1D69
echo "deb [signed-by=/usr/share/keyrings/k6-archive-keyring.gpg] https://dl.k6.io/deb stable main" | sudo tee /etc/apt/sources.list.d/k6.list
sudo apt-get update
sudo apt-get install k6
```

**macOS (Homebrew):**
```bash
brew install k6
```

#### Exécution

```bash
cd load-tests

# Variables d'environnement optionnelles
export BASE_URL=https://localhost:443

# Lancer un test
k6 run smoke-test.js        # Test rapide (30s, 3 VUs)
k6 run stress-test.js       # Validation POC (3.5min, 10→20 VUs)
```

## 🔧 Configuration avancée

### Modifier les ressources allouées
Éditez [docker-compose.yml](docker-compose.yml) :
```yaml
deploy:
  resources:
    limits:
      cpus: '4'      # Augmentez selon vos besoins
      memory: 2G
```

### Modifier les VUs
Éditez [stress-test.js](stress-test.js#L23-L24) :
```javascript
preAllocatedVUs: 500,  # VUs pré-alloués
maxVUs: 2000,          # Maximum possible
```

### Changer le test exécuté
Éditez [docker-compose.yml](docker-compose.yml#L6) :
```yaml
command: run /scripts/smoke-test.js  # Au lieu de stress-test.js
```

## 📊 Comprendre les résultats

### Métriques principales

```
✓ status is 200                           ✓ 1500  ✗ 0
✓ response time < 500ms                   ✓ 1420  ✗ 80

checks.........................: 96.00%  ← Taux de succès des vérifications
data_received..................: 15 MB   ← Données reçues
data_sent......................: 120 kB  ← Données envoyées
http_req_blocked...............: avg=1.2ms   ← Temps de connexion
http_req_connecting............: avg=0.8ms   ← Établissement TCP
http_req_duration..............: avg=245ms   ← ⭐ Temps de réponse moyen
  { expected_response:true }...: avg=235ms
http_req_failed................: 4.00%   ← Taux d'erreur
http_req_receiving.............: avg=2ms     ← Temps de réception
http_req_sending...............: avg=1ms     ← Temps d'envoi
http_req_tls_handshaking.......: avg=3ms     ← Négociation TLS/SSL
http_req_waiting...............: avg=242ms   ← Temps d'attente serveur
http_reqs......................: 1500    ← Nombre total de requêtes
iteration_duration.............: avg=1.25s   ← Durée itération complète
iterations.....................: 1500    ← Itérations réussies
vus............................: 100     ← Utilisateurs virtuels actifs
vus_max........................: 100     ← Maximum d'utilisateurs
```

### Interprétation

**✅ Bon** (Objectif atteint):
- `checks` > 95%
- `http_req_duration p(95)` < 500ms (endpoint simple) ou < 1s (avec distance)
- `http_req_failed` < 5%

**⚠️ Acceptable** (À surveiller):
- `checks` > 90%
- `http_req_duration p(95)` < 1s (simple) ou < 2s (distance)
- `http_req_failed` < 10%

**❌ Problématique** (Action requise):
- `checks` < 90%
- `http_req_duration p(95)` > 2s
- `http_req_failed` > 10%

## 🎯 Scénarios de test détaillés

### 1. Smoke Test (`smoke-test.js`)
**Objectif:** Vérification rapide que l'application répond

**Configuration:**
- 3 utilisateurs virtuels constants
- 30 secondes
- Seuil: p(95) < 1s, erreurs < 1%

**Usage:** Avant chaque déploiement ou après modifications

### 2. Stress Test - VALIDATION POC (`stress-test.js`) 🎯
**Objectif:** Valider les exigences POC : 800 req/s avec < 200ms de réponse

**Configuration:**
- **Executor:** `ramping-arrival-rate` (contrôle précis du débit)
- **Montée progressive:** 50 → 100 → 300 → 500 → **800** → **1000** req/s
- **Durée totale:** 10.5 minutes
- **Critères de validation POC:**
  - ✅ p(95) < 200ms (CRITIQUE)
  - ✅ p(99) < 500ms
  - ✅ avg < 150ms
  - ✅ Erreurs < 2%
  - ✅ Débit > 700 req/s

**Scénarios mixtes (pondérés):**
- 40% Liste d'hôpitaux
- 30% Recherche près de Paris
- 20% Recherche près de Lyon
- 10% Récupération d'un hôpital spécifique

**Résultat:**
Le test affiche un rapport détaillé avec verdict **POC VALIDÉ** ✅ ou **POC NON VALIDÉ** ❌ selon les critères.

**Exemple de sortie:**
```
╔═══════════════════════════════════════════════════════════════╗
║        k6 STRESS TEST - VALIDATION POC 800 req/s              ║
╚═══════════════════════════════════════════════════════════════╝

✅ POC VALIDÉ
✅ Tous les critères POC sont respectés !

🎯 CRITÈRES POC (objectif : 800 req/s)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  ✓ p(95) < 200ms :         ✅ 185.42ms
  ✓ p(99) < 500ms :         ✅ 423.18ms
  ✓ avg < 150ms :           ✅ 124.56ms
  ✓ Erreurs < 2% :          ✅ 0.12%
  ✓ Débit > 700 req/s :     ✅ 856 req/s
```

## 📈 Génération de rapports

### Rapports JSON
Les tests génèrent automatiquement des fichiers `summary*.json` avec les métriques complètes.

### Rapports HTML

Pour générer un rapport HTML visuel :

```bash
k6 run --out json=results.json hospital-list.js
k6 run --summary-export=summary.json hospital-list.js
```

Puis utiliser [k6 HTML reporter](https://github.com/benc-uk/k6-reporter) ou [K6 Web Dashboard](https://github.com/szkiba/xk6-dashboard).

### Intégration Grafana (Avancé)

Pour un monitoring temps réel :

```bash
# Avec InfluxDB
docker run -d -p 8086:8086 influxdb:1.8
k6 run --out influxdb=http://localhost:8086/k6 hospital-list.js

# Dashboard Grafana
docker run -d -p 3000:3000 grafana/grafana
# Importer dashboard k6: https://grafana.com/grafana/dashboards/2587
```

## 🔧 Configuration avancée

### Variables d'environnement

```bash
# Changer l'URL cible
BASE_URL=https://production.example.com k6 run hospital-list.js

# Activer le debug
DEBUG=true k6 run hospital-list.js

# Passer des options
K6_VUS=50 K6_DURATION=2m k6 run smoke-test.js
```

### Options en ligne de commande

```bash
# Modifier le nombre d'utilisateurs
k6 run --vus 50 --duration 1m hospital-list.js

# Forcer un stage
k6 run --stage 30s:10,1m:50,30s:0 hospital-list.js

# Sauvegarder les résultats
k6 run --out csv=results.csv hospital-list.js
```

## 🐛 Dépannage

### Erreur: Connection refused
**Problème:** k6 ne peut pas atteindre les services

**Solutions:**
```bash
# Vérifier que les services sont démarrés
docker ps

# Vérifier le réseau Docker
docker network ls
docker network inspect oc11-code-depot_hospital-network

# Utiliser le bon réseau
docker run --rm -v ${PWD}/load-tests:/scripts --network oc11-code-depot_hospital-network grafana/k6 run /scripts/smoke-test.js
```

### Erreur: SSL certificate problem
**Problème:** Certificats auto-signés

**Solution:** Les scripts incluent déjà `insecureSkipTLSVerify: true`

### Performances dégradées
**Problème:** Les tests montrent des temps de réponse élevés

**Diagnostic:**
```bash
# Vérifier les logs des services
docker logs hospital-service
docker logs distance-service

# Vérifier la RAM/CPU
docker stats

# Tester localement (sans Docker)
k6 run --vus 1 --duration 10s smoke-test.js
```

## 📝 Bonnes pratiques

### Avant de lancer les tests

1. ✅ Vérifier que tous les services sont "healthy"
   ```bash
   docker ps
   ```

2. ✅ Lancer un smoke test d'abord
   ```bash
   k6 run smoke-test.js
   ```

3. ✅ Augmenter progressivement la charge
   - Smoke test → Hospital list → Search distance → Stress test

### Pendant les tests

1. 📊 Monitorer les ressources
   ```bash
   docker stats
   ```

2. 📝 Consulter les logs en temps réel
   ```bash
   docker logs -f hospital-service
   ```

3. ⏸️ Arrêter si nécessaire
   - `Ctrl+C` pour stopper k6
   - Analyser les logs avant de relancer

### Après les tests

1. 📈 Analyser les résultats JSON
2. 📊 Comparer avec les tests précédents
3. 🔍 Identifier les points d'amélioration
4. 📝 Documenter les résultats

## 🎓 Ressources

- [Documentation k6](https://k6.io/docs/)
- [k6 Examples](https://k6.io/docs/examples/)
- [Best Practices](https://k6.io/docs/testing-guides/test-types/)
- [k6 Cloud](https://k6.io/cloud/) - Pour tests distribués

## ✅ Checklist de validation

### Validation fonctionnelle
- [ ] Smoke test passe (> 95% succès)
- [ ] Hospital list: p(95) < 500ms avec 100 users
- [ ] Search distance: p(95) < 1s avec 50 users
- [ ] Pas de memory leak (vérifier `docker stats`)
- [ ] Logs propres (pas d'exceptions)

### Validation POC (CRITIQUE) 🎯
- [ ] **Stress test: POC VALIDÉ** ✅
- [ ] **p(95) < 200ms à 800 req/s**
- [ ] **p(99) < 500ms**
- [ ] **Taux d'erreur < 2%**
- [ ] **Débit stable > 700 req/s**
- [ ] **Système stable jusqu'à 1000 req/s (marge)**

### Si POC NON VALIDÉ ❌
Actions prioritaires:
1. 🔍 Analyser les logs des 3 services
2. 📊 Vérifier `docker stats` (CPU/RAM/Network)
3. 🔧 Profiling Java (JProfiler, async-profiler)
4. 🗄️ Optimiser requêtes DB (index, N+1)
5. 🚀 Activer cache applicatif
6. ⚙️ Augmenter ressources Docker
7. 📈 Envisager scaling horizontal

---

**Note:** Ces tests sont conçus pour l'environnement de développement avec certificats auto-signés. Pour la production, ajustez `BASE_URL` et retirez `insecureSkipTLSVerify`.
