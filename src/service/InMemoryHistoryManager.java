package service;

import model.Node;
import model.Task;

import java.util.*;

/** Класс для объекта-менеджера, в котором реализовано управление историей просмотров задач */
public class InMemoryHistoryManager implements HistoryManager {
    /** Поле История просмотров */
    private final CustomLinkedList<Task> taskHistory = new CustomLinkedList<>();
    /** Поле Мапа, хранящая идентификаторы просмотренных задач и соответствующие узлы списка taskHistory */
    private final Map<Integer, Node<Task>> taskMap = new HashMap<>();

    @Override
    public void add(Task task) {
        if (taskMap.containsKey(task.getId())) {
            taskHistory.removeNode(taskMap.get(task.getId()));
        }
        taskMap.put(task.getId(), taskHistory.linkLast(task));
    }

    @Override
    public void remove(int id) {
        if (taskMap.containsKey(id)) {
            taskHistory.removeNode(taskMap.get(id));
            taskMap.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return taskHistory.getTasks();
    }

    /** Класс для реализации двусвязного списка задач, позволяющий быстро удалить задачу из произвольного места */
    private static class CustomLinkedList<Task> {
        private Node<Task> head;
        private Node<Task> tail;
        private int size = 0;

        /**
         * Метод добавления задачи в конец списка
         * @param task - задача
         * @return возвращает узел списка с добавленной задачей
         */
        private Node<Task> linkLast(Task task) {
            final Node<Task> oldTail = tail;
            final Node<Task> newNode = new Node<>(oldTail, task, null);
            tail = newNode;
            if (oldTail == null) {
                head = newNode;
            } else {
                oldTail.next = newNode;
            }
            size++;
            return newNode;
        }

        /**
         * Метод получения списка задач (собирает все задачи из CustomLinkedList в ArrayList)
         * @return возвращает списко задач ввиде ArrayList
         */
        private List<Task> getTasks() {
            final List<Task> tasks = new ArrayList<>(size);
            for (Node<Task> current = head; current != null; current = current.next) {
                tasks.add(current.data);
            }
            return tasks;
        }

        /**
         * Метод удаления узла списка
         * @param node - узел
         */
        private void removeNode(Node<Task> node) {
            final Node<Task> prevNode = node.prev;
            final Node<Task> nextNode = node.next;
            if (prevNode == null) {
                head = nextNode;
            } else {
                prevNode.next = nextNode;
                node.prev = null;
            }
            if (nextNode == null) {
                tail = prevNode;
            } else {
                nextNode.prev = prevNode;
                node.next = null;
            }
            node.data = null;
            size--;
        }
    }
}
