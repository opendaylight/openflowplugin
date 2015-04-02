/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services;

import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import java.math.BigInteger;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.FlowModInputBuilder;

public class DataCrateBuilder<T> {
    BigInteger iDConnection;
    RequestContext<T> requestContext;
    FlowModInputBuilder flowModInputBuilder;

    public DataCrateBuilder() {
    }

    public DataCrateBuilder<T> setFlowModInputBuilder(final FlowModInputBuilder flowModInputBuilder) {
        this.flowModInputBuilder = flowModInputBuilder;
        return this;
    }

    public DataCrateBuilder<T> setiDConnection(final BigInteger iDConnection) {
        this.iDConnection = iDConnection;
        return this;
    }

    public DataCrateBuilder<T> setRequestContext(final RequestContext<T> requestContext) {
        this.requestContext = requestContext;
        return this;
    }

    public static <T> DataCrateBuilder<T> builder() {
        return new DataCrateBuilder<>();
    }

    public DataCrate<T> build() {
        return new DataCrate<>(this);
    }



}
