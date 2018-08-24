package projects.tcc.simulation.main;

import projects.tcc.simulation.algorithms.online.SolucaoViaAGMO;
import projects.tcc.simulation.algorithms.online.SolucaoViaAGMOSinalgo;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfiguration.SensorConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        InputParameters inputParameters = new InputParameters(args);
        String inputFileName = inputParameters.getInputAmount() + "50";
        SimulationConfigurationLoader.overrideConfigurationFile(inputFileName);
        SimulationConfigurationLoader.overrideCoverageFactor(inputParameters.getCoverageFactor());
        SimulationConfigurationLoader.overrideDimensions(50, 50);
        SimulationConfigurationLoader.overrideCrossoverRate(0.9);
        SimulationConfigurationLoader.overridePopulationSize(300);
        SimulationConfigurationLoader.overrideNumberOfGenerations(150);

        SolucaoViaAGMOSinalgo.setOnStopSimulationMessageMethod(() -> {
        });
        SolucaoViaAGMOSinalgo.setStopSimulationMethod(() -> {
        });

        // ============== Variaveis para Simulacao ====================
        for (int i = inputParameters.getInitialTestNumber(); i < inputParameters.getLastTestNumber(); i++) {

            // =================== Iniciando a Simulacao ==================
            SensorNetwork network = SensorNetwork.newInstance();
            createSensors().forEach(network::addSensors);
            createSinks().forEach(network::addSinks);

            SimulationOutput.println("\n\n========= Teste Numero: " + i + " =========");

            /////////////////////////// MEDICAO DE TEMPO //////////////////////
            Chronometer networkTime = new Chronometer();

            networkTime.start();

            Files.createDirectories(Paths.get(inputParameters.getOutputPath()));
            SolucaoViaAGMO solucao = new SolucaoViaAGMO(inputParameters.getOutputPath());
            solucao.simularRede(i);

            networkTime.end();

            String timeFileName = "tempo" + i + ".out";
            new SimulationOutput(null, null, inputParameters.getOutputPath())
                    .generateTimeOutput(timeFileName, networkTime.getTotalTime());
        }
    }

    private static List<Sensor> createSensors() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        int idCounter = 0;
        List<Sensor> sensors = new ArrayList<>();
        for (SensorConfiguration sensorConfig : config.getSensorConfigurations()) {
            sensors.add(new Sensor(idCounter++, sensorConfig.getX(), sensorConfig.getY(),
                    config.getSensorRadius(), config.getCommRadius(), config.getBatteryEnergy(),
                    config.getActivationPower(), config.getReceivePower(), config.getMaintenancePower(),
                    config.getCommRatio()));
        }
        return sensors;
    }

    private static List<Sink> createSinks() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        int idSink = config.getSensorConfigurations().size(); // pois sera o ultimo na lista de sensores.
        List<Sink> sinks = new ArrayList<>();
        for (SensorConfiguration sinkConfig : config.getSinkConfigurations()) {
            sinks.add(new Sink(idSink++, sinkConfig.getX(), sinkConfig.getY(), config.getCommRatio()));
        }
        return sinks;
    }
}
