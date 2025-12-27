# Données OSRM - Configuration locale

Ce répertoire contient les données cartographiques prétraitées pour OSRM.

## 🚀 Installation rapide

### Option 1 : Télécharger les données pré-traitées (recommandé)

```powershell
# Créer le répertoire
cd osrm-data

# Télécharger les données de la France (environ 1.5 GB)
# ATTENTION: Lien exemple, vérifier la disponibilité sur download.geofabrik.de
Invoke-WebRequest -Uri "https://download.geofabrik.de/europe/france-latest.osm.pbf" -OutFile "france-latest.osm.pbf"

# Extraire les données avec OSRM
docker run --rm -v ${PWD}:/data osrm/osrm-backend osrm-extract -p /opt/car.lua /data/france-latest.osm.pbf

# Partitionner les données (MLD algorithm)
docker run --rm -v ${PWD}:/data osrm/osrm-backend osrm-partition /data/france-latest.osrm

# Personnaliser les données
docker run --rm -v ${PWD}:/data osrm/osrm-backend osrm-customize /data/france-latest.osrm
```

### Option 2 : Données pré-traitées complètes (plus rapide)

Si vous trouvez des données `.osrm` déjà prétraitées, placez-les directement ici :
- `france-latest.osrm`
- `france-latest.osrm.cells`
- `france-latest.osrm.cnbg`
- `france-latest.osrm.cnbg_to_ebg`
- `france-latest.osrm.ebg`
- `france-latest.osrm.ebg_nodes`
- `france-latest.osrm.edges`
- `france-latest.osrm.enw`
- `france-latest.osrm.fileIndex`
- `france-latest.osrm.geometry`
- `france-latest.osrm.icd`
- `france-latest.osrm.maneuver_overrides`
- `france-latest.osrm.mldgr`
- `france-latest.osrm.names`
- `france-latest.osrm.nbg_nodes`
- `france-latest.osrm.partition`
- `france-latest.osrm.properties`
- `france-latest.osrm.ramIndex`
- `france-latest.osrm.timestamp`
- `france-latest.osrm.tld`
- `france-latest.osrm.tls`
- `france-latest.osrm.turn_duration_penalties`
- `france-latest.osrm.turn_penalties_index`
- `france-latest.osrm.turn_weight_penalties`

## 📦 Taille des données

- **france-latest.osm.pbf** : ~1.5 GB (données brutes)
- **france-latest.osrm*** : ~3-4 GB (données prétraitées)

Pour un dataset plus petit (tests uniquement) :
```powershell
# Île-de-France uniquement (~150 MB)
Invoke-WebRequest -Uri "https://download.geofabrik.de/europe/france/ile-de-france-latest.osm.pbf" -OutFile "france-latest.osm.pbf"
```

## 🔧 Utilisation

Une fois les fichiers `.osrm` générés, démarrez simplement :

```bash
docker compose up osrm distance-service
```

L'instance OSRM locale sera accessible sur `http://localhost:5000` et utilisée automatiquement par `distance-service`.

## 📊 Performance attendue

**API publique OSRM** : 200-1000ms par requête (variable)
**OSRM local** : 5-50ms par requête (stable)

## ⚠️ Notes

- Les fichiers `.osrm` sont spécifiques à votre système (architecture CPU)
- Ne pas commiter ces fichiers (ils sont dans `.gitignore`)
- Le processus de prétraitement peut prendre 30-60 minutes pour la France complète
- Pour la production, utiliser un service OSRM dédié ou une API commerciale

## 🌍 Sources de données

- **Geofabrik** : https://download.geofabrik.de/
- **OpenStreetMap** : https://planet.openstreetmap.org/

## 🐛 Dépannage

### OSRM ne démarre pas
```bash
# Vérifier que tous les fichiers .osrm sont présents
ls -la france-latest.osrm*

# Vérifier les logs
docker compose logs osrm
```

### Erreur "file not found"
Assurez-vous que le fichier principal `france-latest.osrm` existe et que tous les fichiers associés sont présents.

### Performance toujours lente
Vérifiez que `OSRM_BASE_URL=http://osrm:5000` est bien configuré dans le service distance-service.
