/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md.core;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.HelloMessage;

public interface HandshakeManager {

    /**
     * Return negotiated version.
     *
     * @return negotiated version.
     */
    Short getVersion();

    /**
     * Process current handshake step.
     *
     * @param receivedHello message from device we need to act upon
     */
    void shake(HelloMessage receivedHello);
}
