package projects.wsn.nodes.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import projects.wsn.nodes.SimulationNode;
import sinalgo.nodes.messages.Message;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ActivationMessage extends Message {

    private final SimulationNode parent;
    private final List<SimulationNode> children;

    @Override
    public Message clone() {
        return this;
    }

}
