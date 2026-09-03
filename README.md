# Pipeline Poliglota — Red de Estaciones Ambientales

El objetivo de este proyecto es construir un único sistema (no programas independientes) donde la salida de cada etapa
se convierte en la entrada de la siguiente, usando un lenguaje distinto por etapa.

## Estructura del proyecto
```
Pipieline-Poliglota-Proyect/
├── README.md
├── run_pipeline.bat        # corre todas las etapas en cascada
├── datos/
│   ├── entrada.csv              # datos crudos (input inicial)
│   ├── datos_normalizados.csv   # salida Etapa 1 / entrada Etapa 2
│   ├── descartados.csv          # registros rechazados en Etapa 1 (auditoría)
│   ├── metricas.csv             # salida Etapa 2 / entrada Etapa 3
│   ├── alertas.csv              # salida Etapa 3 
│   ├── secuencia.txt            # salida Etapa 3 
│   └── resultado_final.txt      # salida Etapa 4 
├── etapa1-basic/
│   └── limpieza.kbs
├── etapa2-fortran/
│   └── metricas.f90
├── etapa3-java/ 
|   ├── Main.java                 
|   ├── ParserReglas.java          
|   ├── Regla.java                 
|   ├── ReglaTemperatura.java     
|   ├── ReglaPrecipitacion.java    
|   ├── ReglaViento.java           
|   ├── ReglaBateria.java          
|   └── reglas.txt                        
└── etapa4-c/  
    └──verificacion.c          
```

## Cómo correr el pipeline

Requisitos: BASIC-256 y MinGW/gfortran instalados, deben haberlos añadido al path de las variables de entorno

```
run_pipeline.bat
```

Una vez se le da click al archivo .bat, esto ejecuta en orden:
1. `basic256 -r limpieza.kbs` → genera `datos_normalizados.csv` y `descartados.csv`.
2. Compila y corre `metricas.f90` → genera `metricas.csv`.

La consola muestra, para cada etapa, cuántos registros se procesaron y si hubo
errores. Si algo falla, el script se detiene y no continúa con la siguiente
etapa.

## Formato de los archivos intermedios

`datos_normalizados.csv`:

ID,ESTACION,TEMPERATURA,PRECIPITACION,VIENTO,BATERIA

001,COTO,31,12,18,82

`descartados.csv`:

ID,ESTACION,TEMPERATURA,PRECIPITACION,VIENTO,BATERIA,MOTIVO

006,COTO,-999,10,15,70,temperatura_invalida

`metricas.csv` (clave-valor, una métrica por fila):

METRICA,VALOR

TEMPERATURA_PROMEDIO,31.17

TEMPERATURA_MAXIMA,38.00

## Etapa 1 · BASIC-256 (`limpieza.kbs`)

Lee `entrada.csv` y valida cada registro contra las siguientes reglas,
descartando el registro (y registrando el motivo) si alguna falla:

| Regla | Motivo registrado |
|---|---|
| Faltan columnas o algún campo viene vacío | `campos_faltantes` |
| Temperatura, precipitación, viento o batería no son numéricos | `valor_no_numerico` |
| Temperatura fuera de `[-30, 60]` | `temperatura_invalida` |
| Precipitación negativa | `precipitacion_negativa` |
| Viento negativo | `viento_negativo` |
| Batería fuera de `[0, 100]` | `bateria_fuera_de_rango` |

Los registros que no cumplan con las reglas van a `descartados.csv` mientras que los que si cumplen van a `datos_normalizados.csv`.
Paradigma imperativo evidenciado en: variables mutables (`valido`, `motivo$`),
control de flujo con `if/endif` anidados, ciclo `while` de lectura línea por
línea, y cambio de estado del programa registro a registro.

## Etapa 2 · Fortran (`metricas.f90`)

Lee `datos_normalizados.csv` en una sola pasada (sin guardar arreglos en
memoria) y acumula: suma y promedio de temperatura, máximo y mínimo de
temperatura, precipitación acumulada, promedio y máximo de viento, y promedio
de batería. Escribe el resultado en `metricas.csv`.

Paradigma imperativo/procedural evidenciado en: acumuladores de estado
(`suma_temp`, `temp_max`, etc.) actualizados dentro de un ciclo `do`, y
control explícito de fin de archivo con `iostat`.

## Pendiente

- Etapa 3 (Java): reglas de negocio + POO/polimorfismo sobre `metricas.csv`,
  genera `alertas.csv` y `secuencia.txt`.
- Etapa 4 (C): verificación de integridad sobre la salida de Java, genera
  `resultado_final.txt`.
- Se debe integrar las 4 etapas en `run_pipeline.bat` para que se ejecute todo en cascada.

