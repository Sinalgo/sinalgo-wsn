package projects.wsn.nodes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import sinalgo.exception.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.Position;

import java.awt.Graphics;
import java.util.List;

@Getter(AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public abstract class SimulationNode extends Node {

    @Getter
    private SimulationNode parent;

    @Getter
    private List<SimulationNode> children;

    private long totalReceivedMessages;
    private long totalSentMessages;

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
    }

    @Override
    public void postStep() {
    }

    @Override
    public void checkRequirements() throws WrongConfigurationException {

    }

    @Override
    public String toString() {
        return "[" + this.superToString() + ": " + String.format(
                "Tx=%d, Rx=%d",
                this.getTotalSentMessages(),
                this.getTotalReceivedMessages()) + "]";
    }

    protected String superToString() {
        return super.toString();
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.drawAsDisk(g, pt, highlight, this.getDefaultDrawingSizeInPixels());
    }

    private static void fillCircle(Graphics g, PositionTransformation pt, Position center, double radius) {
        pt.translateToGUIPosition(center);
        int r = (int) (pt.getZoomFactor() * radius);
        g.fillOval(pt.getGuiX() - r, pt.getGuiY() - r, 2 * r, 2 * r);
    }

}
