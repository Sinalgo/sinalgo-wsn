package projects.tcc.simulation.rssf.sensor.impl;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.rssf.Environment;
import projects.tcc.simulation.rssf.RSSFPosition;
import projects.tcc.simulation.rssf.SensorCollection;
import projects.tcc.simulation.rssf.sensor.GraphNodeProperties;
import projects.tcc.simulation.rssf.sensor.Sensor;
import projects.tcc.simulation.rssf.sensor.Sink;

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

    @Setter(AccessLevel.PRIVATE)
    private long totalChildrenCount;

    @Setter(AccessLevel.PRIVATE)
    private double batteryEnergy;

    private final double originalEnergy;
    private final double minimumEnergy;
    private final double sensorRadius;
    private final double commRadius;

    @Setter(AccessLevel.PRIVATE)
    private boolean active;

    @Setter(AccessLevel.PRIVATE)
    private boolean connected;

    @Setter(AccessLevel.PRIVATE)
    private boolean failed;

    private final double activationPower;
    private final double receivePower;
    private final double maintenancePower;
    private final double commRatio;

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
        this.commRadius = commRadius;
        this.commRatio = commRatio;

        this.graphNodeProperties = new GraphNodeProperties();
        this.neighbors = new LinkedHashMap<>();
        this.coveredPoints = new LinkedHashSet<>();
        this.exclusivelyCoveredPoints = new LinkedHashSet<>();
        this.distances = new HashMap<>();
        this.children = new LinkedHashMap<>();

        this.activationPower = activationPower;
        this.receivePower = receivePower;
        this.maintenancePower = maintenancePower;

        this.setBatteryEnergy(batteryEnergy);

        this.originalEnergy = batteryEnergy;
        this.minimumEnergy = batteryEnergy * MINIMUM_BATTERY_LEVEL;
        this.sensorRadius = sensorRadius;

        this.setActive(false);
        this.setFailed(false);
        this.setConnected(false);
        this.performInitialization();
        this.computeCoveredPoints();

        this.setID(++idCounter);
        SensorCollection.addSensor(this);
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
    public void fail() {
        if (!this.isFailed()) {
            this.deactivate(false);
            this.setFailed(true);
            SensorCollection.update(this);
            Environment.updateCoverage(this);
        }
    }

    @Override
    public void activate() {
        this.activate(true);
    }

    private void activate(boolean updateCoverage) {
        if (!this.isFailed() && !this.isActive()) {
            this.setActive(true);
            SensorCollection.update(this);
            if (updateCoverage) {
                Environment.updateCoverage(this);
            }
        }
    }

    @Override
    public void deactivate() {
        this.deactivate(true);
    }

    private void deactivate(boolean updateCoverage) {
        if (!this.isFailed() && this.isActive()) {
            this.setActive(false);
            this.disconnect();
            SensorCollection.update(this);
            if (updateCoverage) {
                Environment.updateCoverage(this);
            }
        }
    }

    @Override
    public boolean isConnectable() {
        if (this instanceof Sink || this.isConnected()) {
            return true;
        }
        if (this.getParent() == null) {
            this.disconnect();
            return false;
        }
        if (this.getParent() instanceof Sink || this.getParent().isConnected()) {
            this.connect();
            return true;
        }
        if (this.getParent().isConnectable()) {
            this.connect();
        } else {
            this.disconnect();
        }
        return this.isConnected();
    }

    @Override
    public void updateState() {
        if (Double.compare(this.getBatteryEnergy(), this.getMinimumEnergy()) <= 0) {
            this.fail();
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
        this.disconnect();
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
        this.setBatteryEnergy(Math.max(0, this.getBatteryEnergy() - value));
        this.updateState();
    }

    @Override
    public double getEnergySpentInTransmission(double distanceToParent, long numberOfChildren) {
        return this.getCommRatio() * Sensor.getCurrentForDistance(distanceToParent) * (numberOfChildren + 1);
    }

    @Override
    public void disconnectAndPropagate() {
        this.disconnect(true);
    }

    @Override
    public void connectAndPropagate() {
        this.connect(true);
    }

    @Override
    public void connect() {
        this.connect(false);
    }

    @Override
    public void disconnect() {
        this.disconnect(false);
    }

    private void connect(boolean propagate) {
        if (!this.isConnected()) {
            this.activate(false);
            this.setConnected(true);
            if (propagate) {
                this.getChildren().values().forEach(Sensor::connectAndPropagate);
            }
            Environment.updateCoverage(this);
        }
    }

    private void disconnect(boolean propagate) {
        if (this.isConnected()) {
            this.setConnected(false);
            if (propagate) {
                this.getChildren().values().forEach(Sensor::disconnectAndPropagate);
            }
            Environment.updateCoverage(this);
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
