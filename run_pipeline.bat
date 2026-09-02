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

echo === Etapa 3: Java ===
cd etapa3-java
javac *.java
if errorlevel 1 (
    echo *** Error al compilar Java ***
    cd ..
    pause
    exit /b 1
)
java Main
if errorlevel 1 (
    echo *** Error al ejecutar Java ***
    cd ..
    pause
    exit /b 1
)
cd ..
pause

echo === Etapa 4: C ===
cd etapa4-c
gcc verificacion.c -o verificacion.exe
if errorlevel 1 (
    echo *** Error al compilar C ***
    cd ..
    pause
    exit /b 1
)
verificacion.exe
echo Codigo de salida C: %errorlevel%
cd ..
pause

echo.
echo Pipeline (4 etapas) completado.
type datos\resultado_final.txt
pause
endlocal
