import java.util.HashMap;

public class EpicTask extends Task {
    HashMap<Integer, SubTask> subTasks = new HashMap<>();

    public EpicTask(String name, String description) {
        super(name, description);
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
