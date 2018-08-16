package projects.tcc.simulation.algorithms.online;

import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.Simulation;

public class SolucaoViaAGMO {

    private String caminhoSaida;

    public SolucaoViaAGMO(String caminhoSaida) {
        this.caminhoSaida = caminhoSaida;
    }

    public void simularRede(int testNum) throws Exception {
        //gerando a POP de Cromossomos inicial para o AG
        SensorNetwork network = SensorNetwork.currentInstance();
        SimulationOutput saida = new SimulationOutput(network, Simulation.newInstance(), this.caminhoSaida);
        SolucaoViaAGMOSinalgo solucao = SolucaoViaAGMOSinalgo.newInstance();
        solucao.setSimulationOutput(saida);
        int perAtual = 0;
        while (perAtual == 0 ||
                network.getCurrentCoveragePercent() >= network.getCoverageFactor()) {
            solucao.simularRede(perAtual++);
        }
        // Gerar arquivo Final da Simulacao
        saida.generateSimulatorOutput(perAtual++);
        //gerar impressao na tela
        saida.generateConsoleOutput(perAtual);
        SimulationOutput.println("==> Reestruturação foi requisitada " + Simulation.currentInstance().getRestructureCount());
        //gerar arquivo com os dados de cada periodo: Cob, EC e ER.
        saida.generateSimulatorOutput(testNum, "Hibrido");
    }

}
