package projects.tcc.simulation.rssf;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import projects.tcc.simulation.rssf.sensor.Sensor;
import sinalgo.nodes.Position;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true, exclude = {"coveringSensors", "connectedCoveringSensors"})
@ToString(callSuper = true, exclude = {"coveringSensors", "connectedCoveringSensors"})
@NoArgsConstructor
public class RSSFPosition extends Position {
    private final Set<Sensor> coveringSensors = new HashSet<>();
    private final Set<Sensor> connectedCoveringSensors = new HashSet<>();

    public RSSFPosition(double xCoord, double yCoord, double zCoord) {
        super(xCoord, yCoord, zCoord);
    }
}
