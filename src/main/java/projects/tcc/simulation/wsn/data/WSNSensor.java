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
@EqualsAndHashCode(of = "id")
public class WSNSensor implements Comparable<WSNSensor> {

    private static final double DISTANCES_ARRAY[] = {
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

    private static final double[] CURRENT_ARRAY = {
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

    private int wsnSensorId;
    private final Position position;
    private double batteryEnergy;
    private double batteryCapacity;
    private List<WSNSensor> children;
    private WSNSensor parent;
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

    private List<WSNSensor> neighborhood;
    private List<Integer> coveredPoints;
    private List<Integer> exclusivelyCoveredPoints;
    private double costToSink;

    private List<GraphEdge> adjacencies;
    private double minDistance;
    private WSNSensor previous;

    public WSNSensor(int wsnSensorId, double x, double y, double commRadius, double commRatio) {
        this.wsnSensorId = wsnSensorId;
        this.position = new Position(x, y, 0);
        this.commRadius = commRadius;

        this.active = true;

        this.children = new ArrayList<>();
        this.neighborhood = new ArrayList<>();

        this.setAdjacencies(new ArrayList<>());
        this.setMinDistance(Double.POSITIVE_INFINITY);

        this.commRatio = commRatio;
    }

    public WSNSensor(int wsnSensorId, double x, double y, double sensRadius, double commRadius,
                     double batteryEnergy, double activationPower, double receivePower,
                     double maintenancePower, double commRatio) {
        this(wsnSensorId, x, y, commRadius, commRatio);

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

    public void reiniciarSensorParaConectividade() {
        this.parent = null;
        this.setPrevious(null);
        this.connected = false;
        this.getAdjacencies().clear();
        this.setMinDistance(Double.POSITIVE_INFINITY);
        this.children.clear();
    }

    public int compareTo(WSNSensor other) {
        return Double.compare(this.getMinDistance(), other.getMinDistance());
    }

    public void adicionaFilho(WSNSensor child) {
        this.children.add(child);
    }

    public int queryDescendants() {
        int totalFilhos = this.children.size();
        for (WSNSensor sensFilho : this.children) {
            totalFilhos += sensFilho.queryDescendants();
        }
        return totalFilhos;
    }

    //Vetor de Corrente x Distância
    public static double getCurrentPerDistance(double distancia) {
        int i = 0;
        while (DISTANCES_ARRAY[i] <= distancia) {
            i++;
            if (i == DISTANCES_ARRAY.length) {
                System.out.println("\n\nERROR: Distância ao Pai não informada corretamente");
                System.out.println("Valor da Distância: " + distancia);
            }
        }

        return CURRENT_ARRAY[i];
    }

    public void drawEnergySpent(double energySpent) {
        this.batteryEnergy = Math.max(this.batteryEnergy - energySpent, 0);
    }

    public double getPowerToTransmit(double vDistanciaAoPai, int vNumeroFilhos2) {
        double vCorrente = getCurrentPerDistance(vDistanciaAoPai);
        return this.commRatio * vCorrente * (vNumeroFilhos2 + 1);
    }

    public void disconnectChildren() {
        for (WSNSensor sFilho : this.getChildren()) {
            sFilho.setConnected(false);
            sFilho.disconnectChildren();
        }
    }

    public void connectChildren(List<WSNSensor> reconnectedSensors) {
        for (WSNSensor sFilho : this.getChildren()) {
            sFilho.setConnected(true);
            reconnectedSensors.add(sFilho);
            sFilho.connectChildren(reconnectedSensors);
        }
    }

    @Override
    public String toString() {
        return Integer.toString(this.getWsnSensorId());
    }
}
