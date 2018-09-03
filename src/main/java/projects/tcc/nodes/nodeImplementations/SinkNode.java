package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import lombok.Setter;
import projects.tcc.MessageCache;
import projects.tcc.nodes.messages.ActivationMessage;
import projects.tcc.nodes.messages.SimulationMessage;
import projects.tcc.simulation.algorithms.online.SolucaoViaAGMO;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.Simulation;
import projects.tcc.simulation.wsn.data.DemandPoints;
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

    @Setter
    private static Runnable stopSimulationMethod = Tools::stopSimulation;

    @Setter
    private static Runnable onStopSimulationMessageMethod = () -> Tools.minorError("Não foi mais possível se manter acima do mínimo de cobertura");

    @Override
    public void init() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        int index = SensorIndex.currentInstance().getNextIndex(Sink.class);
        this.sensor = new Sink(index, this.getPosition(),
                config.getCommRadius(), config.getSinkCommRadius(), this);
        SensorNetwork.currentInstance().addSink(this.getSensor());
    }

    @Override
    public void handleMessages(Inbox inbox) {
        int size = inbox.size();
        if (size > 0) {
            System.out.println("\nSTART logging received messages for round");
        }
        super.handleMessages(inbox);
        boolean fail = false;
        if (this.timeSinceLastMessage != null
                && this.heights != null
                && this.acknowledgedSensors != null) {
            this.increaseTimeSinceLastMessage();
            long closestFailedNode = this.checkFailures();
            if (closestFailedNode >= 0) {
                fail = true;
                System.out.println("FAILED SENSOR: " + Tools.getNodeByID(closestFailedNode + 1));
            }
        }
        if (size > 0) {
            System.out.println("END logging received messages for round\n");
        }
        boolean restructure = this.stage == 0 || Simulation.currentInstance().simulatePeriod(stage);
        if (fail || restructure) {
            boolean[] activeSensors = this.runSimulation();
            if (this.stage == 0) {
                Simulation.currentInstance().simulatePeriod(stage);
            }
            for (Sensor s : SensorNetwork.currentInstance().getSensors()) {
                if (s.isAvailable()) {
                    this.sendDirect(new ActivationMessage(activeSensors[s.getIndex()]), s.getNode());
                }
            }
            this.resetAcknowledgement(activeSensors.length);
            this.computeExpectedHeights();
        }
        stage++;
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

    private void computeExpectedHeights() {
        this.computeExpectedHeights(this.getSensor(), 1);
    }

    private void computeExpectedHeights(Sensor sensor, int currentHeight) {
        for (Sensor child : sensor.getChildren()) {
            this.heights[child.getIndex()] = currentHeight;
            this.computeExpectedHeights(child, currentHeight + 1);
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
        SimulationOutput.println("===== EVENTO e REESTRUTUROU TEMPO = " + this.stage);
        try {
            boolean[] activeSensors = SolucaoViaAGMO.currentInstance().simularRede();
            if (Double.compare(DemandPoints.currentInstance().getCoveragePercent(), SensorNetwork.currentInstance().getCoverageFactor()) >= 0) {
                if (SolucaoViaAGMO.currentInstance().isStopSimulationOnFailure()) {
                    Tools.stopSimulation();
                }
            } else {
                Tools.stopSimulation();
                Tools.minorError("Não foi mais possível se manter acima do mínimo de cobertura");
            }
            return activeSensors;
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

    @Override
    public void postStep() {
    }

}
