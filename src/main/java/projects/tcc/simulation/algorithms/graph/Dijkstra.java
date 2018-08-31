package projects.tcc.simulation.algorithms.graph;

import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.data.Sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {

    public static void computePaths(Sensor source) {
        source.setMinDistance(0);
        PriorityQueue<Sensor> vertexQueue = new PriorityQueue<>(Comparator.comparingDouble(Sensor::getMinDistance));
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            Sensor u = vertexQueue.poll();

            // Visit each edge exiting u
            for (GraphEdge e : u.getAdjacencies()) {
                Sensor v = e.getTarget();
                double weight = e.getWeight();
                double distanceThroughU = u.getMinDistance() + weight;
                if (Double.compare(distanceThroughU, v.getMinDistance()) < 0) {
                    vertexQueue.remove(v);
                    v.setMinDistance(distanceThroughU);
                    v.setPrevious(u);
                    v.setParent(u); //Adicionando o Pai do Sensor
                    vertexQueue.add(v);
                }
            }
        }
    }

    private static List<Sensor> getShortestPathTo(Sensor target) {
        List<Sensor> path = new ArrayList<>();
        for (Sensor vertex = target; vertex != null; vertex = vertex.getPrevious()) {
            path.add(vertex);
        }
        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args) {

        Sensor s0 = new Sensor(0, 1, 1, 15, 0.25);
        Sensor s1 = new Sensor(1, 1, 5, 15, 0.25);
        Sensor s2 = new Sensor(2, 5, 1, 15, 0.25);
        Sensor s3 = new Sensor(3, 5, 5, 15, 0.25);
        Sensor s4 = new Sensor(4, 5, 15, 15, 0.25);


        s0.getAdjacencies().add(new GraphEdge(s1, 5));
        s0.getAdjacencies().add(new GraphEdge(s2, 10));
        s0.getAdjacencies().add(new GraphEdge(s3, 9));
        s0.getAdjacencies().add(new GraphEdge(s3, 8));

        s1.getAdjacencies().add(new GraphEdge(s0, 5));
        s1.getAdjacencies().add(new GraphEdge(s2, 3));
        s1.getAdjacencies().add(new GraphEdge(s4, 7));

        s2.getAdjacencies().add(new GraphEdge(s0, 10));
        s2.getAdjacencies().add(new GraphEdge(s1, 3));

        s3.getAdjacencies().add(new GraphEdge(s0, 8));
        s3.getAdjacencies().add(new GraphEdge(s4, 2));

        s4.getAdjacencies().add(new GraphEdge(s1, 7));
        s4.getAdjacencies().add(new GraphEdge(s3, 2));


        Sensor[] vertices = {s0, s1, s2, s3, s4};
        computePaths(s0);

        for (Sensor v : vertices) {
            SimulationOutput.println("Distance to " + v + ": " + v.getMinDistance());
            SimulationOutput.println("Sensor: " + v + " - tem o pai o Sensor: " + v.getParent());
            List<Sensor> path = getShortestPathTo(v);
            SimulationOutput.println("Path: " + path);
        }
    }
}