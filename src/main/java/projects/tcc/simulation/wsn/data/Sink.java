package projects.tcc.simulation.wsn.data;

import projects.tcc.nodes.nodeImplementations.SinkNode;

public class Sink extends Sensor {

    public Sink(int id, double x, double y, double commRatio) {
        super(id, x, y, 25, commRatio);
    }

    public Sink(int id, double x, double y, double commRatio, SinkNode node) {
        this(id, x, y, commRatio);
        this.setNode(node);
    }

}
