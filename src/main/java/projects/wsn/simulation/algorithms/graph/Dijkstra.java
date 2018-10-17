package projects.wsn.simulation.algorithms.graph;

import projects.wsn.simulation.network.data.Sensor;
import sinalgo.nodes.Position;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

public class Dijkstra {

    public static void computePaths(Graph.Node<Sensor> source) {
        source.setMinDistance(0);
        PriorityQueue<Graph.Node<Sensor>> vertexQueue
                = new PriorityQueue<>(Comparator.comparingDouble(Graph.Node::getMinDistance));
        vertexQueue.add(source);

        while (!vertexQueue.isEmpty()) {
            Graph.Node<Sensor> u = vertexQueue.poll();

            // Visit each edge exiting u
            for (Graph.Edge<Sensor> e : u.getEdges()) {
                Graph.Node<Sensor> v = e.getTarget();
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

    private static List<Graph.Node> getShortestPathTo(Graph.Node target) {
        List<Graph.Node> path = new ArrayList<>();
        for (Graph.Node vertex = target; vertex != null; vertex = vertex.getPrevious()) {
            path.add(vertex);
        }
        Collections.reverse(path);
        return path;
    }

    public static void main(String[] args) {

        Sensor s0 = new Sensor(0, new Position(1, 1, 0), 15);
        Sensor s1 = new Sensor(1, new Position(1, 5, 0), 15);
        Sensor s2 = new Sensor(2, new Position(5, 1, 0), 15);
        Sensor s3 = new Sensor(3, new Position(5, 5, 0), 15);
        Sensor s4 = new Sensor(4, new Position(5, 15, 0), 15);

        Graph g = new Graph(Collections.emptyList());

        Graph.Node<Sensor> d0 = new Graph.Node<>(s0);
        Graph.Node<Sensor> d1 = new Graph.Node<>(s1);
        Graph.Node<Sensor> d2 = new Graph.Node<>(s2);
        Graph.Node<Sensor> d3 = new Graph.Node<>(s3);
        Graph.Node<Sensor> d4 = new Graph.Node<>(s4);

        Arrays.asList(d0, d1, d2, d3, d4).forEach(d -> g.getSensorNodeMap().put(d.getSource(), d));

        d0.getEdges().add(new Graph.Edge<>(d1, 5));
        d0.getEdges().add(new Graph.Edge<>(d2, 10));
        d0.getEdges().add(new Graph.Edge<>(d3, 9));
        d0.getEdges().add(new Graph.Edge<>(d3, 8));

        d1.getEdges().add(new Graph.Edge<>(d1, 5));
        d1.getEdges().add(new Graph.Edge<>(d2, 3));
        d1.getEdges().add(new Graph.Edge<>(d4, 7));

        d2.getEdges().add(new Graph.Edge<>(d0, 10));
        d2.getEdges().add(new Graph.Edge<>(d1, 3));

        d3.getEdges().add(new Graph.Edge<>(d0, 8));
        d3.getEdges().add(new Graph.Edge<>(d4, 2));

        d4.getEdges().add(new Graph.Edge<>(d1, 7));
        d4.getEdges().add(new Graph.Edge<>(d3, 2));

        computePaths(d0);

        for (Graph.Node v : g.getSensorNodeMap().values()) {
            System.out.println("Distance to " + v.getSource() + ": " + v.getMinDistance());
            System.out.println("Sensor: " + v.getSource() + " - tem o pai o Sensor: "
                    + (v.getPrevious() == null ? null : v.getPrevious().getSource()));
            List<Graph.Node> path = getShortestPathTo(v);
            System.out.println("Path: " + path.stream()
                    .map(Graph.Node<Sensor>::getSource)
                    .collect(Collectors.toList()));
        }
    }
}