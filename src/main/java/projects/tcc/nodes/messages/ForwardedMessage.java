package projects.tcc.nodes.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import projects.tcc.nodes.SimulationNode;
import sinalgo.nodes.messages.Message;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ForwardedMessage<T extends Message & SizedMessage> extends Message implements SizedMessage {

    private final SimulationNode destination;
    private final T message;
    private final List<ForwardedMessage<T>> forwardedMessages;
    private Integer size;

    @Override
    public Message clone() {
        return this;
    }

    @Override
    public int calculateSize() {
        if (this.size != null) {
            return this.size;
        }
        // 32 bit for the destination, plus the size of the message itself
        int size = 32 + this.getMessage().calculateSize();
        if (this.getForwardedMessages() != null) {
            for (ForwardedMessage<T> m : this.getForwardedMessages()) {
                size += m.calculateSize();
            }
        }
        this.size = size;
        return size;
    }
}
