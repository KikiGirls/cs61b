package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V> {
    private BST bst;

    BSTMap() {
        bst = new BST();
    }

    @Override
    public void clear() {
        bst = new BST();
    }

    @Override
    public boolean containsKey(K key) {
        return bst.contains(key);
    }

    @Override
    public V get(K key) {
        return bst.bstGet(key);
    }

    @Override
    public int size() {
        return bst.size;
    }

    @Override
    public void put(K key, V value) {
        BSTNode node = new BSTNode(key, value);
        if (bst.root == null) {
            bst.root = node;
            bst.size++;
        } else {
            bst.insert(node);
        }
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }

    class BSTNode {
        K key;
        V value;
        BSTNode left, right, parent;
        BSTNode(K key, V value) {
            this.key = key;
            this.value = value;
            this.parent = null;
            this.left = null;
            this.right = null;
        }

    }

    class BST {
        int size;
        private BSTNode root;
        public BST() {
            root = null;
            size = 0;
        }
        public BST(K key, V value) {
            root = new BSTNode(key, value);
        }
        private void insert(BSTNode node) {
            BSTNode current = root;
            while (current != null) {
                if (current.key.compareTo(node.key) < 0) {
                    if (current.right == null) {
                        current.right = node;
                        node.parent = current;
                        size++;
                        break;
                    } else {
                        current = current.right;
                    }
                } else if (current.key.compareTo(node.key) > 0) {
                    if (current.left == null) {
                        current.left = node;
                        node.parent = current;
                        size++;
                        break;
                    } else {
                        current = current.left;
                    }
                } else {
                    break;
                }
            }
        }
        private boolean contains(K key) {
            BSTNode current = root;
            while (current != null) {
                if (current.key.compareTo(key) == 0) {
                    return true;
                } else if (current.key.compareTo(key) < 0) {
                    current = current.right;
                } else if (current.key.compareTo(key) > 0) {
                    current = current.left;
                }
            }
            return false;
        }
        private V bstGet(K key) {
            BSTNode current = root;
            while (current != null) {
                if (current.key.compareTo(key) == 0) {
                    return current.value;
                } else if (current.key.compareTo(key) < 0) {
                    current = current.right;
                } else if (current.key.compareTo(key) > 0) {
                    current = current.left;
                }
            }
            return null;
        }
    }

}
