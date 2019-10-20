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
import org.opendaylight.openflowjava.protocol.api.extensibility.OFSerializer;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.multipart.request.multipart.request.body.MultipartRequestMeterStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;

public class MultipartRequestMeterStatsSerializer implements OFSerializer<MultipartRequestMeterStats> {

    private static final byte PADDING_IN_MULTIPART_REQUEST_METER_BODY = 4;

    @Override
    public void serialize(final MultipartRequestMeterStats multipartRequestMeterStats, final ByteBuf byteBuf) {
        byteBuf.writeInt(MoreObjects
            .firstNonNull(multipartRequestMeterStats.getStatMeterId(), new MeterId(OFConstants.OFPM_ALL))
                .getValue().intValue());
        byteBuf.writeZero(PADDING_IN_MULTIPART_REQUEST_METER_BODY);
    }

}
