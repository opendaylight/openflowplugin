/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.openflow.md.core.session;

import java.security.MessageDigest;

/**
 * @author mirehak
 */
public class SwitchConnectionCookieOFImpl extends SwitchSessionKeyOFImpl {

    private short auxiliaryId;

    /**
     * @param encodedId
     * @see {@link SwitchSessionKeyOFImpl#SwitchSessionKeyOFImpl(byte[])}
     */
    public SwitchConnectionCookieOFImpl(byte[] encodedId) {
        super(encodedId);
    }

    /**
     * default ctor
     */
    public SwitchConnectionCookieOFImpl() {
        // do nothing
    }

    /**
     * @param auxiliaryId
     *            the auxiliaryId to set
     */
    public void setAuxiliaryId(short auxiliaryId) {
        this.auxiliaryId = auxiliaryId;
    }

    @Override
    protected void extend(MessageDigest medi) {
        super.extend(medi);
        medi.update(new byte[] { (byte) (auxiliaryId >> 8), (byte) auxiliaryId });
    }

}
