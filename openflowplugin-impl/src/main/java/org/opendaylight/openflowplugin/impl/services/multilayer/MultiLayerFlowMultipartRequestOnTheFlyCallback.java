/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.services.multilayer;

import java.util.List;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContext;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.EventIdentifier;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartRequestOnTheFlyCallback;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public class MultiLayerFlowMultipartRequestOnTheFlyCallback<T extends OfHeader>
                                                        extends AbstractMultipartRequestOnTheFlyCallback<T> {

    public MultiLayerFlowMultipartRequestOnTheFlyCallback(final RequestContext<List<T>> context,
                                                          final Class<?> requestType,
                                                          final DeviceContext deviceContext,
                                                          final EventIdentifier eventIdentifier,
                                                          final MultipartWriterProvider statisticsWriterProvider,
                                                          final ConvertorExecutor convertorExecutor) {
        super(context, requestType, deviceContext, eventIdentifier, statisticsWriterProvider, convertorExecutor);
    }

    @Override
    protected boolean isMultipart(final OfHeader result) {
        return result instanceof MultipartReply
            && ((MultipartReply) result).getType().equals(getMultipartType());
    }

    @Override
    protected boolean isReqMore(final T result) {
        return ((MultipartReply) result).getFlags().getOFPMPFREQMORE();
    }

    @Override
    protected MultipartType getMultipartType() {
        return MultipartType.OFPMPFLOW;
    }
}
