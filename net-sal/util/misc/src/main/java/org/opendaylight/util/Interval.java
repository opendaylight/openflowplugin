/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.io.Serializable;

import static org.opendaylight.util.Interval.Type.*;

/**
 * Intervals (of real numbers ) can be classified into different types. Let
 * {@code a} and {@code b} be real numbers where with {@code a < b}:
 * <ul>
 * <li>Empty: <code> (a, a) = (a, a] = [a, a) = {} = empty</code>.
 * {@code [b, a]} is considered as an empty interval in the literature, but
 * this class do not support it
 * <li>Degenerate: <code>[a, a] = {a}</code>
 * <li>Open: <code>(a, b) = {x | a < x < b}</code>
 * <li>Closed: <code>[a, b] = {x | a <= x <= b}</code>
 * <li>Left-closed, right-open: <code>[a, b) = {x | a <= x < b}</code>
 * <li>Left-open, right-closed: <code>(a, b] = {x | a < x <= b}</code>
 * <li>Left open right unbounded: <code>(a, infinite) = {x | x > a}</code>
 * <li>Left closed right unbounded: <code>[a, infinite) = {x | x >= a}</code>
 * <li>Left unbounded right open: <code>(infinite, b) = {x | x < b}</code>
 * <li>Left unbounded right closed: <code>(infinite, b] = {x | x <= b}</code>
 * <li>Unbounded: <code>(infinite, infinite) = {x | x is a real number}</code>
 * </ul>
 * 
 * @param <D> Interval domain.
 */
public final class Interval<D extends Comparable<D>> implements Serializable {

    private static final long serialVersionUID = -7544040224662924268L;

    private D leftEndpoint;

    private D rightEndpoint;

    private Type type;

    public static enum Type {
        /**
         * An open interval does not include its end points, and is indicated
         * with parentheses. For example {@code (0,1)} means greater than
         * {@code 0} and less than {@code 1}.
         */
        OPEN,
        /**
         * A closed interval includes its end points, and is denoted with
         * square brackets. For example {@code [0,1]} means greater than or
         * equal to {@code 0} and less than or equal to {@code 1}.
         */
        CLOSED,
        /**
         * An interval is said to be left-bounded or right-bounded if there is
         * some real number that is, respectively, smaller than or larger than
         * all its elements. An interval is said to be bounded if it is both
         * left- and right-bounded; and is said to be unbounded otherwise.
         */
        UNBOUNDED,
        /** Left closed right open */
        LEFT_CLOSED_RIGHT_OPEN,
        /** Left open right closed. */
        LEFT_OPEN_RIGHT_CLOSED,
        /** Left open right unbounded */
        LEFT_OPEN_RIGHT_UNBOUNDED,
        /** Left closed right unbounded */
        LEFT_CLOSED_RIGHT_UNBOUNDED,
        /** Left unbounded right open */
        LEFT_UNBOUNDED_RIGHT_OPEN,
        /** Left unbounded right closed */
        LEFT_UNBOUNDED_RIGHT_CLOSED
    }

    /**
     * Creates a open interval: <code>(a, b) = {x | a < x < b}</code>
     * 
     * @param leftEndpoint Left end point.
     * @param rightEndpoint Right end point.
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> valueOfOpen(T leftEndpoint,
                                                                    T rightEndpoint) {
        return new Interval<T>(leftEndpoint, rightEndpoint, OPEN);
    }

    /**
     * Creates a closed interval: <code>[a, b] = {x | a <= x <= b}</code>
     * 
     * @param leftEndpoint Left end point.
     * @param rightEndpoint Right end point.
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> valueOfClosed(T leftEndpoint,
                                                                      T rightEndpoint) {
        return new Interval<T>(leftEndpoint, rightEndpoint, CLOSED);
    }

    /**
     * Creates a left-closed right-open interval:
     * <code>(a, b] = {x | a < x <= b}</code>
     * 
     * @param leftEndpoint Left end point.
     * @param rightEndpoint Right end point.
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> valueOfLeftClosedRightOpen(T leftEndpoint,
                                                                                   T rightEndpoint) {
        return new Interval<T>(leftEndpoint, rightEndpoint,
                               LEFT_CLOSED_RIGHT_OPEN);
    }

    /**
     * Creates a left-open right-closed interval:
     * <code>(a, b] = {x | a <= x < b}</code>
     * 
     * @param leftEndpoint Left end point.
     * @param rightEndpoint Right end point.
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> valueOfLeftOpenRightClosed(T leftEndpoint,
                                                                                   T rightEndpoint) {
        return new Interval<T>(leftEndpoint, rightEndpoint,
                               LEFT_OPEN_RIGHT_CLOSED);
    }

    /**
     * Creates a left open right unbounded interval:
     * <code>(a, infinite) = {x | x > a}</code>
     * 
     * @param leftEndpoint Left end point.
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> valueOfLeftOpenRightUnbounded(T leftEndpoint) {
        return new Interval<T>(leftEndpoint, null, LEFT_OPEN_RIGHT_UNBOUNDED);
    }

    /**
     * Creates a Left closed right unbounded interval:
     * <code>[a, infinite) = {x | x >= a}</code>
     * 
     * @param leftEndpoint Left end point.
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> valueOfLeftClosedRightUnbounded(T leftEndpoint) {
        return new Interval<T>(leftEndpoint, null, LEFT_CLOSED_RIGHT_UNBOUNDED);
    }

    /**
     * Creates a left unbounded right open interval:
     * <code>(infinite, b) = {x | x < b}</code>
     * 
     * @param rightEndpoint Right end point.
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> valueOfLeftUnboundedRightOpen(T rightEndpoint) {
        return new Interval<T>(null, rightEndpoint, LEFT_UNBOUNDED_RIGHT_OPEN);
    }

    /**
     * Creates a left unbounded right closed interval:
     * <code>(infinite, b] = {x | x <= b}</code>
     * 
     * @param rightEndpoint Right end point.
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> valueOfLeftUnboundedRightClosed(T rightEndpoint) {
        return new Interval<T>(null, rightEndpoint, LEFT_UNBOUNDED_RIGHT_CLOSED);
    }

    /**
     * Creates an unbounded interval:
     * <code>(infinite, infinite) = {x | x is a real number}</code>
     * 
     * @return interval
     */
    public static <T extends Comparable<T>> Interval<T> valueOfUnbounded() {
        return new Interval<T>(null, null, UNBOUNDED);
    }

    /**
     * Creates an open-type interval based on the nullability of {@code leftEndpoint} and
     * {@code rightEndpoint}:
     * <ul>
     * <li>Open: if neither {@code leftEndpoint} and {@code rightEndpoint} are {@code null}</li>
     * <li>Left unbounded right open: if {@code leftEndpoint} is {@code null} but
     * {@code rightEndpoint} isn't</li>
     * <li>Left open right unbounded: if {@code leftEndpoint} is not {@code null} but
     * {@code rightEndpoint} is</li>
     * <li>Unbounded: if both {@code leftEndpoint} and {@code rightEndpoint} are {@code null}</li>
     * </ul>
     * 
     * @param leftEndpoint left endpoint
     * @param rightEndpoint right endpoint
     * @return one of the open-type intervals
     * @throws IllegalArgumentException if neither {@code leftEndpoint} and {@code rightEndpoint}
     *             are {@code null} and {@code leftEndpoint} is be greater than
     *             {@code rightEndpoint}
     */
    public static <T extends Comparable<T>> Interval<T> createOpen(T leftEndpoint, T rightEndpoint)
            throws IllegalArgumentException {
        Interval<T> interval = null;
        if (leftEndpoint == null) {
            if (rightEndpoint == null) {
                interval = Interval.valueOfUnbounded();
            }
            else {
                interval = Interval.valueOfLeftUnboundedRightOpen(rightEndpoint);
            }
        }
        else if (rightEndpoint == null) {
            interval = Interval.valueOfLeftOpenRightUnbounded(leftEndpoint);
        }
        else {
            interval = Interval.valueOfOpen(leftEndpoint, rightEndpoint);
        }

        return interval;
    }

    /**
     * Creates a closed-type interval based on the nullability of {@code leftEndpoint} and
     * {@code rightEndpoint}:
     * <ul>
     * <li>Closed: if neither {@code leftEndpoint} and {@code rightEndpoint} are {@code null}</li>
     * <li>Left unbounded right closed: if {@code leftEndpoint} is {@code null} but
     * {@code rightEndpoint} isn't</li>
     * <li>Left closed right unbounded: if {@code leftEndpoint} is not {@code null} but
     * {@code rightEndpoint} is</li>
     * <li>Unbounded: if both {@code leftEndpoint} and {@code rightEndpoint} are {@code null}</li>
     * </ul>
     * 
     * @param leftEndpoint left endpoint
     * @param rightEndpoint right endpoint
     * @return one of the closed-type intervals
     * @throws IllegalArgumentException if neither {@code leftEndpoint} and {@code rightEndpoint}
     *             are {@code null} and {@code leftEndpoint} is be greater than
     *             {@code rightEndpoint}
     */
    public static <T extends Comparable<T>> Interval<T> createClosed(T leftEndpoint, T rightEndpoint)
            throws IllegalArgumentException {
        Interval<T> interval = null;
        if (leftEndpoint == null) {
            if (rightEndpoint == null) {
                interval = Interval.valueOfUnbounded();
            }
            else {
                interval = Interval.valueOfLeftUnboundedRightClosed(rightEndpoint);
            }
        }
        else if (rightEndpoint == null) {
            interval = Interval.valueOfLeftClosedRightUnbounded(leftEndpoint);
        }
        else {
            interval = Interval.valueOfClosed(leftEndpoint, rightEndpoint);
        }

        return interval;
    }

    /**
     * Constructs an interval.
     * 
     * @param leftEndpoint Left end point.
     * @param rightEndpoint Right end point.
     * @param type Interval type.
     * @throws NullPointerException If interval type is null.
     * @throws IllegalArgumentException If interval validation fails based on
     *         the interval type.
     */
    protected Interval(D leftEndpoint, D rightEndpoint, Type type)
            throws NullPointerException, IllegalArgumentException {

        if (type == null)
            throw new NullPointerException("Type cannot be null");

        switch (type) {

        case OPEN:
        case CLOSED:
        case LEFT_CLOSED_RIGHT_OPEN:
        case LEFT_OPEN_RIGHT_CLOSED:

            if (leftEndpoint == null)
                throw new IllegalArgumentException(
                                                   "leftEndpoint cannot be null");

            if (rightEndpoint == null)
                throw new IllegalArgumentException(
                                                   "rightEndpoint cannot be null");

            if (leftEndpoint.compareTo(rightEndpoint) > 0)
                throw new IllegalArgumentException(
                                                   "leftEndpoint cannot be greater than rightEndpoint");

            break;

        case LEFT_OPEN_RIGHT_UNBOUNDED:
        case LEFT_CLOSED_RIGHT_UNBOUNDED:

            if (leftEndpoint == null)
                throw new IllegalArgumentException(
                                                   "leftEndpoint cannot be null");

            break;

        case LEFT_UNBOUNDED_RIGHT_OPEN:
        case LEFT_UNBOUNDED_RIGHT_CLOSED:

            if (rightEndpoint == null)
                throw new IllegalArgumentException(
                                                   "rightEndpoint cannot be null");
            break;

        case UNBOUNDED:
            // Nothing to validate
            break;
        }

        this.type = type;
        this.leftEndpoint = leftEndpoint;
        this.rightEndpoint = rightEndpoint;
    }

    /**
     * Gets the interval left end point.
     * 
     * @return the interval left end point or null
     */
    public D getLeftEndpoint() {
        return isLeftUnbounded() ? null : this.leftEndpoint;
    }

    /**
     * Gets the interval right end point.
     *
     * @return the interval right end point or null
     */
    public D getRightEndpoint() {
        return isRigthUnbounded() ? null : this.rightEndpoint;
    }

    /**
     * Gets the interval type.
     *
     * @return the interval type.
     */
    public Type getType() {
        return this.type;
    }

    /**
     * Verifies whether the given element belongs to the interval.
     *
     * @param element Element to verify.
     * @return {@code true} if {@code element} belongs is inside the interval,
     *         {@code false} otherwise.
     */
    public boolean contains(D element) {
        boolean contained = false;

        if (element != null) {
            switch (this.type) {

                case OPEN:
                    contained = element.compareTo(this.leftEndpoint) > 0
                            && element.compareTo(this.rightEndpoint) < 0;
                    break;

                case CLOSED:
                    contained = element.compareTo(this.leftEndpoint) >= 0
                            && element.compareTo(this.rightEndpoint) <= 0;
                    break;

                case LEFT_CLOSED_RIGHT_OPEN:
                    contained = element.compareTo(this.leftEndpoint) >= 0
                            && element.compareTo(this.rightEndpoint) < 0;
                    break;

                case LEFT_OPEN_RIGHT_CLOSED:
                    contained = element.compareTo(this.leftEndpoint) > 0
                            && element.compareTo(this.rightEndpoint) <= 0;
                    break;

                case LEFT_OPEN_RIGHT_UNBOUNDED:
                    contained = element.compareTo(this.leftEndpoint) > 0;
                    break;

                case LEFT_CLOSED_RIGHT_UNBOUNDED:
                    contained = element.compareTo(this.leftEndpoint) >= 0;
                    break;

                case LEFT_UNBOUNDED_RIGHT_OPEN:
                    contained = element.compareTo(this.rightEndpoint) < 0;
                    break;

                case LEFT_UNBOUNDED_RIGHT_CLOSED:
                    contained = element.compareTo(this.rightEndpoint) <= 0;
                    break;

                case UNBOUNDED:
                    contained = true;
                    break;
            }
        }

        return contained;
    }

    /**
     * Returns whether this interval is empty or not:
     * <p>
     * a, b are real numbers with a < b
     * <p>
     * empty: <code>[b, a] = (a, a) = [a, a) = (a, a] = {}</code>
     *
     * @return True if this interval is empty, false otherwise.
     */
    public boolean isEmpty() {
        return !isLeftUnbounded() && !isRigthUnbounded()
                && this.leftEndpoint.compareTo(this.rightEndpoint) == 0;
    }

    private boolean isLeftUnbounded() {
        return this.type == UNBOUNDED || this.type == LEFT_UNBOUNDED_RIGHT_OPEN
                || this.type == LEFT_UNBOUNDED_RIGHT_CLOSED;
    }

    private boolean isRigthUnbounded() {
        return this.type == UNBOUNDED || this.type == LEFT_OPEN_RIGHT_UNBOUNDED
                || this.type == LEFT_CLOSED_RIGHT_UNBOUNDED;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;

        if (!(obj instanceof Interval<?>))
            return false;

        Interval<?> other = (Interval<?>) obj;

        if (this.type != other.type)
            return false;

        if (this.leftEndpoint == null) {
            if (other.leftEndpoint != null) {
                return false;
            }
        } else if (!this.leftEndpoint.equals(other.leftEndpoint)) {
            return false;
        }

        if (this.rightEndpoint == null) {
            if (other.rightEndpoint != null) {
                return false;
            }
        } else if (!this.rightEndpoint.equals(other.rightEndpoint)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = this.type.hashCode();
        result = prime * result
                + ((this.leftEndpoint == null) ? 0 : this.leftEndpoint
                .hashCode());
        result = prime * result
                + ((this.rightEndpoint == null) ? 0 : this.rightEndpoint
                .hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append(this.getClass().getSimpleName());
        str.append('[');
        str.append("leftEndpoint=");
        str.append(this.leftEndpoint);
        str.append(", rightEndpoint=");
        str.append(this.rightEndpoint);
        str.append(", type=");
        str.append(this.type);
        str.append(']');

        return str.toString();
    }
}
