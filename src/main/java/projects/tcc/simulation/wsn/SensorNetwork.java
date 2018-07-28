package projects.tcc.simulation.wsn;

import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.algorithms.graph.Grafo;
import projects.tcc.simulation.wsn.data.WSNSensor;
import projects.tcc.simulation.wsn.data.WSNSink;
import sinalgo.nodes.Position;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class SensorNetwork {

    private List<WSNSensor> sensors;
    private List<WSNSensor> availableSensors;
    private List<WSNSensor> availableSensorsAndSinks;
    private List<WSNSensor> activeSensors;
    private List<WSNSensor> periodFailedSensors;
    private List<WSNSink> sinks;
    private int[] coverageArray;
    private int numCoveredPoints;
    private double currentCoveragePercent;
    private double[][] conectivityMatrix;
    private List<Position> demandPoints;
    private double area;

    private double coverageFactor;

    public SensorNetwork(String nomeArq, int largura, int comprimento, double coverageFactor) throws IOException {
        this.sensors = new ArrayList<>();
        this.availableSensorsAndSinks = new ArrayList<>();
        this.availableSensors = new ArrayList<>();
        this.activeSensors = new ArrayList<>();
        this.sinks = new ArrayList<>();
        this.setDemandPoints(largura, comprimento);
        this.setSensores(nomeArq);
        this.constroiVetCobertura();
        this.numCoveredPoints = 0;
        this.currentCoveragePercent = 0.;
        this.area = largura * comprimento;
        this.coverageFactor = coverageFactor;
    }

    public int getNumPontosDemanda() {
        return this.demandPoints.size();
    }

    public int[] getVetIdsSensDisp() {
        return this.availableSensors.stream().mapToInt(WSNSensor::getId).toArray();
    }

    private void setDemandPoints(int width, int lenght) {
        this.demandPoints = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < lenght; j++) {
                this.demandPoints.add(new Position(i + 0.5, j + 0.5, 0));
            }
        }
    }

    private void setSensores(String fileName) throws IOException {
        //Fazer a leitura de arquivo.
        BufferedReader input = new BufferedReader(new FileReader(fileName));
        int numberOfSensors = Integer.parseInt(input.readLine());
        double sensRadius = Double.parseDouble(input.readLine());
        double commRadius = Double.parseDouble(input.readLine());
        double batteryEnergy = Double.parseDouble(input.readLine());
        double activationPower = Double.parseDouble(input.readLine());
        double receivePower = Double.parseDouble(input.readLine());
        double maintenancePower = Double.parseDouble(input.readLine());
        double commRatio = Double.parseDouble(input.readLine());

        for (int i = 0; i < numberOfSensors; i++) {
            String aLine = input.readLine();
            String[] values = aLine.split("\\s+");
            int id = Integer.parseInt(values[0]);
            double x = Double.parseDouble(values[1]);
            double y = Double.parseDouble(values[2]);
            WSNSensor aux = new WSNSensor(id, x, y, sensRadius, commRadius, batteryEnergy,
                    activationPower, receivePower, maintenancePower, commRatio);
            this.sensors.add(aux);
        }

        this.availableSensorsAndSinks = new ArrayList<>(this.sensors);
        this.availableSensors = new ArrayList<>(this.sensors);

        input.close();
    }

    private void addSink(double x, double y) {
        int idSink = this.sensors.size(); // pois sera o ultimo na lista de sensores.
        double taxaCom = this.sensors.get(0).getCommRatio();

        WSNSink vSink = new WSNSink(idSink, x, y, 25, taxaCom);

        this.availableSensorsAndSinks.add(vSink);
        this.sinks.add(vSink);
    }

    public void addSink() {
        this.addSink(0, 0);
    }

    public void prepararRede() {
        this.constroiMatrizConectividade();
        this.criaListVizinhosRC();
    }

    private void constroiMatrizConectividade() {
        int numSensoresDisp_Sink = this.availableSensorsAndSinks.size();
        this.conectivityMatrix = new double[numSensoresDisp_Sink][numSensoresDisp_Sink];
        for (WSNSensor sensor1 : this.availableSensorsAndSinks) {
            for (WSNSensor sensor2 : this.availableSensorsAndSinks) {
                if (!sensor1.equals(sensor2)) {
                    double vDistancia = sensor1.getPosition().distanceTo(sensor2.getPosition());
                    this.conectivityMatrix[sensor1.getId()][sensor2.getId()] = vDistancia;
                } else {
                    this.conectivityMatrix[sensor1.getId()][sensor2.getId()] = -1;
                }
            }
        }
    }

    private void constroiVetCobertura() {
        this.coverageArray = new int[this.demandPoints.size()];
        for (WSNSensor sens : this.sensors) {
            List<Integer> listPontosCobertos = new ArrayList<>();
            for (int j = 0; j < this.demandPoints.size(); j++) {
                Position pontoDemanda = this.demandPoints.get(j);
                double vDistancia = sens.getPosition().distanceTo(pontoDemanda);
                if (vDistancia <= sens.getSensRadius()) {
                    listPontosCobertos.add(j);
                }
            }
            sens.setCoveredPoints(listPontosCobertos);
        }
    }

    private void criaListVizinhosRC() {
        for (WSNSensor sensor1 : this.availableSensorsAndSinks) {
            List<WSNSensor> listSensVizinhos = new ArrayList<>();
            for (WSNSensor sensor2 : this.availableSensorsAndSinks) {
                if (!sensor1.equals(sensor2)) {
                    double vDistancia = this.conectivityMatrix[sensor1.getId()][sensor2.getId()];
                    double vRaio = (float) sensor1.getCommRadius();
                    if (vDistancia <= vRaio) {
                        listSensVizinhos.add(sensor2);
                    }
                }
            }
            sensor1.setNeighborhood(listSensVizinhos);
        }
    }

    public double calculaEnergiaConsPer() {
        return this.calculaEnergiaConsPer(this.activeSensors);
    }

    private double calculaEnergiaConsPer(List<WSNSensor> listSens) {
        double energiaGastaAcum = 0;
        for (WSNSensor s : listSens) {
            int idSens = s.getId();
            int sensPai = s.getParent().getId();
            int vNumeroFilhos = s.queryDescendants();
            double enRec = s.getReceivePower() * vNumeroFilhos;
            double vDistanciaAoPai = this.conectivityMatrix[idSens][sensPai];
            double enTrans = s.getPowerToTransmit(vDistanciaAoPai, vNumeroFilhos);
            double enManut = s.getMaintenancePower();
            double energiaGasta = enRec + enTrans + enManut;

            energiaGastaAcum += energiaGasta;
        }
        return energiaGastaAcum;
    }

    public double enAtivPeriodo() {
        double enAtivAcum = 0;
        for (WSNSensor aListSensoresDisp : this.availableSensors) {
            if (aListSensoresDisp.isUseActivationPower() && aListSensoresDisp.isActive()) {
                enAtivAcum += aListSensoresDisp.getActivationPower();
                aListSensoresDisp.setUseActivationPower(false);
            }
        }
        return enAtivAcum;
    }

    public void desligarSensoresDesconexos() {
        int numSensDesligados = 0;
        for (WSNSensor s : this.activeSensors) {
            if (!s.isConnected()) {
                this.desligarSensor(s);
                numSensDesligados++;
            }
        }
        if (numSensDesligados > 0) {
            System.out.println("Numero de Sensores desligados por nao conectividade: " + numSensDesligados);
        }
    }

    private void desligarSensor(WSNSensor s) {
        s.setActive(false);
        this.atualizaCoberturaSemConec(s);
    }

    private boolean verificarConectividade(WSNSensor s) {
        if (s.isConnected()) {
            return true;
        }
        if (s.getParent() == null) {
            return false;
        }
        if (s.getParent() instanceof WSNSink || s.getParent().isConnected()) {
            s.setConnected(true);
            return true;
        }
        boolean conexo = this.verificarConectividade(s.getParent());
        if (conexo) {
            s.setConnected(true);
        }
        return conexo;
    }

    public double calcCobertura() {
        this.retirarCoberturaDesconexos();
        this.currentCoveragePercent = (double) this.numCoveredPoints / (double) this.coverageArray.length;
        return this.currentCoveragePercent;
    }

    private void retirarCoberturaDesconexos() {
        for (WSNSensor s : this.activeSensors) {
            if (!s.isConnected()) {
                List<Integer> listPontosCobertos = s.getCoveredPoints();
                for (Integer listPontosCoberto : listPontosCobertos) {
                    this.coverageArray[listPontosCoberto]--;
                    if (this.coverageArray[listPontosCoberto] == 0) {
                        this.numCoveredPoints--;
                    }
                }
            }
        }
    }

    private void calcCoberturaInicial() {
        this.coverageArray = new int[this.demandPoints.size()];
        this.numCoveredPoints = 0;
        for (WSNSensor sens : this.activeSensors) {
            this.atualizaCoberturaSemConec(sens);
        }
        this.currentCoveragePercent = this.calcCobertura();

    }

    public int getNumSensAtivos() {
        return this.activeSensors.size();
    }

    public void calculaEnergiaPeriodo() {
        for (WSNSensor s : this.activeSensors) {
            int vNumeroFilhos = s.queryDescendants();
            double ER = s.getReceivePower() * vNumeroFilhos;

            double vDistanciaAoPai = this.conectivityMatrix[s.getId()][s.getParent().getId()];
            double vCorrente = WSNSensor.getCurrentPerDistance(vDistanciaAoPai);

            double ET = s.getCommRatio() * vCorrente * (vNumeroFilhos + 1);
            double EM = s.getMaintenancePower();
            double EnergiaGasta = ER + ET + EM;

            s.drawEnergySpent(EnergiaGasta);
        }
    }

    public boolean retirarSensoresFalhaEnergia(List<WSNSensor> listSensFalhosNoPer, double porcBatRet) {
        boolean falha = false;
        for (WSNSensor s : this.activeSensors) {
            double enB = s.getBatteryCapacity();
            if (s.getBatteryEnergy() <= (porcBatRet / 100.) * enB && s.isActive()) {
                falha = true;
                s.setFailed(true);
                s.setActive(false);
                s.setConnected(false);
                s.disconnectChildren();
                listSensFalhosNoPer.add(s);
            }
        }
        for (WSNSensor sens : listSensFalhosNoPer) {
            this.desligarSensor(sens);
            sens.getParent().getChildren().remove(sens);
            this.activeSensors.remove(sens);
            this.availableSensors.remove(sens);
            this.availableSensorsAndSinks.remove(sens);
        }
        if (falha) {
            this.calcCobertura();
        }
        return falha;
    }

    //funcao utilizada pelo AG
    public int avaliaNaoCoberturaSemConect(List<Integer> listIdSensAtivo) {
        int[] vetCoberturaAux = new int[this.demandPoints.size()];
        int numPontosCobertosAux = 0;
        for (int cSensor : listIdSensAtivo) {
            numPontosCobertosAux += this.atualizaCoberturaSemConec(this.sensors.get(cSensor), vetCoberturaAux);
        }
        return (vetCoberturaAux.length - numPontosCobertosAux);
    }

    //funcao para utilizacao no metodo avaliaNaoCoberturaSemConect(List<Integer> listIdSensAtivo)
    private int atualizaCoberturaSemConec(WSNSensor sensor, int[] vetCoberturaAux) {
        int numPontosCobertosAux = 0;
        List<Integer> listPontosCobertos = sensor.getCoveredPoints();
        for (Integer listPontosCoberto : listPontosCobertos) {
            if (vetCoberturaAux[listPontosCoberto] == 0) {
                numPontosCobertosAux++;
            }
            vetCoberturaAux[listPontosCoberto]++;
        }
        return numPontosCobertosAux;
    }

    private int atualizaCoberturaSemConec(WSNSensor sensor) {
        List<Integer> listPontosCobertos = sensor.getCoveredPoints();
        if (sensor.isActive()) {
            for (Integer listPontosCoberto : listPontosCobertos) {
                if (this.coverageArray[listPontosCoberto] == 0) {
                    this.numCoveredPoints++;
                }
                this.coverageArray[listPontosCoberto]++;
            }
        } else {
            for (Integer listPontosCoberto : listPontosCobertos) {
                this.coverageArray[listPontosCoberto]--;
                if (this.coverageArray[listPontosCoberto] == 0) {
                    this.numCoveredPoints--;
                }
            }
        }
        return this.numCoveredPoints;
    }

    public void calCustosCaminho() {
        Grafo grafoCM = new Grafo(this.availableSensorsAndSinks, this.conectivityMatrix);
        grafoCM.construirGrafo();
        grafoCM.caminhosMinimosPara(this.sinks.get(0));
    }

    public void ativarSensoresVetBits(boolean[] vetBoolean) {
        this.activeSensors.clear();
        for (int i = 0; i < vetBoolean.length; i++) {
            if (vetBoolean[i]) {
                this.availableSensors.get(i).setActive(true);
                this.activeSensors.add(this.availableSensors.get(i));
            } else {
                this.availableSensors.get(i).setActive(false);
            }
        }
    }

    private void criarConect() {
        //refazendo as conexoes
        for (WSNSensor sens : this.availableSensorsAndSinks) {
            sens.reiniciarSensorParaConectividade();
        }
        Grafo grafoCM = new Grafo(this.availableSensorsAndSinks, this.conectivityMatrix);
        grafoCM.construirGrafoConect();
        grafoCM.caminhosMinimosPara(this.sinks.get(0));
        this.ativarPaisDesativados();
        this.geraListFilhos();
        for (WSNSensor s : this.activeSensors) {
            this.verificarConectividade(s);
        }
    }

    private void geraListFilhos() {
        for (WSNSensor sens : this.activeSensors) {
            WSNSensor pai = sens.getParent();
            if (pai != null) {
                pai.adicionaFilho(sens);
            }
        }
    }

    private void ativarPaisDesativados() {
        List<WSNSensor> sensAtivos = new ArrayList<>();
        int numSensCox = 0;
        for (WSNSensor sens : this.activeSensors) {
            WSNSensor sensAux = sens;
            while (sensAux.getParent() != null && !sensAux.getParent().isActive() && !(sensAux instanceof WSNSink)) {
                sensAux.getParent().setActive(true);
                sensAtivos.add(sensAux.getParent());
                this.atualizaCoberturaSemConec(sensAux.getParent());
                sensAux = sensAux.getParent();
                numSensCox++;
            }
        }
        this.activeSensors.addAll(sensAtivos);
        if (numSensCox > 0) {
            System.out.println("Numero de Sensores Ativos na Conectividade: " + numSensCox);
        }
    }

    public boolean suprirCobertura() {
        // Utilizado na versão OnlineHíbrido
        for (WSNSensor s : this.activeSensors) {
            if (!s.isConnected()) {
                this.atualizaCoberturaSemConec(s);
            }
        }
        boolean retorno = true;
        double nPontoDemanda = this.coverageArray.length;
        List<WSNSensor> listSensorDesconex = new ArrayList<>();
        double fatorPontoDemanda = this.numCoveredPoints;
        while (fatorPontoDemanda / nPontoDemanda < this.coverageFactor) {
            WSNSensor sensEscolhido = this.escolherSensorSubstituto(listSensorDesconex);
            if (sensEscolhido != null) {
                this.ligaSensor(sensEscolhido);
                this.criarConect();

                if (sensEscolhido.getParent() == null) {
                    //Impossivel conectar o sensor na rede
                    listSensorDesconex.add(sensEscolhido);
                    this.desligarSensor(sensEscolhido);
                    continue;
                }
                //possivel problema que pode ocorrer.
                else if (sensEscolhido.getParent().isFailed()) {
                    //Impossivel conectar o sensor na rede
                    listSensorDesconex.add(sensEscolhido);
                    this.desligarSensor(sensEscolhido);
                    continue;
                } else {
                    System.out.println("WSNSensor Escolhido = " + sensEscolhido);
                    if (!(sensEscolhido.getParent() instanceof WSNSink)) {
                        this.atualizarListaPontosCobExclusivo(sensEscolhido.getParent());
                    }
                }
                fatorPontoDemanda = this.numCoveredPoints;
            } else {
                //nao ha sensores para ativar
                System.out.println("Nao ha mais sensores para ativar e suprir a cobertura");
                fatorPontoDemanda = nPontoDemanda;
                retorno = false;
            }

        }
        this.calcCobertura();
        return retorno;
    }

    private WSNSensor escolherSensorSubstituto(List<WSNSensor> listSensorDesconex) {
        WSNSensor sensEscolhido = null;
        int maiorNumPontCobDescob = 0;
        for (WSNSensor sens : this.availableSensors) {
            if (!listSensorDesconex.contains(sens)) {
                if (!sens.isActive()) {
                    if (sens.isFailed()) {
                        System.out.println("Acessando WSNSensor Falho na lista de Sensores Disponíveis");
                        System.out.println("suprirCoberturaSeNecessario() - RedeSensor");
                        System.exit(1);
                    }
                    int numPontCobDescob = this.atualizarListaPontosCobExclusivo(sens);
                    if (numPontCobDescob > maiorNumPontCobDescob) {
                        sensEscolhido = sens;
                        maiorNumPontCobDescob = numPontCobDescob;
                    }
                }
            }
        }
        return sensEscolhido;
    }

    private void ligaSensor(WSNSensor sensEscolhido) {
        sensEscolhido.setActive(true);
        this.activeSensors.add(sensEscolhido);
        this.atualizaCoberturaSemConec(sensEscolhido);
    }

    private int atualizarListaPontosCobExclusivo(WSNSensor sens) {
        sens.getExclusivelyCoveredPoints().clear();
        int numPontCobDescob = 0;
        for (int pont : sens.getCoveredPoints()) {
            if (this.coverageArray[pont] == 0) {
                numPontCobDescob++;
                sens.getExclusivelyCoveredPoints().add(pont);
            }
        }
        return numPontCobDescob;
    }

    public void constroiRedeInicial(boolean[] vetBoolean) {
        this.ativarSensoresVetBits(vetBoolean);

        // criando a conectividade inicial das redes e atualizando a cobertura.
        this.criarConect();
        // calculo da cobertura sem conectividade.
        this.calcCoberturaInicial();

        // ========= Verificacao se ha pontos descobertos =========
        if (this.currentCoveragePercent < this.coverageFactor) {
            this.suprirCobertura();
        }
    }

    public boolean suprirOnline() {
        for (WSNSensor sensFalho : this.periodFailedSensors) {
            WSNSensor sensorEscolhido = this.escolherSubs(sensFalho);
            if (sensorEscolhido == null) {
                break;
            }
            this.ligaSensor(sensorEscolhido);
            boolean fezConex = this.conectarSensorOnline(sensorEscolhido, sensFalho);
            if (!fezConex) {
                this.criarConect();
            }
        }
        this.calcCobertura();
        if (this.currentCoveragePercent >= this.coverageFactor) {
            return true;
        }
        System.out.println("Não foi possível suprimir cobertura Online");
        return false;
    }

    private boolean conectarSensorOnline(WSNSensor sensorEscolhido, WSNSensor sensFalho) {
        boolean retorno = sensorEscolhido.getNeighborhood().contains(sensFalho.getParent());
        if (retorno) {
            sensorEscolhido.setParent(sensFalho.getParent());
            sensFalho.getParent().adicionaFilho(sensorEscolhido);
            if (sensFalho.getParent().isConnected() || sensFalho.getParent() instanceof WSNSink) {
                sensorEscolhido.setConnected(true);
            }
        }
        List<WSNSensor> listSensorReconex = new ArrayList<>();
        for (WSNSensor sensFilho : sensFalho.getChildren()) {
            if (!sensFilho.isConnected()) {
                retorno = sensorEscolhido.getNeighborhood().contains(sensFilho);
                if (retorno) {
                    sensorEscolhido.adicionaFilho(sensFilho);
                    sensFilho.setParent(sensorEscolhido);
                    sensFilho.setConnected(true);
                    listSensorReconex.add(sensFilho);
                    sensFilho.connectChildren(listSensorReconex);
                }
            }
        }

        for (WSNSensor sensReconex : listSensorReconex) {
            this.atualizaCoberturaSemConec(sensReconex);
        }
        return retorno;
    }

    private WSNSensor escolherSubs(WSNSensor sensFalho) {
        WSNSensor sensEsc = null;
        double distQuad = Double.MAX_VALUE;
        for (WSNSensor sensCand : sensFalho.getNeighborhood()) {
            if (!sensCand.isActive() && !sensCand.isFailed()) {
                double distPai = this.conectivityMatrix[sensCand.getId()][sensFalho.getParent().getId()];
                double distAux = Math.pow(distPai, 2);
                for (WSNSensor sensFilho : sensFalho.getChildren()) {
                    double distFilho = this.conectivityMatrix[sensCand.getId()][sensFilho.getId()];
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




