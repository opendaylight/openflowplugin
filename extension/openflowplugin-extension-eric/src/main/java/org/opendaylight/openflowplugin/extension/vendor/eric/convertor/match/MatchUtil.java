/*
 * Copyright (c) 2019 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.eric.convertor.match;

import java.util.HashSet;
import java.util.Set;
import org.opendaylight.openflowplugin.extension.api.GroupingResolver;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.MatchField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.OxmClassBase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entries.grouping.MatchEntryBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflow.oxm.rev150225.match.entry.value.grouping.MatchEntryValue;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNodesNodeTableFlowApplyActionsSetField;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchPacketInMessage;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchRpcRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchRpcUpdateFlowOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricAugMatchRpcUpdateFlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricOfIcmpv6NdOptionsTypeGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.eric.match.rev180730.EricOfIcmpv6NdReservedGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yangtools.yang.binding.Augmentation;

/**
 * Match utilities.
 */
public final class MatchUtil {
    private static final Set<Class<? extends Augmentation<Extension>>> AUGMENTATIONS_OF_EXTENSION = new HashSet<>();
    public static final GroupingResolver<EricOfIcmpv6NdReservedGrouping, Extension> ICMPV6_ND_RESERVED_RESOLVER
            = new GroupingResolver<>(EricOfIcmpv6NdReservedGrouping.class);
    public static final GroupingResolver<EricOfIcmpv6NdOptionsTypeGrouping, Extension> ICMPV6_ND_OPTIONS_TYPE_RESOLVER
            = new GroupingResolver<>(EricOfIcmpv6NdOptionsTypeGrouping.class);

    static {
        AUGMENTATIONS_OF_EXTENSION.add(EricAugMatchRpcAddFlow.class);
        AUGMENTATIONS_OF_EXTENSION.add(EricAugMatchRpcRemoveFlow.class);
        AUGMENTATIONS_OF_EXTENSION.add(EricAugMatchRpcUpdateFlowOriginal.class);
        AUGMENTATIONS_OF_EXTENSION.add(EricAugMatchRpcUpdateFlowUpdated.class);
        AUGMENTATIONS_OF_EXTENSION.add(EricAugMatchNodesNodeTableFlow.class);
        AUGMENTATIONS_OF_EXTENSION.add(EricAugMatchNotifSwitchFlowRemoved.class);
        AUGMENTATIONS_OF_EXTENSION.add(EricAugMatchNotifPacketIn.class);
        AUGMENTATIONS_OF_EXTENSION.add(EricAugMatchPacketInMessage.class);
        AUGMENTATIONS_OF_EXTENSION.add(EricAugMatchNodesNodeTableFlowApplyActionsSetField.class);
        ICMPV6_ND_RESERVED_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
        ICMPV6_ND_OPTIONS_TYPE_RESOLVER.setAugmentations(AUGMENTATIONS_OF_EXTENSION);
    }

    private MatchUtil() {
    }

    public static MatchEntryBuilder createDefaultMatchEntryBuilder(Class<? extends MatchField> matchField,
                                                                   Class<? extends OxmClassBase> oxmClass,
                                                                   MatchEntryValue matchEntryValue) {
        MatchEntryBuilder matchEntryBuilder = new MatchEntryBuilder();
        matchEntryBuilder.setHasMask(false);
        matchEntryBuilder.setOxmMatchField(matchField);
        matchEntryBuilder.setOxmClass(oxmClass);
        matchEntryBuilder.setMatchEntryValue(matchEntryValue);
        return matchEntryBuilder;
    }
}
