package projects.tcc.simulation.rssf;

import sinalgo.exception.WrongConfigurationException;
import sinalgo.nodes.messages.Inbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Sensor extends SimulationNode {

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

    static {
        Arrays.sort(DISTANCES);
        Arrays.sort(CURRENTS);
    }

    private double batteryEnergy;
    private double originalEnergy;
    private final List<Sensor> children;
    private Sensor parent;
    private double sensorRadius;
    private double commRadius;
    private boolean active;
    private boolean bitEA;
    private boolean connected;
    private boolean failed;

    private double activationPower;
    private double receivePower;
    private double maintenancePower;
    private double commRatio; //Taxa de comunicação durante a transmissão em uma u.t.

    private List<Integer> coveredPoints;
    private List<Integer> exclusivelyCoveredPoints;
    private double pathToSinkCost;

    private double minDistance;
    private Sensor previous;

    public Sensor(double x, double y, double commRadius, double commRatio) {
        super();
        this.setPosition(x, y, 0);

        this.commRadius = commRadius;

        this.active = true;

        this.children = new ArrayList<>();

        this.setMinDistance(Double.POSITIVE_INFINITY);

        this.commRatio = commRatio;
    }

    public Sensor(double x, double y, double sensorRadius, double raioComunicacao,
                  double batteryEnergy, double activationPower, double receivePower, double maintenancePower, double commRatio) {

        this(x, y, raioComunicacao, commRatio);

        this.activationPower = activationPower;
        this.receivePower = receivePower;
        this.maintenancePower = maintenancePower;

        this.batteryEnergy = batteryEnergy;
        this.originalEnergy = batteryEnergy;
        this.sensorRadius = sensorRadius;

        this.parent = null;

        this.active = false;
        this.failed = false;
        this.connected = false;

        this.coveredPoints = new ArrayList<>();
        this.exclusivelyCoveredPoints = new ArrayList<>();
    }

    public void reiniciarSensorParaConectividade() {
        this.setParent(null);
        this.setPrevious(null);
        this.setConnected(false);
        this.setMinDistance(Double.POSITIVE_INFINITY);
        this.children.clear();
    }

    public int compareTo(Sensor other) {
        return Double.compare(this.getMinDistance(), other.getMinDistance());
    }

    @Override
    public void handleMessages(Inbox inbox) {

    }

    @Override
    public void preStep() {

    }

    @Override
    public void init() {

    }

    @Override
    public void neighborhoodChange() {

    }

    @Override
    public void postStep() {

    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }

    public void addChild(Sensor child) {
        children.add(child);
    }

    public List<Sensor> getChildren() {
        return children;
    }

    public Sensor getParent() {
        return parent;
    }

    public void setParent(Sensor parent) {
        this.parent = parent;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (!this.active && active) {
            this.setBitEA(true);
        }
        this.active = active;
    }

    public boolean isBitEA() {
        return bitEA;
    }

    public void setBitEA(boolean bitEA) {
        this.bitEA = bitEA;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public List<Integer> getCoveredPoints() {
        return coveredPoints;
    }

    public void setCoveredPoints(List<Integer> coveredPoints) {
        this.coveredPoints = coveredPoints;
    }

    public List<Integer> getExclusivelyCoveredPoints() {
        return exclusivelyCoveredPoints;
    }

    public void setExclusivelyCoveredPoints(List<Integer> exclusivelyCoveredPoints) {
        this.exclusivelyCoveredPoints = exclusivelyCoveredPoints;
    }

    public double getBatteryEnergy() {
        return batteryEnergy;
    }

    public double getOriginalEnergy() {
        return originalEnergy;
    }

    public double getSensorRadius() {
        return sensorRadius;
    }

    public double getCommRadius() {
        return commRadius;
    }

    public double getActivationPower() {
        return activationPower;
    }

    public double getReceivePower() {
        return receivePower;
    }

    public double getMaintenancePower() {
        return maintenancePower;
    }

    public double getCommRatio() {
        return commRatio;
    }

    public double getPathToSinkCost() {
        return pathToSinkCost;
    }

    public void setPathToSinkCost(double pathToSinkCost) {
        this.pathToSinkCost = pathToSinkCost;
    }

    public long queryDescendants() {
        long totalChildren = this.children.size();
        for (Sensor sensFilho : children) {
            totalChildren += sensFilho.queryDescendants();
        }
        return totalChildren;
    }

    //Vetor de Corrente x distância
    public double queryDistances(double distance) {
        if (Double.compare(distance, DISTANCES[DISTANCES.length - 1]) > 0) {
            throw new RuntimeException("Distância ao Pai não informada corretamente: " + distance);
        }

        int i = 0;
        while (Double.compare(DISTANCES[i], distance) <= 0) {
            i++;
        }

        return CURRENTS[i];
    }

    public void subtractEnergySpent(double value) {
        batteryEnergy = Math.max(0, batteryEnergy - value);
    }

    public double getEnergySpentInTransmission(double distanceToParent, int numberOfChildren) {
        double vCorrente = this.queryDistances(distanceToParent);
        return commRatio * vCorrente * (numberOfChildren + 1);
    }

    public void disconnectChildren() {
        for (Sensor child : this.getChildren()) {
            child.setConnected(false);
            child.disconnectChildren();
        }
    }

    public void connectChildren(List<Sensor> reconnectedSensors) {
        for (Sensor child : this.getChildren()) {
            child.setConnected(true);
            reconnectedSensors.add(child);
            child.connectChildren(reconnectedSensors);
        }
    }

    public double getMinDistance() {
        return minDistance;
    }

    public void setMinDistance(double minDistance) {
        this.minDistance = minDistance;
    }

    public Sensor getPrevious() {
        return previous;
    }

    public void setPrevious(Sensor previous) {
        this.previous = previous;
    }
}
