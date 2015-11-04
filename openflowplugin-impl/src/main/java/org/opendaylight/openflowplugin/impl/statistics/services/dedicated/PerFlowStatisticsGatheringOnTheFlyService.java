/*
 * Copyright (c) 2015 Hewlett Packard Enterprise Development LP and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.dedicated;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.api.openflow.statistics.ofpspecific.StatisticsGatherer;
import org.opendaylight.openflowplugin.impl.common.MultipartRequestInputFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;

import java.math.BigInteger;

/**
 * collects per flow statistics and processes them on the fly
 */
public class PerFlowStatisticsGatheringOnTheFlyService extends StatisticsGatheringOnTheFlyService implements StatisticsGatherer {
    private static final BigInteger cookie = new BigInteger("9223372036854775808" /* 1 << 63 */);

    public PerFlowStatisticsGatheringOnTheFlyService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final MultipartType input) {
        if (MultipartType.OFPMPFLOW.equals(input)) {
            MultipartRequestBody multipartRequestBody = MultipartRequestInputFactory
                    .makeDefaultMultipartRequestFlowCase(getVersion(), cookie, cookie);
            return MultipartRequestInputFactory.makeMultipartRequestInput(xid.getValue(), getVersion(), input,
                    multipartRequestBody);
        } else {
            return MultipartRequestInputFactory.makeMultipartRequestInput(xid.getValue(), getVersion(), input);
        }
    }
}
