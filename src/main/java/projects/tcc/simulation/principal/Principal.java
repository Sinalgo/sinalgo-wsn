package projects.tcc.simulation.principal;

import projects.tcc.simulation.algorithms.online.SolucaoViaAGMO;
import projects.tcc.simulation.io.ConfigurationLoader;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.rssf.Environment;

public class Principal {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        //TODO Auto-generated method stub

        ParametrosEntrada parmEntrada = new ParametrosEntrada(args);

        String nomeArqEntrada = parmEntrada.getCaminhoEntrada() +
                parmEntrada.getNomeQuant() + "50";


        // ============== Variaveis para Simulacao ====================


        for (int i = parmEntrada.getNumTesteInicial(); i < parmEntrada.getNumTeste(); i++) {


            // =================== Iniciando a Simulacao ==================

            ConfigurationLoader.overrideConfiguration(nomeArqEntrada);
            SimulationConfiguration config = ConfigurationLoader.getConfiguration();
            new Environment(config.) (nomeArqEntrada, 50, 50, parmEntrada.getMFatorCobMO());

            rede.addSink();
            rede.prepararRede();


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
