/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;


/**
 * lookup and register key for extension converters, basic case expects this to
 * correlate with input model type
 *
 * @param <T> type of key
 */
public class TypeVersionKey<T> {

    private final Class<? extends T> type;
    private final short ofVersion;

    public TypeVersionKey(Class<? extends T> type, short ofVersion) {
        this.type = type;
        this.ofVersion = ofVersion;
    }

    /**
     * @return key type
     */
    public Class<? extends T> getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ofVersion;
        result = prime * result + (type == null ? 0 : type.hashCode());
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
        TypeVersionKey<?> other = (TypeVersionKey<?>) obj;
        if (ofVersion != other.ofVersion) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [type=" + type + ", ofVersion=" + ofVersion + "]";
    }

}
