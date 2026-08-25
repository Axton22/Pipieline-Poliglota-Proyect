public class ReglaViento extends Regla {

    public ReglaViento(String operador, double limite) {
        super("VIENTO_FUERTE", operador, limite, "VIENTO_MAXIMO", 30);
    }

    @Override
    public boolean evaluar(double valor) {
        return comparar(valor);
    }
}
