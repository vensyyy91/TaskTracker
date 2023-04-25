package service;

import model.*;
import java.util.List;

/** Интерфейс для объектов-менеджеров, содержит список методов для управления историей просмотров задач */
public interface HistoryManager {
    /**
     * Метод добавления задачи в историю просмотров
     * @param task - задача
     */
    void add(Task task);

    /**
     * Метод удаления задачи из истории просмотров
     * @param id - идентификатор задачи
     */
    void remove(int id);

    /**
     * Метод получения истории просмотров задач
     * @return возвращает историю просмотров ввиде списка
     */
    List<Task> getHistory();
}
