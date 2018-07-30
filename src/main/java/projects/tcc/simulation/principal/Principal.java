package projects.tcc.simulation.principal;

import projects.tcc.simulation.algorithms.online.SolucaoViaAGMO;
import projects.tcc.simulation.io.ConfigurationLoader;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfiguration.SensorConfiguration;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;
import projects.tcc.simulation.wsn.data.impl.WSNSensor;
import projects.tcc.simulation.wsn.data.impl.WSNSink;

import java.util.ArrayList;
import java.util.List;

public class Principal {

    public static void main(String[] args) throws Exception {
        ParametrosEntrada parmEntrada = new ParametrosEntrada(args);
        String nomeArqEntrada = parmEntrada.getNomeQuant() + "50";
        ConfigurationLoader.overrideConfigurationFile(nomeArqEntrada);
        ConfigurationLoader.overrideCoverageFactor(parmEntrada.getMFatorCobMO());
        ConfigurationLoader.overrideDimensions(50, 50);

        // ============== Variaveis para Simulacao ====================
        for (int i = parmEntrada.getNumTesteInicial(); i < parmEntrada.getNumTeste(); i++) {

            // =================== Iniciando a Simulacao ==================
            SensorNetwork rede = SensorNetwork.getCurrentInstance();
            createSensors().forEach(rede::addSensors);
            createSinks().forEach(rede::addSinks);
            rede.prepararRede();

            System.out.println("\n\n========= Teste Numero: " + i + " =========");

            /////////////////////////// MEDICAO DE TEMPO //////////////////////
            MedirTempo tempoRede = new MedirTempo();

            tempoRede.iniciar();

            SolucaoViaAGMO solucao = new SolucaoViaAGMO(rede, parmEntrada.getCaminhoSaida());
            solucao.simularRede(i);

            tempoRede.finalizar();

            String sTempo = "tempo" + i + ".out";
            Saidas.geraArqSaidaTempo(sTempo, parmEntrada.getCaminhoSaida(), tempoRede.getTempoTotal());
        }
    }

    private static List<Sensor> createSensors() {
        SimulationConfiguration config = ConfigurationLoader.getConfiguration();
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
        SimulationConfiguration config = ConfigurationLoader.getConfiguration();
        int idSink = config.getSensorConfigurations().size(); // pois sera o ultimo na lista de sensores.
        List<Sink> sinks = new ArrayList<>();
        for (SensorConfiguration sinkConfig : config.getSinkConfigurations()) {
            sinks.add(new WSNSink(idSink++, sinkConfig.getX(), sinkConfig.getY(), config.getCommRatio()));
        }
        return sinks;
    }
}
