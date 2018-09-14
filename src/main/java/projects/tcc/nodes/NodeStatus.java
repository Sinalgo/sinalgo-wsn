package projects.tcc.nodes;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.awt.*;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NodeStatus {

    INACTIVE(Color.BLACK, "Inativo"),
    SLEEP(Color.GRAY, "Modo de espera"),
    TURNING_OFF(Color.BLUE, "Eleito (desativação)"),
    TURNING_ON(Color.GREEN.darker(), "Eleito (ativação)"),
    ACTIVE(Color.GREEN, "Ativo"),
    FAILED_SINK_UNAWARE(Color.MAGENTA, "Falho (não-detectado)"),
    MISIDENTIFIED_FAILURE(Color.ORANGE, "Falho (detecção errônea)"),
    FAILED(Color.RED, "Falho");

    private Color color;
    private String description;
}
