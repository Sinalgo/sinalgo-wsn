package projects.tcc.rssf;

import projects.tcc.algorithms.graph.Grafo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class RedeSensor {

    private ArrayList<Sensor> listSensores;
    private ArrayList<Sensor> listSensoresDisp;
    private ArrayList<Sensor> listSensoresDisp_Sink;
    private ArrayList<Sensor> listSensoresAtivos;
    private ArrayList<Sensor> listSensFalhosNoPer;
    private ArrayList<Sink> listSink;
    private int numSens;
    private int[] vetCobertura;
    private int numPontosCobertos;
    private double porcCobAtual;
    private double[][] matrizConectividade;
    private ArrayList<PontosDemanda> pontosDemanda;
    private double area;

    double fatorCob;

    public RedeSensor(String nomeArq, int largura, int comprimento, double fatorCob) throws IOException {
        listSensores = new ArrayList<>();
        listSensoresDisp_Sink = new ArrayList<>();
        listSensoresDisp = new ArrayList<>();
        listSensoresAtivos = new ArrayList<>();
        listSink = new ArrayList<>();
        setPontosDemanda(largura, comprimento);
        setSensores(nomeArq);
        constroiVetCobertura();
        numPontosCobertos = 0;
        porcCobAtual = 0.;
        area = largura * comprimento;
        this.fatorCob = fatorCob;
    }

    public void setListSensFalhosNoPer(ArrayList<Sensor> listSensFalhosNoPer) {
        this.listSensFalhosNoPer = listSensFalhosNoPer;
    }

    public double getPorcCobAtual() {
        return porcCobAtual;
    }

    public double getFatorCob() {
        return fatorCob;
    }

    public double getArea() {
        return area;
    }

    public int getNumPontosDemanda() {
        return pontosDemanda.size();
    }

    public ArrayList<Sensor> getListSensoresDisp() {
        // TODO Auto-generated method stub
        return listSensoresDisp;
    }

    public ArrayList<Sensor> getListSensores() {
        // TODO Auto-generated method stub
        return listSensores;
    }

    public int[] getVetIdsSensDisp() {
        int[] vetIds = new int[listSensoresDisp.size()];

        for (int i = 0; i < listSensoresDisp.size(); i++) {
            vetIds[i] = listSensoresDisp.get(i).getId();
        }

        return vetIds;
    }

    private void setPontosDemanda(int largura, int comprimento) {

        pontosDemanda = new ArrayList<>();

        for (int i = 0; i < largura; i++) {
            for (int j = 0; j < comprimento; j++) {
                PontosDemanda vPontosDemanda = new PontosDemanda(i + 0.5, j + 0.5);
                pontosDemanda.add(vPontosDemanda);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void setSensores(String nomeArq) throws IOException {

        //Fazer a leitura de arquivo.
        double raioSens, raioCom, energBat, potAtiv, potRec, potManut, taxaCom;

        BufferedReader input = new BufferedReader(new FileReader(nomeArq));

        String aLine;
        this.numSens = Integer.valueOf(input.readLine());
        raioSens = Double.valueOf(input.readLine());
        raioCom = Double.valueOf(input.readLine());
        energBat = Double.valueOf(input.readLine());
        potAtiv = Double.valueOf(input.readLine());
        potRec = Double.valueOf(input.readLine());
        potManut = Double.valueOf(input.readLine());
        taxaCom = Double.valueOf(input.readLine());

        for (int i = 0; i < numSens; i++) {
            aLine = input.readLine();
            String[] values = aLine.split("\t");

            int id = Integer.valueOf(values[0]);
            double x = Double.valueOf(values[1]);
            double y = Double.valueOf(values[2]);

            Sensor aux = new Sensor(id, x, y, raioSens, raioCom, energBat,
                    potAtiv, potRec, potManut, taxaCom);
            listSensores.add(aux);
        }

        listSensoresDisp_Sink = (ArrayList<Sensor>) listSensores.clone();
        listSensoresDisp = (ArrayList<Sensor>) listSensores.clone();

        input.close();

    }

    public void addSink(double x, double y) {
        int idSink = listSensores.size(); // pois sera o ultimo na lista de sensores.
        double taxaCom = listSensores.get(0).getTaxaCom();

        /*Sensor (id, posX, posY, raioComunicacao, taxaCom) */
        Sink vSink = new Sink(idSink, x, y, 25, taxaCom);

        listSensoresDisp_Sink.add(vSink);
        listSink.add(vSink);
    }

    public void addSink() {
        addSink(0., 0.);
    }


    public void prepararRede() throws Exception {

        constroiMatrizConectividade();
        criaListVizinhosRC();

    }

    public void constroiMatrizConectividade() {

        int vLinhas = listSensoresDisp_Sink.size();
        int vColunas = listSensoresDisp_Sink.size();

        matrizConectividade = alocaMatrizDouble(vLinhas, vColunas);

        for (int i = 0; i < listSensoresDisp_Sink.size(); i++) {
            double Xsensor1 = listSensoresDisp_Sink.get(i).getPosX();
            double Ysensor1 = listSensoresDisp_Sink.get(i).getPosY();

            for (int j = 0; j < listSensoresDisp_Sink.size(); j++) {
                if (i != j) {
                    double Xsensor2 = listSensoresDisp_Sink.get(j).getPosX();
                    double Ysensor2 = listSensoresDisp_Sink.get(j).getPosY();
                    double vDistancia = Math.sqrt(((Xsensor1 - Xsensor2) * (Xsensor1 - Xsensor2)) +
                            ((Ysensor1 - Ysensor2) * (Ysensor1 - Ysensor2)));
                    matrizConectividade[i][j] = vDistancia;
                } else
                    matrizConectividade[i][j] = -1;
            }
        }
    }

    private void constroiVetCobertura() {

        vetCobertura = new int[pontosDemanda.size()];

        for (Sensor sens : listSensores) {

            ArrayList<Integer> listPontosCobertos = new ArrayList<>();
            double Xsensor = sens.getPosX();
            double Ysensor = sens.getPosY();

            for (int j = 0; j < pontosDemanda.size(); j++) {

                double Xponto = pontosDemanda.get(j).getPosicaoX();
                double Yponto = pontosDemanda.get(j).getPosicaoY();

                double vDistancia = Math.sqrt(((Xsensor - Xponto) * (Xsensor - Xponto)) + ((Ysensor - Yponto) * (Ysensor - Yponto)));

                if (vDistancia <= sens.getRaioSensoriamento()) {
                    listPontosCobertos.add(j);
                }
            }
            sens.setListPontosCobertos(listPontosCobertos);

        }
    }


    private double[][] alocaMatrizDouble(int vLinhas, int vColunas) {
        // TODO Auto-generated method stub

        double[][] matriz = new double[vLinhas][];

        for (int i = 0; i < vLinhas; i++) {
            matriz[i] = new double[vColunas];
            if (matriz[i] == null) {
                System.out.println("Erro ao alocar coluna da matriz de conectividade");
                System.exit(1);
            }
        }

        return matriz;

    }

    public void criaListVizinhosRC() {
        for (int i = 0; i < listSensoresDisp_Sink.size(); i++) {
            ArrayList<Sensor> listSensVizinhos = new ArrayList<>();
            for (int j = 0; j < listSensoresDisp_Sink.size(); j++) {
                if (i != j) {
                    double vDistancia = matrizConectividade[i][j];
                    double vRaio = (float) listSensoresDisp_Sink.get(i).getRaioComunicacao();

                    if (vDistancia <= vRaio) {
                        listSensVizinhos.add(listSensoresDisp_Sink.get(j));
                    }
                }
            }
            listSensoresDisp_Sink.get(i).setListSensVizinhos(listSensVizinhos);

        }
    }

    public double CalculaEnergiaConsPer() {
        return CalculaEnergiaConsPer(listSensoresAtivos);
    }

    public double CalculaEnergiaConsPer(ArrayList<Sensor> listSens) {

        double EnergiaGastaAcum = 0;

        for (Sensor s : listSens) {
            int idSens = s.getId();
            int sensPai = s.getSensorPai().getId();
            int vNumeroFilhos = s.BuscaDescendentes();

            double ER = s.getPotRec() * vNumeroFilhos;

            double vDistanciaAoPai = matrizConectividade[idSens][sensPai];

            double ET = s.getGastoTransmissao(vDistanciaAoPai, vNumeroFilhos);
            double EM = s.getPotManut();
            double EnergiaGasta = ER + ET + EM;

            EnergiaGastaAcum = EnergiaGastaAcum + EnergiaGasta;
        }
        return EnergiaGastaAcum;
    }

    public double enAtivPeriodo() {

        double enAtivAcum = 0;

        for (Sensor aListSensoresDisp : listSensoresDisp) {
            if (aListSensoresDisp.isBitEA() && aListSensoresDisp.isAtivo()) {
                enAtivAcum += aListSensoresDisp.getPotAtiv();
                aListSensoresDisp.setBitEA(false);
            }
        }

        return enAtivAcum;
    }

    public void desligarSensoresDesconexos() {
        // TODO Auto-generated method stub
		/*for (Sensor s : listSensoresAtivos){
			s.setConex(false);
		}

		int numSensDesligados = 0;
		for (Sensor s : listSensoresDisp){
			if (s.isAtivo()){
				verificarConectividade(s);
				if (!s.isConex()){
					desligarSensor(s);
					numSensDesligados++;
				}

			}
		}*/

        int numSensDesligados = 0;
        for (Sensor s : listSensoresAtivos) {
            if (!s.isConex()) {
                desligarSensor(s);
                numSensDesligados++;
            }
        }


        if (numSensDesligados > 0)
            System.out.println("Numero de Sensores desligados por nao conectividade: " + numSensDesligados);
    }

    private void desligarSensor(Sensor s) {
        // TODO Auto-generated method stub
        s.setAtivo(false);
        atualizaCoberturaSemConec(s);
    }

    private boolean verificarConectividade(Sensor s) {
        // TODO Auto-generated method stub
        if (s.isConex())
            return true;

        if (s.getSensorPai() == null)
            return false;

        if (s.getSensorPai() instanceof Sink) {
            s.setConex(true);
            return true;
        }
        if (s.getSensorPai().isConex()) {
            s.setConex(true);
            return true;
        }
        boolean conexo = verificarConectividade(s.getSensorPai());

        if (conexo) {
            s.setConex(true);
            return true;
        } else
            return false;

    }

    public double calcCobertura() {
        // TODO Auto-generated method stub
        //desligarSensoresDesconexos();
        retirarCoberturaDesconexos();
        porcCobAtual = (double) numPontosCobertos / (double) vetCobertura.length;

        return porcCobAtual;
    }

    private void retirarCoberturaDesconexos() {
        // TODO Auto-generated method stub
        for (Sensor s : listSensoresAtivos) {
            if (!s.isConex()) {
                ArrayList<Integer> listPontosCobertos = s.getListPontosCobertos();
                for (Integer listPontosCoberto : listPontosCobertos) {
                    vetCobertura[listPontosCoberto]--;
                    if (vetCobertura[listPontosCoberto] == 0)
                        numPontosCobertos--;
                }
            }
        }
    }


    public void calcCoberturaInicial() {
        // TODO Auto-generated method stub
        vetCobertura = new int[pontosDemanda.size()];
        ;
        this.numPontosCobertos = 0;

        for (Sensor sens : listSensoresAtivos) {
            atualizaCoberturaSemConec(sens);
        }

        porcCobAtual = calcCobertura();

    }

    public int getNumSensAtivos() {
        return listSensoresAtivos.size();

    }

    public void CalculaEnergiaPeriodo() {

        for (Sensor s : listSensoresAtivos) {

            int vNumeroFilhos = s.BuscaDescendentes();
            double ER = s.getPotRec() * vNumeroFilhos;

            double vDistanciaAoPai = matrizConectividade[s.getId()][s.getSensorPai().getId()];
            double vCorrente = s.BuscaCorrente_Distancia(vDistanciaAoPai);

            double ET = s.getTaxaCom() * vCorrente * (vNumeroFilhos + 1);
            double EM = s.getPotManut();
            double EnergiaGasta = ER + ET + EM;

            s.retirarEnergGastaPeriodo(EnergiaGasta);

        }
    }

    public boolean retirarSensoresFalhaEnergia(
            ArrayList<Sensor> listSensFalhosNoPer, double porcBatRet) {
        // TODO Auto-generated method stub
        boolean falha = false;

        for (Sensor s : listSensoresAtivos) {

            double enB = s.getEnergiaOriginal();

            if (s.getEnergiaBat() <= (porcBatRet / 100.) * enB && s.isAtivo()) {

                falha = true;

                s.setFalho(true);
                s.setAtivo(false);
                s.setConex(false);
                s.desconectafilhos();
                listSensFalhosNoPer.add(s);
            } // end if
        } // end for

        for (Sensor sens : listSensFalhosNoPer) {
            this.desligarSensor(sens);
            sens.getSensorPai().getListFilhos().remove(sens);
            listSensoresAtivos.remove(sens);
            listSensoresDisp.remove(sens);
            listSensoresDisp_Sink.remove(sens);
        }

        if (falha) {
            //criarConect();
            calcCobertura();
        }

        return falha;
    }


    //funcao utilizada pelo AG
    public int avaliaNaoCoberturaSemConect(ArrayList<Integer> listIdSensAtivo) {
        // TODO Auto-generated method stub
        // metodo utilizado no AG.

        int[] vetCoberturaAux = new int[pontosDemanda.size()];

        int numPontosCobertosAux = 0;
        for (int cSensor : listIdSensAtivo) {
            numPontosCobertosAux += atualizaCoberturaSemConec(listSensores.get(cSensor), vetCoberturaAux);
        }

        return (vetCoberturaAux.length - numPontosCobertosAux);
    }

    //funcao para utilizacao no metodo avaliaNaoCoberturaSemConect(ArrayList<Integer> listIdSensAtivo)
    private int atualizaCoberturaSemConec(Sensor sensor, int[] vetCoberturaAux) {
        // TODO Auto-generated method stub
        int numPontosCobertosAux = 0;
        ArrayList<Integer> listPontosCobertos = sensor.getListPontosCobertos();

        for (Integer listPontosCoberto : listPontosCobertos) {
            if (vetCoberturaAux[listPontosCoberto] == 0) {
                numPontosCobertosAux++;
            }
            vetCoberturaAux[listPontosCoberto]++;
        }
        return numPontosCobertosAux;
    }

    private int atualizaCoberturaSemConec(Sensor sensor) {
        // TODO Auto-generated method stub
        ArrayList<Integer> listPontosCobertos = sensor.getListPontosCobertos();

        if (sensor.isAtivo()) {
            for (Integer listPontosCoberto : listPontosCobertos) {
                if (vetCobertura[listPontosCoberto] == 0)
                    numPontosCobertos++;
                vetCobertura[listPontosCoberto]++;
            }
        } else {
            for (Integer listPontosCoberto : listPontosCobertos) {
                vetCobertura[listPontosCoberto]--;
                if (vetCobertura[listPontosCoberto] == 0)
                    numPontosCobertos--;
            }
        }
        return numPontosCobertos;
    }


    public void calCustosCaminho() {

        Grafo grafoCM = new Grafo(listSensoresDisp_Sink, matrizConectividade);
        grafoCM.construirGrafo();
        grafoCM.caminhosMinimosPara(listSink.get(0));

    }


    public void ativarSensoresVetBits(boolean[] vetBoolean) {
        // TODO Auto-generated method stub

        listSensoresAtivos.clear();
        for (int i = 0; i < vetBoolean.length; i++) {
            if (vetBoolean[i]) {
                listSensoresDisp.get(i).setAtivo(true);
                listSensoresAtivos.add(listSensoresDisp.get(i));
            } else
                listSensoresDisp.get(i).setAtivo(false);
        }

    }

    public void ativarSensoresVetBits(int[] vetBoolean) {
        // TODO Auto-generated method stub

        listSensoresAtivos.clear();
        for (int i = 0; i < vetBoolean.length; i++) {
            if (vetBoolean[i] == 1) {
                listSensoresDisp.get(i).setAtivo(true);
                listSensoresAtivos.add(listSensoresDisp.get(i));
            } else
                listSensoresDisp.get(i).setAtivo(false);
        }

    }


    public void criarConect() {
        // TODO Auto-generated method stub

        //refazendo as conexoes
        for (Sensor sens : listSensoresDisp_Sink) {
            sens.reiniciarSensorParaConectividade();
        }

        Grafo grafoCM = new Grafo(listSensoresDisp_Sink, matrizConectividade);
        grafoCM.construirGrafoConect();
        grafoCM.caminhosMinimosPara(listSink.get(0));
        ativarPaisDesativados();
        geraListFilhos();
        for (Sensor s : listSensoresAtivos) {
            verificarConectividade(s);
        }

    }

    public void criarConect(int[] listSensPai) {
        // TODO Auto-generated method stub

        //refazendo as conexoes
        for (Sensor sens : listSensoresDisp) {
            sens.reiniciarSensorParaConectividade();
            int indSensPai = listSensPai[sens.getId()];
            sens.setSensorPai(listSensores.get(indSensPai));
        }

        geraListFilhos();

    }

    private void geraListFilhos() {
        // TODO Auto-generated method stub

        for (Sensor sens : listSensoresAtivos) {

            Sensor pai = sens.getSensorPai();
            if (pai != null)
                pai.adicionaFilho(sens);

        }

    }


    private void ativarPaisDesativados() {
        // TODO Auto-generated method stub
        ArrayList<Sensor> sensAtivos = new ArrayList<>();
        int numSensCox = 0;
        for (Sensor sens : listSensoresAtivos) {
            Sensor sensAux = sens;
            while (sensAux.getSensorPai() != null && !sensAux.getSensorPai().isAtivo() && !(sensAux instanceof Sink)) {
                sensAux.getSensorPai().setAtivo(true);
                sensAtivos.add(sensAux.getSensorPai());
                atualizaCoberturaSemConec(sensAux.getSensorPai());
                sensAux = sensAux.getSensorPai();
                numSensCox++;
            }
        }

        listSensoresAtivos.addAll(sensAtivos);

        if (numSensCox > 0) {
            System.out.println("Numero de Sensores Ativos na Conectividade: " + numSensCox);
        }

    }


    public boolean suprirCobertura() {
        // TODO Auto-generated method stub
        // Utilizado na vers�o OnlineH�brido

        for (Sensor s : listSensoresAtivos) {
            if (!s.isConex())
                atualizaCoberturaSemConec(s);
        }

        boolean retorno = true;

        double nPontoDemanda = vetCobertura.length;

        ArrayList<Sensor> listSensorDesconex = new ArrayList<>();

        double fatorPontoDemanda = numPontosCobertos;

        while (fatorPontoDemanda / nPontoDemanda < fatorCob) {

            Sensor sensEscolhido = escolherSensorSubstituto(listSensorDesconex);

            if (sensEscolhido != null) {
                ligaSensor(sensEscolhido);
                criarConect();

                if (sensEscolhido.getSensorPai() == null) {
                    //Impossivel conectar o sensor na rede
                    listSensorDesconex.add(sensEscolhido);
                    desligarSensor(sensEscolhido);
                    continue;
                }
                //possivel problema que pode ocorrer.
                else if (sensEscolhido.getSensorPai().isFalho()) {
                    //Impossivel conectar o sensor na rede
                    listSensorDesconex.add(sensEscolhido);
                    desligarSensor(sensEscolhido);
                    continue;
                } else {
                    System.out.println("Sensor Escolhido = " + sensEscolhido);


                    if (!(sensEscolhido.getSensorPai() instanceof Sink)) {
                        atualizarListaPontosCobExclusivo(sensEscolhido.getSensorPai());
                    }

                }
                fatorPontoDemanda = numPontosCobertos;
            } else {
                //nao ha sensores para ativar
                System.out.println("Nao ha mais sensores para ativar e suprir a cobertura");
                fatorPontoDemanda = nPontoDemanda;
                retorno = false;
            }

        }// end While


        calcCobertura();

        return retorno;
    }


    private Sensor escolherSensorSubstituto(ArrayList<Sensor> listSensorDesconex) {
        // TODO Auto-generated method stub
        Sensor sensEscolhido = null;
        int maiorNumPontCobDescob = 0;
        for (Sensor sens : listSensoresDisp) {

            if (!listSensorDesconex.contains(sens)) {
                if (!sens.isAtivo()) {
                    if (sens.isFalho()) {
                        System.out.println("Acessando Sensor Falho na lista de Sensores Dispon�veis");
                        System.out.println("suprirCoberturaSeNecessario() - RedeSensor");
                        System.exit(1);
                    }

                    int numPontCobDescob = atualizarListaPontosCobExclusivo(sens);

                    if (numPontCobDescob > maiorNumPontCobDescob) {
                        sensEscolhido = sens;
                        maiorNumPontCobDescob = numPontCobDescob;
                    }

                }
            }
        }
        return sensEscolhido;
    }

    private void ligaSensor(Sensor sensEscolhido) {
        // TODO Auto-generated method stub
        sensEscolhido.setAtivo(true);
        listSensoresAtivos.add(sensEscolhido);
        atualizaCoberturaSemConec(sensEscolhido);

    }

    private int atualizarListaPontosCobExclusivo(Sensor sens) {
        // TODO Auto-generated method stub

        ArrayList<Integer> listPontosCobExclusivo = sens.getListPontosCobExclusivo();
        listPontosCobExclusivo.clear();

        ArrayList<Integer> listPontosCob = sens.getListPontosCobertos();

        int numPontCobDescob = 0;
        for (int pont : listPontosCob) {

            if (vetCobertura[pont] == 0) {
                numPontCobDescob++;
                listPontosCobExclusivo.add(pont);
            }
        }
        sens.setListPontosCobExclusivo(listPontosCobExclusivo);

        return numPontCobDescob;
    }

    public void constroiRedeInicial(boolean[] vetBoolean) {
        // TODO Auto-generated method stub

        ativarSensoresVetBits(vetBoolean);


        // criando a conectividade inicial das redes e atualizando a cobertura.
        criarConect();
        // calculo da cobertura sem conectividade.
        calcCoberturaInicial();
        //calcCobertura();

        // ========= Verificacao se ha pontos descobertos =========
        if (porcCobAtual < fatorCob)
            suprirCobertura();

    }


    public boolean suprirOnline() {
        // TODO Auto-generated method stub

        Sensor sensorEscolhido;

        for (Sensor sensFalho : listSensFalhosNoPer) {
            sensorEscolhido = escolherSubs(sensFalho);
            if (sensorEscolhido == null)
                break;
            ligaSensor(sensorEscolhido);
            boolean fezConex = conectarSensorOnline(sensorEscolhido, sensFalho);
            if (!fezConex) {
                criarConect();
            }
        }
        calcCobertura();
        if (this.porcCobAtual >= this.fatorCob) {
            return true;
        } else {
            System.out.println("Não foi possível suprimir cobertura Online");
            return false;
        }
    }


    private boolean conectarSensorOnline(Sensor sensorEscolhido, Sensor sensFalho) {
        // TODO Auto-generated method stub

        boolean retorno = true;
        if (sensorEscolhido.getListSensVizinhos().contains(sensFalho.getSensorPai())) {
            sensorEscolhido.setSensorPai(sensFalho.getSensorPai());
            sensFalho.getSensorPai().adicionaFilho(sensorEscolhido);
            if (sensFalho.getSensorPai().isConex() || sensFalho.getSensorPai() instanceof Sink)
                sensorEscolhido.setConex(true);
        } else {
            retorno = false;
        }

        ArrayList<Sensor> listSensorReconex = new ArrayList<>();
        for (Sensor sensFilho : sensFalho.getListFilhos()) {
            if (!sensFilho.isConex())
                if (sensorEscolhido.getListSensVizinhos().contains(sensFilho)) {
                    sensorEscolhido.adicionaFilho(sensFilho);
                    sensFilho.setSensorPai(sensorEscolhido);
                    sensFilho.setConex(true);
                    listSensorReconex.add(sensFilho);
                    sensFilho.conectafilhos(listSensorReconex);
                } else
                    retorno = false;
        }

        for (Sensor sensReconex : listSensorReconex) {
            atualizaCoberturaSemConec(sensReconex);
        }

        return retorno;
    }


    private Sensor escolherSubs(Sensor sensFalho) {
        // TODO Auto-generated method stub

        Sensor sensEsc = null;

        double distQuad = Double.MAX_VALUE;


        for (Sensor sensCand : sensFalho.getListSensVizinhos()) {

            if (!sensCand.isAtivo() && !sensCand.isFalho()) {
                double distAux = 0;
                double distPai = matrizConectividade[sensCand.getId()][sensFalho.getSensorPai().getId()];

                distAux = distAux + Math.pow(distPai, 2);

                double distFilho;
                for (Sensor sensFilho : sensFalho.getListFilhos()) {
                    distFilho = matrizConectividade[sensCand.getId()][sensFilho.getId()];
                    distAux = distAux + Math.pow(distFilho, 2);
                }

                if (distAux < distQuad) {
                    distQuad = distAux;
                    sensEsc = sensCand;
                }

            }

        }

        return sensEsc;
    }


}




