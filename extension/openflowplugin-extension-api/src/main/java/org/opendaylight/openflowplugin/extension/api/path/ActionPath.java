/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api.path;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.general.extension.grouping.Extension;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @author msunal
 *
 */
public enum ActionPath implements AugmentationPath {

//    Util.createIIdBuilderFor(Nodes.class).child(Node.class).augmentation(FlowCapableNode.class).child(Table.class).child(Flow.class).child(Instructions.class).child(Instruction.class)
    /**
     * openflowplugin-extension-general.yang
     * <br> Match augmentations block
     */
    NODES_NODE_TABLE_FLOW_INSTRUCTIONS_INSTRUCTION_WRITEACTIONSCASE_WRITEACTIONS_ACTION_ACTION_EXTENSIONLIST_EXTENSION(null);

    private final InstanceIdentifier<Extension> iid;

    private ActionPath(InstanceIdentifier<Extension> iid) {
        this.iid = iid;
    }

    @Override
    public final InstanceIdentifier<Extension> getInstanceIdentifier() {
        return iid;
    }

}
