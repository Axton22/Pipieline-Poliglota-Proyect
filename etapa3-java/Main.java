import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Main {
    private static final Path ARCHIVO_METRICAS = Paths.get("..", "datos", "metricas.csv");
    private static final Path ARCHIVO_REGLAS = Paths.get("reglas.txt");
    private static final Path ARCHIVO_ALERTAS = Paths.get("..", "datos", "alertas.csv");
    private static final Path ARCHIVO_SECUENCIAS = Paths.get("..", "datos", "secuencias.txt");

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);

        try {
            Map<String, Double> metricas = leerMetricas();
            List<Regla> reglas = leerReglas();
            List<String> alertas = new ArrayList<>();
            List<String> secuencias = new ArrayList<>();

            for (Regla regla : reglas) {
                Double valor = metricas.get(regla.getMetrica());

                if (valor == null) {
                    System.out.println("Metrica faltante para la regla " + regla.getIdentificador()
                            + ": " + regla.getMetrica());
                    continue;
                }

                if (regla.evaluar(valor)) {
                    alertas.add(formatearAlerta(regla, valor));
                    secuencias.add(String.valueOf(regla.getCodigoSecuencia()));
                }
            }

            escribirAlertas(alertas);
            escribirSecuencias(secuencias);

            System.out.println("Etapa 3 completada.");
            System.out.println("Alertas generadas: " + alertas.size());
            System.out.println("Secuencias generadas: " + secuencias.size());
        } catch (IOException | IllegalArgumentException ex) {
            System.out.println("Error en Etapa 3: " + ex.getMessage());
            System.exit(1);
        }
    }

    private static Map<String, Double> leerMetricas() throws IOException {
        Map<String, Double> metricas = new HashMap<>();

        try (BufferedReader lector = Files.newBufferedReader(ARCHIVO_METRICAS)) {
            String linea = lector.readLine();

            if (linea == null || !linea.trim().equals("METRICA,VALOR")) {
                throw new IllegalArgumentException("metricas.csv no tiene el encabezado esperado METRICA,VALOR");
            }

            while ((linea = lector.readLine()) != null) {
                if (linea.trim().isEmpty()) {
                    continue;
                }

                String[] partes = linea.split(",", -1);
                if (partes.length != 2) {
                    throw new IllegalArgumentException("Linea invalida en metricas.csv: " + linea);
                }

                String nombre = partes[0].trim();
                double valor = Double.parseDouble(partes[1].trim());
                metricas.put(nombre, valor);
            }
        }

        return metricas;
    }

    private static List<Regla> leerReglas() throws IOException {
        List<Regla> reglas = new ArrayList<>();
        ParserReglas parser = new ParserReglas();

        try (BufferedReader lector = Files.newBufferedReader(ARCHIVO_REGLAS)) {
            String linea;
            int numeroLinea = 0;

            while ((linea = lector.readLine()) != null) {
                numeroLinea++;

                if (linea.trim().isEmpty()) {
                    continue;
                }

                reglas.add(parser.parsear(linea, numeroLinea));
            }
        }

        if (reglas.isEmpty()) {
            throw new IllegalArgumentException("No se encontraron reglas en " + ARCHIVO_REGLAS);
        }

        return reglas;
    }

    private static String formatearAlerta(Regla regla, double valor) {
        return regla.getIdentificador() + ","
                + regla.getMetrica() + ","
                + String.format(Locale.US, "%.2f", valor) + ","
                + regla.getOperador() + ","
                + String.format(Locale.US, "%.2f", regla.getLimite());
    }

    private static void escribirAlertas(List<String> alertas) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(ARCHIVO_ALERTAS)) {
            escritor.write("REGLA,METRICA,VALOR,OPERADOR,LIMITE");
            escritor.newLine();

            for (String alerta : alertas) {
                escritor.write(alerta);
                escritor.newLine();
            }
        }
    }

    private static void escribirSecuencias(List<String> secuencias) throws IOException {
        try (BufferedWriter escritor = Files.newBufferedWriter(ARCHIVO_SECUENCIAS)) {
            for (String secuencia : secuencias) {
                escritor.write(secuencia);
                escritor.newLine();
            }
        }
    }
}
