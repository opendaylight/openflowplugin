/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart;

import static java.util.Objects.requireNonNullElse;

import io.netty.buffer.ByteBuf;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.request.multipart.request.body.MultipartRequestQueueStats;

public class MultipartRequestQueueStatsSerializer implements OFSerializer<MultipartRequestQueueStats> {
    @Override
    public void serialize(final MultipartRequestQueueStats multipartRequestQueueStats, final ByteBuf byteBuf) {
        if (multipartRequestQueueStats.getNodeConnectorId() == null) {
            byteBuf.writeInt(OFConstants.OFPP_ANY.intValue());
        } else {
            byteBuf.writeInt(InventoryDataServiceUtil
                .portNumberfromNodeConnectorId(
                    OpenflowVersion.OF13,
                    multipartRequestQueueStats.getNodeConnectorId()).intValue());
        }

        byteBuf.writeInt(requireNonNullElse(multipartRequestQueueStats.getQueueId(), new QueueId(OFConstants.OFPQ_ALL))
                .getValue().intValue());
    }
}
