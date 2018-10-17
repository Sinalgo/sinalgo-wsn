package projects.wsn.nodes.messages;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import projects.wsn.nodes.SimulationNode;
import sinalgo.nodes.messages.Message;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class ActivationMessage extends Message implements SizedMessage {

    private final boolean active;
    private final int waitTime;
    private final SimulationNode parent;
    private final List<SimulationNode> children;

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
        // 8 bit for active, 32 for waitTime, 32 for parent
        int size = 8 + 32 + 32;
        if (this.getChildren() != null) {
            // 32 more for each child in the list
            size += children.size() * 32;
        }
        this.size = size;
        return size;
    }

}
