package service;

import model.*;
import java.util.List;
import java.util.Map;

/** Интерфейс для объектов-менеджеров, содержит список методов для управления задачами */
public interface TaskManager {
    /**
     * Метод-геттер для получения мапы с задачами
     * @return возвращает мапу с задачами
     */
    Map<Integer, Task> getTasksMap();

    /**
     * Метод-геттер для получения мапы с эпиками
     * @return возвращает мапу с эпиками
     */
    Map<Integer, EpicTask> getEpicTasksMap();

    /**
     * Метод-геттер для получения мапы с подзадачами
     * @return возвращает мапу с подзадачами
     */
    Map<Integer, SubTask> getSubTasksMap();

    /**
     * Метод-геттер для получения менеджера истории
     * @return объект класса InMemoryHistoryManager
     */
    HistoryManager getHistoryManager();

    /**
     * Метод получения списка задач
     * @return возвращает ArrayList со списком задач
     */
    List<Task> getTaskList();

    /**
     * Метод получения списка эпиков
     * @return возвращает ArrayList со списком эпиков
     */
    List<EpicTask> getEpicTaskList();

    /**
     * Метод получения списка подзадач
     * @return возвращает ArrayList со списком подзадач
     */
    List<SubTask> getSubTaskList();

    /**
     * Метод удаления всех задач
     */
    void removeTasks();

    /**
     * Метод удаления всех эпиков, также удаляет все подзадачи эпиков
     */
    void removeEpicTasks();

    /**
     * Метод удаления всех подзадач, также обновляет статусы всех эпиков
     */
    void removeSubTasks();

    /**
     * Метод получения задачи по идентификатору
     * @param id - идентификатор задачи
     * @return возвращает задачу (объект класса Task)
     */
    Task getTaskById(int id);

    /**
     * Метод получения эпика по идентификатору
     * @param id - идентификатор эпика
     * @return возвращает эпик (объект класса EpicTask)
     */
    EpicTask getEpicTaskById(int id);

    /**
     * Метод получения подзадачи по идентификатору
     * @param id - идентификатор подзадачи
     * @return возвращает подзадачу (объект класса SubTask)
     */
    SubTask getSubTaskById(int id);

    /**
     * Метод создания задачи
     * @param task - задача (объект класса Task)
     */
    void createTask(Task task);

    /**
     * Метод создания эпика
     * @param epicTask - эпик (объект класса EpicTask)
     */
    void createEpicTask(EpicTask epicTask);

    /**
     * Метод создания подзадачи, также обновляет статус соответствующего эпика
     * @param subTask - подзачдача (объект класса SubTask)
     */
    void createSubTask(SubTask subTask);

    /**
     * Метод обновления задачи
     * @param task - обновленная задача (объект класса Task)
     */
    void updateTask(Task task);

    /**
     * Метод обновления эпика
     * @param epicTask - обновленный эпик (объект класса EpicTask)
     */
    void updateEpicTask(EpicTask epicTask);

    /**
     * Метод обновления подзадачи, также обновляет статус соответствующего эпика
     * @param subTask - обновленная подзадача (объект класса SubTask)
     */
    void updateSubTask(SubTask subTask);

    /**
     * Метод удаления задачи по идентификатору
     * @param id - идентификатор задачи
     */
    void removeTaskById(int id);

    /**
     * Метод удаления эпика по идентификатору, также удаляет все подзадачи данного эпика
     * @param id - идентификатор эпика
     */
    void removeEpicTaskById(int id);

    /**
     * Метод удаления подзадачи по идентификатору, также обновляет статус соответствующего эпика
     * @param id - идентификатор подзадачи
     */
    void removeSubTaskById(int id);

    /**
     * Метод получения списка всех подзадач конкретного эпика по идентификатору
     * @param id - идентификатор эпика
     * @return возвращает список подзадач
     */
    List<SubTask> getEpicSubTasks(int id);

    /**
     * Метод получения истории просмотров
     * @return возвращает список просмотренных задач
     */
    List<Task> getHistory();

    /**
     * Метод получения списка задач, отсортированных по времени старта
     * @return возвращает список задач
     */
    List<Task> getPrioritizedTasks();
}
