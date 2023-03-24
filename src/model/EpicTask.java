package model;

import java.util.HashMap;

public class EpicTask extends Task {
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();

    public EpicTask(String name, String description) {
        super(name, description);
    }

    public HashMap<Integer, SubTask> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(HashMap<Integer, SubTask> subTasks) {
        this.subTasks = subTasks;
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
