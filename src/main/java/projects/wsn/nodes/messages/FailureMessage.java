package projects.wsn.nodes.messages;

import lombok.NoArgsConstructor;
import sinalgo.nodes.messages.Message;

@NoArgsConstructor
public class FailureMessage extends Message {
    @Override
    public Message clone() {
        return this;
    }
}
