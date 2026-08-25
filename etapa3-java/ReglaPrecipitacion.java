public class ReglaPrecipitacion extends Regla {

    public ReglaPrecipitacion(String operador, double limite) {
        super("LLUVIA_INTENSA", operador, limite, "PRECIPITACION_ACUMULADA", 20);
    }

    @Override
    public boolean evaluar(double valor) {
        return comparar(valor);
    }
}
