#!/bin/sh
# Script de génération de certificats SSL avec SAN (compatible k6)
# Usage: docker run --rm -v $(pwd):/certs -w /certs alpine/openssl sh generate-certs-san.sh

echo "🔐 Génération des certificats SSL avec SAN pour k6..."

# Configuration des services
generate_cert() {
    SERVICE=$1
    CN=$2
    HOSTS=$3
    
    echo ""
    echo "📝 Génération du certificat pour $SERVICE..."
    
    KEYFILE="${SERVICE}.key"
    CERTFILE="${SERVICE}.crt"
    P12FILE="${SERVICE}.p12"
    CSRFILE="${SERVICE}.csr"
    EXTFILE="${SERVICE}.ext"
    
    # Créer le fichier de configuration SAN
    cat > $EXTFILE <<EOF
subjectAltName = @alt_names

[alt_names]
EOF
    
    # Ajouter les DNS et IP
    DNS_INDEX=1
    IP_INDEX=1
    for HOST in $HOSTS; do
        case $HOST in
            *[0-9].[0-9]*)
                echo "IP.${IP_INDEX} = $HOST" >> $EXTFILE
                IP_INDEX=$((IP_INDEX + 1))
                ;;
            *)
                echo "DNS.${DNS_INDEX} = $HOST" >> $EXTFILE
                DNS_INDEX=$((DNS_INDEX + 1))
                ;;
        esac
    done
    
    # Générer la clé privée
    echo "  → Clé privée: $KEYFILE"
    openssl genrsa -out $KEYFILE 2048 2>/dev/null
    
    # Générer la CSR
    echo "  → CSR: $CSRFILE"
    openssl req -new -key $KEYFILE -out $CSRFILE \
        -subj "/C=FR/ST=IDF/L=Paris/O=Hospital-POC/OU=Dev/CN=$CN" 2>/dev/null
    
    # Générer le certificat auto-signé avec SAN
    echo "  → Certificat avec SAN: $CERTFILE"
    openssl x509 -req -in $CSRFILE -signkey $KEYFILE -out $CERTFILE \
        -days 365 -extfile $EXTFILE 2>/dev/null
    
    # Vérifier le SAN
    echo "  → Vérification SAN:"
    openssl x509 -in $CERTFILE -noout -text | grep -A1 "Subject Alternative Name"
    
    # Pour les services Spring Boot, créer un keystore PKCS12
    if [ "$SERVICE" != "hospital-ui" ]; then
        echo "  → Keystore PKCS12: $P12FILE"
        openssl pkcs12 -export -in $CERTFILE -inkey $KEYFILE \
            -out $P12FILE -name $SERVICE -passout pass:changeit 2>/dev/null
    fi
    
    # Nettoyer les fichiers temporaires
    rm -f $CSRFILE $EXTFILE
    
    echo "  ✅ Certificat généré pour $SERVICE"
}

# Générer les certificats pour chaque service
generate_cert "distance-service" "distance-service" "distance-service localhost 127.0.0.1"
generate_cert "hospital-service" "hospital-service" "hospital-service localhost 127.0.0.1"
generate_cert "hospital-ui" "hospital-ui" "hospital-ui localhost 127.0.0.1"

echo ""
echo "✅ Tous les certificats avec SAN ont été générés!"
echo ""
echo "Fichiers générés:"
ls -1 *.key *.crt *.p12 2>/dev/null | sed 's/^/  - /'

echo ""
echo "⚠️  Note: Certificats auto-signés (dev uniquement)"
echo "   k6 peut maintenant les utiliser avec insecureSkipTLSVerify: true"
