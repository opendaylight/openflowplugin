/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.multipart.request.multipart.request.body.MultipartRequestPortStats;

public class MultipartRequestPortStatsSerializer implements OFSerializer<MultipartRequestBody> {

    private static final byte PADDING_IN_MULTIPART_REQUEST_PORTSTATS_BODY = 4;

    @Override
    public void serialize(final MultipartRequestBody multipartRequestBody, final ByteBuf byteBuf) {
        final MultipartRequestPortStats multipartRequestPortStats = MultipartRequestPortStats
            .class
            .cast(multipartRequestBody);

        byteBuf.writeInt(Math.toIntExact(InventoryDataServiceUtil
            .portNumberfromNodeConnectorId(
                OpenflowVersion.OF13,
                multipartRequestPortStats.getNodeConnectorId())));

        byteBuf.writeZero(PADDING_IN_MULTIPART_REQUEST_PORTSTATS_BODY);
    }

}
