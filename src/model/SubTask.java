package model;

/** Класс подзадачи */
public class SubTask extends Task {
    /** Поле Идентификатор эпика, которому принадлежит подзадача */
    private int masterTaskId;

    public SubTask(String name, String description, EpicTask masterTask) {
        super(name, description);
        this.masterTaskId = masterTask.getId();
    }

    public int getMasterTaskId() {
        return masterTaskId;
    }

    public void setMasterTaskId(int masterTaskId) {
        this.masterTaskId = masterTaskId;
    }

    @Override
    public String toString() {
        return String.format("SubTask{name='%s', description='%s', status=%s, masterTaskId=%d, id=%d}",
                name, description, status, masterTaskId, id);
    }
}
