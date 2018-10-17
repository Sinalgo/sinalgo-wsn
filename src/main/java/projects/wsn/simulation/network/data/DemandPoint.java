package projects.wsn.simulation.network.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;
import sinalgo.nodes.Position;

@Data
@EqualsAndHashCode(of = "index", callSuper = false)
@ToString(of = "index", callSuper = true)
public class DemandPoint extends Position {

    private final int index;

    @Setter(AccessLevel.NONE)
    private int coverage;

    public DemandPoint(int index, double x, double y, double z) {
        super(x, y, z);
        this.index = index;
        this.coverage = 0;
    }

    public void addCoverage() {
        this.coverage += 1;
    }

    public void removeCoverage() {
        this.coverage -= 1;
    }

}
