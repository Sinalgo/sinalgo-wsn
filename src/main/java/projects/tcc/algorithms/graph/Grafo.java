package projects.tcc.algorithms.graph;

import projects.tcc.rssf.Sensor;
import projects.tcc.rssf.Sink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Grafo {

    private ArrayList<Sensor> listSensores_Sink;
    private double[][] matrizConectividade;

    public Grafo(ArrayList<Sensor> listSensores, double[][] matrizConectividade) {
        this.listSensores_Sink = listSensores;
        this.matrizConectividade = matrizConectividade;
    }

    public void construirGrafo() {
        for (Sensor vertA : listSensores_Sink) {
            for (Sensor vertB : vertA.getListSensVizinhos()) {
                double vDistancia = matrizConectividade[vertA.getId()][vertB.getId()];
                double peso = vertA.BuscaCorrente_Distancia(vDistancia);
                vertA.adjacencies.add(new Edge(vertB, peso));
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
            vert.setCustoCaminhoSink(vert.minDistance);
        }
    }

    public static List<Sensor> getShortestPathTo(Sensor target) {
        List<Sensor> path = new ArrayList<>();
        for (Sensor vertex = target; vertex != null; vertex = vertex.previous)
            path.add(vertex);
        Collections.reverse(path);
        return path;
    }

    public void construirGrafoConect() {

        double penalidade = 2500;

        for (Sensor vertA : listSensores_Sink) {
            for (Sensor vertB : vertA.getListSensVizinhos()) {
                if (!vertB.isFalho()) {
                    double vDistancia = matrizConectividade[vertA.getId()][vertB.getId()];
                    double peso = vertA.BuscaCorrente_Distancia(vDistancia);

                    //	if (vertA.isAtivo() && vertB.isAtivo())
                    //		peso = peso;

                    if ((vertA.isAtivo() && !vertB.isAtivo()) ||
                            (!vertA.isAtivo() && vertB.isAtivo()))
                        peso = peso * penalidade;

                    if (!vertA.isAtivo() && !vertB.isAtivo())
                        peso = peso * penalidade * penalidade;

                    vertA.adjacencies.add(new Edge(vertB, peso));
                }
            }
        }

    }

}
