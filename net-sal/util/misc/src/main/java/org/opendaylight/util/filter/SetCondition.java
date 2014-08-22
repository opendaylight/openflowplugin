/*
 * (c) Copyright 2012 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.filter;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Set Condition. 
 *
 * @param <D> type of the attribute to apply the condition to.
 */
public class SetCondition<D> implements Serializable {
    private static final long serialVersionUID = 6509361704076315162L;

    private Set<D> values;
    private Mode mode;

    /**
     * Creates a new set condition.
     * 
     * @param values values to apply the condition against.
     * @param mode the mode to use the condition in.
     */
    public SetCondition(Set<D> values, Mode mode) {
        this.values = Collections.unmodifiableSet(new HashSet<D>(values));
        this.mode = mode;
    }

    /**
     * Gets the values associated with this condition.
     * 
     * @return the condition value set.
     */
    public Set<D> getValues() {
        return this.values;
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
        result = prime * result + ((values == null) ? 0 : values.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SetCondition<?> other = (SetCondition<?>) obj;
        if (mode != other.mode)
            return false;
        if (values == null) {
            if (other.values != null)
                return false;
        } else if (!values.equals(other.values))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append(this.getClass().getSimpleName());
        str.append('[');
        str.append("values=");
        str.append(this.values);
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
         * Elements that are in to the specified values are selected by the
         * condition.
         */
        IN,
        /**
         * Elements that are not in the specified values are selected by the
         * condition.
         */
        NOT_IN
    }
}
