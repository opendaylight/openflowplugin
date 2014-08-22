/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.api;

import java.io.Serializable;

/**
 * Identifier.
 * 
 * @param <T> type of the object the identifier identifies.
 * @param <I> type of the identifier value. It should be an immutable type. It
 *        is critical this type implements equals() and hashCode() correctly.
 * @author Fabiel Zuniga
 */
public final class Id<T, I extends Serializable> implements Distributable,
        Serializable {

    private static final long serialVersionUID = -4630408631022927244L;

    private final I value;

    private Id(I value) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }

        this.value = value;
    }

    /**
     * Creates an identifier with the given value.
     *
     * @param value value.
     * @return an identifier.
     */
    public static <T, I extends Serializable> Id<T, I> valueOf(I value) {
        return new Id<T, I>(value);
    }

    /**
     * Gets the internal representation.
     *
     * @return internal representation.
     */
    public I getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
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

        Id<?, ?> other = (Id<?, ?>) obj;
        return this.value.equals(other.value);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[value=" + this.value.toString()
                + ']';
    }
}
