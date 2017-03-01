/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.protocol.serialization.multipart;

import com.google.common.base.MoreObjects;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.md.util.OpenflowVersion;
import org.opendaylight.openflowplugin.openflow.md.util.InventoryDataServiceUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.queue.rev130925.QueueId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.multipart.request.multipart.request.body.MultipartRequestQueueStats;

public class MultipartRequestQueueStatsSerializer implements OFSerializer<MultipartRequestBody> {

    @Override
    public void serialize(final MultipartRequestBody multipartRequestBody, final ByteBuf byteBuf) {
        final MultipartRequestQueueStats multipartRequestQueueStats = MultipartRequestQueueStats
            .class
            .cast(multipartRequestBody);

        if (Objects.isNull(multipartRequestQueueStats.getNodeConnectorId())) {
            byteBuf.writeInt(OFConstants.OFPP_ANY.intValue());
        } else {
            byteBuf.writeInt(InventoryDataServiceUtil
                .portNumberfromNodeConnectorId(
                    OpenflowVersion.OF13,
                    multipartRequestQueueStats.getNodeConnectorId()).intValue());
        }

        byteBuf.writeInt(MoreObjects
            .firstNonNull(multipartRequestQueueStats.getQueueId(), new QueueId(OFConstants.OFPQ_ALL)).getValue().intValue());
    }

}
