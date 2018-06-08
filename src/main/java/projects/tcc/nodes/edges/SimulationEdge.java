package projects.tcc.nodes.edges;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import sinalgo.nodes.edges.Edge;

@Getter
@ToString(callSuper = true)
@RequiredArgsConstructor
public class SimulationEdge extends Edge {
    private final double weight;
}
