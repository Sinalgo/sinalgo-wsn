package projects.tcc.algorithms.graph;

import projects.tcc.rssf.Sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {

    public static void computePaths(Sensor source) {
        source.minDistance = 0.;
        PriorityQueue<Sensor> vertexQueue = new PriorityQueue<>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            Sensor u = vertexQueue.poll();

            // Visit each edge exiting u
            if (u != null) {
                for (Edge e : u.adjacencies) {
                    Sensor v = e.getTarget();
                    double weight = e.getWeight();
                    double distanceThroughU = u.minDistance + weight;
                    if (distanceThroughU < v.minDistance) {
                        vertexQueue.remove(v);
                        v.minDistance = distanceThroughU;
                        v.previous = u;

                        v.setSensorPai(u); //Adicionando o Pai do Sensor

                        vertexQueue.add(v);
                    }
                }
            }
        }
    }

    public static List<Sensor> getShortestPathTo(Sensor target) {
        List<Sensor> path = new ArrayList<>();
        for (Sensor vertex = target; vertex != null; vertex = vertex.previous) {
            path.add(vertex);
        }
        Collections.reverse(path);
        return path;
    }


    public static void main(String[] args) {

        Sensor s0 = new Sensor(0, 1., 1., 15, 0.25);
        Sensor s1 = new Sensor(1, 1., 5., 15, 0.25);
        Sensor s2 = new Sensor(2, 5., 1., 15, 0.25);
        Sensor s3 = new Sensor(3, 5., 5., 15, 0.25);
        Sensor s4 = new Sensor(4, 5., 15., 15, 0.25);


        s0.adjacencies.add(new Edge(s1, 5));
        s0.adjacencies.add(new Edge(s2, 10));
        s0.adjacencies.add(new Edge(s3, 9));
        s0.adjacencies.add(new Edge(s3, 8));

        s1.adjacencies.add(new Edge(s0, 5));
        s1.adjacencies.add(new Edge(s2, 3));
        s1.adjacencies.add(new Edge(s4, 7));

        s2.adjacencies.add(new Edge(s0, 10));
        s2.adjacencies.add(new Edge(s1, 3));

        s3.adjacencies.add(new Edge(s0, 8));
        s3.adjacencies.add(new Edge(s4, 2));

        s4.adjacencies.add(new Edge(s1, 7));
        s4.adjacencies.add(new Edge(s3, 2));


        Sensor[] vertices = {s0, s1, s2, s3, s4};
        computePaths(s0);

        for (Sensor v : vertices) {
            System.out.println("Distance to " + v + ": " + v.minDistance);


            System.out.println("Sensor: " + v + " - tem o pai o Sensor: " + v.getSensorPai());
            List<Sensor> path = getShortestPathTo(v);
            System.out.println("Path: " + path);
        }
    }
}