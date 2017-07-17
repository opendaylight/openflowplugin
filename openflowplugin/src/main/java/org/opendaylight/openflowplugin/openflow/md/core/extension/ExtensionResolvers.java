/**
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

/**
 *
 */
public class ExtensionResolvers {

    private static GroupingLooseResolver<GeneralExtensionListGrouping> matchExtensionResolver =
            new GroupingLooseResolver<>(GeneralExtensionListGrouping.class)
                    .add(GeneralAugMatchRpcAddFlow.class)
                    .add(GeneralAugMatchRpcRemoveFlow.class)
                    .add(GeneralAugMatchRpcUpdateFlowOriginal.class)
                    .add(GeneralAugMatchRpcUpdateFlowUpdated.class)
                    .add(GeneralAugMatchNodesNodeTableFlow.class)
                    .add(GeneralAugMatchNotifPacketIn.class)
                    .add(GeneralAugMatchNotifUpdateFlowStats.class)
                    .add(GeneralAugMatchNotifSwitchFlowRemoved.class)
                    // SetField extensions
                    .add(GeneralAugMatchRpcAddFlowWriteActionsSetField.class)
                    .add(GeneralAugMatchRpcAddFlowApplyActionsSetField.class)
                    .add(GeneralAugMatchRpcRemoveFlowWriteActionsSetField.class)
                    .add(GeneralAugMatchRpcRemoveFlowApplyActionsSetField.class)
                    .add(GeneralAugMatchRpcUpdateFlowOriginalWriteActionsSetField.class)
                    .add(GeneralAugMatchRpcUpdateFlowOriginalApplyActionsSetField.class)
                    .add(GeneralAugMatchRpcUpdateFlowUpdatedWriteActionsSetField.class)
                    .add(GeneralAugMatchRpcUpdateFlowUpdatedApplyActionsSetField.class)
                    .add(GeneralAugMatchRpcAddGroupSetField.class)
                    .add(GeneralAugMatchRpcRemoveGroupSetField.class)
                    .add(GeneralAugMatchRpcUpdateGroupOriginalSetField.class)
                    .add(GeneralAugMatchRpcUpdateGroupUpdatedSetField.class)
                    .add(GeneralAugMatchRpcTransmitPacketSetField.class)
                    .add(GeneralAugMatchNodesNodeTableFlowWriteActionsSetField.class)
                    .add(GeneralAugMatchNodesNodeTableFlowApplyActionsSetField.class)
                    .add(GeneralAugMatchNodesNodeGroupBucketsBucketActionsSetField.class)
                    .add(GeneralAugMatchNotifFlowsStatisticsUpdateWriteActionsSetField.class)
                    .add(GeneralAugMatchNotifFlowsStatisticsUpdateApplyActionsSetField.class)
                    .add(GeneralAugMatchNotifGroupDescStatsUpdatedSetField.class);

    /**
     * @return the matchExtensionResolver (covers match rpcs and inventory augmentations)
     */
    public static GroupingLooseResolver<GeneralExtensionListGrouping> getMatchExtensionResolver() {
        return matchExtensionResolver;
    }

}
