package deque;

import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ArrayDeque<T> {
    private int size;
    private DequeIndex index;
    private T[] array;
    private usageRate UsageRate;  //


    class usageRate{ //
        private final double minUsageRate = 0.25;  // 最小使用率
        private final double maxUsageRate = 0.80;

        public void Check(){
            double currentUsageRate = (double) size / array.length;
            if (currentUsageRate < minUsageRate && array.length >=16){
                sizeUpdate(array.length / 2);
            }else if (currentUsageRate > maxUsageRate){
                sizeUpdate(array.length * 2);
            }
        }
        private void sizeUpdate(int newSize){
            T[] newArray =(T[]) new Object[newSize];
            if (index.IndexArray[0] + size >= array.length ){
                System.arraycopy(array, 0, newArray, 0, index.IndexArray[1]);
                System.arraycopy(array, index.IndexArray[0]+1, newArray, newSize -array.length -index.IndexArray[0] +1, array.length - 1 - index.IndexArray[0]);
            }else {
                System.arraycopy(array, index.IndexArray[0] + 1, newArray, index.IndexArray[0] + 1, size);
            }
            index.IndexArray = new int[]{index.IndexArray[0], index.IndexArray[1] + newSize - array.length};
            array = newArray;
        }
    }

    class DequeIndex{
        public int[] IndexArray;
        public DequeIndex(){
            IndexArray = new int[]{array.length - 1, 0};
        }

        public void Indexupdate(int index, String operation){
            String operation2 = index + operation;
            switch (operation2){
                case "1add":
                case "0remove":
                    IndexArray[index]++;
                    break;
                case "2add":
                case "1remove":
                    IndexArray[index]--;
                    break;
            }
            if (IndexArray[index] == - 1) {
                IndexArray[index] = array.length - 1;
            }else if (IndexArray[index] == array.length) {
                IndexArray[index] = 0;
            }
        }


    }

    public ArrayDeque() {
        int beginsize = 8;
        array = (T[]) new Object[beginsize];
        size = 0;
        index = new DequeIndex();
        UsageRate = new usageRate();
    }

    public ArrayDeque(T item) {
        this();
        this.addFirst(item);
    }

    public int size(){
        return this.size;
    }

    public boolean isEmpty(){
        return this.size == 0;
    }

    public void addFirst(T item) {
        array[index.IndexArray[0]] = item;
        size++;
        index.Indexupdate(0, "add");
        UsageRate.Check();
    }

    public void addLast(T item) {
        array[index.IndexArray[1]] = item;
        size++;
        index.Indexupdate(1, "add");
        UsageRate.Check();
    }

    public T removeFirst(){
        if (isEmpty()) {
            return null;
        }else {
            T item = this.array[index.IndexArray[0] + 1];
            index.Indexupdate(0, "remove");
            UsageRate.Check();
            return item;
        }

    }

    public T removeLast(){
        if (isEmpty()) {
            return null;
        }else {
            T item = this.array[index.IndexArray[1] - 1];
            index.Indexupdate(1, "remove");
            UsageRate.Check();
            return item;
        }

    }

    public T get(int index){
        if (index >= this.size) {
            return null;
        }else if (index + this.index.IndexArray[0] < this.array.length) {
            return this.array[index + this.index.IndexArray[0] +1];
        }else {
            return this.array[index + this.index.IndexArray[0] + 1 - array.length];
        }
    }

    public void printDeque(){
        Iterator<T> it = this.iterator();
        for (int i = 1; i < this.size; i++) {
            System.out.print(it.next() + " ");
        }
        System.out.println(it.next());
    }

    public boolean equals(Object o){
        boolean result = false;
        if (o instanceof ArrayDeque) {
            ArrayDeque other = (ArrayDeque) o;
            if (this.size == other.size) {
                result = true;
                for (int i = 0; i < this.size; i++) {
                    if (this.array[i] != other.array[i]) {
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    public Iterator<T> iterator(){
        return new DequeIterator();
    }
        private class DequeIterator implements Iterator<T>{
            private int currentindex = index.IndexArray[0] + 1;
            public boolean hasNext(){
                return currentindex != index.IndexArray[1];
            }
            public T next(){
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }else {
                    if (currentindex == array.length) {
                        currentindex = 0;
                    }
                    currentindex++;
                    return array[currentindex - 1];
                }
            }
        }
}

