package service;

import model.EpicTask;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTest extends TaskManagerTest<TaskManager> {
    private KVServer server;

    @BeforeEach
    public void beforeEach() throws IOException {
        server = new KVServer();
        server.start();
        manager = Managers.getDefault();
        task = new Task("TestTask", "Task for test", "25.06.2023 21:00", 30);
        manager.createTask(task);
        epicTask = new EpicTask("TestEpicTask", "EpicTask for test");
        manager.createEpicTask(epicTask);
        subTask = new SubTask("TestSubTask", "SubTask for test", "25.06.2023 23:00", 30, 2);
        subTask.setStatus(TaskStatus.DONE);
        manager.createSubTask(subTask);
    }

    @AfterEach
    public void afterEach() {
        server.stop();
    }

    @Test
    public void saveAndLoad() {
        manager.getEpicTaskById(2);
        manager.getTaskById(1);
        manager.getSubTaskById(3);
        HttpTaskManager newManager = new HttpTaskManager("http://localhost:8078/", true);

        assertEquals(manager.getTaskList(), newManager.getTaskList(),
                "Списки задач не совпадают.");
        assertEquals(manager.getEpicTaskList(), newManager.getEpicTaskList(),
                "Списки эпиков не совпадают.");
        assertEquals(manager.getSubTaskList(), newManager.getSubTaskList(),
                "Списки подзадач не совпадают.");
        assertEquals(manager.getHistory(), newManager.getHistory(),
                "Списки истории просмотров не совпадают.");
        assertEquals(manager.getPrioritizedTasks(), newManager.getPrioritizedTasks(),
                "Списки отсортированных задач не совпадают.");
        assertEquals(manager.getId(), newManager.getId(),
                "Идентификаторы не совпадают.");
    }
}
