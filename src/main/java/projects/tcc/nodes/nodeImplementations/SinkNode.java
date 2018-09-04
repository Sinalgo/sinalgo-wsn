package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import projects.tcc.MessageCache;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.nodes.messages.ActivationMessage;
import projects.tcc.nodes.messages.SimulationMessage;
import projects.tcc.simulation.algorithms.MultiObjectiveGeneticAlgorithm;
import projects.tcc.simulation.io.SimulationConfiguration;
import projects.tcc.simulation.io.SimulationConfigurationLoader;
import projects.tcc.simulation.io.SimulationOutput;
import projects.tcc.simulation.wsn.SensorNetwork;
import projects.tcc.simulation.wsn.Simulation;
import projects.tcc.simulation.wsn.data.DemandPoints;
import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.SensorIndex;
import projects.tcc.simulation.wsn.data.Sink;
import sinalgo.configuration.Configuration;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.tools.Tools;

import java.awt.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SinkNode extends SensorNode {

    @Getter
    private Sink sensor;

    private int[] heights;
    private int[] timeSinceLastMessage;
    private boolean[] acknowledgedSensors;

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
                if (MultiObjectiveGeneticAlgorithm.currentInstance().isStopSimulationOnFailure()) {
                    Tools.stopSimulation();
                }
            }
        }
        if (size > 0) {
            System.out.println("END logging received messages for round\n");
        }
        int stage = (int) Tools.getGlobalTime();
        boolean[] activeSensors = null;
        if (stage == 1) {
            activeSensors = this.computeActiveSensors();
        }
        // Isto só funciona aqui porque o Sink é o último nó a ser colocado.
        // Alterar para o preRound/postRound do CustomGlobal!
        Simulation.currentInstance().simulatePeriod(stage);
        if (fail || Simulation.currentInstance().restructureTest(stage)) {
            activeSensors = this.computeActiveSensors();
        }
        if (activeSensors != null) {
            for (Sensor s : SensorNetwork.currentInstance().getSensors()) {
                if (s.isAvailable()) {
                    boolean active = activeSensors[s.getIndex()];
                    this.sendDirect(new ActivationMessage(active, active ? s.getParentNode() : null), s.getNode());
                }
            }
            this.resetAcknowledgement(activeSensors.length);
            this.computeExpectedHeights();
        }
    }

    @Override
    protected void sendMessage(Supplier<SimulationMessage> m, SimulationNode n) {
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
                int maximumTime = (Configuration.isInterference() ? 2 : 1)
                        + (this.acknowledgedSensors[i] ? 0 : height);
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

    private boolean[] computeActiveSensors() {
        SimulationOutput.println("===== Running Genetic Algorithm at round: " + (int) Tools.getGlobalTime());
        boolean[] activeSensors = MultiObjectiveGeneticAlgorithm.currentInstance().computeActiveSensors();
        if (Double.compare(DemandPoints.currentInstance().getCoveragePercent(), SensorNetwork.currentInstance().getCoverageFactor()) < 0) {
            Tools.stopSimulation();
            Tools.minorError("The coverage could not be kept above the desired factor anymore. Stopping simulation.");
            return null;
        }
        return activeSensors;
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

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isFailed() {
        return false;
    }

}
