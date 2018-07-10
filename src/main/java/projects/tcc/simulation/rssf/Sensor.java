package projects.tcc.simulation.rssf;

import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.data.SensorHolder;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.nodes.Position;
import sinalgo.nodes.messages.Inbox;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static projects.tcc.simulation.io.ConfigurationLoader.getConfiguration;

@Getter
@Setter
public class Sensor extends SimulationNode {

    private final static double MINIMUM_BATTERY_LEVEL = 0.1;

    private final static double DISTANCES[] = {
            5.142,
            5.769,
            6.473,
            7.263,
            8.150,
            9.144,
            10.260,
            11.512,
            12.916,
            14.492,
            16.261,
            18.245,
            20.471,
            22.969,
            25.771,
            28.916,
            32.444,
            36.403,
            40.845,
            45.829,
            51.420,
            57.695,
            64.735,
            72.633,
            81.496,
            91.440
    };

    private final static double CURRENTS[] = {
            8.6,
            8.8,
            9.0,
            9.0,
            9.1,
            9.3,
            9.3,
            9.5,
            9.7,
            9.9,
            10.1,
            10.4,
            10.6,
            10.8,
            11.1,
            13.8,
            14.5,
            14.5,
            15.1,
            15.8,
            16.8,
            17.2,
            18.5,
            19.2,
            21.3,
            25.4
    };

    //Vetor de Corrente x distância
    public static double getCurrentForDistance(double distance) {
        if (Double.compare(distance, DISTANCES[DISTANCES.length - 1]) > 0) {
            throw new RuntimeException("Distância ao Pai não informada corretamente: " + distance);
        }
        int i = 0;
        while (Double.compare(DISTANCES[i], distance) <= 0) {
            i++;
        }
        return CURRENTS[i];
    }

    @Getter
    @Setter
    public static class GraphNodeProperties {
        private Long parentId;
        private final Map<Long, Double> pathToSinkCost = new HashMap<>();

        public void reset() {
            this.setParentId(null);
            this.getPathToSinkCost().clear();
        }
    }

    private final GraphNodeProperties graphNodeProperties;

    private Sensor parent;
    private final Map<Long, Sensor> children;
    private long totalChildrenCount;

    private double batteryEnergy;
    private double originalEnergy;
    private double minimumEnergy;
    private double sensorRadius;
    private double commRadius;
    private boolean active;
    private boolean connected;
    private boolean failed;

    private double activationPower;
    private double receivePower;
    private double maintenancePower;
    private double commRatio; //Taxa de comunicação durante a transmissão em uma u.t.

    private final Map<Long, Sensor> neighbors;
    private final Set<Position> coveredPoints;
    private final Set<Position> exclusivelyCoveredPoints;
    private final Map<Long, Double> distances;

    public Sensor() {
        this(getConfiguration().getCommRadius(), getConfiguration().getCommRatio(), getConfiguration().getBatteryEnergy(),
                getConfiguration().getActivationPower(), getConfiguration().getReceivePower(),
                getConfiguration().getMaintenancePower(), getConfiguration().getSensorRadius());
    }

    private Sensor(double commRadius, double commRatio,
                   double batteryEnergy, double activationPower, double receivePower,
                   double maintenancePower, double sensorRadius) {
        super();
        this.setCommRadius(commRadius);
        this.setActive(true);
        this.setCommRatio(commRatio);

        this.graphNodeProperties = new GraphNodeProperties();
        this.neighbors = new LinkedHashMap<>();
        this.coveredPoints = new LinkedHashSet<>();
        this.exclusivelyCoveredPoints = new LinkedHashSet<>();
        this.distances = new HashMap<>();
        this.children = new LinkedHashMap<>();

        this.setActivationPower(activationPower);
        this.setReceivePower(receivePower);
        this.setMaintenancePower(maintenancePower);

        this.setBatteryEnergy(batteryEnergy);
        this.setOriginalEnergy(batteryEnergy);
        this.setMinimumEnergy(batteryEnergy * MINIMUM_BATTERY_LEVEL);
        this.setSensorRadius(sensorRadius);

        this.setActive(false);
        this.setFailed(false);
        this.setConnected(false);
    }

    @Override
    public void handleMessages(Inbox inbox) {

    }

    @Override
    public void preStep() {

    }

    @Override
    public void init() {
        SensorHolder.addSensors(this);
    }

    @Override
    public void neighborhoodChange() {
        // Do not use. We don't have a mobility model.
    }

    @Override
    public void postStep() {

    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }

    public void updateState() {
        this.setFailed(Double.compare(this.getBatteryEnergy(), this.getMinimumEnergy()) <= 0);
        if (this.isFailed()) {
            this.setActive(false);
            this.setConnected(false);
        }
    }

    public void reset() {
        this.resetConnectivity();
        this.getNeighbors().clear();
        this.getCoveredPoints().clear();
        this.getExclusivelyCoveredPoints().clear();
        this.getGraphNodeProperties().reset();
    }

    public void resetConnectivity() {
        this.setTotalChildrenCount(0);
        this.setConnected(false);
        this.setParent(null);
        this.getChildren().clear();
    }

    public void addChild(Sensor child) {
        getChildren().put(child.getID(), child);
    }

    public void subtractEnergySpent(double value) {
        this.setBatteryEnergy(Math.max(0, batteryEnergy - value));
    }

    public double getEnergySpentInTransmission(double distanceToParent, long numberOfChildren) {
        return this.getCommRatio() * getCurrentForDistance(distanceToParent) * (numberOfChildren + 1);
    }

    public void disconnectAndPropagate() {
        this.setConnected(false);
        this.getChildren().values().forEach(Sensor::disconnectAndPropagate);
    }

    public void connectAndPropagate() {
        if (!this.isConnected()) {
            this.setConnected(true);
            this.setActive(true);
            this.getChildren().values().forEach(Sensor::connectAndPropagate);
        }
    }

    public void queryDescendants() {
        this.getChildren().values().forEach(Sensor::queryDescendants);
        long sum = this.getChildren().size();
        for (Sensor child : this.getChildren().values()) {
            sum += child.getTotalChildrenCount();
        }
        this.setTotalChildrenCount(sum);
    }

}
