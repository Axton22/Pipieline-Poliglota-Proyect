public abstract class Regla {
    protected final String identificador;
    protected final String operador;
    protected final double limite;
    protected final String metrica;
    protected final int codigoSecuencia;

    public Regla(String identificador, String operador, double limite, String metrica, int codigoSecuencia) {
        this.identificador = identificador;
        this.operador = operador;
        this.limite = limite;
        this.metrica = metrica;
        this.codigoSecuencia = codigoSecuencia;
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getOperador() {
        return operador;
    }

    public double getLimite() {
        return limite;
    }

    public String getMetrica() {
        return metrica;
    }

    public int getCodigoSecuencia() {
        return codigoSecuencia;
    }

    public abstract boolean evaluar(double valor);

    protected boolean comparar(double valor) {
        if (operador.equals(">")) {
            return valor > limite;
        }

        if (operador.equals("<")) {
            return valor < limite;
        }

        if (operador.equals(">=")) {
            return valor >= limite;
        }

        if (operador.equals("<=")) {
            return valor <= limite;
        }

        return false;
    }
}
