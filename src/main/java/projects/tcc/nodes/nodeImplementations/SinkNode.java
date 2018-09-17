package projects.tcc.nodes.nodeImplementations;

import lombok.Getter;
import projects.tcc.CustomGlobal;
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
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SinkNode extends SensorNode {

    @Getter
    private Sink sensor;

    @Getter
    private Graph networkGraph;

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
        this.networkGraph = null;
        int size = inbox.size();
        if (size > 0) {
            System.out.println("\nSTART logging received messages for round");
        }
        this.incrementTotalReceivedMessages(inbox);
        this.handleMessageReceiving(inbox);
        boolean fail = false;
        increaseTimeSinceLastMessage();
        List<SimulationNode> failedNodes = this.checkFailures();
        if (!failedNodes.isEmpty()) {
            fail = true;
            failedNodes.forEach(n -> {
                n.getSensor().fail();
                System.out.println("FAILED SENSOR: " + n);
            });
            if (((CustomGlobal) Tools.getCustomGlobal()).isStopSimulationOnFailure()) {
                Tools.stopSimulation();
            }
        }
        if (size > 0) {
            System.out.println("END logging received messages for round\n");
        }
        int stage = (int) Tools.getGlobalTime();
        boolean[] currentActiveSensors = null;
        boolean[] activeSensors = null;
        // Isto só funciona aqui porque o Sink é o último nó a ser colocado.
        // Alterar para o preRound/postRound do CustomGlobal!
        Simulation.currentInstance().simulatePeriod(stage);
        if (fail || stage == 1) {
            currentActiveSensors = SensorNetwork.currentInstance().getSensorsStatusArray();
            activeSensors = computeActiveSensors();
        }
        if (activeSensors != null) {
            this.networkGraph = new Graph(SensorNetwork.currentInstance().getSensorsAndSinks());
            this.networkGraph.computeEdges(false);
            TreeNode<Sensor> root = this.networkGraph.getTreeRepresentation(this.getSensor());
            this.minimizeActivationTree(root, currentActiveSensors, activeSensors);
            System.out.println();
            root.print();
            System.out.println();
            int maxDepth = getMaxDepth(root);
            this.setWaitTime(maxDepth + 1);
            List<ForwardedMessage<ActivationMessage>> activationMessages =
                    convertToForwardedMessageList(maxDepth, activeSensors, root);
            for (ForwardedMessage m : activationMessages) {
                this.sendDirect(m, m.getDestination());
            }
            resetAcknowledgement();
            computeExpectedHeights(this.getSensor());
        }
    }

    private void minimizeActivationTree(TreeNode<Sensor> root, boolean[] currentActiveSensors, boolean[] activeSensors) {
        if (SimulationConfigurationLoader.getConfiguration().isMinimizeActivationTree()) {
            this.minimizeActivationTreeRecursive(root, currentActiveSensors, activeSensors);
        }
    }

    private boolean minimizeActivationTreeRecursive(TreeNode<Sensor> node, boolean[] currentActiveSensors, boolean[] activeSensors) {
        boolean hasDiff = false;
        for (Iterator<TreeNode<Sensor>> i = node.getChildren().iterator(); i.hasNext(); ) {
            TreeNode<Sensor> child = i.next();
            if (this.minimizeActivationTreeRecursive(child, currentActiveSensors, activeSensors)) {
                hasDiff = true;
            } else {
                this.getNetworkGraph().getSensorNodeMap().remove(child.getValue());
                i.remove();
            }
        }
        return hasDiff
                || node.getValue() instanceof Sink
                || activeSensors[node.getValue().getIndex()]
                || currentActiveSensors[node.getValue().getIndex()] != activeSensors[node.getValue().getIndex()];
    }

    private static SimulationNode getParentNode(Sensor s) {
        return s.getParent() == null ? null : s.getParent().getNode();
    }

    private static List<SimulationNode> getChildrenNodes(Sensor s) {
        List<SimulationNode> childrenNodes = new ArrayList<>(s.getChildren().size());
        for (Sensor c : s.getChildren()) {
            childrenNodes.add(c.getNode());
        }
        return childrenNodes;
    }

    @Override
    protected void sendMessage(Supplier<SimulationMessage> m, SimulationNode n) {
    }

    private static void resetAcknowledgement() {
        List<Sensor> sensors = SensorNetwork.currentInstance().getSensors();
        for (Sensor s : sensors) {
            s.resetAcknowledgement();
        }
    }

    private static void increaseTimeSinceLastMessage() {
        for (Sensor s : SensorNetwork.currentInstance().getSensors()) {
            if (s.getHeight() > 0) {
                s.setTimeSinceLastMessage(s.getTimeSinceLastMessage() + 1);
            }
        }
    }

    private List<SimulationNode> checkFailures() {
        List<SimulationNode> failedNodes = new ArrayList<>();
        if (SimulationConfigurationLoader.getConfiguration().isInstantaneousFailureDetection()) {
            for (Sensor s : SensorNetwork.currentInstance().getSensors()) {
                if (s.getNode().isFailed() && s.isAvailable()) {
                    failedNodes.add(s.getNode());
                }
            }
        } else {
            if (this.getSensor().getChildren() != null) {
                this.getSensor().getChildren().forEach(n -> this.checkFailures(n, failedNodes));
            }
        }
        return failedNodes;

    }

    private void checkFailures(Sensor s, List<SimulationNode> failedNodes) {
        if (s.isActive()) {
            int maximumTime = (Configuration.isInterference() ? 2 : 1)
                    + (s.isAcknowledged() ? 0 : s.getHeight());
            if (s.getTimeSinceLastMessage() > maximumTime) {
                failedNodes.add(s.getNode());
            } else if (s.getChildren() != null) {
                s.getChildren().forEach(c -> this.checkFailures(c, failedNodes));
            }
        }
    }

    private static void computeExpectedHeights(Sensor root) {
        computeExpectedHeights(root, 1);
    }

    private static void computeExpectedHeights(Sensor sensor, int currentHeight) {
        for (Sensor child : sensor.getChildren()) {
            child.setHeight(currentHeight);
            computeExpectedHeights(child, currentHeight + 1);
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

    private static boolean[] computeActiveSensors() {
        SimulationOutput.println("===== Running Genetic Algorithm at round: " + (int) Tools.getGlobalTime());
        boolean[] activeSensors = MultiObjectiveGeneticAlgorithm.currentInstance().computeActiveSensors();
        if (Double.compare(DemandPoints.currentInstance().getCoveragePercent(), SensorNetwork.currentInstance().getCoverageFactor()) < 0) {
            return null;
        }
        return activeSensors;
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        this.setColor(this.isSleep() ? Color.GRAY : Color.BLUE);
        this.setDefaultDrawingSizeInPixels(20);
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

    private static int getMaxDepth(TreeNode<Sensor> n) {
        return getMaxDepth(1, n);
    }

    private static int getMaxDepth(int depth, TreeNode<Sensor> n) {
        int maxDepth = depth;
        for (TreeNode<Sensor> c : n.getChildren()) {
            maxDepth = Math.max(maxDepth, getMaxDepth(depth + 1, c));
        }
        return maxDepth;
    }

    private static List<ForwardedMessage<ActivationMessage>> convertToForwardedMessageList(int waitTime, boolean[] activeSensors, TreeNode<Sensor> n) {
        List<ForwardedMessage<ActivationMessage>> messages = new ArrayList<>(n.getChildren().size());
        for (TreeNode<Sensor> c : n.getChildren()) {
            Sensor s = c.getValue();
            boolean active = activeSensors[s.getIndex()];
            ActivationMessage m = new ActivationMessage(active,
                    waitTime,
                    active ? getParentNode(s) : null,
                    active ? getChildrenNodes(s) : null);
            messages.add(new ForwardedMessage<>(s.getNode(), m,
                    convertToForwardedMessageList(waitTime - 1, activeSensors, c)));
        }
        return messages;
    }

    @Override
    public String toString() {
        return "[" + this.superToString() + ": " + String.format(
                "Rx=%d", this.getTotalReceivedMessages()) + "]";
    }

}
