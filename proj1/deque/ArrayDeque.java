package deque;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private int size;
    private DequeIndex dequeIndex;
    private T[] array;
    private UsageRate usageRate;  //

    private class UsageRate { //
        private final double minUsageRate = 0.25;  // 最小使用率
        private final double maxUsageRate = 0.80;

        public void check() {
            double currentUsageRate = (double) size / array.length;
            if (currentUsageRate < minUsageRate && array.length >= 16) {
                sizeUpdate(array.length / 2);
            } else if (currentUsageRate > maxUsageRate) {
                sizeUpdate(array.length * 2);
            }
        }
        private void sizeUpdate(int newSize) {
            T[] newArray = (T[]) new Object[newSize];
            int f = dequeIndex.indexArray[0];
            int s = dequeIndex.indexArray[1];
            if (f + size >= array.length - 1) {
                System.arraycopy(array, 0, newArray, 0, s);
                System.arraycopy(array, f + 1, newArray,
                        (newSize - array.length) + f + 1, array.length - 1 - f);
                dequeIndex.indexArray = new int[]{f + (newSize - array.length), s};
            } else {
                System.arraycopy(array, f + 1, newArray, 0, size);
                dequeIndex.indexArray = new int[]{newSize - 1, size};
            }
            array = newArray;
        }
    }

    private class DequeIndex {
        private int[] indexArray;
        DequeIndex() {
            indexArray = new int[]{array.length - 1, 0};
        }

        public void indexupdate(int firstLast, String operation) {
            String operation2 = firstLast + operation;
            switch (operation2) {
                case "1add":
                case "0remove":
                    indexArray[firstLast]++;
                    break;
                case "0add":
                case "1remove":
                    indexArray[firstLast]--;
                    break;
                default:
                    break;
            }
            if (indexArray[firstLast] == -1) {
                indexArray[firstLast] = array.length - 1;
            } else if (indexArray[firstLast] == array.length) {
                indexArray[firstLast] = 0;
            }
        }


    }

    public ArrayDeque() {
        int beginsize = 8;
        array = (T[]) new Object[beginsize];
        size = 0;
        dequeIndex = new DequeIndex();
        usageRate = new UsageRate();
    }

    /*public ArrayDeque(T item) {
        this();
        this.addFirst(item);
    }*/


    public int size() {
        return this.size;
    }


    public void addFirst(T item) {
        array[dequeIndex.indexArray[0]] = item;
        size++;
        dequeIndex.indexupdate(0, "add");
        usageRate.check();
    }


    public void addLast(T item) {
        array[dequeIndex.indexArray[1]] = item;
        size++;
        dequeIndex.indexupdate(1, "add");
        usageRate.check();
    }


    public T removeFirst() {
        if (isEmpty()) {
            return null;
        } else {
            dequeIndex.indexupdate(0, "remove");
            size--;
            T item = array[dequeIndex.indexArray[0]];
            array[dequeIndex.indexArray[0]] = null;
            usageRate.check();
            return item;
        }

    }

    public T removeLast() {
        if (isEmpty()) {
            return null;
        } else {
            dequeIndex.indexupdate(1, "remove");
            size--;
            T item = array[dequeIndex.indexArray[1]];
            array[dequeIndex.indexArray[1]] = null;
            usageRate.check();
            return item;
        }

    }

    public T get(int index) {
        if (index >= this.size) {
            return null;
        } else if (index + this.dequeIndex.indexArray[0] + 1  < this.array.length) {
            return this.array[index + this.dequeIndex.indexArray[0] + 1];
        } else {
            return this.array[index + this.dequeIndex.indexArray[0] + 1 - array.length];
        }
    }

    public void printDeque() {
        Iterator<T> it = this.iterator();
        for (int i = 1; i < this.size; i++) {
            System.out.print(it.next() + " ");
        }
        System.out.println(it.next());
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
        private int currentindex = dequeIndex.indexArray[0] + 1;
        private int count = 0;

        @Override
        public boolean hasNext() {
            return count < size;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            if (currentindex == array.length) {
                currentindex = 0;
            }
            currentindex++;
            count++;
            return array[currentindex - 1];
        }
    }
}

