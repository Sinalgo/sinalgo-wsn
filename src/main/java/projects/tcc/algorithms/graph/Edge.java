package projects.tcc.algorithms.graph;

import lombok.Data;
import projects.tcc.rssf.Sensor;

@Data
public class Edge {
    private final Sensor target;
    private final double weight;
}