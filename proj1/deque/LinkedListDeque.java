package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private class ListNode {
        public T item;
        public ListNode prev;
        public ListNode next;

        //构造函数
        public ListNode(){} //哨兵结点的构造
        public ListNode(T item){
            this.item = item;
        } // 值结点的构造
    }

    private ListNode sentinel;
    private int size;

    public LinkedListDeque(){
        sentinel = new ListNode();
        sentinel.next = sentinel;
        sentinel.prev = sentinel;
        this.size = 0;
    }

    /*public LinkedListDeque(T item){
        ListNode items = new ListNode(item);
        sentinel = new ListNode();
        sentinel.next = items;
        sentinel.prev = items;
        items.prev = sentinel;
        items.next = sentinel;
        this.size = 1;
    }*/


    public int size(){
        return size;
    }


    public void addFirst(T item){
        ListNode addFirstNode = new ListNode(item);
        addFirstNode.next = sentinel.next;
        addFirstNode.prev = sentinel;
        sentinel.next.prev = addFirstNode;
        sentinel.next = addFirstNode;
        this.size++;
    }


    public void addLast(T item){
        ListNode addLastNode = new ListNode(item);
        addLastNode.prev = sentinel.prev;
        addLastNode.next = sentinel;
        sentinel.prev.next = addLastNode;
        sentinel.prev = addLastNode;
        this.size++;
    }


    public T removeFirst(){
        if (isEmpty()) {
            return null;
        }else {
            ListNode removeFirstNode = sentinel.next;
            sentinel.next = removeFirstNode.next;
            removeFirstNode.next.prev = sentinel;
            this.size--;
            return removeFirstNode.item;
        }
    }


    public T removeLast(){
        if (isEmpty()) {
            return null;
        }else {
            ListNode removeLastNode = sentinel.prev;
            sentinel.prev = removeLastNode.prev;
            removeLastNode.prev.next = sentinel;
            this.size--;
            return removeLastNode.item;
        }
    }


    public T get(int index){
        int getIndex = 0;
        ListNode getIndex_currentNode = sentinel.next;
        if (index < 0 || index + 1 > size) {
            return null;
        }else {
            while (getIndex != index) {
                getIndex_currentNode = getIndex_currentNode.next;
                getIndex++;
            }
            return getIndex_currentNode.item;
        }
    }

    public T getRecursive(int index) {
        class GetRecursiveHelp {
            public T getRecursiveHelp(ListNode t, int index) {
                if (index < 0 || t == null) {
                    return null;
                } else if (index == 0) {
                    return t.item;
                }else {
                    return getRecursiveHelp(t.next, index - 1);
                }
            }
        }
        GetRecursiveHelp getRecursivehelp = new GetRecursiveHelp();
        return getRecursivehelp.getRecursiveHelp(sentinel.next, index);
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
    public Iterator<T> iterator(){
        return new DequeIterator();
    }
        private class DequeIterator implements Iterator<T>{
            private ListNode current = sentinel.next;

            @Override
            public boolean hasNext(){
                return current != sentinel;
            }
            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                T ret = current.item;
                current = current.next;
                return ret;
            }

        }
    public void printDeque(){
        ListNode current = sentinel.next;
        for (int i = 0; i < size ; i++) {
            if (i == size - 1) {
                System.out.println(current.item);
            }else {
                System.out.print(current.item + " ");
                current = current.next;
            }
        }
    }
}
