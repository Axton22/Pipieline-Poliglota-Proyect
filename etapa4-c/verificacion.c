/*
 * Etapa 4 - Verificacion de integridad (en C, reemplaza al ensamblador MIPS)
 *
 * Contrato de entrada:
 *   ../datos/secuencias.txt
 *   Un codigo numerico por linea, generado por la Etapa 3 (Java).
 *   Codigos posibles: 10 (TEMP_ALTA), 20 (LLUVIA_INTENSA),
 *                      30 (VIENTO_FUERTE), 40 (BATERIA_BAJA)
 *   El archivo puede venir vacio si no hubo alertas.
 *
 * Contrato de salida:
 *   ../datos/resultado_final.txt
 *   Reporte legible con la secuencia procesada y el checksum final.
 *
 * Algoritmo de checksum (equivalente al pseudocodigo original de MIPS):
 *   posicion empieza en 1
 *   por cada valor leido:
 *       checksum = checksum + valor
 *       checksum = checksum XOR posicion
 *       posicion = posicion + 1
 */

#include <stdio.h>
#include <stdlib.h>

#define RUTA_ENTRADA  "../datos/secuencias.txt"
#define RUTA_SALIDA   "../datos/resultado_final.txt"
#define MAX_VALORES   1000

int main(void) {
    FILE *entrada = fopen(RUTA_ENTRADA, "r");
    if (entrada == NULL) {
        fprintf(stderr, "Error: no se pudo abrir %s\n", RUTA_ENTRADA);
        fprintf(stderr, "Asegurate de correr este programa desde la carpeta etapa4-c\n");
        return 1;
    }

    int valores[MAX_VALORES];
    int cantidad = 0;
    int linea;

    while (cantidad < MAX_VALORES && fscanf(entrada, "%d", &linea) == 1) {
        valores[cantidad] = linea;
        cantidad++;
    }
    fclose(entrada);

    /* Calculo del checksum, igual al pseudocodigo original */
    unsigned int checksum = 0;
    for (int i = 0; i < cantidad; i++) {
        int posicion = i + 1;
        checksum = checksum + (unsigned int)valores[i];
        checksum = checksum ^ (unsigned int)posicion;
    }

    /* Escribir el reporte final */
    FILE *salida = fopen(RUTA_SALIDA, "w");
    if (salida == NULL) {
        fprintf(stderr, "Error: no se pudo escribir %s\n", RUTA_SALIDA);
        return 1;
    }

    fprintf(salida, "=== Etapa 4: Verificacion de integridad (C) ===\n\n");

    if (cantidad == 0) {
        fprintf(salida, "No se recibieron alertas desde la Etapa 3 (Java).\n");
        fprintf(salida, "Secuencia procesada: (vacia)\n");
    } else {
        fprintf(salida, "Alertas recibidas: %d\n", cantidad);
        fprintf(salida, "Secuencia procesada: ");
        for (int i = 0; i < cantidad; i++) {
            fprintf(salida, "%d", valores[i]);
            if (i < cantidad - 1) {
                fprintf(salida, ", ");
            }
        }
        fprintf(salida, "\n");
    }

    fprintf(salida, "\nChecksum final (decimal): %u\n", checksum);
    fprintf(salida, "Checksum final (hex):     0x%X\n", checksum);
    fprintf(salida, "\nPIPELINE COMPLETADO\n");

    fclose(salida);

    /* Tambien mostrar el resumen en consola, para el .bat */
    printf("Alertas procesadas: %d\n", cantidad);
    printf("Checksum final: %u (0x%X)\n", checksum, checksum);
    printf("Resultado escrito en %s\n", RUTA_SALIDA);

    return 0;
}
