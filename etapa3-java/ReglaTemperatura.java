public class ReglaTemperatura extends Regla {

    public ReglaTemperatura(String operador, double limite) {
        super("TEMP_ALTA", operador, limite, "TEMPERATURA_MAXIMA", 10);
    }

    @Override
    public boolean evaluar(double valor) {
        return comparar(valor);
    }
}
