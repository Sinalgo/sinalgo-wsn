package projects.tcc.principal;


public class ParametrosEntrada {

    private String nomeQuant;
    private String caminhoEntrada;
    private String caminhoSaida;
    private String nomeArqEntrada;
    private String tipoFalha;
    private int numTeste;
    private int numTesteInicial;
    private int modMat;

    private int T; //Tempo Total de funcionamento da rede.
    private int t; //Subdivis�o do tempo total.

    private boolean medirTempoAlg;
    private boolean falhaGeradas;

    private boolean imprimirPeriodos;
    private boolean imprimirOnline;
    private boolean imprimirHibrido;

    private double mFatorCobMO;

    public ParametrosEntrada(String[] args) {

        int i = 0;

        nomeQuant = args[i++];
        numTesteInicial = Integer.parseInt(args[i++]);
        numTeste = Integer.parseInt(args[i++]);
        modMat = 1;
        caminhoEntrada = args[i++];
        caminhoSaida = args[i++];

        medirTempoAlg = true;

        mFatorCobMO = Integer.parseInt(args[i++]);
        mFatorCobMO = mFatorCobMO / 100.;

        falhaGeradas = Integer.parseInt(args[i++]) == 1;

        if (args.length > 7) {
            tipoFalha = args[i];
        }
    }

    public String getCaminhoEntrada() {
        return caminhoEntrada;
    }

    public String getCaminhoSaida() {
        return caminhoSaida;
    }

    public boolean isFalhaGeradas() {
        return falhaGeradas;
    }

    public boolean isImprimirHibrido() {
        return imprimirHibrido;
    }

    public boolean isImprimirOnline() {
        return imprimirOnline;
    }

    public boolean isImprimirPeriodos() {
        return imprimirPeriodos;
    }

    public boolean isMedirTempoAlg() {
        return medirTempoAlg;
    }

    public int getModMat() {
        return modMat;
    }

    public String getNomeArqEntrada() {
        return nomeArqEntrada;
    }

    public String getNomeQuant() {
        return nomeQuant;
    }

    public int getNumTesteInicial() {
        return numTesteInicial;
    }

    public int getNumTeste() {
        return numTeste;
    }

    public int getQuantPerRede() {
        return T / t;
    }

    public String getTipoFalha() {
        return tipoFalha;
    }

    public double getMFatorCobMO() {
        return mFatorCobMO;
    }


}
