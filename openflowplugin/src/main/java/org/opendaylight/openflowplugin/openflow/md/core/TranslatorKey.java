/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

/**
 * @author mirehak
 */
public class TranslatorKey {
    
    private int version;
    private String messageClass;
    
    /**
     * @param version
     * @param messageClass
     */
    public TranslatorKey(int version, String messageClass) {
        this.version = version;
        this.messageClass = messageClass;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((messageClass == null) ? 0 : messageClass.hashCode());
        result = prime * result + version;
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
        TranslatorKey other = (TranslatorKey) obj;
        if (messageClass == null) {
            if (other.messageClass != null)
                return false;
        } else if (!messageClass.equals(other.messageClass))
            return false;
        if (version != other.version)
            return false;
        return true;
    }

}
