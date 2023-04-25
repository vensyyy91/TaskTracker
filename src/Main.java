import model.*;
import service.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task shopping = new Task("Сходить в магазин",
                "Сходить в продуктовый магазин, купить хлеба, десяток яиц, палку колбасы, кусок сыра");
        manager.createTask(shopping); // id = 1
        Task film = new Task("Посмотреть фильмы",
                "Посмотреть фильмы, которые находятся в закладках браузера");
        manager.createTask(film); // id = 2
        EpicTask studying = new EpicTask("Сдать 5 ТЗ",
                "Доделать и сдать финальное задание 5 спринта");
        manager.createEpicTask(studying); // id = 3
        SubTask studying1 = new SubTask("Дописать код",
                "Дописать код в классе InMemoryHistoryManager", studying);
        manager.createSubTask(studying1); // id = 4
        SubTask studying2 = new SubTask("Сдать задание",
                "Загрузить код на GitHub, отправить на проверку ревьюеру", studying);
        manager.createSubTask(studying2); // id = 5
        SubTask studying3 = new SubTask("Пройти ревью",
                "Изучить фидбэк, при необходимости внести исправления", studying);
        manager.createSubTask(studying3); // id = 6
        EpicTask coding = new EpicTask("Codewars",
                "Попрактиковаться в решении задач на codewars");
        manager.createEpicTask(coding); // id = 7

        manager.getTaskById(1);
        manager.getSubTaskById(4);
        manager.getEpicTaskById(7);
        manager.getTaskById(1);
        printHistory(manager.getHistory());

        manager.getSubTaskById(5);
        manager.getEpicTaskById(3);
        manager.getSubTaskById(4);
        printHistory(manager.getHistory());

        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getSubTaskById(6);
        manager.getTaskById(1);
        manager.getEpicTaskById(7);
        manager.getSubTaskById(4);
        printHistory(manager.getHistory());

        manager.removeTaskById(2);
        printHistory(manager.getHistory());

        manager.removeEpicTaskById(3);
        printHistory(manager.getHistory());
    }

    public static void printHistory(List<Task> taskHistory) {
        for (Task task : taskHistory) {
            System.out.println(task);
        }
        System.out.println("----------------------------------------");
    }
}
