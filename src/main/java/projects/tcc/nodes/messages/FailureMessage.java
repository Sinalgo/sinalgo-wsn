package projects.tcc.nodes.messages;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import projects.tcc.nodes.nodeImplementations.SensorNode;

@RequiredArgsConstructor
public class FailureMessage extends SimulationMessage {

    @Getter
    private final SensorNode failedNode;

}
