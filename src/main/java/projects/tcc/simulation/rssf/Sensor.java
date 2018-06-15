package projects.tcc.simulation.rssf;

import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.data.SensorHolder;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.tools.storage.ReusableIterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static projects.tcc.simulation.io.ConfigurationLoader.getConfiguration;

@Getter
@Setter
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

    private final List<Sensor> neighbors;
    private final Set<Integer> coveredPoints;
    private final Set<Integer> exclusivelyCoveredPoints;
    private double pathToSinkCost;

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
        this.neighbors = new ArrayList<>();
        this.coveredPoints = new LinkedHashSet<>();
        this.exclusivelyCoveredPoints = new LinkedHashSet<>();

        this.setActivationPower(activationPower);
        this.setReceivePower(receivePower);
        this.setMaintenancePower(maintenancePower);

        this.setBatteryEnergy(batteryEnergy);
        this.setOriginalEnergy(batteryEnergy);
        this.setSensorRadius(sensorRadius);

        this.setActive(false);
        this.setFailed(false);
        this.setConnected(false);

        if (!(this instanceof Sink)) {
            SensorHolder.addSensors(this);
        }
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

    public void setActive(boolean active) {
        if (!this.active && active) {
            this.setBitEA(true);
        }
        this.active = active;
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

    public double getEnergySpentInTransmission(double distanceToParent, long numberOfChildren) {
        double vCorrente = this.queryDistances(distanceToParent);
        return commRatio * vCorrente * (numberOfChildren + 1);
    }

    public void disconnectChildren() {
        for (Sensor child : this.getChildren()) {
            child.setConnected(false);
            child.disconnectChildren();
        }
    }

    public List<Sensor> connectChildren() {
        List<Sensor> connectedChildren = new ArrayList<>();
        connectedChildren.add(this);
        this.connectChildren(connectedChildren);
        return connectedChildren;
    }

    private void connectChildren(List<Sensor> reconnectedSensors) {
        for (Sensor child : this.getChildren()) {
            child.setConnected(true);
            reconnectedSensors.add(child);
            child.connectChildren(reconnectedSensors);
        }
    }

    public Sensor getParent() {
        Iterator<Edge> iterator = this.getOutgoingConnections().iterator();
        ((ReusableIterator<?>) iterator).reset();
        if (iterator.hasNext()) {
            return (Sensor) iterator.next().getEndNode();
        }
        return null;
    }

}
