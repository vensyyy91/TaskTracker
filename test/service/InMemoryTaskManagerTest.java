package service;

import model.EpicTask;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;

import java.time.Duration;
import java.time.LocalDateTime;

class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {

    @BeforeEach
    public void beforeEach() {
        manager = Managers.getDefault();
        task = new Task("TestTask", "Task for test");
        task.setStartTime(LocalDateTime.of(2023, 6, 25, 21, 0));
        task.setDuration(Duration.ofMinutes(30));
        manager.createTask(task);
        epicTask = new EpicTask("TestEpicTask", "EpicTask for test");
        manager.createEpicTask(epicTask);
        subTask = new SubTask("TestSubTask", "SubTask for test", epicTask);
        subTask.setStatus(TaskStatus.DONE);
        subTask.setStartTime(LocalDateTime.of(2023, 6, 25, 23, 0));
        subTask.setDuration(Duration.ofMinutes(30));
        manager.createSubTask(subTask);
    }
}