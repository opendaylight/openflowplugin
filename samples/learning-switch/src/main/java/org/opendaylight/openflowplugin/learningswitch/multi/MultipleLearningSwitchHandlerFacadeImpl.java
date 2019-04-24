/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.learningswitch.multi;

import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.openflowplugin.learningswitch.FlowCommitWrapper;
import org.opendaylight.openflowplugin.learningswitch.InstanceIdentifierUtils;
import org.opendaylight.openflowplugin.learningswitch.LearningSwitchHandler;
import org.opendaylight.openflowplugin.learningswitch.LearningSwitchHandlerSimpleImpl;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleLearningSwitchHandlerFacadeImpl implements LearningSwitchHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MultipleLearningSwitchHandlerFacadeImpl.class);
    private final FlowCommitWrapper dataStoreAccessor;
    private final PacketProcessingService packetProcessingService;
    private final PacketInDispatcherImpl packetInDispatcher;

    public MultipleLearningSwitchHandlerFacadeImpl(@NonNull FlowCommitWrapper dataStoreAccessor,
            @NonNull PacketProcessingService packetProcessingService,
            @NonNull PacketInDispatcherImpl packetInDispatcher) {
        this.dataStoreAccessor = Objects.requireNonNull(dataStoreAccessor);
        this.packetProcessingService = Objects.requireNonNull(packetProcessingService);
        this.packetInDispatcher = Objects.requireNonNull(packetInDispatcher);
    }


    @Override
    public synchronized void onSwitchAppeared(InstanceIdentifier<Table> appearedTablePath) {
        LOG.debug("expected table acquired, learning ..");

        /**
         * appearedTablePath is in form of /nodes/node/node-id/table/table-id
         * so we shorten it to /nodes/node/node-id to get identifier of switch.
         */
        InstanceIdentifier<Node> nodePath = InstanceIdentifierUtils.getNodePath(appearedTablePath);

        /**
         * We check if we already initialized dispatcher for that node,
         * if not we create new handler for switch.
         */
        if (!packetInDispatcher.getHandlerMapping().containsKey(nodePath)) {
            // delegate this node (owning appearedTable) to SimpleLearningSwitchHandler
            LearningSwitchHandlerSimpleImpl simpleLearningSwitch = new LearningSwitchHandlerSimpleImpl(
                    dataStoreAccessor, packetProcessingService, null);

            /**
             * We propagate table event to newly instantiated instance of learning switch
             */
            simpleLearningSwitch.onSwitchAppeared(appearedTablePath);
            /**
             * We update mapping of already instantiated LearningSwitchHanlders
             */
            packetInDispatcher.getHandlerMapping().put(nodePath, simpleLearningSwitch);
        }
    }
}
