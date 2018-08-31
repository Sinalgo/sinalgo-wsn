package projects.tcc.simulation.algorithms.graph;

import lombok.extern.java.Log;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;

import java.util.List;

@Log
public class Graph {

    public static void computeAdjacencies(List<Sensor> sensorSinkList) {
        for (Sensor vertA : sensorSinkList) {
            if (vertA.isAvailable()) {
                vertA.getAdjacencies().clear();
                vertA.getNeighborhood().forEach((vertB, neighborData) -> {
                    if (vertB.isAvailable()) {
                        vertA.getAdjacencies().add(new GraphEdge(vertB, neighborData.getCurrent()));
                    }
                });
            }
        }
    }

    public static void computeMinimalPathsTo(List<Sensor> sensorSinkList, Sink sink) {
        Dijkstra.computePaths(sink);
        for (Sensor vert : sensorSinkList) {
            if (vert.isAvailable()) {
                vert.setCostToSink(vert.getMinDistance());
            }
        }
    }

    public static void computeAdjacenciesWithPenalties(List<Sensor> sensorSinkList) {
        double penalty = 2500;
        for (Sensor vertA : sensorSinkList) {
            if (vertA.isAvailable()) {
                vertA.getAdjacencies().clear();
                vertA.getNeighborhood().forEach((vertB, neighborData) -> {
                    if (vertB.isAvailable()) {
                        double weight = neighborData.getCurrent();
                        if ((vertA.isActive() && !vertB.isActive()) ||
                                (!vertA.isActive() && vertB.isActive())) {
                            weight = weight * penalty;
                        } else if (!vertA.isActive() && !vertB.isActive()) {
                            weight = weight * penalty * penalty;
                        }
                        vertA.getAdjacencies().add(new GraphEdge(vertB, weight));
                    }
                });
            }
        }
    }
}
