/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.core;

import org.opendaylight.openflowplugin.api.openflow.connection.HandshakeContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;

/**
 * @author mirehak
 *
 */
public interface HandshakeListener {

    /**
     * @param featureOutput obtained
     * @param version negotiated
     */
    void onHandshakeSuccessful(GetFeaturesOutput featureOutput, Short version);

    /**
     * This method is called when handshake fails for some reason. It allows
     * all necessary cleanup operations.
     */
    void onHandshakeFailure();

    /**
     * @param handshakeContext
     */
    void setHandshakeContext(HandshakeContext handshakeContext);
}
