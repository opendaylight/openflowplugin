/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.statistics.services.direct;

import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.impl.datastore.MultipartWriterProvider;
import org.opendaylight.openflowplugin.openflow.md.core.sal.convertor.ConvertorExecutor;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.direct.statistics.rev160511.GetGroupStatisticsOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.MultipartRequestBody;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.multipart.request.group._case.MultipartRequestGroupBuilder;

/**
 * The Group direct statistics service.
 */
public abstract class AbstractGroupDirectStatisticsService<T extends OfHeader>
        extends AbstractDirectStatisticsService<GetGroupStatisticsInput, GetGroupStatisticsOutput, T> {

    protected AbstractGroupDirectStatisticsService(final RequestContextStack requestContextStack,
                                                   final DeviceContext deviceContext,
                                                   final ConvertorExecutor convertorExecutor,
                                                   final MultipartWriterProvider statisticsWriterProvider) {
        super(MultipartType.OFPMPGROUP, requestContextStack, deviceContext, convertorExecutor, statisticsWriterProvider);
    }

    @Override
    public MultipartRequestBody buildRequestBody(GetGroupStatisticsInput input) {
        final MultipartRequestGroupBuilder mprGroupBuild = new MultipartRequestGroupBuilder();

        if (input.getGroupId() != null) {
            mprGroupBuild.setGroupId(new GroupId(input.getGroupId().getValue()));
        } else {
            mprGroupBuild.setGroupId(new GroupId(OFConstants.OFPG_ALL));
        }

        return new MultipartRequestGroupCaseBuilder()
                .setMultipartRequestGroup(mprGroupBuild.build())
                .build();
    }

}
