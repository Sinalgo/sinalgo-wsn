package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import lombok.experimental.Delegate;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.simulation.io.ConfigurationLoader;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.wsn.data.Sink;
import projects.tcc.simulation.wsn.data.impl.WSNSink;

public class SinkNode extends SensorNode implements Sink {

    @Getter
    @Delegate(types = Sink.class, excludes = SimulationNode.class)
    private Sink sensor;

    @Override
    public void init() {
        SimulationConfiguration config = ConfigurationLoader.getConfiguration();
        this.sensor = new WSNSink((int) this.getID(), this.getPosition().getXCoord(),
                this.getPosition().getYCoord(), config.getCommRadius());
    }
}
