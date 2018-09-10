package projects.tcc.nodes.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import projects.tcc.nodes.SimulationNode;
import sinalgo.nodes.messages.Message;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ActivationMessage extends Message {

    private final boolean active;
    private final int waitTime;
    private final SimulationNode parent;
    private final List<SimulationNode> children;

    @Override
    public Message clone() {
        return this;
    }

}
