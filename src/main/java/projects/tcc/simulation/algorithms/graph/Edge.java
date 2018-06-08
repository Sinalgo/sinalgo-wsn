package projects.tcc.simulation.algorithms.graph;

import lombok.Data;
import projects.tcc.simulation.rssf.Sensor;

@Data
public class Edge {
    private final Sensor target;
    private final double weight;
}