package service.serializer;

import com.google.gson.*;
import model.EpicTask;
import model.SubTask;
import model.TaskStatus;
import model.TaskType;
import service.TaskManager;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SubTaskSerializer implements JsonSerializer<SubTask>, JsonDeserializer<SubTask> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final TaskManager manager;

    public SubTaskSerializer(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public JsonElement serialize(SubTask subTask, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", subTask.getId());
        jsonObject.addProperty("name", subTask.getName());
        jsonObject.addProperty("status", subTask.getStatus().name());
        jsonObject.addProperty("type", subTask.getType().name());
        jsonObject.addProperty("description", subTask.getDescription());
        jsonObject.addProperty("startTime", subTask.getStartTime().format(formatter));
        jsonObject.addProperty("duration", subTask.getDuration().toMinutes());
        jsonObject.addProperty("endTime", subTask.getEndTime().format(formatter));
        jsonObject.addProperty("masterTaskId", subTask.getMasterTaskId());

        return jsonObject;
    }

    @Override
    public SubTask deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();
        int masterTaskId = jsonObject.get("masterTaskId").getAsInt();
        EpicTask masterTask = manager.getEpicTaskList().stream()
                .filter(epic -> epic.getId() == masterTaskId)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        SubTask subTask = new SubTask(name, description, masterTask);
        if (jsonObject.has("id")) {
            subTask.setId(jsonObject.get("id").getAsInt());
        }
        if (jsonObject.has("status")) {
            subTask.setStatus(TaskStatus.valueOf(jsonObject.get("status").getAsString()));
        }
        if (jsonObject.has("type")) {
            subTask.setType(TaskType.valueOf(jsonObject.get("type").getAsString()));
        }
        if (jsonObject.has("startTime")) {
            subTask.setStartTime(LocalDateTime.parse(jsonObject.get("startTime").getAsString(), formatter));
        }
        if (jsonObject.has("duration")) {
            subTask.setDuration(Duration.ofMinutes(jsonObject.get("duration").getAsLong()));
        }
        if (jsonObject.has("masterTaskId")) {
            subTask.setMasterTaskId(jsonObject.get("masterTaskId").getAsInt());
        }
        return subTask;
    }
}
