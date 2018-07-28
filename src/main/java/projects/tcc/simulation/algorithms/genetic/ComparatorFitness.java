package projects.tcc.simulation.algorithms.genetic;

import java.util.Comparator;

public class ComparatorFitness implements Comparator<Cromossomo> {

    private FitnessType fitnessType;

    public ComparatorFitness(FitnessType fitnessType) {
        this.fitnessType = fitnessType;
    }

    public int compare(Cromossomo cromo1, Cromossomo cromo2) {
        return Double.compare(cromo1.getFitness(this.fitnessType), cromo2.getFitness(this.fitnessType));
    }

}
