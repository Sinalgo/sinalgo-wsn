package projects.tcc.simulation.algorithms.online;

import projects.tcc.simulation.algorithms.genetic.AG_Estatico_MO_arq;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.principal.Saidas;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.Simulation;

import java.util.ArrayList;
import java.util.List;

public class SolucaoViaAGMO {

    private SensorNetwork sensorNetwork;

    private int numeroGeracoes;
    private int tamanhoPopulacao;
    private double txCruzamento;
    private String caminhoSaida;

    public SolucaoViaAGMO(SimulationConfiguration config, String caminhoSaida) {
        this.sensorNetwork = SensorNetwork.getCurrentInstance();
        this.numeroGeracoes = config.getNumberOfGenerations();
        this.tamanhoPopulacao = config.getPopulationSize();
        this.txCruzamento = config.getCrossoverRate();
        this.caminhoSaida = caminhoSaida;
    }

    public void simularRede(int testNum) throws Exception {
        //gerando a POP de Cromossomos inicial para o AG
        boolean[] vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(this.sensorNetwork, this.numeroGeracoes, this.tamanhoPopulacao, this.txCruzamento);
        /////////////////////////// REDE INICIAL ///////////////////////////////

        List<String> vetSensAtivStr = new ArrayList<>(vetSensAtiv.length);
        for (boolean i : vetSensAtiv) {
            vetSensAtivStr.add(i ? "1" : "0");
        }
        System.out.println(String.join(" ", vetSensAtivStr));
        this.sensorNetwork.constroiRedeInicial(vetSensAtiv);
        Simulation redeSim = new Simulation(this.sensorNetwork);
        Saidas saida = new Saidas(this.sensorNetwork, redeSim, this.caminhoSaida);
        int perAtual = 0;
        while (this.sensorNetwork.getCurrentCoveragePercent() >= this.sensorNetwork.getCoverageFactor()) {
            boolean evento = redeSim.simulaUmPer(perAtual++, saida);
            boolean reestruturar = redeSim.isReestrutrarRede();
            if (reestruturar) {
                //gerando a POP de Cromossomos inicial para o AG
                vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(this.sensorNetwork, this.numeroGeracoes, this.tamanhoPopulacao, this.txCruzamento);
                this.sensorNetwork.constroiRedeInicial(vetSensAtiv);
                System.out.println("===== EVENTO e REESTRUTUROU TEMPO = " + perAtual);
            }
            if (evento && !reestruturar) {
                System.out.println("===== EVENTO TEMPO = " + perAtual);
                if (!this.sensorNetwork.suprirOnline()) {
                    this.sensorNetwork.suprirCobertura();
                    this.sensorNetwork.desligarSensoresDesconexos();
                }
            }
        }
        // Gerar arquivo Final da Simulacao
        saida.generateSimulatorOutput(perAtual++);
        //gerar impressao na tela
        saida.gerarSaidaTela(perAtual);
        System.out.println("==> Reestruturação foi requisitada " + redeSim.getContChamadaReest());
        //gerar arquivo com os dados de cada periodo: Cob, EC e ER.
        saida.generateSimulatorOutput(testNum, "Hibrido");
    }

}
