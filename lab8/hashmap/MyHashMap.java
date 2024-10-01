package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {
    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!

    private int size = 0;

    private int initialSize;

    private double loadFactor;

    private Collection<Node>[] table;

    private Set<K> keySet = new HashSet<>();

    /** Constructors */
    public MyHashMap() {
        initialSize = 16;
        table = createTable(initialSize);
        loadFactor = 0.75;

    }

    public MyHashMap(int initialSize) {
        this.initialSize = initialSize;
        table = createTable(initialSize);
        loadFactor = 0.75;
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        this.initialSize = initialSize;
        this.loadFactor = maxLoad;
        table = createTable(initialSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new ArrayList<Node>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = (Collection<Node>[]) new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    private void sizeCheck() {
        if ((double) size / table.length >= loadFactor) {
            Collection<Node>[] newTable = createTable(table.length * 2);
            for (Collection<Node> buckets : table) {
                for (Node node : buckets) {
                    int hash = node.key.hashCode();
                    int index = hash % newTable.length;
                    newTable[index].add(node);
                }
            }

            table = newTable;
        }
    }

    // Your code won't compile until you do so!

    @Override
    public void clear() {
        int size = table.length;
        this.table = createTable(size);
        this.size = 0;
        keySet = new HashSet<>();
    }

    @Override
    public boolean containsKey(K key) {
        int hash = key.hashCode();
        int index = Math.abs(hash % table.length);
        for (Node node : table[index]) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int hash = key.hashCode();
        int index = Math.abs(hash % table.length);
        for (Node node : table[index]) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;

    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        int hash = key.hashCode();
        int index = Math.abs(hash % table.length);
        for (Node node : table[index]) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }
        table[index].add(new Node(key, value));
        keySet.add(key);
        size++;
        sizeCheck();
    }

    @Override
    public Set<K> keySet() {
        return keySet;
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        return null;

    }
}
