public class SubTask extends Task {
    private EpicTask masterTask;

    public SubTask(String name, String description, EpicTask masterTask) {
        super(name, description);
        this.masterTask = masterTask;
    }

    public EpicTask getMasterTask() {
        return masterTask;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", id=" + id +
                ", masterTask=" + masterTask.getName() +
                '}';
    }
}
