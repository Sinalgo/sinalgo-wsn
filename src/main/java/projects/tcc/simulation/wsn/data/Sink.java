package projects.tcc.simulation.wsn.data;

import projects.tcc.nodes.nodeImplementations.SinkNode;
import sinalgo.nodes.Position;

public class Sink extends Sensor {

    public Sink(int id, Position position, double commRadius, SinkNode node) {
        super(id, position, commRadius);
        this.setNode(node);
    }

}
