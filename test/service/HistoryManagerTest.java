package service;

import model.EpicTask;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HistoryManagerTest {

    static TaskManager manager;
    static Task task1;
    static Task task2;
    static EpicTask epicTask;
    static SubTask subTask1;
    static SubTask subTask2;
    HistoryManager historyManager;

    @BeforeAll
    public static void beforeAll() {
        manager = new InMemoryTaskManager();
        task1 = new Task("TestTask1", "First task for test");
        manager.createTask(task1); // id:1
        task2 = new Task("TestTask2", "Second task for test");
        manager.createTask(task2); // id:2
        epicTask = new EpicTask("TestEpicTask", "EpicTask for test");
        manager.createEpicTask(epicTask); // id:3
        subTask1 = new SubTask("SubTask1", "First subtask for test", 3);
        manager.createSubTask(subTask1); // id:4
        subTask2 = new SubTask("SubTask2", "Second subtask for test", 3);
        manager.createSubTask(subTask2); // id:5
    }

    @BeforeEach
    public void beforeEach() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    void addTask() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не возвращается.");
        assertEquals(history.size(), 1, "Размер истории не совпадает.");
        assertTrue(history.contains(task1), "Задача не добавляется.");
    }

    @Test
    void addTaskThatIsAlreadyInHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epicTask);
        historyManager.add(subTask1);
        historyManager.add(subTask2);
        historyManager.add(task1);
        historyManager.add(subTask1);
        List<Task> history = historyManager.getHistory();

        assertIterableEquals(Arrays.asList(task2, epicTask, subTask2, task1, subTask1), history,
                "История просмотров не совпадает.");
    }

    @Test
    void addNull() {
        assertThrows(NullPointerException.class, () -> historyManager.add(null));
    }

    @Test
    void removeFirstElement() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epicTask);
        historyManager.add(subTask1);
        historyManager.add(subTask2);
        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не возвращается.");
        assertEquals(history.size(), 4, "Размер истории не совпадает.");
        assertFalse(history.contains(task1), "Задача не удаляется.");
    }

    @Test
    void removeMiddleElement() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epicTask);
        historyManager.add(subTask1);
        historyManager.add(subTask2);
        historyManager.remove(3);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не возвращается.");
        assertEquals(history.size(), 4, "Размер истории не совпадает.");
        assertFalse(history.contains(epicTask), "Задача не удаляется.");
    }

    @Test
    void removeLastElement() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epicTask);
        historyManager.add(subTask1);
        historyManager.add(subTask2);
        historyManager.remove(5);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не возвращается.");
        assertEquals(history.size(), 4, "Размер истории не совпадает.");
        assertFalse(history.contains(subTask2), "Задача не удаляется.");
    }

    @Test
    void removeWithNonExistingId() {
        historyManager.add(task1);
        historyManager.remove(6);
        List<Task> history = historyManager.getHistory();

        assertTrue(history.contains(task1), "Задача удаляется из списка просмотров.");
        assertEquals(1, history.size(), "Меняется размер списка просмотров.");
    }

    @Test
    void getHistoryWhenEmpty() {
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не возвращается.");
        assertTrue(history.isEmpty(), "История не пустая.");
    }

    @Test
    void getHistoryWhenNotEmpty() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertNotNull(history, "История не возвращается.");
        assertFalse(history.isEmpty(), "История пустая.");
    }
}