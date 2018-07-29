package projects.tcc.simulation.wsn.data.impl;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.algorithms.graph.GraphEdge;
import projects.tcc.simulation.wsn.data.Sensor;
import sinalgo.nodes.Position;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@EqualsAndHashCode(of = "sensorId")
public class WSNSensor implements Sensor {

    private final int sensorId;
    private final Position position;
    private double batteryEnergy;
    private double batteryCapacity;
    private List<Sensor> children;
    private Sensor parent;
    private double sensRadius;
    private double commRadius;
    private boolean active;
    private boolean useActivationPower;
    private boolean connected;
    private boolean failed;

    private double activationPower;
    private double receivePower;
    private double maintenancePower;
    private double commRatio; //Taxa de comunicação durante a transmissão em uma u.t.

    private List<Sensor> neighborhood;
    private List<Integer> coveredPoints;
    private List<Integer> exclusivelyCoveredPoints;
    private double costToSink;

    private List<GraphEdge> adjacencies;
    private double minDistance;
    private Sensor previous;

    public WSNSensor(int sensorId, double x, double y, double commRadius, double commRatio) {
        this.sensorId = sensorId;
        this.position = new Position(x, y, 0);
        this.setCommRadius(commRadius);

        this.setActive(true);

        this.setChildren(new ArrayList<>());
        this.setNeighborhood(new ArrayList<>());

        this.setAdjacencies(new ArrayList<>());
        this.setMinDistance(Double.POSITIVE_INFINITY);

        this.setCommRatio(commRatio);
    }

    public WSNSensor(int sensorId, double x, double y, double sensRadius, double commRadius,
                     double batteryEnergy, double activationPower, double receivePower,
                     double maintenancePower, double commRatio) {
        this(sensorId, x, y, commRadius, commRatio);

        this.setActivationPower(activationPower);
        this.setReceivePower(receivePower);
        this.setMaintenancePower(maintenancePower);

        this.setBatteryEnergy(batteryEnergy);
        this.setBatteryCapacity(batteryEnergy);
        this.setSensRadius(sensRadius);

        this.setParent(null);

        this.setActive(false);
        this.setFailed(false);
        this.setConnected(false);

        this.setCoveredPoints(new ArrayList<>());
        this.setExclusivelyCoveredPoints(new ArrayList<>());
    }

    @Override
    public void resetConnections() {
        this.setParent(null);
        this.setPrevious(null);
        this.setConnected(false);
        this.getAdjacencies().clear();
        this.setMinDistance(Double.POSITIVE_INFINITY);
        this.getChildren().clear();
    }

    @Override
    public void addChild(Sensor child) {
        this.getChildren().add(child);
    }

    @Override
    public int queryDescendants() {
        int totalChildCount = this.getChildren().size();
        for (Sensor child : this.getChildren()) {
            totalChildCount += child.queryDescendants();
        }
        return totalChildCount;
    }

    @Override
    public void drawEnergySpent(double energySpent) {
        this.setBatteryEnergy(Math.max(this.getBatteryEnergy() - energySpent, 0));
    }

    @Override
    public double getPowerToTransmit(double distanceToParent, int totalChildCount) {
        double current = Sensor.getCurrentPerDistance(distanceToParent);
        return this.commRatio * current * (totalChildCount + 1);
    }

    @Override
    public void disconnectChildren() {
        for (Sensor child : this.getChildren()) {
            child.setConnected(false);
            child.disconnectChildren();
        }
    }

    @Override
    public void connectChildren(List<Sensor> reconnectedSensors) {
        for (Sensor child : this.getChildren()) {
            child.setConnected(true);
            reconnectedSensors.add(child);
            child.connectChildren(reconnectedSensors);
        }
    }

    @Override
    public String toString() {
        return Integer.toString(this.getSensorId());
    }
}
