package projects.tcc.simulation.algorithms.online;

import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.algorithms.genetic.AG_Estatico_MO_arq;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.Simulation;
import sinalgo.tools.Tools;

import java.util.ArrayList;
import java.util.List;

public class SolucaoViaAGMO {

    private SensorNetwork sensorNetwork;

    private int numeroGeracoes;
    private int tamanhoPopulacao;
    private double txCruzamento;

    @Setter
    private SimulationOutput simulationOutput;

    @Getter
    @Setter
    private boolean stopSimulationOnFailure;

    @Setter
    private static Runnable stopSimulationMethod = Tools::stopSimulation;

    @Setter
    private static Runnable onStopSimulationMessageMethod = () -> Tools.minorError("Não foi mais possível se manter acima do mínimo de cobertura");

    private static SolucaoViaAGMO currentInstance;

    public static SolucaoViaAGMO currentInstance() {
        if (currentInstance == null) {
            return newInstance();
        }
        return currentInstance;
    }

    public static SolucaoViaAGMO newInstance() {
        currentInstance = new SolucaoViaAGMO(SimulationConfigurationLoader.getConfiguration());
        return currentInstance;
    }

    private SolucaoViaAGMO(SimulationConfiguration config) {
        this.sensorNetwork = SensorNetwork.currentInstance();
        this.numeroGeracoes = config.getNumberOfGenerations();
        this.tamanhoPopulacao = config.getPopulationSize();
        this.txCruzamento = config.getCrossoverRate();
        this.simulationOutput = new SimulationOutput();
    }

    public boolean[] simularRede(int currentPeriod) throws Exception {
        //gerando a POP de Cromossomos inicial para o AG
        boolean[] vetSensAtiv = null;
        if (currentPeriod == 0) {
            vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(this.sensorNetwork, this.numeroGeracoes,
                    this.tamanhoPopulacao, this.txCruzamento);
            /////////////////////////// REDE INICIAL ///////////////////////////////
            List<String> vetSensAtivStr = new ArrayList<>(vetSensAtiv.length);
            for (boolean i : vetSensAtiv) {
                vetSensAtivStr.add(i ? "1" : "0");
            }
            SimulationOutput.println(String.join(" ", vetSensAtivStr) + "\n");
            vetSensAtiv = this.sensorNetwork.buildInitialNetwork(vetSensAtiv);
        }
        Simulation redeSim = Simulation.currentInstance();
        if (this.sensorNetwork.getCurrentCoveragePercent() >= this.sensorNetwork.getCoverageFactor()) {
            boolean evento = redeSim.simulatePeriod(currentPeriod, this.simulationOutput);
            boolean reestruturar = redeSim.isRestructureNetwork();
            if (reestruturar || evento) {
                //gerando a POP de Cromossomos inicial para o AG
                vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(this.sensorNetwork, this.numeroGeracoes,
                        this.tamanhoPopulacao, this.txCruzamento);
                vetSensAtiv = this.sensorNetwork.buildInitialNetwork(vetSensAtiv);
                SimulationOutput.println("===== EVENTO e REESTRUTUROU TEMPO = " + currentPeriod);
                if (isStopSimulationOnFailure()) {
                    stopSimulationMethod.run();
                }
            }
        } else {
            stopSimulationMethod.run();
            onStopSimulationMessageMethod.run();
        }
        SimulationOutput.println("==> Reestruturação foi requisitada " + redeSim.getRestructureCount());
        return vetSensAtiv;
    }

}
