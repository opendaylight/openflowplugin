/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.filter;

import java.io.Serializable;

/**
 * Comparable Condition. 
 *
 * @param <D> type of the attribute to apply the condition to.
 */
public class ComparableCondition<D extends Comparable<D>> implements Serializable {
    private static final long serialVersionUID = -6992443867652620399L;

    private D value;
    private Mode mode;

    /**
     * Creates a new comparable condition.
     * 
     * @param value value to apply the condition against.
     * @param mode the mode to use the condition in.
     */
    public ComparableCondition(D value, Mode mode) {
        this.value = value;
        this.mode = mode;
    }

    /**
     * Gets the value associated with this condition.
     * 
     * @return the condition value.
     */
    public D getValue() {
        return this.value;
    }

    /**
     * Gets the mode related to this condition.
     * 
     * @return the condition mode of operation.
     */
    public Mode getMode() {
        return this.mode;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof ComparableCondition))
            return false;
        ComparableCondition<?> other = (ComparableCondition<?>) obj;
        if (mode != other.mode)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append(this.getClass().getSimpleName());
        str.append('[');
        str.append("value=");
        str.append(this.value);
        str.append(", mode=");
        str.append(this.mode);
        str.append(']');

        return str.toString();
    }

    /**
     * Condition mode.
     */
    public static enum Mode {
        /**
         * Elements that are less than the specified value are selected by the
         * condition.
         */
        LESS_THAN,
        /**
         * Elements that are less than or equal to the specified value are
         * selected by the condition.
         */
        LESS_THAN_OR_EQUAL_TO,
        /**
         * Elements that are equal to the specified value are selected by the
         * condition.
         */
        EQUAL,
        /**
         * Elements that are greater than or equal to the specified value are
         * selected by the condition.
         */
        GREATER_THAN_OR_EQUAL_TO,
        /**
         * Elements that are greater than the specified value are selected by
         * the condition.
         */
        GREATER_THAN
    }
}
