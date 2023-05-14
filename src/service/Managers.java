package service;

import model.EpicTask;
import model.SubTask;
import model.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * Утилитарный класс, отвечает за создание менеджеров
 */
public class Managers {
    /**
     * Метод получения объекта-менеджера задач
     * @return возвращает объект, реализуцющий интерфейс TaskManager
     */
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    /**
     * Метод получения объекта-менеджера истории просмотров
     * @return возвращает объект, реализуцющий интерфейс HistoryManager
     */
    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    /**
     * Метод, восстанавливающий данные менеджера из файла
     * @param file - файл с данными
     * @return возвращает объект класса FileBackedTaskManager
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getPath());
        try {
            String[] dataArray = Files.readString(Path.of(manager.path)).split("\n");
            if (dataArray.length <= 1) {
                return manager;
            }
            for (String data : dataArray) {
                if (data.isBlank()) {
                    break;
                }
                Task task = manager.fromString(data);
                if (task instanceof EpicTask) {
                    manager.epicTasks.put(task.getId(), (EpicTask) task);
                } else if (task instanceof SubTask) {
                    manager.subTasks.put(task.getId(), (SubTask) task);
                } else if (task != null) {
                    manager.tasks.put(task.getId(), task);
                }
            }
            List<Integer> historyIdList = historyFromString(dataArray[dataArray.length - 1]);
            for (int id : historyIdList) {
                if (manager.tasks.containsKey(id)) {
                    manager.historyManager.add(manager.tasks.get(id));
                } else if (manager.epicTasks.containsKey(id)) {
                    manager.historyManager.add(manager.epicTasks.get(id));
                } else if (manager.subTasks.containsKey(id)) {
                    manager.historyManager.add(manager.subTasks.get(id));
                }
            }
        } catch (IOException ex) {
            throw new ManagerSaveException("Произошла ошибка во время загрузки файла.");
        }
        return manager;
    }

    /**
     * Метод перевода истории просмотров в текстовую строку
     * @param historyManager - объект класса HistoryManager (история просмотров)
     * @return возвращает историю просмотров ввиде строки
     */
    public static String historyToString(HistoryManager historyManager) {
        StringJoiner joiner = new StringJoiner(",");
        for (Task task : historyManager.getHistory()) {
            joiner.add(Integer.toString(task.getId()));
        }
        return joiner.toString();
    }

    /**
     * Метод перевода истории просмотров из строки в список id задач
     * @param value - строка, представляющая историю просмотров
     * @return возвращает список id задач из истории просмотров
     */
    public static List<Integer> historyFromString(String value) {
        List<Integer> history = new ArrayList<>();
        String[] historyData = value.split(",");
        for (String data : historyData) {
            history.add(Integer.parseInt(data));
        }
        return history;
    }
}
