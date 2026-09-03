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

## Etapa 3 · Java (`etapa3-java`)

Lee `metricas.csv` generado por Fortran y evalúa las reglas definidas en `reglas.txt`.

Las reglas siguen la estructura:

```text
<regla> ::= <identificador> <operador> <numero>

<operador> ::= ">" | "<" | ">=" | "<="

<identificador> ::= "TEMP_ALTA"
                  | "LLUVIA_INTENSA"
                  | "VIENTO_FUERTE"
                  | "BATERIA_BAJA"
```

Ejemplo de reglas:

```text
TEMP_ALTA > 35
LLUVIA_INTENSA > 50
VIENTO_FUERTE > 40
BATERIA_BAJA < 20
```

`ParserReglas.java` valida la sintaxis de cada regla y crea el objeto correspondiente.

La programación orientada a objetos se evidencia mediante la clase abstracta `Regla` y las clases:

* `ReglaTemperatura`
* `ReglaPrecipitacion`
* `ReglaViento`
* `ReglaBateria`

Todas heredan de `Regla` e implementan el método `evaluar()`, permitiendo utilizar polimorfismo durante la evaluación de las métricas.

Las métricas utilizadas son:

| Regla            | Métrica evaluada          | Código |
| ---------------- | ------------------------- | -----: |
| `TEMP_ALTA`      | `TEMPERATURA_MAXIMA`      |     10 |
| `LLUVIA_INTENSA` | `PRECIPITACION_ACUMULADA` |     20 |
| `VIENTO_FUERTE`  | `VIENTO_MAXIMO`           |     30 |
| `BATERIA_BAJA`   | `BATERIA_PROMEDIO`        |     40 |

La etapa genera:

* `alertas.csv`: contiene las reglas que se activaron.
* `secuencias.txt`: contiene los códigos numéricos que serán utilizados por la Etapa 4.

Ejemplo de `alertas.csv`:

```text
REGLA,METRICA,VALOR,OPERADOR,LIMITE
TEMP_ALTA,TEMPERATURA_MAXIMA,38.00,>,35.00
LLUVIA_INTENSA,PRECIPITACION_ACUMULADA,138.00,>,50.00
VIENTO_FUERTE,VIENTO_MAXIMO,42.00,>,40.00
```

Ejemplo de `secuencias.txt`:

```text
10
20
30
```

Para ejecutar únicamente esta etapa desde `etapa3-java`:

```text
javac --release 8 *.java
java Main
```

Si una regla contiene un identificador, operador o valor inválido, la etapa informa el error y finaliza sin continuar.

-`Etapa 4 (C): verificación de integridad sobre la salida de Java, genera`
`resultado_final.txt`.

Recibe secuencias.txt generado por la Etapa 3 y calcula un checksum de verificación de integridad.

Por cada valor leído, en orden, con posicion empezando en 1:

checksum = checksum + valor
checksum = checksum XOR posicion
posicion = posicion + 1

Si secuencias.txt viene vacío (ninguna regla se disparó), el programa lo reporta explícitamente en vez de fallar. El resultado final, junto con la secuencia procesada y el checksum en decimal y hexadecimal, se escribe en resultado_final.txt.


- Se debe integrar las 4 etapas en `run_pipeline.bat` para que se ejecute todo en cascada.

