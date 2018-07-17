package projects.tcc.simulation.rssf.sensor;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class GraphNodeProperties {
    private Long parentId;
    private final Map<Long, Double> pathToSinkCost = new HashMap<>();

    public void reset() {
        this.setParentId(null);
        this.getPathToSinkCost().clear();
    }
}
