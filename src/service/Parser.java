package service;

import model.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/** Утилитарный класс, отвечает за парсинг данных в файле */
public class Parser {
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
