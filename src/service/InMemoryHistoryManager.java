package service;

import model.Task;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс для объекта-менеджера, в котором реализовано управление историей просмотров задач
 */
public class InMemoryHistoryManager implements HistoryManager {
    /** Поле История просмотров */
    private final List<Task> taskHistory = new ArrayList<>();

    @Override
    public void add(Task task) {
        taskHistory.add(task);
        if (taskHistory.size() > 10) {
            taskHistory.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory;
    }
}
