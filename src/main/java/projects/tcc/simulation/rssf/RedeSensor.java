package projects.tcc.simulation.rssf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.algorithms.graph.GraphHolder;
import sinalgo.nodes.Position;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class RedeSensor {

    @Getter
    private List<Sensor> listSensores;
    @Getter
    private List<Sensor> availableSensors;
    private List<Sensor> listSensoresDisp_Sink;
    private List<Sensor> activeSensors;
    private List<Sensor> listSensFalhosNoPer;
    private Sink sink;
    private int[] coverageMatrix;
    private int numPontosCobertos;

    @Getter
    private double porcCobAtual;
    private double[][] connectivityMatrix;
    private Position[] pontosDemanda;

    @Getter
    private double area;

    @Getter
    private double fatorCob;

    public RedeSensor(String nomeArq, int largura, int comprimento, double fatorCob) throws IOException {
        listSensores = new ArrayList<>();
        listSensoresDisp_Sink = new ArrayList<>();
        availableSensors = new ArrayList<>();
        activeSensors = new ArrayList<>();
        setPontosDemanda(largura, comprimento);
        constroiVetCobertura();
        numPontosCobertos = 0;
        porcCobAtual = 0.;
        area = largura * comprimento;
        this.fatorCob = fatorCob;
    }

    public void setListSensFalhosNoPer(List<Sensor> listSensFalhosNoPer) {
        this.listSensFalhosNoPer = listSensFalhosNoPer;
    }

    public int getNumPontosDemanda() {
        return pontosDemanda.length;
    }

    public long[] getVetIdsSensDisp() {
        long[] vetIds = new long[availableSensors.size()];
        for (int i = 0; i < availableSensors.size(); i++) {
            vetIds[i] = availableSensors.get(i).getID();
        }
        return vetIds;
    }

    private void setPontosDemanda(int largura, int comprimento) {
        this.pontosDemanda = new Position[largura * comprimento];
        for (int i = 0; i < largura; i++) {
            for (int j = 0; j < comprimento; j++) {
                pontosDemanda[i * largura + j] = new Position(i + 0.5, j + 0.5, 0);
            }
        }
    }

    public void addSink() {
        this.sink = new Sink();
        listSensoresDisp_Sink.add(sink);
    }

    public void prepararRede() throws Exception {
        constroiMatrizConectividade();
        criaListVizinhosRC();
    }

    public void constroiMatrizConectividade() {

        int vLinhas = listSensoresDisp_Sink.size();
        int vColunas = listSensoresDisp_Sink.size();

        this.connectivityMatrix = new double[vLinhas][vColunas];

        for (int i = 0; i < listSensoresDisp_Sink.size(); i++) {
            for (int j = 0; j < listSensoresDisp_Sink.size(); j++) {
                if (i != j) {
                    double vDistancia = listSensoresDisp_Sink.get(i).getPosition().distanceTo(listSensoresDisp_Sink.get(j).getPosition());
                    connectivityMatrix[i][j] = vDistancia;
                } else
                    connectivityMatrix[i][j] = -1;
            }
        }
    }

    private void constroiVetCobertura() {
        this.coverageMatrix = new int[this.pontosDemanda.length];
        for (Sensor sens : listSensores) {
            Set<Integer> listPontosCobertos = sens.getCoveredPoints();
            listPontosCobertos.clear();
            for (int j = 0; j < this.pontosDemanda.length; j++) {
                double vDistancia = sens.getPosition().distanceTo(this.pontosDemanda[j]);
                if (Double.compare(vDistancia, sens.getSensorRadius()) <= 0) {
                    listPontosCobertos.add(j);
                }
            }

        }
    }

    public void criaListVizinhosRC() {
        for (int i = 0; i < listSensoresDisp_Sink.size(); i++) {
            List<Sensor> listSensVizinhos = listSensoresDisp_Sink.get(i).getNeighbors();
            listSensVizinhos.clear();
            for (int j = 0; j < listSensoresDisp_Sink.size(); j++) {
                if (i != j) {
                    double vDistancia = connectivityMatrix[i][j];
                    double vRaio = (float) listSensoresDisp_Sink.get(i).getCommRadius();

                    if (vDistancia <= vRaio) {
                        listSensVizinhos.add(listSensoresDisp_Sink.get(j));
                    }
                }
            }

        }
    }

    public double CalculaEnergiaConsPer() {
        return CalculaEnergiaConsPer(activeSensors);
    }

    public double CalculaEnergiaConsPer(List<Sensor> listSens) {

        double EnergiaGastaAcum = 0;

        for (Sensor s : listSens) {
            long idSens = s.getID();
            long sensPai = s.getParent().getID();
            long vNumeroFilhos = s.queryDescendants();

            double ER = s.getReceivePower() * vNumeroFilhos;

            double vDistanciaAoPai = connectivityMatrix[(int) idSens][(int) sensPai];

            double ET = s.getEnergySpentInTransmission(vDistanciaAoPai, vNumeroFilhos);
            double EM = s.getMaintenancePower();
            double EnergiaGasta = ER + ET + EM;

            EnergiaGastaAcum = EnergiaGastaAcum + EnergiaGasta;
        }
        return EnergiaGastaAcum;
    }

    public double enAtivPeriodo() {

        double enAtivAcum = 0;

        for (Sensor aListSensoresDisp : availableSensors) {
            if (aListSensoresDisp.isBitEA() && aListSensoresDisp.isActive()) {
                enAtivAcum += aListSensoresDisp.getActivationPower();
                aListSensoresDisp.setBitEA(false);
            }
        }

        return enAtivAcum;
    }

    public void desligarSensoresDesconexos() {
		/*for (Sensor s : activeSensors){
			s.setConnected(false);
		}

		int numSensDesligados = 0;
		for (Sensor s : availableSensors){
			if (s.isActive()){
				verificarConectividade(s);
				if (!s.isConnected()){
					desligarSensor(s);
					numSensDesligados++;
				}

			}
		}*/

        int numSensDesligados = 0;
        for (Sensor s : activeSensors) {
            if (!s.isConnected()) {
                desligarSensor(s);
                numSensDesligados++;
            }
        }


        if (numSensDesligados > 0)
            System.out.println("Numero de Sensores desligados por nao conectividade: " + numSensDesligados);
    }

    private void desligarSensor(Sensor s) {
        s.setActive(false);
        this.atualizaCoberturaSemConec(s);
    }

    private boolean verificarConectividade(Sensor s) {
        if (s.isConnected())
            return true;

        if (s.getParent() == null)
            return false;

        if (s.getParent() instanceof Sink) {
            s.setConnected(true);
            return true;
        }
        if (s.getParent().isConnected()) {
            s.setConnected(true);
            return true;
        }
        boolean conexo = this.verificarConectividade(s.getParent());

        if (conexo) {
            s.setConnected(true);
            return true;
        } else
            return false;

    }

    public double calcCobertura() {
        //desligarSensoresDesconexos();
        this.retirarCoberturaDesconexos();
        porcCobAtual = (double) numPontosCobertos / (double) coverageMatrix.length;

        return porcCobAtual;
    }

    private void retirarCoberturaDesconexos() {
        for (Sensor s : activeSensors) {
            if (!s.isConnected()) {
                Set<Integer> listPontosCobertos = s.getCoveredPoints();
                for (Integer listPontosCoberto : listPontosCobertos) {
                    coverageMatrix[listPontosCoberto]--;
                    if (coverageMatrix[listPontosCoberto] == 0)
                        numPontosCobertos--;
                }
            }
        }
    }


    public void calcCoberturaInicial() {
        this.coverageMatrix = new int[pontosDemanda.length];
        this.numPontosCobertos = 0;
        for (Sensor sens : activeSensors) {
            atualizaCoberturaSemConec(sens);
        }
        this.porcCobAtual = calcCobertura();
    }

    public int getNumSensAtivos() {
        return activeSensors.size();
    }

    public void CalculaEnergiaPeriodo() {

        for (Sensor s : activeSensors) {

            long vNumeroFilhos = s.queryDescendants();
            double ER = s.getReceivePower() * vNumeroFilhos;

            double vDistanciaAoPai = connectivityMatrix[(int) s.getID()][(int) s.getParent().getID()];
            double vCorrente = s.queryDistances(vDistanciaAoPai);

            double ET = s.getCommRatio() * vCorrente * (vNumeroFilhos + 1);
            double EM = s.getMaintenancePower();
            double EnergiaGasta = ER + ET + EM;

            s.subtractEnergySpent(EnergiaGasta);

        }
    }

    public boolean retirarSensoresFalhaEnergia(
            List<Sensor> listSensFalhosNoPer, double porcBatRet) {
        boolean falha = false;

        for (Sensor s : activeSensors) {

            double enB = s.getOriginalEnergy();

            if (s.getBatteryEnergy() <= (porcBatRet / 100.) * enB && s.isActive()) {

                falha = true;

                s.setFailed(true);
                s.setActive(false);
                s.setConnected(false);
                s.disconnectChildren();
                listSensFalhosNoPer.add(s);
            } // end if
        } // end for

        for (Sensor sens : listSensFalhosNoPer) {
            this.desligarSensor(sens);
            sens.getParent().getChildren().remove(sens);
            activeSensors.remove(sens);
            availableSensors.remove(sens);
        }

        if (falha) {
            //createConnection();
            calcCobertura();
        }

        return falha;
    }


    //funcao utilizada pelo AG
    public int avaliaNaoCoberturaSemConect(List<Long> listIdSensAtivo) {
        // metodo utilizado no AG.

        int[] vetCoberturaAux = new int[this.pontosDemanda.length];

        int numPontosCobertosAux = 0;
        for (long cSensor : listIdSensAtivo) {
            numPontosCobertosAux += this.atualizaCoberturaSemConec(listSensores.get((int) cSensor), vetCoberturaAux);
        }

        return (vetCoberturaAux.length - numPontosCobertosAux);
    }

    //funcao para utilizacao no metodo avaliaNaoCoberturaSemConect(ArrayList<Integer> listIdSensAtivo)
    private int atualizaCoberturaSemConec(Sensor sensor, int[] vetCoberturaAux) {
        int numPontosCobertosAux = 0;
        Set<Integer> listPontosCobertos = sensor.getCoveredPoints();

        for (Integer listPontosCoberto : listPontosCobertos) {
            if (vetCoberturaAux[listPontosCoberto] == 0) {
                numPontosCobertosAux++;
            }
            vetCoberturaAux[listPontosCoberto]++;
        }
        return numPontosCobertosAux;
    }

    private int atualizaCoberturaSemConec(Sensor sensor) {
        Set<Integer> listPontosCobertos = sensor.getCoveredPoints();

        if (sensor.isActive()) {
            for (Integer listPontosCoberto : listPontosCobertos) {
                if (coverageMatrix[listPontosCoberto] == 0)
                    numPontosCobertos++;
                coverageMatrix[listPontosCoberto]++;
            }
        } else {
            for (Integer listPontosCoberto : listPontosCobertos) {
                coverageMatrix[listPontosCoberto]--;
                if (coverageMatrix[listPontosCoberto] == 0)
                    numPontosCobertos--;
            }
        }
        return numPontosCobertos;
    }

    public void ativarSensoresVetBits(boolean[] vetBoolean) {

        activeSensors.clear();
        for (int i = 0; i < vetBoolean.length; i++) {
            if (vetBoolean[i]) {
                availableSensors.get(i).setActive(true);
                activeSensors.add(availableSensors.get(i));
            } else
                availableSensors.get(i).setActive(false);
        }

    }

    public void ativarSensoresVetBits(int[] vetBoolean) {

        activeSensors.clear();
        for (int i = 0; i < vetBoolean.length; i++) {
            if (vetBoolean[i] == 1) {
                availableSensors.get(i).setActive(true);
                activeSensors.add(availableSensors.get(i));
            } else
                availableSensors.get(i).setActive(false);
        }

    }


    public void createConnection() {
        //refazendo as conexoes
        for (Sensor sens : listSensoresDisp_Sink) {
            sens.resetConnectivity();
        }

        GraphHolder grafoCM = new GraphHolder(listSensoresDisp_Sink, sink, connectivityMatrix);
        grafoCM.update();
        reactivateParents();
        fillChildrenList();
        for (Sensor s : activeSensors) {
            verificarConectividade(s);
        }

    }

    public void createConnection(int[] parentSensors) {
        //refazendo as conexoes
        for (Sensor sensor : this.availableSensors) {
            sensor.resetConnectivity();
            sensor.addConnectionTo(listSensores.get(parentSensors[(int) sensor.getID()]));
        }
        fillChildrenList();
    }

    private void fillChildrenList() {
        for (Sensor sensor : this.activeSensors) {
            Sensor parent = sensor.getParent();
            if (parent != null)
                parent.addChild(sensor);
        }
    }

    private void reactivateParents() {
        List<Sensor> newActiveSensors = new ArrayList<>();
        int numSensCox = 0;
        for (Sensor sens : activeSensors) {
            Sensor sensAux = sens;
            while (sensAux.getParent() != null && !sensAux.getParent().isActive() && !(sensAux instanceof Sink)) {
                sensAux.getParent().setActive(true);
                newActiveSensors.add(sensAux.getParent());
                atualizaCoberturaSemConec(sensAux.getParent());
                sensAux = sensAux.getParent();
                numSensCox++;
            }
        }

        activeSensors.addAll(newActiveSensors);

        if (numSensCox > 0) {
            System.out.println("Numero de Sensores Ativos na Conectividade: " + numSensCox);
        }

    }


    public boolean suprirCobertura() {
        // Utilizado na vers�o OnlineH�brido

        for (Sensor s : activeSensors) {
            if (!s.isConnected())
                atualizaCoberturaSemConec(s);
        }

        boolean retorno = true;

        double nPontoDemanda = coverageMatrix.length;

        ArrayList<Sensor> listSensorDesconex = new ArrayList<>();

        double fatorPontoDemanda = numPontosCobertos;

        while (fatorPontoDemanda / nPontoDemanda < fatorCob) {

            Sensor sensEscolhido = escolherSensorSubstituto(listSensorDesconex);

            if (sensEscolhido != null) {
                ligaSensor(sensEscolhido);
                createConnection();

                if (sensEscolhido.getParent() == null) {
                    //Impossivel conectar o sensor na rede
                    listSensorDesconex.add(sensEscolhido);
                    desligarSensor(sensEscolhido);
                    continue;
                }
                //possivel problema que pode ocorrer.
                else if (sensEscolhido.getParent().isFailed()) {
                    //Impossivel conectar o sensor na rede
                    listSensorDesconex.add(sensEscolhido);
                    desligarSensor(sensEscolhido);
                    continue;
                } else {
                    System.out.println("Sensor Escolhido = " + sensEscolhido);


                    if (!(sensEscolhido.getParent() instanceof Sink)) {
                        atualizarListaPontosCobExclusivo(sensEscolhido.getParent());
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
        Sensor sensEscolhido = null;
        int maiorNumPontCobDescob = 0;
        for (Sensor sens : availableSensors) {

            if (!listSensorDesconex.contains(sens)) {
                if (!sens.isActive()) {
                    if (sens.isFailed()) {
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
        sensEscolhido.setActive(true);
        activeSensors.add(sensEscolhido);
        atualizaCoberturaSemConec(sensEscolhido);

    }

    private int atualizarListaPontosCobExclusivo(Sensor sens) {
        Set<Integer> exclusivelyCoveredPoints = sens.getExclusivelyCoveredPoints();
        Set<Integer> coveredPoints = sens.getCoveredPoints();

        exclusivelyCoveredPoints.clear();
        int uncoveredPointsCount = 0;
        for (int point : coveredPoints) {
            if (coverageMatrix[point] == 0) {
                uncoveredPointsCount++;
                exclusivelyCoveredPoints.add(point);
            }
        }

        return uncoveredPointsCount;
    }

    public void createInitialNetwork(boolean[] vetBoolean) {
        ativarSensoresVetBits(vetBoolean);

        // criando a conectividade inicial das redes e atualizando a cobertura.
        createConnection();
        // calculo da cobertura sem conectividade.
        calcCoberturaInicial();
        //calcCobertura();

        // ========= Verificacao se ha pontos descobertos =========
        if (porcCobAtual < fatorCob)
            suprirCobertura();

    }


    public boolean suprirOnline() {
        for (Sensor sensFalho : listSensFalhosNoPer) {
            Sensor sensorEscolhido = escolherSubs(sensFalho);
            if (sensorEscolhido == null)
                break;
            ligaSensor(sensorEscolhido);
            boolean fezConex = connectOnlineSensor(sensorEscolhido, sensFalho);
            if (!fezConex) {
                createConnection();
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


    private boolean connectOnlineSensor(Sensor sensorEscolhido, Sensor sensFalho) {
        boolean result = true;
        if (sensorEscolhido.getNeighbors().contains(sensFalho.getParent())) {
            sensorEscolhido.addConnectionTo(sensFalho.getParent());
            sensFalho.getParent().addChild(sensorEscolhido);
            if (sensFalho.getParent().isConnected() || sensFalho.getParent() instanceof Sink)
                sensorEscolhido.setConnected(true);
        } else {
            result = false;
        }

        List<Sensor> listSensorReconex = new ArrayList<>();
        for (Sensor sensFilho : sensFalho.getChildren()) {
            if (!sensFilho.isConnected())
                if (sensorEscolhido.getNeighbors().contains(sensFilho)) {
                    sensorEscolhido.addChild(sensFilho);
                    sensFilho.addConnectionTo(sensorEscolhido);
                    sensFilho.setConnected(true);
                    listSensorReconex.addAll(sensFilho.connectChildren());
                } else
                    result = false;
        }

        for (Sensor sensReconex : listSensorReconex) {
            atualizaCoberturaSemConec(sensReconex);
        }

        return result;
    }


    private Sensor escolherSubs(Sensor sensFalho) {
        Sensor sensEsc = null;
        double squaredDist = Double.MAX_VALUE;
        for (Sensor candidate : sensFalho.getNeighbors()) {
            if (!candidate.isActive() && !candidate.isFailed()) {
                double auxDist = 0;
                double parentDist = connectivityMatrix[(int) candidate.getID()][(int) sensFalho.getParent().getID()];

                auxDist = auxDist + Math.pow(parentDist, 2);

                for (Sensor sensFilho : sensFalho.getChildren()) {
                    double distFilho = connectivityMatrix[(int) candidate.getID()][(int) sensFilho.getID()];
                    auxDist = auxDist + Math.pow(distFilho, 2);
                }

                if (Double.compare(auxDist, squaredDist) < 0) {
                    squaredDist = auxDist;
                    sensEsc = candidate;
                }

            }

        }

        return sensEsc;
    }


}




