package service;

import model.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/** Класс для объекта-менеджера, в котором реализовано управление всеми задачами, хранит данные в оперативной памяти */
public class InMemoryTaskManager implements TaskManager {
    /** Поле Задачи */
    protected final Map<Integer, Task> tasks = new HashMap<>();
    /** Поле Эпики */
    protected final Map<Integer, EpicTask> epicTasks = new HashMap<>();
    /** Поле Подзадачи */
    protected final Map<Integer, SubTask> subTasks = new HashMap<>();
    /** Поле Идентификатор */
    protected int id;
    /** Поле История просмотров */
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    /** Поле Список задач, отсортированных по времени старта */
    protected final Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));
    /** Поле Таблица, где ключами являются 15-минутные промежутки, а значениями объект boolean (свободно ли время) */
    protected final Map<LocalDateTime, Boolean> timeMap = createTimeMap();
    /** Константа, хранящая количество 15-минутных промежутков в году */
    private static final int INTERVALS_15MIN_IN_YEAR = 365 * 24 * 4;


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
        tasks.keySet().forEach(historyManager::remove);
        tasks.values().forEach(prioritizedTasks::remove);
        tasks.values().forEach(this::clearTimeMapForTask);
        tasks.clear();
    }

    @Override
    public void removeEpicTasks() {
        epicTasks.keySet().forEach(historyManager::remove);
        epicTasks.clear();
        subTasks.keySet().forEach(historyManager::remove);
        subTasks.clear();
    }

    @Override
    public void removeSubTasks() {
        subTasks.keySet().forEach(historyManager::remove);
        subTasks.values().forEach(prioritizedTasks::remove);
        subTasks.values().forEach(this::clearTimeMapForTask);
        subTasks.clear();
        for (EpicTask epicTask : epicTasks.values()) {
            epicTask.getSubTasksIdList().clear();
            checkEpicTaskStatusAndTime(epicTask);
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
        checkTimeIntersection(task);
        task.setId(getNewId());
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
    }

    @Override
    public void createEpicTask(EpicTask epicTask) {
        epicTask.setId(getNewId());
        epicTasks.put(epicTask.getId(), epicTask);
    }

    @Override
    public void createSubTask(SubTask subTask) {
        checkTimeIntersection(subTask);
        subTask.setId(getNewId());
        subTasks.put(subTask.getId(), subTask);
        prioritizedTasks.add(subTask);
        EpicTask masterTask = epicTasks.get(subTask.getMasterTaskId());
        masterTask.getSubTasksIdList().add(subTask.getId());
        checkEpicTaskStatusAndTime(masterTask);
    }

    @Override
    public void updateTask(Task task) {
        int id = task.getId();
        if (tasks.containsKey(id)) {
            clearTimeMapForTask(tasks.get(id));
            prioritizedTasks.remove(tasks.get(id));
        }
        checkTimeIntersection(task);
        prioritizedTasks.add(task);
        tasks.put(task.getId(), task);
    }

    @Override
    public void updateEpicTask(EpicTask epicTask) {
        epicTasks.put(epicTask.getId(), epicTask);
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        int id = subTask.getId();
        if (subTasks.containsKey(id)) {
            clearTimeMapForTask(subTasks.get(id));
            prioritizedTasks.remove(subTasks.get(id));
        }
        checkTimeIntersection(subTask);
        prioritizedTasks.add(subTask);
        subTasks.put(id, subTask);
        EpicTask masterTask = epicTasks.get(subTask.getMasterTaskId());
        checkEpicTaskStatusAndTime(masterTask);
    }

    @Override
    public void removeTaskById(int id) {
        if (tasks.containsKey(id)) {
            prioritizedTasks.remove(tasks.get(id));
            tasks.remove(id);
            historyManager.remove(id);
        }
    }

    @Override
    public void removeEpicTaskById(int id) {
        if (epicTasks.containsKey(id)) {
            for (int subTaskId : epicTasks.remove(id).getSubTasksIdList()) {
                prioritizedTasks.remove(subTasks.get(subTaskId));
                subTasks.remove(subTaskId);
                historyManager.remove(subTaskId);
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void removeSubTaskById(int id) {
        if (subTasks.containsKey(id)) {
            prioritizedTasks.remove(subTasks.get(id));
            EpicTask masterTask = epicTasks.get(subTasks.remove(id).getMasterTaskId());
            masterTask.getSubTasksIdList().remove(Integer.valueOf(id));
            checkEpicTaskStatusAndTime(masterTask);
            historyManager.remove(id);
        }
    }

    @Override
    public List<SubTask> getEpicSubTasks(int id) {
        return epicTasks.get(id).getSubTasksIdList().stream().map(subTasks::get).collect(Collectors.toList());
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    /**
     * Метод получения уникального идентификатора
     * @return возвращает уникальный идентификатор
     */
    private int getNewId() {
        return ++id;
    }

    /**
     * Метод обновления статуса эпика
     * @param epicTask - эпик (объект класса EpicTask)
     */
    private void checkEpicTaskStatus(EpicTask epicTask) {
        List<Integer> subTaskList = epicTask.getSubTasksIdList();
        boolean isDone = !subTaskList.isEmpty() && subTaskList.stream()
                .map(id -> subTasks.get(id).getStatus())
                .allMatch(status -> status.equals(TaskStatus.DONE));
        boolean isInProgress = !subTaskList.isEmpty() && !isDone && subTaskList.stream()
                .map(id -> subTasks.get(id).getStatus())
                .anyMatch(status -> status.equals(TaskStatus.DONE) || status.equals(TaskStatus.IN_PROGRESS));
        if (isDone) {
            epicTask.setStatus(TaskStatus.DONE);
        } else if (isInProgress) {
            epicTask.setStatus(TaskStatus.IN_PROGRESS);
        } else {
            epicTask.setStatus(TaskStatus.NEW);
        }
    }

    /**
     * Метод обновления временных полей эпика (время старта, продолжительность, время завершения)
     * @param epicTask - эпик (объект класса EpicTask)
     */
    private void checkEpicTaskTime(EpicTask epicTask) {
        List<Integer> subTaskList = epicTask.getSubTasksIdList();
        if (!subTaskList.isEmpty()) {
            epicTask.setStartTime(subTaskList.stream()
                    .map(id -> subTasks.get(id).getStartTime())
                    .min(LocalDateTime::compareTo)
                    .orElse(epicTask.getStartTime()));
            epicTask.setEndTime(subTaskList.stream()
                    .map(id -> subTasks.get(id).getEndTime())
                    .max(LocalDateTime::compareTo)
                    .orElse(epicTask.getEndTime()));
            epicTask.setDuration(Duration.between(epicTask.getStartTime(), epicTask.getEndTime()));
        } else {
            epicTask.setStartTime(LocalDateTime.of(2099, 12, 31, 0, 0));
            epicTask.setDuration(Duration.ofMinutes(0));
            epicTask.setEndTime(LocalDateTime.of(2099, 12, 31, 0, 0));
        }
    }

    /**
     * Метод, обновляющий статус и время эпика
     * @param epicTask - эпик (объект класса EpicTask)
     */
    private void checkEpicTaskStatusAndTime(EpicTask epicTask) {
        checkEpicTaskStatus(epicTask);
        checkEpicTaskTime(epicTask);
    }

    /**
     * Метод создания мапы, где ключами являются 15-минутные промежутки, а значениями объект boolean (свободно ли время)
     * @return возвращает мапу со всеми значениями false
     */
    private Map<LocalDateTime, Boolean> createTimeMap() {
        Map<LocalDateTime, Boolean> timeMap = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime roundedTime = now.truncatedTo(ChronoUnit.HOURS).plusMinutes(15 * (now.getMinute() / 15));
        for (int i = 0; i < INTERVALS_15MIN_IN_YEAR; i++) {
            timeMap.put(roundedTime.plus(Duration.ofMinutes(15 * i)), false);
        }
        return timeMap;
    }

    /**
     * Метод проверки пересечения задачи по времени с уже существующими задачами
     * @param task - любая задача (объект класса Task, EpicTask или SubTask)
     */
    private void checkTimeIntersection(Task task) {
        Map<LocalDateTime, Boolean> currentTimeMap = new HashMap<>(timeMap);
        LocalDateTime time = task.getStartTime();
        if (time.equals(LocalDateTime.of(2099, 12, 31, 0, 0))) {
            return;
        }
        while (time.isBefore(task.getEndTime())) {
            if (timeMap.get(time)) {
                timeMap.putAll(currentTimeMap);
                throw new TimeValidationException("Задача '" + task.getName() + "' пересекается по времени с другими задачами.");
            } else {
                timeMap.put(time, true);
            }
            time = time.plus(Duration.ofMinutes(15));
        }
    }

    /**
     * Метод, который "очищает" все временные промежуткив в timeMap для конкретной задачи
     * @param task - любая задача (объект класса Task, EpicTask или SubTask)
     */
    private void clearTimeMapForTask(Task task) {
        LocalDateTime time = task.getStartTime();
        while (time.isBefore(task.getEndTime())) {
            timeMap.put(time, false);
            time = time.plus(Duration.ofMinutes(15));
        }
    }
}
