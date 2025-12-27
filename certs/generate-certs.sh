#!/bin/bash
# Script Bash pour générer des certificats SSL auto-signés pour les services
# Usage: ./generate-certs.sh

echo "🔐 Génération des certificats SSL auto-signés pour les services..."

# Vérifier si OpenSSL est installé
if ! command -v openssl &> /dev/null; then
    echo "❌ OpenSSL n'est pas installé"
    exit 1
fi

# Services à configurer
services=("distance-service:distance-service:8443" "hospital-service:hospital-service:8444" "hospital-ui:hospital-ui:443")

for service_info in "${services[@]}"; do
    IFS=':' read -r service cn port <<< "$service_info"
    
    echo ""
    echo "📝 Génération du certificat pour $service..."
    
    keyfile="${service}.key"
    certfile="${service}.crt"
    p12file="${service}.p12"
    
    # Générer la clé privée
    echo "  → Clé privée: $keyfile"
    openssl genrsa -out "$keyfile" 2048 2>/dev/null
    
    # Générer le certificat auto-signé (valide 365 jours)
    echo "  → Certificat: $certfile"
    openssl req -new -x509 -key "$keyfile" -out "$certfile" -days 365 \
        -subj "/C=FR/ST=IDF/L=Paris/O=Hospital-POC/OU=Dev/CN=$cn" 2>/dev/null
    
    # Pour les services Spring Boot, créer un keystore PKCS12
    if [ "$service" != "hospital-ui" ]; then
        echo "  → Keystore PKCS12: $p12file"
        openssl pkcs12 -export -in "$certfile" -inkey "$keyfile" \
            -out "$p12file" -name "$service" -passout pass:changeit 2>/dev/null
    fi
    
    echo "  ✅ Certificat généré pour $service"
done

echo ""
echo "✅ Tous les certificats ont été générés avec succès!"
echo ""
echo "Fichiers générés:"
ls -1 *.{key,crt,p12} 2>/dev/null | sed 's/^/  - /'

echo ""
echo "⚠️  Note: Ces certificats sont auto-signés (pour dev uniquement)"
echo "   En production, utilisez des certificats signés par une CA de confiance"
