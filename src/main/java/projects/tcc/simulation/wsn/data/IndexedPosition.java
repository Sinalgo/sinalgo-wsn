package projects.tcc.simulation.wsn.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sinalgo.nodes.Position;

@Data
@EqualsAndHashCode(of = "index", callSuper = false)
@ToString(of = "index", callSuper = true)
public class IndexedPosition extends Position {

    private final int index;

    public IndexedPosition(int index, double x, double y, double z) {
        super(x, y, z);
        this.index = index;
    }

}
