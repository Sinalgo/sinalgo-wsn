package projects.tcc.simulation.wsn;

import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.algorithms.graph.Graph;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;
import sinalgo.exception.SinalgoFatalException;
import sinalgo.nodes.Position;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class SensorNetwork {

    private List<Sensor> sensors;
    private List<Sensor> availableSensors;
    private List<Sensor> availableSensorsAndSinks;
    private List<Sensor> activeSensors;
    private List<Sensor> periodFailedSensors;
    private List<Sink> sinks;
    private int[] coverageArray;
    private int numCoveredPoints;
    private double currentCoveragePercent;
    private double[][] conectivityMatrix;
    private List<Position> demandPoints;
    private double area;

    private double coverageFactor;

    private boolean prepared = false;

    private static SensorNetwork currentInstance;

    public static SensorNetwork getCurrentInstance() {
        if (currentInstance == null) {
            currentInstance = new SensorNetwork(SimulationConfigurationLoader.getConfiguration());
        }
        return currentInstance;
    }

    private SensorNetwork(SimulationConfiguration configuration) {
        this.sensors = new ArrayList<>();
        this.availableSensorsAndSinks = new ArrayList<>();
        this.availableSensors = new ArrayList<>();
        this.activeSensors = new ArrayList<>();
        this.sinks = new ArrayList<>();
        this.computeDemandPoints(configuration.getDimX(), configuration.getDimY());
        this.numCoveredPoints = 0;
        this.currentCoveragePercent = 0;
        this.area = configuration.getDimX() * configuration.getDimY();
        this.coverageFactor = configuration.getCoverageFactor();
    }

    public int getNumPontosDemanda() {
        return this.demandPoints.size();
    }

    public int[] getVetIdsSensDisp() {
        return this.availableSensors.stream().mapToInt(Sensor::getSensorId).toArray();
    }

    private void computeDemandPoints(int width, int lenght) {
        this.setDemandPoints(new ArrayList<>());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < lenght; j++) {
                this.getDemandPoints().add(new Position(i + 0.5, j + 0.5, 0));
            }
        }
    }

    public void addSensors(Sensor sensor) {
        this.sensors.add(sensor);
        this.availableSensorsAndSinks.add(sensor);
        this.availableSensors.add(sensor);

    }

    public void addSinks(Sink sink) {
        this.availableSensorsAndSinks.add(sink);
        this.sinks.add(sink);
    }

    public void prepararRede() {
        if (this.isPrepared()) {
            throw new SinalgoFatalException("Double initialization of the SensorNetwork object");
        }
        this.setPrepared(true);
        this.constroiVetCobertura();
        this.constroiMatrizConectividade();
        this.criaListVizinhosRC();
    }

    private void constroiMatrizConectividade() {
        int numSensoresDisp_Sink = this.availableSensorsAndSinks.size();
        this.conectivityMatrix = new double[numSensoresDisp_Sink][numSensoresDisp_Sink];
        for (Sensor sensor1 : this.availableSensorsAndSinks) {
            for (Sensor sensor2 : this.availableSensorsAndSinks) {
                if (!sensor1.equals(sensor2)) {
                    double vDistancia = sensor1.getPosition().distanceTo(sensor2.getPosition());
                    this.conectivityMatrix[sensor1.getSensorId()][sensor2.getSensorId()] = vDistancia;
                } else {
                    this.conectivityMatrix[sensor1.getSensorId()][sensor2.getSensorId()] = -1;
                }
            }
        }
    }

    private void constroiVetCobertura() {
        this.coverageArray = new int[this.demandPoints.size()];
        for (Sensor sens : this.sensors) {
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
        for (Sensor sensor1 : this.availableSensorsAndSinks) {
            List<Sensor> listSensVizinhos = new ArrayList<>();
            for (Sensor sensor2 : this.availableSensorsAndSinks) {
                if (!sensor1.equals(sensor2)) {
                    double vDistancia = this.conectivityMatrix[sensor1.getSensorId()][sensor2.getSensorId()];
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

    private double calculaEnergiaConsPer(List<Sensor> listSens) {
        double energiaGastaAcum = 0;
        for (Sensor s : listSens) {
            int idSens = s.getSensorId();
            int sensPai = s.getParent().getSensorId();
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
        for (Sensor aListSensoresDisp : this.availableSensors) {
            if (aListSensoresDisp.isUseActivationPower() && aListSensoresDisp.isActive()) {
                enAtivAcum += aListSensoresDisp.getActivationPower();
                aListSensoresDisp.setUseActivationPower(false);
            }
        }
        return enAtivAcum;
    }

    public void desligarSensoresDesconexos() {
        int numSensDesligados = 0;
        for (Sensor s : this.activeSensors) {
            if (!s.isConnected()) {
                this.desligarSensor(s);
                numSensDesligados++;
            }
        }
        if (numSensDesligados > 0) {
            System.out.println("Numero de Sensores desligados por nao conectividade: " + numSensDesligados);
        }
    }

    private void desligarSensor(Sensor s) {
        s.setActive(false);
        this.atualizaCoberturaSemConec(s);
    }

    private boolean verificarConectividade(Sensor s) {
        if (s.isConnected()) {
            return true;
        }
        if (s.getParent() == null) {
            return false;
        }
        if (s.getParent() instanceof Sink || s.getParent().isConnected()) {
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
        for (Sensor s : this.activeSensors) {
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
        for (Sensor sens : this.activeSensors) {
            this.atualizaCoberturaSemConec(sens);
        }
        this.currentCoveragePercent = this.calcCobertura();

    }

    public int getNumSensAtivos() {
        return this.activeSensors.size();
    }

    public void calculaEnergiaPeriodo() {
        for (Sensor s : this.activeSensors) {
            int vNumeroFilhos = s.queryDescendants();
            double ER = s.getReceivePower() * vNumeroFilhos;

            double vDistanciaAoPai = this.conectivityMatrix[s.getSensorId()][s.getParent().getSensorId()];
            double vCorrente = Sensor.getCurrentPerDistance(vDistanciaAoPai);

            double ET = s.getCommRatio() * vCorrente * (vNumeroFilhos + 1);
            double EM = s.getMaintenancePower();
            double EnergiaGasta = ER + ET + EM;

            s.drawEnergySpent(EnergiaGasta);
        }
    }

    public boolean retirarSensoresFalhaEnergia(List<Sensor> listSensFalhosNoPer, double porcBatRet) {
        boolean falha = false;
        for (Sensor s : this.activeSensors) {
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
        for (Sensor sens : listSensFalhosNoPer) {
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
    private int atualizaCoberturaSemConec(Sensor sensor, int[] vetCoberturaAux) {
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

    private int atualizaCoberturaSemConec(Sensor sensor) {
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
        Graph graphCM = new Graph(this.availableSensorsAndSinks, this.conectivityMatrix);
        graphCM.build();
        graphCM.computeMinimalPathsTo(this.sinks.get(0));
    }

    public void activateSensors(boolean[] activeArray) {
        this.activeSensors.clear();
        for (int i = 0; i < activeArray.length; i++) {
            if (activeArray[i]) {
                this.availableSensors.get(i).setActive(true);
                this.activeSensors.add(this.availableSensors.get(i));
            } else {
                this.availableSensors.get(i).setActive(false);
            }
        }
    }

    private void criarConect() {
        //refazendo as conexoes
        for (Sensor sens : this.availableSensorsAndSinks) {
            sens.resetConnections();
        }
        Graph graphCM = new Graph(this.availableSensorsAndSinks, this.conectivityMatrix);
        graphCM.construirGrafoConect();
        graphCM.computeMinimalPathsTo(this.sinks.get(0));
        this.ativarPaisDesativados();
        this.geraListFilhos();
        for (Sensor s : this.activeSensors) {
            this.verificarConectividade(s);
        }
    }

    private void geraListFilhos() {
        for (Sensor sens : this.activeSensors) {
            Sensor pai = sens.getParent();
            if (pai != null) {
                pai.addChild(sens);
            }
        }
    }

    private void ativarPaisDesativados() {
        List<Sensor> sensAtivos = new ArrayList<>();
        int numSensCox = 0;
        for (Sensor sens : this.activeSensors) {
            Sensor sensAux = sens;
            while (sensAux.getParent() != null && !sensAux.getParent().isActive() && !(sensAux instanceof Sink)) {
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
        for (Sensor s : this.activeSensors) {
            if (!s.isConnected()) {
                this.atualizaCoberturaSemConec(s);
            }
        }
        boolean retorno = true;
        double nPontoDemanda = this.coverageArray.length;
        List<Sensor> listSensorDesconex = new ArrayList<>();
        double fatorPontoDemanda = this.numCoveredPoints;
        while (fatorPontoDemanda / nPontoDemanda < this.coverageFactor) {
            Sensor sensEscolhido = this.escolherSensorSubstituto(listSensorDesconex);
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
                    System.out.println("Sensor Escolhido = " + sensEscolhido);
                    if (!(sensEscolhido.getParent() instanceof Sink)) {
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

    private Sensor escolherSensorSubstituto(List<Sensor> listSensorDesconex) {
        Sensor sensEscolhido = null;
        int maiorNumPontCobDescob = 0;
        for (Sensor sens : this.availableSensors) {
            if (!listSensorDesconex.contains(sens)) {
                if (!sens.isActive()) {
                    if (sens.isFailed()) {
                        System.out.println("Acessando Sensor Falho na lista de Sensores Disponíveis");
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

    private void ligaSensor(Sensor sensEscolhido) {
        sensEscolhido.setActive(true);
        this.activeSensors.add(sensEscolhido);
        this.atualizaCoberturaSemConec(sensEscolhido);
    }

    private int atualizarListaPontosCobExclusivo(Sensor sens) {
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
        this.activateSensors(vetBoolean);

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
        for (Sensor sensFalho : this.periodFailedSensors) {
            Sensor sensorEscolhido = this.escolherSubs(sensFalho);
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

    private boolean conectarSensorOnline(Sensor sensorEscolhido, Sensor sensFalho) {
        boolean retorno = sensorEscolhido.getNeighborhood().contains(sensFalho.getParent());
        if (retorno) {
            sensorEscolhido.setParent(sensFalho.getParent());
            sensFalho.getParent().addChild(sensorEscolhido);
            if (sensFalho.getParent().isConnected() || sensFalho.getParent() instanceof Sink) {
                sensorEscolhido.setConnected(true);
            }
        }
        List<Sensor> listSensorReconex = new ArrayList<>();
        for (Sensor sensFilho : sensFalho.getChildren()) {
            if (!sensFilho.isConnected()) {
                retorno = sensorEscolhido.getNeighborhood().contains(sensFilho);
                if (retorno) {
                    sensorEscolhido.addChild(sensFilho);
                    sensFilho.setParent(sensorEscolhido);
                    sensFilho.setConnected(true);
                    listSensorReconex.add(sensFilho);
                    sensFilho.connectChildren(listSensorReconex);
                }
            }
        }

        for (Sensor sensReconex : listSensorReconex) {
            this.atualizaCoberturaSemConec(sensReconex);
        }
        return retorno;
    }

    private Sensor escolherSubs(Sensor sensFalho) {
        Sensor sensEsc = null;
        double distQuad = Double.MAX_VALUE;
        for (Sensor sensCand : sensFalho.getNeighborhood()) {
            if (!sensCand.isActive() && !sensCand.isFailed()) {
                double distPai = this.conectivityMatrix[sensCand.getSensorId()][sensFalho.getParent().getSensorId()];
                double distAux = Math.pow(distPai, 2);
                for (Sensor sensFilho : sensFalho.getChildren()) {
                    double distFilho = this.conectivityMatrix[sensCand.getSensorId()][sensFilho.getSensorId()];
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




