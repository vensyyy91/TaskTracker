package service;

import model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Класс для объекта-менеджера, в котором реализовано управление всеми задачами, хранит данные в файле */
public class FileBackedTaskManager extends InMemoryTaskManager {
    /** Поле Путь к файлу с данными */
    protected final String path;

    public FileBackedTaskManager(String path) {
        this.path = path;
    }

    @Override
    public void removeTasks() {
        super.removeTasks();
        save();
    }

    @Override
    public void removeEpicTasks() {
        super.removeEpicTasks();
        save();
    }

    @Override
    public void removeSubTasks() {
        super.removeSubTasks();
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = super.getTaskById(id);
        save();
        return task;
    }

    @Override
    public EpicTask getEpicTaskById(int id) {
        EpicTask epicTask = super.getEpicTaskById(id);
        save();
        return epicTask;
    }

    @Override
    public SubTask getSubTaskById(int id) {
        SubTask subTask = super.getSubTaskById(id);
        save();
        return subTask;
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createEpicTask(EpicTask epicTask) {
        super.createEpicTask(epicTask);
        save();
    }

    @Override
    public void createSubTask(SubTask subTask) {
        super.createSubTask(subTask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpicTask(EpicTask epicTask) {
        super.updateEpicTask(epicTask);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void removeEpicTaskById(int id) {
        super.removeEpicTaskById(id);
        save();
    }

    @Override
    public void removeSubTaskById(int id) {
        super.removeSubTaskById(id);
        save();
    }

    /**
     * Метод сохранения текущего состояния менеджера в файл
     */
    private void save() {
        try (FileWriter writer = new FileWriter(path, StandardCharsets.UTF_8)) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : tasks.values()) {
                writer.write(task.toString() + "\n");
            }
            for (EpicTask epicTask : epicTasks.values()) {
                writer.write(epicTask.toString() + "\n");
            }
            for (SubTask subTask : subTasks.values()) {
                writer.write(subTask.toString() + "\n");
            }
            writer.write("\n" + Parser.historyToString(historyManager));
        } catch (IOException ex) {
            throw new ManagerSaveException("Произошла ошибка во время записи файла.");
        }
    }

    /**
     * Метод перевода задачи из строки в объект класса Task
     * @param value - строка с данными о задаче
     * @return возвращает задачу (объект класса Task)
     */
    private Task fromString(String value) {
        String[] taskData = value.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        switch (taskData[1]) {
            case "TASK":
                Task task = new Task(taskData[2], taskData[4]);
                task.setId(Integer.parseInt(taskData[0]));
                task.setStatus(TaskStatus.valueOf(taskData[3]));
                return task;
            case "EPIC":
                EpicTask epicTask = new EpicTask(taskData[2], taskData[4]);
                epicTask.setId(Integer.parseInt(taskData[0]));
                epicTask.setStatus(TaskStatus.valueOf(taskData[3]));
                return epicTask;
            case "SUBTASK":
                EpicTask masterTask = epicTasks.get(Integer.parseInt(taskData[5]));
                SubTask subTask = new SubTask(taskData[2], taskData[4], masterTask);
                subTask.setId(Integer.parseInt(taskData[0]));
                subTask.setStatus(TaskStatus.valueOf(taskData[3]));
                masterTask.getSubTasksIdList().add(subTask.getId());
                return subTask;
        }
        return null;
    }

    /**
     * Метод, восстанавливающий данные менеджера из файла
     * @param file - файл с данными
     * @return возвращает объект класса FileBackedTaskManager
     */
    private static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getPath());
        try {
            String[] dataArray = Files.readString(Path.of(manager.path)).split("\n");
            if (dataArray.length <= 1) {
                return manager;
            }
            boolean isHistoryEmpty = true;
            for (String data : dataArray) {
                if (data.isBlank()) {
                    isHistoryEmpty = false;
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
            if (!isHistoryEmpty) {
                List<Integer> historyIdList = Parser.historyFromString(dataArray[dataArray.length - 1]);
                for (int id : historyIdList) {
                    if (manager.tasks.containsKey(id)) {
                        manager.historyManager.add(manager.tasks.get(id));
                    } else if (manager.epicTasks.containsKey(id)) {
                        manager.historyManager.add(manager.epicTasks.get(id));
                    } else if (manager.subTasks.containsKey(id)) {
                        manager.historyManager.add(manager.subTasks.get(id));
                    }
                }
            }
        } catch (IOException ex) {
            throw new ManagerSaveException("Произошла ошибка во время загрузки файла.");
        }
        return manager;
    }

    public static void main(String[] args) {
        FileBackedTaskManager manager = new FileBackedTaskManager("resources" + File.separator + "tasks.csv");

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
        manager.getSubTaskById(5);
        manager.getEpicTaskById(3);
        manager.getSubTaskById(4);
        manager.getTaskById(1);
        manager.getTaskById(2);
        manager.getSubTaskById(6);
        manager.getTaskById(1);
        manager.getEpicTaskById(7);
        manager.getSubTaskById(4); // история 5,3,2,6,1,7,4

        FileBackedTaskManager newManager = loadFromFile(new File("resources" + File.separator + "tasks.csv"));
        System.out.println("Задачи:");
        System.out.println(newManager.getTaskList());
        System.out.println("Эпики:");
        System.out.println(newManager.getEpicTaskList());
        System.out.println("Подзадачи:");
        System.out.println(newManager.getSubTaskList());
        System.out.println("История просмотров:");
        for (Task task : newManager.getHistory()) {
            System.out.println(task);
        }
    }
}
