/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.droptest;

import java.math.BigInteger;

import org.opendaylight.controller.sal.binding.api.data.DataModificationTransaction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public class DropTestCommiter extends AbstractDropTest {
    private final static Logger LOG = LoggerFactory.getLogger(DropTestProvider.class);

    private final DropTestProvider manager;

    public DropTestCommiter(final DropTestProvider manager) {
        this.manager = Preconditions.checkNotNull(manager);
    }

    @Override
    protected void processPacket(final NodeKey node, final Match match, final Instructions instructions) {

        // Finally build our flow
        final FlowBuilder fb = new FlowBuilder();
        fb.setMatch(match);
        fb.setInstructions(instructions);
        fb.setId(new FlowId(Long.toString(fb.hashCode())));
        fb.setPriority(4);
        fb.setBufferId(0L);
        final BigInteger value = new BigInteger("10", 10);
        fb.setCookie(new FlowCookie(value));
        fb.setCookieMask(new FlowCookie(value));

        fb.setTableId(Short.valueOf(((short) 0)));
        fb.setHardTimeout(300);
        fb.setIdleTimeout(240);
        fb.setFlags(new FlowModFlags(false, false, false, false, false));

        // Construct the flow instance id
        final InstanceIdentifier<Flow> flowInstanceId =
                InstanceIdentifier.builder(Nodes.class) // File under nodes
                        .child(Node.class, node) // A particular node indentified by nodeKey
                        .augmentation(FlowCapableNode.class) // That is flow capable, only FlowCapableNodes have tables
                        .child(Table.class, new TableKey((short) 0)) // In the table identified by TableKey
                        .child(Flow.class, new FlowKey(fb.getId())) // A flow identified by flowKey
                        .build();

        final Flow flow = fb.build();
        final DataModificationTransaction transaction = manager.getDataService().beginTransaction();

        // LOG.debug("onPacketReceived - About to write flow - " + flow);
        transaction.putConfigurationData(flowInstanceId, flow);
        transaction.commit();
        // LOG.debug("onPacketReceived - About to write flow commited");
    }
}
