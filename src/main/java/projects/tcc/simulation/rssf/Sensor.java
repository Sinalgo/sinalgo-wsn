package projects.tcc.simulation.rssf;

import projects.tcc.simulation.algorithms.graph.Edge;

import java.util.ArrayList;

public class Sensor implements Comparable<Sensor> {

    private int id;
    private double posX;
    private double posY;
    private double energiaBat;
    private double energiaOriginal;
    private ArrayList<Sensor> listFilhos;
    private Sensor sensorPai;
    private double raioSensoriamento;
    private double raioComunicacao;
    private boolean ativo;
    private boolean bitEA;
    private boolean conex;
    private boolean falho;

    private double potAtiv;
    private double potRec;
    private double potManut;
    private double taxaCom; //Taxa de comunica��o durante a transmiss�o em uma u.t.

    private ArrayList<Sensor> listSensVizinhos;
    private ArrayList<Integer> listPontosCobertos;
    private ArrayList<Integer> listPontosCobExclusivo;
    private double custoCaminhoSink;

    public ArrayList<Edge> adjacencies;
    public double minDistance;
    public Sensor previous;


    public Sensor(int id, double posX, double posY, double raioComunicacao, double taxaCom) {

        this.id = id;
        this.posX = posX;
        this.posY = posY;
        this.raioComunicacao = raioComunicacao;

        this.ativo = true;

        this.listFilhos = new ArrayList<>();
        this.listSensVizinhos = new ArrayList<>();

        this.adjacencies = new ArrayList<>();
        this.minDistance = Double.POSITIVE_INFINITY;

        this.taxaCom = taxaCom;

    }

    public Sensor(int id, double posX, double posY, double raioSensoriamento, double raioComunicacao,
                  double energiaBat, double potAtiv, double potRec, double potManut, double taxaCom) {

        this(id, posX, posY, raioComunicacao, taxaCom);

        this.potAtiv = potAtiv;
        this.potRec = potRec;
        this.potManut = potManut;

        this.energiaBat = energiaBat;
        energiaOriginal = energiaBat;
        this.raioSensoriamento = raioSensoriamento;

        this.sensorPai = null; //-1 n�o usado  | -2 sem possibilidade de ter pai  | -3 pai retirado  |

        this.ativo = false;
        this.falho = false;
        this.conex = false;

        this.listPontosCobertos = new ArrayList<>();
        this.listPontosCobExclusivo = new ArrayList<>();
    }

    public void reiniciarSensorParaConectividade() {
        this.sensorPai = null;
        this.previous = null;
        this.conex = false;
        this.adjacencies.clear();
        this.minDistance = Double.POSITIVE_INFINITY;
        this.listFilhos.clear();
    }

    public int compareTo(Sensor other) {
        return Double.compare(minDistance, other.minDistance);
    }

    public String toString() {
        return String.valueOf(id);
    }

    public void adicionaFilho(Sensor sensFilho) {
        listFilhos.add(sensFilho);
    }

    public ArrayList<Sensor> getListFilhos() {
        return listFilhos;
    }

    public void setListFilhos(ArrayList<Sensor> listFilhos) {
        this.listFilhos = listFilhos;
    }

    public Sensor getSensorPai() {
        return sensorPai;
    }

    public void setSensorPai(Sensor sensorPai) {
        this.sensorPai = sensorPai;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        if (!this.ativo && ativo) {
            this.bitEA = ativo;
        }
        this.ativo = ativo;
    }

    public boolean isBitEA() {
        return bitEA;
    }

    public void setBitEA(boolean bitEA) {
        this.bitEA = bitEA;
    }

    public boolean isConex() {
        return conex;
    }

    public void setConex(boolean conex) {
        this.conex = conex;
    }

    public boolean isFalho() {
        return falho;
    }

    public void setFalho(boolean falho) {
        this.falho = falho;
    }

    public ArrayList<Sensor> getListSensVizinhos() {
        return listSensVizinhos;
    }

    public void setListSensVizinhos(ArrayList<Sensor> listSensVizinhos) {
        this.listSensVizinhos = listSensVizinhos;
    }

    public ArrayList<Integer> getListPontosCobertos() {
        return listPontosCobertos;
    }

    public void setListPontosCobertos(ArrayList<Integer> listPontosCobertos) {
        this.listPontosCobertos = listPontosCobertos;
    }

    public ArrayList<Integer> getListPontosCobExclusivo() {
        return listPontosCobExclusivo;
    }

    public void setListPontosCobExclusivo(ArrayList<Integer> listPontosCobExclusivo) {
        this.listPontosCobExclusivo = listPontosCobExclusivo;
    }

    public int getId() {
        return id;
    }

    public double getPosX() {
        return posX;
    }

    public double getPosY() {
        return posY;
    }

    public double getEnergiaBat() {
        return energiaBat;
    }

    public double getEnergiaOriginal() {
        return energiaOriginal;
    }

    public double getRaioSensoriamento() {
        return raioSensoriamento;
    }

    public double getRaioComunicacao() {
        return raioComunicacao;
    }

    public double getPotAtiv() {
        return potAtiv;
    }

    public double getPotRec() {
        return potRec;
    }

    public double getPotManut() {
        return potManut;
    }

    public double getTaxaCom() {
        return taxaCom;
    }

    public double getCustoCaminhoSink() {
        return custoCaminhoSink;
    }

    public void setCustoCaminhoSink(double custoCaminhoSink) {
        this.custoCaminhoSink = custoCaminhoSink;
    }

    public int BuscaDescendentes() {

        int TotalFilhos = this.listFilhos.size();

        for (Sensor sensFilho : listFilhos) {
            TotalFilhos += sensFilho.BuscaDescendentes();
        }
        return (TotalFilhos);
    }

    //Vetor de Corrente x dist�ncia

    public double BuscaCorrente_Distancia(double Distancia) {

        double VetorDistancias[] = {
                5.142,
                5.769,
                6.473,
                7.263,
                8.150,
                9.144,
                10.260,
                11.512,
                12.916,
                14.492,
                16.261,
                18.245,
                20.471,
                22.969,
                25.771,
                28.916,
                32.444,
                36.403,
                40.845,
                45.829,
                51.420,
                57.695,
                64.735,
                72.633,
                81.496,
                91.440};

        double VetorCorrente[] = {
                8.6,
                8.8,
                9.0,
                9.0,
                9.1,
                9.3,
                9.3,
                9.5,
                9.7,
                9.9,
                10.1,
                10.4,
                10.6,
                10.8,
                11.1,
                13.8,
                14.5,
                14.5,
                15.1,
                15.8,
                16.8,
                17.2,
                18.5,
                19.2,
                21.3,
                25.4,
        };

        int i = 0;

        while (VetorDistancias[i] <= Distancia) {
            i++;
            if (i == VetorDistancias.length) {
                System.out.println("\n\nERROR: Dist�ncia ao Pai n�o informada corretamente");
                System.out.println("Valor da Dist�ncia: " + Distancia);
                //System.exit(-1);
            }


        }

        return (VetorCorrente[i]);
    }


    public void retirarEnergGastaPeriodo(double Valor) {

        energiaBat -= Valor;
        if (energiaBat < 0)
            energiaBat = 0;
    }

    public double getGastoTransmissao(double vDistanciaAoPai, int vNumeroFilhos2) {
        // TODO Auto-generated method stub

        double vCorrente = BuscaCorrente_Distancia(vDistanciaAoPai);

        return taxaCom * vCorrente * (vNumeroFilhos2 + 1);
    }

    public void desconectafilhos() {
        // TODO Auto-generated method stub
        for (Sensor sFilho : this.getListFilhos()) {
            sFilho.setConex(false);
            sFilho.desconectafilhos();
        }
        //listFilhos.clear();
    }

    public void conectafilhos(ArrayList<Sensor> listSensorReconex) {
        // TODO Auto-generated method stub
        for (Sensor sFilho : this.getListFilhos()) {
            sFilho.setConex(true);
            listSensorReconex.add(sFilho);
            sFilho.conectafilhos(listSensorReconex);
        }
        //listFilhos.clear();
    }


}
