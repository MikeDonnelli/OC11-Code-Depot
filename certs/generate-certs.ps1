# Script PowerShell pour générer des certificats SSL auto-signés pour les services
# Usage: .\generate-certs.ps1

Write-Host "🔐 Génération des certificats SSL auto-signés pour les services..." -ForegroundColor Cyan

# Vérifier si OpenSSL est installé
try {
    $null = Get-Command openssl -ErrorAction Stop
} catch {
    Write-Host "❌ OpenSSL n'est pas installé ou n'est pas dans le PATH" -ForegroundColor Red
    Write-Host "Installez OpenSSL depuis: https://slproweb.com/products/Win32OpenSSL.html" -ForegroundColor Yellow
    exit 1
}

# Créer le répertoire de sortie s'il n'existe pas
$certDir = $PSScriptRoot
if (-not (Test-Path $certDir)) {
    New-Item -ItemType Directory -Path $certDir | Out-Null
}

# Configuration des services
$services = @(
    @{Name="distance-service"; CN="distance-service"; Port=8443},
    @{Name="hospital-service"; CN="hospital-service"; Port=8444},
    @{Name="hospital-ui"; CN="hospital-ui"; Port=443}
)

foreach ($service in $services) {
    Write-Host "`n📝 Génération du certificat pour $($service.Name)..." -ForegroundColor Yellow
    
    $keyFile = Join-Path $certDir "$($service.Name).key"
    $certFile = Join-Path $certDir "$($service.Name).crt"
    $p12File = Join-Path $certDir "$($service.Name).p12"
    
    # Générer la clé privée
    Write-Host "  → Clé privée: $keyFile"
    openssl genrsa -out $keyFile 2048 2>$null
    
    # Générer le certificat auto-signé (valide 365 jours)
    Write-Host "  → Certificat: $certFile"
    openssl req -new -x509 -key $keyFile -out $certFile -days 365 `
        -subj "/C=FR/ST=IDF/L=Paris/O=Hospital-POC/OU=Dev/CN=$($service.CN)" 2>$null
    
    # Pour les services Spring Boot, créer un keystore PKCS12
    if ($service.Name -ne "hospital-ui") {
        Write-Host "  → Keystore PKCS12: $p12File"
        openssl pkcs12 -export -in $certFile -inkey $keyFile `
            -out $p12File -name $($service.Name) -passout pass:changeit 2>$null
    }
    
    Write-Host "  ✅ Certificat généré pour $($service.Name)" -ForegroundColor Green
}

Write-Host "`n✅ Tous les certificats ont été générés avec succès!" -ForegroundColor Green
Write-Host "`nFichiers générés:" -ForegroundColor Cyan
Get-ChildItem $certDir -File | ForEach-Object {
    Write-Host "  - $($_.Name)" -ForegroundColor Gray
}

Write-Host "`n⚠️  Note: Ces certificats sont auto-signés (pour dev uniquement)" -ForegroundColor Yellow
Write-Host "   En production, utilisez des certificats signés par une CA de confiance" -ForegroundColor Yellow
