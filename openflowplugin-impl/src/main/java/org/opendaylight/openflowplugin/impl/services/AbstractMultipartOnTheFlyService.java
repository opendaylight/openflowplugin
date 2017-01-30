/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.services;

import com.google.common.util.concurrent.FutureCallback;
import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.services.multilayer.MultiLayerFlowMultipartRequestOnTheFlyCallback;
import org.opendaylight.openflowplugin.impl.services.singlelayer.SingleLayerFlowMultipartRequestOnTheFlyCallback;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public abstract class AbstractMultipartOnTheFlyService<I, T extends OfHeader> extends AbstractMultipartService<I, T> {

    private final ConvertorExecutor convertorExecutor;
    private final MultipartWriterProvider statisticsWriterProvider;

    protected AbstractMultipartOnTheFlyService(final RequestContextStack requestContextStack,
                                               final DeviceContext deviceContext,
                                               final ConvertorExecutor convertorExecutor,
                                               final MultipartWriterProvider statisticsWriterProvider) {
        super(requestContextStack, deviceContext);
        this.convertorExecutor = convertorExecutor;
        this.statisticsWriterProvider = statisticsWriterProvider;
    }

    @Override
    protected final FutureCallback<OfHeader> createCallback(final RequestContext<List<T>> context, final Class<?> requestType) {
        return canUseSingleLayerSerialization()
            ? new SingleLayerFlowMultipartRequestOnTheFlyCallback<>(context, requestType, getDeviceContext(), getEventIdentifier(), statisticsWriterProvider)
            : new MultiLayerFlowMultipartRequestOnTheFlyCallback<>(context, requestType, getDeviceContext(), getEventIdentifier(), statisticsWriterProvider, convertorExecutor);
    }

}
