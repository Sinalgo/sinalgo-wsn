package projects.tcc.simulation.algorithms.online;


import lombok.extern.java.Log;
import projects.tcc.simulation.algorithms.genetic.AG_Estatico_MO_arq;
import projects.tcc.simulation.principal.Saidas;
import projects.tcc.simulation.rssf.Environment;
import projects.tcc.simulation.rssf.SensorNetwork;
import projects.tcc.simulation.rssf.Simulacao;

@Log
public class SolucaoViaAGMO {

    private int numeroGeracoes;
    private int tamanhoPopulacao;
    private double txCruzamento;
    private double txMutacao;
    private int testeNumero;
    private String caminhoSaida;

    public SolucaoViaAGMO(int testeNumero, String caminhoSaida) {
        this.numeroGeracoes = 150;
        this.tamanhoPopulacao = 300;
        this.txCruzamento = 0.9;
        this.txMutacao = 0.2;
        this.testeNumero = testeNumero;
        this.caminhoSaida = caminhoSaida;
    }

    public void simularRede(int testNum) throws Exception {

        //gerando a POP de Cromossomos inicial para o AG
        boolean[] vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(numeroGeracoes, tamanhoPopulacao, txCruzamento, txMutacao);
        /////////////////////////// REDE INICIAL ///////////////////////////////

        StringBuilder sb = new StringBuilder();
        for (boolean i : vetSensAtiv) {
            if (i)
                sb.append("1 ");
            else
                sb.append("0 ");
        }
        log.info(sb.toString());

        SensorNetwork.createInitialNetwork(vetSensAtiv);

        Simulacao redeSim = new Simulacao();
        redeSim.setTesteNumero(testeNumero);

        Saidas saida = new Saidas(redeSim, caminhoSaida);

        int perAtual = 0;
        boolean evento = true;
        while (Double.compare(Environment.getCurrentCoverage(),
                Environment.getCoverageFactor()) >= 0) {

            evento = redeSim.simulaUmPer(evento, perAtual++, saida);

            boolean reestruturar = redeSim.isReestrutrarRede();

            if (reestruturar) {
                //gerando a POP de Cromossomos inicial para o AG
                vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(numeroGeracoes, tamanhoPopulacao, txCruzamento, txMutacao);
                SensorNetwork.createInitialNetwork(vetSensAtiv);
                log.info("===== EVENTO e REESTRUTUROU TEMPO = " + perAtual);
            }

            if (evento && !reestruturar) {

                log.info("===== EVENTO TEMPO = " + perAtual);
                if (!SensorNetwork.supplyCoverageOnline()) {
                    SensorNetwork.supplyCoverage();
                    SensorNetwork.updateConnections();
                }

            }

        }
        // Gerar arquivo Final da Simulacao
        saida.geraArquivoSimulador(perAtual++);
        //gerar impressao na tela
        saida.gerarSaidaTela(perAtual);
        log.info("==> Reestruturação foi requisitada " + redeSim.getContChamadaReest());
        //gerar arquivo com os dados de cada periodo: Cob, EC e ER.
        saida.gerarArqSimulacao(testNum, "Hibrido");

    }

}
