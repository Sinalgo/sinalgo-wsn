package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import lombok.experimental.Delegate;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sink;
import projects.tcc.simulation.wsn.data.impl.WSNSink;

public class SinkNode extends SensorNode implements Sink {

    @Getter
    @Delegate(types = Sink.class, excludes = SimulationNode.class)
    private Sink sensor;

    @Override
    public void init() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        this.sensor = new WSNSink((int) this.getID() - 1, this.getPosition().getXCoord(),
                this.getPosition().getYCoord(), config.getCommRadius());
        SensorNetwork.getCurrentInstance().addSinks(this);
    }
}
