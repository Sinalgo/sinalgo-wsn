package projects.tcc.simulation.algorithms.graph;

import projects.tcc.simulation.io.ConfigurationLoader;
import projects.tcc.simulation.io.SimulationConfiguration;
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

    public static List<Sensor> getShortestPathTo(Sensor target) {
        List<Sensor> path = new ArrayList<>();
        for (Sensor vertex = target; vertex != null; vertex = vertex.getPrevious()) {
            path.add(vertex);
        }
        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args) {

        ConfigurationLoader.overrideConfiguration(SimulationConfiguration.builder()
                .commRadius(15)
                .commRatio(0.25)
                .build());

        Sensor s0 = new Sensor();
        Sensor s1 = new Sensor();
        Sensor s2 = new Sensor();
        Sensor s3 = new Sensor();
        Sensor s4 = new Sensor();

        s0.setPosition(1, 1, 0);
        s1.setPosition(1, 5, 0);
        s2.setPosition(5, 1, 0);
        s3.setPosition(5, 15, 0);
        s4.setPosition(5, 15, 0);

        s0.addConnectionTo(s1);
        s0.addConnectionTo(s2);
        s0.addConnectionTo(s3);
        s0.addConnectionTo(s3);

        s1.addConnectionTo(s0);
        s1.addConnectionTo(s2);
        s1.addConnectionTo(s4);

        s2.addConnectionTo(s0);
        s2.addConnectionTo(s1);

        s3.addConnectionTo(s0);
        s3.addConnectionTo(s4);

        s4.addConnectionTo(s1);
        s4.addConnectionTo(s3);


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