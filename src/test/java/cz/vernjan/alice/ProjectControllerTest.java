package cz.vernjan.alice;

import cz.vernjan.alice.domain.Interval;
import cz.vernjan.alice.domain.Project;
import cz.vernjan.alice.domain.ProjectTask;
import cz.vernjan.alice.domain.TaskRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class ProjectControllerTest {

    @MockBean
    private ProjectService projectService;

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TaskRecord taskRecord = TaskRecordFactory.newRootTaskRecord("testId", 10, 5);
        ProjectTask projectTask = new ProjectTask(taskRecord, new Interval(0, 0), new Interval(10, 10));
        Project project = new Project(10, 5, List.of(projectTask));

        when(projectService.loadProject(anyString())).thenReturn(project);
    }

    @Test
    public void getProject() throws Exception {
        String expectedResponseBody = """
                {"totalDuration": 10, "highestCrewAssignment": 5}""";

        mockMvc.perform(get("/project/foo"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponseBody));
    }

    @Test
    public void getProjectTasks() throws Exception {
        String expectedResponseBody = """
                [
                  {
                    "record": {
                      "taskCode": "testId",
                      "operationName": "operation",
                      "elementName": "element",
                      "duration": 10,
                      "crew": {
                        "name": "crew",
                        "assignment": 5
                      },
                      "dependencies": []
                    },
                    "startInterval": {
                      "from": 0,
                      "to": 0
                    },
                    "endInterval": {
                      "from": 10,
                      "to": 10
                    }
                  }
                ]""";

        mockMvc.perform(get("/project/foo/task"))
                .andExpect(status().isOk())
                .andExpect(content().json(expectedResponseBody));
    }

}