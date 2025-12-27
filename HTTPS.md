# Configuration HTTPS pour le POC Hospital

## 🔐 Vue d'ensemble

Tous les services utilisent maintenant **HTTPS avec des certificats auto-signés** pour sécuriser les communications inter-services.

### Architecture de sécurité

```
┌─────────────────────────────────────────────────────────────┐
│                     Navigateur (Client)                     │
│                   https://localhost                         │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTPS (port 443)
                           ▼
              ┌────────────────────────┐
              │   hospital-ui (nginx)  │
              │   Port 443 (HTTPS)     │
              │   + Certificat SSL     │
              └────────┬───────────────┘
                       │ HTTPS (port 8444)
                       │ proxy_ssl_verify off
                       ▼
              ┌────────────────────────┐
              │  hospital-service      │
              │  Port 8444 (HTTPS)     │
              │  + Keystore PKCS12     │
              └────────┬───────────────┘
                       │ HTTPS (port 8443)
                       │ SSL Context (trust all)
                       ▼
              ┌────────────────────────┐
              │  distance-service      │
              │  Port 8443 (HTTPS)     │
              │  + Keystore PKCS12     │
              └────────────────────────┘
```

## 📋 Ports et Protocoles

| Service           | Port HTTPS | Description                    |
|-------------------|------------|--------------------------------|
| distance-service  | **8443**   | Calcul de distances            |
| hospital-service  | **8444**   | API backend hôpitaux           |
| hospital-ui       | **443**    | Interface web                  |

> 💡 Tous les services utilisent **uniquement HTTPS**. Aucun port HTTP n'est exposé.

## 🚀 Démarrage

### Prérequis
Les certificats doivent être générés avant le premier lancement :

```bash
cd certs
docker run --rm -v $(pwd):/certs -w /certs --entrypoint sh alpine/openssl /certs/generate-certs-san.sh
# ou
bash generate-certs-san.sh  # Linux/Mac/Git Bash
```

### Lancement avec HTTPS

```bash
docker compose up --build -d
```

### Vérification

```bash
# Vérifier les conteneurs
docker ps

# Tester les endpoints HTTPS
curl -k https://localhost:8443/actuator/health  # distance-service
curl -k https://localhost:8444/actuator/health  # hospital-service
curl -k https://localhost/                      # UI (redirige vers HTTPS)
```

L'option `-k` (ou `--insecure`) est nécessaire car les certificats sont auto-signés.

## 🌐 Accès à l'application

**URL principale** : https://localhost

Au premier accès, votre navigateur affichera un avertissement de sécurité car le certificat est auto-signé. 

### Accepter le certificat dans votre navigateur

#### Chrome/Edge
1. Cliquer sur "Avancé" ou "Advanced"
2. Cliquer sur "Continuer vers localhost (dangereux)" / "Proceed to localhost (unsafe)"

#### Firefox
1. Cliquer sur "Avancé" / "Advanced"
2. Cliquer sur "Accepter le risque et continuer" / "Accept the Risk and Continue"

> 💡 **Astuce** : Pour Chrome, vous pouvez taper `thisisunsafe` (invisible) sur la page d'avertissement pour contourner

## 🔧 Configuration technique

### Services Spring Boot (distance-service, hospital-service)

**Fichier** : `application.yml`

```yaml
server:
  port: 8443  # ou 8444 pour hospital-service
  ssl:
    enabled: true
    key-store: /app/keystore.p12
    key-store-password: changeit
    key-store-type: PKCS12
    key-alias: distance-service  # ou hospital-service
```

**Volumes Docker** :
```yaml
volumes:
  - ./certs/distance-service.p12:/app/keystore.p12:ro
```

### Service Nginx (hospital-ui)

**Fichier** : `nginx.conf`

```nginx
server {
    listen 443 ssl;
    ssl_certificate /etc/nginx/ssl/hospital-ui.crt;
    ssl_certificate_key /etc/nginx/ssl/hospital-ui.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    
    location /api/ {
        proxy_pass https://hospital-service:8444/api/;
        proxy_ssl_verify off;  # ⚠️ Dev only
    }
}
```

**Volumes Docker** :
```yaml
volumes:
  - ./certs/hospital-ui.crt:/etc/nginx/ssl/hospital-ui.crt:ro
  - ./certs/hospital-ui.key:/etc/nginx/ssl/hospital-ui.key:ro
```

### Communication inter-service

Le `hospital-service` est configuré pour accepter les certificats auto-signés du `distance-service` via `SslConfig.java` :

```java
SslContext sslContext = SslContextBuilder
    .forClient()
    .trustManager(InsecureTrustManagerFactory.INSTANCE)  // ⚠️ Dev only
    .build();
```

## 📁 Certificats

### Localisation
Tous les certificats sont dans le répertoire `certs/` (ignoré par Git).

### Types de fichiers

| Extension | Description                      | Utilisation                    |
|-----------|----------------------------------|--------------------------------|
| `.key`    | Clé privée RSA 2048 bits         | Nginx                          |
| `.crt`    | Certificat X.509 auto-signé      | Nginx                          |
| `.p12`    | Keystore PKCS12                  | Spring Boot (Java)             |

### Validité
- **Durée** : 365 jours
- **Renouvellement** : Régénérer les certificats et redémarrer les conteneurs

### Informations des certificats

| Champ     | Valeur              |
|-----------|---------------------|
| Country   | FR                  |
| State     | IDF                 |
| City      | Paris               |
| Org       | Hospital-POC        |
| OU        | Dev                 |
| CN        | distance-service / hospital-service / hospital-ui |

## ⚠️ Avertissements de sécurité

### Pour le développement uniquement

Cette configuration HTTPS utilise :
- ✅ Certificats auto-signés
- ✅ `InsecureTrustManagerFactory` (accepte tous les certificats)
- ✅ `proxy_ssl_verify off` (nginx ne vérifie pas les certificats)

### ⛔ NE JAMAIS utiliser en production

Pour la production, vous **DEVEZ** :
1. Obtenir des certificats signés par une CA de confiance (Let's Encrypt, DigiCert, etc.)
2. Activer la vérification des certificats SSL
3. Supprimer `InsecureTrustManagerFactory`
4. Configurer un truststore avec les CA racines
5. Utiliser des secrets management (Vault, AWS Secrets Manager)
6. Implémenter mTLS pour l'authentification mutuelle

## 🔄 Migration vers mTLS (Production)

Pour une sécurité maximale en production :

1. **Créer une CA privée**
   ```bash
   openssl genrsa -out ca.key 4096
   openssl req -new -x509 -key ca.key -out ca.crt -days 3650
   ```

2. **Générer des certificats signés par la CA**
3. **Configurer le truststore** dans les services Java
4. **Activer la vérification mutuelle** (client + serveur)

Voir `MTLS.md` pour un guide complet (à créer si nécessaire).

## 🧪 Tests

### Test des communications HTTPS

```bash
# Test direct du distance-service
curl -k -X POST https://localhost:8443/api/distance \
  -H "Content-Type: application/json" \
  -d '{"lat1": 48.8566, "lon1": 2.3522, "lat2": 48.8584, "lon2": 2.2945}'

# Test via hospital-service
curl -k https://localhost:8444/api/hospitals

# Test de l'UI
curl -k https://localhost/
```

### Vérification des certificats

```bash
# Voir les détails du certificat distance-service
openssl s_client -connect localhost:8443 -showcerts

# Voir les détails du certificat hospital-service
openssl s_client -connect localhost:8444 -showcerts

# Voir les détails du certificat UI
openssl s_client -connect localhost:443 -showcerts
```

## 🐛 Dépannage

### Erreur "Connection refused"
- Vérifier que les conteneurs sont démarrés : `docker ps`
- Vérifier les logs : `docker logs distance-service`

### Erreur "SSL handshake failed"
- Vérifier que les certificats sont montés dans le conteneur
- Vérifier les permissions des fichiers `.p12` et `.key`
- Régénérer les certificats si nécessaire

### Navigateur bloque l'accès
- Accepter le certificat auto-signé (voir section "Accès à l'application")
- Pour Chrome : taper `thisisunsafe` sur la page d'avertissement

### Health checks échouent
Les health checks utilisent `wget --no-check-certificate` pour accepter les certificats auto-signés :
```yaml
healthcheck:
  test: ["CMD-SHELL", "wget --no-check-certificate --quiet --tries=1 --spider https://localhost:8443/actuator/health || exit 1"]
```

## 📚 Ressources

- [Spring Boot SSL Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.webserver.configure-ssl)
- [Nginx SSL Module](https://nginx.org/en/docs/http/ngx_http_ssl_module.html)
- [OpenSSL Documentation](https://www.openssl.org/docs/)
- [Let's Encrypt](https://letsencrypt.org/) - Certificats gratuits pour la production
- [mTLS Best Practices](https://cloud.google.com/architecture/security-foundations/authentication-authorization#mtls)

## 📝 Logs

Les logs montrent clairement le protocole utilisé :
```
Tomcat started on port(s): 8443 (https) with context path ''
Tomcat started on port(s): 8444 (https) with context path ''
```

## ✅ Checklist de vérification

- [ ] Certificats générés dans `certs/`
- [ ] Services démarrés avec `docker-compose up -d`
- [ ] Health checks "healthy" (`docker ps`)
- [ ] UI accessible sur https://localhost
- [ ] Certificat accepté dans le navigateur
- [ ] Communication inter-service fonctionne
- [ ] Logs confirment HTTPS activé

---

**Note** : Cette configuration est optimale pour le développement et les tests. Pour la production, suivez les recommandations de sécurité ci-dessus.
