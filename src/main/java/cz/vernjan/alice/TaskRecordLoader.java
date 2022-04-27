package cz.vernjan.alice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.vernjan.alice.domain.TaskRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Load task records from a file located in application resources.
 */
@Component
public class TaskRecordLoader {

    private static final Logger LOG = LoggerFactory.getLogger(TaskRecordLoader.class);

    private final ObjectMapper mapper = new ObjectMapper().
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<TaskRecord> loadFromResource(String resourceName) {
        try {
            InputStream resourceInputStream = new ClassPathResource(resourceName).getInputStream();
            List<TaskRecord> taskRecords = List.of(mapper.reader().readValue(resourceInputStream, TaskRecord[].class));
            LOG.info("Loaded {} task records from {}", taskRecords.size(), resourceName);
            return taskRecords;
        } catch (IOException e) {
            LOG.error("Failed to load task records from {}", resourceName, e);
            throw new UncheckedIOException(e);
        }
    }

}
