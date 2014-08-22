/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.heap;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Simple array-backed heap implementation that relies on the
 * supplied comparator to impose the sense of order.
 * <p>
 * This class is not thread-safe.
 * <p>
 * Unlike the {@link java.util.PriorityQueue}, this class allows external
 * entities to force re-establishment of the heap property via {@code heapify}
 * method as a response to any mutations in elements' relative priorities.
 * 
 * @param <T> type of item on the heap
 * @author Thomas Vachuska
 */
public class Heap<T> {
    
    private static final String E_SIZE = "Size exceeds length of array";
    private static final String E_OVERFLOW = "Heap overflow";
    private static final String E_NO_MORE = "No more elements";
    private static final String E_UNSUPPORTED = "Destructive iteration is not supported";

    private final Comparator<T> comparator;

    // Use 0-based index
    private final T data[];
    
    // Keeps the current heap-size
    private int size;
    
    /**
     * Creates a new heap using the supplied array of items as the heap
     * backing. The size of array serves as the maximum allowed heap-size. Any
     * insertions into the heap beyond this size will be prevented.
     * 
     * @param data array to be used as heap backing
     * @param size initial size of heap
     * @param comparator comparator to be used for comparing items
     */
    public Heap(T data[], int size, Comparator<T> comparator) {
        if (size > data.length)
            throw new IllegalArgumentException(E_SIZE);
        this.data = data;
        this.size = size;
        this.comparator = comparator;
        heapify();
    }

    /**
     * Rearranges the backing array of items to establish the heap property.
     * <p>
     * This should be called following any change of the item key value.
     */
    public void heapify() {
        for (int i = size/2; i >= 0; i--)
            heapify(i);
    }
    
    /**
     * Sorts the given heap layer so it has the heap property.
     * 
     * @param i heap index or 'layer'
     */
    private void heapify(int i) {
       int left = 2 * i + 1;
       int right = 2 * i + 2;
       int extreme = i;
       
       if (left < size && comparator.compare(data[left], data[extreme]) > 0)
           extreme = left;
       if (right < size && comparator.compare(data[right], data[extreme]) > 0)
           extreme = right;
       
       if (extreme != i) {
           swap(i, extreme);
           heapify(extreme);
       }
    }
    
    // Swaps the two heap items given by their indeces.
    private void swap(int i, int j) {
        T tmp = data[i];
        data[i] = data[j];
        data[j] = tmp;
    }
    
    /**
     * Returns the heap size, i.e. number if items in the heap
     * 
     * @return heap size
     */
    public int size() {
        return size;
    }
    
    /**
     * Indicates whether the heap contains any items or not.
     * 
     * @return true if the heap is empty
     */
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * Returns the item that represents the heap extreme.
     * 
     * @return heap extreme (min/max); null if heap is empty
     */
    public T extreme() {
        return size > 0 ? data[0] : null;
    }
    
    /**
     * Extracts the item that represents the heap extreme.
     * 
     * @return heap extreme; null if heap is empty
     */
    public T extractExtreme() {
        if (size == 0)
            return null;
        
        T extreme = extreme();
            
        // Replace the old extreme with the last item and decrease heap size
        data[0] = data[size - 1];
        size--;
        
        // Then recover the heap property
        heapify(0);
        
        return extreme;
    }

    /**
     * Inserts a new item into the heap.
     * 
     * @param item item to be inserted
     * @return self
     * @throws IllegalStateException if the heap is full
     */
    public Heap<T> insert(T item) {
        if (size >= data.length)
            throw new IllegalStateException(E_OVERFLOW);
        data[size] = item;
        size++;
        bubbleUp();
        return this;
    }

    // Percolates the last item up to recover the heap property.
    private void bubbleUp() {
        int child = size - 1;
        while (child > 0) {
            int parent = child/2;
            if (comparator.compare(data[child], data[parent]) < 0)
                break;
            swap(parent, child);
            child = parent;
        }
    }
    
    /**
     * Returns non-destructive level-by-level top-down heap iterator.
     * 
     * @return heap iterator
     */
    public Iterator<T> iterator() {
        return new HeapIterator(); 
    }

    // Level-by-level top-down heap iterator
    private class HeapIterator implements Iterator<T> {
        
        private int current = 0;

        @Override
        public boolean hasNext() {
            return current < size;
        }

        @Override
        public T next() {
            if (current < data.length)
                return data[current++];
            throw new NoSuchElementException(E_NO_MORE);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException(E_UNSUPPORTED);
        }
        
    }
    
}
