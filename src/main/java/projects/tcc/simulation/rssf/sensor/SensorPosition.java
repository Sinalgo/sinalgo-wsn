package projects.tcc.simulation.rssf.sensor;

import sinalgo.nodes.Position;

public interface SensorPosition {
    Position getPosition();

    default void setPosition(Position position) {
        getPosition().assign(position);
    }

    default void setPosition(double x, double y, double z) {
        getPosition().assign(x, y, z);
    }
}
