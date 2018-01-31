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
 * Enumerates the match paths.
 *
 * @author msunal
 */
public enum MatchPath implements AugmentationPath {

    /**
     * openflowplugin-extension-general.yang.
     * <pre>
     * module: sal-flow
     * notifications:
     *    +---n switch-flow-removed
     *    |  +--ro match
     * </pre>
     */
    SWITCH_FLOW_REMOVED_MATCH(null),

    /**
     * openflowplugin-extension-general.yang.
     * <pre>
     * module: packet-processing
     * notifications:
     *    +---n packet-received
     *       +--ro match
     * </pre>
     */
    PACKET_RECEIVED_MATCH(null),

    /**
     * openflowplugin-extension-general.yang.
     * <pre>
     * module: packet-processing
     * container:
     *    +---n packet-in-message
     *       +--ro match
     * </pre>
     */
    PACKET_IN_MESSAGE_MATCH(null),

    /**
     * openflowplugin-extension-general.yang.
     * <pre>
     * module: opendaylight-flow-statistics
     * notifications:
     *    +---n flows-statistics-update
     *    |  +--ro flow-and-statistics-map-list* [flow-id]
     *    |  |  +--ro match
     * </pre>
     */
    FLOWS_STATISTICS_UPDATE_MATCH(null),

    /**
     * openflowplugin-extension-general.yang.
     * <pre>
     * module: opendaylight-direct-statistics
     * rpc:
     *    +---n get-flow-statistics
     *    |  +--ro flow-and-statistics-map-list* [flow-id]
     *    |  |  +--ro match
     * </pre>
     */
    FLOWS_STATISTICS_RPC_MATCH(null);

    private final InstanceIdentifier<Extension> iid;

    MatchPath(InstanceIdentifier<Extension> iid) {
        this.iid = iid;
    }

    @Override
    public final InstanceIdentifier<Extension> getInstanceIdentifier() {
        return iid;
    }

}
