/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.api.openflow.md;

import com.google.common.base.Optional;
import java.math.BigInteger;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.openflowplugin.api.openflow.md.core.session.SessionContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.statistics.rev130819.OpendaylightFlowStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.table.statistics.rev131215.OpendaylightFlowTableStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.service.rev130918.SalGroupService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.statistics.rev131111.OpendaylightGroupStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.service.rev130918.SalMeterService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.statistics.rev131111.OpendaylightMeterStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.module.config.rev141015.NodeConfigService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.service.rev131107.SalPortService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.OpendaylightPortStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.queue.statistics.rev131216.OpendaylightQueueStatisticsService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.table.service.rev131026.SalTableService;
import org.opendaylight.yangtools.concepts.Identifiable;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

/**
 * interface concatenating all md-sal services provided by OF-switch.
 */
public interface ModelDrivenSwitch
        extends
        SalGroupService,
        SalFlowService,
        SalMeterService, SalTableService, SalPortService, PacketProcessingService, NodeConfigService,
        OpendaylightGroupStatisticsService, OpendaylightMeterStatisticsService, OpendaylightFlowStatisticsService,
        OpendaylightPortStatisticsService, OpendaylightFlowTableStatisticsService, OpendaylightQueueStatisticsService,
        Identifiable<InstanceIdentifier<Node>> {

    /**
     * Register.
     * @param rpcProviderRegistry rpc provider
     * @return wrapped list of {service provider + path} registration couples
     */
    ModelDrivenSwitchRegistration register(RpcProviderRegistry rpcProviderRegistry);

    /**
     * Getter.
     * @return id of encapsulated node (served by this impl)
     */
    NodeId getNodeId();

    /**
     * returnes the session context associated with this model-driven switch.
     *
     * @return session context object
     */
    SessionContext getSessionContext();

    /**
     * Returns whether this *instance* is entity owner or not.
     * @return true if it's entity owner, else false.
     */
    boolean isEntityOwner();

    /**
     * Set entity ownership satus of this switch in *this* instance.
     * @param isOwner is owner
     */
    void setEntityOwnership(boolean isOwner);

    /**
     * Send table feature to the switch to get tables features for all the tables.
     * @return Transaction id
     */
    Optional<BigInteger> sendEmptyTableFeatureRequest();

    /**
     * Method send port/desc multipart request to the switch to fetch the initial details.
     */
    void requestSwitchDetails();

}
