/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core;

import org.opendaylight.openflowplugin.api.openflow.md.core.ErrorHandler;
import org.opendaylight.openflowplugin.api.openflow.md.core.HandshakeListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.GetFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;

/**
 * @author mirehak
 *
 */
public interface HandshakeManager {

    /**
     * @return negotiated version
     */
    Short getVersion();
    
    /**
     * @return obtained connection features
     */
    GetFeaturesOutput getFeatures();

    /**
     * @param receivedHello from switch
     */
    void setReceivedHello(HelloMessage receivedHello);

    /**
     * @param errorHandler the errorHandler to set
     */
    void setErrorHandler(ErrorHandler errorHandler);

    /**
     * @param handshakeListener the handshakeListener to set
     */
    void setHandshakeListener(HandshakeListener handshakeListener);

    /**
     * @param isBitmapNegotiationEnable
     */
    void setUseVersionBitmap(boolean isBitmapNegotiationEnable);

    /**
     * process current handshake step
     */
    void shake();
}
