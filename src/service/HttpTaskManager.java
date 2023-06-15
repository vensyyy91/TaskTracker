package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.EpicTask;
import model.SubTask;
import model.Task;
import service.serializer.EpicTaskSerializer;
import service.serializer.SubTaskSerializer;
import service.serializer.TaskSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** Класс для объекта-менеджера, в котором реализовано управление всеми задачами, хранит данные на сервере */
public class HttpTaskManager extends FileBackedTaskManager {
    /** Поле Клиент */
    private final KVTaskClient client;
    /** Поле-константа объект класса Gson для серилизации/десериализации */
    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Task.class, new TaskSerializer())
            .registerTypeAdapter(EpicTask.class, new EpicTaskSerializer())
            .registerTypeAdapter(SubTask.class, new SubTaskSerializer())
            .create();
    /** Поле-константа Ключ для получения списка всех задач с сервера */
    private final static String TASK_KEY = "task";
    /** Поле-константа Ключ для получения списка всех эпиков с сервера */
    private final static String EPIC_KEY = "epic";
    /** Поле-константа Ключ для получения списка всех подзадач с сервера */
    private final static String SUBTASK_KEY = "subtask";
    /** Поле-константа Ключ для получения списка истории с сервера */
    private final static String HISTORY_KEY = "history";

    public HttpTaskManager(String url, boolean isLoading) {
        super(url);
        client = new KVTaskClient(url);
        if (isLoading) {
            loadFromServer();
        }
    }

    public HttpTaskManager(String url) {
        this(url, false);
    }

    @Override
    protected void save() {
        client.put(TASK_KEY, gson.toJson(getTaskList()));
        client.put(EPIC_KEY, gson.toJson(getEpicTaskList()));
        client.put(SUBTASK_KEY, gson.toJson(getSubTaskList()));
        client.put(HISTORY_KEY, gson.toJson(getHistory().stream().map(Task::getId).collect(Collectors.toList())));
    }

    /**
     * Метод восстановления состояния менеджера с сервера через клиент
     */
    private void loadFromServer() {
        int lastId = 0;
        String taskJson = client.load(TASK_KEY);
        Type TaskType = new TypeToken<ArrayList<Task>>() {}.getType();
        if (!taskJson.isBlank()) {
            List<Task> taskList = gson.fromJson(taskJson, TaskType);
            for (Task task : taskList) {
                lastId = Math.max(lastId, task.getId());
                tasks.put(task.getId(), task);
                prioritizedTasks.add(task);
            }
        }
        String epicTaskJson = client.load(EPIC_KEY);
        Type EpicTaskType = new TypeToken<ArrayList<EpicTask>>() {}.getType();
        if (!epicTaskJson.isBlank()) {
            List<EpicTask> epicTaskList = gson.fromJson(epicTaskJson, EpicTaskType);
            for (EpicTask epicTask : epicTaskList) {
                lastId = Math.max(lastId, epicTask.getId());
                epicTasks.put(epicTask.getId(), epicTask);
            }
        }
        String subTaskJson = client.load(SUBTASK_KEY);
        Type SubTaskType = new TypeToken<ArrayList<SubTask>>() {}.getType();
        if (!subTaskJson.isBlank()) {
            List<SubTask> subTaskList = gson.fromJson(subTaskJson, SubTaskType);
            for (SubTask subTask : subTaskList) {
                lastId = Math.max(lastId, subTask.getId());
                subTasks.put(subTask.getId(), subTask);
                prioritizedTasks.add(subTask);
            }
        }
        id = lastId;
        String historyJson = client.load(HISTORY_KEY);
        if (!historyJson.isBlank()) {
            String history = historyJson.replace("[", "").replace("]", "");
            List<Integer> historyIdList = Parser.historyFromString(history);
            for (int id : historyIdList) {
                if (tasks.containsKey(id)) {
                    historyManager.add(tasks.get(id));
                } else if (epicTasks.containsKey(id)) {
                    historyManager.add(epicTasks.get(id));
                } else if (subTasks.containsKey(id)) {
                    historyManager.add(subTasks.get(id));
                }
            }
        }
    }
}