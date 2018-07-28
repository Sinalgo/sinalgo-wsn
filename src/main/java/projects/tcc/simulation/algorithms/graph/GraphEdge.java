package projects.tcc.simulation.algorithms.graph;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import projects.tcc.simulation.wsn.data.WSNSensor;

@Getter
@RequiredArgsConstructor
public class GraphEdge {
    private final WSNSensor target;
    private final double weight;
}