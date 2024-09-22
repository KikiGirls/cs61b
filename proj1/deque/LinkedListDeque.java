package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class ListNode {
        private T item;
        private ListNode prev;
        private ListNode next;
        //构造函数
        ListNode() {
        } //哨兵结点的构造
        ListNode(T item) {
            this.setItem(item);
        } // 值结点的构造

        private T getItem() {
            return item;
        }

        private void setItem(T item) {
            this.item = item;
        }

        private ListNode getPrev() {
            return prev;
        }

        private void setPrev(ListNode prev) {
            this.prev = prev;
        }

        private ListNode getNext() {
            return next;
        }

        private void setNext(ListNode next) {
            this.next = next;
        }
    }

    private ListNode sentinel;
    private int size;

    public LinkedListDeque() {
        sentinel = new ListNode();
        sentinel.setNext(sentinel);
        sentinel.setPrev(sentinel);
        this.size = 0;
    }

    /*public LinkedListDeque(T item) {
        ListNode items = new ListNode(item);
        sentinel = new ListNode();
        sentinel.next = items;
        sentinel.prev = items;
        items.prev = sentinel;
        items.next = sentinel;
        this.size = 1;
    }*/

    public int size() {
        return size;
    }

    public void addFirst(T item) {
        ListNode addFirstNode = new ListNode(item);
        addFirstNode.setNext(sentinel.getNext());
        addFirstNode.setPrev(sentinel);
        sentinel.getNext().setPrev(addFirstNode);
        sentinel.setNext(addFirstNode);
        this.size++;
    }

    public void addLast(T item) {
        ListNode addLastNode = new ListNode(item);
        addLastNode.setPrev(sentinel.getPrev());
        addLastNode.setNext(sentinel);
        sentinel.getPrev().setNext(addLastNode);
        sentinel.setPrev(addLastNode);
        this.size++;
    }

    public T removeFirst() {
        if (isEmpty()) {
            return null;
        } else {
            ListNode removeFirstNode = sentinel.getNext();
            sentinel.setNext(removeFirstNode.getNext());
            removeFirstNode.getNext().setPrev(sentinel);
            this.size--;
            return removeFirstNode.getItem();
        }
    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        } else {
            ListNode removeLastNode = sentinel.getPrev();
            sentinel.setPrev(removeLastNode.getPrev());
            removeLastNode.getPrev().setNext(sentinel);
            this.size--;
            return removeLastNode.getItem();
        }
    }

    public T get(int index) {
        int getIndex = 0;
        ListNode getIndexCurrentNode = sentinel.getNext();
        if (index < 0 || index + 1 > size) {
            return null;
        } else {
            while (getIndex != index) {
                getIndexCurrentNode = getIndexCurrentNode.getNext();
                getIndex++;
            }
            return getIndexCurrentNode.getItem();
        }
    }

    public T getRecursive(int index) {
        class GetRecursiveHelp {
            public T getRecursiveHelp(ListNode t, int index) {
                if (index < 0 || t == null) {
                    return null;
                } else if (index == 0) {
                    return t.getItem();
                } else {
                    return getRecursiveHelp(t.getNext(), index - 1);
                }
            }
        }
        GetRecursiveHelp getRecursivehelp = new GetRecursiveHelp();
        return getRecursivehelp.getRecursiveHelp(sentinel.getNext(), index);
    }

    @Override
    public boolean equals(Object o) {
        // 先检查是否为同一个引用
        if (this == o) {
            return true;
        }

        // 检查传入对象是否是实现了 Deque 且是 Iterable 类型的实例
        if (!(o instanceof Deque<?> && o instanceof Iterable<?>)) {
            return false;
        }

        // 强制转换为 Deque 和 Iterable
        Deque<?> other = (Deque<?>) o;
        Iterable<?> otherIterable = (Iterable<?>) o;

        // 检查大小是否相等
        if (this.size() != other.size()) {
            return false;
        }

        // 比较各个元素
        Iterator<T> thisIterator = this.iterator();
        Iterator<?> otherIterator = otherIterable.iterator();

        while (thisIterator.hasNext() && otherIterator.hasNext()) {
            T thisItem = thisIterator.next();
            Object otherItem = otherIterator.next();

            // 使用 equals() 方法比较元素内容
            if (!thisItem.equals(otherItem)) {
                return false;
            }
        }

        // 如果所有元素都相等，返回 true
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return new DequeIterator();
    }
    private class DequeIterator implements Iterator<T> {
        private ListNode current = sentinel.getNext();

        @Override
        public boolean hasNext() {
            return current != sentinel;
        }
        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            T ret = current.getItem();
            current = current.getNext();
            return ret;
        }
    }
    public void printDeque() {
        ListNode current = sentinel.getNext();
        for (int i = 0; i < size; i++) {
            if (i == size - 1) {
                System.out.println(current.getItem());
            } else {
                System.out.print(current.getItem() + " ");
                current = current.getNext();
            }
        }
    }
}
