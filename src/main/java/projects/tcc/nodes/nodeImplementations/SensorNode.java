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
import java.util.function.Supplier;

@Getter
public class SensorNode extends SimulationNode {

    private Sensor sensor;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private int waitTime;

    @Override
    public void init() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        int index = SensorIndex.currentInstance().getNextIndex(Sensor.class);
        this.sensor = new Sensor(index, this.getPosition(),
                config.getSensorRadius(), config.getCommRadius(), this);
        SensorNetwork.currentInstance().addSensor(this.getSensor());
        this.setActivationPower(config.getActivationPower());
        this.setReceivePower(config.getReceivePower());
        this.setMaintenancePower(config.getMaintenancePower());
        this.setBatteryEnergy(config.getBatteryEnergy());
        this.setBatteryCapacity(config.getBatteryEnergy());
        this.setMinBatteryThreshold(config.getMinBatteryThreshold());
        this.setCommRatio(config.getCommRatio());
        this.setTransmitSpeedBps(config.getTransmitSpeedBps());
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        this.setColor(this.getColorByStatus());
        this.setDefaultDrawingSizeInPixels(10);
        this.superDraw(g, pt, highlight);
    }

    private Color getColorByStatus() {
        if (this.isFailed()) {
            if (this.getSensor().isFailed()) {
                return Color.RED;
            }
            return Color.MAGENTA;
        } else if (this.getSensor().isFailed()) {
            return Color.ORANGE;
        }
        if (this.isSleep()) {
            return Color.GRAY;
        }
        if (this.isActive()) {
            if (this.getSensor().isActive()) {
                return Color.GREEN;
            }
            return Color.PINK;
        } else {
            if (this.getSensor().isActive()) {
                return Color.BLUE;
            }
            return Color.BLACK;
        }
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
        if (this.isAvailable()) {
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
        this.setTotalReceivedMessages(this.getTotalReceivedMessages() + inbox.size());
    }

    private void drawReceiveEnergy(Inbox inbox) {
        for (int i = 0; i < inbox.size(); i++) {
            this.drawReceiveEnergy();
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
                ForwardedMessage<?> fm = (ForwardedMessage) m;
                Message fmm = fm.getMessage();
                if (fmm instanceof ActivationMessage) {
                    ActivationMessage am = (ActivationMessage) fmm;
                    if (!this.isActive() && am.isActive()) {
                        this.setWaitTime(am.getWaitTime());
                        this.drawActivationEnergy();
                    }
                    this.setActive(am.isActive());
                    this.setParent(am.getParent());
                    this.setChildren(am.getChildren());
                }
                for (ForwardedMessage<?> c : fm.getForwardedMessages()) {
                    this.drawTransmitEnergy(c.getDestination(),
                            (double) c.getSize() / (double) this.getTransmitSpeedBps());
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
                this.setActive(false);
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
            this.setTotalSentMessages(this.getTotalSentMessages() + 1);
            this.drawTransmitEnergy(n);
            this.send(m, n);
        }
    }

}
