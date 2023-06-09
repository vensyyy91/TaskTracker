package service.serializer;

import com.google.gson.*;
import model.*;
import service.TaskManager;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskSerializer implements JsonSerializer<Task>, JsonDeserializer<Task> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final TaskManager manager;

    public TaskSerializer(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public JsonElement serialize(Task task, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", task.getId());
        jsonObject.addProperty("name", task.getName());
        jsonObject.addProperty("status", task.getStatus().name());
        jsonObject.addProperty("type", task.getType().name());
        jsonObject.addProperty("description", task.getDescription());
        jsonObject.addProperty("startTime", task.getStartTime().format(formatter));
        jsonObject.addProperty("duration", task.getDuration().toMinutes());
        jsonObject.addProperty("endTime", task.getEndTime().format(formatter));

        return jsonObject;
    }

    @Override
    public Task deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();
        Task task = new Task(name, description);
        if (jsonObject.has("type")) {
            String taskType = jsonObject.get("type").getAsString();
            switch (taskType) {
                case "EPIC":
                    task = new EpicTask(name, description);
                    break;
                case "SUBTASK":
                    int masterTaskId = jsonObject.get("masterTaskId").getAsInt();
                    EpicTask masterTask = manager.getEpicTaskList().stream()
                            .filter(epic -> epic.getId() == masterTaskId)
                            .findFirst()
                            .orElseThrow(IllegalArgumentException::new);
                    task = new SubTask(name, description, masterTask);
                    break;
            }
            task.setType(TaskType.valueOf(taskType));
        }
        if (jsonObject.has("id")) {
            task.setId(jsonObject.get("id").getAsInt());
        }
        if (jsonObject.has("status")) {
            task.setStatus(TaskStatus.valueOf(jsonObject.get("status").getAsString()));
        }
        if (jsonObject.has("startTime")) {
            task.setStartTime(LocalDateTime.parse(jsonObject.get("startTime").getAsString(), formatter));
        }
        if (jsonObject.has("duration")) {
            task.setDuration(Duration.ofMinutes(jsonObject.get("duration").getAsLong()));
        }

        return task;
    }
}
