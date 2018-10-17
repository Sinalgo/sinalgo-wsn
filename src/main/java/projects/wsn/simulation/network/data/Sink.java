package projects.wsn.simulation.network.data;

import projects.wsn.nodes.nodeImplementations.SinkNode;
import sinalgo.nodes.Position;

public class Sink extends Sensor {

    public Sink(int id, Position position, double commRadius, SinkNode node) {
        super(id, position, commRadius);
        this.setNode(node);
    }

}
