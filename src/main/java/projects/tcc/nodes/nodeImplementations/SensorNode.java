package projects.tcc.nodes.nodeImplementations;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.MessageCache;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.nodes.messages.ActivationMessage;
import projects.tcc.nodes.messages.SimulationMessage;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.SensorIndex;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

import java.awt.*;
import java.util.function.Supplier;

@Getter
public class SensorNode extends SimulationNode {

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    private long totalReceivedMessages;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    private long totalSentMessages;

    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.NONE)
    private boolean receivedActivationMessage;

    private Sensor sensor;
    private boolean active;
    private SimulationNode parent;

    @Override
    public void init() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        int index = SensorIndex.currentInstance().getNextIndex(Sensor.class);
        this.sensor = new Sensor(index, this.getPosition(),
                config.getSensorRadius(), config.getCommRadius(), config.getBatteryEnergy(),
                config.getActivationPower(), config.getReceivePower(),
                config.getMaintenancePower(), config.getCommRatio(),
                config.getMinBatteryThreshold(), this);
        SensorNetwork.currentInstance().addSensor(this.getSensor());
    }

    @Override
    public boolean isFailed() {
        return this.getSensor().isFailed();
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        this.setColor(this.isFailed() ? Color.RED :
                this.isActive() ?
                        this.getSensor().isActive() ? Color.GREEN : Color.YELLOW :
                        this.getSensor().isActive() ? Color.ORANGE : Color.BLACK);
        this.setDefaultDrawingSizeInPixels(this.isFailed() ? 10 : this.isActive() || this.getSensor().isActive() ? 20 : 10);
        this.superDraw(g, pt, highlight);
    }

    protected void superDraw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.draw(g, pt, highlight);
    }

    @Override
    public void handleMessages(Inbox inbox) {
        if (this.getSensor().isAvailable()) {
            this.handleMessageReceiving(inbox);
            this.handleMessageSending(MessageCache::pop);
        }
    }

    private void handleMessageReceiving(Inbox inbox) {
        this.handleActivationMessages(inbox);
        while (inbox.hasNext()) {
            Message m = inbox.next();
            if (m instanceof SimulationMessage) {
                this.totalReceivedMessages++;
                this.getSensor().drawReceiveEnergy();
                this.handleMessageReceiving((SimulationMessage) m);
            }
        }
    }

    private void handleActivationMessages(Inbox inbox) {
        while (inbox.hasNext()) {
            Message m = inbox.next();
            if (m instanceof ActivationMessage) {
                this.totalReceivedMessages++;
                this.getSensor().drawReceiveEnergy();
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
        this.receivedActivationMessage = true;
        this.active = m.isActive();
        this.parent = m.getParent();
    }

    private void handleMessageSending(Supplier<SimulationMessage> m) {
        if (this.isActive()
                && !this.isReceivedActivationMessage()
                && this.getOutgoingConnections().size() == 0) {
            this.sendMessage(m, this.getParent());
        }
        for (Edge e : this.getOutgoingConnections()) {
            if (e.getEndNode() instanceof SimulationNode) {
                this.sendMessage(m, (SimulationNode) e.getEndNode());
            }
        }
    }

    protected void sendMessage(Supplier<SimulationMessage> m, SimulationNode n) {
        this.totalSentMessages++;
        this.getSensor().drawTransmitEnergy(n.getSensor());
        SimulationMessage message = m.get();
        message.getNodes().push(this);
        this.send(message, n);
    }

    @NodePopupMethod(menuText = "Deactivate")
    public void deactivate() {
        this.active = false;
        this.getSensor().deactivate();
    }

    @NodePopupMethod(menuText = "Force failure")
    public void fail() {
        this.getSensor().fail();
    }

    @Override
    public boolean isActive() {
        return this.active && !this.isFailed();
    }

    @Override
    public void preStep() {
        this.receivedActivationMessage = false;
    }

}
