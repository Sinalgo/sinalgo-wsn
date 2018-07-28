package projects.tcc.simulation.principal;

import java.util.Date;

public class MedirTempo {

    private Date inicio;
    private Date fim;
    private long tempoTotal;

    public MedirTempo() {
        this.tempoTotal = 0;
    }

    public double getTempoTotal() {
        return (double) (this.tempoTotal) / (double) (1000);
    }

    private void addTempo(long tempo) {
        this.tempoTotal += tempo;
    }

    public void iniciar() {
        this.inicio = new Date();
    }

    public void finalizar() {
        this.fim = new Date();
        this.getTempoExecSeg();
    }

    private void getTempoExecSeg() {
        long tempo = this.fim.getTime() - this.inicio.getTime();
        this.addTempo(tempo);
    }

}
