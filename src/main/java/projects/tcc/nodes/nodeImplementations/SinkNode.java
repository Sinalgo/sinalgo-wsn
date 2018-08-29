package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import projects.tcc.MessageCache;
import projects.tcc.nodes.messages.ActivationMessage;
import projects.tcc.nodes.messages.FailureMessage;
import projects.tcc.nodes.messages.SimulationMessage;
import projects.tcc.simulation.algorithms.online.SolucaoViaAGMOSinalgo;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;
import sinalgo.exception.SinalgoWrappedException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;

import java.awt.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SinkNode extends SensorNode {

    @Getter
    private Sink sensor;

    private int stage = 0;

    @Override
    public void init() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        this.sensor = new Sink((int) this.getID() - 1, this.getPosition().getXCoord(),
                this.getPosition().getYCoord(), config.getCommRadius(), this);
        SensorNetwork.currentInstance().addSinks(this.getSensor());
    }

    @Override
    public void handleMessages(Inbox inbox) {
        int size = inbox.size();
        if (size > 0) {
            System.out.println("\nSTART logging received messages for round");
        }
        super.handleMessages(inbox);
        if (size > 0) {
            System.out.println("END logging received messages for round\n");
        }
        boolean[] activeSensors = this.runSimulation();
        if (activeSensors != null) {
            ActivationMessage m = new ActivationMessage(activeSensors);
            for (Sensor s : SensorNetwork.currentInstance().getAvailableSensors()) {
                this.sendDirect(m, s.getNode());
            }
            this.handleMessageReceiving(m);
            this.resetSensorStatus();
        }
    }

    @Override
    protected void handleMessageReceiving(SimulationMessage m) {
        m.getNodes().push(this);
        String messageStr = m.getNodes().stream()
                .map(SensorNode::toString)
                .collect(Collectors.joining(", "));
        if (m instanceof FailureMessage) {
            messageStr += ", FAILED: " + ((FailureMessage) m).getFailedNode();
        }
        System.out.println(messageStr);
        MessageCache.push(m);
    }

    @Override
    protected void handleMessageSending(Supplier<SimulationMessage> m) {
        SimulationMessage message = m.get();
        if (message instanceof FailureMessage) {
            // trigger faile detection reaction
        }
    }

    private boolean[] runSimulation() {
        SolucaoViaAGMOSinalgo solucao = SolucaoViaAGMOSinalgo.currentInstance();
        try {
            return solucao.simularRede(stage++);
        } catch (Exception e) {
            throw new SinalgoWrappedException(e);
        }
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        this.setColor(Color.BLUE);
        this.setDefaultDrawingSizeInPixels(30);
        this.superDraw(g, pt, highlight);
    }

}
