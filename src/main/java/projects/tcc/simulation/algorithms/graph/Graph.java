package projects.tcc.simulation.algorithms.graph;

import projects.tcc.simulation.wsn.data.Sensor;
import projects.tcc.simulation.wsn.data.Sink;

import java.util.List;

public class Graph {

    private List<Sensor> listSensores_Sink;
    private double[][] matrizConectividade;

    public Graph(List<Sensor> listSensores, double[][] matrizConectividade) {
        this.listSensores_Sink = listSensores;
        this.matrizConectividade = matrizConectividade;
    }

    public void build() {
        for (Sensor vertA : this.listSensores_Sink) {
            for (Sensor vertB : vertA.getNeighborhood()) {
                double vDistancia = this.matrizConectividade[vertA.getSensorId()][vertB.getSensorId()];
                double peso = Sensor.getCurrentPerDistance(vDistancia);
                vertA.getAdjacencies().add(new GraphEdge(vertB, peso));
            }
        }
    }

    public void computeMinimalPathsTo(Sensor sens) {
        if (sens instanceof Sink) {
            Dijkstra.computePaths(sens);
            this.registrarCustoCaminhoSens();
        } else {
            System.out.println("O Caminho Mínimo para o sensor escolhido nao foi calculado pois ele nao é o Sink.");
        }
    }

    private void registrarCustoCaminhoSens() {
        for (Sensor vert : this.listSensores_Sink) {
            vert.setCostToSink(vert.getMinDistance());
        }
    }

    public void construirGrafoConect() {
        double penalidade = 2500;
        for (Sensor vertA : this.listSensores_Sink) {
            for (Sensor vertB : vertA.getNeighborhood()) {
                if (!vertB.isFailed()) {
                    double vDistancia = this.matrizConectividade[vertA.getSensorId()][vertB.getSensorId()];
                    double peso = Sensor.getCurrentPerDistance(vDistancia);
                    if ((vertA.isActive() && !vertB.isActive()) ||
                            (!vertA.isActive() && vertB.isActive())) {
                        peso = peso * penalidade;
                    } else if (!vertA.isActive() && !vertB.isActive()) {
                        peso = peso * penalidade * penalidade;
                    }
                    vertA.getAdjacencies().add(new GraphEdge(vertB, peso));
                }
            }
        }
    }
}
