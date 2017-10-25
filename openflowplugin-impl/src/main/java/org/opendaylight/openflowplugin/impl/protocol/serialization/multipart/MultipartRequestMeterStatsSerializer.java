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
import org.opendaylight.yang.gen.v1.urn.opendaylight.multipart.types.rev170112.multipart.request.MultipartRequestBody;

public class MultipartRequestMeterStatsSerializer implements OFSerializer<MultipartRequestBody> {

    private static final byte PADDING_IN_MULTIPART_REQUEST_METER_BODY = 4;

    @Override
    public void serialize(final MultipartRequestBody multipartRequestBody, final ByteBuf byteBuf) {
        final MultipartRequestMeterStats multipartRequestMeterStats = MultipartRequestMeterStats
            .class
            .cast(multipartRequestBody);

        byteBuf.writeInt(MoreObjects
            .firstNonNull(multipartRequestMeterStats.getMeterId(), new MeterId(OFConstants.OFPM_ALL))
                .getValue().intValue());
        byteBuf.writeZero(PADDING_IN_MULTIPART_REQUEST_METER_BODY);
    }

}
