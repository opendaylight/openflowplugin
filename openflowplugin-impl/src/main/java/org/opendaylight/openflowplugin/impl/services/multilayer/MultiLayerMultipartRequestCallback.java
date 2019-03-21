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
import org.opendaylight.openflowplugin.impl.services.AbstractMultipartRequestCallback;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartRequestFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;

public class MultiLayerMultipartRequestCallback<T extends OfHeader> extends AbstractMultipartRequestCallback<T> {

    public MultiLayerMultipartRequestCallback(RequestContext<List<T>> context, Class<?> requestType,
                                              DeviceContext deviceContext,
                                              EventIdentifier eventIdentifier) {
        super(context, requestType, deviceContext, eventIdentifier);
    }

    @Override
    protected boolean isMultipart(OfHeader result) {
        return result instanceof MultipartReply;
    }

    @Override
    protected boolean isReqMore(T result) {
        final MultipartRequestFlags flags = ((MultipartReply) result).getFlags();
        return flags != null && flags.isOFPMPFREQMORE();
    }
}
