/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.AddFlowInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.GeneralAugNxMatchRpcAddFlow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.extension.grouping.Extension;
import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.ExtensionList;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier.InstanceIdentifierBuilder;

/**
 * @author msunal
 *
 */
public enum AugmentationPath {

    ACTION_RPC_ADD_FLOW_INPUT(createIIdBuilderFor(AddFlowInput.class).child(Match.class)
            .augmentation(GeneralAugNxMatchRpcAddFlow.class).child(ExtensionList.class).child(Extension.class).build());

    private final InstanceIdentifier<Extension> iid;

    private AugmentationPath(InstanceIdentifier<Extension> iid) {
        this.iid = iid;
    }

    public final InstanceIdentifier<Extension> getInstanceIdentifier() {
        return iid;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <T extends DataObject> InstanceIdentifierBuilder<T> createIIdBuilderFor(Class<T> input) {
        return InstanceIdentifier.builder((Class) input);
    }

}
