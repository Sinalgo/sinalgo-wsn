package projects.tcc.simulation.rssf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import projects.tcc.simulation.algorithms.graph.GraphHolder;
import projects.tcc.simulation.data.SensorHolder;
import sinalgo.nodes.Position;
import sinalgo.tools.logging.Logging;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SensorNetwork {

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static double availableEnergy;

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static Environment environment;

    public static void init(Environment environment) {
        computeDistances();
        computeNeighbors();
        setEnvironment(environment);
    }

    private static void computeDistances() {
        Collection<Sensor> allSensorsAndSinks = SensorHolder.getAllSensorsAndSinks().values();
        allSensorsAndSinks.forEach(s1 -> allSensorsAndSinks.forEach(s2 -> computeDistance(s1, s2)));
    }

    private static void computeDistance(Sensor s1, Sensor s2) {
        if (!Objects.equals(s1, s2)) {
            s1.getDistances().computeIfAbsent(s2.getID(), v ->
                    s2.getDistances().computeIfAbsent(s1.getID(), v2 ->
                            s1.getPosition().distanceTo(s2.getPosition())));
        }
    }

    private static void computeNeighbors() {
        SensorHolder.getAvailableSensors().values().forEach(s -> s.getDistances().forEach((k, v) -> {
            if (Double.compare(v, s.getCommRadius()) < 0) {
                s.getNeighbors().put(k, SensorHolder.getAllSensorsAndSinks().get(k));
            }
        }));
    }

    public static void updateAvailableEnergy() {
        setAvailableEnergy(SensorHolder.getAvailableSensors().values().stream()
                .mapToDouble(Sensor::getBatteryEnergy)
                .sum());
    }

    public static void updateActiveSensors(boolean[] vetBoolean) {
        SensorHolder.getAvailableSensors().values().forEach(s -> s.setActive(vetBoolean[(int) s.getID()]));
        SensorHolder.updateCollections();
    }

    /**
     * This should be executed AFTER the AG has decided which Sensors should be activated/connected.
     * It needs to use the GraphNodeProperties of each Sensor in order to determine which ones will
     * be used in this method.
     */
    public static void updateConnections() {
        SensorHolder.getActiveSensors().values().forEach(Sensor::resetConnectivity);
        SensorHolder.getSinks().values().forEach(Sensor::resetConnectivity);
        SensorHolder.getActiveSensors().values().forEach(s -> {
            s.setParent(SensorHolder.getAllSensorsAndSinks().get(s.getGraphNodeProperties().getParentId()));
            if (s.getParent() != null) {
                s.getParent().addChild(s);
            }
        });
        SensorHolder.getSinks().values().forEach(Sensor::connectAndPropagate);
        SensorHolder.getSinks().values().forEach(Sensor::queryDescendants);
        SensorHolder.getActiveSensors().values().forEach(s -> s.setActive(s.isActive() && s.isConnected()));
        SensorHolder.updateCollections();
    }

    public static double getActivationEnergyForThisRound() {
        return SensorHolder.getPreviousRoundActivatedSensors().values().stream()
                .mapToDouble(Sensor::getActivationPower)
                .sum();
    }

    private static double getConsumedEnergy(Sensor s) {
        long totalChildrenCount = s.getTotalChildrenCount();
        double receiveEnergy = s.getReceivePower() * totalChildrenCount;

        double distanceToParent = s.getDistances().get(s.getParent().getID());
        double current = Sensor.getCurrentForDistance(distanceToParent);

        double transmissionEnergy = s.getCommRatio() * current * (totalChildrenCount + 1);
        double maintenanceEnergy = s.getMaintenancePower();

        return receiveEnergy + transmissionEnergy + maintenanceEnergy;
    }

    public static void updateRemainingBatteryEnergy() {
        SensorHolder.getActiveSensors().values().forEach(s -> s.subtractEnergySpent(getConsumedEnergy(s)));
    }

    public static double getTotalConsumedEnergy() {
        return SensorHolder.getActiveSensors().values().stream().mapToDouble(SensorNetwork::getConsumedEnergy).sum();
    }

    public static boolean supplyCoverageOnline() {
        for (Sensor failedSensor : SensorHolder.getPreviousRoundFailedSensors().values()) {
            Sensor chosenReplacement = findReplacementOnline(failedSensor);
            if (chosenReplacement == null) {
                break;
            }
            chosenReplacement.setActive(true);
            if (!connectSensorOnline(chosenReplacement, failedSensor)) {
                GraphHolder.update();
            }
        }
        getEnvironment().updateCoverage();
        if (Double.compare(getEnvironment().getCurrentCoverage(), getEnvironment().getCoverageFactor()) >= 0) {
            return true;
        } else {
            Logging.getLogger().logln("Couldn't supply online coverage");
            return false;
        }
    }

    private static boolean connectSensorOnline(Sensor chosenReplacement, Sensor failedSensor) {
        boolean result = chosenReplacement.getNeighbors().containsKey(failedSensor.getParent().getID());
        if (result) {
            failedSensor.getParent().addChild(chosenReplacement);
            chosenReplacement.setConnected(failedSensor.getParent().isConnected()
                    || failedSensor.getParent() instanceof Sink);
        }

        for (Sensor failedChild : failedSensor.getChildren().values()) {
            if (!failedChild.isConnected()) {
                if (chosenReplacement.getNeighbors().containsKey(failedChild.getID())) {
                    chosenReplacement.addChild(failedChild);
                    failedChild.setConnected(true);
                } else {
                    result = false;
                }
            }
        }

        return result;
    }

    private static Sensor findReplacementOnline(Sensor failedSensor) {
        Sensor chosen = null;
        double minDistance = Double.MAX_VALUE;
        for (Sensor candidate : failedSensor.getNeighbors().values()) {
            if (!candidate.isActive() && !candidate.isFailed()) {
                double totalDistanceToChildren = candidate.getDistances().get(failedSensor.getParent().getID());
                for (Long childId : failedSensor.getChildren().keySet()) {
                    totalDistanceToChildren += candidate.getDistances().get(childId);
                }
                if (Double.compare(totalDistanceToChildren, minDistance) < 0) {
                    minDistance = totalDistanceToChildren;
                    chosen = candidate;
                }
            }
        }
        return chosen;
    }

    public static boolean supplyCoverage() {
        boolean result = true;
        Set<Long> blacklist = new HashSet<>();
        getEnvironment().update();
        while (result && isCoverageLow()) {
            Sensor chosenReplacement = findReplacement(blacklist);
            if (chosenReplacement != null) {
                chosenReplacement.setActive(true);
                SensorHolder.updateCollections();
                updateConnections();
                if (chosenReplacement.getParent() == null || chosenReplacement.getParent().isFailed()) {
                    chosenReplacement.setActive(false);
                    blacklist.add(chosenReplacement.getID());
                    continue;
                } else {
                    Logging.getLogger().logln("Chosen replacement = " + chosenReplacement.getID());
                }
                getEnvironment().updateCoverage();
                getEnvironment().updateDisconnectedCoverage();
            } else {
                Logging.getLogger().logln("There are no available sensors to supply the demanded coverage");
                result = false;
            }

        }
        getEnvironment().updateCoverage();
        getEnvironment().updateDisconnectedCoverage();
        return result;
    }

    private static boolean isCoverageLow() {
        return Double.compare(getEnvironment().getCurrentCoverage(), getEnvironment().getCoverageFactor()) < 0;
    }

    private static Sensor findReplacement(Set<Long> blacklist) {
        int maxCoveredPoints = Integer.MIN_VALUE;
        Sensor chosen = null;
        getEnvironment().updateExclusivelyCoveredPoints();
        for (Sensor sensor : SensorHolder.getInactiveSensors().values()) {
            if (!blacklist.contains(sensor.getID())) {
                Set<Position> coveredPoints = new HashSet<>(getEnvironment().getCoveredPoints());
                if (coveredPoints.addAll(sensor.getExclusivelyCoveredPoints())
                        && Integer.compare(maxCoveredPoints, coveredPoints.size()) < 0) {
                    maxCoveredPoints = coveredPoints.size();
                    chosen = sensor;
                }
            }
        }
        return chosen;
    }

    public static void createInitialNetwork(boolean[] vetBoolean) {
        updateActiveSensors(vetBoolean);
        updateConnections();
        getEnvironment().update();
        if (isCoverageLow()) {
            supplyCoverage();
        }
    }

}
