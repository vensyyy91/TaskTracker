package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/** Класс задачи */
public class Task {
    protected String name;
    protected String description;
    protected TaskStatus status;
    protected TaskType type;
    protected int id;
    protected LocalDateTime startTime;
    protected Duration duration;
    protected final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public Task(String name, String description) {
        this.name = name;
        this.description = description;
        this.startTime = LocalDateTime.of(2099, 12, 31, 0, 0);
        this.duration = Duration.ofMinutes(0);
        this.status = TaskStatus.NEW;
        this.type = TaskType.TASK;
    }

    public Task(String name, String description, String startTime, long duration) {
        this.name = name;
        this.description = description;
        this.startTime = LocalDateTime.parse(startTime, formatter);
        this.duration = Duration.ofMinutes(duration);
        this.status = TaskStatus.NEW;
        this.type = TaskType.TASK;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public TaskType getType() {
        return type;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return startTime.plus(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id && Objects.equals(name, task.name)
                && Objects.equals(description, task.description)
                && Objects.equals(status, task.status)
                && Objects.equals(type, task.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, status, type);
    }

    @Override
    public String toString() {
        return String.format("%d,%s,\"%s\",%s,\"%s\",%s,%s",
                id, type, name, status, description, startTime.format(formatter), getEndTime().format(formatter));
    }
}
