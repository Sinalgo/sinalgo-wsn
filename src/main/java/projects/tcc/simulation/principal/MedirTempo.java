package projects.tcc.simulation.principal;

import java.util.Date;

public class MedirTempo {

    private Date inicio;
    private Date fim;
    private long tempoTotal;

    public MedirTempo() {
        tempoTotal = 0;
    }

    public double getTempoTotal() {
        return (double) (tempoTotal) / (double) (1000);
    }

    public void addTempo(long tempo) {
        tempoTotal += tempo;
    }

    public void iniciar() {
        inicio = new Date();
    }

    public void finalizar() {
        fim = new Date();
        getTempoExecSeg();
    }

    public void getTempoExecSeg() {
        long tempo = fim.getTime() - inicio.getTime();
        addTempo(tempo);
    }

}
