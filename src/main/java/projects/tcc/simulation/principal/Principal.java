package projects.tcc.simulation.principal;

import projects.tcc.nodes.nodeImplementations.Sensor;
import projects.tcc.nodes.nodeImplementations.Sink;
import projects.tcc.simulation.algorithms.online.SolucaoViaAGMO;
import projects.tcc.simulation.data.SensorHolder;
import projects.tcc.simulation.io.ConfigurationLoader;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfiguration.SensorConfiguration;
import projects.tcc.simulation.rssf.SensorNetwork;

public class Principal {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        ParametrosEntrada parmEntrada = new ParametrosEntrada(args);

        String nomeArqEntrada = parmEntrada.getCaminhoEntrada() +
                parmEntrada.getNomeQuant() + "50";


        // ============== Variaveis para Simulacao ====================


        for (int i = parmEntrada.getNumTesteInicial(); i < parmEntrada.getNumTeste(); i++) {


            // =================== Iniciando a Simulacao ==================

            SensorNetwork.init(50, 50, parmEntrada.getMFatorCobMO());

            ConfigurationLoader.overrideConfiguration(nomeArqEntrada);
            SimulationConfiguration config = ConfigurationLoader.getConfiguration();

            SensorHolder.addSensor(new Sink());
            for (SensorConfiguration sensorConfiguration : config.getSensorConfigurations()) {
                Sensor sensor = new Sensor();
                sensor.setPosition(sensorConfiguration.toPosition());
                SensorHolder.addSensor(sensor);
            }

            System.out.println("\n\n========= Teste Numero: " + i + " =========");

            /////////////////////////// MEDICAO DE TEMPO //////////////////////
            MedirTempo tempoRede = new MedirTempo();

            tempoRede.iniciar();

            SolucaoViaAGMO solucao = new SolucaoViaAGMO(i, parmEntrada.getCaminhoSaida());
            solucao.simularRede(i);

            tempoRede.finalizar();

            String sTempo = "tempo" + i + ".out";
            Saidas.geraArqSaidaTempo(sTempo, parmEntrada.getCaminhoSaida(), tempoRede.getTempoTotal());

        }
    }
}
