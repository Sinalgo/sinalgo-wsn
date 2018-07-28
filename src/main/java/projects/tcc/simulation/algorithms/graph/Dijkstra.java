package projects.tcc.simulation.algorithms.graph;

import projects.tcc.simulation.wsn.data.WSNSensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra {

    public static void computePaths(WSNSensor source) {
        source.setMinDistance(0);
        PriorityQueue<WSNSensor> vertexQueue = new PriorityQueue<>();
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            WSNSensor u = vertexQueue.poll();

            // Visit each edge exiting u
            for (GraphEdge e : u.getAdjacencies()) {
                WSNSensor v = e.getTarget();
                double weight = e.getWeight();
                double distanceThroughU = u.getMinDistance() + weight;
                if (Double.compare(distanceThroughU, v.getMinDistance()) < 0) {
                    vertexQueue.remove(v);
                    v.setMinDistance(distanceThroughU);
                    v.setPrevious(u);
                    v.setParent(u); //Adicionando o Pai do WSNSensor
                    vertexQueue.add(v);
                }
            }
        }
    }

    private static List<WSNSensor> getShortestPathTo(WSNSensor target) {
        List<WSNSensor> path = new ArrayList<>();
        for (WSNSensor vertex = target; vertex != null; vertex = vertex.getPrevious()) {
            path.add(vertex);
        }
        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args) {

        WSNSensor s0 = new WSNSensor(0, 1, 1, 15, 0.25);
        WSNSensor s1 = new WSNSensor(1, 1, 5, 15, 0.25);
        WSNSensor s2 = new WSNSensor(2, 5, 1, 15, 0.25);
        WSNSensor s3 = new WSNSensor(3, 5, 5, 15, 0.25);
        WSNSensor s4 = new WSNSensor(4, 5, 15, 15, 0.25);


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


        WSNSensor[] vertices = {s0, s1, s2, s3, s4};
        computePaths(s0);

        for (WSNSensor v : vertices) {
            System.out.println("Distance to " + v + ": " + v.getMinDistance());
            System.out.println("WSNSensor: " + v + " - tem o pai o WSNSensor: " + v.getParent());
            List<WSNSensor> path = getShortestPathTo(v);
            System.out.println("Path: " + path);
        }
    }
}