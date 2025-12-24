# 🏥 Hospital Routing System - Docker Setup

## Architecture des conteneurs

Le projet est composé de 3 services conteneurisés :

- **`distance-service`** : Service de calcul de distance et temps de trajet (port 8082)
- **`hospital-service`** : Service de gestion des hôpitaux et spécialités (port 8081)
- **`hospital-ui`** : Interface utilisateur Vue.js servie par nginx (port 5173 → 80)

## 🚀 Démarrage rapide

### Prérequis
- Docker Desktop installé et en cours d'exécution
- Ports 5173, 8081 et 8082 disponibles

### Lancer l'application complète

```bash
# Build et démarrer tous les conteneurs
docker-compose up --build

# En arrière-plan (détaché)
docker-compose up --build -d

# Voir les logs
docker-compose logs -f

# Voir les logs d'un service spécifique
docker-compose logs -f hospital-ui
```

### Accéder à l'application

- **Interface utilisateur** : http://localhost:5173
- **API Hospital Service** : http://localhost:8081/api/hospitals
- **API Distance Service** : http://localhost:8082/api/distance

### Arrêter l'application

```bash
# Arrêter les conteneurs
docker-compose down

# Arrêter et supprimer les volumes
docker-compose down -v
```

## 🔧 Développement

### Build et tests

Les tests sont automatiquement exécutés lors du build de chaque conteneur :

- **Services Java** : `mvn clean package -DskipTests=false`
- **UI Vue.js** : `npm run test -- --run`

Pour builder un service spécifique :

```bash
docker-compose build distance-service
docker-compose build hospital-service
docker-compose build hospital-ui
```

### Développement local (sans Docker)

Pour développer en local sans Docker, les services peuvent être lancés individuellement :

#### distance-service
```bash
cd distance-service
mvn spring-boot:run
# Accessible sur http://localhost:8082
```

#### hospital-service
```bash
cd hospital-service
mvn spring-boot:run
# Accessible sur http://localhost:8081
```

#### UI
```bash
cd ui
npm install
npm run dev
# Accessible sur http://localhost:5173
```

## 🌐 Configuration réseau

Les services communiquent via le réseau Docker `hospital-network` :

- `hospital-ui` → appels API vers → `hospital-service:8081`
- `hospital-service` → appels API vers → `distance-service:8082`
- `distance-service` → appels API vers → OSRM public API

## 📋 Variables d'environnement

### hospital-service
- `DISTANCE_SERVICE_URL` : URL du service de distance (défaut: `http://distance-service:8082`)

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
docker-compose build --no-cache

# Voir l'état des conteneurs
docker-compose ps

# Exécuter une commande dans un conteneur
docker-compose exec hospital-service sh

# Voir les logs en temps réel
docker-compose logs -f --tail=100

# Redémarrer un service spécifique
docker-compose restart hospital-ui
```

## 🐛 Troubleshooting

### Les conteneurs ne démarrent pas
```bash
# Vérifier les logs
docker-compose log

# Vérifier que les ports ne sont pas déjà utilisés
netstat -ano | findstr "8081 8082 5173"
```

### Les tests échouent lors du build
Les tests utilisent des mocks et doivent s'exécuter sans services externes. Si un test échoue :
1. Vérifier les logs du build : `docker-compose build [service]`
2. Corriger les tests en local
3. Re-builder le conteneur

### L'UI ne peut pas contacter l'API
Vérifier la configuration nginx et que `hospital-service` est healthy :
```bash
docker-compose ps
docker-compose logs hospital-service
```
