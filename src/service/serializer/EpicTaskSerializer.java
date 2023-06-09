package service.serializer;

import com.google.gson.*;
import model.EpicTask;
import model.TaskStatus;
import model.TaskType;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class EpicTaskSerializer implements JsonSerializer<EpicTask>, JsonDeserializer<EpicTask> {
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Override
    public JsonElement serialize(EpicTask epicTask, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", epicTask.getId());
        jsonObject.addProperty("name", epicTask.getName());
        jsonObject.addProperty("status", epicTask.getStatus().name());
        jsonObject.addProperty("type", epicTask.getType().name());
        jsonObject.addProperty("description", epicTask.getDescription());
        jsonObject.addProperty("startTime", epicTask.getStartTime().format(formatter));
        jsonObject.addProperty("duration", epicTask.getDuration().toMinutes());
        jsonObject.addProperty("endTime", epicTask.getEndTime().format(formatter));
        jsonObject.addProperty("subTasksIdList", epicTask.getSubTasksIdList().toString());

        return jsonObject;
    }

    @Override
    public EpicTask deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String name = jsonObject.get("name").getAsString();
        String description = jsonObject.get("description").getAsString();
        EpicTask epicTask = new EpicTask(name, description);

        if (jsonObject.has("id")) {
            epicTask.setId(jsonObject.get("id").getAsInt());
        }
        if (jsonObject.has("status")) {
            epicTask.setStatus(TaskStatus.valueOf(jsonObject.get("status").getAsString()));
        }
        if (jsonObject.has("type")) {
            epicTask.setType(TaskType.valueOf(jsonObject.get("type").getAsString()));
        }
        if (jsonObject.has("startTime")) {
            epicTask.setStartTime(LocalDateTime.parse(jsonObject.get("startTime").getAsString(), formatter));
        }
        if (jsonObject.has("duration")) {
            epicTask.setDuration(Duration.ofMinutes(jsonObject.get("duration").getAsLong()));
        }
        if (jsonObject.has("subTasksIdList")) {
            String[] idString = jsonObject.get("subTasksIdList").getAsString()
                    .replace("[", "")
                    .replace("]", "")
                    .split(", ");
            if (!idString[0].equals("")) {
                Arrays.stream(idString).mapToInt(Integer::parseInt).forEach(id -> epicTask.getSubTasksIdList().add(id));
            }
        }
        return epicTask;
    }
}
