package projects.tcc.simulation.algorithms.graph;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import projects.tcc.simulation.data.SensorHolder;
import projects.tcc.simulation.rssf.Sensor;


public class GraphHolder {

    private static final double PENALTY = 2500;
    private static final double PENALTY_SQUARED = PENALTY * PENALTY;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static DefaultDirectedWeightedGraph<Long, DefaultWeightedEdge> graph;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private static boolean initialized = false;

    public static void update() {
        update(!isInitialized());
        setInitialized(true);
    }

    public static void update(boolean reset) {
        if (reset) {
            init();
        }
        updateConnections();
        updateCostsToSink();
    }

    private static void init() {
        setGraph(new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class));
        SensorHolder.getAvailableSensors().values().forEach(s -> getGraph().addVertex(s.getID()));
        SensorHolder.getAvailableSensors().values().forEach(s -> s.getNeighbors().keySet()
                .forEach(s2 -> getGraph().addEdge(s.getID(), s2)));
    }

    private static void updateConnections() {
        SensorHolder.getAvailableSensors().values().forEach(s -> {
            if (getGraph().containsVertex(s.getID())) {
                if (!s.isFailed()) {
                    s.getNeighbors().forEach((neighborId, distance) -> {
                        Sensor neighbor = SensorHolder.getAllSensorsAndSinks().get(neighborId);
                        if (!neighbor.isFailed()) {
                            double weight = s.getCurrentForDistance(distance);

                            if ((s.isActive() && !neighbor.isActive()) || (!s.isActive() && neighbor.isActive())) {
                                weight = weight * PENALTY;
                            } else if (!s.isActive() && !neighbor.isActive()) {
                                weight = weight * PENALTY_SQUARED;
                            }

                            graph.setEdgeWeight(graph.getEdge(s.getID(), neighbor.getID()), weight);
                        }
                    });
                }
            }
        });
    }

    private static void updateCostsToSink() {
        DijkstraShortestPath<Long, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(graph);
        SensorHolder.getAvailableSensors().values().forEach(s -> SensorHolder.getSinks().values()
                .forEach(s2 -> s.getPathToSinkCost().put(s2.getID(), dijkstra.getPathWeight(s.getID(), s2.getID()))));
    }

}
