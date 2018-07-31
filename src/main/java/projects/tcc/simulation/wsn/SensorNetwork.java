package projects.tcc.simulation.wsn;

import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.algorithms.graph.Graph;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.io.SimulationOutput;
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

    private boolean initialized = false;

    private static SensorNetwork currentInstance;

    public static SensorNetwork currentInstance() {
        if (currentInstance == null) {
            return newInstance();
        }
        return currentInstance;
    }

    public static SensorNetwork newInstance() {
        currentInstance = new SensorNetwork(SimulationConfigurationLoader.getConfiguration());
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

    public int[] getAvailableSensorsArray() {
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

    private void setUp() {
        if (!this.isInitialized()) {
            this.setInitialized(true);
            this.constroiVetCobertura();
            this.constroiMatrizConectividade();
            this.criaListVizinhosRC();
        }
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

    public double computePeriodActivationEnergy() {
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
                this.deactivateSensor(s);
                numSensDesligados++;
            }
        }
        if (numSensDesligados > 0) {
            SimulationOutput.println("Numero de Sensores desligados por nao conectividade: " + numSensDesligados);
        }
    }

    private void deactivateSensor(Sensor s) {
        s.setActive(false);
        this.computeDisconnectedCoverage(s);
    }

    private boolean checkConnectivity(Sensor s) {
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
        boolean conexo = this.checkConnectivity(s.getParent());
        if (conexo) {
            s.setConnected(true);
        }
        return conexo;
    }

    public double computeCoverage() {
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

    private void computeInitialCoverage() {
        this.coverageArray = new int[this.demandPoints.size()];
        this.numCoveredPoints = 0;
        for (Sensor sens : this.activeSensors) {
            this.computeDisconnectedCoverage(sens);
        }
        this.currentCoveragePercent = this.computeCoverage();

    }

    public int getActiveSensorCount() {
        return this.activeSensors.size();
    }

    public void computePeriodConsumedEnergy() {
        for (Sensor s : this.activeSensors) {
            int totalNumerOfChildren = s.queryDescendants();
            double receiveEnergy = s.getReceivePower() * totalNumerOfChildren;

            double distanceToParent = this.conectivityMatrix[s.getSensorId()][s.getParent().getSensorId()];
            double consumedCurrent = Sensor.getCurrentPerDistance(distanceToParent);

            double transmitEnergy = s.getCommRatio() * consumedCurrent * (totalNumerOfChildren + 1);
            double maintenanceEnergy = s.getMaintenancePower();

            s.drawEnergySpent(receiveEnergy + transmitEnergy + maintenanceEnergy);
        }
    }

    public boolean removeFailedSensors(List<Sensor> periodFailedSensors, double remainingBatteryPercentage) {
        boolean fail = false;
        for (Sensor s : this.activeSensors) {
            if (s.isActive() &&
                    Double.compare(s.getBatteryEnergy(),
                            (remainingBatteryPercentage / 100) * s.getBatteryCapacity()) <= 0) {
                fail = true;
                s.setFailed(true);
                s.setActive(false);
                s.setConnected(false);
                s.disconnectChildren();
                periodFailedSensors.add(s);
            }
        }
        for (Sensor sens : periodFailedSensors) {
            this.deactivateSensor(sens);
            sens.getParent().getChildren().remove(sens);
            this.activeSensors.remove(sens);
            this.availableSensors.remove(sens);
            this.availableSensorsAndSinks.remove(sens);
        }
        if (fail) {
            this.computeCoverage();
        }
        return fail;
    }

    //funcao utilizada pelo AG
    public int computeDisconnectedCoverage(List<Integer> activeSensorIds) {
        int[] vetCoberturaAux = new int[this.demandPoints.size()];
        int numPontosCobertosAux = 0;
        for (int cSensor : activeSensorIds) {
            numPontosCobertosAux += this.computeDisconnectedCoverage(this.sensors.get(cSensor), vetCoberturaAux);
        }
        return (vetCoberturaAux.length - numPontosCobertosAux);
    }

    //funcao para utilizacao no metodo computeDisconnectedCoverage(List<Integer> listIdSensAtivo)
    private int computeDisconnectedCoverage(Sensor sensor, int[] auxCoverageMatrix) {
        int auxCoveredPointsCount = 0;
        List<Integer> coveredPoints = sensor.getCoveredPoints();
        for (Integer point : coveredPoints) {
            if (auxCoverageMatrix[point] == 0) {
                auxCoveredPointsCount++;
            }
            auxCoverageMatrix[point]++;
        }
        return auxCoveredPointsCount;
    }

    private int computeDisconnectedCoverage(Sensor sensor) {
        List<Integer> coveredPoints = sensor.getCoveredPoints();
        if (sensor.isActive()) {
            for (Integer point : coveredPoints) {
                if (this.coverageArray[point] == 0) {
                    this.numCoveredPoints++;
                }
                this.coverageArray[point]++;
            }
        } else {
            for (Integer point : coveredPoints) {
                this.coverageArray[point]--;
                if (this.coverageArray[point] == 0) {
                    this.numCoveredPoints--;
                }
            }
        }
        return this.numCoveredPoints;
    }

    public void computeCostToSink() {
        Graph graph = new Graph(this.availableSensorsAndSinks, this.conectivityMatrix);
        graph.build();
        graph.computeMinimalPathsTo(this.sinks.get(0));
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

    private void createConnections() {
        //refazendo as conexoes
        for (Sensor sens : this.availableSensorsAndSinks) {
            sens.resetConnections();
        }
        Graph graph = new Graph(this.availableSensorsAndSinks, this.conectivityMatrix);
        graph.buildConnectionGraph();
        graph.computeMinimalPathsTo(this.sinks.get(0));
        this.activateNeededParents();
        this.generateChildrenLists();
        for (Sensor s : this.activeSensors) {
            this.checkConnectivity(s);
        }
    }

    private void generateChildrenLists() {
        for (Sensor sens : this.activeSensors) {
            Sensor pai = sens.getParent();
            if (pai != null) {
                pai.addChild(sens);
            }
        }
    }

    private void activateNeededParents() {
        List<Sensor> activatedSensors = new ArrayList<>();
        int numSensCox = 0;
        for (Sensor sens : this.activeSensors) {
            Sensor curr = sens;
            while (curr.getParent() != null && !curr.getParent().isActive() && !(curr instanceof Sink)) {
                curr.getParent().setActive(true);
                activatedSensors.add(curr.getParent());
                this.computeDisconnectedCoverage(curr.getParent());
                curr = curr.getParent();
                numSensCox++;
            }
        }
        this.activeSensors.addAll(activatedSensors);
        if (numSensCox > 0) {
            SimulationOutput.println("Numero de Sensores Ativos na Conectividade: " + numSensCox);
        }
    }

    public void supplyCoverage() {
        // Utilizado na versão OnlineHíbrido
        for (Sensor s : this.activeSensors) {
            if (!s.isConnected()) {
                this.computeDisconnectedCoverage(s);
            }
        }
        List<Sensor> disconnectedSensors = new ArrayList<>();
        double fatorPontoDemanda = this.numCoveredPoints;
        while (Double.compare(fatorPontoDemanda / this.coverageArray.length, this.coverageFactor) < 0) {
            Sensor chosen = this.chooseReplacement(disconnectedSensors);
            if (chosen != null) {
                this.activateSensor(chosen);
                this.createConnections();

                if (chosen.getParent() == null) {
                    //Impossivel conectar o sensor na rede
                    disconnectedSensors.add(chosen);
                    this.deactivateSensor(chosen);
                    continue;
                }
                //possivel problema que pode ocorrer.
                else if (chosen.getParent().isFailed()) {
                    //Impossivel conectar o sensor na rede
                    disconnectedSensors.add(chosen);
                    this.deactivateSensor(chosen);
                    continue;
                } else {
                    SimulationOutput.println("Sensor Escolhido = " + chosen);
                    if (!(chosen.getParent() instanceof Sink)) {
                        this.updateExclusivelyCoveredPoints(chosen.getParent());
                    }
                }
                fatorPontoDemanda = this.numCoveredPoints;
            } else {
                //nao ha sensores para ativar
                SimulationOutput.println("Nao ha mais sensores para ativar e suprir a cobertura");
                fatorPontoDemanda = this.coverageArray.length;
            }

        }
        this.computeCoverage();
    }

    private Sensor chooseReplacement(List<Sensor> listSensorDesconex) {
        Sensor chosen = null;
        int maxDiscoveredPoints = 0;
        for (Sensor sensor : this.availableSensors) {
            if (!listSensorDesconex.contains(sensor)) {
                if (!sensor.isActive()) {
                    if (sensor.isFailed()) {
                        throw new SinalgoFatalException("Accessing failed sensor in the list of available sensors");
                    }
                    int discoveredPoints = this.updateExclusivelyCoveredPoints(sensor);
                    if (discoveredPoints > maxDiscoveredPoints) {
                        chosen = sensor;
                        maxDiscoveredPoints = discoveredPoints;
                    }
                }
            }
        }
        return chosen;
    }

    private void activateSensor(Sensor sensEscolhido) {
        sensEscolhido.setActive(true);
        this.activeSensors.add(sensEscolhido);
        this.computeDisconnectedCoverage(sensEscolhido);
    }

    private int updateExclusivelyCoveredPoints(Sensor sens) {
        sens.getExclusivelyCoveredPoints().clear();
        int discoveredPoints = 0;
        for (int point : sens.getCoveredPoints()) {
            if (this.coverageArray[point] == 0) {
                discoveredPoints++;
                sens.getExclusivelyCoveredPoints().add(point);
            }
        }
        return discoveredPoints;
    }

    public void buildInitialNetwork(boolean[] activeSensors) {
        this.setUp();
        this.activateSensors(activeSensors);

        // criando a conectividade inicial das redes e atualizando a cobertura.
        this.createConnections();
        // calculo da cobertura sem conectividade.
        this.computeInitialCoverage();

        // ========= Verificacao se ha pontos descobertos =========
        if (this.currentCoveragePercent < this.coverageFactor) {
            this.supplyCoverage();
        }
    }

    public boolean supplyCoverageOnline() {
        for (Sensor failedSensor : this.periodFailedSensors) {
            Sensor chosen = this.chooseReplacement(failedSensor);
            if (chosen == null) {
                break;
            }
            this.activateSensor(chosen);
            boolean fezConex = this.connectSensorOnline(chosen, failedSensor);
            if (!fezConex) {
                this.createConnections();
            }
        }
        this.computeCoverage();
        if (this.currentCoveragePercent >= this.coverageFactor) {
            return true;
        }
        SimulationOutput.println("Não foi possível suprimir cobertura Online");
        return false;
    }

    private boolean connectSensorOnline(Sensor sensorEscolhido, Sensor sensFalho) {
        boolean connected = sensorEscolhido.getNeighborhood().contains(sensFalho.getParent());
        if (connected) {
            sensorEscolhido.setParent(sensFalho.getParent());
            sensFalho.getParent().addChild(sensorEscolhido);
            if (sensFalho.getParent().isConnected() || sensFalho.getParent() instanceof Sink) {
                sensorEscolhido.setConnected(true);
            }
        }
        List<Sensor> reconnectedSensors = new ArrayList<>();
        for (Sensor child : sensFalho.getChildren()) {
            if (!child.isConnected()) {
                connected = sensorEscolhido.getNeighborhood().contains(child);
                if (connected) {
                    sensorEscolhido.addChild(child);
                    child.setParent(sensorEscolhido);
                    child.setConnected(true);
                    reconnectedSensors.add(child);
                    child.connectChildren(reconnectedSensors);
                }
            }
        }
        for (Sensor sensReconex : reconnectedSensors) {
            this.computeDisconnectedCoverage(sensReconex);
        }
        return connected;
    }

    private Sensor chooseReplacement(Sensor failedSensor) {
        Sensor chosen = null;
        double minSquaredDistance = Double.MAX_VALUE;
        for (Sensor candidate : failedSensor.getNeighborhood()) {
            if (!candidate.isActive() && !candidate.isFailed()) {
                double totalSquaredDistance = Math.pow(this.conectivityMatrix[candidate.getSensorId()][failedSensor.getParent().getSensorId()], 2);
                for (Sensor sensFilho : failedSensor.getChildren()) {
                    totalSquaredDistance += Math.pow(this.conectivityMatrix[candidate.getSensorId()][sensFilho.getSensorId()], 2);
                }
                if (Double.compare(totalSquaredDistance, minSquaredDistance) < 0) {
                    minSquaredDistance = totalSquaredDistance;
                    chosen = candidate;
                }
            }
        }
        return chosen;
    }

}




