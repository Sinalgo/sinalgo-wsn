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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Setter
@Getter
public class SensorNetwork {

    private List<Sensor> sensors;
    private List<Sensor> availableSensors;
    private List<Sensor> availableSensorsAndSinks;
    private List<Sensor> activeSensors;
    private List<Sensor> periodFailedSensors;
    private List<Sink> sinks;
    private int numCoveredPoints;
    private double currentCoveragePercent;
    private Map<Position, AtomicLong> demandPoints;
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
        this.setDemandPoints(new LinkedHashMap<>());
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < lenght; j++) {
                this.getDemandPoints().put(new Position(i + 0.5, j + 0.5, 0), new AtomicLong());
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
            this.addCoveredPoints();
            this.criaListVizinhosRC();
        }
    }

    private void addCoveredPoints() {
        for (Sensor sens : this.sensors) {
            for (Position pontoDemanda : this.getDemandPoints().keySet()) {
                double vDistancia = sens.getPosition().distanceTo(pontoDemanda);
                if (vDistancia <= sens.getSensRadius()) {
                    sens.getCoveredPoints().add(pontoDemanda);
                }
            }
        }
    }

    private void criaListVizinhosRC() {
        for (Sensor sensor1 : this.availableSensorsAndSinks) {
            for (Sensor sensor2 : this.availableSensorsAndSinks) {
                if (!sensor1.equals(sensor2)) {
                    double vDistancia = sensor1.getPosition().distanceTo(sensor2.getPosition());
                    double vRaio = sensor1.getCommRadius();
                    if (Double.compare(vDistancia, vRaio) <= 0) {
                        sensor1.getNeighborhood().put(sensor2, Sensor.buildNeighborData(vDistancia));
                    }
                }
            }
        }
    }

    public double calculaEnergiaConsPer() {
        return this.calculaEnergiaConsPer(this.activeSensors);
    }

    private double calculaEnergiaConsPer(List<Sensor> listSens) {
        double energiaGastaAcum = 0;
        for (Sensor s : listSens) {
            int vNumeroFilhos = s.queryDescendants();
            double enRec = s.getReceivePower() * vNumeroFilhos;
            double enTrans = s.getPowerToTransmit(s.getParent(), vNumeroFilhos);
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
        this.currentCoveragePercent = (double) this.numCoveredPoints / (double) this.getNumPontosDemanda();
        return this.currentCoveragePercent;
    }

    private void retirarCoberturaDesconexos() {
        for (Sensor s : this.activeSensors) {
            if (!s.isConnected()) {
                for (Position pontoCoberto : s.getCoveredPoints()) {
                    long coverage = this.getDemandPoints().get(pontoCoberto).decrementAndGet();
                    if (coverage == 0) {
                        this.numCoveredPoints--;
                    }
                }
            }
        }
    }

    private void computeInitialCoverage() {
        for (AtomicLong v : this.getDemandPoints().values()) {
            v.set(0);
        }
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
            double transmitEnergy = s.getPowerToTransmit(s.getParent(), totalNumerOfChildren);
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
        Map<Position, Long> demandPointsAux = new LinkedHashMap<>();
        int numPontosCobertosAux = 0;
        for (int cSensor : activeSensorIds) {
            numPontosCobertosAux += this.computeDisconnectedCoverage(this.sensors.get(cSensor), demandPointsAux);
        }
        return (this.getNumPontosDemanda() - numPontosCobertosAux);
    }

    //funcao para utilizacao no metodo computeDisconnectedCoverage(List<Integer> listIdSensAtivo)
    private int computeDisconnectedCoverage(Sensor sensor, Map<Position, Long> auxCoverageMap) {
        int auxCoveredPointsCount = 0;
        for (Position point : sensor.getCoveredPoints()) {
            if (auxCoverageMap.getOrDefault(point, 0L) == 0) {
                auxCoveredPointsCount++;
            }
            auxCoverageMap.merge(point, 1L, Long::sum);
        }
        return auxCoveredPointsCount;
    }

    private void computeDisconnectedCoverage(Sensor sensor) {
        if (sensor.isActive()) {
            for (Position point : sensor.getCoveredPoints()) {
                if (this.getDemandPoints().get(point).getAndIncrement() == 0) {
                    this.numCoveredPoints++;
                }
            }
        } else {
            for (Position point : sensor.getCoveredPoints()) {
                if (this.getDemandPoints().get(point).decrementAndGet() == 0) {
                    this.numCoveredPoints--;
                }
            }
        }
    }

    public void computeCostToSink() {
        Graph graph = new Graph(this.availableSensorsAndSinks);
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
        Graph graph = new Graph(this.availableSensorsAndSinks);
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
        while (Double.compare(fatorPontoDemanda / this.getNumPontosDemanda(), this.coverageFactor) < 0) {
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
                fatorPontoDemanda = this.getNumPontosDemanda();
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
        for (Position point : sens.getCoveredPoints()) {
            if (this.getDemandPoints().get(point).get() == 0) {
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

}




