package projects.tcc.nodes.messages;

import lombok.RequiredArgsConstructor;
import sinalgo.nodes.messages.Message;

@RequiredArgsConstructor
public class ActivationMessage extends Message {

    private final boolean[] activeSensors;

    public boolean isActive(int id) {
        return activeSensors[id];
    }

    @Override
    public Message clone() {
        return this;
    }

}
