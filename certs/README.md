# Certificats SSL

Ce répertoire contient les certificats SSL auto-signés pour sécuriser les communications inter-services.

## ⚠️ Important
- Ces certificats sont **auto-signés** et destinés uniquement au **développement/test**
- **NE PAS** utiliser en production
- Les certificats sont ignorés par Git (voir `.gitignore`)

## Génération des certificats

### Option 1: Docker (recommandé)
Les certificats seront générés automatiquement au premier démarrage si le répertoire est vide.

### Option 2: Manuellement avec OpenSSL

#### Sous Windows (Git Bash ou WSL)
```bash
cd certs
bash generate-certs.sh
```

#### Sous Linux/Mac
```bash
cd certs
chmod +x generate-certs.sh
./generate-certs.sh
```

#### Commandes individuelles
Voir le fichier `COMMANDS.txt` pour les commandes OpenSSL complètes.

## Fichiers générés

Pour chaque service, les fichiers suivants sont créés :

### Distance Service (port 8443)
- `distance-service.key` - Clé privée
- `distance-service.crt` - Certificat public
- `distance-service.p12` - Keystore PKCS12 (password: `changeit`)

### Hospital Service (port 8444)
- `hospital-service.key` - Clé privée
- `hospital-service.crt` - Certificat public
- `hospital-service.p12` - Keystore PKCS12 (password: `changeit`)

### Hospital UI (port 443)
- `hospital-ui.key` - Clé privée
- `hospital-ui.crt` - Certificat public

## Configuration

Les certificats sont montés dans les conteneurs Docker via des volumes :
```yaml
volumes:
  - ./certs/distance-service.p12:/app/keystore.p12:ro
```

## Renouvellement

Les certificats sont valides **365 jours**. Pour les renouveler :
1. Supprimer les anciens certificats
2. Relancer le script de génération
3. Redémarrer les conteneurs Docker

## Accès HTTPS

Une fois configuré :
- UI : https://localhost (accepter le certificat auto-signé dans le navigateur)
- Hospital Service : https://localhost:8444
- Distance Service : https://localhost:8443
