package projects.tcc.simulation.wsn.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sinalgo.nodes.Position;

@Data
@EqualsAndHashCode(of = "index", callSuper = false)
@ToString(of = "index", callSuper = true)
public class DemandPoint extends Position {

    private final int index;
    private int coverage;

    public DemandPoint(int index, double x, double y, double z) {
        super(x, y, z);
        this.index = index;
    }

}
