package model;

import java.util.ArrayList;
import java.util.List;

/** Класс эпика */
public class EpicTask extends Task {
    /** Поле Список идентификаторов подзадач, принадлежащих эпику */
    private List<Integer> subTasksIdList = new ArrayList<>();

    public EpicTask(String name, String description) {
        super(name, description);
        this.type = TaskType.EPIC;
    }

    public List<Integer> getSubTasksIdList() {
        return subTasksIdList;
    }

    public void setSubTasksIdList(List<Integer> subTasksIdList) {
        this.subTasksIdList = subTasksIdList;
    }
}
