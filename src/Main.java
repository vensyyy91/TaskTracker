public class Main {

    public static void main(String[] args) {
        Manager manager = new Manager();
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

        System.out.println("Получение задачи, подзадачи или эпика по идентификаторам 1, 3, 5, 19:");
        System.out.println(manager.getTaskById(1));
        System.out.println(manager.getTaskById(3));
        System.out.println(manager.getTaskById(5));
        System.out.println(manager.getTaskById(19));
        System.out.println("----------------------------------------");

        shopping.setStatus("IN_PROGRESS");
        manager.updateTask(shopping);
        studying.setName("Сдать 3 финальное задание");
        manager.updateEpicTask(studying);
        studying1.setStatus("IN_PROGRESS");
        manager.updateSubTask(studying1);
        studying2.setStatus("DONE");
        manager.updateSubTask(studying2);
        coding1.setStatus("DONE");
        manager.updateSubTask(coding1);

        System.out.println("Список после обновления:");
        System.out.println(manager.getTaskList());
        System.out.println(manager.getEpicTaskList());
        System.out.println(manager.getSubTaskList());
        System.out.println("----------------------------------------");

        manager.removeTaskById(1);
        manager.removeTaskById(6);
        manager.removeTaskById(123);

        System.out.println("Список после удаления отдельных заданий:");
        System.out.println(manager.getTaskList());
        System.out.println(manager.getEpicTaskList());
        System.out.println(manager.getSubTaskList());
        System.out.println("----------------------------------------");

        System.out.println("Список всех подзадач эпика с идентификатором 3:");
        System.out.println(manager.getEpicSubTasks(3));
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
