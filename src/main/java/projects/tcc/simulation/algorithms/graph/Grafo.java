package projects.tcc.simulation.algorithms.graph;

import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import projects.tcc.simulation.rssf.Sensor;
import projects.tcc.simulation.rssf.Sink;

import java.util.List;


public class Grafo {

    private List<Sensor> listSensores_Sink;
    private double[][] matrizConectividade;

    private static final double PENALIDADE = 2500;

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

    public void construirGrafoConect() {
        DefaultDirectedWeightedGraph<Long, DefaultWeightedEdge> graph =
                new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
        listSensores_Sink.forEach(s -> graph.addVertex(s.getID()));
        for (Sensor vertA : listSensores_Sink) {
            for (Sensor vertB : vertA.getNeighbors()) {
                if (!vertB.isFailed()) {
                    double vDistancia = matrizConectividade[(int) vertA.getID()][(int) vertB.getID()];
                    double peso = vertA.queryDistances(vDistancia);

                    if ((vertA.isActive() && !vertB.isActive()) ||
                            (!vertA.isActive() && vertB.isActive()))
                        peso = peso * PENALIDADE;

                    if (!vertA.isActive() && !vertB.isActive())
                        peso = peso * PENALIDADE * PENALIDADE;

                    graph.setEdgeWeight(graph.addEdge(vertA.getID(), vertB.getID()), peso);
                }
            }
        }

        DijkstraShortestPath<Long, DefaultWeightedEdge> dijkstra = new DijkstraShortestPath<>(graph);
        Sink sink = (Sink) listSensores_Sink.get(0);
        listSensores_Sink.subList(1, listSensores_Sink.size())
                .forEach(s -> s.setMinDistance(dijkstra.getPathWeight(s.getID(), sink.getID())));
    }

}
