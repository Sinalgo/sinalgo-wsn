package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import projects.tcc.MessageCache;
import projects.tcc.nodes.messages.ActivationMessage;
import projects.tcc.nodes.messages.SimulationMessage;
import projects.tcc.simulation.algorithms.online.SolucaoViaAGMO;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.SensorIndex;
import projects.tcc.simulation.wsn.data.Sink;
import sinalgo.exception.SinalgoWrappedException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.tools.Tools;

import java.awt.*;
import java.util.stream.Collectors;

public class SinkNode extends SensorNode {

    @Getter
    private Sink sensor;

    private int stage = 0;

    private int[] heights;
    private int[] timeSinceLastMessage;
    private boolean[] acknowledgedSensors;

    @Override
    public void init() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        int index = SensorIndex.getNextIndex(Sink.class);
        this.sensor = new Sink(index, config.getSinkPositions().get(index).toPosition(),
                config.getCommRadius(), config.getSinkCommRadius(), this);
        this.setPosition(this.getSensor().getPosition());
        SensorNetwork.currentInstance().addSink(this.getSensor());
    }

    @Override
    public void handleMessages(Inbox inbox) {
        int size = inbox.size();
        if (size > 0) {
            System.out.println("\nSTART logging received messages for round");
        }
        super.handleMessages(inbox);
        if (this.timeSinceLastMessage != null
                && this.heights != null
                && this.acknowledgedSensors != null) {
            this.increaseTimeSinceLastMessage();
            long closestFailedNode = this.checkFailures();
            if (closestFailedNode >= 0) {
                System.out.println("FAILED SENSOR: " + Tools.getNodeByID(closestFailedNode + 1));
            }
        }
        if (size > 0) {
            System.out.println("END logging received messages for round\n");
        }
        boolean[] activeSensors = this.runSimulation();
        if (activeSensors != null) {
            for (Sensor s : SensorNetwork.currentInstance().getSensors()) {
                if (s.isAvailable()) {
                    this.sendDirect(new ActivationMessage(activeSensors[s.getIndex()]), s.getNode());
                }
            }
            this.resetAcknowledgement(activeSensors.length);
            this.setExpectedHeights();
        }
    }

    private void resetAcknowledgement(int size) {
        if (this.acknowledgedSensors == null || this.acknowledgedSensors.length != size) {
            this.acknowledgedSensors = new boolean[size];
        } else {
            for (int i = 0; i < this.acknowledgedSensors.length; i++) {
                this.acknowledgedSensors[i] = false;
            }
        }
        if (this.heights == null || this.heights.length != size) {
            this.heights = new int[size];
        } else {
            for (int i = 0; i < this.heights.length; i++) {
                this.heights[i] = 0;
            }
        }
        if (this.timeSinceLastMessage == null || this.timeSinceLastMessage.length != size) {
            this.timeSinceLastMessage = new int[size];
        } else {
            for (int i = 0; i < this.timeSinceLastMessage.length; i++) {
                this.timeSinceLastMessage[i] = 0;
            }
        }

    }

    private void increaseTimeSinceLastMessage() {
        for (int i = 0; i < this.timeSinceLastMessage.length; i++) {
            if (this.heights[i] > 0) {
                this.timeSinceLastMessage[i]++;
            }
        }
    }

    private long checkFailures() {
        int minFailedHeight = Integer.MAX_VALUE;
        long closestFailedSensor = -1;
        for (int i = 0; i < this.timeSinceLastMessage.length; i++) {
            int height = this.heights[i];
            if (height > 0) {
                int maximumTime = 3 + (this.acknowledgedSensors[i] ? 0 : height);
                if (this.timeSinceLastMessage[i] > maximumTime) {
                    if (height < minFailedHeight) {
                        minFailedHeight = height;
                        closestFailedSensor = i;
                    }
                }
            }
        }
        return closestFailedSensor;
    }

    private void setExpectedHeights() {
        this.setExpectedHeights(this.getSensor(), 1);
    }

    private void setExpectedHeights(Sensor sensor, int currentHeight) {
        for (Sensor child : sensor.getChildren()) {
            this.heights[child.getIndex()] = currentHeight;
            this.setExpectedHeights(child, currentHeight + 1);
        }
    }

    @Override
    protected void handleMessageReceiving(SimulationMessage m) {
        m.getNodes().push(this);
        String messageStr = m.getNodes().stream()
                .map(sn -> {
                    this.acknowledgedSensors[sn.getSensor().getIndex()] = true;
                    this.timeSinceLastMessage[sn.getSensor().getIndex()] = 0;
                    return sn.toString();
                })
                .collect(Collectors.joining(", "));
        System.out.println(messageStr);
        MessageCache.push(m);
    }

    private boolean[] runSimulation() {
        SolucaoViaAGMO solucao = SolucaoViaAGMO.currentInstance();
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
