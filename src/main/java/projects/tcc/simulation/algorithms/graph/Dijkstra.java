package projects.tcc.simulation.algorithms.graph;

import projects.tcc.simulation.wsn.data.Sensor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class Dijkstra {

    public static void computePaths(Graph.Data<Sensor> source) {
        source.setMinDistance(0);
        PriorityQueue<Graph.Data<Sensor>> vertexQueue
                = new PriorityQueue<>(Comparator.comparingDouble(Graph.Data::getMinDistance));
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            Graph.Data<Sensor> u = vertexQueue.poll();

            // Visit each edge exiting u
            for (Graph.Data.Edge<Sensor> e : u.getEdges()) {
                Graph.Data<Sensor> v = e.getTarget();
                double weight = e.getWeight();
                double distanceThroughU = u.getMinDistance() + weight;
                if (Double.compare(distanceThroughU, v.getMinDistance()) < 0) {
                    vertexQueue.remove(v);
                    v.setMinDistance(distanceThroughU);
                    v.setPrevious(u);
                    vertexQueue.add(v);
                }
            }
        }
    }

    private static List<Graph.Data> getShortestPathTo(Graph.Data target) {
        List<Graph.Data> path = new ArrayList<>();
        for (Graph.Data vertex = target; vertex != null; vertex = vertex.getPrevious()) {
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

        Graph g = new Graph(Collections.emptyList());

        Graph.Data<Sensor> d0 = new Graph.Data<>(s0);
        Graph.Data<Sensor> d1 = new Graph.Data<>(s1);
        Graph.Data<Sensor> d2 = new Graph.Data<>(s2);
        Graph.Data<Sensor> d3 = new Graph.Data<>(s3);
        Graph.Data<Sensor> d4 = new Graph.Data<>(s4);

        Arrays.asList(d0, d1, d2, d3, d4).forEach(d -> g.getGraphData().put(d.getSource(), d));

        d0.getEdges().add(new Graph.Data.Edge<>(d1, 5));
        d0.getEdges().add(new Graph.Data.Edge<>(d2, 10));
        d0.getEdges().add(new Graph.Data.Edge<>(d3, 9));
        d0.getEdges().add(new Graph.Data.Edge<>(d3, 8));

        d1.getEdges().add(new Graph.Data.Edge<>(d1, 5));
        d1.getEdges().add(new Graph.Data.Edge<>(d2, 3));
        d1.getEdges().add(new Graph.Data.Edge<>(d4, 7));

        d2.getEdges().add(new Graph.Data.Edge<>(d0, 10));
        d2.getEdges().add(new Graph.Data.Edge<>(d1, 3));

        d3.getEdges().add(new Graph.Data.Edge<>(d0, 8));
        d3.getEdges().add(new Graph.Data.Edge<>(d4, 2));

        d4.getEdges().add(new Graph.Data.Edge<>(d1, 7));
        d4.getEdges().add(new Graph.Data.Edge<>(d3, 2));

        computePaths(d0);

        for (Graph.Data v : g.getGraphData().values()) {
            System.out.println("Distance to " + v.getSource() + ": " + v.getMinDistance());
            System.out.println("Sensor: " + v.getSource() + " - tem o pai o Sensor: "
                    + (v.getPrevious() == null ? null : v.getPrevious().getSource()));
            List<Graph.Data> path = getShortestPathTo(v);
            System.out.println("Path: " + path.stream()
                    .map(Graph.Data<Sensor>::getSource)
                    .collect(Collectors.toList()));
        }
    }
}