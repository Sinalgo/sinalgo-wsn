package projects.tcc.simulation.algorithms.graph;

import projects.tcc.simulation.wsn.data.WSNSensor;
import projects.tcc.simulation.wsn.data.WSNSink;

import java.util.List;

public class Grafo {

    private List<WSNSensor> listSensores_Sink;
    private double[][] matrizConectividade;

    public Grafo(List<WSNSensor> listSensores, double[][] matrizConectividade) {
        this.listSensores_Sink = listSensores;
        this.matrizConectividade = matrizConectividade;
    }

    public void construirGrafo() {
        for (WSNSensor vertA : this.listSensores_Sink) {
            for (WSNSensor vertB : vertA.getNeighborhood()) {
                double vDistancia = this.matrizConectividade[vertA.getWsnSensorId()][vertB.getWsnSensorId()];
                double peso = WSNSensor.getCurrentPerDistance(vDistancia);
                vertA.getAdjacencies().add(new GraphEdge(vertB, peso));
            }
        }
    }

    public void caminhosMinimosPara(WSNSensor sens) {
        if (sens instanceof WSNSink) {
            Dijkstra.computePaths(sens);
            this.registrarCustoCaminhoSens();
        } else {
            System.out.println("O Caminho Mínimo para o sensor escolhido nao foi calculado pois ele nao é o WSNSink.");
        }
    }

    private void registrarCustoCaminhoSens() {
        for (WSNSensor vert : this.listSensores_Sink) {
            vert.setCostToSink(vert.getMinDistance());
        }
    }

    public void construirGrafoConect() {
        double penalidade = 2500;
        for (WSNSensor vertA : this.listSensores_Sink) {
            for (WSNSensor vertB : vertA.getNeighborhood()) {
                if (!vertB.isFailed()) {
                    double vDistancia = this.matrizConectividade[vertA.getWsnSensorId()][vertB.getWsnSensorId()];
                    double peso = WSNSensor.getCurrentPerDistance(vDistancia);
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
