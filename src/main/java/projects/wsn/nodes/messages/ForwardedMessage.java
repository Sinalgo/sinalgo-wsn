package projects.wsn.nodes.messages;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import projects.wsn.nodes.SimulationNode;
import sinalgo.nodes.messages.Message;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ForwardedMessage<T extends Message & SizedMessage> extends Message implements SizedMessage {

    private final SimulationNode destination;
    private final T message;
    private final List<ForwardedMessage<T>> forwardedMessages;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
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
