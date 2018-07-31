package projects.tcc.simulation.wsn.data;

import projects.tcc.simulation.algorithms.graph.GraphEdge;
import projects.tcc.simulation.io.SimulationOutput;
import sinalgo.nodes.Position;

import java.util.List;

public interface Sensor {

    double DISTANCES_ARRAY[] = {
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

    double[] CURRENT_ARRAY = {
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
    static double getCurrentPerDistance(double distancia) {
        int i = 0;
        while (DISTANCES_ARRAY[i] <= distancia) {
            i++;
            if (i == DISTANCES_ARRAY.length) {
                SimulationOutput.println("\n\nERROR: Distância ao Pai não informada corretamente");
                SimulationOutput.println("Valor da Distância: " + distancia);
            }
        }

        return CURRENT_ARRAY[i];
    }

    void resetConnections();

    void addChild(Sensor child);

    int queryDescendants();

    void drawEnergySpent(double energySpent);

    double getPowerToTransmit(double vDistanciaAoPai, int vNumeroFilhos2);

    void disconnectChildren();

    void connectChildren(List<Sensor> reconnectedSensors);

    int getSensorId();

    Position getPosition();

    double getBatteryEnergy();

    double getBatteryCapacity();

    List<Sensor> getChildren();

    Sensor getParent();

    double getSensRadius();

    double getCommRadius();

    boolean isActive();

    boolean isUseActivationPower();

    boolean isConnected();

    boolean isFailed();

    double getActivationPower();

    double getReceivePower();

    double getMaintenancePower();

    double getCommRatio();

    List<Sensor> getNeighborhood();

    List<Integer> getCoveredPoints();

    List<Integer> getExclusivelyCoveredPoints();

    double getCostToSink();

    List<GraphEdge> getAdjacencies();

    double getMinDistance();

    Sensor getPrevious();

    void setBatteryEnergy(double batteryEnergy);

    void setBatteryCapacity(double batteryCapacity);

    void setChildren(List<Sensor> children);

    void setParent(Sensor parent);

    void setSensRadius(double sensRadius);

    void setCommRadius(double commRadius);

    void setActive(boolean active);

    void setUseActivationPower(boolean useActivationPower);

    void setConnected(boolean connected);

    void setFailed(boolean failed);

    void setActivationPower(double activationPower);

    void setReceivePower(double receivePower);

    void setMaintenancePower(double maintenancePower);

    void setCommRatio(double commRatio);

    void setNeighborhood(List<Sensor> neighborhood);

    void setCoveredPoints(List<Integer> coveredPoints);

    void setExclusivelyCoveredPoints(List<Integer> exclusivelyCoveredPoints);

    void setCostToSink(double costToSink);

    void setAdjacencies(List<GraphEdge> adjacencies);

    void setMinDistance(double minDistance);

    void setPrevious(Sensor previous);
}
