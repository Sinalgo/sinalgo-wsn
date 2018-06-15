package projects.tcc.simulation.algorithms.genetic;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.Comparator;

public class ComparatorFitness implements Comparator<Cromossomo> {

    public enum FitnessType {
        TYPE_1, TYPE_2
    }

    @Getter(AccessLevel.PRIVATE)
    private final FitnessType fitnessType;

    public ComparatorFitness(FitnessType fitnessType) {
        this.fitnessType = fitnessType;
    }

    public int compare(Cromossomo cromo1, Cromossomo cromo2) {
        return Double.compare(cromo1.getFitnessOfType(fitnessType), cromo2.getFitnessOfType(fitnessType));
    }

}
