package projects.tcc.nodes.messages;

import lombok.Getter;
import projects.tcc.nodes.SimulationNode;
import sinalgo.nodes.messages.Message;

import java.util.ArrayDeque;
import java.util.Deque;

public class SimulationMessage extends Message {

    @Getter
    private Deque<SimulationNode> nodes = new ArrayDeque<>();

    @Override
    public Message clone() {
        return this;
    }

    public void reset() {
        this.getNodes().clear();
    }
}
