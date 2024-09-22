package deque;

import java.util.Comparator;
import java.util.Iterator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private final Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c){
        comparator = c;
    }

    public T max(Comparator<T> c){
        if (isEmpty()) return null;

        // 初始化为第一个非空元素
        T maxItem = this.get(0);

        // 使用迭代器遍历
        Iterator<T> iterator = this.iterator();
        while (iterator.hasNext()) {
            T item = iterator.next();
            if (item != null && c.compare(item, maxItem) > 0) {
                maxItem = item;
            }
        }
        return maxItem;
    }

    public T max() {
        // 默认使用构造函数传入的 comparator
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator cannot be null");
        }
        return max(comparator);
    }
}
