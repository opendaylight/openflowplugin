/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import org.opendaylight.openflowplugin.api.openflow.md.core.SwitchConnectionDistinguisher;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

/**
 * @author mirehak
 */
public class SwitchConnectionCookieOFImpl implements SwitchConnectionDistinguisher {

    private short auxiliaryId;
    private long cookie;

    /**
     * @param cookie switch connection cookie
     */
    public SwitchConnectionCookieOFImpl(long cookie) {
        this.cookie = cookie;
    }

    /**
     * default ctor
     */
    public SwitchConnectionCookieOFImpl() {
        // NOOP
    }

    /**
     * @param auxiliaryId  the auxiliaryId to set
     */
    public void setAuxiliaryId(short auxiliaryId) {
        this.auxiliaryId = auxiliaryId;
    }
    
    /**
     * compute pseudorandom key unique for given seed and {@link #auxiliaryId}
     * @param seed random int but fixed per session
     */
    public void init(int seed) {
        if (auxiliaryId <= 0) {
            throw new IllegalStateException("auxiliaryId must be greater than 0");
        }
        
        HashFunction mm32Hf = Hashing.murmur3_32(seed);
        Hasher hasher = mm32Hf.newHasher(8);
        hasher.putInt(auxiliaryId);
        long hash = 0xFFFFFFFFL & hasher.hash().asInt();
        cookie = (auxiliaryId << 24) | (hash >> 8);
    }
    
    @Override
    public long getCookie() {
        return cookie;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (cookie ^ (cookie >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SwitchConnectionCookieOFImpl other = (SwitchConnectionCookieOFImpl) obj;
        if (cookie != other.cookie)
            return false;
        return true;
    }
    
    

}
