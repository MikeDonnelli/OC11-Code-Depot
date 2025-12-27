# 🏥 Hospital Routing System - Docker Setup

## Architecture des conteneurs

Le projet est composé de 3 services conteneurisés avec communication **HTTPS uniquement** :

- **`distance-service`** : Service de calcul de distance et temps de trajet (port 8443 HTTPS)
- **`hospital-service`** : Service de gestion des hôpitaux et spécialités (port 8444 HTTPS)
- **`hospital-ui`** : Interface utilisateur Vue.js servie par nginx (port 443 HTTPS)

## 🚀 Démarrage rapide

### Prérequis
- Docker Desktop installé et en cours d'exécution
- Ports 443, 8443 et 8444 disponibles
- Certificats SSL auto-signés (générés automatiquement au premier démarrage)

### Lancer l'application complète

```bash
# Build et démarrer tous les conteneurs
docker compose up --build

# En arrière-plan (détaché)
docker compose up --build -d

# Voir les logs
docker compose logs -f

# Voir les logs d'un service spécifique
docker compose logs -f hospital-ui
```

### Accéder à l'application

- **Interface utilisateur** : https://localhost (accepter le certificat auto-signé)
- **API Hospital Service** : https://localhost:8444/api/hospitals
- **API Distance Service** : https://localhost:8443/api/distance

### Arrêter l'application

```bash
# Arrêter les conteneurs
docker compose down

# Arrêter et supprimer les volumes
docker compose down -v
```

## 🔧 Développement

### Build et tests

Les tests sont automatiquement exécutés lors du build de chaque conteneur :

- **Services Java** : `mvn clean package -DskipTests=false`
- **UI Vue.js** : `npm run test -- --run`

Pour builder un service spécifique :

```bash
docker compose build distance-service
docker compose build hospital-service
docker compose build hospital-ui
```

### Développement local (sans Docker)

Pour développer en local sans Docker, les services peuvent être lancés individuellement :

#### distance-service
```bash
cd distance-service
mvn spring-boot:run
# Accessible sur https://localhost:8443
```

#### hospital-service
```bash
cd hospital-service
mvn spring-boot:run
# Accessible sur https://localhost:8444
```

#### UI
```bash
cd ui
npm install
npm run dev
# Serveur de développement Vite accessible sur http://localhost:5173
# Note: En mode dev local, Vite utilise HTTP. En Docker, nginx sert l'UI en HTTPS.
```

**Tests unitaires** :
```bash
npm run test          # Tests Vitest (pas de serveur HTTP)
npm run test:coverage # Tests avec rapport de couverture
```

## 🔐 Certificats SSL

Les certificats SSL auto-signés sont générés automatiquement au premier démarrage si le répertoire `certs/` est vide.

Pour générer manuellement les certificats :

```bash
cd certs
bash generate-certs-san.sh
```

**Important** : Les certificats sont auto-signés et destinés uniquement au développement/test. Ne pas utiliser en production.

Voir [certs/README.md](certs/README.md) pour plus de détails.

## 🌐 Configuration réseau

Les services communiquent via le réseau Docker `hospital-network` en **HTTPS** :

- `hospital-ui` → appels HTTPS vers → `hospital-service:8444`
- `hospital-service` → appels HTTPS vers → `distance-service:8443`
- `distance-service` → appels HTTPS vers → OSRM public API

## 📋 Variables d'environnement

### distance-service
- `SSL_KEYSTORE_PATH` : Chemin du keystore PKCS12 (défaut: `/app/keystore.p12`)
- `SSL_KEYSTORE_PASSWORD` : Mot de passe du keystore (défaut: `changeit`)

### hospital-service
- `DISTANCE_SERVICE_URL` : URL du service de distance (défaut: `https://distance-service:8443`)
- `SSL_KEYSTORE_PATH` : Chemin du keystore PKCS12 (défaut: `/app/keystore.p12`)
- `SSL_KEYSTORE_PASSWORD` : Mot de passe du keystore (défaut: `changeit`)

## 🔍 Health Checks

Les services sont configurés avec des health checks pour garantir un démarrage ordonné :

1. ✅ `distance-service` démarre en premier
2. ✅ `hospital-service` attend que distance-service soit healthy
3. ✅ `hospital-ui` attend que hospital-service soit healthy

## 📦 Structure des Dockerfiles

### Services Java (multi-stage)
1. **Build stage** : Maven build avec tests
2. **Runtime stage** : JRE Alpine léger

### UI (multi-stage)
1. **Build stage** : Node.js build + tests
2. **Runtime stage** : Nginx pour servir les assets statiques

## 🛠️ Commandes utiles

```bash
# Reconstruire sans cache
docker compose build --no-cache

# Voir l'état des conteneurs
docker compose ps

# Exécuter une commande dans un conteneur
docker compose exec hospital-service sh

# Voir les logs en temps réel
docker compose logs -f --tail=100

# Redémarrer un service spécifique
docker compose restart hospital-ui
```

## 🐛 Troubleshooting

### Les conteneurs ne démarrent pas
```bash
# Vérifier les logs
docker compose logs

# Vérifier que les ports ne sont pas déjà utilisés (PowerShell)
Get-NetTCPConnection -LocalPort 443,8443,8444 -ErrorAction SilentlyContinue

# ou (CMD)
netstat -ano | findstr "443 8443 8444"
```

### Certificats SSL manquants
```bash
# Générer les certificats manuellement
cd certs
bash generate-certs-san.sh

# ou avec Docker
cd certs
docker run --rm -v ${PWD}:/certs -w /certs --entrypoint sh alpine/openssl /certs/generate-certs-san.sh
```

### Les tests échouent lors du build
Les tests utilisent des mocks et doivent s'exécuter sans services externes. Si un test échoue :
1. Vérifier les logs du build : `docker compose build [service]`
2. Corriger les tests en local
3. Re-builder le conteneur

### L'UI ne peut pas contacter l'API
Vérifier la configuration nginx et que `hospital-service` est healthy :
```bash
docker compose ps
docker compose logs hospital-service
```

### Erreur de certificat SSL dans le navigateur
Les certificats sont auto-signés. Dans le navigateur :
1. Cliquer sur "Avancé" ou "Advanced"
2. Accepter le certificat pour continuer
3. Le certificat sera mémorisé pour cette session

### Tests k6 - Load Testing

Pour lancer les tests de charge k6 :

```bash
# Smoke test (rapide - 30 secondes)
docker run --rm \
  -v ${PWD}/load-tests:/scripts \
  --network oc11-code-depot_hospital-network \
  grafana/k6:latest run /scripts/smoke-test.js

# Stress test (validation POC - 3.5 minutes)
docker run --rm --cpus=4 --memory=2g \
  -v ${PWD}/load-tests:/scripts \
  --network oc11-code-depot_hospital-network \
  grafana/k6:latest run /scripts/stress-test.js
```

Voir [load-tests/README.md](load-tests/README.md) pour plus de détails.
