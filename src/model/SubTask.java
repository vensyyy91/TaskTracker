package model;

/** Класс подзадачи */
public class SubTask extends Task {
    /** Поле Идентификатор эпика, которому принадлежит подзадача */
    private int masterTaskId;

    public SubTask(String name, String description, EpicTask masterTask) {
        super(name, description);
        this.masterTaskId = masterTask.getId();
        this.type = TaskType.SUBTASK;
    }

    public int getMasterTaskId() {
        return masterTaskId;
    }

    public void setMasterTaskId(int masterTaskId) {
        this.masterTaskId = masterTaskId;
    }

    @Override
    public String toString() {
        return String.format("%d,%s,\"%s\",%s,\"%s\",%d", id, type, name, status, description, masterTaskId);
    }
}
