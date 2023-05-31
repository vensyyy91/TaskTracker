package service;

import model.EpicTask;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;
    protected Task task;
    protected EpicTask epicTask;
    protected SubTask subTask;

    @Test
    void getTaskListWithTasks() {
        List<Task> taskList = manager.getTaskList();

        assertNotNull(taskList, "Список задач не возвращается.");
        assertIterableEquals(Collections.singletonList(task), taskList, "Списки задач не совпадают.");
    }

    @Test
    void getTaskListWhenEmpty() {
        manager.removeTasks();
        List<Task> taskList = manager.getTaskList();

        assertNotNull(taskList, "Список задач не возвращается.");
        assertTrue(taskList.isEmpty(), "Список задач не пуст.");
    }

    @Test
    void getEpicTaskListWithTasks() {
        List<EpicTask> epicTaskList = manager.getEpicTaskList();

        assertNotNull(epicTaskList, "Список эпиков не возвращается.");
        assertIterableEquals(Collections.singletonList(epicTask), epicTaskList, "Списки эпиков не совпадают.");
    }

    @Test
    void getEpicTaskListWhenEmpty() {
        manager.removeEpicTasks();
        List<EpicTask> epicTaskList = manager.getEpicTaskList();

        assertNotNull(epicTaskList, "Список эпиков не возвращается.");
        assertTrue(epicTaskList.isEmpty(), "Список эпиков не пуст.");
    }

    @Test
    void getSubTaskListWithTasks() {
        List<SubTask> subTaskList = manager.getSubTaskList();

        assertNotNull(subTaskList, "Список подзадач не возвращается.");
        assertIterableEquals(Collections.singletonList(subTask), subTaskList, "Списки подзадач не совпадают.");
    }

    @Test
    void getSubTaskListWhenEmpty() {
        manager.removeSubTasks();
        List<SubTask> subTaskList = manager.getSubTaskList();

        assertNotNull(subTaskList, "Список подзадач не возвращается.");
        assertTrue(subTaskList.isEmpty(), "Список подзадач не пуст.");
    }

    @Test
    void removeTasks() {
        manager.removeTasks();

        assertTrue(manager.getTaskList().isEmpty(), "Список задач не очищен.");
    }

    @Test
    void removeEpicTasks() {
        manager.removeEpicTasks();

        assertTrue(manager.getEpicTaskList().isEmpty(), "Список эпиков не очищен.");
        assertTrue(manager.getSubTaskList().isEmpty(), "Список подзадач не очищен.");
    }

    @Test
    void removeSubTasks() {
        manager.removeSubTasks();

        assertTrue(manager.getSubTaskList().isEmpty(), "Список подзадач не очищен.");
        assertTrue(epicTask.getSubTasksIdList().isEmpty(), "Список идентификаторов подзадач эпика не очищен.");
        assertEquals(TaskStatus.NEW, manager.getEpicTaskById(2).getStatus(), "Некорректный статус эпика.");
        assertEquals(LocalDateTime.of(2099, 12, 31, 0, 0), manager.getEpicTaskById(2).getStartTime(),
                "Не меняется время старта эпика.");
        assertEquals(Duration.ofMinutes(0), manager.getEpicTaskById(2).getDuration(),
                "Не меняется продолжительность эпика.");
    }

    @Test
    void getTaskByIdWithExistingId() {
        Task taskById = manager.getTaskById(1);

        assertNotNull(taskById, "Задача не возвращается");
        assertEquals(task, taskById, "Задачи не совпадают.");
    }

    @Test
    void getTaskByIdWithAnotherTypeId() {
        assertThrows(IllegalArgumentException.class, () -> manager.getTaskById(2));
    }
    
    @Test
    void getTaskByIdWithNonExistingId() {
        assertThrows(IllegalArgumentException.class, () -> manager.getTaskById(5));
    }

    @Test
    void getEpicTaskByIdWithExistingId() {
        EpicTask epicTaskById = manager.getEpicTaskById(2);

        assertNotNull(epicTaskById, "Задача не возвращается");
        assertEquals(epicTask, epicTaskById, "Задачи не совпадают.");
    }

    @Test
    void getEpicTaskByIdWithAnotherTypeId() {
        assertThrows(IllegalArgumentException.class, () -> manager.getEpicTaskById(1));
    }

    @Test
    void getEpicTaskByIdWithNonExistingId() {
        assertThrows(IllegalArgumentException.class, () -> manager.getEpicTaskById(5));
    }

    @Test
    void getSubTaskByIdWithExistingId() {
        SubTask subTaskById = manager.getSubTaskById(3);

        assertNotNull(subTaskById, "Задача не возвращается");
        assertEquals(subTask, subTaskById, "Задачи не совпадают.");
    }

    @Test
    void getSubTaskByIdWithAnotherTypeId() {
        assertThrows(IllegalArgumentException.class, () -> manager.getSubTaskById(1));
    }

    @Test
    void getSubTaskByIdWithNonExistingId() {
        assertThrows(IllegalArgumentException.class, () -> manager.getSubTaskById(5));
    }

    @Test
    void createTask() {
        Task newTask = new Task("NewTestTask", "New task for test");
        manager.createTask(newTask);

        assertEquals(4, newTask.getId(), "Задаче присваивается неверный id.");
        assertEquals(newTask, manager.getTaskById(4), "Задача не создается.");
        assertIterableEquals(Arrays.asList(task, newTask), manager.getTaskList(), "Списки задач не совпадают.");
    }

    @Test
    void createTaskWithTimeIntersection() {
        Task newTask = new Task("NewTestTask", "New task for test",
                "25.06.2023 20:45", 30);

        TimeValidationException ex = assertThrows(TimeValidationException.class, () -> manager.createTask(newTask));
        assertEquals("Задача 'NewTestTask' пересекается по времени с другими задачами.", ex.getMessage());
    }

    @Test
    void createEpicTask() {
        EpicTask newEpicTask = new EpicTask("NewTestEpicTask", "New EpicTask for test");
        manager.createEpicTask(newEpicTask);

        assertEquals(4, newEpicTask.getId(), "Эпику присваивается неверный id.");
        assertEquals(newEpicTask, manager.getEpicTaskById(4), "Эпик не создается.");
        assertIterableEquals(Arrays.asList(epicTask, newEpicTask), manager.getEpicTaskList(), "Списки эпиков не совпадают.");
    }

    @Test
    void createSubTask() {
        SubTask newSubTask = new SubTask("NewTestSubTask", "New SubTask for test",
                "25.06.2023 22:00", 60, epicTask);
        manager.createSubTask(newSubTask);

        assertEquals(4, newSubTask.getId(), "Подзадаче присваивается неверный id.");
        assertEquals(newSubTask, manager.getSubTaskById(4), "Подзадача не создается.");
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicTaskById(2).getStatus(),
                "Не меняется статус эпика.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 22, 0), manager.getEpicTaskById(2).getStartTime(),
                "Не меняется время старта эпика.");
        assertEquals(Duration.ofMinutes(90), manager.getEpicTaskById(2).getDuration(),
                "Не меняется длительность эпика.");
        assertIterableEquals(Arrays.asList(subTask, newSubTask), manager.getSubTaskList(),
                "Списки подзадач не совпадают.");
    }

    @Test
    void createSubTaskWithTimeIntersection() {
        SubTask newSubTask = new SubTask("NewTestSubTask", "New SubTask for test",
                "25.06.2023 21:15", 30, epicTask);

        TimeValidationException ex = assertThrows(TimeValidationException.class, () -> manager.createTask(newSubTask));
        assertEquals("Задача 'NewTestSubTask' пересекается по времени с другими задачами.", ex.getMessage());
    }

    @Test
    void updateTask() {
        Task updatedTask = new Task("UpdatedTask", "New updated Task for test",
                "25.06.2023 21:30", 15);
        updatedTask.setId(1);
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(updatedTask);

        assertEquals(updatedTask, manager.getTaskById(1), "Задача не обновляется.");
        assertEquals(TaskStatus.IN_PROGRESS, manager.getTaskById(1).getStatus(), "Статус задачи не обновляется.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 21, 30), manager.getTaskById(1).getStartTime(),
                "Не меняется время старта задачи.");
        assertEquals(Duration.ofMinutes(15), manager.getTaskById(1).getDuration(),
                "Не меняется длительность задачи.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 21, 45), manager.getTaskById(1).getEndTime(),
                "Не меняется время окончания задачи.");
    }

    @Test
    void updateTaskWithTimeIntersection() {
        Task updatedTask = new Task("UpdatedTask", "New updated Task for test",
                "25.06.2023 22:30", 45);
        updatedTask.setId(1);
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);

        TimeValidationException ex = assertThrows(TimeValidationException.class, () -> manager.updateTask(updatedTask));
        assertEquals("Задача 'UpdatedTask' пересекается по времени с другими задачами.", ex.getMessage());
    }

    @Test
    void updateEpicTask() {
        EpicTask updatedEpicTask = new EpicTask("UpdatedEpicTask", "New updated EpicTask for test");
        updatedEpicTask.setId(2);
        manager.updateEpicTask(updatedEpicTask);

        assertEquals(updatedEpicTask, manager.getEpicTaskById(2), "Эпик не обновляется.");
        assertEquals("New updated EpicTask for test", manager.getEpicTaskById(2).getDescription(),
                "Описание эпика не обновляется.");
    }

    @Test
    void updateSubTask() {
        SubTask updatedSubTask = new SubTask("UpdatedSubTask", "New updated SubTask for test",
                "25.06.2023 23:30", 15, epicTask);
        updatedSubTask.setId(3);
        updatedSubTask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubTask(updatedSubTask);

        assertEquals(updatedSubTask, manager.getSubTaskById(3), "Подзадача не обновляется.");
        assertEquals(TaskStatus.IN_PROGRESS, manager.getSubTaskById(3).getStatus(), "Статус подзадачи не обновляется.");
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicTaskById(2).getStatus(), "Статус эпика не обновляется.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 23, 30), manager.getSubTaskById(3).getStartTime(),
                "Не меняется время старта задачи.");
        assertEquals(Duration.ofMinutes(15), manager.getSubTaskById(3).getDuration(),
                "Не меняется длительность задачи.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 23, 45), manager.getSubTaskById(3).getEndTime(),
                "Не меняется время окончания задачи.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 23, 30), manager.getEpicTaskById(2).getStartTime(),
                "Не меняется время старта задачи.");
        assertEquals(Duration.ofMinutes(15), manager.getEpicTaskById(2).getDuration(),
                "Не меняется длительность задачи.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 23, 45), manager.getEpicTaskById(2).getEndTime(),
                "Не меняется время окончания задачи.");
    }

    @Test
    void updateSubTaskWithTimeIntersection() {
        SubTask updatedSubTask = new SubTask("UpdatedSubTask", "New updated SubTask for test",
                "25.06.2023 21:15", 45, epicTask);
        updatedSubTask.setId(3);
        updatedSubTask.setStatus(TaskStatus.IN_PROGRESS);

        TimeValidationException ex = assertThrows(TimeValidationException.class, () -> manager.updateSubTask(updatedSubTask));
        assertEquals("Задача 'UpdatedSubTask' пересекается по времени с другими задачами.", ex.getMessage());
    }

    @Test
    void removeTaskByIdWithExistingId() {
        manager.removeTaskById(1);

        assertFalse(manager.getTaskList().contains(task), "Задача не удаляется.");
        assertThrows(IllegalArgumentException.class, () -> manager.getTaskById(1));
    }

    @Test
    void removeTaskByIdWithAnotherTypeId() {
        manager.removeTaskById(2);

        assertTrue(manager.getTaskList().contains(task), "Задача удаляется.");
        assertTrue(manager.getEpicTaskList().contains(epicTask), "Удаляется задача другого типа по id.");
    }

    @Test
    void removeTaskByIdWithNonExistingId() {
        manager.removeTaskById(5);

        assertTrue(manager.getTaskList().contains(task), "Задача удаляется.");
    }

    @Test
    void removeEpicTaskByIdWithExistingId() {
        manager.removeEpicTaskById(2);

        assertFalse(manager.getEpicTaskList().contains(epicTask), "Эпик не удаляется.");
        assertFalse(manager.getSubTaskList().contains(subTask), "Подзадача не удаляется.");
        assertThrows(IllegalArgumentException.class, () -> manager.getEpicTaskById(2));
        assertThrows(IllegalArgumentException.class, () -> manager.getSubTaskById(3));
    }

    @Test
    void removeEpicTaskByIdWithAnotherTypeId() {
        manager.removeEpicTaskById(1);

        assertTrue(manager.getEpicTaskList().contains(epicTask), "Эпик удаляется.");
        assertTrue(manager.getSubTaskList().contains(subTask), "Подзадача удаляется.");
        assertTrue(manager.getTaskList().contains(task), "Удаляется задача другого типа по id.");
    }

    @Test
    void removeEpicTaskByIdWithNonExistingId() {
        manager.removeEpicTaskById(5);

        assertTrue(manager.getEpicTaskList().contains(epicTask), "Эпик удаляется.");
        assertTrue(manager.getSubTaskList().contains(subTask), "Подзадача удаляется.");
    }

    @Test
    void removeSubTaskByIdWithExistingId() {
        manager.removeSubTaskById(3);

        assertFalse(manager.getSubTaskList().contains(subTask), "Подзадача не удаляется.");
        assertEquals(TaskStatus.NEW, manager.getEpicTaskById(2).getStatus(), "Статус эпика не обновляется.");
        assertTrue(manager.getEpicTaskById(2).getSubTasksIdList().isEmpty(), "У эпика не удаляется id подзадачи.");
    }

    @Test
    void removeSubTaskByIdWithAnotherTypeId() {
        manager.removeSubTaskById(1);

        assertTrue(manager.getSubTaskList().contains(subTask), "Подзадача удаляется.");
        assertTrue(manager.getTaskList().contains(task), "Удаляется задача другого типа по id.");
    }

    @Test
    void removeSubTaskByIdWithNonExistingId() {
        manager.removeSubTaskById(5);

        assertTrue(manager.getSubTaskList().contains(subTask), "Подзадача удаляется.");
    }

    @Test
    void getEpicSubTasksWhenEpicHaveSubTasks() {
        List<SubTask> epicSubTasksList = manager.getEpicSubTasks(2);

        assertNotNull(epicSubTasksList, "Список подзадач не возвращается.");
        assertIterableEquals(Collections.singletonList(subTask), epicSubTasksList, "Списки подзадач не совпадают.");
    }

    @Test
    void getEpicSubTasksWhenEpicHaveNoSubTasks() {
        manager.removeSubTasks();
        List<SubTask> epicSubTasksList = manager.getEpicSubTasks(2);

        assertNotNull(epicSubTasksList, "Список подзадач не возвращается.");
        assertTrue(epicSubTasksList.isEmpty(), "Списки подзадач не пуст.");
    }

    @Test
    void getEpicSubTasksWithNonExistingId() {
        assertThrows(NullPointerException.class, () -> manager.getEpicSubTasks(5));
    }

    @Test
    void getHistory() {
        manager.getTaskById(1);
        manager.getEpicTaskById(2);
        manager.getSubTaskById(3);
        List<Task> history = manager.getHistory();

        assertNotNull(history, "История просмотров не возвращается.");
        assertIterableEquals(Arrays.asList(task, epicTask, subTask), history, "История задач не совпадает.");
    }

    @Test
    void getHistoryWhenEmpty() {
        List<Task> history = manager.getHistory();

        assertNotNull(history, "История просмотров не возвращается.");
        assertTrue(history.isEmpty(), "История просмотров не пуста.");
    }

    @Test
    void getPrioritizedTasks() {
        SubTask subTask2 = new SubTask("TestSubTask2", "Second SubTask for test",
                "25.06.2023 20:00", 60, epicTask);
        manager.createSubTask(subTask2);
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertNotNull(prioritizedTasks, "Список не возвращается.");
        assertIterableEquals(Arrays.asList(subTask2, task, subTask), prioritizedTasks, "Списки не совпадают.");
    }

    @Test
    void getPrioritizedTasksWhenEmpty() {
        manager.removeTasks();
        manager.removeSubTasks();
        List<Task> prioritizedTasks = manager.getPrioritizedTasks();

        assertNotNull(prioritizedTasks, "Список отсортированных задач не возвращается.");
        assertTrue(prioritizedTasks.isEmpty(), "Список отсортированных задач не пуст.");
    }

    @Test
    void checkEpicTaskStatusWithNoSubtasks() {
        manager.removeSubTasks();

        assertEquals(TaskStatus.NEW, manager.getEpicTaskById(2).getStatus(), "Некорректный статус эпика.");
    }

    @Test
    void checkEpicTaskStatusWithNEWSubtasks() {
        subTask.setStatus(TaskStatus.NEW);
        manager.updateSubTask(subTask);
        SubTask subTask2 = new SubTask("TestSubTask2", "Second SubTask for test", epicTask);
        manager.createSubTask(subTask2);
        SubTask subTask3 = new SubTask("TestSubTask3", "Third SubTask for test", epicTask);
        manager.createSubTask(subTask3);

        assertEquals(TaskStatus.NEW, manager.getEpicTaskById(2).getStatus(), "Некорректный статус эпика.");
    }

    @Test
    void checkEpicTaskStatusWithINPROGRESSSubtasks() {
        subTask.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubTask(subTask);
        SubTask subTask2 = new SubTask("TestSubTask2", "Second SubTask for test", epicTask);
        subTask2.setStatus(TaskStatus.IN_PROGRESS);
        manager.createSubTask(subTask2);
        SubTask subTask3 = new SubTask("TestSubTask3", "Third SubTask for test", epicTask);
        subTask3.setStatus(TaskStatus.IN_PROGRESS);
        manager.createSubTask(subTask3);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicTaskById(2).getStatus(), "Некорректный статус эпика.");
    }

    @Test
    void checkEpicTaskStatusWithDONESubtasks() {
        SubTask subTask2 = new SubTask("TestSubTask2", "Second SubTask for test", epicTask);
        subTask2.setStatus(TaskStatus.DONE);
        manager.createSubTask(subTask2);
        SubTask subTask3 = new SubTask("TestSubTask3", "Third SubTask for test", epicTask);
        subTask3.setStatus(TaskStatus.DONE);
        manager.createSubTask(subTask3);

        assertEquals(TaskStatus.DONE, manager.getEpicTaskById(2).getStatus(), "Некорректный статус эпика.");
    }

    @Test
    void checkEpicTaskStatusWithDifferentSubtasks() {
        SubTask subTask2 = new SubTask("TestSubTask2", "Second SubTask for test", epicTask);
        manager.createSubTask(subTask2);
        SubTask subTask3 = new SubTask("TestSubTask3", "Third SubTask for test", epicTask);
        subTask3.setStatus(TaskStatus.IN_PROGRESS);
        manager.createSubTask(subTask3);

        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicTaskById(2).getStatus(), "Некорректный статус эпика.");
    }
}