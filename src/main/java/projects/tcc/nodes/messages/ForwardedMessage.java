package projects.tcc.nodes.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import projects.tcc.nodes.SimulationNode;
import sinalgo.nodes.messages.Message;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ForwardedMessage extends Message {

    private final SimulationNode destination;
    private final int waitTime;
    private final ActivationMessage message;
    private final List<ForwardedMessage> forwardedMessages;

    @Override
    public Message clone() {
        return this;
    }
}
