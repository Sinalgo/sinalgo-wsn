package projects.tcc.simulation.algorithms.genetic;

import java.util.Comparator;

public class ComparatorPareto implements Comparator<Cromossomo> {

    public int compare(Cromossomo cromo1, Cromossomo cromo2) {
        return Integer.compare(cromo1.getIdPareto(), cromo2.getIdPareto());
    }

}
