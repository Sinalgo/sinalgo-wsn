package projects.tcc.simulation.algorithms.online;

import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.algorithms.genetic.AG_Estatico_MO_arq;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.io.SinalgoSimulationOutput;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.Simulation;
import sinalgo.tools.Tools;

public class SolucaoViaAGMOSinalgo {

    private SensorNetwork sensorNetwork;

    private int numeroGeracoes;
    private int tamanhoPopulacao;
    private double txCruzamento;

    @Getter
    @Setter
    private boolean stopSimulationOnFailure;

    @Setter
    private boolean ignoreOnline = true;

    private static SolucaoViaAGMOSinalgo currentInstance;

    public static SolucaoViaAGMOSinalgo currentInstance() {
        if (currentInstance == null) {
            return newInstance();
        }
        return currentInstance;
    }

    public static SolucaoViaAGMOSinalgo newInstance() {
        currentInstance = new SolucaoViaAGMOSinalgo(SimulationConfigurationLoader.getConfiguration());
        return currentInstance;
    }

    private SolucaoViaAGMOSinalgo(SimulationConfiguration config) {
        this.sensorNetwork = SensorNetwork.currentInstance();
        this.numeroGeracoes = config.getNumberOfGenerations();
        this.tamanhoPopulacao = config.getPopulationSize();
        this.txCruzamento = config.getCrossoverRate();
    }

    public void simularRede(int currentPeriod) throws Exception {
        //gerando a POP de Cromossomos inicial para o AG
        if (currentPeriod == 0) {
            boolean[] vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(this.sensorNetwork, this.numeroGeracoes, this.tamanhoPopulacao, this.txCruzamento);
            /////////////////////////// REDE INICIAL ///////////////////////////////
            this.sensorNetwork.buildInitialNetwork(vetSensAtiv);
        }
        Simulation redeSim = Simulation.currentInstance();
        if (this.sensorNetwork.getCurrentCoveragePercent() >= this.sensorNetwork.getCoverageFactor()) {
            boolean evento = redeSim.simulatePeriod(currentPeriod, new SinalgoSimulationOutput());
            boolean reestruturar = redeSim.isRestructureNetwork();
            if (reestruturar || (evento && ignoreOnline)) {
                //gerando a POP de Cromossomos inicial para o AG
                boolean[] vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(this.sensorNetwork, this.numeroGeracoes, this.tamanhoPopulacao, this.txCruzamento);
                this.sensorNetwork.buildInitialNetwork(vetSensAtiv);
                SimulationOutput.println("===== EVENTO e REESTRUTUROU TEMPO = " + currentPeriod);
                if (isStopSimulationOnFailure()) {
                    Tools.stopSimulation();
                }
            }
            if (evento && !reestruturar && !ignoreOnline) {
                if (!this.sensorNetwork.supplyCoverageOnline()) {
                    this.sensorNetwork.supplyCoverage();
                    this.sensorNetwork.desligarSensoresDesconexos();
                }
                SimulationOutput.println("===== EVENTO TEMPO = " + currentPeriod);
            }
        } else {
            Tools.stopSimulation();
            Tools.minorError("Não foi mais possível se manter acima do mínimo de cobertura");
        }
        SimulationOutput.println("==> Reestruturação foi requisitada " + redeSim.getRestructureCount());
    }

}
