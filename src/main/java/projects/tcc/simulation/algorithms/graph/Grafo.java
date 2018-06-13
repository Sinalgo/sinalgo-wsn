package projects.tcc.simulation.algorithms.graph;

import projects.tcc.simulation.rssf.Sensor;
import projects.tcc.simulation.rssf.Sink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Grafo {

    private List<Sensor> listSensores_Sink;
    private double[][] matrizConectividade;

    public Grafo(List<Sensor> listSensores, double[][] matrizConectividade) {
        this.listSensores_Sink = listSensores;
        this.matrizConectividade = matrizConectividade;
    }

    public void construirGrafo() {
        for (Sensor vertA : listSensores_Sink) {
            for (Sensor vertB : vertA.getNeighbors()) {
                vertA.addConnectionTo(vertB);
            }
        }

    }

    public void caminhosMinimosPara(Sensor sens) {
        if (sens instanceof Sink) {
            Dijkstra.computePaths(sens);
            registrarCustoCaminhoSens();
        } else
            System.out.println("O Caminho Minimo para o sensor escolhido nao foi calculado pois ele nao eh o sink.");

    }

    private void registrarCustoCaminhoSens() {
        for (Sensor vert : listSensores_Sink) {
            vert.setPathToSinkCost(vert.getMinDistance());
        }
    }

    public static List<Sensor> getShortestPathTo(Sensor target) {
        List<Sensor> path = new ArrayList<>();
        for (Sensor vertex = target; vertex != null; vertex = vertex.getPrevious())
            path.add(vertex);
        Collections.reverse(path);
        return path;
    }

    public void construirGrafoConect() {

        double penalidade = 2500;

        for (Sensor vertA : listSensores_Sink) {
            for (Sensor vertB : vertA.getNeighbors()) {
                if (!vertB.isFailed()) {
                    double vDistancia = matrizConectividade[(int) vertA.getID()][(int) vertB.getID()];
                    double peso = vertA.queryDistances(vDistancia);

                    if ((vertA.isActive() && !vertB.isActive()) ||
                            (!vertA.isActive() && vertB.isActive()))
                        peso = peso * penalidade;

                    if (!vertA.isActive() && !vertB.isActive())
                        peso = peso * penalidade * penalidade;

                    vertA.addConnectionTo(vertB, peso);
                }
            }
        }

    }

}
