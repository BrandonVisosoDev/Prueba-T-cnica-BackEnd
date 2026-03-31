# Validación de Cobertura de Pruebas - Windows (PowerShell)

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "Validación de Cobertura de Pruebas" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Ejecutar pruebas y generar reporte
Write-Host "📋 Ejecutando pruebas y generando reporte JaCoCo..." -ForegroundColor Yellow
mvn clean test jacoco:report -q

# Verificar si el reporte fue generado
if (Test-Path "target/site/jacoco/jacoco.csv") {
    Write-Host "✅ Reporte generado exitosamente" -ForegroundColor Green
    Write-Host ""
    Write-Host "📊 Estadísticas de Cobertura:" -ForegroundColor Cyan
    Write-Host "---" -ForegroundColor DarkGray

    # Mostrar resumen del archivo CSV
    $csv = Get-Content "target/site/jacoco/jacoco.csv" | Measure-Object -Line
    Write-Host "Total de líneas analizadas: $($csv.Lines)" -ForegroundColor Gray

    Write-Host ""
    Write-Host "🎯 Ruta al reporte HTML:" -ForegroundColor Cyan
    Write-Host "   target/site/jacoco/index.html" -ForegroundColor White
    Write-Host ""
    Write-Host "💻 Para abrir en el navegador:" -ForegroundColor Yellow
    Write-Host "   Invoke-Item target/site/jacoco/index.html" -ForegroundColor White
    Write-Host ""
    Write-Host "📈 Ver detalles de cobertura por paquete:" -ForegroundColor Cyan
    Write-Host "   target/site/jacoco/com.ordenaris.riesgocrediticio/" -ForegroundColor Gray
} else {
    Write-Host "❌ Error: No se pudo generar el reporte" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "✅ Validación completada" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan

