package projects.tcc.nodes.nodeImplementations;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.MessageCache;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.nodes.messages.ActivationMessage;
import projects.tcc.nodes.messages.FailureMessage;
import projects.tcc.nodes.messages.ForwardedMessage;
import projects.tcc.nodes.messages.SimulationMessage;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.SensorIndex;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;

import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

@Getter
public class SensorNode extends SimulationNode {

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    private long totalReceivedMessages;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.NONE)
    private long totalSentMessages;

    private Sensor sensor;
    private boolean active;
    private SimulationNode parent;
    private List<SimulationNode> children;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private int waitTime;

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
                this.isSleep() ? Color.GRAY :
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
    public void handleNAckMessages(NackBox nackBox) {
        if (nackBox.hasNext() && this.isActive()) {
            for (SimulationNode n : this.getChildren()) {
                this.sendMessage(new FailureMessage(), n);
            }
        }
    }

    @Override
    public void handleMessages(Inbox inbox) {
        if (this.isSleep()) {
            return;
        }
        if (this.getSensor().isAvailable()) {
            if (this.isActive()) {
                this.sendMessage(MessageCache::pop, this.getParent());
            }
            this.handleMessageReceiving(inbox);
        }
    }

    private void handleMessageReceiving(Inbox inbox) {
        this.incrementTotalReceivedMessages(inbox);
        this.drawReceiveEnergy(inbox);
        this.handleReceiveSimulationMessages(inbox);
        this.handleReceiveFailureMessages(inbox); // Check where exactly this should be processed.
        this.handleForwardedMessages(inbox);
    }

    protected void incrementTotalReceivedMessages(Inbox inbox) {
        this.totalReceivedMessages += inbox.size();
    }

    private void drawReceiveEnergy(Inbox inbox) {
        for (int i = 0; i < inbox.size(); i++) {
            this.getSensor().drawReceiveEnergy();
        }
    }

    private void handleReceiveSimulationMessages(Inbox inbox) {
        for (Message m : inbox) {
            if (m instanceof SimulationMessage) {
                this.sendMessage(() -> (SimulationMessage) m, this.getParent());
            }
        }
        inbox.reset();
    }

    private void handleForwardedMessages(Inbox inbox) {
        for (Message m : inbox) {
            if (m instanceof ForwardedMessage) {
                ForwardedMessage fm = (ForwardedMessage) m;
                this.setWaitTime(fm.getWaitTime());
                ActivationMessage am = fm.getMessage();
                if (!this.isActive() && am.isActive()) {
                    this.getSensor().drawActivationEnergy();
                }
                this.active = am.isActive();
                this.parent = am.getParent();
                this.children = am.getChildren();
                for (ForwardedMessage c : fm.getForwardedMessages()) {
                    this.getSensor().drawTransmitEnergy(c.getDestination().getSensor());
                    this.sendDirect(c, c.getDestination());
                }
                break;
            }
        }
        inbox.reset();
    }

    private void handleReceiveFailureMessages(Inbox inbox) {
        for (Message m : inbox) {
            if (m instanceof FailureMessage) {
                for (SimulationNode n : this.getChildren()) {
                    this.sendMessage(m, n);
                }
                this.active = false;
                break;
            }
        }
        inbox.reset();
    }

    protected void sendMessage(Supplier<SimulationMessage> m, SimulationNode n) {
        if (this.isActive()) {
            SimulationMessage message = m.get();
            message.getNodes().push(this);
            this.sendMessage(message, n);
        }
    }

    private void sendMessage(Message m, SimulationNode n) {
        if (this.isActive()) {
            this.totalSentMessages++;
            this.getSensor().drawTransmitEnergy(n.getSensor());
            this.send(m, n);
        }
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

}
