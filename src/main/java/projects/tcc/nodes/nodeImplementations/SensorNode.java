package projects.tcc.nodes.nodeImplementations;

import lombok.AccessLevel;
import lombok.Getter;
import projects.tcc.MessageCache;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.nodes.messages.SimulationMessage;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        this.sensor = new Sensor((int) this.getID() - 1,
                this.getPosition().getXCoord(), this.getPosition().getYCoord(),
                config.getSensorRadius(), config.getCommRadius(), config.getBatteryEnergy(),
                config.getActivationPower(), config.getReceivePower(),
                config.getMaintenancePower(), config.getCommRatio());
        SensorNetwork.currentInstance().addSensors(this.getSensor());
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        this.setColor(this.getSensor().isFailed() ? Color.RED : this.getSensor().isActive() ? Color.GREEN : Color.BLACK);
        this.setDefaultDrawingSizeInPixels(this.getSensor().isFailed() ? 10 : this.getSensor().isActive() ? 20 : 10);
        this.superDraw(g, pt, highlight);
    }

    protected void superDraw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.draw(g, pt, highlight);
    }

    @Override
    public void handleMessages(Inbox inbox) {
        for (Edge e : this.getOutgoingConnections()) {
            if (e.getEndNode() instanceof SensorNode) {
                sendMessage(MessageCache.pop(), e);
            }
        }
        while (inbox.hasNext()) {
            Message m = inbox.next();
            if (m instanceof SimulationMessage && inbox.getSender() instanceof SensorNode) {
                this.totalReceivedMessages++;
                for (Edge e : this.getOutgoingConnections()) {
                    if (e.getEndNode() instanceof SensorNode) {
                        sendMessage((SimulationMessage) m, e);
                    }
                }
            }
        }
    }

    @Override
    public void preStep() {
        for (Sensor child : this.getSensor().getChildren()) {
            this.consecutiveChildrenFailures.putIfAbsent(child, new AtomicInteger());
        }
    }

    private void sendMessage(SimulationMessage m, Edge e) {
        this.totalSentMessages++;
        m.getNodes().push(this);
        this.send(m, e.getEndNode());
    }

}
