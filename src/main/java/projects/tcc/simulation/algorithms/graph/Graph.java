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
    @Setter
    @RequiredArgsConstructor
    public static class Data<T> {

        @Getter
        @RequiredArgsConstructor
        public static class Edge<K> {
            private final Data<K> target;
            private final double weight;
        }

        private final T source;
        private final List<Edge<T>> edges = new ArrayList<>();
        private double minDistance = Double.POSITIVE_INFINITY;
        private Data<T> previous;
    }

    private interface ThreeOperandFunction<T, U, W, R> {
        R apply(T t, U u, W w);
    }

    private final static double PENALTY = 2500;
    private final static double PENALTY_SQUARED = PENALTY * PENALTY;

    @Getter
    private final Map<Sensor, Data<Sensor>> graphData;
    private final List<Sensor> sensorSinkList;

    public Graph(List<Sensor> sensorSinkList) {
        this.sensorSinkList = sensorSinkList;
        this.graphData = new HashMap<>();
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

    private void computeEdges(ThreeOperandFunction<Data<Sensor>, Data<Sensor>, Sensor.NeighborData, Double> weightFunction) {
        this.getGraphData().clear();
        for (Sensor s : this.getSensorSinkList()) {
            if (s.isAvailable()) {
                Data<Sensor> d = this.getOrCreate(s);
                s.getNeighborhood().forEach((s2, neighborData) -> {
                    if (s2.isAvailable()) {
                        Data<Sensor> d2 = this.getOrCreate(s2);
                        d.getEdges().add(new Data.Edge<>(d2, weightFunction.apply(d, d2, neighborData)));
                    }
                });
            }
        }
    }

    private Data<Sensor> getOrCreate(Sensor s) {
        Data<Sensor> d = this.getGraphData().get(s);
        if (d == null) {
            d = new Data<>(s);
            this.getGraphData().put(s, d);
        }
        return d;
    }

    public void computeMinimalPathsTo(Sink sink) {
        Dijkstra.computePaths(this.getGraphData().get(sink));
        this.getGraphData().forEach((s, data) -> {
            s.setCostToSink(data.getMinDistance());
            s.setParent(data.getPrevious() == null ? null : data.getPrevious().getSource());
        });
    }

}
