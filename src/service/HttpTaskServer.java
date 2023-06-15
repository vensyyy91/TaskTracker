package service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import model.EpicTask;
import model.SubTask;
import model.Task;
import service.serializer.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/** Класс сервера, в котором реализован маппинг клиентских запросов на методы TaskManager */
public class HttpTaskServer {
    /** Поле-константа Порт, который будет слушать сервер */
    private static final int PORT = 8080;
    /** Поле-константа Кодировка, используемая при чтении запросов
     */
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    /** Поле Менеджер задач, используемый сервером */
    private final TaskManager manager;
    /** Поле Сервер */
    private final HttpServer server;
    /** Поле-константа объект класса Gson для серилизации/десериализации */
    private final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Task.class, new TaskSerializer())
            .registerTypeAdapter(EpicTask.class, new EpicTaskSerializer())
            .registerTypeAdapter(SubTask.class, new SubTaskSerializer())
            .create();

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/tasks", this::handleTasks);
    }

    /** Метод, запускающий сервер */
    public void start() {
        server.start();
        System.out.println("Сервер запущен, порт:" + PORT);
    }

    /** Метод, останавливающий сервер */
    public void stop() {
        server.stop(0);
        System.out.println("Сервер остановлен, порт:" + PORT);
    }

    /**
     * Метод-обработчик, связывающий эндпоинты и методы менеджера-задач
     * @param exchange - объект класса HttpExchange
     * @throws IOException - если заголовки ответа уже были отправлены или произошла ошибка ввода-вывода
     */
    private void handleTasks(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        URI uri = exchange.getRequestURI();
        String path = uri.getPath();
        String query = uri.getQuery();
        String[] pathParts = path.split("/");
        String response;
        switch (method) {
            case "GET":
                if (pathParts.length == 2) {
                    response = gson.toJson(manager.getPrioritizedTasks());
                    exchange.sendResponseHeaders(200, 0);
                } else if (pathParts.length == 3) {
                    switch (pathParts[2]) {
                        case "task":
                            if (query != null) {
                                int id = Integer.parseInt(query.substring(3));
                                if (manager.getTaskList().stream().anyMatch(t -> t.getId() == id)) {
                                    response = gson.toJson(manager.getTaskById(id));
                                    exchange.sendResponseHeaders(200, 0);
                                } else {
                                    exchange.sendResponseHeaders(404, 0);
                                    response = "Несуществующий id задачи, проверьте корректность ссылки.";
                                }
                            } else {
                                response = gson.toJson(manager.getTaskList());
                                exchange.sendResponseHeaders(200, 0);
                            }
                            break;
                        case "epic":
                            if (query != null) {
                                int id = Integer.parseInt(query.substring(3));
                                if (manager.getEpicTaskList().stream().anyMatch(e -> e.getId() == id)) {
                                    response = gson.toJson(manager.getEpicTaskById(id));
                                    exchange.sendResponseHeaders(200, 0);
                                } else {
                                    exchange.sendResponseHeaders(404, 0);
                                    response = "Несуществующий id эпика, проверьте корректность ссылки.";
                                }
                            } else {
                                response = gson.toJson(manager.getEpicTaskList());
                                exchange.sendResponseHeaders(200, 0);
                            }
                            break;
                        case "subtask":
                            if (query != null) {
                                int id = Integer.parseInt(query.substring(3));
                                if (manager.getSubTaskList().stream().anyMatch(s -> s.getId() == id)) {
                                    response = gson.toJson(manager.getSubTaskById(id));
                                    exchange.sendResponseHeaders(200, 0);
                                } else {
                                    exchange.sendResponseHeaders(404, 0);
                                    response = "Несуществующий id подзадачи, проверьте корректность ссылки.";
                                }
                            } else {
                                response = gson.toJson(manager.getSubTaskList());
                                exchange.sendResponseHeaders(200, 0);
                            }
                            break;
                        case "history":
                            response = gson.toJson(manager.getHistory());
                            exchange.sendResponseHeaders(200, 0);
                            break;
                        default:
                            exchange.sendResponseHeaders(400, 0);
                            response = "Произошла ошибка, проверьте корректность ссылки.";
                    }
                } else if (path.equals("/tasks/subtask/epic/") && query != null){
                    int epicTaskId = Integer.parseInt(query.substring(3));
                    if (manager.getEpicTaskList().stream().anyMatch(e -> e.getId() == epicTaskId)) {
                        response = gson.toJson(manager.getEpicSubTasks(epicTaskId));
                        exchange.sendResponseHeaders(200, 0);
                    } else {
                        exchange.sendResponseHeaders(404, 0);
                        response = "Несуществующий id эпика, проверьте корректность ссылки.";
                    }
                } else {
                    exchange.sendResponseHeaders(400, 0);
                    response = "Произошла ошибка, проверьте корректность ссылки.";
                }
                break;
            case "POST":
                String body = readBody(exchange);
                if (body.isEmpty()) {
                    exchange.sendResponseHeaders(400, 0);
                    response = "Произошла ошибка, проверьте корректность ссылки.";
                    break;
                }
                boolean isExisting;
                if (pathParts.length == 3) {
                    switch (pathParts[2]) {
                        case "task":
                            Task task = gson.fromJson(body, Task.class);
                            isExisting = manager.getTaskList().stream().anyMatch(t -> t.getId() == task.getId());
                            if (isExisting) {
                                manager.updateTask(task);
                                response = "Задача успешно обновлена!";
                                exchange.sendResponseHeaders(200, 0);
                            } else {
                                manager.createTask(task);
                                response = "Задача успешно создана!";
                                exchange.sendResponseHeaders(201, 0);
                            }
                            break;
                        case "epic":
                            EpicTask epicTask = gson.fromJson(body, EpicTask.class);
                            isExisting = manager.getEpicTaskList().stream().anyMatch(e -> e.getId() == epicTask.getId());
                            if (isExisting) {
                                manager.updateEpicTask(epicTask);
                                response = "Эпик успешно обновлен!";
                                exchange.sendResponseHeaders(200, 0);
                            } else {
                                manager.createEpicTask(epicTask);
                                response = "Эпик успешно создан!";
                                exchange.sendResponseHeaders(201, 0);
                            }
                            break;
                        case "subtask":
                            SubTask subTask = gson.fromJson(body, SubTask.class);
                            isExisting = manager.getSubTaskList().stream().anyMatch(s -> s.getId() == subTask.getId());
                            if (isExisting) {
                                manager.updateSubTask(subTask);
                                response = "Подзадача успешно обновлена!";
                                exchange.sendResponseHeaders(200, 0);
                            } else {
                                manager.createSubTask(subTask);
                                response = "Подзадача успешно создана!";
                                exchange.sendResponseHeaders(201, 0);
                            }
                            break;
                        default:
                            exchange.sendResponseHeaders(400, 0);
                            response = "Произошла ошибка, проверьте корректность ссылки.";
                    }
                } else {
                    exchange.sendResponseHeaders(400, 0);
                    response = "Произошла ошибка, проверьте корректность ссылки.";
                }
                break;
            case "DELETE":
                if (pathParts.length == 3) {
                    switch (pathParts[2]) {
                        case "task":
                            if (query != null) {
                                int id = Integer.parseInt(query.substring(3));
                                if (manager.getTaskList().stream().anyMatch(t -> t.getId() == id)) {
                                    manager.removeTaskById(id);
                                    response = "Задача с id=" + id + " успешно удалена.";
                                    exchange.sendResponseHeaders(200, 0);
                                } else {
                                    exchange.sendResponseHeaders(404, 0);
                                    response = "Несуществующий id задачи, проверьте корректность ссылки.";
                                }
                            } else {
                                manager.removeTasks();
                                response = "Все задачи успешно удалены.";
                                exchange.sendResponseHeaders(200, 0);
                            }
                            break;
                        case "epic":
                            if (query != null) {
                                int id = Integer.parseInt(query.substring(3));
                                if (manager.getEpicTaskList().stream().anyMatch(e -> e.getId() == id)) {
                                    manager.removeEpicTaskById(id);
                                    response = "Эпик с id=" + id + " успешно удален.";
                                    exchange.sendResponseHeaders(200, 0);
                                } else {
                                    exchange.sendResponseHeaders(404, 0);
                                    response = "Несуществующий id эпика, проверьте корректность ссылки.";
                                }
                            } else {
                                manager.removeEpicTasks();
                                response = "Все эпики успешно удалены.";
                                exchange.sendResponseHeaders(200, 0);
                            }
                            break;
                        case "subtask":
                            if (query != null) {
                                int id = Integer.parseInt(query.substring(3));
                                if (manager.getSubTaskList().stream().anyMatch(s -> s.getId() == id)) {
                                    manager.removeSubTaskById(id);
                                    response = "Подзадача с id=" + id + " успешно удалена.";
                                    exchange.sendResponseHeaders(200, 0);
                                } else {
                                    exchange.sendResponseHeaders(404, 0);
                                    response = "Несуществующий id подзадачи, проверьте корректность ссылки.";
                                }
                            } else {
                                manager.removeSubTasks();
                                response = "Все подзадачи успешно удалены.";
                                exchange.sendResponseHeaders(200, 0);
                            }
                            break;
                        default:
                            exchange.sendResponseHeaders(400, 0);
                            response = "Произошла ошибка, проверьте корректность ссылки.";
                    }
                } else {
                    exchange.sendResponseHeaders(400, 0);
                    response = "Произошла ошибка, проверьте корректность ссылки.";
                }
                break;
            default:
                exchange.sendResponseHeaders(405, 0);
                response = "Ожидается GET, POST или DELETE запрос, получили: " + exchange.getRequestMethod();
        }
        writeResponse(exchange, response);
    }

    /**
     * Метод чтения тела запроса
     * @param exchange - объект класса HttpExchange
     * @return возвращает тело запроса
     * @throws IOException - если произошла ошибка ввода-вывода
     */
    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), DEFAULT_CHARSET);
    }

    /**
     * Метод записи в тело ответа
     * @param exchange - объект класса HttpExchange
     * @param response - текст, который необходимо записать в тело ответа
     * @throws IOException - если произошла ошибка ввода-вывода
     */
    private void writeResponse(HttpExchange exchange, String response) throws IOException {
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}