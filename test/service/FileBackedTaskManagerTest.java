package service;

import model.EpicTask;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.exception.ManagerSaveException;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {

    @BeforeEach
    public void beforeEach() {
        manager = new FileBackedTaskManager("test" + File.separator + "test.csv");
        task = new Task("TestTask", "Task for test", "25.06.2023 21:00", 30);
        manager.createTask(task);
        epicTask = new EpicTask("TestEpicTask", "EpicTask for test");
        manager.createEpicTask(epicTask);
        subTask = new SubTask("TestSubTask", "SubTask for test", "25.06.2023 23:00", 30, epicTask);
        subTask.setStatus(TaskStatus.DONE);
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