/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.openflow.md.core.extension;

import org.opendaylight.openflowplugin.extension.api.GroupingLooseResolver;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchNodesNodeTableFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcRemoveFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcUpdateFlowOriginal;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugMatchRpcUpdateFlowUpdated;
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
            .add(GeneralAugMatchNodesNodeTableFlow.class);
    
    /**
     * @return the matchExtensionResolver (covers match rpcs and inventory augmentations)
     */
    public static GroupingLooseResolver<GeneralExtensionListGrouping> getMatchExtensionResolver() {
        return matchExtensionResolver;
    }

}
