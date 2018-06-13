package projects.tcc.simulation.rssf;

import projects.tcc.simulation.principal.Saidas;

import java.util.ArrayList;

public class Simulacao {

    private ArrayList<Double> vEnResRede;
    private ArrayList<Double> vEnConsRede;
    private ArrayList<Double> vpCobertura;
    private double pCobAtual;       // porcentagem de cobertura atual
    private double enResRede;       // Energia Total Residual da rede.
    private double enConsRede;      // Energia Total Consumida da rede.
    private int porcBatRet;        // limite que se considera como bateria esgotada.
    private double limAumentoEnergiaCons; //usado no teste de reestruturacao da rede.

    private int somaModDiffAtivos;
    private boolean reestrutrarRede;
    private double energiaResAnt;
    private int contChamadaReest;


    private ArrayList<Sensor> listSensores;

    private RedeSensor rede;

    private ArrayList<Integer> nSensorAtivos;
    private ArrayList<Integer> nEstagio;

    private ArrayList<Sensor> listSensFalhosNoPer;

    public Simulacao(RedeSensor rede) {

        vEnResRede = new ArrayList<>();
        vEnConsRede = new ArrayList<>();
        vpCobertura = new ArrayList<>();

        nSensorAtivos = new ArrayList<>();
        nEstagio = new ArrayList<>();

        this.rede = rede;
        listSensores = rede.getAvailableSensors();

        porcBatRet = 10;

        this.listSensFalhosNoPer = new ArrayList<>();

        this.somaModDiffAtivos = 0;
        this.energiaResAnt = 0.0;
        this.limAumentoEnergiaCons = 0.05;
        this.reestrutrarRede = false;
        this.contChamadaReest = 0;

    }


    public int getContChamadaReest() {
        return contChamadaReest;
    }


    public ArrayList<Double> getvEnResRede() {
        return vEnResRede;
    }


    public ArrayList<Double> getvEnConsRede() {
        return vEnConsRede;
    }


    public ArrayList<Double> getVpCobertura() {
        return vpCobertura;
    }


    public void setTesteNumero(int i) {
    }


    public boolean isReestrutrarRede() {
        return reestrutrarRede;
    }

    public double getpCobAtual() {
        return rede.getPorcCobAtual();
    }

    public double getEnResRede() {
        return enResRede;
    }

    public double getEnConsRede() {
        return enConsRede;
    }

    public RedeSensor getRede() {
        return rede;
    }

    public boolean simulaUmPer(boolean eventAnt, int estagioAtual, Saidas saida) throws Exception {

        saida.geraArquivoSimulador(estagioAtual);

        boolean evento;

        listSensFalhosNoPer.clear();

        // ========= Verificacao e Calculo de Energia no Periodo de tempo =========
        enResRede = 0;
        enConsRede = 0;

        int numSensores = listSensores.size();


		/*int fimLista = vEnResRede.size()-1;
		if (!eventAnt && vEnConsRede.size() > 2 && vEnConsRede.get(fimLista) == vEnConsRede.get(fimLista-1)){		
			enResRede = vEnResRede.get(fimLista)-vEnConsRede.get(fimLista);
			enConsRede = vEnConsRede.get(fimLista);
			pCobAtual = vpCobertura.get(fimLista);
		}
		else{*/

        for (Sensor listSensore : listSensores) {
            enResRede += listSensore.getBatteryEnergy();
        }

        //Calculando a energia consumida
        enConsRede = rede.CalculaEnergiaConsPer();

        //////////////////////// necessario para algumas aplicacoes //////////////////
        if (testeReestruturarRede(estagioAtual))
            contChamadaReest++;
        ///////////////////////////////////////////////////////////////////////////////

        //Incluindo Energia consumida por Ativacao.
        enConsRede += rede.enAtivPeriodo();
        //-----------------------------------------
        pCobAtual = rede.calcCobertura();

        //}

        nSensorAtivos.add(rede.getNumSensAtivos());
        nEstagio.add(estagioAtual);

        vEnResRede.add(enResRede);
        vEnConsRede.add(enConsRede);
        vpCobertura.add(pCobAtual);

        //gerar impressao na tela
        saida.gerarSaidaTela(estagioAtual);

        rede.CalculaEnergiaPeriodo();

        //Verificando se algum sensor nao estara na proxima simulacao
        evento = rede.retirarSensoresFalhaEnergia(listSensFalhosNoPer, (double) porcBatRet);
        rede.setListSensFalhosNoPer(listSensFalhosNoPer);

        return evento;

    }

    public boolean testeReestruturarRede(int estagioAtual) {
        // TODO Auto-generated method stub
        reestrutrarRede = false;
        //testando se ira reestruturar - nao considerar EA ///////////////////////////
        if (enConsRede - energiaResAnt > limAumentoEnergiaCons * energiaResAnt) {
            energiaResAnt = enConsRede;
            if (estagioAtual > 1) {
                somaModDiffAtivos = 0;
                reestrutrarRede = true;
            }
        }
        if (estagioAtual > 0) {
            somaModDiffAtivos = Math.abs(nSensorAtivos.get(nSensorAtivos.size() - 1) - rede.getNumSensAtivos());
            if (somaModDiffAtivos > limAumentoEnergiaCons * rede.getAvailableSensors().size()) {
                somaModDiffAtivos = 0;
                reestrutrarRede = true;
            }
        }
        return reestrutrarRede;
    }


    public double calcMedCob() {

        double medCob = 0;

        int i;
        for (i = 0; i < vpCobertura.size() - 1; i++) {
            medCob = medCob + vpCobertura.get(i);
        }

        return 100 * (medCob / (double) i);
    }


}
