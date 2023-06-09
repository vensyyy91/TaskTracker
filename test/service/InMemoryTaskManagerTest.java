package service;

import model.EpicTask;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;

class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {

    @BeforeEach
    public void beforeEach() {
        manager = new InMemoryTaskManager();
        task = new Task("TestTask", "Task for test", "25.06.2023 21:00", 30);
        manager.createTask(task);
        epicTask = new EpicTask("TestEpicTask", "EpicTask for test");
        manager.createEpicTask(epicTask);
        subTask = new SubTask("TestSubTask", "SubTask for test", "25.06.2023 23:00", 30, epicTask);
        subTask.setStatus(TaskStatus.DONE);
        manager.createSubTask(subTask);
    }
}