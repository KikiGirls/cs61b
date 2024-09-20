package deque;

import java.util.Comparator;
import java.util.Iterator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private final Comparator<T> comparator;
    public MaxArrayDeque(Comparator<T> c){
        comparator = c;
    }

    public T max(Comparator<T> c){
        if(isEmpty()) return null;
        T maxnew = this.get(0);
        Iterator<T> iterator =this.iterator();
        while (iterator.hasNext()) {
            T item = iterator.next();
            if (c.compare(item, maxnew) > 0) {maxnew = item;};
        }
        return maxnew;
    }
    public T max(){
        return max(comparator);
    }

}
