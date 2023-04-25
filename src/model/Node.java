package model;

public class Node <T> {
    /** Поле Значение узла */
    public T data;
    /** Поле Ссылка на следующий узел */
    public Node<T> next;
    /** Поле Ссылка на предыдущий узел */
    public Node<T> prev;

    public Node(Node<T> prev, T data, Node<T> next) {
        this.prev = prev;
        this.data = data;
        this.next = next;
    }
}
