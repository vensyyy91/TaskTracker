package service;

/**
 * Утилитарный класс, отвечает за создание менеджеров
 */
public class Managers {
    private Managers() {
    }

    /**
     * Метод получения объекта-менеджера задач
     * @return возвращает объект, реализуцющий интерфейс TaskManager
     */
    public static TaskManager getDefault() {
        return new HttpTaskManager("http://localhost:8078/");
    }

    /**
     * Метод получения объекта-менеджера истории просмотров
     * @return возвращает объект, реализуцющий интерфейс HistoryManager
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
