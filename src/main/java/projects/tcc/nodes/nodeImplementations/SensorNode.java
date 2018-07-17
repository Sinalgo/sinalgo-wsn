package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import lombok.experimental.Delegate;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.simulation.rssf.sensor.Sensor;
import projects.tcc.simulation.rssf.sensor.SensorPosition;

public class SensorNode extends SimulationNode implements Sensor {

    @Getter
    @Delegate(types = Sensor.class, excludes = SensorPosition.class)
    private Sensor sensor;
}
