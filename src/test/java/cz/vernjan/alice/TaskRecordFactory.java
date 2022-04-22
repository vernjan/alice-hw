package cz.vernjan.alice;

import cz.vernjan.alice.domain.TaskRecord;

import java.util.List;

public class TaskRecordFactory {

    static TaskRecord newRootTaskRecord(String id, int duration, int crewMembers) {
        return newTaskRecord(id, duration, crewMembers, List.of());
    }

    static TaskRecord newTaskRecord(String id, int duration, int crewMembers, List<String> dependencies) {
        return new TaskRecord(
                id,
                "operation",
                "element",
                duration,
                new TaskRecord.Crew("crew", crewMembers),
                dependencies);
    }

}
