package br.com.fiap.incidentmanagement.model;

import java.util.Arrays;
import java.util.List;

public enum IncidentStatus {
    WAITING("Aguardando atendimento"),
    PROGRESSING("Em atendimento"),
    SOLVED("Resolvida"),
    UNSOLVED("Encerrada sem solução"),
    CLOSED("Fechada");

    /** Ocorrências "ativas" são as que ainda estão em aberto. */
    public static final List<IncidentStatus> OPEN = List.of(WAITING, PROGRESSING);

    private final String description;

    IncidentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public static IncidentStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(s -> s.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Status inválido: " + value));
    }
}
