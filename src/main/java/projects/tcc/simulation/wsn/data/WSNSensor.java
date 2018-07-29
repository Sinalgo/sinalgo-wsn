package projects.tcc.simulation.wsn.data;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.algorithms.graph.GraphEdge;
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
        this.commRadius = commRadius;

        this.active = true;

        this.children = new ArrayList<>();
        this.neighborhood = new ArrayList<>();

        this.setAdjacencies(new ArrayList<>());
        this.setMinDistance(Double.POSITIVE_INFINITY);

        this.commRatio = commRatio;
    }

    public WSNSensor(int sensorId, double x, double y, double sensRadius, double commRadius,
                     double batteryEnergy, double activationPower, double receivePower,
                     double maintenancePower, double commRatio) {
        this(sensorId, x, y, commRadius, commRatio);

        this.activationPower = activationPower;
        this.receivePower = receivePower;
        this.maintenancePower = maintenancePower;

        this.batteryEnergy = batteryEnergy;
        this.batteryCapacity = batteryEnergy;
        this.sensRadius = sensRadius;

        this.parent = null;

        this.active = false;
        this.failed = false;
        this.connected = false;

        this.coveredPoints = new ArrayList<>();
        this.exclusivelyCoveredPoints = new ArrayList<>();
    }

    @Override
    public void reiniciarSensorParaConectividade() {
        this.parent = null;
        this.setPrevious(null);
        this.connected = false;
        this.getAdjacencies().clear();
        this.setMinDistance(Double.POSITIVE_INFINITY);
        this.children.clear();
    }

    @Override
    public void adicionaFilho(Sensor child) {
        this.children.add(child);
    }

    @Override
    public int queryDescendants() {
        int totalFilhos = this.children.size();
        for (Sensor sensFilho : this.children) {
            totalFilhos += sensFilho.queryDescendants();
        }
        return totalFilhos;
    }

    @Override
    public void drawEnergySpent(double energySpent) {
        this.batteryEnergy = Math.max(this.batteryEnergy - energySpent, 0);
    }

    @Override
    public double getPowerToTransmit(double vDistanciaAoPai, int vNumeroFilhos2) {
        double vCorrente = Sensor.getCurrentPerDistance(vDistanciaAoPai);
        return this.commRatio * vCorrente * (vNumeroFilhos2 + 1);
    }

    @Override
    public void disconnectChildren() {
        for (Sensor sFilho : this.getChildren()) {
            sFilho.setConnected(false);
            sFilho.disconnectChildren();
        }
    }

    @Override
    public void connectChildren(List<Sensor> reconnectedSensors) {
        for (Sensor sFilho : this.getChildren()) {
            sFilho.setConnected(true);
            reconnectedSensors.add(sFilho);
            sFilho.connectChildren(reconnectedSensors);
        }
    }

    @Override
    public String toString() {
        return Integer.toString(this.getSensorId());
    }
}
