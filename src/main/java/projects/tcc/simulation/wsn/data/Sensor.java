package projects.tcc.simulation.wsn.data;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.nodes.nodeImplementations.SensorNode;
import projects.tcc.simulation.io.SimulationOutput;
import sinalgo.nodes.Position;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Setter(AccessLevel.PRIVATE)
@Getter(AccessLevel.PRIVATE)
@EqualsAndHashCode(of = {"index", "type"})
public class Sensor {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NeighborData {
        private final double distance;
        private final double current;
    }

    private static double DISTANCES_ARRAY[] = {
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
            91.440};

    private static double[] CURRENT_ARRAY = {
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
            25.4,
    };

    //Vetor de Corrente x Distância
    private static double getCurrentPerDistance(double distancia) {
        int i = 0;
        while (Double.compare(DISTANCES_ARRAY[i], distancia) <= 0) {
            i++;
            if (i == DISTANCES_ARRAY.length) {
                SimulationOutput.println("\n\nERROR: Distância ao Pai não informada corretamente");
                SimulationOutput.println("Valor da Distância: " + distancia);
            }
        }

        return CURRENT_ARRAY[i];
    }

    public void addNeighbor(Sensor neighbor, double distance) {
        this.getNeighborhood().put(neighbor,
                new NeighborData(distance, getCurrentPerDistance(distance)));
    }

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private SimulationNode node;

    @Getter
    private final int index;

    @Getter
    private final Class<? extends Sensor> type;

    @Getter
    private final Position position;

    @Getter
    private double batteryEnergy;

    @Getter
    private double batteryCapacity;

    @Getter
    private List<Sensor> children;

    @Getter
    @Setter
    private Sensor parent;

    @Getter
    private double sensRadius;

    @Getter
    private double commRadius;

    @Getter
    private boolean active;

    @Getter
    @Setter
    private boolean connected;

    @Getter
    private boolean failed;

    private double activationPower;
    private double receivePower;
    private double maintenancePower;
    private double commRatio; //Taxa de comunicação durante a transmissão em uma u.t.
    private double minBatteryThreshold;

    @Getter
    private Map<Sensor, NeighborData> neighborhood;

    @Getter
    private List<DemandPoint> coveredPoints;

    @Getter
    @Setter
    private double costToSink;

    public Sensor(int index, Position position, double commRadius, double commRatio) {
        this.index = index;
        this.type = this.getClass();
        this.position = position;
        this.setCommRadius(commRadius);

        this.setActive(true);
        this.setFailed(false);

        this.setChildren(new ArrayList<>());
        this.setNeighborhood(new LinkedHashMap<>());

        this.setCommRatio(commRatio);
    }

    public Sensor(int index, Position position, double sensRadius, double commRadius,
                  double batteryEnergy, double activationPower, double receivePower,
                  double maintenancePower, double commRatio, double minBatteryThreshold,
                  SensorNode node) {
        this(index, position, commRadius, commRatio);

        this.setActivationPower(activationPower);
        this.setReceivePower(receivePower);
        this.setMaintenancePower(maintenancePower);

        this.setBatteryEnergy(batteryEnergy);
        this.setBatteryCapacity(batteryEnergy);
        this.setSensRadius(sensRadius);
        this.setMinBatteryThreshold(minBatteryThreshold);

        this.setParent(null);

        this.setActive(false);
        this.setFailed(false);
        this.setConnected(false);

        this.setCoveredPoints(new ArrayList<>());
        this.setNode(node);
    }

    public void resetConnections() {
        this.setParent(null);
        this.setConnected(false);
        this.getChildren().clear();
    }

    public void addChild(Sensor child) {
        this.getChildren().add(child);
    }

    private void drawEnergySpent(double energySpent) {
        this.setBatteryEnergy(Math.max(this.getBatteryEnergy() - energySpent, 0));
    }

    private double getTransmitPower(Sensor neighbor) {
        double current = this.getNeighborhood().get(neighbor).getCurrent();
        return this.getCommRatio() * current;
    }

    private void disconnectChildren() {
        for (Sensor child : this.getChildren()) {
            child.setConnected(false);
            child.disconnectChildren();
        }
    }

    public boolean isAvailable() {
        return !this.isFailed();
    }

    public void fail() {
        if (this.isAvailable()) {
            this.deactivate();
            this.disconnectChildren();
            if (this.getParent() != null) {
                this.getParent().getChildren().remove(this);
            }
            this.resetConnections();
            this.setFailed(true);
        }
    }

    public void activate() {
        if (this.isAvailable() && !this.isActive()) {
            this.setActive(true);
            DemandPoints.currentInstance().addCoverage(this);
        }
    }

    public void deactivate() {
        if (this.isAvailable() && this.isActive()) {
            this.setActive(false);
            DemandPoints.currentInstance().removeCoverage(this);
        }
    }

    public void drawActivationEnergy() {
        this.drawEnergySpent(this.getActivationPower());
    }

    public void drawMaintenanceEnergy() {
        this.drawEnergySpent(this.getMaintenancePower());
    }

    public void drawReceiveEnergy() {
        this.drawEnergySpent(this.getReceivePower());
    }

    public void drawTransmitEnergy(Sensor neighbor) {
        this.drawEnergySpent(this.getTransmitPower(neighbor));
    }

    public void updateState() {
        if (this.isAvailable() && this.isActive() &&
                Double.compare(this.getBatteryEnergy(), this.getMinBatteryThreshold() * this.getBatteryCapacity()) <= 0) {
            this.fail();
        }
    }

    @Override
    public String toString() {
        return this.getNode() != null ? this.getNode().toString() : Integer.toString(this.getIndex());
    }

}
