package projects.tcc.nodes.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sinalgo.nodes.messages.Message;

@RequiredArgsConstructor
public class ActivationMessage extends Message {

    @Getter
    private final boolean[] activeSensors;

    @Override
    public Message clone() {
        return this;
    }

}
