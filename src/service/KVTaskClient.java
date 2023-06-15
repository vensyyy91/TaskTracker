package service;

import service.exception.*;

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
    /** Поле Токен, который выдается при регистрации на сервере */
    private final String apiToken;

    public KVTaskClient(String url) {
        client = HttpClient.newHttpClient();
        this.url = url;
        this.apiToken = register();
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
            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            if (response.statusCode() != 200) {
                throw new ClientSaveException("Произошла ошибка при сохранении на сервер, код ответа: " +  response.statusCode());
            }
        } catch (IOException | InterruptedException ex) {
            throw new ClientSaveException("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    /**
     * Метод, загружающий данные с сервера по ключу
     * @param key - ключ
     * @return возвращает данные в формате json
     */
    public String load(String key) {
        URI uri = URI.create(url + "load/" + key + "?API_TOKEN=" + apiToken);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new ClientLoadException("Произошла ошибка при загрузке с сервера, код ответа: " +  response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException ex) {
            throw new ClientLoadException("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }

    /**
     * Метод регистрации на сервере
     * @return возращает уникальный токен
     */
    private String register() {
        URI uri = URI.create(url + "register");
        HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new RegistrationException("Произошла ошибка при регистрации, код состояния: " + response.statusCode());
            }
            return response.body();
        } catch (IOException | InterruptedException ex) {
            throw new RegistrationException("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
    }
}
