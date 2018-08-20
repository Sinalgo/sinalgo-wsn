package projects.tcc.nodes.messages;

import lombok.Getter;
import sinalgo.nodes.messages.Message;

import java.util.ArrayDeque;
import java.util.Deque;

public class SimulationMessage extends Message {

    @Getter
    private Deque<String> nodes = new ArrayDeque<>();

    @Override
    public Message clone() {
        return this;
    }
}
