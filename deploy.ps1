Write-Host "🚀 Iniciando deploy..." -ForegroundColor Cyan

Write-Host "📦 Compilando backend..." -ForegroundColor Yellow
cd C:\spanggit\backend
.\mvnw clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erro no build do backend!" -ForegroundColor Red
    exit 1
}

Write-Host "🎨 Compilando frontend..." -ForegroundColor Yellow
cd C:\spanggit\frontend
ng build --configuration production

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Erro no build do frontend!" -ForegroundColor Red
    exit 1
}

Write-Host "📋 Copiando arquivos para deploy..." -ForegroundColor Yellow
cd C:\spanggit
Copy-Item backend\target\hotel-divan-1.0.0.jar D:\deploy\ -Force
xcopy frontend\dist\divan\browser D:\deploy\browser\ /E /I /Y

Write-Host "✅ Deploy concluído!" -ForegroundColor Green
Get-ChildItem D:\deploy\