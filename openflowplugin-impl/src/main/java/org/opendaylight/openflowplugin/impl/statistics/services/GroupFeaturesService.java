/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.impl.statistics.services;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceContext;
import org.opendaylight.openflowplugin.api.openflow.device.RequestContextStack;
import org.opendaylight.openflowplugin.api.openflow.device.Xid;
import org.opendaylight.openflowplugin.impl.services.util.RequestInputUtils;
import org.opendaylight.openflowplugin.impl.services.util.ServiceException;
import org.opendaylight.openflowplugin.impl.statistics.services.compatibility.AbstractCompatibleStatService;
import org.opendaylight.openflowplugin.impl.util.GroupUtil;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.transaction.rev150304.TransactionId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GetGroupFeaturesOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupFeaturesUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.GroupFeaturesUpdatedBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.Chaining;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.ChainingChecks;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupAll;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupCapability;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupFf;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupIndirect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupSelect;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectLiveness;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.SelectWeight;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupCapabilities;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.common.types.rev130731.MultipartType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartReply;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.MultipartRequestInputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.OfHeader;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.MultipartReplyGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.reply.multipart.reply.body.multipart.reply.group.features._case.MultipartReplyGroupFeatures;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.protocol.rev130731.multipart.request.multipart.request.body.MultipartRequestGroupFeaturesCaseBuilder;

final class GroupFeaturesService
        extends AbstractCompatibleStatService<GetGroupFeaturesInput, GetGroupFeaturesOutput, GroupFeaturesUpdated> {

    private static final MultipartRequestGroupFeaturesCase GROUP_FEAT_CASE =
            new MultipartRequestGroupFeaturesCaseBuilder().build();

    public GroupFeaturesService(RequestContextStack requestContextStack, DeviceContext deviceContext, AtomicLong compatibilityXidSeed) {
        super(requestContextStack, deviceContext, compatibilityXidSeed);
    }

    @Override
    protected OfHeader buildRequest(final Xid xid, final GetGroupFeaturesInput input) throws ServiceException {
        final MultipartRequestInputBuilder mprInput = RequestInputUtils.createMultipartHeader(
                MultipartType.OFPMPGROUPFEATURES, xid.getValue(), getVersion());
        mprInput.setMultipartRequestBody(GROUP_FEAT_CASE);
        return mprInput.build();
    }

    @Override
    public GetGroupFeaturesOutput buildTxCapableResult(TransactionId emulatedTxId) {
        return new GetGroupFeaturesOutputBuilder().setTransactionId(emulatedTxId).build();
    }

    @Override
    public GroupFeaturesUpdated transformToNotification(List<MultipartReply> result, TransactionId emulatedTxId) {
        final int mpSize = result.size();
        Preconditions.checkArgument(mpSize == 1, "unexpected (!=1) mp-reply size received: {}", mpSize);

        GroupFeaturesUpdatedBuilder notification = new GroupFeaturesUpdatedBuilder();
        notification.setId(getDeviceInfo().getNodeId());
        notification.setMoreReplies(Boolean.FALSE);
        notification.setTransactionId(emulatedTxId);

        MultipartReplyGroupFeaturesCase caseBody = (MultipartReplyGroupFeaturesCase) result.get(0).getMultipartReplyBody();
        MultipartReplyGroupFeatures replyBody = caseBody.getMultipartReplyGroupFeatures();

        notification.setGroupTypesSupported(extractSupportedGroupTypes(replyBody.getTypes()));
        notification.setMaxGroups(replyBody.getMaxGroups());
        notification.setGroupCapabilitiesSupported(extractSupportedCapabilities(replyBody.getCapabilities()));
        notification.setActions(GroupUtil.extractGroupActionsSupportBitmap(replyBody.getActionsBitmap()));

        return notification.build();
    }

    @VisibleForTesting
    static List<Class<? extends GroupCapability>> extractSupportedCapabilities(GroupCapabilities capabilities) {
        List<Class<? extends GroupCapability>> supportedCapabilities = new ArrayList<>();

        if (capabilities.isOFPGFCCHAINING()) {
            supportedCapabilities.add(Chaining.class);
        }
        if (capabilities.isOFPGFCCHAININGCHECKS()) {
            supportedCapabilities.add(ChainingChecks.class);
        }
        if (capabilities.isOFPGFCSELECTLIVENESS()) {
            supportedCapabilities.add(SelectLiveness.class);
        }
        if (capabilities.isOFPGFCSELECTWEIGHT()) {
            supportedCapabilities.add(SelectWeight.class);
        }
        return supportedCapabilities;
    }

    @VisibleForTesting
    static List<Class<? extends GroupType>> extractSupportedGroupTypes(GroupTypes types) {
        List<Class<? extends GroupType>> supportedGroups = new ArrayList<>();

        if (types.isOFPGTALL()) {
            supportedGroups.add(GroupAll.class);
        }
        if (types.isOFPGTSELECT()) {
            supportedGroups.add(GroupSelect.class);
        }
        if (types.isOFPGTINDIRECT()) {
            supportedGroups.add(GroupIndirect.class);
        }
        if (types.isOFPGTFF()) {
            supportedGroups.add(GroupFf.class);
        }
        return supportedGroups;
    }
}
