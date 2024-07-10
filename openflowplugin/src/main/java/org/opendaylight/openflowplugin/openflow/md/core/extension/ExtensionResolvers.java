/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import org.opendaylight.openflowplugin.extension.api.GroupingLooseResolver;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeGroupBucketsBucketActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowApplyActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlowWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifFlowsStatisticsUpdateApplyActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifFlowsStatisticsUpdateWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifGroupDescStatsUpdatedSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchPacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcAddFlowApplyActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcAddFlowWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcAddGroupSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcRemoveFlowApplyActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcRemoveFlowWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcRemoveGroupSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcTransmitPacketSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcUpdateFlowOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcUpdateFlowOriginalApplyActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcUpdateFlowOriginalWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcUpdateFlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcUpdateFlowUpdatedApplyActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcUpdateFlowUpdatedWriteActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcUpdateGroupOriginalSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcUpdateGroupUpdatedSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralExtensionListGrouping;

public final class ExtensionResolvers {
    private ExtensionResolvers() {
    }

    private static GroupingLooseResolver<GeneralExtensionListGrouping> matchExtensionResolver =
        new GroupingLooseResolver<>(GeneralExtensionListGrouping.class,
            GeneralAugMatchRpcAddFlow.class, GeneralAugMatchRpcRemoveFlow.class,
            GeneralAugMatchRpcUpdateFlowOriginal.class, GeneralAugMatchRpcUpdateFlowUpdated.class,
            GeneralAugMatchNodesNodeTableFlow.class, GeneralAugMatchNotifPacketIn.class,
            GeneralAugMatchNotifUpdateFlowStats.class, GeneralAugMatchNotifSwitchFlowRemoved.class,
            GeneralAugMatchPacketInMessage.class,
            // SetField extensions
            GeneralAugMatchRpcAddFlowWriteActionsSetField.class, GeneralAugMatchRpcAddFlowApplyActionsSetField.class,
            GeneralAugMatchRpcRemoveFlowWriteActionsSetField.class,
            GeneralAugMatchRpcRemoveFlowApplyActionsSetField.class,
            GeneralAugMatchRpcUpdateFlowOriginalWriteActionsSetField.class,
            GeneralAugMatchRpcUpdateFlowOriginalApplyActionsSetField.class,
            GeneralAugMatchRpcUpdateFlowUpdatedWriteActionsSetField.class,
            GeneralAugMatchRpcUpdateFlowUpdatedApplyActionsSetField.class, GeneralAugMatchRpcAddGroupSetField.class,
            GeneralAugMatchRpcRemoveGroupSetField.class, GeneralAugMatchRpcUpdateGroupOriginalSetField.class,
            GeneralAugMatchRpcUpdateGroupUpdatedSetField.class, GeneralAugMatchRpcTransmitPacketSetField.class,
            GeneralAugMatchNodesNodeTableFlowWriteActionsSetField.class,
            GeneralAugMatchNodesNodeTableFlowApplyActionsSetField.class,
            GeneralAugMatchNodesNodeGroupBucketsBucketActionsSetField.class,
            GeneralAugMatchNotifFlowsStatisticsUpdateWriteActionsSetField.class,
            GeneralAugMatchNotifFlowsStatisticsUpdateApplyActionsSetField.class,
            GeneralAugMatchNotifGroupDescStatsUpdatedSetField.class);

    /**
     * Returns the matchExtensionResolver (covers match rpcs and inventory augmentations).
     */
    public static GroupingLooseResolver<GeneralExtensionListGrouping> getMatchExtensionResolver() {
        return matchExtensionResolver;
    }
}
