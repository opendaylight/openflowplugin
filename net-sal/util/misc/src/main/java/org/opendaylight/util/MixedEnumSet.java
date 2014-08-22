/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * This class implements the {@link java.util.Set} interface and is designed
 * to contain enumeration constants from multiple enumerations.
 *
 * @author Simon Hunt
 */
public class MixedEnumSet implements Set<Enum<?>> {

    private static final String DELIM = ", ";
    private static final String OPEN_B = "[";
    private static final String CLOSE_B = "]";
    private static final String DOT = ".";

    // internally we'll maintain a set of objects..
    private final Set<Object> mySet = new HashSet<Object>();

    /** Constructs an empty mixed enum set. */
    public MixedEnumSet() { }

    /** Constructs a new mixed enum set, initialized to the given
     * collection.
     *
     * @param c a collection of enum constants
     */
    public MixedEnumSet(Collection<? extends Enum<?>> c) {
        addAll(c);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(OPEN_B);
        for (final Object o : mySet) {
            Enum<?> e = (Enum<?>)o;
            sb.append(e.getClass().getSimpleName())
              .append(DOT).append(e).append(DELIM);
        }
        sb.replace(sb.length()-2, sb.length(), CLOSE_B); // remove trailing delim
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return mySet.equals(((MixedEnumSet)o).mySet);
    }

    @Override
    public int hashCode() {
        return mySet.hashCode();
    }

    @Override
    public int size() {
        return mySet.size();
    }

    @Override
    public boolean isEmpty() {
        return mySet.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return mySet.contains(o);
    }

    @Override
    public Iterator<Enum<?>> iterator() {
        return new MyIterator(mySet.iterator());
    }

    @Override
    // Java 1.6 allows us to declare a narrower return type (was Object[])
    //  and still override the method
    public Enum<?>[] toArray() {
        return mySet.toArray(new Enum[mySet.size()]);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(Enum<?> anEnum) {
        return mySet.add(anEnum);
    }

    @Override
    public boolean remove(Object o) {
        return mySet.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return mySet.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Enum<?>> c) {
        return mySet.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return mySet.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return mySet.removeAll(c);
    }

    @Override
    public void clear() {
        mySet.clear();
    }

    //== Iterator implementation
    private static class MyIterator implements Iterator<Enum<?>> {
        private final Iterator<?> it;

        private MyIterator(Iterator<?> it) {
            this.it = it;
        }

        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Enum<?> next() {
            return (Enum<?>) it.next();
        }

        @Override
        public void remove() {
            it.remove();
        }
    }
    
}
