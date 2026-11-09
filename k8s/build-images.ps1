# Builds each service's jar with the host's Maven, then its image with the shared root Dockerfile.
# Usage:  powershell -ExecutionPolicy Bypass -File k8s\build-images.ps1 [-Tag 0.1] [-Services order-service,cart-service]
# When you change -Tag, change the images: tags in k8s/base/kustomization.yaml to match.

param(
    [string]$Tag = '0.1',
    [string[]]$Services = @('api-gateway', 'auth-service', 'inventory-service', 'cart-service', 'order-service')
)

# "powershell -File" passes "a,b" as one string, so split it here.
$Services = $Services -split ',' | Where-Object { $_ }
$root = Split-Path $PSScriptRoot -Parent
foreach ($s in $Services) {
    Write-Host "Packaging $s ..."
    Push-Location (Join-Path $root $s)
    mvn -q -B clean package -DskipTests
    $built = $LASTEXITCODE
    Pop-Location
    if ($built -ne 0) { Write-Error "Maven build failed for $s"; exit 1 }

    Write-Host "Building image eshop/${s}:$Tag ..."
    docker build --build-arg SERVICE=$s -t "eshop/${s}:$Tag" $root
    if ($LASTEXITCODE -ne 0) { Write-Error "Image build failed for $s"; exit 1 }
}
docker images 'eshop/*'
