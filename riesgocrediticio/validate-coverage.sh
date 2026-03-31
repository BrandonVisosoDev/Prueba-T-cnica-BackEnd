#!/bin/bash
# Script para validar la cobertura de pruebas

echo "=========================================="
echo "Validación de Cobertura de Pruebas"
echo "=========================================="
echo ""

# Ejecutar pruebas y generar reporte
echo "📋 Ejecutando pruebas y generando reporte JaCoCo..."
mvn clean test jacoco:report -q

# Verificar si el reporte fue generado
if [ -f "target/site/jacoco/jacoco.csv" ]; then
    echo "✅ Reporte generado exitosamente"
    echo ""
    echo "📊 Estadísticas de Cobertura:"
    echo "---"

    # Leer CSV y mostrar estadísticas principales
    tail -5 target/site/jacoco/jacoco.csv | head -3

    echo ""
    echo "🎯 Ruta al reporte HTML:"
    echo "   target/site/jacoco/index.html"
    echo ""
    echo "💻 Para abrir en el navegador:"
    echo "   open target/site/jacoco/index.html (macOS)"
    echo "   start target/site/jacoco/index.html (Windows)"
    echo "   firefox target/site/jacoco/index.html (Linux)"
else
    echo "❌ Error: No se pudo generar el reporte"
    exit 1
fi

echo ""
echo "=========================================="
echo "✅ Validación completada"
echo "=========================================="

