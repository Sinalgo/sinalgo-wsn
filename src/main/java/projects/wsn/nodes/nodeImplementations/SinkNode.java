package projects.wsn.nodes.nodeImplementations;

import projects.wsn.MessageCache;
import projects.wsn.nodes.SimulationNode;
import projects.wsn.nodes.messages.ActivationMessage;
import projects.wsn.nodes.messages.ForwardedMessage;
import projects.wsn.nodes.messages.SimulationMessage;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

import java.awt.Graphics;
import java.util.Collections;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SinkNode extends SensorNode {

    @Override
    public void handleMessages(Inbox inbox) {
        int size = inbox.size();
        if (size > 0) {
            System.out.println("\nSTART logging received messages for round");
        }
        this.incrementTotalReceivedMessages(inbox);
        this.handleMessageReceiving(inbox);
        if (size > 0) {
            System.out.println("END logging received messages for round\n");
        }
        // Isto só funciona aqui porque o Sink é o último nó a ser colocado.
        // Alterar para o preRound/postRound do CustomGlobal!
        ActivationMessage a = new ActivationMessage(this, null);
        ForwardedMessage<ActivationMessage> f =
                new ForwardedMessage<>(
                        (SimulationNode) Tools.getNodeByID(2),
                        a,
                        Collections.emptyList());
        this.sendDirect(f, f.getDestination());
    }

    @Override
    protected void sendMessage(Supplier<SimulationMessage> m, SimulationNode n) {
    }

    private void handleMessageReceiving(Inbox inbox) {
        for (Message m : inbox) {
            if (m instanceof SimulationMessage) {
                this.handleMessageReceiving((SimulationMessage) m);
            }
        }
    }

    private void handleMessageReceiving(SimulationMessage m) {
        m.getNodes().push(this);
        String messageStr = m.getNodes().stream().map(Object::toString)
                .collect(Collectors.joining(", "));
        System.out.println(messageStr);
        MessageCache.push(m);
    }

    @Override
    public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
        this.setDefaultDrawingSizeInPixels(20);
        this.superDraw(g, pt, highlight);
    }

    @Override
    public void postStep() {
    }

    @Override
    public String toString() {
        return "[" + this.superToString() + ": " + String.format(
                "Rx=%d", this.getTotalReceivedMessages()) + "]";
    }

}
