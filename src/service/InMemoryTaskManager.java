package service;

import model.*;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * Класс для объекта-менеджера, в котором реализовано управление всеми задачами
 */
public class InMemoryTaskManager extends Managers implements TaskManager {
    /** Поле Задачи */
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    /** Поле Эпики */
    private final HashMap<Integer, EpicTask> epicTasks = new HashMap<>();
    /** Поле Подзадачи */
    private final HashMap<Integer, SubTask> subTasks = new HashMap<>();
    /** Поле Идентификатор */
    private int id;
    /** Поле История просмотров */
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public HashMap<Integer, Task> getTaskList() {
        return tasks;
    }

    @Override
    public HashMap<Integer, EpicTask> getEpicTaskList() {
        return epicTasks;
    }

    @Override
    public HashMap<Integer, SubTask> getSubTaskList() {
        return subTasks;
    }

    @Override
    public void removeTasks() {
        tasks.clear();
    }

    @Override
    public void removeEpicTasks() {
        epicTasks.clear();
        subTasks.clear();
    }

    @Override
    public void removeSubTasks() {
        subTasks.clear();
        for (EpicTask epicTask : epicTasks.values()) {
            epicTask.getSubTasks().clear();
            checkEpicTaskStatus(epicTask);
        }
    }

    @Override
    public Task getTaskById(int id) {
        if (tasks.get(id) != null) {
            historyManager.add(tasks.get(id));
        }
        return Optional.ofNullable(tasks.get(id)).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public EpicTask getEpicTaskById(int id) {
        if (epicTasks.get(id) != null) {
            historyManager.add(epicTasks.get(id));
        }
        return Optional.ofNullable(epicTasks.get(id)).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public SubTask getSubTaskById(int id) {
        if (subTasks.get(id) != null) {
            historyManager.add(subTasks.get(id));
        }
        return Optional.ofNullable(subTasks.get(id)).orElseThrow(IllegalArgumentException::new);
    }

    @Override
    public void createTask(Task task) {
        task.setId(getNewId());
        tasks.put(id, task);
    }

    @Override
    public void createEpicTask(EpicTask epicTask) {
        epicTask.setId(getNewId());
        epicTasks.put(id, epicTask);
    }

    @Override
    public void createSubTask(SubTask subTask) {
        subTask.setId(getNewId());
        subTasks.put(id, subTask);
        subTask.getMasterTask().getSubTasks().put(id, subTask);
        checkEpicTaskStatus(subTask.getMasterTask());
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpicTask(EpicTask epicTask) {
        epicTasks.put(epicTask.getId(), epicTask);
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        subTasks.put(subTask.getId(), subTask);
        checkEpicTaskStatus(subTask.getMasterTask());
    }

    @Override
    public void removeTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public void removeEpicTaskById(int id) {
        for (int i : epicTasks.get(id).getSubTasks().keySet()) {
            subTasks.remove(i);
        }
        epicTasks.remove(id);
    }

    @Override
    public void removeSubTaskById(int id) {
        subTasks.get(id).getMasterTask().getSubTasks().remove(id);
        checkEpicTaskStatus(subTasks.get(id).getMasterTask());
        subTasks.remove(id);
    }

    @Override
    public HashMap<Integer, SubTask> getEpicSubTasks(int id) {
        return epicTasks.get(id).getSubTasks();
    }

    @Override
    public void checkEpicTaskStatus(EpicTask epicTask) {
        boolean isInProgress = false;
        boolean isDone = !epicTask.getSubTasks().isEmpty();
        for (SubTask task : epicTask.getSubTasks().values()) {
            switch (task.getStatus()) {
                case DONE:
                    isInProgress = true;
                    break;
                case IN_PROGRESS:
                    isInProgress = true;
                    isDone = false;
                    break;
                case NEW:
                    isDone = false;
                    break;
            }
        }
        if (isDone) {
            epicTask.setStatus(TaskStatus.DONE);
        } else if (isInProgress) {
            epicTask.setStatus(TaskStatus.IN_PROGRESS);
        } else {
            epicTask.setStatus(TaskStatus.NEW);
        }
    }

    @Override
    public int getNewId() {
        id++;
        return id;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
