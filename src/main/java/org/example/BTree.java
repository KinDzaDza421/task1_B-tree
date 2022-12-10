package org.example;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

class BTree<T extends Comparable<T>> {

    private int minKeySize;
    private int minChildrenSize;
    private int maxKeySize;
    private int maxChildrenSize;
    private int size = 0;
    private Node<T> root = null;

    public BTree() {
        minKeySize = 1;
        minChildrenSize = minKeySize + 1;
        maxKeySize = 2 * minKeySize;
        maxChildrenSize = maxKeySize + 1;
    }

    public BTree(int minKeySize) {
        this.minKeySize = minKeySize;
        this.minChildrenSize = this.minKeySize + 1;
        this.maxKeySize = 2 * this.minKeySize;
        this.maxChildrenSize = maxKeySize + 1;
    }

    public int size() { //возвращает кол-во элементов в дереве
        return size;
    }

    public void clear() {
        root = null;
        size = 0;
    }

    public void add(T value) {

        if (root == null) {
            root = new Node<>(null, maxKeySize, maxChildrenSize);
            root.addKey(value);
        }

        else {
            Node<T> node = root;
            while (node != null) {
                if (node.numberOfChildren() == 0) {
                    node.addKey(value);
                    if (node.numberOfKeys() <= maxKeySize) {
                        break;
                    }
                    split(node);
                    break;
                }

                T lesser = node.getKey(0);
                if (value.compareTo(lesser) <= 0) {
                    node = node.getChild(0);
                    continue;
                }

                int numberOfKeys = node.numberOfKeys();
                int last = numberOfKeys - 1;
                T greater = node.getKey(last);

                if (value.compareTo(greater) > 0) {
                    node = node.getChild(numberOfKeys);
                    continue;
                }

                for (int i = 1; i < node.numberOfKeys(); i++) {
                    T prev = node.getKey(i - 1);
                    T next = node.getKey(i);
                    if (value.compareTo(prev) > 0 && value.compareTo(next) <= 0) {
                        node = node.getChild(i);
                        break;
                    }
                }
            }
        }
        size++;
    }

    public boolean contains(T value) {
        Node<T> node = getNode(value);
        return (node != null);
    }

    public Node<T> remove(T value) {
        Node<T> node = this.getNode(value);
        remove(value,node);
        return node;
    }

    private void remove(T value, Node<T> node) {

        if (node == null) {
            return;
        }

        int index = node.indexOf(value);
        node.removeKey(value);

        if (node.numberOfChildren() == 0) {
            if (node.parent != null && node.numberOfKeys() < minKeySize) {
                this.combine(node);
            }
            else if (node.parent == null && node.numberOfKeys() == 0) {
                root = null;
            }
        }

        else {

            Node<T> lesser = node.getChild(index);
            Node<T> greatest = this.getGreatestNode(lesser);
            T replaceValue = this.removeGreatestValue(greatest);
            node.addKey(replaceValue);

            if (greatest.parent != null && greatest.numberOfKeys() < minKeySize) {
                this.combine(greatest);
            }

            if (greatest.numberOfChildren() > maxChildrenSize) {
                this.split(greatest);
            }
        }
        size--;
    }

    private void split(Node<T> nodeToSplit) {

        Node<T> node = nodeToSplit;
        int numberOfKeys = node.numberOfKeys();
        int medianIndex = numberOfKeys / 2;
        T medianValue = node.getKey(medianIndex);
        Node<T> left = new Node<>(null, maxKeySize, maxChildrenSize);

        for (int i = 0; i < medianIndex; i++) {
            left.addKey(node.getKey(i));
        }

        if (node.numberOfChildren() > 0) {
            for (int j = 0; j <= medianIndex; j++) {
                Node<T> c = node.getChild(j);
                assert c != null;
                left.addChild(c);
            }
        }

        Node<T> right = new Node<>(null, maxKeySize, maxChildrenSize);

        for (int i = medianIndex + 1; i < numberOfKeys; i++) {
            right.addKey(node.getKey(i));
        }

        if (node.numberOfChildren() > 0) {
            for (int j = medianIndex + 1; j < node.numberOfChildren(); j++) {
                Node<T> c = node.getChild(j);
                assert c != null;
                right.addChild(c);
            }
        }

        if (node.parent == null) {
            Node<T> newRoot = new Node<>(null, maxKeySize, maxChildrenSize);
            newRoot.addKey(medianValue);
            node.parent = newRoot;
            root = newRoot;
            node = root;
            node.addChild(left);
            node.addChild(right);
        }

        else {

            Node<T> parent = node.parent;
            parent.addKey(medianValue);
            parent.removeChild(node);
            parent.addChild(left);
            parent.addChild(right);

            if (parent.numberOfKeys() > maxKeySize) {
                split(parent);
            }
        }
    }

    private void combine(Node<T> node) {

        Node<T> parent = node.parent;
        int index = parent.indexOf(node);
        int indexOfLeftNeighbor = index - 1;
        int indexOfRightNeighbor = index + 1;
        Node<T> rightNeighbor = null;
        int rightNeighborSize = -minChildrenSize;

        if (indexOfRightNeighbor < parent.numberOfChildren()) {
            rightNeighbor = parent.getChild(indexOfRightNeighbor);
            assert rightNeighbor != null;
            rightNeighborSize = rightNeighbor.numberOfKeys();
        }

        if (rightNeighbor != null && rightNeighborSize > minKeySize) {
            T removeValue = rightNeighbor.getKey(0);
            int prev = getPreviousValueIndex(parent, removeValue);
            T parentValue = parent.removeKey(prev);
            T neighborValue = rightNeighbor.removeKey(0);
            node.addKey(parentValue);
            parent.addKey(neighborValue);

            if (rightNeighbor.numberOfChildren() > 0) {
                node.addChild(Objects.requireNonNull(rightNeighbor.removeChild(0)));
            }
        }

        else {

            Node<T> leftNeighbor = null;
            int leftNeighborSize = -minChildrenSize;

            if (indexOfLeftNeighbor >= 0) {
                leftNeighbor = parent.getChild(indexOfLeftNeighbor);
                assert leftNeighbor != null;
                leftNeighborSize = leftNeighbor.numberOfKeys();
            }

            if (leftNeighbor != null && leftNeighborSize > minKeySize) {
                T removeValue = leftNeighbor.getKey(leftNeighbor.numberOfKeys() - 1);
                int prev = getNextValueIndex(parent, removeValue);
                T parentValue = parent.removeKey(prev);
                T neighborValue = leftNeighbor.removeKey(leftNeighbor.numberOfKeys() - 1);
                node.addKey(parentValue);
                parent.addKey(neighborValue);

                if (leftNeighbor.numberOfChildren() > 0) {
                    node.addChild(Objects.requireNonNull(leftNeighbor.removeChild(leftNeighbor.numberOfChildren() - 1)));
                }
            }

            else if (rightNeighbor != null && parent.numberOfKeys() > 0) {
                T removeValue = rightNeighbor.getKey(0);
                int prev = getPreviousValueIndex(parent, removeValue);
                T parentValue = parent.removeKey(prev);
                parent.removeChild(rightNeighbor);
                node.addKey(parentValue);

                for (int i = 0; i < rightNeighbor.keysSize; i++) {
                    T v = rightNeighbor.getKey(i);
                    node.addKey(v);
                }

                for (int i = 0; i < rightNeighbor.childrenSize; i++) {
                    Node<T> c = rightNeighbor.getChild(i);
                    assert c != null;
                    node.addChild(c);
                }

                if (parent.parent != null && parent.numberOfKeys() < minKeySize) {
                    this.combine(parent);
                }

                else if (parent.numberOfKeys() == 0) {
                    node.parent = null;
                    root = node;
                }
            }

            else if (leftNeighbor != null && parent.numberOfKeys() > 0) {
                T removeValue = leftNeighbor.getKey(leftNeighbor.numberOfKeys() - 1);
                int prev = getNextValueIndex(parent, removeValue);
                T parentValue = parent.removeKey(prev);
                parent.removeChild(leftNeighbor);
                node.addKey(parentValue);

                for (int i = 0; i < leftNeighbor.keysSize; i++) {
                    T v = leftNeighbor.getKey(i);
                    node.addKey(v);

                }

                for (int i = 0; i < leftNeighbor.childrenSize; i++) {
                    Node<T> c = leftNeighbor.getChild(i);
                    assert c != null;
                    node.addChild(c);
                }

                if (parent.parent != null && parent.numberOfKeys() < minKeySize) {
                    this.combine(parent);
                }

                else if (parent.numberOfKeys() == 0) {
                    node.parent = null;
                    root = node;
                }
            }
        }
    }

    public Node<T> getNode(T value) {
        Node<T> node = root;
        while (node != null) {

            T least = node.getKey(0);

            if (value.compareTo(least) < 0) {
                if (node.numberOfChildren() > 0){
                    node = node.getChild(0);
                }
                else {
                    node = null;
                }
                continue;
            }

            int numberOfKeys = node.numberOfKeys();
            int last = numberOfKeys - 1;
            T largest = node.getKey(last);

            if (value.compareTo(largest) > 0) {
                if (node.numberOfChildren() > numberOfKeys){
                    node = node.getChild(numberOfKeys);
                }
                else{
                    node = null;
                }
                continue;
            }

            for (int i = 0; i < numberOfKeys; i++) {
                T currentValue = node.getKey(i);
                if (currentValue.compareTo(value) == 0) {
                    return node;
                }
                int next = i + 1;
                if (next <= last) {
                    T nextValue = node.getKey(next);
                    if (currentValue.compareTo(value) < 0 && nextValue.compareTo(value) > 0) {
                        if (next < node.numberOfChildren()) {
                            node = node.getChild(next);
                            break;
                        }
                        return null;
                    }
                }
            }
        }
        return null;
    }

    private T removeGreatestValue(Node<T> node) {
        T value = null;

        if (node.numberOfKeys() > 0)
            value = node.removeKey(node.numberOfKeys() - 1);

        return value;
    }

    private Node<T> getGreatestNode(Node<T> nodeToGet) {
        Node<T> node = nodeToGet;

        while (true) {
            assert node != null;
            if (!(node.numberOfChildren() > 0)) break;

            node = node.getChild(node.numberOfChildren() - 1);
        }

        return node;
    }

    private int getPreviousValueIndex(Node<T> node, T value) {
        for (int i = 1; i < node.numberOfKeys(); i++) {
            T t = node.getKey(i);
            if (t.compareTo(value) >= 0) {
                return i - 1;
            }
        }
        return node.numberOfKeys() - 1;
    }

    private int getNextValueIndex(Node<T> node, T value) {
        for (int i = 0; i < node.numberOfKeys(); i++) {
            T t = node.getKey(i);
            if (t.compareTo(value) >= 0){
                return i;
            }
        }
        return node.numberOfKeys() - 1;
    }

    private static class Node<T extends Comparable<T>> {

        private final T[] keys;
        private int keysSize;
        private final Node<T>[] children;
        private int childrenSize;
        private Node<T> parent;
        private Comparator<Node<T>> comparator = Comparator.comparing(arg0 -> arg0.getKey(0));

        private Node(Node<T> parent, int maxKeySize, int maxChildrenSize) {
            this.parent = parent;
            this.keys = (T[]) new Comparable[maxKeySize + 1];
            this.keysSize = 0;
            this.children = new Node[maxChildrenSize + 1];
            this.childrenSize = 0;
        }

        private T getKey(int index) { //получаем данные по ключу
            return keys[index];
        }

        private void addKey(T value) {
            keys[keysSize++] = value;
            Arrays.sort(keys, 0, keysSize);
        }

        private int numberOfKeys() { //возвращает кол-во ключей
            return keysSize;
        }

        private Node<T> getChild(int index) {
            if (index >= childrenSize){
                return null;
            }
            return children[index];
        }

        private void addChild(Node<T> child) {
            child.parent = this;
            children[childrenSize++] = child;
            Arrays.sort(children, 0, childrenSize, comparator);
        }

        private void removeChild(Node<T> child) {
            boolean found = false;
            if (childrenSize == 0) {
                return;
            }
            for (int i = 0; i < childrenSize; i++) {
                if (children[i].equals(child)) {
                    found = true;
                }
                else if (found) {
                    children[i - 1] = children[i];
                }
            }
            if (found) { //если нашли
                childrenSize--;
                children[childrenSize] = null;
            }
        }

        private int numberOfChildren() { //возвращает кол-во потомков
            return childrenSize;
        }

        private int indexOf(T value) {
            for (int i = 0; i < keysSize; i++) {
                if (keys[i].equals(value)) {
                    return i;
                }
            }
            return -1;
        }

        private void removeKey(T value) {
            boolean found = false;
            if (keysSize == 0) {
                return;
            }

            for (int i = 0; i < keysSize; i++) {
                if (keys[i].equals(value)) {
                    found = true;
                }
                else if (found) {
                    keys[i - 1] = keys[i];
                }
            }

            if (found) {
                keysSize--;
                keys[keysSize] = null;
            }
        }

        private T removeKey(int index) {
            if (index >= keysSize){
                return null;
            }
            T value = keys[index];
            if (keysSize - (index + 1) >= 0){
                System.arraycopy(keys, index + 1, keys, index + 1 - 1, keysSize - (index + 1));
            }
            keysSize--;
            keys[keysSize] = null;
            return value;
        }

        private int indexOf(Node<T> child) {
            for (int i = 0; i < childrenSize; i++) {
                if (children[i].equals(child)){
                    return i;
                }
            }
            return -1;
        }

        private Node<T> removeChild(int index) {
            if (index >= childrenSize) {
                return null;
            }
            Node<T> value = children[index];
            children[index] = null;

            if (childrenSize - (index + 1) >= 0) {
                System.arraycopy(children, index + 1, children, index + 1 - 1, childrenSize - (index + 1));
            }
            childrenSize--;
            children[childrenSize] = null;
            return value;
        }
    }
}