package projects.tcc.simulation.algorithms.graph;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.java.Log;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log
@Getter(AccessLevel.PRIVATE)
public class Graph {

    @Getter
    @RequiredArgsConstructor
    public static class Edge<K> {
        private final Node<K> target;
        private final double weight;
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    public static class Node<T> {
        private final T source;
        private final List<Edge<T>> edges = new ArrayList<>();
        private double minDistance = Double.POSITIVE_INFINITY;
        private Node<T> previous;

        public T getPreviousSource() {
            return this.getPrevious() == null ? null : this.getPrevious().getSource();
        }
    }

    private interface ThreeOperandFunction<T, U, W, R> {
        R apply(T t, U u, W w);
    }

    private final static double PENALTY = 2500;
    private final static double PENALTY_SQUARED = PENALTY * PENALTY;

    @Getter
    private final Map<Sensor, Node<Sensor>> sensorNodeMap;
    private final List<Sensor> sensorSinkList;

    public Graph(List<Sensor> sensors) {
        this.sensorSinkList = sensors;
        this.sensorNodeMap = new HashMap<>();
    }

    public void computeEdges(boolean usePenalties) {
        if (usePenalties) {
            this.computeEdges((d, d2, neighborData) -> {
                Sensor s = d.getSource();
                Sensor s2 = d2.getSource();
                double weight = neighborData.getCurrent();
                if (s.isActive() ^ s2.isActive()) {
                    weight = weight * PENALTY;
                } else if (!s.isActive() && !s2.isActive()) {
                    weight = weight * PENALTY_SQUARED;
                }
                return weight;
            });
        } else {
            this.computeEdges((s, s2, neighborData) -> neighborData.getCurrent());
        }
    }

    private void computeEdges(ThreeOperandFunction<Node<Sensor>, Node<Sensor>, Sensor.NeighborData, Double> weightFunction) {
        this.getSensorNodeMap().clear();
        for (Sensor s : this.getSensorSinkList()) {
            if (s.isAvailable()) {
                Node<Sensor> d = this.getOrCreate(s);
                s.getNeighborhood().forEach((s2, neighborData) -> {
                    if (s2.isAvailable()) {
                        Node<Sensor> d2 = this.getOrCreate(s2);
                        d.getEdges().add(new Edge<>(d2, weightFunction.apply(d, d2, neighborData)));
                    }
                });
            }
        }
    }

    private Node<Sensor> getOrCreate(Sensor s) {
        Node<Sensor> d = this.getSensorNodeMap().get(s);
        if (d == null) {
            d = new Node<>(s);
            this.getSensorNodeMap().put(s, d);
        }
        return d;
    }

    public void computeMinimalPathsTo(Sink sink) {
        Dijkstra.computePaths(this.getSensorNodeMap().get(sink));
        this.getSensorNodeMap().forEach((s, node) -> {
            s.setCostToSink(node.getMinDistance());
            s.setParent(node.getPreviousSource());
        });
    }

    public TreeNode<Sensor> getTreeRepresentation(Sink sink) {
        Dijkstra.computePaths(this.getSensorNodeMap().get(sink));
        Map<Sensor, TreeNode<Sensor>> treeNodes = new HashMap<>(this.getSensorNodeMap().size());
        this.getSensorNodeMap().keySet().forEach(s -> treeNodes.put(s, new TreeNode<>(s)));
        this.getSensorNodeMap().values().forEach(n -> {
            TreeNode<Sensor> parent = treeNodes.get(n.getPreviousSource());
            TreeNode<Sensor> current = treeNodes.get(n.getSource());
            current.setParent(parent);
            if (parent != null) {
                parent.getChildren().add(current);
            }
        });
        for (TreeNode<Sensor> t : treeNodes.values()) {
            if (t.getParent() == null) {
                return t;
            }
        }
        return null;
    }

}
