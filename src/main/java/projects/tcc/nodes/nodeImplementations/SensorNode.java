package projects.tcc.nodes.nodeImplementations;

import lombok.AccessLevel;
import lombok.Getter;
import projects.tcc.MessageCache;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.nodes.messages.ActivationMessage;
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
import java.util.function.Supplier;

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
                config.getMaintenancePower(), config.getCommRatio(), this);
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
        if (this.getSensor().isActive()) {
            this.handleMessageSending(MessageCache::pop);
            this.handleMessageReceiving(inbox);
        }
    }

    private void handleMessageReceiving(Inbox inbox) {
        this.handleActivationMessages(inbox);
        while (inbox.hasNext()) {
            Message m = inbox.next();
            if (inbox.getSender() instanceof SensorNode && m instanceof SimulationMessage) {
                this.totalReceivedMessages++;
                this.handleMessageReceiving((SimulationMessage) m);
            }
        }
    }

    private void handleActivationMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            Message m = inbox.next();
            if (m instanceof ActivationMessage) {
                this.handleMessageReceiving((ActivationMessage) m);
                break;
            }
        }
        inbox.reset();
    }

    protected void handleMessageReceiving(SimulationMessage m) {
        this.handleMessageSending(() -> m);
    }

    private void handleMessageReceiving(ActivationMessage m) {
        this.getSensor().setActive(m.isActiveSensor());
    }

    private void handleMessageSending(Supplier<SimulationMessage> m) {
        for (Edge e : this.getOutgoingConnections()) {
            if (e.getEndNode() instanceof SensorNode) {
                sendMessage(m.get(), e);
            }
        }
    }

    private void sendMessage(SimulationMessage m, Edge e) {
        this.totalSentMessages++;
        m.getNodes().push(this);
        this.send(m, e.getEndNode());
    }

    @NodePopupMethod(menuText = "Deactivate")
    public void deactivate() {
        this.getSensor().setActive(false);
    }

    @NodePopupMethod(menuText = "Force failure")
    public void fail() {
        this.getSensor().setFailed(true);
        this.getSensor().setActive(false);
    }

}
