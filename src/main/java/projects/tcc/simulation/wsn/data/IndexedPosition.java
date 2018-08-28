package projects.tcc.simulation.wsn.data;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import sinalgo.nodes.Position;

@Data
@EqualsAndHashCode(of = "ID", callSuper = false)
@ToString(of = "ID", callSuper = true)
public class IndexedPosition extends Position {

    private static int nextID = 0;

    private final int ID = nextID++;

    public IndexedPosition(double x, double y, double z) {
        super(x, y, z);
    }

    public static void resetCounter() {
        nextID = 0;
    }
}
