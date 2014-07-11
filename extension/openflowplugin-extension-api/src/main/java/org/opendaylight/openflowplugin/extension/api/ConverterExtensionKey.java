/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yangtools.yang.binding.DataContainer;

/**
 * lookup and register key for extension converters, basic case expects this to correlate with input model type
 * 
 * @param <TYPE> type of key
 * 
 * TODO: we probably need version here and one more class in case that for example actions would be handled 
 * multiple times and differently
 */
public abstract class ConverterExtensionKey<TYPE extends DataContainer> {
    
    private Class<? extends TYPE> type;
    
    /**
     * @param type
     */
    public ConverterExtensionKey(Class<? extends TYPE> type) {
        this.type = type;
    }

    /**
     * @return key type
     */
    public Class<? extends TYPE> getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
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
        ConverterExtensionKey<?> other = (ConverterExtensionKey<?>) obj;
        if (type == null) {
            if (other.type != null)
                return false;
        } else if (!type.equals(other.type))
            return false;
        return true;
    }
    
    
}
