package projects.tcc.nodes.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import projects.tcc.nodes.SimulationNode;
import sinalgo.nodes.messages.Message;

@RequiredArgsConstructor
@Getter
public class ActivationMessage extends Message {

    private final boolean active;
    private final SimulationNode parent;

    @Override
    public Message clone() {
        return this;
    }

}
