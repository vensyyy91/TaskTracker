package service;

import model.EpicTask;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

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
        subTask = new SubTask("TestSubTask", "SubTask for test", "25.06.2023 23:00", 30, epicTask);
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
        HistoryManager historyManager = manager.getHistoryManager();
        manager.getTasksMap().keySet().forEach(historyManager::remove);
        manager.getEpicTasksMap().keySet().forEach(historyManager::remove);
        manager.getSubTasksMap().keySet().forEach(historyManager::remove);
        manager.getTasksMap().clear();
        manager.getEpicTasksMap().clear();
        manager.getSubTasksMap().clear();

        TaskManager newManager = Managers.getDefault();

        assertEquals(Collections.singletonList(task), newManager.getTaskList(),
                "Списки задач не совпадают.");
        assertEquals(Collections.singletonList(epicTask), newManager.getEpicTaskList(),
                "Списки эпиков не совпадают.");
        assertEquals(Collections.singletonList(subTask), newManager.getSubTaskList(),
                "Списки подзадач не совпадают.");
        assertEquals(Arrays.asList(epicTask, task, subTask), newManager.getHistory(),
                "Списки истории просмотров не совпадают.");
    }
}
