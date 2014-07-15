/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.vendor.nicira.convertor.match;

import org.opendaylight.openflowplugin.extension.api.GroupingResolver;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifPacketIn;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifSwitchFlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchNotifUpdateFlowStats;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcUpdateFlowOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxAugMatchRpcUpdateFlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.nicira.match.rev140714.NxMatchRegGrouping;

import com.google.common.base.Optional;

/**
 * @author msunal
 *
 */
public class MatchUtil {
    
    private final static GroupingResolver<NxMatchRegGrouping, Extension> regResolver = new GroupingResolver<>(NxMatchRegGrouping.class);
    
    private MatchUtil() {
        regResolver.add(NxAugMatchRpcAddFlow.class);
        regResolver.add(NxAugMatchRpcRemoveFlow.class);
        regResolver.add(NxAugMatchRpcUpdateFlowOriginal.class);
        regResolver.add(NxAugMatchRpcUpdateFlowUpdated.class);
        regResolver.add(NxAugMatchNodesNodeTableFlow.class);
        regResolver.add(NxAugMatchNotifSwitchFlowRemoved.class);
        regResolver.add(NxAugMatchNotifPacketIn.class);
        regResolver.add(NxAugMatchNotifUpdateFlowStats.class);
    }
    
    public static Optional<NxMatchRegGrouping> getNxMatchRegGrouping(Extension data) {
        return regResolver.getExtension(data);
    }

}
