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

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class SensorNetwork {

    private List<Sensor> sensors;
    private List<Sensor> sensorsAndSinks;
    private List<Sink> sinks;

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
        this.coverageFactor = configuration.getCoverageFactor();
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
            DemandPoints.currentInstance().computeSensorsCoveredPoints(this.getSensors());
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

    //funcao utilizada pelo AG
    public int computeNonCoverage(List<Integer> activeSensorIds) {
        int[] auxCoverage = new int[DemandPoints.currentInstance().getTotalNumPoints()];
        int coveredPoints = 0;
        for (int cSensor : activeSensorIds) {
            for (DemandPoint p : this.sensors.get(cSensor).getCoveredPoints()) {
                coveredPoints += auxCoverage[p.getIndex()]++ == 0 ? 1 : 0;
            }
        }
        return DemandPoints.currentInstance().getTotalNumPoints() - coveredPoints;
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
                if (activeArray[index++]) {
                    s.activate();
                } else {
                    s.deactivate();
                }
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
                    curr.getParent().activate();
                    curr = curr.getParent();
                    numSensCox++;
                }
            }
        }
        if (numSensCox > 0) {
            SimulationOutput.println("Numero de Sensores Ativos na Conectividade: " + numSensCox);
        }
    }

    private void supplyCoverage() {
        // Utilizado na versão OnlineHíbrido
        boolean[] alreadyEvaluated = new boolean[this.getSensors().size()];
        DemandPoints demandPoints = DemandPoints.currentInstance();
        double coveredPointsCount = demandPoints.getCoveredNumPoints();
        while (Double.compare(coveredPointsCount / demandPoints.getTotalNumPoints(), this.getCoverageFactor()) < 0) {
            Sensor chosen = this.chooseReplacement(alreadyEvaluated);
            if (chosen != null) {
                chosen.activate();
                this.createConnections();
                if (chosen.getParent() == null || chosen.getParent().isFailed()) {
                    //Impossivel conectar o sensor na rede
                    alreadyEvaluated[chosen.getIndex()] = true;
                    chosen.deactivate();
                    continue;
                } else {
                    SimulationOutput.println("Sensor Escolhido = " + chosen);
                }
                coveredPointsCount = demandPoints.getCoveredNumPoints();
            } else {
                //nao ha sensores para ativar
                SimulationOutput.println("There are no more sensors that could be activated to supply enough coverage");
                coveredPointsCount = demandPoints.getTotalNumPoints();
            }
        }
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

        DemandPoints demandPoints = DemandPoints.currentInstance();
        // ========= Verificacao se ha pontos descobertos =========
        if (demandPoints.getCoveragePercent() < this.getCoverageFactor()) {
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




