package projects.tcc.simulation.graph;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import projects.tcc.simulation.data.SensorHolder;
import projects.tcc.simulation.rssf.sensor.Sensor;
import projects.tcc.simulation.rssf.sensor.Sink;

import java.util.List;


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
        SensorHolder.getAllSensorsAndSinks().values().forEach(s -> getGraph().addVertex(s.getID()));
        SensorHolder.getAllSensorsAndSinks().values().forEach(s -> s.getNeighbors().keySet()
                .forEach(s2 -> getGraph().addEdge(s.getID(), s2)));
        setInitialized(true);
    }

    private static void updateConnections() {
        SensorHolder.getAvailableSensors().values().forEach(s -> s.getGraphNodeProperties().reset());
        SensorHolder.getSinks().values().forEach(s -> s.getGraphNodeProperties().reset());
        SensorHolder.getFailedSensors().keySet().forEach(graph::removeVertex);
        SensorHolder.getAvailableSensors().values().forEach(s -> {
            if (getGraph().containsVertex(s.getID())) {
                s.getNeighbors().keySet().forEach(neighborId -> {
                    Sensor neighbor = SensorHolder.getAllSensorsAndSinks().get(neighborId);
                    double weight = Sensor.getCurrentForDistance(s.getDistances().get(neighborId));
                    if ((s.isActive() && !neighbor.isActive()) || (!s.isActive() && neighbor.isActive())) {
                        weight = weight * PENALTY;
                    } else if (!s.isActive() && !neighbor.isActive()) {
                        weight = weight * PENALTY_SQUARED;
                    }
                    graph.setEdgeWeight(graph.getEdge(s.getID(), neighbor.getID()), weight);
                });
            }
        });
    }

    private static void updateCostsToSink() {
        DijkstraShortestPath<Long, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(graph);
        SensorHolder.getAvailableSensors().values().forEach(s -> SensorHolder.getSinks().values()
                .forEach(s2 -> {
                    GraphPath<Long, DefaultWeightedEdge> path = dijkstra.getPath(s.getID(), s2.getID());
                    if (path != null) {
                        setPathToSinkCost(s, s2, path);
                        setParentSensor(s, path);
                    }
                }));
    }

    private static void setParentSensor(Sensor s, GraphPath<Long, DefaultWeightedEdge> path) {
        List<Long> vertexList = path.getVertexList();
        s.getGraphNodeProperties().setParentId(vertexList.size() <= 1 ? null : vertexList.get(1));
    }

    private static void setPathToSinkCost(Sensor s, Sink s2, GraphPath<Long, DefaultWeightedEdge> path) {
        s.getGraphNodeProperties().getPathToSinkCost().put(s2.getID(), path.getWeight());
    }

}
