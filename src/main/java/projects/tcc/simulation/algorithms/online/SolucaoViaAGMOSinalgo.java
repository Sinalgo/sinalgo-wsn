package projects.tcc.simulation.algorithms.online;

import projects.tcc.simulation.algorithms.genetic.AG_Estatico_MO_arq;
import projects.tcc.simulation.io.FakeSimulationOutput;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.Simulation;

public class SolucaoViaAGMOSinalgo {

    private SensorNetwork sensorNetwork;

    private int numeroGeracoes;
    private int tamanhoPopulacao;
    private double txCruzamento;

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
        } else {
            Simulation redeSim = Simulation.currentInstance();
            if (this.sensorNetwork.getCurrentCoveragePercent() >= this.sensorNetwork.getCoverageFactor()) {
                boolean evento = redeSim.simulatePeriod(currentPeriod, new FakeSimulationOutput());
                boolean reestruturar = redeSim.isRestructureNetwork();
                if (reestruturar) {
                    //gerando a POP de Cromossomos inicial para o AG
                    boolean[] vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(this.sensorNetwork, this.numeroGeracoes, this.tamanhoPopulacao, this.txCruzamento);
                    this.sensorNetwork.buildInitialNetwork(vetSensAtiv);
                    System.out.println("===== EVENTO e REESTRUTUROU TEMPO = " + currentPeriod);
                }
                if (evento && !reestruturar) {
                    if (!this.sensorNetwork.supplyCoverageOnline()) {
                        this.sensorNetwork.supplyCoverage();
                        this.sensorNetwork.desligarSensoresDesconexos();
                    }
                    System.out.println("===== EVENTO TEMPO = " + currentPeriod);
                }
            } else {
                System.out.println("Não foi mais possível se manter acima do mínimo de cobertura");
            }
            System.out.println("==> Reestruturação foi requisitada " + redeSim.getRestructureCount());
        }
    }

}
