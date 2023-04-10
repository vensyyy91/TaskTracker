package service;

import model.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/** Класс для объекта-менеджера, в котором реализовано управление всеми задачами */
public class InMemoryTaskManager implements TaskManager {
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

    /**
     * Метод обновления статуса эпика
     * @param epicTask - эпик (объект класса EpicTask)
     */
    private void checkEpicTaskStatus(EpicTask epicTask) {
        boolean isInProgress = false;
        boolean isDone = !epicTask.getSubTasksIdList().isEmpty();
        for (Integer subTaskId : epicTask.getSubTasksIdList()) {
            switch (subTasks.get(subTaskId).getStatus()) {
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

    /**
     * Метод получения уникального идентификатора
     * @return возвращает уникальный идентификатор
     */
    private int getNewId() {
        id++;
        return id;
    }

    @Override
    public ArrayList<Task> getTaskList() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<EpicTask> getEpicTaskList() {
        return new ArrayList<>(epicTasks.values());
    }

    @Override
    public ArrayList<SubTask> getSubTaskList() {
        return new ArrayList<>(subTasks.values());
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
            epicTask.getSubTasksIdList().clear();
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
        tasks.put(task.getId(), task);
    }

    @Override
    public void createEpicTask(EpicTask epicTask) {
        epicTask.setId(getNewId());
        epicTasks.put(epicTask.getId(), epicTask);
    }

    @Override
    public void createSubTask(SubTask subTask) {
        subTask.setId(getNewId());
        subTasks.put(subTask.getId(), subTask);
        EpicTask masterTask = epicTasks.get(subTask.getMasterTaskId());
        masterTask.getSubTasksIdList().add(subTask.getId());
        checkEpicTaskStatus(masterTask);
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
        checkEpicTaskStatus(epicTasks.get(subTask.getMasterTaskId()));
    }

    @Override
    public void removeTaskById(int id) {
        tasks.remove(id);
    }

    @Override
    public void removeEpicTaskById(int id) {
        for (int subTaskId : epicTasks.get(id).getSubTasksIdList()) {
            subTasks.remove(subTaskId);
        }
        epicTasks.remove(id);
    }

    @Override
    public void removeSubTaskById(int id) {
        EpicTask masterTask = epicTasks.get(subTasks.get(id).getMasterTaskId());
        masterTask.getSubTasksIdList().remove(Integer.valueOf(id));
        checkEpicTaskStatus(masterTask);
        subTasks.remove(id);
    }

    @Override
    public ArrayList<SubTask> getEpicSubTasks(int id) {
        ArrayList<SubTask> epicSubTasks = new ArrayList<>();
        for (Integer subTaskId : epicTasks.get(id).getSubTasksIdList()) {
            epicSubTasks.add(subTasks.get(subTaskId));
        }
        return epicSubTasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}
