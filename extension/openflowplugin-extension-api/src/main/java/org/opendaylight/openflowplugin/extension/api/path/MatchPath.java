/**
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.extension.api.path;

import org.opendaylight.yang.gen.v1.urn.opendaylight.openflowplugin.extension.general.rev140714.extension.grouping.Extension;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * @author msunal
 *
 */
public enum MatchPath implements AugmentationPath {

    ACTION_RPC_ADD_FLOW_INPUT(null);

    private final InstanceIdentifier<Extension> iid;

    private MatchPath(InstanceIdentifier<Extension> iid) {
        this.iid = iid;
    }

    @Override
    public final InstanceIdentifier<Extension> getInstanceIdentifier() {
        return iid;
    }

}
