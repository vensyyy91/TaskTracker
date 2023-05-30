package service;

import model.EpicTask;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @BeforeEach
    public void beforeEach() {
        manager = new FileBackedTaskManager("test" + File.separator + "test.csv");
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

    @Test
    void saveAndLoad() {
        manager.getTaskById(1);
        manager.getEpicTaskById(2);
        manager.getSubTaskById(3);
        TaskManager newManager = FileBackedTaskManager.loadFromFile(new File("test" + File.separator + "test.csv"));

        assertIterableEquals(manager.getTaskList(), newManager.getTaskList(), "Списки задач не совпадают.");
        assertIterableEquals(manager.getEpicTaskList(), newManager.getEpicTaskList(), "Списки эпиков не совпадают.");
        assertIterableEquals(manager.getSubTaskList(), newManager.getSubTaskList(), "Списки подзадач не совпадают.");
        assertIterableEquals(manager.getHistory(), newManager.getHistory(), "Истории задач не совпадают.");
    }

    @Test
    void saveAndLoadWithEmptyTaskList() {
        manager.removeTasks();
        manager.removeEpicTasks();
        manager.removeSubTasks();
        TaskManager newManager = FileBackedTaskManager.loadFromFile(new File("test" + File.separator + "test.csv"));

        assertIterableEquals(manager.getTaskList(), newManager.getTaskList(), "Списки задач не совпадают.");
        assertIterableEquals(manager.getEpicTaskList(), newManager.getEpicTaskList(), "Списки эпиков не совпадают.");
        assertIterableEquals(manager.getSubTaskList(), newManager.getSubTaskList(), "Списки подзадач не совпадают.");
        assertIterableEquals(manager.getHistory(), newManager.getHistory(), "Истории задач не совпадают.");
    }

    @Test
    void saveAndLoadWithEmptyHistory() {
        TaskManager newManager = FileBackedTaskManager.loadFromFile(new File("test" + File.separator + "test.csv"));

        assertIterableEquals(manager.getTaskList(), newManager.getTaskList(), "Списки задач не совпадают.");
        assertIterableEquals(manager.getEpicTaskList(), newManager.getEpicTaskList(), "Списки эпиков не совпадают.");
        assertIterableEquals(manager.getSubTaskList(), newManager.getSubTaskList(), "Списки подзадач не совпадают.");
        assertIterableEquals(manager.getHistory(), newManager.getHistory(), "Истории задач не совпадают.");
    }

    @Test
    void saveAndLoadWithNoSubTasks() {
        manager.getTaskById(1);
        manager.getEpicTaskById(2);
        manager.getSubTaskById(3);
        manager.removeSubTasks();
        TaskManager newManager = FileBackedTaskManager.loadFromFile(new File("test" + File.separator + "test.csv"));

        assertIterableEquals(manager.getTaskList(), newManager.getTaskList(), "Списки задач не совпадают.");
        assertIterableEquals(manager.getEpicTaskList(), newManager.getEpicTaskList(), "Списки эпиков не совпадают.");
        assertIterableEquals(manager.getSubTaskList(), newManager.getSubTaskList(), "Списки подзадач не совпадают.");
        assertIterableEquals(manager.getHistory(), newManager.getHistory(), "Истории задач не совпадают.");
    }

    @Test
    void saveAndLoadWithNoFile() {
        TaskManager saveManager = new FileBackedTaskManager(".....");
        ManagerSaveException exSave = assertThrows(ManagerSaveException.class, () -> saveManager.createTask(task));

        assertEquals("Произошла ошибка во время записи файла.", exSave.getMessage());

        ManagerSaveException exLoad = assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(new File("file.csv")));

        assertEquals("Произошла ошибка во время загрузки файла.", exLoad.getMessage());
    }
}