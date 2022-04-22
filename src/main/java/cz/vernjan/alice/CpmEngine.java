package cz.vernjan.alice;

import cz.vernjan.alice.domain.Interval;
import cz.vernjan.alice.domain.Project;
import cz.vernjan.alice.domain.ProjectTask;
import cz.vernjan.alice.domain.TaskRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.*;

/**
 * Represents Critical path method algorithm.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Critical_path_method">Critical path method (wiki)</a>
 */
@Component
public class CpmEngine {

    /**
     * Compute project summary and start and intervals for all project tasks.
     *
     * @param taskRecords task records
     * @return evaluated project
     */
    public Project evaluate(List<TaskRecord> taskRecords) {
        List<TaskNode> taskNodes = initializeTaskHierarchy(taskRecords);

        CpmProject cpmProject = new CpmProject(taskNodes);

        return new Project(
                cpmProject.getTotalDuration(),
                cpmProject.calculateHighestCrewAssignment(),
                cpmProject.getTasks());
    }

    private List<TaskNode> initializeTaskHierarchy(List<TaskRecord> taskRecords) {
        Map<String, TaskNode> taskNodeMap = taskRecords.stream()
                .map(TaskNode::new)
                .collect(toMap(TaskNode::taskCode, node -> node, (e1, e2) -> e1, LinkedHashMap::new));

        taskNodeMap.values().forEach(task ->
                task.record.dependencies().forEach(taskDependency ->
                        taskNodeMap.get(taskDependency).addChild(task)));

        return List.copyOf(taskNodeMap.values());
    }

    private static class CpmProject {

        private static final Logger LOG = LoggerFactory.getLogger(CpmProject.class);

        private final List<TaskNode> taskNodes;

        private CpmProject(List<TaskNode> taskNodes) {
            this.taskNodes = taskNodes;
            evaluateTaskIntervals();
        }

        private void evaluateTaskIntervals() {
            Set<TaskNode> leafNodes = taskLeafNodes();
            LOG.debug("Found leaf tasks: {}", leafNodes);
            leafNodes.forEach(this::calculateEarlyInterval);

            Set<TaskNode> rootNodes = taskRootNodes();
            LOG.debug("Found root tasks: {}", rootNodes);
            rootNodes.forEach(root -> calculateLateInterval(root, getTotalDuration()));
        }

        private Set<TaskNode> taskRootNodes() {
            return taskNodes.stream()
                    .filter(task -> task.parents().isEmpty())
                    .collect(toUnmodifiableSet());
        }

        private Set<TaskNode> taskLeafNodes() {
            return taskNodes.stream()
                    .filter(task -> task.children().isEmpty())
                    .collect(toUnmodifiableSet());
        }

        private TaskNode calculateEarlyInterval(TaskNode node) {
            if (node.earlyFinish != null) {
                return node;
            }

            int earlyStart = node.parents().stream()
                    .mapToInt(parent -> calculateEarlyInterval(parent).earlyFinish)
                    .max()
                    .orElse(0);

            node.earlyStart = earlyStart;
            node.earlyFinish = earlyStart + node.duration();

            LOG.debug("Calculated early interval for {}: {}-{}", node, node.earlyStart, node.earlyFinish);

            return node;
        }

        private TaskNode calculateLateInterval(TaskNode node, int totalDuration) {
            if (node.lateFinish != null) {
                return node;
            }

            int lateFinish = node.children().stream()
                    .mapToInt(child -> calculateLateInterval(child, totalDuration).lateStart)
                    .min()
                    .orElse(totalDuration);

            node.lateStart = lateFinish - node.duration();
            node.lateFinish = lateFinish;

            LOG.debug("Calculated late interval for {}: {}-{}", node, node.lateStart, node.lateFinish);

            return node;
        }

        /**
         * @return the shortest possible duration of all tasks (i.e. the greatest early finish)
         */
        private int getTotalDuration() {
            return taskLeafNodes().stream()
                    .mapToInt(TaskNode::earlyFinish)
                    .max()
                    .orElse(0);
        }

        /**
         * @return tasks with start interval and end interval
         */
        private List<ProjectTask> getTasks() {
            return taskNodes.stream()
                    .map(this::convertToProjectTask)
                    .toList();
        }

        private ProjectTask convertToProjectTask(TaskNode node) {
            return new ProjectTask(
                    node.record,
                    new Interval(node.earlyStart, node.lateStart),
                    new Interval(node.earlyFinish, node.lateFinish));
        }

        /**
         * @return highest crew assignment at any given time (based on the early interval)
         */
        private int calculateHighestCrewAssignment() {
            List<TaskNode> tasksSortedByEarlyStart = taskNodes.stream()
                    .sorted(Comparator.comparingInt(TaskNode::earlyStart))
                    .toList();

            Set<TaskNode> concurrentTasks = new TreeSet<>(
                    Comparator.comparingInt(TaskNode::earlyFinish).thenComparing(TaskNode::taskCode));

            int highestCrewAssignment = 0;

            for (TaskNode task : tasksSortedByEarlyStart) {
                removeTasksFinishedBefore(concurrentTasks, task.earlyStart);
                concurrentTasks.add(task);

                int currentCrewAssignment = concurrentTasks.stream()
                        .mapToInt(concurrentTask -> concurrentTask.record.crew().assignment())
                        .sum();

                highestCrewAssignment = Math.max(highestCrewAssignment, currentCrewAssignment);
            }

            return highestCrewAssignment;
        }

        private void removeTasksFinishedBefore(Set<TaskNode> concurrentTasks, int before) {
            Iterator<TaskNode> iterator = concurrentTasks.iterator();
            while (iterator.hasNext() && iterator.next().earlyFinish <= before) {
                iterator.remove();
            }
        }
    }

    private static class TaskNode {

        private final TaskRecord record;
        private final Set<TaskNode> children;
        private final Set<TaskNode> parents;

        private Integer earlyStart;
        private Integer earlyFinish;
        private Integer lateStart;
        private Integer lateFinish;

        TaskNode(TaskRecord record) {
            this.record = record;
            this.children = new HashSet<>();
            this.parents = new HashSet<>();
        }

        String taskCode() {
            return record.taskCode();
        }

        int duration() {
            return record.duration();
        }

        Set<TaskNode> children() {
            return children;
        }

        void addChild(TaskNode child) {
            child.parents.add(this);
            children.add(child);
        }

        Set<TaskNode> parents() {
            return parents;
        }

        Integer earlyStart() {
            return earlyStart;
        }

        Integer earlyFinish() {
            return earlyFinish;
        }

        @Override
        public String toString() {
            return String.format("%s (parents: %d, children: %d)", taskCode(), parents.size(), children.size());
        }
    }

}
