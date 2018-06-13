package projects.tcc.simulation.algorithms.graph;

import projects.tcc.simulation.rssf.Sensor;
import sinalgo.nodes.edges.Edge;

import java.util.PriorityQueue;

public class Dijkstra {

    public static void computePaths(Sensor source) {
        source.setMinDistance(0);
        PriorityQueue<Sensor> vertexQueue = new PriorityQueue<>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            Sensor u = vertexQueue.poll();

            // Visit each edge exiting u
            if (u != null) {
                for (Edge e : u.getOutgoingConnections()) {
                    Sensor v = (Sensor) e.getEndNode();
                    double weight = v.getAdjacenciesMatrix().get(e.getEndNode().getID());
                    double distanceThroughU = u.getMinDistance() + weight;
                    if (distanceThroughU < v.getMinDistance()) {
                        vertexQueue.remove(v);
                        v.setMinDistance(distanceThroughU);
                        v.setPrevious(u);
                        v.addConnectionTo(u); //Adicionando o Pai do Sensor
                        vertexQueue.add(v);
                    }
                }
            }
        }
    }
}