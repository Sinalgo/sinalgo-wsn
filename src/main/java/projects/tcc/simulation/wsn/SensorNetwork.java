package projects.tcc.simulation.wsn;

import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.algorithms.graph.Graph;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.data.DemandPoint;
import projects.tcc.simulation.wsn.data.DemandPoints;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;
import sinalgo.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class SensorNetwork {

    private List<Sensor> sensors;
    private List<Sensor> sensorsAndSinks;
    private List<Sink> sinks;
    private double currentCoveragePercent;
    private DemandPoints demandPoints;

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
        this.sensorsAndSinks = new ArrayList<>();
        this.sinks = new ArrayList<>();
        this.demandPoints = new DemandPoints(Configuration.getDimX(), Configuration.getDimY());
        this.currentCoveragePercent = 0;
        this.coverageFactor = configuration.getCoverageFactor();
    }

    public int getDemandPointsCount() {
        return this.getDemandPoints().getNumPoints();
    }

    public int[] getAvailableSensorsArray() {
        return this.getSensors().stream()
                .filter(Sensor::isAvailable)
                .mapToInt(Sensor::getIndex)
                .toArray();
    }

    public void addSensor(Sensor sensor) {
        this.getSensors().add(sensor);
        this.getSensorsAndSinks().add(sensor);
    }

    public void addSink(Sink sink) {
        this.getSensorsAndSinks().add(sink);
        this.getSinks().add(sink);
    }

    private void setUp() {
        if (!this.isInitialized()) {
            this.setInitialized(true);
            this.getDemandPoints().computeSensorsCoveredPoints(this.getSensors());
            this.buildNeighborhoods();
        }
    }

    private void buildNeighborhoods() {
        for (Sensor sensor1 : this.getSensorsAndSinks()) {
            for (Sensor sensor2 : this.getSensorsAndSinks()) {
                if (!sensor1.equals(sensor2)) {
                    double distance = sensor1.getPosition().distanceTo(sensor2.getPosition());
                    double commRadius = Math.min(sensor1.getCommRadius(), sensor2.getCommRadius());
                    if (Double.compare(distance, commRadius) <= 0) {
                        sensor1.addNeighbor(sensor2, distance);
                    }
                }
            }
        }
    }

    public double getTotalConsumedPowerInRound() {
        double totalEnergySpent = 0;
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable() && s.isActive()) {
                int childrenCount = s.queryDescendants();
                double receivePower = s.getReceivePower() * childrenCount;
                double transmitPower = s.getTransmitPower(s.getParent(), childrenCount);
                double maintenancePower = s.getMaintenancePower();
                double energySpent = receivePower + transmitPower + maintenancePower;

                totalEnergySpent += energySpent;
            }
        }
        return totalEnergySpent;
    }

    public double computePeriodActivationEnergy() {
        double totalActivationEnergy = 0;
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable() && s.isUseActivationPower() && s.isActive()) {
                totalActivationEnergy += s.getActivationPower();
                s.setUseActivationPower(false);
            }
        }
        return totalActivationEnergy;
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
        this.removeDisconnectedCoverage();
        this.currentCoveragePercent =
                (double) this.getDemandPoints().getNumCoveredPoints() / (double) this.getDemandPointsCount();
        return this.currentCoveragePercent;
    }

    private void removeDisconnectedCoverage() {
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable() && s.isActive() && !s.isConnected()) {
                this.getDemandPoints().removeCoverage(s);
            }
        }
    }

    private void computeInitialCoverage() {
        this.getDemandPoints().resetCoverage();
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable() && s.isActive()) {
                this.computeDisconnectedCoverage(s);
            }
        }
        this.currentCoveragePercent = this.computeCoverage();
    }

    public int getActiveSensorCount() {
        int activeSensorsCount = 0;
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable() && s.isActive()) {
                activeSensorsCount++;
            }
        }
        return activeSensorsCount;
    }

    public int getAvailableSensorCount() {
        int availableSensorsCount = 0;
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable()) {
                availableSensorsCount++;
            }
        }
        return availableSensorsCount;
    }

    public void computePeriodConsumedEnergy() {
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable() && s.isActive()) {
                int childrenCount = s.queryDescendants();
                double receivePower = s.getReceivePower() * childrenCount;
                double transmitPower = s.getTransmitPower(s.getParent(), childrenCount);
                double maintenancePower = s.getMaintenancePower();

                s.drawEnergySpent(receivePower + transmitPower + maintenancePower);
            }
        }
    }

    public boolean removeFailedSensors() {
        boolean fail = false;
        double threshold = SimulationConfigurationLoader.getConfiguration().getMinBatteryThreshold();
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable() && s.isActive() &&
                    Double.compare(s.getBatteryEnergy(), threshold * s.getBatteryCapacity()) <= 0) {
                fail = true;
                s.setFailed(true);
                s.setActive(false);
                s.setConnected(false);
                s.disconnectChildren();
                s.getParent().getChildren().remove(s);
                s.setParent(null);
                this.deactivateSensor(s);
            }
        }
        if (fail) {
            this.computeCoverage();
        }
        return fail;
    }

    //funcao utilizada pelo AG
    public int computeNonCoverage(List<Integer> activeSensorIds) {
        int[] auxCoverage = new int[this.getDemandPointsCount()];
        int coveredPoints = 0;
        for (int cSensor : activeSensorIds) {
            for (DemandPoint p : this.sensors.get(cSensor).getCoveredPoints()) {
                coveredPoints += auxCoverage[p.getIndex()]++ == 0 ? 1 : 0;
            }
        }
        return this.getDemandPointsCount() - coveredPoints;
    }

    private void computeDisconnectedCoverage(Sensor sensor) {
        if (sensor.isActive()) {
            this.getDemandPoints().addCoverage(sensor);
        } else {
            this.getDemandPoints().removeCoverage(sensor);
        }
    }

    public void computeCostToSink() {
        Graph g = new Graph(this.getSensorsAndSinks());
        g.computeEdges(false);
        g.computeMinimalPathsTo(this.getSinks().get(0));
    }

    public void activateSensors(boolean[] activeArray) {
        int index = 0;
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable()) {
                s.setActive(activeArray[index++]);
            }
        }
    }

    private void createConnections() {
        //refazendo as conexoes
        for (Sensor s : this.getSensorsAndSinks()) {
            if (s.isAvailable()) {
                s.resetConnections();
            }
        }
        Graph g = new Graph(this.getSensorsAndSinks());
        g.computeEdges(true);
        g.computeMinimalPathsTo(this.getSinks().get(0));
        this.activateNeededParents();
        this.generateChildrenLists();
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable() && s.isActive()) {
                this.checkConnectivity(s);
            }
        }
    }

    private void generateChildrenLists() {
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable() && s.isActive()) {
                Sensor pai = s.getParent();
                if (pai != null) {
                    pai.addChild(s);
                }
            }
        }
    }

    private void activateNeededParents() {
        int numSensCox = 0;
        for (Sensor sens : this.getSensors()) {
            if (sens.isAvailable() && sens.isActive()) {
                Sensor curr = sens;
                while (curr.getParent() != null && !curr.getParent().isActive()) {
                    curr.getParent().setActive(true);
                    this.computeDisconnectedCoverage(curr.getParent());
                    curr = curr.getParent();
                    numSensCox++;
                }
            }
        }
        if (numSensCox > 0) {
            SimulationOutput.println("Numero de Sensores Ativos na Conectividade: " + numSensCox);
        }
    }

    public void supplyCoverage() {
        // Utilizado na versão OnlineHíbrido
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable() && s.isActive() && !s.isConnected()) {
                this.computeDisconnectedCoverage(s);
            }
        }
        boolean[] alreadyEvaluated = new boolean[this.getSensors().size()];
        double coveredPointsCount = this.getDemandPoints().getNumCoveredPoints();
        while (Double.compare(coveredPointsCount / this.getDemandPointsCount(), this.getCoverageFactor()) < 0) {
            Sensor chosen = this.chooseReplacement(alreadyEvaluated);
            if (chosen != null) {
                this.activateSensor(chosen);
                this.createConnections();
                if (chosen.getParent() == null || chosen.getParent().isFailed()) {
                    //Impossivel conectar o sensor na rede
                    alreadyEvaluated[chosen.getIndex()] = true;
                    this.deactivateSensor(chosen);
                    continue;
                } else {
                    SimulationOutput.println("Sensor Escolhido = " + chosen);
                    if (!(chosen.getParent() instanceof Sink)) {
                        this.computeDiscoveredPoints(chosen.getParent());
                    }
                }
                coveredPointsCount = this.getDemandPoints().getNumCoveredPoints();
            } else {
                //nao ha sensores para ativar
                SimulationOutput.println("There are no more sensors that could be activated to supply enough coverage");
                coveredPointsCount = this.getDemandPointsCount();
            }

        }
        this.computeCoverage();
    }

    private Sensor chooseReplacement(boolean[] alreadyEvaluated) {
        Sensor chosen = null;
        int maxDiscoveredPoints = 0;
        for (Sensor sensor : this.getSensors()) {
            if (sensor.isAvailable() && !alreadyEvaluated[sensor.getIndex()]) {
                if (!sensor.isActive()) {
                    int discoveredPoints = this.computeDiscoveredPoints(sensor);
                    if (discoveredPoints > maxDiscoveredPoints) {
                        chosen = sensor;
                        maxDiscoveredPoints = discoveredPoints;
                    }
                }
            }
        }
        return chosen;
    }

    private void activateSensor(Sensor chosenReplacement) {
        chosenReplacement.setActive(true);
        this.computeDisconnectedCoverage(chosenReplacement);
    }

    private int computeDiscoveredPoints(Sensor sens) {
        int discoveredPoints = 0;
        for (DemandPoint p : sens.getCoveredPoints()) {
            if (p.getCoverage() == 0) {
                discoveredPoints++;
            }
        }
        return discoveredPoints;
    }

    public boolean[] buildInitialNetwork(boolean[] activeSensors) {
        this.setUp();
        this.activateSensors(activeSensors);

        // criando a conectividade inicial das redes e atualizando a cobertura.
        this.createConnections();
        // calculo da cobertura sem conectividade.
        this.computeInitialCoverage();

        // ========= Verificacao se ha pontos descobertos =========
        if (this.getCurrentCoveragePercent() < this.getCoverageFactor()) {
            this.supplyCoverage();
        }

        boolean[] finalActiveSensors = new boolean[this.getSensors().size()];
        for (Sensor s : this.getSensors()) {
            if (s.isAvailable() && s.isActive()) {
                finalActiveSensors[s.getIndex()] = true;
            }
        }
        return finalActiveSensors;
    }

}




