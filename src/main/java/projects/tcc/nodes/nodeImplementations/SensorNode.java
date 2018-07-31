package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.impl.WSNSensor;
import sinalgo.gui.transformation.PositionTransformation;

import java.awt.*;

public class SensorNode extends SimulationNode {

    @Getter
    private Sensor sensor;

    @Override
    public void init() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        this.sensor = new WSNSensor((int) this.getID() - 1,
                this.getPosition().getXCoord(), this.getPosition().getYCoord(),
                config.getSensorRadius(), config.getCommRadius(), config.getBatteryEnergy(),
                config.getActivationPower(), config.getReceivePower(),
                config.getMaintenancePower(), config.getCommRatio());
        SensorNetwork.currentInstance().addSensors(this.getSensor());
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        this.setColor(this.getSensor().isFailed() ? Color.RED : this.getSensor().isActive() ? Color.GREEN : Color.BLACK);
        super.drawAsDisk(g, pt, highlight, this.getSensor().isFailed() ? 10 : this.getSensor().isActive() ? 20 : 10);
    }
}
