/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.opendaylight.openflowplugin.openflow.md.core.SwitchConnectionDistinguisher;

/**
 * @author mirehak
 */
public class SwitchSessionKeyOFImpl implements SwitchConnectionDistinguisher {

    protected byte[] encodedId;
    private BigInteger datapathId;

    /**
     * default ctor
     */
    public SwitchSessionKeyOFImpl() {
        // do nothing
    }

    /**
     * Special constructor for situation where no datapathId available, do not
     * call {@link #initId()} on this instance otherwise id will be overwritten.
     *
     * @param encodedId
     */
    public SwitchSessionKeyOFImpl(byte[] encodedId) {
        this.encodedId = encodedId;
    }

    @Override
    public byte[] getId() {
        return encodedId;
    }

    /**
     * @param datapathId
     *            the datapathId to set
     */
    public void setDatapathId(BigInteger datapathId) {
        this.datapathId = datapathId;
    }

    /**
     * compute and set {@link #encodedId} based on {@link #datapathId} and
     * {@link #auxiliaryId}
     */
    public void initId() {
        try {
            MessageDigest medi = MessageDigest.getInstance("sha-1");
            extend(medi);
            encodedId = medi.digest();
        } catch (NoSuchAlgorithmException | NullPointerException e) {
            throw new IllegalArgumentException("can not proceed datapathId: "
                    + datapathId);
        }
    }

    /**
     * extend the content the hash sum is computed from
     * @param medi
     */
    protected void extend(MessageDigest medi) {
        medi.update(datapathId.toByteArray());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(encodedId);
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
        SwitchSessionKeyOFImpl other = (SwitchSessionKeyOFImpl) obj;
        if (!Arrays.equals(encodedId, other.encodedId))
            return false;
        return true;
    }

}
