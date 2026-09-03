# Pipeline Poliglota — Red de Estaciones Ambientales

El objetivo de este proyecto es construir un único sistema (no programas independientes) donde la salida de cada etapa
se convierte en la entrada de la siguiente, usando un lenguaje distinto por etapa.

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

# Etapa 3 · Java

## Motor de reglas

Esta etapa recibe las métricas generadas por Fortran desde:

`../datos/metricas.csv`

y evalúa las reglas definidas en:

`reglas.txt`

El objetivo es identificar condiciones de interés utilizando programación orientada a objetos, herencia, polimorfismo y un parser sencillo para interpretar las reglas.

## Archivos principales

* `Main.java`: coordina la ejecución de la Etapa 3.
* `ParserReglas.java`: valida e interpreta las reglas.
* `Regla.java`: clase abstracta base.
* `ReglaTemperatura.java`: regla para temperatura alta.
* `ReglaPrecipitacion.java`: regla para lluvia intensa.
* `ReglaViento.java`: regla para viento fuerte.
* `ReglaBateria.java`: regla para batería baja.
* `reglas.txt`: archivo donde se definen las reglas.

## Reglas utilizadas

```text
TEMP_ALTA > 35
LLUVIA_INTENSA > 50
VIENTO_FUERTE > 40
BATERIA_BAJA < 20
```

Los operadores permitidos son:

```text
>
<
>=
<=
```

## Gramática

```text
<regla> ::= <identificador> <operador> <numero>

<operador> ::= ">" | "<" | ">=" | "<="

<identificador> ::= "TEMP_ALTA"
                  | "LLUVIA_INTENSA"
                  | "VIENTO_FUERTE"
                  | "BATERIA_BAJA"
```

El parser valida que cada regla tenga un identificador permitido, un operador válido y un valor numérico.

Por ejemplo:

```text
TEMP_ALTA > 35
```

es válida.

Mientras que:

```text
TEMP_ALTA = 35
```

es inválida porque el operador `=` no forma parte de la gramática.

## Herencia y polimorfismo

La clase abstracta `Regla` contiene los atributos y comportamientos comunes.

De ella heredan:

```text
Regla
├── ReglaTemperatura
├── ReglaPrecipitacion
├── ReglaViento
└── ReglaBateria
```

Todas implementan el método:

```java
evaluar(double valor)
```

Durante la ejecución, `Main` trabaja con objetos de tipo `Regla` y llama a `evaluar()`, permitiendo utilizar polimorfismo.

## Métricas evaluadas

| Regla            | Métrica utilizada         | Código |
| ---------------- | ------------------------- | -----: |
| `TEMP_ALTA`      | `TEMPERATURA_MAXIMA`      |     10 |
| `LLUVIA_INTENSA` | `PRECIPITACION_ACUMULADA` |     20 |
| `VIENTO_FUERTE`  | `VIENTO_MAXIMO`           |     30 |
| `BATERIA_BAJA`   | `BATERIA_PROMEDIO`        |     40 |

## Archivos de salida

### `../datos/alertas.csv`

Contiene las reglas que se activaron.

Ejemplo:

```csv
REGLA,METRICA,VALOR,OPERADOR,LIMITE
TEMP_ALTA,TEMPERATURA_MAXIMA,38.00,>,35.00
LLUVIA_INTENSA,PRECIPITACION_ACUMULADA,138.00,>,50.00
VIENTO_FUERTE,VIENTO_MAXIMO,42.00,>,40.00
```

### `../datos/secuencias.txt`

Contiene los códigos numéricos que utilizará la siguiente etapa del pipeline.

Ejemplo:

```text
10
20
30
```

## Ejecución

Desde la carpeta `etapa3-java`:

```bash
javac --release 8 *.java
java Main
```

Resultado esperado con los datos actuales:

```text
Etapa 3 completada.
Alertas generadas: 3
Secuencias generadas: 3
```

## Validación de errores

Si una regla no cumple con la gramática, la etapa se detiene e informa el error.

Ejemplo:

```text
TEMP_ALTA = 35
```

Resultado:

```text
Error en Etapa 3: Operador invalido en linea 1: TEMP_ALTA = 35
```

## Integración en el pipeline

```text
metricas.csv
     ↓
   JAVA
     ↓
alertas.csv
secuencias.txt
     ↓
Etapa 4
```

La Etapa 3 utiliza directamente la salida generada por Fortran y produce los archivos que serán utilizados por la etapa siguiente.

