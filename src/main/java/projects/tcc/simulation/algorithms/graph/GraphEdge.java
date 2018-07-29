package projects.tcc.simulation.algorithms.graph;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import projects.tcc.simulation.wsn.data.Sensor;

@Getter
@RequiredArgsConstructor
public class GraphEdge {
    private final Sensor target;
    private final double weight;
}