package projects.tcc.simulation.principal;

import projects.tcc.simulation.algorithms.online.SolucaoViaAGMO;
import projects.tcc.simulation.io.ConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;

public class Principal {

    public static void main(String[] args) throws Exception {
        ParametrosEntrada parmEntrada = new ParametrosEntrada(args);
        String nomeArqEntrada = parmEntrada.getCaminhoEntrada() +
                parmEntrada.getNomeQuant() + "50";
        ConfigurationLoader.overrideConfigurationFile(nomeArqEntrada);
        ConfigurationLoader.overrideCoverageFactor(parmEntrada.getMFatorCobMO());
        ConfigurationLoader.overrideDimensions(50, 50);

        // ============== Variaveis para Simulacao ====================
        for (int i = parmEntrada.getNumTesteInicial(); i < parmEntrada.getNumTeste(); i++) {

            // =================== Iniciando a Simulacao ==================
            SensorNetwork rede = new SensorNetwork(ConfigurationLoader.getConfiguration());
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
}
