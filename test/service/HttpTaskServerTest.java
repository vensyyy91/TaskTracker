package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import model.EpicTask;
import model.SubTask;
import model.Task;
import model.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.serializer.EpicTaskSerializer;
import service.serializer.SubTaskSerializer;
import service.serializer.TaskSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HttpTaskServerTest {
    private HttpTaskServer server;
    private KVServer kvserver;
    private HttpClient client;
    private static final Type TaskType = new TypeToken<List<Task>>() {}.getType();
    private static final Type EpicTaskType = new TypeToken<List<EpicTask>>() {}.getType();
    private static final Type SubTaskType = new TypeToken<List<SubTask>>() {}.getType();
    private TaskManager manager;
    private Task task1;
    private Task task2;
    private EpicTask epicTask1;
    private EpicTask epicTask2;
    private SubTask subTask1;
    private SubTask subTask2;
    private Gson gson;

    @BeforeEach
    public void beforeEach() throws IOException {
        kvserver = new KVServer();
        kvserver.start();
        manager = Managers.getDefault();
        task1 = new Task("TestTask1", "First task for test", "25.06.2023 21:00", 30);
        manager.createTask(task1);
        task2 = new Task("TestTask2", "Second task for test", "26.06.2023 21:30", 60);
        manager.createTask(task2);
        epicTask1 = new EpicTask("TestEpicTask1", "First epicTask for test");
        manager.createEpicTask(epicTask1);
        epicTask2 = new EpicTask("TestEpicTask2", "Second epicTask for test");
        manager.createEpicTask(epicTask2);
        subTask1 = new SubTask("TestSubTask1", "First subTask for test", "25.06.2023 23:00", 30, epicTask1);
        subTask1.setStatus(TaskStatus.DONE);
        manager.createSubTask(subTask1);
        subTask2 = new SubTask("TestSubTask2", "Second subTask for test", "26.06.2023 21:00", 30, epicTask1);
        subTask2.setStatus(TaskStatus.DONE);
        manager.createSubTask(subTask2);
        gson = new GsonBuilder()
                .registerTypeAdapter(Task.class, new TaskSerializer(manager))
                .registerTypeAdapter(EpicTask.class, new EpicTaskSerializer())
                .registerTypeAdapter(SubTask.class, new SubTaskSerializer(manager))
                .create();
        server = new HttpTaskServer(manager);
        server.start();
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    public void afterEach() {
        server.stop();
        kvserver.stop();
    }

    @Test
    void getTaskList() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> taskList = gson.fromJson(response.body(), TaskType);

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertNotNull(taskList, "Список задач не возвращается.");
        assertIterableEquals(manager.getTaskList(), taskList, "Списки задач не совпадают.");
    }

    @Test
    void getEpicTaskList() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> epicTaskList = gson.fromJson(response.body(), EpicTaskType);

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertNotNull(epicTaskList, "Список эпиков не возвращается.");
        assertIterableEquals(manager.getEpicTaskList(), epicTaskList, "Списки эпиков не совпадают.");
    }

    @Test
    void getSubTaskList() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> subTaskList = gson.fromJson(response.body(), SubTaskType);

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertNotNull(subTaskList, "Список подзадач не возвращается.");
        assertIterableEquals(manager.getSubTaskList(), subTaskList, "Списки подзадач не совпадают.");
    }

    @Test
    void removeTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertTrue(manager.getTaskList().isEmpty(), "Список задач не пуст.");
    }

    @Test
    void removeEpicTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertTrue(manager.getEpicTaskList().isEmpty(), "Список эпиков не пуст.");
        assertTrue(manager.getSubTaskList().isEmpty(), "Список подзадач не пуст.");
    }

    @Test
    void removeSubTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertTrue(manager.getSubTaskList().isEmpty(), "Список подзадач не пуст.");
        assertEquals(TaskStatus.NEW, manager.getEpicTaskById(3).getStatus(), "Статус эпика не меняется.");
        assertEquals(LocalDateTime.of(2099, 12, 31, 0, 0), manager.getEpicTaskById(3).getStartTime(),
                "Не меняется время старта эпика.");
        assertEquals(Duration.ofMinutes(0), manager.getEpicTaskById(3).getDuration(),
                "Не меняется продолжительность эпика.");
    }

    @Test
    void getTaskById() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Task task = gson.fromJson(response.body(), Task.class);

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertNotNull(task, "Задача не возвращается.");
        assertEquals(task1, task, "Задачи не совпадают.");
    }

    @Test
    void getTaskByNonExistingId() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=8");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Несуществующий id задачи, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void getEpicTaskById() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        EpicTask epicTask = gson.fromJson(response.body(), EpicTask.class);

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertNotNull(epicTask, "Эпик не возвращается.");
        assertEquals(epicTask1, epicTask, "Эпики не совпадают.");
    }

    @Test
    void getEpicTaskByNonExistingId() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=8");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Несуществующий id эпика, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void getSubTaskById() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=5");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        SubTask subTask = gson.fromJson(response.body(), SubTask.class);

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertNotNull(subTask, "Подзадача не возвращается.");
        assertEquals(subTask1, subTask, "Подзадачи не совпадают.");
    }

    @Test
    void getSubTaskByNonExistingId() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=8");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Несуществующий id подзадачи, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void createTask() throws IOException, InterruptedException {
        Task newTask = new Task("NewTestTask", "New task for test",
                "25.06.2023 20:30", 30);
        String taskJson = gson.toJson(newTask);
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не совпадает.");
        assertEquals(3, manager.getTaskList().size(), "Список задач не увеличился.");
        assertEquals(7, manager.getTaskList().get(2).getId(), "Задаче присвоен некорректный id.");
    }

    @Test
    void createEpicTask() throws IOException, InterruptedException {
        EpicTask newEpicTask = new EpicTask("NewTestEpicTask", "New EpicTask for test");
        String epicTaskJson = gson.toJson(newEpicTask);
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicTaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не совпадает.");
        assertEquals(3, manager.getEpicTaskList().size(), "Список эпиков не увеличился.");
        assertEquals(7, manager.getEpicTaskList().get(2).getId(), "Эпику присвоен некорректный id.");
    }

    @Test
    void createSubTask() throws IOException, InterruptedException {
        SubTask newSubTask = new SubTask("NewTestSubTask", "New SubTask for test",
                "25.06.2023 22:00", 60, epicTask1);
        String subTaskJson = gson.toJson(newSubTask);
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "Код ответа не совпадает.");
        assertEquals(3, manager.getSubTaskList().size(), "Список подзадач не увеличился.");
        assertEquals(7, manager.getSubTaskList().get(2).getId(), "Подзадаче присвоен некорректный id.");
        assertEquals(TaskStatus.IN_PROGRESS, manager.getEpicTaskById(3).getStatus(),
                "Не меняется статус эпика.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 22, 0), manager.getEpicTaskById(3).getStartTime(),
                "Не меняется время старта эпика.");
        assertEquals(Duration.ofMinutes(1410), manager.getEpicTaskById(3).getDuration(),
                "Не меняется длительность эпика.");
    }

    @Test
    void updateTask() throws IOException, InterruptedException {
        Task updatedTask = new Task("UpdatedTask", "New updated Task for test",
                "25.06.2023 21:30", 15);
        updatedTask.setId(1);
        updatedTask.setStatus(TaskStatus.IN_PROGRESS);
        String taskJson = gson.toJson(updatedTask);
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertEquals(2, manager.getTaskList().size(), "Количество задач увеличилось.");
        assertEquals(TaskStatus.IN_PROGRESS, manager.getTaskById(1).getStatus(), "Статус задачи не обновляется.");
        assertEquals("UpdatedTask", manager.getTaskById(1).getName(),
                "Не обновляется название задачи.");
        assertEquals("New updated Task for test", manager.getTaskById(1).getDescription(),
                "Не обновляется описание задачи.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 21, 30), manager.getTaskById(1).getStartTime(),
                "Не обновляется время старта задачи.");
        assertEquals(Duration.ofMinutes(15), manager.getTaskById(1).getDuration(),
                "Не обновляется длительность задачи.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 21, 45), manager.getTaskById(1).getEndTime(),
                "Не обновляется время окончания задачи.");
    }

    @Test
    void updateEpicTask() throws IOException, InterruptedException {
        EpicTask updatedEpicTask = new EpicTask("UpdatedEpicTask", "New updated EpicTask for test");
        updatedEpicTask.setId(3);
        String epicTaskJson = gson.toJson(updatedEpicTask);
        URI url = URI.create("http://localhost:8080/tasks/epic/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicTaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertEquals(2, manager.getEpicTaskList().size(), "Количество эпиков увеличилось.");
        assertEquals("UpdatedEpicTask", manager.getEpicTaskById(3).getName(),
                "Не обновляется название эпика.");
        assertEquals("New updated EpicTask for test", manager.getEpicTaskById(3).getDescription(),
                "Не обновляется описание эпика.");
    }

    @Test
    void updateSubTask() throws IOException, InterruptedException {
        SubTask updatedSubTask = new SubTask("UpdatedSubTask", "New updated SubTask for test",
                "25.06.2023 23:30", 15, epicTask1);
        updatedSubTask.setId(5);
        updatedSubTask.setStatus(TaskStatus.IN_PROGRESS);
        String subTaskJson = gson.toJson(updatedSubTask);
        URI url = URI.create("http://localhost:8080/tasks/subtask/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subTaskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertEquals(2, manager.getSubTaskList().size(), "Количество подзадач увеличилось.");
        assertEquals(TaskStatus.IN_PROGRESS, manager.getSubTaskById(5).getStatus(), "Статус подзадачи не обновляется.");
        assertEquals("UpdatedSubTask", manager.getSubTaskById(5).getName(),
                "Не обновляется название подзадачи.");
        assertEquals("New updated SubTask for test", manager.getSubTaskById(5).getDescription(),
                "Не обновляется описание подзадачи.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 23, 30), manager.getSubTaskById(5).getStartTime(),
                "Не обновляется время старта подзадачи.");
        assertEquals(Duration.ofMinutes(15), manager.getSubTaskById(5).getDuration(),
                "Не обновляется длительность подзадачи.");
        assertEquals(LocalDateTime.of(2023, 6, 25, 23, 45), manager.getSubTaskById(5).getEndTime(),
                "Не обновляется время окончания подзадачи.");
    }

    @Test
    void removeTaskById() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=1");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertEquals(1, manager.getTaskList().size(), "Количество задач не изменилось.");
        assertFalse(manager.getTaskList().contains(task1), "Задача не удалена.");
        assertThrows(IllegalArgumentException.class, () -> manager.getTaskById(1));
    }

    @Test
    void removeTaskByNonExistingId() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/?id=8");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Несуществующий id задачи, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void removeEpicTaskById() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertEquals(1, manager.getEpicTaskList().size(), "Количество эпиков не изменилось.");
        assertFalse(manager.getEpicTaskList().contains(epicTask1), "Эпик не удален.");
        assertThrows(IllegalArgumentException.class, () -> manager.getEpicTaskById(3));
        assertTrue(manager.getSubTaskList().isEmpty(), "Подзадачи не удаляются.");
    }

    @Test
    void removeEpicTaskByNonExistingId() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/epic/?id=8");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Несуществующий id эпика, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void removeSubTaskById() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=5");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertEquals(1, manager.getSubTaskList().size(), "Количество подзадач не изменилось.");
        assertFalse(manager.getSubTaskList().contains(subTask1), "Подзадача не удалена.");
        assertThrows(IllegalArgumentException.class, () -> manager.getSubTaskById(5));
        assertFalse(manager.getEpicSubTasks(3).contains(subTask1), "Подзадача не удаляется у эпика.");
    }

    @Test
    void removeSubTaskByNonExistingId() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/?id=8");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Несуществующий id подзадачи, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void getEpicSubtasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=3");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> subTaskList = gson.fromJson(response.body(), SubTaskType);

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertNotNull(subTaskList, "Список подзадач не возвращается.");
        assertIterableEquals(manager.getEpicSubTasks(3), subTaskList, "Списки подзадач не совпадают.");
    }

    @Test
    void getEpicSubtasksWithNonExistingId () throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/epic/?id=8");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Несуществующий id эпика, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void getHistory() throws IOException, InterruptedException {
        manager.getTaskById(1);
        manager.getEpicTaskById(3);
        manager.getSubTaskById(6);
        manager.getEpicTaskById(4);
        manager.getTaskById(2);
        manager.getTaskById(1);

        URI url = URI.create("http://localhost:8080/tasks/history/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> history = gson.fromJson(response.body(), TaskType);

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertNotNull(history, "История просмотров не возвращается.");
        assertEquals(Arrays.asList(epicTask1, subTask2, epicTask2, task2, task1), history,
                "История задач не совпадает.");
    }

    @Test
    void getPrioritizedTasks() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        List<Task> prioritizedTasks = gson.fromJson(response.body(), TaskType);

        assertEquals(200, response.statusCode(), "Код ответа не совпадает.");
        assertNotNull(prioritizedTasks, "Список задач не возвращается.");
        assertEquals(Arrays.asList(task1, subTask1, subTask2, task2), prioritizedTasks,
                "Списки задач не совпадают.");
    }

    @Test
    void requestWithWrongMethod() throws IOException, InterruptedException {
        Task newTask = new Task("NewTestTask", "New task for test",
                "25.06.2023 20:30", 30);
        String taskJson = gson.toJson(newTask);
        URI url = URI.create("http://localhost:8080/tasks/task/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .PUT(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Ожидается GET, POST или DELETE запрос, получили: PUT", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void requestGETWithWrongURI() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/supertask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Произошла ошибка, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void requestGETWithAnotherWrongURI() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/task/gettask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Произошла ошибка, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void requestPOSTWithWrongURI() throws IOException, InterruptedException {
        Task newTask = new Task("NewTestTask", "New task for test",
                "25.06.2023 20:30", 30);
        String taskJson = gson.toJson(newTask);
        URI url = URI.create("http://localhost:8080/tasks/supertask/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Произошла ошибка, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void requestPOSTWithAnotherWrongURI() throws IOException, InterruptedException {
        Task newTask = new Task("NewTestTask", "New task for test",
                "25.06.2023 20:30", 30);
        String taskJson = gson.toJson(newTask);
        URI url = URI.create("http://localhost:8080/tasks/");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Произошла ошибка, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void requestDELETEWithWrongURI() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/supertask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Произошла ошибка, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }

    @Test
    void requestDELETEWithAnotherWrongURI() throws IOException, InterruptedException {
        URI url = URI.create("http://localhost:8080/tasks/subtask/removesubtask/");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Код ответа не совпадает.");
        assertEquals("Произошла ошибка, проверьте корректность ссылки.", response.body(),
                "Сообщение не совпадает.");
    }
}