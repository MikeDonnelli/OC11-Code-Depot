# Script PowerShell pour générer des certificats SSL avec SAN (Subject Alternative Names)
# Compatible avec k6 et navigateurs modernes

Write-Host "🔐 Génération des certificats SSL avec SAN pour k6..." -ForegroundColor Cyan

# Vérifier OpenSSL
if (-not (Get-Command openssl -ErrorAction SilentlyContinue)) {
    Write-Host "❌ OpenSSL n'est pas installé" -ForegroundColor Red
    Write-Host "Installez-le via: choco install openssl" -ForegroundColor Yellow
    exit 1
}

# Configuration des services
$services = @(
    @{Name="distance-service"; CN="distance-service"; Port="8443"; Hosts=@("distance-service", "localhost", "127.0.0.1")},
    @{Name="hospital-service"; CN="hospital-service"; Port="8444"; Hosts=@("hospital-service", "localhost", "127.0.0.1")},
    @{Name="hospital-ui"; CN="hospital-ui"; Port="443"; Hosts=@("hospital-ui", "localhost", "127.0.0.1")}
)

foreach ($service in $services) {
    Write-Host ""
    Write-Host "📝 Génération du certificat pour $($service.Name)..." -ForegroundColor Yellow
    
    $keyfile = "$($service.Name).key"
    $certfile = "$($service.Name).crt"
    $p12file = "$($service.Name).p12"
    $csrfile = "$($service.Name).csr"
    $extfile = "$($service.Name).ext"
    
    # Créer le fichier de configuration SAN
    $sanConfig = @"
subjectAltName = @alt_names

[alt_names]
"@
    
    $index = 1
    foreach ($host in $service.Hosts) {
        if ($host -match '^\d+\.\d+\.\d+\.\d+$') {
            $sanConfig += "`nIP.$index = $host"
        } else {
            $sanConfig += "`nDNS.$index = $host"
        }
        $index++
    }
    
    Set-Content -Path $extfile -Value $sanConfig
    
    # Générer la clé privée
    Write-Host "  → Clé privée: $keyfile" -ForegroundColor Gray
    & openssl genrsa -out $keyfile 2048 2>$null
    
    # Générer la CSR (Certificate Signing Request)
    Write-Host "  → CSR: $csrfile" -ForegroundColor Gray
    & openssl req -new -key $keyfile -out $csrfile `
        -subj "/C=FR/ST=IDF/L=Paris/O=Hospital-POC/OU=Dev/CN=$($service.CN)" 2>$null
    
    # Générer le certificat auto-signé avec SAN
    Write-Host "  → Certificat avec SAN: $certfile" -ForegroundColor Gray
    & openssl x509 -req -in $csrfile -signkey $keyfile -out $certfile `
        -days 365 -extfile $extfile 2>$null
    
    # Vérifier le SAN
    Write-Host "  → Vérification SAN:" -ForegroundColor Green
    & openssl x509 -in $certfile -noout -text | Select-String -Pattern "DNS:|IP Address:"
    
    # Pour les services Spring Boot, créer un keystore PKCS12
    if ($service.Name -ne "hospital-ui") {
        Write-Host "  → Keystore PKCS12: $p12file" -ForegroundColor Gray
        & openssl pkcs12 -export -in $certfile -inkey $keyfile `
            -out $p12file -name $service.Name -passout pass:changeit 2>$null
    }
    
    # Nettoyer les fichiers temporaires
    Remove-Item $csrfile, $extfile -ErrorAction SilentlyContinue
    
    Write-Host "  ✅ Certificat généré pour $($service.Name)" -ForegroundColor Green
}

Write-Host ""
Write-Host "✅ Tous les certificats avec SAN ont été générés!" -ForegroundColor Green
Write-Host ""
Write-Host "Fichiers générés:" -ForegroundColor Cyan
Get-ChildItem *.key, *.crt, *.p12 | ForEach-Object { Write-Host "  - $($_.Name)" }

Write-Host ""
Write-Host "⚠️  Note: Certificats auto-signés (dev uniquement)" -ForegroundColor Yellow
Write-Host "   k6 peut maintenant les utiliser avec insecureSkipTLSVerify: true" -ForegroundColor Yellow
