package service;

import service.exception.ManagerSaveException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Класс-клиент, который делегирует вызовы методов в HTTP-запросы */
public class KVTaskClient {
    /** Поле Клиент */
    private final HttpClient client;
    /** Поле Ссылка к серверу */
    private final String url;
    /** Токен, который выдается при регистрации на сервере */
    private String apiToken;

    public KVTaskClient(String url) {
        client = HttpClient.newHttpClient();
        this.url = url;
        URI uri = URI.create(url + "register");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.statusCode());
            apiToken = response.body();
        } catch (IOException | InterruptedException ex) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    /**
     * Метод, записывающий данные на сервер
     * @param key - ключ
     * @param json - данные
     */
    public void put(String key, String json) {
        URI uri = URI.create(url + "save/" + key + "?API_TOKEN=" + apiToken);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ManagerSaveException("Произошла ошибка при сохранении на сервер, код состояния: " +  response.statusCode());
            }
        } catch (IOException | InterruptedException ex) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    /**
     * Метод, загружающий данные с сервера по ключу
     * @param key - ключ
     * @return возвращает данные в формате json
     */
    public String load(String key) {
        String value = "";
        URI uri = URI.create(url + "load/" + key + "?API_TOKEN=" + apiToken);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            value = response.body();
        } catch (IOException | InterruptedException ex) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        return value;
    }
}
