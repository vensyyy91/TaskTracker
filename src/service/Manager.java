package service;

import model.*;
import java.util.HashMap;
import java.util.Optional;

public class Manager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, EpicTask> epicTasks = new HashMap<>();
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();
    private int id;

    public HashMap<Integer, Task> getTaskList() { // Получение списка задач
        return tasks;
    }

    public HashMap<Integer, EpicTask> getEpicTaskList() { // Получение списка эпиков
        return epicTasks;
    }

    public HashMap<Integer, SubTask> getSubTaskList() { // Получение списка подзадач
        return subTasks;
    }

    public void removeTasks() { // Удаление всех задач
        tasks.clear();
    }

    public void removeEpicTasks() { // Удаление всех эпиков
        epicTasks.clear();
        subTasks.clear();
    }

    public void removeSubTasks() { // Удаление всех подзадач
        subTasks.clear();
        for (EpicTask epicTask : epicTasks.values()) {
            epicTask.getSubTasks().clear();
            checkEpicTaskStatus(epicTask);
        }
    }

    public Task getTaskById(int id) { // Возвращает задачу по идентификатору
            return Optional.ofNullable(tasks.get(id)).orElseThrow(IllegalArgumentException::new);
    }

    public EpicTask getEpicTaskById(int id) { // Возвращает эпик по идентификатору
        return Optional.ofNullable(epicTasks.get(id)).orElseThrow(IllegalArgumentException::new);
    }

    public SubTask getSubTaskById(int id) { // Возвращает подзадачу по идентификатору
        return Optional.ofNullable(subTasks.get(id)).orElseThrow(IllegalArgumentException::new);
    }

    public void createTask(Task task) { // Создание задачи
        task.setId(getNewId());
        tasks.put(id, task);
    }

    public void createEpicTask(EpicTask epicTask) { // Создание эпика
        epicTask.setId(getNewId());
        epicTasks.put(id, epicTask);
    }

    public void createSubTask(SubTask subTask) { // Создание подзадачи
        subTask.setId(getNewId());
        subTasks.put(id, subTask);
        subTask.getMasterTask().getSubTasks().put(id, subTask);
        checkEpicTaskStatus(subTask.getMasterTask());
    }

    public void updateTask(Task task) { // Обновление задачи
        tasks.put(task.getId(), task);
    }

    public void updateEpicTask(EpicTask epicTask) { // Обновление эпика
        epicTasks.put(epicTask.getId(), epicTask);
    }

    public void updateSubTask(SubTask subTask) { // Обновление подзадачи
        subTasks.put(subTask.getId(), subTask);
        checkEpicTaskStatus(subTask.getMasterTask());
    }

    public void removeTaskById(int id) { // Удаление задачи по индентификатору
            tasks.remove(id);
    }

    public void removeEpicTaskById(int id) { // Удаление эпика по идентификатору
        for (int i : epicTasks.get(id).getSubTasks().keySet()) {
            subTasks.remove(i);
        }
        epicTasks.remove(id);
    }

    public void removeSubTaskById(int id) { // Удаление подзадачи по идентификатору
        subTasks.get(id).getMasterTask().getSubTasks().remove(id);
        checkEpicTaskStatus(subTasks.get(id).getMasterTask());
        subTasks.remove(id);
    }

    public HashMap<Integer, SubTask> getEpicSubTasks(int id) { // Получение списка всех подзадач конкретного эпика по идентификатору
        return epicTasks.get(id).getSubTasks();
    }

    public void checkEpicTaskStatus(EpicTask epicTask) { // Обновление статуса эпика
        boolean isInProgress = false;
        boolean isDone = true;
        if (epicTask.getSubTasks().isEmpty()) {
            isDone = false;
        }
        for (SubTask task : epicTask.getSubTasks().values()) {
            switch (task.getStatus()) {
                case "DONE":
                    isInProgress = true;
                    break;
                case "IN_PROGRESS":
                    isInProgress = true;
                    isDone = false;
                    break;
                case "NEW":
                    isDone = false;
                    break;
            }
        }
        if (isDone) {
            epicTask.setStatus("DONE");
        } else if (isInProgress) {
            epicTask.setStatus("IN_PROGRESS");
        } else {
            epicTask.setStatus("NEW");
        }
    }

    public int getNewId() { // Получение уникального ID
        id++;
        return id;
    }
}
