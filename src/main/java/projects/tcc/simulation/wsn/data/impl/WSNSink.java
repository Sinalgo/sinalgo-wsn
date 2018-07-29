package projects.tcc.simulation.wsn.data.impl;

import projects.tcc.simulation.wsn.data.Sink;

public class WSNSink extends WSNSensor implements Sink {

    public WSNSink(int id, double x, double y, double commRatio) {
        super(id, x, y, 25, commRatio);
    }

}
