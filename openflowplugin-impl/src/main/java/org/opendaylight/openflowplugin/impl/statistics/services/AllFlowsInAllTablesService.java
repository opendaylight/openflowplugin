/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.AbstractSimpleService;
import org.opendaylight.openflowplugin.impl.services.RequestInputUtils;
import org.opendaylight.openflowplugin.openflow.md.util.FlowCreatorUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.GetAllFlowsStatisticsFromAllFlowTablesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestFlowCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.flow._case.MultipartRequestFlowBuilder;

final class AllFlowsInAllTablesService extends AbstractSimpleService<GetAllFlowsStatisticsFromAllFlowTablesInput, GetAllFlowsStatisticsFromAllFlowTablesOutput> {
    private final MultipartRequestFlowCase flowCase;

    AllFlowsInAllTablesService(final RequestContextStack requestContextStack, final DeviceContext deviceContext) {
        super(requestContextStack, deviceContext, GetAllFlowsStatisticsFromAllFlowTablesOutput.class);

        final MultipartRequestFlowCaseBuilder multipartRequestFlowCaseBuilder = new MultipartRequestFlowCaseBuilder();
        final MultipartRequestFlowBuilder mprFlowRequestBuilder = new MultipartRequestFlowBuilder();
        mprFlowRequestBuilder.setTableId(OFConstants.OFPTT_ALL);
        mprFlowRequestBuilder.setOutPort(OFConstants.OFPP_ANY);
        mprFlowRequestBuilder.setOutGroup(OFConstants.OFPG_ANY);
        mprFlowRequestBuilder.setCookie(OFConstants.DEFAULT_COOKIE);
        mprFlowRequestBuilder.setCookieMask(OFConstants.DEFAULT_COOKIE_MASK);
        FlowCreatorUtil.setWildcardedFlowMatch(getVersion(), mprFlowRequestBuilder);
        multipartRequestFlowCaseBuilder.setMultipartRequestFlow(mprFlowRequestBuilder.build());

        flowCase = multipartRequestFlowCaseBuilder.build();
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetAllFlowsStatisticsFromAllFlowTablesInput input) {
        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPFLOW, xid.getValue(), getVersion());
        mprInput.setMultipartRequestBody(flowCase);

        return mprInput.build();
    }
}
