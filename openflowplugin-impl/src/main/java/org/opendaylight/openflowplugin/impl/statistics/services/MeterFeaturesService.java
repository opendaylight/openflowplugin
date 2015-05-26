/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.GetMeterFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestMeterFeaturesCaseBuilder;

final class MeterFeaturesService extends AbstractSimpleService<GetMeterFeaturesInput, GetMeterFeaturesOutput> {
    private static final MultipartRequestMeterFeaturesCase METER_FEATURES_CASE = new MultipartRequestMeterFeaturesCaseBuilder().build();

    protected MeterFeaturesService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, GetMeterFeaturesOutput.class);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetMeterFeaturesInput input) {
        MultipartRequestInputBuilder mprInput =
                RequestInputUtils.createMultipartHeader(MultipartType.OFPMPMETERFEATURES, xid.getValue(), getVersion());
        mprInput.setMultipartRequestBody(METER_FEATURES_CASE);
        return mprInput.build();
    }
}
