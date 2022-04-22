package cz.vernjan.alice.domain;

import java.util.List;

public record TaskRecord(
        String taskCode,
        String operationName,
        String elementName,
        int duration,
        Crew crew,
        List<String> dependencies) {

    public Crew crew() {
        return crew != null ? crew : Crew.EMPTY;
    }

    public List<String> dependencies() {
        return dependencies != null ? dependencies : List.of();
    }

    public record Crew(String name, int assignment) {
        private static final Crew EMPTY = new Crew("", 0);
    }

}
