package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import lombok.experimental.Delegate;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.impl.WSNSensor;

public class SensorNode extends SimulationNode implements Sensor {

    @Getter
    @Delegate(types = Sensor.class, excludes = SimulationNode.class)
    private Sensor sensor;

    @Override
    public void init() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        this.sensor = new WSNSensor((int) this.getID() - 1,
                this.getPosition().getXCoord(), this.getPosition().getYCoord(),
                config.getSensorRadius(), config.getCommRadius(), config.getBatteryEnergy(),
                config.getActivationPower(), config.getReceivePower(),
                config.getMaintenancePower(), config.getCommRatio());
        SensorNetwork.currentInstance().addSensors(this);
    }
}
