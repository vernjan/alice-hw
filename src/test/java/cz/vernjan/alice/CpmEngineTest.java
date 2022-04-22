package cz.vernjan.alice;

import cz.vernjan.alice.domain.Interval;
import cz.vernjan.alice.domain.Project;
import cz.vernjan.alice.domain.ProjectTask;
import cz.vernjan.alice.domain.TaskRecord;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static cz.vernjan.alice.TaskRecordFactory.newRootTaskRecord;
import static cz.vernjan.alice.TaskRecordFactory.newTaskRecord;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CpmEngineTest {

    private final CpmEngine fixture = new CpmEngine();

    private final List<TaskRecord> taskRecords = List.of(
            newRootTaskRecord("A1", 2, 1),
            newRootTaskRecord("A2", 3, 3),
            newTaskRecord("B", 4, 5, List.of("A1", "A2")),
            newTaskRecord("C", 2, 1, List.of("A2")),
            newTaskRecord("D", 5, 3, List.of("B")),
            newTaskRecord("E", 1, 2, List.of("C")),
            newTaskRecord("F", 2, 4, List.of("C")),
            newTaskRecord("G", 4, 4, List.of("D", "E")),
            newTaskRecord("H1", 3, 1, List.of("F", "G")),
            newTaskRecord("H2", 1, 20, List.of("F"))
    );

    @Test
    void evaluateEmptyList() {
        Project project = fixture.evaluate(List.of());

        assertEquals(0, project.totalDuration());
        assertEquals(0, project.highestCrewAssignment());
        assertEquals(List.of(), project.tasks());
    }

    @Test
    void evaluateTotalDuration() {
        Project project = fixture.evaluate(taskRecords);

        assertEquals(19, project.totalDuration());
    }

    @Test
    void evaluateHighestCrewAssignment() {
        Project project = fixture.evaluate(taskRecords);

        assertEquals(23, project.highestCrewAssignment());
    }

    @Test
    void evaluateTasks() {
        Project project = fixture.evaluate(taskRecords);

        assertEquals(taskRecords.size(), project.tasks().size());
    }

    @Test
    void evaluateTasks_startIntervals() {
        Project project = fixture.evaluate(taskRecords);

        Map<String, ProjectTask> taskMap = createTaskMap(project.tasks());

        assertEquals(new Interval(0, 1), taskMap.get("A1").startInterval());
        assertEquals(new Interval(0, 0), taskMap.get("A2").startInterval());
        assertEquals(new Interval(3, 3), taskMap.get("B").startInterval());
        assertEquals(new Interval(3, 9), taskMap.get("C").startInterval());
        assertEquals(new Interval(7, 7), taskMap.get("D").startInterval());
        assertEquals(new Interval(5, 11), taskMap.get("E").startInterval());
        assertEquals(new Interval(5, 14), taskMap.get("F").startInterval());
        assertEquals(new Interval(12, 12), taskMap.get("G").startInterval());
        assertEquals(new Interval(16, 16), taskMap.get("H1").startInterval());
        assertEquals(new Interval(7, 18), taskMap.get("H2").startInterval());
    }

    @Test
    void evaluateTasks_endIntervals() {
        Project project = fixture.evaluate(taskRecords);

        Map<String, ProjectTask> taskMap = createTaskMap(project.tasks());

        assertEquals(new Interval(2, 3), taskMap.get("A1").endInterval());
        assertEquals(new Interval(3, 3), taskMap.get("A2").endInterval());
        assertEquals(new Interval(7, 7), taskMap.get("B").endInterval());
        assertEquals(new Interval(5, 11), taskMap.get("C").endInterval());
        assertEquals(new Interval(12, 12), taskMap.get("D").endInterval());
        assertEquals(new Interval(6, 12), taskMap.get("E").endInterval());
        assertEquals(new Interval(7, 16), taskMap.get("F").endInterval());
        assertEquals(new Interval(16, 16), taskMap.get("G").endInterval());
        assertEquals(new Interval(19, 19), taskMap.get("H1").endInterval());
        assertEquals(new Interval(8, 19), taskMap.get("H2").endInterval());
    }

    private static Map<String, ProjectTask> createTaskMap(List<ProjectTask> tasks) {
        return tasks.stream().collect(toMap(task -> task.record().taskCode(), task -> task));
    }

}