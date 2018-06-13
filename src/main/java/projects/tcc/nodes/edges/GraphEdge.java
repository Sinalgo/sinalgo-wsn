package projects.tcc.nodes.edges;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import sinalgo.nodes.edges.Edge;

@Getter
@Setter
@ToString(callSuper = true)
public class GraphEdge extends Edge {
    private double weight;
}
