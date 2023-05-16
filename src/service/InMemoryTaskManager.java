package service;

import model.*;

import java.util.*;

/** Класс для объекта-менеджера, в котором реализовано управление всеми задачами, хранит данные в оперативной памяти */
public class InMemoryTaskManager implements TaskManager {
    /** Поле Задачи */
    protected final Map<Integer, Task> tasks = new HashMap<>();
    /** Поле Эпики */
    protected final Map<Integer, EpicTask> epicTasks = new HashMap<>();
    /** Поле Подзадачи */
    protected final Map<Integer, SubTask> subTasks = new HashMap<>();
    /** Поле Идентификатор */
    private int id;
    /** Поле История просмотров */
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public List<Task> getTaskList() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<EpicTask> getEpicTaskList() {
        return new ArrayList<>(epicTasks.values());
    }

    @Override
    public List<SubTask> getSubTaskList() {
        return new ArrayList<>(subTasks.values());
    }

    @Override
    public void removeTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void removeEpicTasks() {
        for (Integer id : epicTasks.keySet()) {
            historyManager.remove(id);
        }
        epicTasks.clear();
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
        }
        subTasks.clear();
    }

    @Override
    public void removeSubTasks() {
        for (Integer id : subTasks.keySet()) {
            historyManager.remove(id);
        }
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
        historyManager.remove(id);
    }

    @Override
    public void removeEpicTaskById(int id) {
        for (int subTaskId : epicTasks.remove(id).getSubTasksIdList()) {
            subTasks.remove(subTaskId);
            historyManager.remove(subTaskId);
        }
        historyManager.remove(id);
    }

    @Override
    public void removeSubTaskById(int id) {
        EpicTask masterTask = epicTasks.get(subTasks.remove(id).getMasterTaskId());
        masterTask.getSubTasksIdList().remove(Integer.valueOf(id));
        checkEpicTaskStatus(masterTask);
        historyManager.remove(id);
    }

    @Override
    public List<SubTask> getEpicSubTasks(int id) {
        List<SubTask> epicSubTasks = new ArrayList<>();
        for (Integer subTaskId : epicTasks.get(id).getSubTasksIdList()) {
            epicSubTasks.add(subTasks.get(subTaskId));
        }
        return epicSubTasks;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

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
}
