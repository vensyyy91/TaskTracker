package model;

public class SubTask extends Task {
    private int masterTaskId;

    public SubTask(String name, String description) {
        super(name, description);
    }

    public int getMasterTaskId() {
        return masterTaskId;
    }

    public void setMasterTaskId(int masterTaskId) {
        this.masterTaskId = masterTaskId;
    }

    @Override
    public String toString() {
        return "SubTask{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                ", id=" + id +
                ", masterTaskId=" + masterTaskId +
                '}';
    }
}
