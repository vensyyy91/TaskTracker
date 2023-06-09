package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Класс эпика */
public class EpicTask extends Task {
    /** Поле Список идентификаторов подзадач, принадлежащих эпику */
    private List<Integer> subTasksIdList = new ArrayList<>();
    /** Поле Время окончания */
    private LocalDateTime endTime = startTime.plus(duration);

    public EpicTask(String name, String description) {
        super(name, description);
        this.type = TaskType.EPIC;
    }

    public EpicTask(String name, String description, String startTime, long duration) {
        super(name, description, startTime, duration);
        this.type = TaskType.EPIC;
    }

    public List<Integer> getSubTasksIdList() {
        return subTasksIdList;
    }

    public void setSubTasksIdList(List<Integer> subTasksIdList) {
        this.subTasksIdList = subTasksIdList;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
