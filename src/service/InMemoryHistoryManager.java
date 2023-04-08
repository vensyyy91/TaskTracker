package service;

import model.Task;
import java.util.LinkedList;
import java.util.List;

/** Класс для объекта-менеджера, в котором реализовано управление историей просмотров задач */
public class InMemoryHistoryManager implements HistoryManager {
    /** Константа Размер истории просмотров */
    private static final int HISTORY_SIZE = 10;
    /** Поле История просмотров */
    private final LinkedList<Task> taskHistory = new LinkedList<>();

    @Override
    public void add(Task task) {
        taskHistory.add(task);
        if (taskHistory.size() > HISTORY_SIZE) {
            taskHistory.remove(0);
        }
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory;
    }
}
