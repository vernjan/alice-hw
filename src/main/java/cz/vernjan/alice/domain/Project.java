package cz.vernjan.alice.domain;

import java.util.List;

public record Project(int totalDuration, int highestCrewAssignment, List<ProjectTask> tasks) {

    public List<ProjectTask> tasks() {
        return tasks != null ? tasks : List.of();
    }

}
