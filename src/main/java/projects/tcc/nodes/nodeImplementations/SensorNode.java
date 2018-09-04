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
    private long totalReceivedMessages;

    @Getter(AccessLevel.PROTECTED)
    private long totalSentMessages;

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
    public boolean isConnected() {
        return this.getSensor().isConnected();
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
            this.handleMessageSending(MessageCache::pop);
            this.handleMessageReceiving(inbox);
        }
    }

    private void handleMessageReceiving(Inbox inbox) {
        this.handleActivationMessages(inbox);
        while (inbox.hasNext()) {
            Message m = inbox.next();
            if (m instanceof SimulationMessage) {
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
        this.getSensor().drawReceiveEnergy();
        this.handleMessageSending(() -> m);
    }

    private void handleMessageReceiving(ActivationMessage m) {
        this.getSensor().drawReceiveEnergy();
        this.active = m.isActive();
        this.parent = m.getParent();
    }

    private void handleMessageSending(Supplier<SimulationMessage> m) {
        for (Edge e : this.getOutgoingConnections()) {
            if (e.getEndNode() instanceof SimulationNode) {
                this.getSensor().drawTransmitEnergy(((SimulationNode) e.getEndNode()).getSensor());
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
        this.active = false;
        this.getSensor().deactivate();
    }

    @NodePopupMethod(menuText = "Force failure")
    public void fail() {
        this.getSensor().fail();
    }

    public boolean isActive() {
        return this.active && !this.isFailed();
    }

}
