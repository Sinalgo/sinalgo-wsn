package projects.tcc.simulation.principal;

import projects.tcc.simulation.algorithms.online.SolucaoViaAGMO;
import projects.tcc.simulation.rssf.RedeSensor;

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

            RedeSensor rede;
            rede = new RedeSensor(nomeArqEntrada, 50, 50, parmEntrada.getMFatorCobMO());

            rede.addSink();
            rede.prepararRede();


            System.out.println("\n\n========= Teste Numero: " + i + " =========");

            /////////////////////////// MEDICAO DE TEMPO //////////////////////
            MedirTempo tempoRede = new MedirTempo();

            tempoRede.iniciar();

            SolucaoViaAGMO solucao = new SolucaoViaAGMO(rede, i, parmEntrada.getCaminhoSaida());
            solucao.simularRede(i);

            tempoRede.finalizar();

            String sTempo = "tempo" + i + ".out";
            Saidas.geraArqSaidaTempo(sTempo, parmEntrada.getCaminhoSaida(), tempoRede.getTempoTotal());

        }
    }
}
