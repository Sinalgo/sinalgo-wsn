package projects.tcc.simulation.algorithms.graph;

import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import projects.tcc.simulation.rssf.Sensor;
import projects.tcc.simulation.rssf.Sink;

import java.util.List;


public class GraphHolder {

    private static final double PENALTY = 2500;

    private DefaultDirectedWeightedGraph<Long, DefaultWeightedEdge> graph;

    private List<Sensor> sensors;
    private Sink sink;
    private double[][] connectivityMatrix;

    public GraphHolder(List<Sensor> sensors, Sink sink, double[][] connectivityMatrix) {
        this.sensors = sensors;
        this.sink = sink;
        this.connectivityMatrix = connectivityMatrix;
    }

    public void update() {
        if (this.graph == null) {
            init();
        }
        updateConnections();
        updateCostsToSink();
    }

    private void init() {
        this.graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        this.sensors.forEach(s -> this.graph.addVertex(s.getID()));
        this.sensors.forEach(s -> s.getNeighbors().forEach(s2 -> this.graph.addEdge(s.getID(), s2.getID())));
    }

    private void updateConnections() {
        this.sensors.forEach(s -> {
            if (this.graph.containsVertex(s.getID())) {
                if (!s.isFailed()) {
                    s.getNeighbors().forEach(s2 -> {
                        if (!s2.isFailed()) {
                            double distance = connectivityMatrix[(int) s.getID()][(int) s2.getID()];
                            double weight = s.queryDistances(distance);

                            if ((s.isActive() && !s2.isActive()) || (!s.isActive() && s2.isActive())) {
                                weight = weight * PENALTY;
                            } else if (!s.isActive() && !s2.isActive()) {
                                weight = weight * PENALTY * PENALTY;
                            }

                            graph.setEdgeWeight(graph.getEdge(s.getID(), s2.getID()), weight);
                        }
                    });
                } else {
                    graph.removeVertex(s.getID());
                }
            }
        });
    }

    private void updateCostsToSink() {
        DijkstraShortestPath<Long, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(graph);
        sensors.forEach(s -> s.setMinDistance(dijkstra.getPathWeight(s.getID(), sink.getID())));
    }

}
