package projects.tcc.simulation.rssf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Getter(AccessLevel.PRIVATE)
@Setter(AccessLevel.PRIVATE)
public class RedeSensor {

    public boolean suprirCobertura() {
        // Utilizado na vers�o OnlineH�brido

        for (Sensor s : activeSensors) {
            if (!s.isConnected())
                atualizaCoberturaSemConec(s);
        }

        boolean retorno = true;

        double nPontoDemanda = coverageMatrix.length;

        ArrayList<Sensor> listSensorDesconex = new ArrayList<>();

        double fatorPontoDemanda = numPontosCobertos;

        while (fatorPontoDemanda / nPontoDemanda < fatorCob) {

            Sensor sensEscolhido = escolherSensorSubstituto(listSensorDesconex);

            if (sensEscolhido != null) {
                ligaSensor(sensEscolhido);
                createConnection();

                if (sensEscolhido.getParent() == null) {
                    //Impossivel conectar o sensor na rede
                    listSensorDesconex.add(sensEscolhido);
                    desligarSensor(sensEscolhido);
                    continue;
                }
                //possivel problema que pode ocorrer.
                else if (sensEscolhido.getParent().isFailed()) {
                    //Impossivel conectar o sensor na rede
                    listSensorDesconex.add(sensEscolhido);
                    desligarSensor(sensEscolhido);
                    continue;
                } else {
                    System.out.println("Sensor Escolhido = " + sensEscolhido);


                    if (!(sensEscolhido.getParent() instanceof Sink)) {
                        atualizarListaPontosCobExclusivo(sensEscolhido.getParent());
                    }

                }
                fatorPontoDemanda = numPontosCobertos;
            } else {
                //nao ha sensores para ativar
                System.out.println("Nao ha mais sensores para ativar e suprir a cobertura");
                fatorPontoDemanda = nPontoDemanda;
                retorno = false;
            }

        }// end While


        calcCobertura();

        return retorno;
    }


    private Sensor escolherSensorSubstituto(ArrayList<Sensor> listSensorDesconex) {
        Sensor sensEscolhido = null;
        int maiorNumPontCobDescob = 0;
        for (Sensor sens : availableSensors) {

            if (!listSensorDesconex.contains(sens)) {
                if (!sens.isActive()) {
                    if (sens.isFailed()) {
                        System.out.println("Acessando Sensor Falho na lista de Sensores Dispon�veis");
                        System.out.println("suprirCoberturaSeNecessario() - RedeSensor");
                        System.exit(1);
                    }

                    int numPontCobDescob = atualizarListaPontosCobExclusivo(sens);

                    if (numPontCobDescob > maiorNumPontCobDescob) {
                        sensEscolhido = sens;
                        maiorNumPontCobDescob = numPontCobDescob;
                    }

                }
            }
        }
        return sensEscolhido;
    }

    private void ligaSensor(Sensor sensEscolhido) {
        sensEscolhido.setActive(true);
        activeSensors.add(sensEscolhido);
        atualizaCoberturaSemConec(sensEscolhido);

    }


    public void createInitialNetwork(boolean[] vetBoolean) {
        ativarSensoresVetBits(vetBoolean);

        // criando a conectividade inicial das redes e atualizando a cobertura.
        createConnection();
        // calculo da cobertura sem conectividade.
        calcCoberturaInicial();
        //calcCobertura();

        // ========= Verificacao se ha pontos descobertos =========
        if (porcCobAtual < fatorCob)
            suprirCobertura();

    }


    public boolean suprirOnline() {
        for (Sensor sensFalho : listSensFalhosNoPer) {
            Sensor sensorEscolhido = escolherSubs(sensFalho);
            if (sensorEscolhido == null)
                break;
            ligaSensor(sensorEscolhido);
            boolean fezConex = connectOnlineSensor(sensorEscolhido, sensFalho);
            if (!fezConex) {
                createConnection();
            }
        }
        calcCobertura();
        if (this.porcCobAtual >= this.fatorCob) {
            return true;
        } else {
            System.out.println("Não foi possível suprimir cobertura Online");
            return false;
        }
    }


    private boolean connectOnlineSensor(Sensor sensorEscolhido, Sensor sensFalho) {
        boolean result = true;
        if (sensorEscolhido.getNeighbors().contains(sensFalho.getParent())) {
            sensorEscolhido.addConnectionTo(sensFalho.getParent());
            sensFalho.getParent().addChild(sensorEscolhido);
            if (sensFalho.getParent().isConnected() || sensFalho.getParent() instanceof Sink)
                sensorEscolhido.setConnected(true);
        } else {
            result = false;
        }

        List<Sensor> listSensorReconex = new ArrayList<>();
        for (Sensor sensFilho : sensFalho.getChildren()) {
            if (!sensFilho.isConnected())
                if (sensorEscolhido.getNeighbors().contains(sensFilho)) {
                    sensorEscolhido.addChild(sensFilho);
                    sensFilho.addConnectionTo(sensorEscolhido);
                    sensFilho.setConnected(true);
                    listSensorReconex.addAll(sensFilho.connectChildren());
                } else
                    result = false;
        }

        for (Sensor sensReconex : listSensorReconex) {
            atualizaCoberturaSemConec(sensReconex);
        }

        return result;
    }


    private Sensor escolherSubs(Sensor sensFalho) {
        Sensor sensEsc = null;
        double squaredDist = Double.MAX_VALUE;
        for (Sensor candidate : sensFalho.getNeighbors()) {
            if (!candidate.isActive() && !candidate.isFailed()) {
                double auxDist = 0;
                double parentDist = connectivityMatrix[(int) candidate.getID()][(int) sensFalho.getParent().getID()];

                auxDist = auxDist + Math.pow(parentDist, 2);

                for (Sensor sensFilho : sensFalho.getChildren()) {
                    double distFilho = connectivityMatrix[(int) candidate.getID()][(int) sensFilho.getID()];
                    auxDist = auxDist + Math.pow(distFilho, 2);
                }

                if (Double.compare(auxDist, squaredDist) < 0) {
                    squaredDist = auxDist;
                    sensEsc = candidate;
                }

            }

        }

        return sensEsc;
    }


}




