import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserReglas {
    private static final Pattern PATRON_REGLA = Pattern.compile(
            "^(\\S+)\\s+(\\S+)\\s+(\\S+)$");

    public Regla parsear(String linea, int numeroLinea) {
        Matcher matcher = PATRON_REGLA.matcher(linea.trim());

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Regla invalida en linea " + numeroLinea + ": " + linea);
        }

        String identificador = matcher.group(1);
        String operador = matcher.group(2);
        String valor = matcher.group(3);

        validarIdentificador(identificador, linea, numeroLinea);
        validarOperador(operador, linea, numeroLinea);
        double limite = parsearNumero(valor, linea, numeroLinea);

        return crearRegla(identificador, operador, limite);
    }

    private double parsearNumero(String texto, String linea, int numeroLinea) {
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Valor numerico invalido en linea " + numeroLinea + ": " + linea);
        }
    }

    private void validarIdentificador(String identificador, String linea, int numeroLinea) {
        if (identificador.equals("TEMP_ALTA")
                || identificador.equals("LLUVIA_INTENSA")
                || identificador.equals("VIENTO_FUERTE")
                || identificador.equals("BATERIA_BAJA")) {
            return;
        }

        throw new IllegalArgumentException("Identificador invalido en linea " + numeroLinea + ": " + linea);
    }

    private void validarOperador(String operador, String linea, int numeroLinea) {
        if (operador.equals(">")
                || operador.equals("<")
                || operador.equals(">=")
                || operador.equals("<=")) {
            return;
        }

        throw new IllegalArgumentException("Operador invalido en linea " + numeroLinea + ": " + linea);
    }

    private Regla crearRegla(String identificador, String operador, double limite) {
        if (identificador.equals("TEMP_ALTA")) {
            return new ReglaTemperatura(operador, limite);
        }

        if (identificador.equals("LLUVIA_INTENSA")) {
            return new ReglaPrecipitacion(operador, limite);
        }

        if (identificador.equals("VIENTO_FUERTE")) {
            return new ReglaViento(operador, limite);
        }

        if (identificador.equals("BATERIA_BAJA")) {
            return new ReglaBateria(operador, limite);
        }

        throw new IllegalArgumentException("Identificador no reconocido: " + identificador);
    }
}
