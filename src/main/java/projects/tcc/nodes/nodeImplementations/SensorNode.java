package projects.tcc.nodes.nodeImplementations;

import lombok.AccessLevel;
import lombok.Getter;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.nodes.messages.SimulationMessage;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.impl.WSNSensor;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

import java.awt.*;

public class SensorNode extends SimulationNode {

    @Getter(AccessLevel.PROTECTED)
    private long totalReceivedMessages;

    @Getter(AccessLevel.PROTECTED)
    private long totalSentMessages;

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

    @Override
    public void handleMessages(Inbox inbox) {
        for (Edge e : this.getOutgoingConnections()) {
            if (e.getEndNode() instanceof SensorNode &&
                    ((SensorNode) e.getEndNode()).getSensor().equals(this.getSensor().getParent())) {
                sendMessage(new SimulationMessage(), e);
                break;
            }
        }
        while (inbox.hasNext()) {
            Message m = inbox.next();
            if (m instanceof SimulationMessage && inbox.getSender() instanceof SensorNode) {
                this.totalReceivedMessages++;
                if (this.getSensor().getParent() == null ||
                        ((SensorNode) inbox.getSender()).getSensor().equals(this.getSensor().getParent())) {
                    continue;
                }
                for (Edge e : this.getOutgoingConnections()) {
                    if (e.getEndNode() instanceof SensorNode &&
                            ((SensorNode) e.getEndNode()).getSensor().equals(this.getSensor().getParent())) {
                        sendMessage(m, e);
                        break;
                    }
                }
            }
        }
    }

    private void sendMessage(Message m, Edge e) {
        this.totalSentMessages++;
        ((SimulationMessage) m).getNodes().push(this.toString());
        this.send(m, e.getEndNode());
    }

}
