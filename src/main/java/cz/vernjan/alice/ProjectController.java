package cz.vernjan.alice;

import cz.vernjan.alice.domain.Project;
import cz.vernjan.alice.domain.ProjectTask;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping("/{id}")
    public ProjectView getProject(@PathVariable String id) {
        Project project = projectService.loadProject(id);
        return new ProjectView(project.totalDuration(), project.highestCrewAssignment());
    }

    @GetMapping("/{id}/task")
    public List<ProjectTask> getProjectTasks(@PathVariable String id) {
        Project project = projectService.loadProject(id);
        return project.tasks();
    }

    record ProjectView(int totalDuration, int highestCrewAssignment) {

    }

}
