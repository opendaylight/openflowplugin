/*
 * (C) Copyright 2006 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.io.Serializable;

/**
 * Property.
 * 
 * @param <I> Type for the property identity
 * @param <E> Type for the property's value
 * @author Fabiel Zuniga
 */
public final class Property<I, E> implements Serializable {
    private static final long serialVersionUID = 3860869636328374546L;

    private final I identity;
    private final E value;

    private Property(I identity, E value) {
        if (identity == null) {
            throw new NullPointerException("identity cannot be null");
        }

        this.identity = identity;
        this.value = value;
    }

    /**
     * Creates a property.
     *
     * @param identity property identity
     * @param value property's value
     * @return a property
     */
    public static <I, E> Property<I, E> valueOf(I identity, E value) {
        return new Property<I, E>(identity, value);
    }

    /**
     * Gets the property identity.
     *
     * @return The property identity
     */
    public I getIdentity() {
        return this.identity;
    }

    /**
     * Gets the property value.
     *
     * @return The property value
     */
    public E getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.identity.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        Property<?, ?> other = (Property<?, ?>)obj;

        if (!this.identity.equals(other.identity)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        // DO NOT use ObjectToStringConverter because that will create a circular dependency
        StringBuilder str = new StringBuilder(32);
        str.append(getClass().getSimpleName());
        str.append("[");
        str.append("identity=");
        str.append(this.identity);
        str.append(", value=");
        str.append(this.value);
        str.append(']');
        return str.toString();
    }
}
