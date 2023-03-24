import java.util.HashMap;

public class Manager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, EpicTask> epicTasks = new HashMap<>();
    private HashMap<Integer, SubTask> subTasks = new HashMap<>();
    int id;

    public HashMap<Integer, Task> getTaskList() { // Получение списка задач
        return tasks;
    }

    public HashMap<Integer, EpicTask> getEpicTaskList() { // Получение списка эпиков
        return epicTasks;
    }

    public HashMap<Integer, SubTask> getSubTaskList() { // Получение списка подзадач
        return subTasks;
    }

    public void removeTasks() { // Удаление всех задач
        tasks.clear();
    }

    public void removeEpicTasks() { // Удаление всех эпиков
        epicTasks.clear();
        subTasks.clear();
    }

    public void removeSubTasks() { // Удаление всех подзадач
        subTasks.clear();
    }

    public Object getTaskById(int id) { // Возвращает задачу, эпик или подзадачу по идентификатору
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        } else if (epicTasks.containsKey(id)) {
            return epicTasks.get(id);
        } else if (subTasks.containsKey(id)) {
            return subTasks.get(id);
        } else {
            return null;
        }
    }

    public void createTask(Task task) { // Создание задачи
        id++;
        task.setId(id);
        tasks.put(id, task);
    }

    public void createEpicTask(EpicTask epicTask) { // Создание эпика
        id++;
        epicTask.setId(id);
        epicTasks.put(id, epicTask);
    }

    public void createSubTask(SubTask subTask) { // Создание подзадачи
        id++;
        subTask.setId(id);
        subTasks.put(id, subTask);
        subTask.getMasterTask().subTasks.put(id, subTask);
    }

    public void updateTask(Task task) { // Обновление задачи
        tasks.put(task.getId(), task);
    }

    public void updateEpicTask(EpicTask epicTask) { // Обновление эпика
        epicTasks.put(epicTask.getId(), epicTask);
    }

    public void updateSubTask(SubTask subTask) { // Обновление подзадачи
        subTasks.put(subTask.getId(), subTask);
        boolean isInProgress = false;
        boolean isDone = true;
        for (SubTask task : subTask.getMasterTask().subTasks.values()) {
            if (task.getStatus().equals("IN_PROGRESS")) {
                isInProgress = true;
                isDone = false;
            } else if (task.getStatus().equals("NEW")) {
                isDone = false;
            }
        }
        if (isInProgress) {
            subTask.getMasterTask().setStatus("IN_PROGRESS");
        }
        if (isDone) {
            subTask.getMasterTask().setStatus("DONE");
        }
    }

    public void removeTaskById(int id) { // Удаление конкретной задачи, эпика или подзадачи по индентификатору
        if (tasks.containsKey(id)) {
            tasks.remove(id);
            System.out.println("Задача с идентификатором " + id + " удалена.");
        } else if (epicTasks.containsKey(id)) {
            for (int i : epicTasks.get(id).subTasks.keySet()) {
                subTasks.remove(i);
            }
            epicTasks.remove(id);
            System.out.println("Эпик с идентификатором " + id + " и все его подзадачи удалены.");
        } else if (subTasks.containsKey(id)) {
            subTasks.remove(id);
            System.out.println("Подзадача с идентификатором " + id + " удалена.");
        } else {
            System.out.println("Задачи с идентификатором " + id + " не существует.");
        }
    }

    public HashMap<Integer, SubTask> getEpicSubTasks(int id) { // Получение списка всех подзадач конкретного эпика по идентификатору
        return epicTasks.get(id).subTasks;
    }
}
