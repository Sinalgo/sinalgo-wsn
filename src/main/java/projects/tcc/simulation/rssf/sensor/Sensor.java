package projects.tcc.simulation.rssf.sensor;

import projects.tcc.simulation.rssf.RSSFPosition;
import sinalgo.nodes.Position;

import java.util.Map;
import java.util.Set;

public interface Sensor {

    double DISTANCES[] = {
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

    double CURRENTS[] = {
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
    static double getCurrentForDistance(double distance) {
        if (Double.compare(distance, DISTANCES[DISTANCES.length - 1]) > 0) {
            throw new RuntimeException("Distância ao Pai não informada corretamente: " + distance);
        }
        int i = 0;
        while (Double.compare(DISTANCES[i], distance) <= 0) {
            i++;
        }
        return CURRENTS[i];
    }

    long getID();

    void setID(long ID);

    RSSFPosition getPosition();

    default void setPosition(Position position) {
        getPosition().assign(position);
    }

    default void setPosition(double x, double y, double z) {
        getPosition().assign(x, y, z);
    }

    void updateState();

    void reset();

    void resetConnectivity();

    void addChild(Sensor child);

    void subtractEnergySpent(double value);

    double getEnergySpentInTransmission(double distanceToParent, long numberOfChildren);

    void disconnectAndPropagate();

    void connectAndPropagate();

    void connect();

    void disconnect();

    void queryDescendants();

    void setParent(Sensor parent);

    void setBatteryEnergy(double batteryEnergy);

    GraphNodeProperties getGraphNodeProperties();

    Sensor getParent();

    Map<Long, Sensor> getChildren();

    long getTotalChildrenCount();

    double getBatteryEnergy();

    double getOriginalEnergy();

    double getMinimumEnergy();

    double getSensorRadius();

    double getCommRadius();

    boolean isActive();

    boolean isConnected();

    boolean isFailed();

    double getActivationPower();

    double getReceivePower();

    double getMaintenancePower();

    double getCommRatio();

    void fail();

    void activate();

    void deactivate();

    Map<Long, Sensor> getNeighbors();

    Set<RSSFPosition> getCoveredPoints();

    Set<RSSFPosition> getExclusivelyCoveredPoints();

    Map<Long, Double> getDistances();
}
