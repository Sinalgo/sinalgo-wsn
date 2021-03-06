package projects.wsn.nodes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.wsn.CustomGlobal;
import projects.wsn.nodes.nodeImplementations.SinkNode;
import projects.wsn.simulation.algorithms.graph.Graph;
import projects.wsn.simulation.network.SensorNetwork;
import projects.wsn.simulation.network.data.Sensor;
import projects.wsn.simulation.network.data.Sink;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;
import sinalgo.runtime.Global;

import java.awt.*;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public abstract class SimulationNode extends Node {

    public abstract Sensor getSensor();

    private boolean active;

    @Getter
    private boolean failed;

    @Getter
    private SimulationNode parent;

    @Getter
    private List<SimulationNode> children;

    @Getter
    private double batteryEnergy;

    @Getter
    private double batteryCapacity;

    @Getter
    private int transmitSpeedBps;

    private int waitTime;
    private long totalReceivedMessages;
    private long totalSentMessages;
    private double activationPower;
    private double receivePower;
    private double maintenancePower;
    private double commRatio; //Taxa de comunicação durante a transmissão em uma u.t.
    private double minBatteryThreshold;

    public boolean isAvailable() {
        return !this.isFailed();
    }

    public boolean isActive() {
        return this.active && this.isAvailable();
    }

    @Override
    protected String nodeTypeName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void neighborhoodChange() {
        // Do not use, we don't have a mobility model.
    }

    @Override
    public void preStep() {
        if (this.isActive() && !this.isSleep()) {
            this.drawMaintenanceEnergy();
        }
    }

    @Override
    public void postStep() {
        this.updateState();
        this.setWaitTime(Math.max(0, this.getWaitTime() - 1));
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }

    public boolean isSleep() {
        return this.getWaitTime() > 0;
    }

    @Override
    public String toString() {
        return "[" + this.superToString() + ": " + String.format(
                "Tx=%d, Rx=%d, Pwr=%.2f, State=%s",
                this.getTotalSentMessages(),
                this.getTotalReceivedMessages(),
                this.getBatteryEnergy(),
                (this.isSleep() ? "SLEEP" :
                        this.isActive() ? "ACTIVE" :
                                this.isAvailable() ? "INACTIVE" : "FAILED")) + "]";
    }

    protected String superToString() {
        return super.toString();
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.drawAsDisk(g, pt, highlight, this.getDefaultDrawingSizeInPixels());
        this.drawActivationTree(g, pt);
        this.drawCommSensRadius(g, pt);
    }

    private void drawCommSensRadius(Graphics g, PositionTransformation pt) {
        CustomGlobal customGlobal = ((CustomGlobal) Global.getCustomGlobal());
        if (customGlobal.isDrawCommRadius() && this.isActive() && !this.isSleep()) {
            drawRadius(g, pt, this, Color.ORANGE, this.getSensor().getCommRadius());
        }
        if (customGlobal.isDrawSensorRadius() && this.isActive() && !this.isSleep() && !(this instanceof SinkNode)) {
            drawRadius(g, pt, this, Color.MAGENTA, this.getSensor().getSensRadius());
        }
    }

    private void drawActivationTree(Graphics g, PositionTransformation pt) {
        CustomGlobal customGlobal = ((CustomGlobal) Global.getCustomGlobal());
        if (customGlobal.isDrawActivationTree()) {
            for (Sink s : SensorNetwork.currentInstance().getSinks()) {
                SinkNode sinkNode = (SinkNode) s.getNode();
                if (sinkNode.getNetworkGraph() != null) {
                    Graph.Node<Sensor> node = sinkNode.getNetworkGraph().getSensorNodeMap().get(this.getSensor());
                    if (node != null && node.getPreviousSource() != null) {
                        SimulationNode previous = node.getPreviousSource().getNode();
                        drawLine(g, pt, previous, this);
                    }
                }
            }
        }
    }

    private static void drawLine(Graphics g, PositionTransformation pt, Node source, Node destination) {
        Color backupColor = g.getColor();
        g.setColor(Color.LIGHT_GRAY);
        pt.translateToGUIPosition(source.getPosition());
        int sourceX = pt.getGuiX();
        int sourceY = pt.getGuiY();
        pt.translateToGUIPosition(destination.getPosition());
        int destinationX = pt.getGuiX();
        int destinationY = pt.getGuiY();
        g.drawLine(sourceX, sourceY, destinationX, destinationY);
        g.setColor(backupColor);
    }

    private static void drawRadius(Graphics g, PositionTransformation pt, Node s, Color color, double radius) {
        Color backupColor = g.getColor();
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * 0.07));
        g.setColor(fillColor);
        fillCircle(g, pt, s.getPosition(), radius);
        g.setColor(color);
        pt.drawCircle(g, s.getPosition(), radius);
        g.setColor(backupColor);
    }

    private static void fillCircle(Graphics g, PositionTransformation pt, Position center, double radius) {
        pt.translateToGUIPosition(center);
        int r = (int) (pt.getZoomFactor() * radius);
        g.fillOval(pt.getGuiX() - r, pt.getGuiY() - r, 2 * r, 2 * r);
    }

    private void drawEnergySpent(double energySpent) {
        this.setBatteryEnergy(Math.max(this.getBatteryEnergy() - energySpent, 0));
    }

    private double getTransmitPower(SimulationNode neighbor, double occupiedBandwidth) {
        double current = this.getSensor().getNeighborhood().get(neighbor.getSensor()).getCurrent();
        return occupiedBandwidth * current;
    }

    protected void drawActivationEnergy() {
        this.drawEnergySpent(this.getActivationPower());
    }

    private void drawMaintenanceEnergy() {
        this.drawEnergySpent(this.getMaintenancePower());
    }

    protected void drawReceiveEnergy() {
        this.drawEnergySpent(this.getReceivePower());
    }

    protected void drawTransmitEnergy(SimulationNode neighbor) {
        this.drawTransmitEnergy(neighbor, this.getCommRatio());
    }

    protected void drawTransmitEnergy(SimulationNode neighbor, double occupiedBandwidth) {
        this.drawEnergySpent(this.getTransmitPower(neighbor, occupiedBandwidth));
    }

    private void updateState() {
        if (this.isAvailable() &&
                Double.compare(this.getBatteryEnergy(), this.getMinBatteryThreshold() * this.getBatteryCapacity()) <= 0) {
            this.setFailed(true);
        }
    }

    @NodePopupMethod(menuText = "Deactivate")
    public void deactivate() {
        this.setActive(false);
    }

    @NodePopupMethod(menuText = "Force failure")
    public void fail() {
        this.setFailed(true);
    }

}
