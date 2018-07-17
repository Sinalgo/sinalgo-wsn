package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import lombok.experimental.Delegate;
import projects.tcc.simulation.rssf.sensor.SensorPosition;
import projects.tcc.simulation.rssf.sensor.Sink;
import projects.tcc.simulation.rssf.sensor.impl.RSSFSink;

public class SinkNode extends SensorNode implements Sink {

    @Getter
    @Delegate(types = Sink.class, excludes = SensorPosition.class)
    private Sink sensor = new RSSFSink();
}
