package projects.tcc.simulation.algorithms.online;

import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.algorithms.genetic.AG_Estatico_MO_arq;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;

public class SolucaoViaAGMO {

    private SensorNetwork sensorNetwork;

    private int numeroGeracoes;
    private int tamanhoPopulacao;
    private double txCruzamento;

    @Getter
    @Setter
    private boolean stopSimulationOnFailure;

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
    }

    public boolean[] simularRede() {
        //gerando a POP de Cromossomos inicial para o AG
        boolean[] vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(this.sensorNetwork, this.numeroGeracoes,
                this.tamanhoPopulacao, this.txCruzamento);
        return this.sensorNetwork.buildInitialNetwork(vetSensAtiv);
    }

}
