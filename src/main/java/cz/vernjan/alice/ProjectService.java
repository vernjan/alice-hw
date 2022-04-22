package cz.vernjan.alice;

import cz.vernjan.alice.domain.Project;
import cz.vernjan.alice.domain.TaskRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProjectService {

    private static final Logger LOG = LoggerFactory.getLogger(ProjectService.class);

    private final TaskRecordLoader recordLoader;
    private final CpmEngine cpmEngine;

    @Autowired
    public ProjectService(TaskRecordLoader recordLoader, CpmEngine cpmEngine) {
        this.recordLoader = recordLoader;
        this.cpmEngine = cpmEngine;
    }

    public Project loadProject(String id) {
        List<TaskRecord> taskRecords = recordLoader.loadFromResource("LEO2-BE.json");
        Project project = cpmEngine.evaluate(taskRecords);
        LOG.info("Project '{}' successfully loaded (total tasks: {})", id, project.tasks().size());
        return project;
    }

}
