import model.*;
import service.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task shopping = new Task("Сходить в магазин",
                "Сходить в продуктовый магазин, купить хлеба, десяток яиц, палку колбасы, кусок сыра");
        shopping.setStartTime(LocalDateTime.of(2023,6,25,21,30));
        shopping.setDuration(Duration.ofMinutes(30));
        manager.createTask(shopping); // id = 1
        Task film = new Task("Посмотреть фильмы",
                "Посмотреть фильмы, которые находятся в закладках браузера");
        manager.createTask(film); // id = 2
        EpicTask studying = new EpicTask("Сдать 7 ТЗ",
                "Доделать и сдать финальное задание 7 спринта");
        manager.createEpicTask(studying); // id = 3
        SubTask studying1 = new SubTask("Дописать тесты",
                "Дописать тесты на все методы", studying);
        studying1.setStartTime(LocalDateTime.of(2023,6,25,22,0));
        studying1.setDuration(Duration.ofMinutes(60));
        manager.createSubTask(studying1); // id = 4
        SubTask studying2 = new SubTask("Сдать задание",
                "Загрузить код на GitHub, отправить на проверку ревьюеру", studying);
        studying2.setStartTime(LocalDateTime.of(2023,6,25,23,0));
        studying2.setDuration(Duration.ofMinutes(10));
        manager.createSubTask(studying2); // id = 5
        SubTask studying3 = new SubTask("Пройти ревью",
                "Изучить фидбэк, при необходимости внести исправления", studying);
        studying3.setStartTime(LocalDateTime.of(2023,6,26,22,0));
        studying3.setDuration(Duration.ofMinutes(60));
        manager.createSubTask(studying3); // id = 6
        EpicTask coding = new EpicTask("Codewars",
                "Попрактиковаться в решении задач на codewars");
        manager.createEpicTask(coding); // id = 7

        manager.getTaskList().forEach(System.out::println);
        manager.getEpicTaskList().forEach(System.out::println);
        manager.getSubTaskList().forEach(System.out::println);
        System.out.println("-----------------------------------------");
        manager.getPrioritizedTasks().forEach(System.out::println);
        System.out.println("-----------------------------------------");
    }

    public static void printHistory(List<Task> taskHistory) {
        for (Task task : taskHistory) {
            System.out.println(task);
        }
        System.out.println("----------------------------------------");
    }
}
