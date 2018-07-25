package projects.tcc.simulation.rssf;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import projects.tcc.simulation.graph.GraphHolder;
import projects.tcc.simulation.rssf.sensor.Sensor;
import projects.tcc.simulation.rssf.sensor.Sink;
import sinalgo.nodes.Position;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Log
public class SensorNetwork {

    @Getter
    @Setter(AccessLevel.PRIVATE)
    private static double availableEnergy;

    public static void init() {
        computeDistances();
        computeNeighbors();
    }

    private static void computeDistances() {
        Collection<Sensor> allSensorsAndSinks = SensorCollection.getAllSensorsAndSinks().values();
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
        SensorCollection.getAvailableSensors().values().forEach(s -> s.getDistances().forEach((k, v) -> {
            if (Double.compare(v, s.getCommRadius()) < 0) {
                s.getNeighbors().put(k, SensorCollection.getAllSensorsAndSinks().get(k));
            }
        }));
    }

    public static void updateAvailableEnergy() {
        setAvailableEnergy(SensorCollection.getAvailableSensors().values().stream()
                .mapToDouble(Sensor::getBatteryEnergy)
                .sum());
    }

    public static void updateActiveSensors(boolean[] booleanArray) {
        SensorCollection.getAvailableSensors().values().forEach(s -> {
            if (booleanArray[(int) s.getID() - 1]) {
                s.activate();
            } else {
                s.deactivate();
            }
        });
    }

    public static void updateActiveSensors(int[] intArray) {
        SensorCollection.getAvailableSensors().values().forEach(s -> {
            if (intArray[(int) s.getID() - 1] == 1) {
                s.activate();
            } else {
                s.deactivate();
            }
        });
    }

    /**
     * This should be executed AFTER the AG has decided which Sensors should be activated/connected.
     * It needs to use the GraphNodeProperties of each Sensor in order to determine which ones will
     * be used in this method.
     */
    public static void updateConnections() {
        SensorCollection.getAvailableSensors().values().forEach(Sensor::resetConnectivity);
        SensorCollection.getSinks().values().forEach(Sensor::resetConnectivity);
        SensorCollection.getActiveSensors().values().stream()
                .filter(s -> s.getGraphNodeProperties().getParentId() != null)
                .collect(Collectors.toList()).forEach(s -> {
            SensorCollection.getAllSensorsAndSinks().get(s.getGraphNodeProperties().getParentId()).addChild(s);
            SensorNetwork.activateNeededParents(s);
        });
        SensorCollection.getSinks().values().forEach(Sensor::connectAndPropagate);
        SensorCollection.getSinks().values().forEach(Sensor::queryDescendants);
        List<Sensor> sensorsToDeactivate = SensorCollection.getActiveSensors().values().stream()
                .filter(s -> !(s.isActive() && s.isConnected()))
                .collect(Collectors.toList());
        sensorsToDeactivate.forEach(Sensor::deactivate);
    }

    private static void activateNeededParents(Sensor s) {
        Sensor parent = s.getParent();
        while (parent != null && !(parent instanceof Sink) && !parent.isActive()) {
            parent.activate();
            parent = parent.getParent();
        }
    }

    public static double getActivationEnergyForThisRound() {
        return SensorCollection.getCurrentRoundActivatedSensors().values().stream()
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
        SensorCollection.getActiveSensors().values().forEach(s -> s.subtractEnergySpent(getConsumedEnergy(s)));
    }

    public static double getTotalConsumedEnergy() {
        return SensorCollection.getActiveSensors().values().stream().mapToDouble(SensorNetwork::getConsumedEnergy).sum();
    }

    public static boolean supplyCoverageOnline() {
        for (Sensor failedSensor : SensorCollection.getCurrentRoundFailedSensors().values()) {
            Sensor chosenReplacement = findReplacementOnline(failedSensor);
            if (chosenReplacement == null) {
                break;
            }
            chosenReplacement.activate();
            if (!connectSensorOnline(chosenReplacement, failedSensor)) {
                GraphHolder.update();
                updateConnections();
            }
        }
        if (Double.compare(Environment.getCurrentCoverage(), Environment.getCoverageFactor()) >= 0) {
            return true;
        } else {
            log.warning("Couldn't supply online coverage");
            return false;
        }
    }

    private static boolean connectSensorOnline(Sensor chosenReplacement, Sensor failedSensor) {
        boolean result = chosenReplacement.getNeighbors().containsKey(failedSensor.getParent().getID());
        if (result) {
            failedSensor.getParent().addChild(chosenReplacement);
            if (failedSensor.getParent().isConnected() || failedSensor.getParent() instanceof Sink) {
                chosenReplacement.connect();
            }
        }

        for (Sensor failedChild : failedSensor.getChildren().values()) {
            if (!failedChild.isConnected()) {
                if (chosenReplacement.getNeighbors().containsKey(failedChild.getID())) {
                    chosenReplacement.addChild(failedChild);
                    failedChild.connect();
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

    public static void supplyCoverage() {
        boolean result = true;
        Set<Long> blacklist = new HashSet<>();
        while (result && isCoverageLow()) {
            Sensor chosenReplacement = findReplacement(blacklist);
            if (chosenReplacement != null) {
                chosenReplacement.connect();
                updateConnections();
                if ((chosenReplacement.getGraphNodeProperties().getParentId() == null
                        && chosenReplacement.getParent() == null) || chosenReplacement.getParent().isFailed()) {
                    log.warning("Skipping possible replacement because of connection issues");
                    chosenReplacement.deactivate();
                    blacklist.add(chosenReplacement.getID());
                } else {
                    log.info("Chosen replacement = " + chosenReplacement.getID());
                }
            } else {
                log.warning("There are no available sensors to supply the demanded coverage");
                result = false;
            }

        }
    }

    private static boolean isCoverageLow() {
        log.warning("Coverage is " + Environment.getCurrentCoverage());
        return Double.compare(Environment.getCurrentCoverage(), Environment.getCoverageFactor()) < 0;
    }

    private static Sensor findReplacement(Set<Long> blacklist) {
        int maxCoveredPoints = Integer.MIN_VALUE;
        Sensor chosen = null;
        Environment.updateExclusivelyCoveredPoints();
        for (Sensor sensor : SensorCollection.getInactiveSensors().values()) {
            if (!blacklist.contains(sensor.getID())) {
                Set<Position> coveredPoints = new HashSet<>(Environment.getConnectedCoveredPoints());
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
        Environment.updateExclusivelyCoveredPoints();
        if (isCoverageLow()) {
            supplyCoverage();
            updateConnections();
            Environment.updateExclusivelyCoveredPoints();
        }
    }

}
