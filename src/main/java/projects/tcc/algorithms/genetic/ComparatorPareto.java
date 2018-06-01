package projects.tcc.algorithms.genetic;

import java.util.Comparator;

public class ComparatorPareto implements Comparator<Cromossomo> {

    public int compare(Cromossomo cromo1, Cromossomo cromo2) {

        double difFitness = cromo1.getIdPareto() - cromo2.getIdPareto();

        if (difFitness == 0)
            return 0;
        else if (difFitness > 0)
            return 1;
        else
            return -1;

    }

}
