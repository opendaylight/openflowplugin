/*
 * Copyright (c) 2016, 2017 Ericsson Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.bulk.o.matic;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Ipv4Prefix;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

public final class BulkOMaticUtils {

    public static final int DEFUALT_STATUS = FlowCounter.OperationStatus.INIT.status();
    public static final int DEFAULT_FLOW_COUNT = 0;
    public static final int DEFAULT_TABLE_COUNT = 0;
    public static final long DEFAULT_COMPLETION_TIME = 0;
    public static final String DEFAULT_UNITS = "ns";
    public static final String DEVICE_TYPE_PREFIX = "openflow:";

    private BulkOMaticUtils() {
    }

    public static String ipIntToStr(int number) {
        return new StringBuilder().append(number >> 24 & 0xFF).append(".")
                .append(number >> 16 & 0xFF).append(".")
                .append(number >> 8 & 0xFF).append(".")
                .append(number & 0xFF).append("/32").toString();
    }

    public static Match getMatch(final int sourceIp) {
        return new MatchBuilder()
                .setLayer3Match(new Ipv4MatchBuilder().setIpv4Source(new Ipv4Prefix(ipIntToStr(sourceIp))).build())
                .setEthernetMatch(new EthernetMatchBuilder()
                    .setEthernetType(new EthernetTypeBuilder().setType(new EtherType(Uint32.valueOf(2048))).build())
                    .build())
                .build();
    }

    public static Flow buildFlow(short tableId, String flowId, Match match) {
        return new FlowBuilder()
                .withKey(new FlowKey(new FlowId(flowId)))
                .setTableId(Uint8.valueOf(tableId))
                .setMatch(match)
                .build();
    }

    public static InstanceIdentifier<Flow> getFlowInstanceIdentifier(short tableId, String flowId, String dpId) {
        return InstanceIdentifier.create(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(dpId)))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(Uint8.valueOf(tableId)))
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow.class,
                        new FlowKey(new FlowId(flowId)));
    }

    public static InstanceIdentifier<Node> getFlowCapableNodeId(String dpId) {
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId(dpId))).build();
    }

    public static InstanceIdentifier<Table> getTableId(short tableId, String dpId) {
        return InstanceIdentifier.builder(Nodes.class).child(Node.class, new NodeKey(new NodeId(dpId)))
                .augmentation(FlowCapableNode.class).child(Table.class, new TableKey(Uint8.valueOf(tableId))).build();
    }

    public static InstanceIdentifier<Flow> getFlowId(final InstanceIdentifier<Table> tablePath, final String flowId) {
        return tablePath.child(Flow.class, new FlowKey(new FlowId(flowId)));
    }
}
