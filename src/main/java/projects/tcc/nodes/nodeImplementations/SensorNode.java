package projects.tcc.nodes.nodeImplementations;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.MessageCache;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.nodes.messages.ActivationMessage;
import projects.tcc.nodes.messages.FailureMessage;
import projects.tcc.nodes.messages.SimulationMessage;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sensor.NeighborData;
import projects.tcc.simulation.wsn.data.Sink;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;

import java.awt.*;
import java.util.Map.Entry;
import java.util.function.Supplier;

public class SensorNode extends SimulationNode {

    @Getter(AccessLevel.PROTECTED)
    private long totalReceivedMessages;

    @Getter(AccessLevel.PROTECTED)
    private long totalSentMessages;

    @Getter
    private Sensor sensor;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private boolean[] activeSensors = new boolean[0];
    private boolean[] sensorStatus;

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
        this.resetSensorStatus();
        while (inbox.hasNext()) {
            Message m = inbox.next();
            if (inbox.getSender() instanceof SensorNode && m instanceof SimulationMessage) {
                this.totalReceivedMessages++;
                this.sensorStatus[((SensorNode) inbox.getSender()).getSensor().getSensorId()] = true;
                this.handleMessageReceiving((SimulationMessage) m);
            }
        }
        for (Entry<Sensor, NeighborData> entry : this.getSensor().getNeighborhood().entrySet()) {
            Sensor s = entry.getKey();
            NeighborData d = entry.getValue();
            if (s instanceof Sink) {
                continue;
            }
            if (this.getActiveSensors()[s.getSensorId()] && this.getSensor().equals(s.getParent())) {
                if (this.sensorStatus[s.getSensorId()]) {
                    d.resetMissedMessageCounter();
                } else if (d.getMissedMessageCounter() <= 7) {
                    d.increaseMessageCounter();
                }
                if (d.getMissedMessageCounter() > 3
                        && d.getMissedMessageCounter() < 7) {
                    this.handleMessageSending(() -> new FailureMessage(s.getNode()));
                }
            }
        }
    }

    protected void resetSensorStatus() {
        if (this.getActiveSensors() != null &&
                (this.sensorStatus == null || this.sensorStatus.length != this.getActiveSensors().length)) {
            this.sensorStatus = new boolean[this.getActiveSensors().length];
        } else {
            for (int i = 0; i < this.sensorStatus.length; i++) {
                this.sensorStatus[i] = false;
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

    protected void handleMessageReceiving(ActivationMessage m) {
        this.setActiveSensors(m.getActiveSensors());
    }

    protected void handleMessageSending(Supplier<SimulationMessage> m) {
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

}
