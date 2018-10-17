package projects.wsn.simulation.algorithms;

import projects.wsn.simulation.algorithms.genetic.StaticMultiObjectiveGeneticAlgorithm;
import projects.wsn.simulation.io.SimulationConfiguration;
import projects.wsn.simulation.io.SimulationConfigurationLoader;
import projects.wsn.simulation.network.SensorNetwork;

public class MultiObjectiveGeneticAlgorithm {

    private SensorNetwork sensorNetwork;

    private int numberOfGenerations;
    private int populationSize;
    private double crossoverRate;

    private static MultiObjectiveGeneticAlgorithm currentInstance;

    public static MultiObjectiveGeneticAlgorithm currentInstance() {
        if (currentInstance == null) {
            return newInstance();
        }
        return currentInstance;
    }

    public static MultiObjectiveGeneticAlgorithm newInstance() {
        currentInstance = new MultiObjectiveGeneticAlgorithm(SimulationConfigurationLoader.getConfiguration());
        return currentInstance;
    }

    private MultiObjectiveGeneticAlgorithm(SimulationConfiguration config) {
        this.sensorNetwork = SensorNetwork.currentInstance();
        this.numberOfGenerations = config.getNumberOfGenerations();
        this.populationSize = config.getPopulationSize();
        this.crossoverRate = config.getCrossoverRate();
    }

    public boolean[] computeActiveSensors() {
        //gerando a POP de Cromossomos inicial para o AG
        boolean[] geneticAlgorithmOutput =
                StaticMultiObjectiveGeneticAlgorithm.runMultiObjectiveGeneticAlgorithm(
                        this.sensorNetwork, this.numberOfGenerations,
                        this.populationSize, this.crossoverRate);
        return this.sensorNetwork.buildInitialNetwork(geneticAlgorithmOutput);
    }

}
