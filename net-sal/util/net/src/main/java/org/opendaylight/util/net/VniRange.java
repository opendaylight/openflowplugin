/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.net;

import java.lang.NullPointerException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Represents a range of Virtual Network Identifiers. Instances of this class
 * are created using the {@link #valueOf} factory method.
 * <p>
 * A string specification is used to define the VNI range, using the form:
 * <pre>
 *     {first}-{last}
 * </pre>
 * where <em>first</em> and <em>last</em> are values accepted by the
 * {@link Vni#valueOf(String)} method.
 * <p>
 * For example, the specification {@code "15-30"} declares that the range
 * of VNIs starts with 15 and ends with 30.
 *
 * @author Simon Hunt
 */
public final class VniRange extends AbstractRange<Vni> {

    /**
     * Constructs a VNI range from the given specification.
     *
     * @param spec the range specification
     */
    private VniRange(String spec) {
        super(spec);
    }

    @Override
    protected void parseSpec(String spec) {
        String[] items = splitSpec(spec);
        first = Vni.valueOf(items[FIRST]);
        last = Vni.valueOf(items[LAST]);
        if (first.compareTo(last) > 0)
            throw new IllegalArgumentException(E_BAD_SPEC + spec);
        size = last.toInt() - first.toInt() + 1;
    }

    @Override
    public Vni random() {
        int r = RANDOM.nextInt(size) + first.toInt();
        return Vni.valueOf(r);
    }

    @Override
    public Iterator<Vni> iterator() {
        return new VniIterator(first, last);
    }


    // == Implementation of the iterator

    private static class VniIterator extends RangeIterator<Vni> {

        protected VniIterator(Vni first, Vni last) {
            super(first, last);
        }

        @Override
        public Vni next() {
            if (!hasNext())
                throw new NoSuchElementException();

            Vni given = current;
            current = Vni.valueOf(given.toInt() + 1);
            return given;
        }
    }

    /**
     * Returns a Virtual Network Identifier (VNI) range instance, representing
     * a range of VNI values. The specification should be of the form:
     * <pre>
     *     "{first}-{last}"
     * </pre>
     * @param spec the range specification
     * @return the range instance
     */
    public static VniRange valueOf(String spec) {
        return new VniRange(spec);
    }

    /**
     * Returns a Virtual Network Identifier (VNI) range instance, representing a
     * range of VNI values from {@code first} to {@code last}, inclusively.
     *
     * @param first the first Vni in the range
     * @param last the last Vni in the range
     * @return the range instance
     * @throws NullPointerException if either parameter is null
     *
     */
    public static VniRange valueOf(Vni first, Vni last) {
        return new VniRange(first.toInt() + HYPHEN + last.toInt());
    }
}
