package projects.tcc.models.distributionModels;

import sinalgo.models.DistributionModel;
import sinalgo.nodes.Position;

public class DummyPositionModel extends DistributionModel {

    public DummyPositionModel() {
    }

    @Override
    public Position getNextPosition() {
        return new Position(0, 0, 0);
    }
}
