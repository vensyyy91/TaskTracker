import model.*;
import service.*;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        Task shopping = new Task("Сходить в магазин",
                "Сходить в продуктовый магазин, купить хлеба, десяток яиц, палку колбасы, кусок сыра");
        Task film = new Task("Посмотреть фильмы",
                "Посмотреть фильмы, которые находятся в закладках браузера");
        EpicTask studying = new EpicTask("Сдать 3 ТЗ",
                "Доделать и сдать финальное задание 3 спринта");
        EpicTask coding = new EpicTask("Codewars",
                "Попрактиковаться в решении задач на codewars");
        SubTask studying1 = new SubTask("Дописать код",
                "Дописать код в классах Manager и SubTask", studying);
        SubTask studying2 = new SubTask("Сдать задание",
                "Загрузить код на GitHub, отправить на проверку ревьюеру", studying);
        SubTask coding1 = new SubTask("5 kyu",
                "Поднять уровень в Java до 5 kyu", coding);

        manager.createTask(shopping);
        manager.createTask(film);
        manager.createEpicTask(studying);
        manager.createSubTask(studying1);
        manager.createSubTask(studying2);
        manager.createEpicTask(coding);
        manager.createSubTask(coding1);

        System.out.println("Список всех задач, подзадач и эпиков:");
        System.out.println(manager.getTaskList());
        System.out.println(manager.getEpicTaskList());
        System.out.println(manager.getSubTaskList());
        System.out.println("----------------------------------------");

        System.out.println("Получение задачи, подзадачи или эпика по идентификаторам:");
        System.out.println(manager.getTaskById(1));
        System.out.println(manager.getEpicTaskById(3));
        System.out.println(manager.getSubTaskById(5));
        System.out.println(manager.getTaskById(1));
        System.out.println(manager.getTaskById(2));
        System.out.println(manager.getEpicTaskById(6));
        System.out.println(manager.getSubTaskById(7));
        System.out.println(manager.getSubTaskById(5));
        System.out.println(manager.getSubTaskById(4));
        System.out.println(manager.getTaskById(2));
        System.out.println(manager.getEpicTaskById(3));
        System.out.println("----------------------------------------");

        System.out.println("История просмотров:");
        List<Task> taskHistory = manager.getHistory();
        for (Task task : taskHistory) {
            System.out.println(task);
        }
        System.out.println("----------------------------------------");

        shopping.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateTask(shopping);
        studying.setName("Сдать 3 финальное задание");
        manager.updateEpicTask(studying);
        studying1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubTask(studying1);
        coding1.setStatus(TaskStatus.DONE);
        manager.updateSubTask(coding1);

        System.out.println("Список после обновления:");
        System.out.println(manager.getTaskList());
        System.out.println(manager.getEpicTaskList());
        System.out.println(manager.getSubTaskList());
        System.out.println("----------------------------------------");

        System.out.println("Список всех подзадач эпика с идентификатором 3:");
        System.out.println(manager.getEpicSubTasks(3));
        System.out.println("----------------------------------------");

        manager.removeTaskById(1);
        System.out.println("Задача с идентификатором 1 удалена.");
        manager.removeEpicTaskById(6);
        System.out.println("Эпик с идентификатором 6 удален.");
        manager.removeSubTaskById(4);
        System.out.println("Подзадача с идентификатором 4 удалена.");

        System.out.println("Список после удаления отдельных заданий:");
        System.out.println(manager.getTaskList());
        System.out.println(manager.getEpicTaskList());
        System.out.println(manager.getSubTaskList());
        System.out.println("----------------------------------------");

        manager.removeTasks();
        manager.removeSubTasks();
        manager.removeEpicTasks();

        System.out.println("Список после удаления всех заданий:");
        System.out.println(manager.getTaskList());
        System.out.println(manager.getEpicTaskList());
        System.out.println(manager.getSubTaskList());
        System.out.println("----------------------------------------");
    }
}
