/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.queue;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import org.opendaylight.openflowplugin.api.openflow.md.queue.QueueItem;
import org.opendaylight.openflowplugin.api.openflow.md.queue.WaterMarkListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WrapperQueueImpl<E> implements Queue<E> {

    private static final Logger LOG = LoggerFactory
            .getLogger(WrapperQueueImpl.class);

    private int lowWaterMark;
    private int highWaterMark;

    private WaterMarkListener queueListenerMark;

    private Queue<E> queueDefault;

    private boolean flooded;

    /**
     * @param capacity
     */
    public WrapperQueueImpl(int capacity, Queue<E> queueDefault,
            WaterMarkListener queueListenerMark) {
        this.queueListenerMark = queueListenerMark;
        this.queueDefault = Preconditions.checkNotNull(queueDefault);

        this.highWaterMark = (int) (capacity * 0.8);
        this.lowWaterMark = (int) (capacity * 0.65);
    }

    /**
     * Marking checks size of {@link #queueDefault} and on the basis of this is
     * set autoRead
     */
    private void marking() {
        if (queueDefault.size() >= highWaterMark && !flooded) {
            queueListenerMark.onHighWaterMark();
            flooded = true;
        } else if (queueDefault.size() <= lowWaterMark && flooded) {
            queueListenerMark.onLowWaterMark();
            flooded = false;
        }
    }

    /**
     * @return true if flooded
     */
    public boolean isFlooded() {
        return flooded;
    }

    /**
     * poll {@link QueueItem} and call {@link #marking()} for check marks and
     * set autoRead if it need it
     *
     * @return polled item
     */
    public E poll() {
        E nextQueueItem = queueDefault.poll();
        marking();
        return nextQueueItem;
    }

    public boolean add(E e) {
        return queueDefault.add(e);
    }

    public int size() {
        return queueDefault.size();
    }

    public boolean isEmpty() {
        return queueDefault.isEmpty();
    }

    public boolean contains(Object o) {
        return queueDefault.contains(o);
    }

    public boolean offer(E e) {
        boolean enqueueResult = queueDefault.offer(e);
        marking();
        return enqueueResult;
    }

    public Iterator<E> iterator() {
        return queueDefault.iterator();
    }

    public E remove() {
        return queueDefault.remove();
    }

    public Object[] toArray() {
        return queueDefault.toArray();
    }

    public E element() {
        return queueDefault.element();
    }

    public E peek() {
        return queueDefault.peek();
    }

    public <T> T[] toArray(T[] a) {
        return queueDefault.toArray(a);
    }

    public boolean remove(Object o) {
        return queueDefault.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return queueDefault.containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        return queueDefault.addAll(c);
    }

    public boolean removeAll(Collection<?> c) {
        return queueDefault.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return queueDefault.retainAll(c);
    }

    public void clear() {
        queueDefault.clear();
    }

    public boolean equals(Object o) {
        return queueDefault.equals(o);
    }

    public int hashCode() {
        return queueDefault.hashCode();
    }

}
