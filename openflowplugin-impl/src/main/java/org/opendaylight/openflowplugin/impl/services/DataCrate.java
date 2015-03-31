/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import java.math.BigInteger;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;

public final class DataCrate<T> {
    private final BigInteger iDConnection;
    private final RequestContext<T> requestContext;
    private final FlowModInputBuilder flowModInputBuilder;

    public DataCrate(final DataCrateBuilder<T> builder) {
        iDConnection = builder.iDConnection;
        flowModInputBuilder = builder.flowModInputBuilder;
        requestContext = builder.requestContext;
    }

    public FlowModInputBuilder getFlowModInputBuilder() {
        return flowModInputBuilder;
    }

    public BigInteger getiDConnection() {
        return iDConnection;
    }

    public RequestContext<T> getRequestContext() {
        return requestContext;
    }
}
