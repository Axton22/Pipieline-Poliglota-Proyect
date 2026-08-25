@echo off
cd /d "%~dp0"
setlocal

echo Carpeta actual:
cd
echo.

echo === Etapa 1: BASIC-256 ===
cd etapa1-basic
basic256 -r limpieza.kbs
echo Codigo de salida BASIC-256: %errorlevel%
cd ..
pause

echo === Etapa 2: Fortran ===
cd etapa2-fortran
gfortran metricas.f90 -o metricas.exe
if errorlevel 1 (
    echo *** Error al compilar Fortran ***
    cd ..
    pause
    exit /b 1
)
metricas.exe
echo Codigo de salida Fortran: %errorlevel%
cd ..
pause

echo.
echo Pipeline (etapas 1 y 2) completado.
pause
endlocal