package projects.tcc.simulation.rssf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import sinalgo.nodes.Position;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Environment {

    private final double area;
    private final long height, width;
    private final double coverageFactor;

    private final List<Position> positions;

    @Setter(AccessLevel.NONE)
    private double currentCoverage;

    public Environment(long height, long width, double coverageFactor) {
        this.height = height;
        this.width = width;
        this.coverageFactor = coverageFactor;
        this.area = height * width;
        this.positions = new ArrayList<>();
        this.currentCoverage = 0;
        this.generatePositions();
    }

    private void generatePositions() {
        for (long i = 0; i < this.getHeight(); i++) {
            for (long j = 0; j < this.getWidth(); j++) {
                this.getPositions().add(new Position(i + 0.5, j + 0.5, 0));
            }
        }
    }
    
}
