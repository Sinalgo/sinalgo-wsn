package projects.tcc.simulation.principal;

import lombok.Getter;

@Getter
public class ParametrosEntrada {

    private String nomeQuant;
    private String caminhoEntrada;
    private String caminhoSaida;
    private int numTeste;
    private int numTesteInicial;

    private double mFatorCobMO;

    public ParametrosEntrada(String[] args) {
        int i = 0;
        this.nomeQuant = args[i++];
        this.numTesteInicial = Integer.parseInt(args[i++]);
        this.numTeste = Integer.parseInt(args[i++]);
        this.caminhoEntrada = args[i++];
        this.caminhoSaida = args[i++];

        this.mFatorCobMO = Integer.parseInt(args[i]);
        this.mFatorCobMO = this.mFatorCobMO / 100.;
    }

}
