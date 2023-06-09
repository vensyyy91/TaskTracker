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

/** Класс для объекта-менеджера, в котором реализовано управление всеми задачами, хранит данные на сервере */
public class HttpTaskManager extends FileBackedTaskManager {
    /** Поле Клиент */
    private final KVTaskClient client;
    /** Поле объект Класса Gson для серилизации/десериализации */
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Task.class, new TaskSerializer(this))
            .registerTypeAdapter(EpicTask.class, new EpicTaskSerializer())
            .registerTypeAdapter(SubTask.class, new SubTaskSerializer(this))
            .create();

    public HttpTaskManager(String url) {
        super(url);
        client = new KVTaskClient(url);
        loadFromServer(client);
    }

    @Override
    protected void save() {
        client.put("task", gson.toJson(getTaskList()));
        client.put("epic", gson.toJson(getEpicTaskList()));
        client.put("subtask", gson.toJson(getSubTaskList()));
        client.put("history", gson.toJson(getHistory()));
    }

    /**
     * Метод восстановления состояния менеджера с сервера через клиент
     * @param client - клиент
     */
    private void loadFromServer(KVTaskClient client) {
        int lastId = 0;
        String taskJson = client.load("task");
        Type TaskType = new TypeToken<ArrayList<Task>>() {}.getType();
        if (!taskJson.isBlank()) {
            List<Task> taskList = gson.fromJson(taskJson, TaskType);
            taskList.forEach(task -> tasks.put(task.getId(), task));
            for (Integer id : tasks.keySet()) {
                if (id > lastId) {
                    lastId = id;
                }
            }
            prioritizedTasks.addAll(taskList);
        }
        String epicTaskJson = client.load("epic");
        Type EpicTaskType = new TypeToken<ArrayList<EpicTask>>() {}.getType();
        if (!epicTaskJson.isBlank()) {
            List<EpicTask> epicTaskList = gson.fromJson(epicTaskJson, EpicTaskType);
            epicTaskList.forEach(epicTask -> epicTasks.put(epicTask.getId(), epicTask));
            for (Integer id : epicTasks.keySet()) {
                if (id > lastId) {
                    lastId = id;
                }
            }
        }
        String subTaskJson = client.load("subtask");
        Type SubTaskType = new TypeToken<ArrayList<SubTask>>() {}.getType();
        if (!subTaskJson.isBlank()) {
            List<SubTask> subTaskList = gson.fromJson(subTaskJson, SubTaskType);
            subTaskList.forEach(subTask -> subTasks.put(subTask.getId(), subTask));
            for (Integer id : subTasks.keySet()) {
                if (id > lastId) {
                    lastId = id;
                }
            }
            prioritizedTasks.addAll(subTaskList);
        }
        id = lastId;
        String historyJson = client.load("history");
        if (!historyJson.isBlank()) {
            List<Task> history = gson.fromJson(historyJson, TaskType);
            history.forEach(historyManager::add);
        }
    }
}