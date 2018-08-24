package projects.tcc;

import projects.tcc.nodes.messages.SimulationMessage;

import java.util.ArrayDeque;
import java.util.Deque;

public class MessageCache {
    private static Deque<SimulationMessage> freeMessages = new ArrayDeque<>();

    public static SimulationMessage pop() {
        if (freeMessages.isEmpty()) {
            return new SimulationMessage();
        }
        return freeMessages.pop();
    }

    public static void push(SimulationMessage message) {
        message.reset();
        freeMessages.push(message);
    }
}
