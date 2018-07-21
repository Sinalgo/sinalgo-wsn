package projects.tcc.simulation.rssf.sensor.impl;

import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.rssf.Environment;
import projects.tcc.simulation.rssf.RSSFPosition;
import projects.tcc.simulation.rssf.SensorHolder;
import projects.tcc.simulation.rssf.sensor.GraphNodeProperties;
import projects.tcc.simulation.rssf.sensor.Sensor;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static projects.tcc.simulation.io.ConfigurationLoader.getConfiguration;

@Getter
@Setter
public class RSSFSensor implements Sensor, Comparable<Sensor> {

    private final static double MINIMUM_BATTERY_LEVEL = 0.1;

    private final GraphNodeProperties graphNodeProperties;

    /**
     * A counter to assign each node a unique ID, at the time when it is generated.
     */
    private static long idCounter = 0;

    private long ID;

    private final RSSFPosition position = new RSSFPosition(0, 0, 0);

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
    private final Set<RSSFPosition> coveredPoints;
    private final Set<RSSFPosition> exclusivelyCoveredPoints;
    private final Map<Long, Double> distances;

    public RSSFSensor() {
        this(getConfiguration().getCommRadius(), getConfiguration().getCommRatio(), getConfiguration().getBatteryEnergy(),
                getConfiguration().getActivationPower(), getConfiguration().getReceivePower(),
                getConfiguration().getMaintenancePower(), getConfiguration().getSensorRadius());
    }

    private RSSFSensor(double commRadius, double commRatio,
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
        this.performInitialization();
        this.computeCoveredPoints();

        this.setID(++idCounter);
        SensorHolder.addSensor(this);
    }

    protected void computeCoveredPoints() {
        Environment.getPoints().forEach(p -> {
            if (Double.compare(this.getPosition().distanceTo(p), this.getSensorRadius()) <= 0) {
                this.getCoveredPoints().add(p);
                p.getCoveringSensors().add(this);
            }
        });
    }

    protected void performInitialization() {

    }

    @Override
    public void setFailed(boolean failed) {
        this.failed = failed;
        if (this.isFailed()) {
            Environment.getPoints().removeAll()
        }
    }

    @Override
    public void updateState() {
        this.setFailed(Double.compare(this.getBatteryEnergy(), this.getMinimumEnergy()) <= 0);
        if (this.isFailed()) {
            this.setActive(false);
            this.setConnected(false);
        }
    }

    @Override
    public void reset() {
        this.resetConnectivity();
        this.getNeighbors().clear();
        this.getCoveredPoints().clear();
        this.getExclusivelyCoveredPoints().clear();
        this.getGraphNodeProperties().reset();
    }

    @Override
    public void resetConnectivity() {
        this.setTotalChildrenCount(0);
        this.setConnected(false);
        this.setParent(null);
        this.getChildren().clear();
    }

    @Override
    public void addChild(Sensor child) {
        child.setParent(this);
        getChildren().put(child.getID(), child);
    }

    @Override
    public void subtractEnergySpent(double value) {
        this.setBatteryEnergy(Math.max(0, batteryEnergy - value));
    }

    @Override
    public double getEnergySpentInTransmission(double distanceToParent, long numberOfChildren) {
        return this.getCommRatio() * Sensor.getCurrentForDistance(distanceToParent) * (numberOfChildren + 1);
    }

    @Override
    public void disconnectAndPropagate() {
        this.setConnected(false);
        this.getChildren().values().forEach(Sensor::disconnectAndPropagate);
    }

    @Override
    public void connectAndPropagate() {
        if (!this.isConnected()) {
            this.setConnected(true);
            this.setActive(true);
            this.getChildren().values().forEach(Sensor::connectAndPropagate);
        }
    }

    @Override
    public void queryDescendants() {
        this.getChildren().values().forEach(Sensor::queryDescendants);
        long sum = this.getChildren().size();
        for (Sensor child : this.getChildren().values()) {
            sum += child.getTotalChildrenCount();
        }
        this.setTotalChildrenCount(sum);
    }

    @Override
    public int compareTo(Sensor o) {
        return Long.compare(this.getID(), o.getID());
    }
}
