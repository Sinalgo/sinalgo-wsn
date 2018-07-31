package projects.tcc.simulation.algorithms.graph;

import lombok.extern.java.Log;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;

import java.util.List;

@Log
public class Graph {

    private List<Sensor> sensorSinkList;
    private double[][] connectivityMatrix;

    public Graph(List<Sensor> sensorSinkList, double[][] connectivityMatrix) {
        this.sensorSinkList = sensorSinkList;
        this.connectivityMatrix = connectivityMatrix;
    }

    public void build() {
        for (Sensor vertA : this.sensorSinkList) {
            for (Sensor vertB : vertA.getNeighborhood()) {
                double distance = this.connectivityMatrix[vertA.getSensorId()][vertB.getSensorId()];
                double weight = Sensor.getCurrentPerDistance(distance);
                vertA.getAdjacencies().add(new GraphEdge(vertB, weight));
            }
        }
    }

    public void computeMinimalPathsTo(Sensor sens) {
        if (sens instanceof Sink) {
            Dijkstra.computePaths(sens);
            for (Sensor vert : this.sensorSinkList) {
                vert.setCostToSink(vert.getMinDistance());
            }
        } else {
            log.severe("Tried to compute paths to non-sink sensor");
        }
    }

    public void buildConnectionGraph() {
        double penalty = 2500;
        for (Sensor vertA : this.sensorSinkList) {
            for (Sensor vertB : vertA.getNeighborhood()) {
                if (!vertB.isFailed()) {
                    double distance = this.connectivityMatrix[vertA.getSensorId()][vertB.getSensorId()];
                    double weight = Sensor.getCurrentPerDistance(distance);
                    if ((vertA.isActive() && !vertB.isActive()) ||
                            (!vertA.isActive() && vertB.isActive())) {
                        weight = weight * penalty;
                    } else if (!vertA.isActive() && !vertB.isActive()) {
                        weight = weight * penalty * penalty;
                    }
                    vertA.getAdjacencies().add(new GraphEdge(vertB, weight));
                }
            }
        }
    }
}
