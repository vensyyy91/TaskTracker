package model;

import java.util.ArrayList;
import java.util.List;

public class EpicTask extends Task {
    private List<Integer> subTasksIdList = new ArrayList<>();

    public EpicTask(String name, String description) {
        super(name, description);
    }

    public List<Integer> getSubTasksIdList() {
        return subTasksIdList;
    }

    public void setSubTasksIdList(List<Integer> subTasksIdList) {
        this.subTasksIdList = subTasksIdList;
    }

    @Override
    public String toString() {
        return "EpicTask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", id=" + id +
                '}';
    }
}
