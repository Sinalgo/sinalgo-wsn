package projects.tcc.simulation.principal;

import projects.tcc.simulation.algorithms.online.SolucaoViaAGMO;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfiguration.SensorConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;
import projects.tcc.simulation.wsn.data.impl.WSNSensor;
import projects.tcc.simulation.wsn.data.impl.WSNSink;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Principal {

    public static void main(String[] args) throws Exception {
        ParametrosEntrada parmEntrada = new ParametrosEntrada(args);
        String nomeArqEntrada = parmEntrada.getNomeQuant() + "50";
        SimulationConfigurationLoader.overrideConfigurationFile(nomeArqEntrada);
        SimulationConfigurationLoader.overrideCoverageFactor(parmEntrada.getMFatorCobMO());
        SimulationConfigurationLoader.overrideDimensions(50, 50);
        SimulationConfigurationLoader.overrideCrossoverRate(0.9);
        SimulationConfigurationLoader.overridePopulationSize(300);
        SimulationConfigurationLoader.overrideNumberOfGenerations(150);

        // ============== Variaveis para Simulacao ====================
        for (int i = parmEntrada.getNumTesteInicial(); i < parmEntrada.getNumTeste(); i++) {

            // =================== Iniciando a Simulacao ==================
            SensorNetwork rede = SensorNetwork.newInstance();
            createSensors().forEach(rede::addSensors);
            createSinks().forEach(rede::addSinks);

            System.out.println("\n\n========= Teste Numero: " + i + " =========");

            /////////////////////////// MEDICAO DE TEMPO //////////////////////
            MedirTempo tempoRede = new MedirTempo();

            tempoRede.iniciar();

            Files.createDirectories(Paths.get(parmEntrada.getCaminhoSaida()));
            SolucaoViaAGMO solucao = new SolucaoViaAGMO(SimulationConfigurationLoader.getConfiguration(), parmEntrada.getCaminhoSaida());
            solucao.simularRede(i);

            tempoRede.finalizar();

            String sTempo = "tempo" + i + ".out";
            Saidas.geraArqSaidaTempo(sTempo, parmEntrada.getCaminhoSaida(), tempoRede.getTempoTotal());
        }
    }

    private static List<Sensor> createSensors() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        int idCounter = 0;
        List<Sensor> sensors = new ArrayList<>();
        for (SensorConfiguration sensorConfig : config.getSensorConfigurations()) {
            sensors.add(new WSNSensor(idCounter++, sensorConfig.getX(), sensorConfig.getY(),
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
            sinks.add(new WSNSink(idSink++, sinkConfig.getX(), sinkConfig.getY(), config.getCommRatio()));
        }
        return sinks;
    }
}
