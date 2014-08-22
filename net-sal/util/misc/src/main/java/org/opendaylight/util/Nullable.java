/*
 * (C) Copyright 2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.opendaylight.util.api.Distributable;

import java.io.Serializable;

/**
 * Represents a value type that can be assigned {@code null}.
 * <p>
 * This is actually not necessary in Java because all object type references
 * can be assigned {@code null}, however this class may be useful when an
 * object type cannot be {@code null} (For example, frameworks to send
 * messages between JVMs may require messages to be non-null).
 * <p>
 * This class is {@link Serializable}, however to accomplish Serialization the
 * generic argument should also be.
 * 
 * @param <T> type of the value
 * @author Fabiel Zuniga
 */
public final class Nullable<T> implements Distributable, Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final Nullable NULL = new Nullable(null);

    private final T value;

    private Nullable(T value) {
        this.value = value;
    }

    /**
     * Factory method that creates a Nullable with {@code null} value.
     * 
     * @return a Nullable with {@code null} value
     */
    @SuppressWarnings({ "cast", "unchecked" })
    public static <T> Nullable<T> nullValue() {
        return (Nullable<T>) NULL;
    }

    /**
     * Factory method that creates a Nullable with a non-null value.
     * 
     * @param value value
     * @return a Nullable with a non-null value
     */
    public static <T> Nullable<T> valueOf(T value) {
        return new Nullable<T>(value);
    }

    /**
     * Gets the value.
     * 
     * @return the value
     */
    public T getValue() {
        return this.value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.value == null) ? 0 : this.value.hashCode());
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

        if (!(obj instanceof Nullable)) {
            return false;
        }

        Nullable<?> other = (Nullable<?>) obj;

        if (this.value == null) {
            if (other.value != null) {
                return false;
            }
        }
        else if (!this.value.equals(other.value)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return ObjectToStringConverter.toString(
                this,
                Property.valueOf("value", this.value)
        );
    }
}
