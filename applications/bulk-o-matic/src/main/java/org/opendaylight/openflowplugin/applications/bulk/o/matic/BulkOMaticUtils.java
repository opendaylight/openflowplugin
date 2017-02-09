/*
 * Copyright (c) 2016 Ericsson Systems, Inc. and others.  All rights reserved.
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._3.match.Ipv4MatchBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

public final class BulkOMaticUtils {

    public static final int DEFUALT_STATUS = FlowCounter.OperationStatus.INIT.status();
    public static final int DEFAULT_FLOW_COUNT = 0;
    public static final int DEFAULT_TABLE_COUNT = 0;
    public static final long DEFAULT_COMPLETION_TIME = 0;
    public static final String DEFAULT_UNITS = "ns";
    public static final String DEVICE_TYPE_PREFIX = "openflow:";

    private BulkOMaticUtils() {
    }

    public static String ipIntToStr (int k) {
        return new StringBuilder().append(k >> 24 & 0xFF).append(".")
                .append(k >> 16 & 0xFF).append(".")
                .append(k >> 8 & 0xFF).append(".")
                .append(k & 0xFF).append("/32").toString();
    }

    public static Match getMatch(final Integer sourceIp){
        Ipv4Match ipv4Match = new Ipv4MatchBuilder().setIpv4Source(
                new Ipv4Prefix(ipIntToStr(sourceIp))).build();
        MatchBuilder matchBuilder = new MatchBuilder();
        matchBuilder.setLayer3Match(ipv4Match);
        EthernetTypeBuilder ethTypeBuilder = new EthernetTypeBuilder();
        EthernetMatchBuilder ethMatchBuilder = new EthernetMatchBuilder();
        ethTypeBuilder.setType(new EtherType(2048L));
        ethMatchBuilder.setEthernetType(ethTypeBuilder.build());
        matchBuilder.setEthernetMatch(ethMatchBuilder.build());
        return matchBuilder.build();
    }

    public static Flow buildFlow(Short tableId, String flowId, Match match){
        FlowBuilder flowBuilder = new FlowBuilder();
        flowBuilder.setKey(new FlowKey(new FlowId(flowId)));
        flowBuilder.setTableId(tableId);
        flowBuilder.setMatch(match);
        return flowBuilder.build();
    }

    public static InstanceIdentifier<Flow> getFlowInstanceIdentifier(Short tableId, String flowId, String dpId) {
        return InstanceIdentifier.create(Nodes.class).child(Node.class,
                new NodeKey(new NodeId(dpId)))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableId))
                .child(org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow.class,
                        new FlowKey(new FlowId(flowId)));
    }

    public static InstanceIdentifier<Node> getFlowCapableNodeId(String dpId){
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(dpId)))
                .build();
    }

    public static InstanceIdentifier<Table> getTableId(Short tableId, String dpId) {
        return InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(dpId)))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(tableId))
                .build();
    }

    public static InstanceIdentifier<Flow> getFlowId(final InstanceIdentifier<Table> tablePath, final String flowId) {
        return tablePath.child(Flow.class, new FlowKey(new FlowId(flowId)));
    }
}
