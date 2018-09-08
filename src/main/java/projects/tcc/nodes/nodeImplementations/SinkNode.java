package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import projects.tcc.MessageCache;
import projects.tcc.nodes.SimulationNode;
import projects.tcc.nodes.messages.ActivationMessage;
import projects.tcc.nodes.messages.ForwardedMessage;
import projects.tcc.nodes.messages.SimulationMessage;
import projects.tcc.simulation.algorithms.MultiObjectiveGeneticAlgorithm;
import projects.tcc.simulation.algorithms.graph.Graph;
import projects.tcc.simulation.algorithms.graph.TreeNode;
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
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SinkNode extends SensorNode {

    @Getter
    private Sink sensor;

    @Override
    public void init() {
        SimulationConfiguration config = SimulationConfigurationLoader.getConfiguration();
        int index = SensorIndex.currentInstance().getNextIndex(Sink.class);
        this.sensor = new Sink(index, this.getPosition(), config.getSinkCommRadius(), this);
        SensorNetwork.currentInstance().addSink(this.getSensor());
    }

    @Override
    public void handleMessages(Inbox inbox) {
        if (this.isSleep()) {
            return;
        }
        int size = inbox.size();
        if (size > 0) {
            System.out.println("\nSTART logging received messages for round");
        }
        this.incrementTotalReceivedMessages(inbox);
        this.handleMessageReceiving(inbox);
        boolean fail = false;
        this.increaseTimeSinceLastMessage();
        SimulationNode closestFailedNode = this.checkFailures();
        if (closestFailedNode != null) {
            fail = true;
            closestFailedNode.getSensor().fail();
            System.out.println("FAILED SENSOR: " + closestFailedNode);
            if (MultiObjectiveGeneticAlgorithm.currentInstance().isStopSimulationOnFailure()) {
                Tools.stopSimulation();
            }
        }
        if (size > 0) {
            System.out.println("END logging received messages for round\n");
        }
        int stage = (int) Tools.getGlobalTime();
        boolean[] activeSensors = null;
        // Isto só funciona aqui porque o Sink é o último nó a ser colocado.
        // Alterar para o preRound/postRound do CustomGlobal!
        Simulation.currentInstance().simulatePeriod(stage);
        if (fail || stage == 1) {
            activeSensors = this.computeActiveSensors();
        }
        if (activeSensors != null) {
            TreeNode<Sensor> root = this.getSensorGraphAsTree();
            System.out.println();
            root.print();
            System.out.println();
            this.setWaitTime(this.getMaxDepth(root));
            List<ForwardedMessage> forwardedMessages =
                    this.convertToForwardedMessageList(this.getWaitTime(), activeSensors, this.getSensorGraphAsTree());
            for (ForwardedMessage m : forwardedMessages) {
                this.sendDirect(m, m.getDestination());
            }
            this.setChildren(this.getChildrenNodes(this.getSensor()));
            this.resetAcknowledgement();
            this.computeExpectedHeights();
        }
    }

    private SimulationNode getParentNode(Sensor s) {
        return s.getParent() == null ? null : s.getParent().getNode();
    }

    private List<SimulationNode> getChildrenNodes(Sensor s) {
        List<SimulationNode> childrenNodes = new ArrayList<>(s.getChildren().size());
        for (Sensor c : s.getChildren()) {
            childrenNodes.add(c.getNode());
        }
        return childrenNodes;
    }

    @Override
    protected void sendMessage(Supplier<SimulationMessage> m, SimulationNode n) {
    }

    private void resetAcknowledgement() {
        List<Sensor> sensors = SensorNetwork.currentInstance().getSensors();
        for (Sensor s : sensors) {
            s.resetAcknowledgement();
        }
    }

    private void increaseTimeSinceLastMessage() {
        List<Sensor> sensors = SensorNetwork.currentInstance().getSensors();
        for (Sensor s : sensors) {
            if (s.getHeight() > 0) {
                s.setTimeSinceLastMessage(s.getTimeSinceLastMessage() + 1);
            }
        }
    }

    private SimulationNode checkFailures() {
        int minFailedHeight = Integer.MAX_VALUE;
        SimulationNode closestFailedSensor = null;
        List<Sensor> sensors = SensorNetwork.currentInstance().getSensors();
        for (Sensor s : sensors) {
            if (s.getHeight() > 0) {
                int maximumTime = (Configuration.isInterference() ? 2 : 1)
                        + (s.isAcknowledged() ? 0 : s.getHeight());
                if (s.getTimeSinceLastMessage() > maximumTime) {
                    if (s.getHeight() < minFailedHeight) {
                        minFailedHeight = s.getHeight();
                        closestFailedSensor = s.getNode();
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
            child.setHeight(currentHeight);
            this.computeExpectedHeights(child, currentHeight + 1);
        }
    }

    private void handleMessageReceiving(Inbox inbox) {
        for (Message m : inbox) {
            if (m instanceof SimulationMessage) {
                this.handleMessageReceiving((SimulationMessage) m);
            }
        }
    }

    private void handleMessageReceiving(SimulationMessage m) {
        m.getNodes().forEach(sn -> {
            sn.getSensor().setAcknowledged(true);
            sn.getSensor().setTimeSinceLastMessage(0);
        });
        m.getNodes().push(this);
        String messageStr = m.getNodes().stream().map(Object::toString)
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
        this.setWaitTime(Math.max(0, this.getWaitTime() - 1));
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public boolean isFailed() {
        return false;
    }

    private TreeNode<Sensor> getSensorGraphAsTree() {
        Graph g = new Graph(SensorNetwork.currentInstance().getSensorsAndSinks());
        g.computeEdges(false);
        return g.getTreeRepresentation(this.getSensor());
    }

    private int getMaxDepth(TreeNode<Sensor> n) {
        return this.getMaxDepth(1, n);
    }

    private int getMaxDepth(int depth, TreeNode<Sensor> n) {
        int maxDepth = depth;
        for (TreeNode<Sensor> c : n.getChildren()) {
            maxDepth = Math.max(maxDepth, this.getMaxDepth(depth + 1, c));
        }
        return maxDepth;
    }

    private List<ForwardedMessage> convertToForwardedMessageList(int waitTime, boolean[] activeSensors, TreeNode<Sensor> n) {
        List<ForwardedMessage> messages = new ArrayList<>(n.getChildren().size());
        for (TreeNode<Sensor> c : n.getChildren()) {
            Sensor s = c.getValue();
            boolean active = activeSensors[s.getIndex()];
            ActivationMessage m = new ActivationMessage(active,
                    active ? this.getParentNode(s) : null,
                    active ? this.getChildrenNodes(s) : null);
            messages.add(new ForwardedMessage(s.getNode(), waitTime, m,
                    this.convertToForwardedMessageList(waitTime - 1, activeSensors, c)));
        }
        return messages;
    }

    @Override
    public String toString() {
        return "[" + this.superToString() + ": " + String.format(
                "Rx=%d", this.getTotalReceivedMessages()) + "]";
    }

}
