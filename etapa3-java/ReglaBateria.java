public class ReglaBateria extends Regla {

    public ReglaBateria(String operador, double limite) {
        super("BATERIA_BAJA", operador, limite, "BATERIA_PROMEDIO", 40);
    }

    @Override
    public boolean evaluar(double valor) {
        return comparar(valor);
    }
}
