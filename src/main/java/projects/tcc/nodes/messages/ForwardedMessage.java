package projects.tcc.nodes.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import projects.tcc.nodes.SimulationNode;
import sinalgo.nodes.messages.Message;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ForwardedMessage<T extends Message> extends Message {

    private final SimulationNode destination;
    private final T message;
    private final List<ForwardedMessage<T>> forwardedMessages;

    @Override
    public Message clone() {
        return this;
    }
}
