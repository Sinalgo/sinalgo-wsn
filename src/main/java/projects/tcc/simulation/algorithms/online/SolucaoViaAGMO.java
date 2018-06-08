package projects.tcc.simulation.algorithms.online;


import projects.tcc.simulation.algorithms.genetic.AG_Estatico_MO_arq;
import projects.tcc.simulation.principal.Saidas;
import projects.tcc.simulation.rssf.RedeSensor;
import projects.tcc.simulation.rssf.Simulacao;

public class SolucaoViaAGMO {

    private RedeSensor rede;

    private int numeroGeracoes;
    private int tamanhoPopulacao;
    private double txCruzamento;
    private double txMutacao;
    private int testeNumero;
    private String caminhoSaida;

    public SolucaoViaAGMO(RedeSensor rede, int testeNumero, String caminhoSaida) {

        this.rede = rede;

        this.numeroGeracoes = 150;
        this.tamanhoPopulacao = 300;
        this.txCruzamento = 0.9;
        this.txMutacao = 0.2;
        this.testeNumero = testeNumero;
        this.caminhoSaida = caminhoSaida;
    }

    public void simularRede(int testNum) throws Exception {

        //gerando a POP de Cromossomos inicial para o AG
        boolean[] vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(rede, numeroGeracoes, tamanhoPopulacao, txCruzamento, txMutacao);
        /////////////////////////// REDE INICIAL ///////////////////////////////

        for (boolean i : vetSensAtiv) {
            if (i)
                System.out.print("1 ");
            else
                System.out.print("0 ");
        }

        rede.constroiRedeInicial(vetSensAtiv);

        Simulacao redeSim;
        redeSim = new Simulacao(rede);
        redeSim.setTesteNumero(testeNumero);

        Saidas saida = new Saidas(rede, redeSim, caminhoSaida);

        int perAtual = 0;
        boolean evento = true;
        while (rede.getPorcCobAtual() >= rede.getFatorCob()) {

            evento = redeSim.simulaUmPer(evento, perAtual++, saida);

            boolean reestruturar = redeSim.isReestrutrarRede();

            if (reestruturar) {
                //gerando a POP de Cromossomos inicial para o AG
                vetSensAtiv = AG_Estatico_MO_arq.resolveAG_Estatico_MO(rede, numeroGeracoes, tamanhoPopulacao, txCruzamento, txMutacao);
                rede.constroiRedeInicial(vetSensAtiv);
                System.out.println("===== EVENTO e REESTRUTUROU TEMPO = " + perAtual);
            }

            if (evento && !reestruturar) {

                System.out.println("===== EVENTO TEMPO = " + perAtual);
                if (!rede.suprirOnline()) {
                    rede.suprirCobertura();
                    rede.desligarSensoresDesconexos();
                }

            }

        }
        // Gerar arquivo Final da Simulacao
        saida.geraArquivoSimulador(perAtual++);
        //gerar impressao na tela
        saida.gerarSaidaTela(perAtual);
        System.out.println("==> Reestruturação foi requisitada " + redeSim.getContChamadaReest());
        //gerar arquivo com os dados de cada periodo: Cob, EC e ER.
        saida.gerarArqSimulacao(testNum, "Hibrido");

    }

}
