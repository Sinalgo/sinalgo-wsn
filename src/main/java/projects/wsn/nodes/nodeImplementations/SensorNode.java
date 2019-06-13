package projects.wsn.nodes.nodeImplementations;

import lombok.Getter;
import projects.wsn.nodes.SimulationNode;
import projects.wsn.nodes.messages.ActivationMessage;
import projects.wsn.nodes.messages.ForwardedMessage;
import projects.wsn.nodes.messages.SimulationMessage;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.messages.NackBox;

import java.awt.Graphics;
import java.util.function.Supplier;

@Getter
public class SensorNode extends SimulationNode {

    @Override
    public void init() {
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        this.setDefaultDrawingSizeInPixels(10);
        super.draw(g, pt, highlight);
    }

    protected void superDraw(Graphics g, PositionTransformation pt, boolean highlight) {
        super.draw(g, pt, highlight);
    }

    @Override
    public void handleNAckMessages(NackBox nackBox) {
    }

    @Override
    public void handleMessages(Inbox inbox) {
        this.handleMessageReceiving(inbox);
    }

    private void handleMessageReceiving(Inbox inbox) {
        this.incrementTotalReceivedMessages(inbox);
        this.handleReceiveSimulationMessages(inbox);
        this.handleForwardedMessages(inbox);
    }

    protected void incrementTotalReceivedMessages(Inbox inbox) {
        this.setTotalReceivedMessages(this.getTotalReceivedMessages() + inbox.size());
    }

    private void handleReceiveSimulationMessages(Inbox inbox) {
        for (Message m : inbox) {
            if (m instanceof SimulationMessage) {
                this.sendMessage(() -> (SimulationMessage) m, this.getParent());
            }
        }
        if (this.getParent() != null) {
            this.sendMessage(SimulationMessage::new, this.getParent());
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
                    this.setParent(am.getParent());
                    this.setChildren(am.getChildren());
                }
                for (ForwardedMessage<?> c : fm.getForwardedMessages()) {
                    this.sendDirect(c, c.getDestination());
                }
                break;
            }
        }
        inbox.reset();
    }

    protected void sendMessage(Supplier<SimulationMessage> m, SimulationNode n) {
        if (this.getParent() != null) {
            SimulationMessage message = m.get();
            message.getNodes().push(this);
            this.sendMessage(message, n);
        }
    }

    private void sendMessage(Message m, SimulationNode n) {
        if (this.getParent() != null) {
            this.setTotalSentMessages(this.getTotalSentMessages() + 1);
            this.send(m, n);
        }
    }

}
