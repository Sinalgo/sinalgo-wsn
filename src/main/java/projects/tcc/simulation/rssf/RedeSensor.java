package projects.tcc.simulation.rssf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;


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


}




