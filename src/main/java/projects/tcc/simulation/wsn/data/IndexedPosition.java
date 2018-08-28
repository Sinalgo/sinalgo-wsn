package projects.tcc.simulation.wsn.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sinalgo.nodes.Position;

@Data
@EqualsAndHashCode(of = "ID", callSuper = false)
@ToString(of = "ID", callSuper = true)
public class IndexedPosition extends Position {

    private final int ID;

    public IndexedPosition(int ID, double x, double y, double z) {
        super(x, y, z);
        this.ID = ID;
    }

}
