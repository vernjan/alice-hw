package cz.vernjan.alice;

import java.io.UncheckedIOException;
import java.util.List;

import com.fasterxml.jackson.databind.JsonMappingException;
import cz.vernjan.alice.domain.TaskRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskRecordLoaderTest {

    private final TaskRecordLoader fixture = new TaskRecordLoader();

    @Test
    void loadTaskRecordsFromResource() {
        TaskRecord expectedFirstTask = new TaskRecord(
                "A610360338",
                "Close Forms",
                "B1_A_L5_Core Wall",
                16,
                new TaskRecord.Crew("C_Carpenter Crew", 1),
                List.of("A163819636"));

        List<TaskRecord> tasks = fixture.loadFromResource("LEO2-BE-test.json");

        assertEquals(2, tasks.size());
        assertEquals(expectedFirstTask, tasks.get(0));
    }

    @Test
    void loadTaskRecordsFromResource_throwUncheckedIOException_whenResourceNotFound() {
        assertThrows(UncheckedIOException.class, () -> fixture.loadFromResource("NO_SUCH_FILE.json"));
    }

    @Test
    void loadTaskRecordsFromResource_throwUncheckedIOException_whenResourceMalformed() {
        assertThrows(UncheckedIOException.class, () -> fixture.loadFromResource("malformed.json"));
    }

}