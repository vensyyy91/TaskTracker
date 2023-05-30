package service;

import model.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            writer.write("id,type,name,status,description,start,end,epic\n");
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        String[] taskData = value.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        String name = taskData[2].substring(1,taskData[2].length() - 1);
        String description = taskData[4].substring(1,taskData[4].length() - 1);
        LocalDateTime start = LocalDateTime.parse(taskData[5], formatter);
        LocalDateTime end = LocalDateTime.parse(taskData[6], formatter);
        switch (taskData[1]) {
            case "TASK":
                Task task = new Task(name, description);
                task.setId(Integer.parseInt(taskData[0]));
                task.setStatus(TaskStatus.valueOf(taskData[3]));
                task.setStartTime(start);
                task.setDuration(Duration.between(start, end));
                return task;
            case "EPIC":
                EpicTask epicTask = new EpicTask(name, description);
                epicTask.setId(Integer.parseInt(taskData[0]));
                epicTask.setStatus(TaskStatus.valueOf(taskData[3]));
                epicTask.setStartTime(start);
                epicTask.setDuration(Duration.between(start, end));
                epicTask.setEndTime(end);
                return epicTask;
            case "SUBTASK":
                EpicTask masterTask = epicTasks.get(Integer.parseInt(taskData[7]));
                SubTask subTask = new SubTask(name, description, masterTask);
                subTask.setId(Integer.parseInt(taskData[0]));
                subTask.setStatus(TaskStatus.valueOf(taskData[3]));
                subTask.setStartTime(start);
                subTask.setDuration(Duration.between(start, end));
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
    protected static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getPath());
        try {
            String[] data = Files.readString(Path.of(manager.path)).split("\n");
            if (data.length <= 1) {
                return manager;
            }
            int lastId = 0;
            boolean isHistoryEmpty = true;
            for (int i = 1; i < data.length; i++) {
                if (data[i].isBlank()) {
                    isHistoryEmpty = false;
                    break;
                }
                Task task = manager.fromString(data[i]);
                if (task!= null) {
                    lastId = Math.max(task.getId(), lastId);
                    if (task instanceof EpicTask) {
                        manager.epicTasks.put(task.getId(), (EpicTask) task);
                    } else if (task instanceof SubTask) {
                        manager.subTasks.put(task.getId(), (SubTask) task);
                    } else {
                        manager.tasks.put(task.getId(), task);
                    }
                }
            }
            manager.prioritizedTasks.addAll(manager.tasks.values());
            manager.prioritizedTasks.addAll(manager.subTasks.values());
            manager.id = lastId;
            if (!isHistoryEmpty) {
                List<Integer> historyIdList = Parser.historyFromString(data[data.length - 1]);
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
}