package projects.tcc.simulation.algorithms.graph;

import lombok.extern.java.Log;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;

import java.util.List;

@Log
public class Graph {

    private List<Sensor> sensorSinkList;

    public Graph(List<Sensor> sensorSinkList) {
        this.sensorSinkList = sensorSinkList;
    }

    public void build() {
        for (Sensor vertA : this.sensorSinkList) {
            vertA.getNeighborhood().forEach((vertB, neighborData) -> {
                if (vertA instanceof Sink || vertA.isAvailable()) {
                    vertA.getAdjacencies().add(new GraphEdge(vertB, neighborData.getCurrent()));
                }
            });
        }
    }

    public void computeMinimalPathsTo(Sensor sens) {
        if (sens instanceof Sink) {
            Dijkstra.computePaths(sens);
            for (Sensor vert : this.sensorSinkList) {
                if (vert instanceof Sink || vert.isAvailable()) {
                    vert.setCostToSink(vert.getMinDistance());
                }
            }
        } else {
            log.severe("Tried to compute paths to non-sink sensor");
        }
    }

    public void buildConnectionGraph() {
        double penalty = 2500;
        for (Sensor vertA : this.sensorSinkList) {
            if ((vertA instanceof Sink || vertA.isAvailable())) {
                vertA.getNeighborhood().forEach((vertB, neighborData) -> {
                    if (vertB instanceof Sink || vertB.isAvailable()) {
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
