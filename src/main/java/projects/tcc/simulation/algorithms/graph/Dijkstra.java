package projects.tcc.simulation.algorithms.graph;

import projects.tcc.nodes.edges.GraphEdge;
import projects.tcc.simulation.rssf.Sensor;
import sinalgo.nodes.edges.Edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
                for (Edge edge : u.getOutgoingConnections()) {
                    Sensor v = (Sensor) edge.getEndNode();
                    double weight = ((GraphEdge) edge).getWeight();
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

    public static List<Sensor> getShortestPathTo(Sensor target) {
        List<Sensor> path = new ArrayList<>();
        for (Sensor vertex = target; vertex != null; vertex = vertex.getPrevious()) {
            path.add(vertex);
        }
        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args) {
        Sensor s0 = new Sensor(1., 1., 15, 0.25);
        Sensor s1 = new Sensor(1., 5., 15, 0.25);
        Sensor s2 = new Sensor(5., 1., 15, 0.25);
        Sensor s3 = new Sensor(5., 5., 15, 0.25);
        Sensor s4 = new Sensor(5., 15., 15, 0.25);

        s0.addConnectionTo(s1);

        s0.getAdjacencies().add(new Edge(s1, 5));
        s0.getAdjacencies().add(new Edge(s2, 10));
        s0.getAdjacencies().add(new Edge(s3, 9));
        s0.getAdjacencies().add(new Edge(s3, 8));

        s1.getAdjacencies().add(new Edge(s0, 5));
        s1.getAdjacencies().add(new Edge(s2, 3));
        s1.getAdjacencies().add(new Edge(s4, 7));

        s2.getAdjacencies().add(new Edge(s0, 10));
        s2.getAdjacencies().add(new Edge(s1, 3));

        s3.getAdjacencies().add(new Edge(s0, 8));
        s3.getAdjacencies().add(new Edge(s4, 2));

        s4.getAdjacencies().add(new Edge(s1, 7));
        s4.getAdjacencies().add(new Edge(s3, 2));


        Sensor[] vertices = {s0, s1, s2, s3, s4};
        computePaths(s0);

        for (Sensor v : vertices) {
            System.out.println("Distance to " + v + ": " + v.getMinDistance());


            System.out.println("Sensor: " + v + " - tem o pai o Sensor: " + v.getParent());
            List<Sensor> path = getShortestPathTo(v);
            System.out.println("Path: " + path);
        }
    }
}