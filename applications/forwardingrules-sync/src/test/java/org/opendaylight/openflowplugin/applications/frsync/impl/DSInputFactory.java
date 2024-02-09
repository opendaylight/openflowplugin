/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.applications.frsync.impl;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;
import org.opendaylight.yangtools.yang.binding.util.BindingMap;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint8;

/**
 * Provides create methods for dataObjects involved in
 * {@link org.opendaylight.mdsal.binding.api.DataTreeChangeListener} by inventory.
 */
public final class DSInputFactory {
    private DSInputFactory() {
    }

    public static Group createGroup(final Uint32 groupIdValue) {
        return new GroupBuilder()
                .setGroupId(new GroupId(groupIdValue))
                .setBuckets(new BucketsBuilder().build())
                .build();
    }

    public static Group createGroupWithAction(final Uint32 groupIdValue) {
        final Buckets buckets = new BucketsBuilder()
                .setBucket(BindingMap.of(new BucketBuilder()
                        .setBucketId(new BucketId(Uint32.ZERO))
                        .setAction(List.of(new ActionBuilder()
                                .setAction(new OutputActionCaseBuilder()
                                        .setOutputAction(new OutputActionBuilder()
                                                .setOutputNodeConnector(new Uri("ut-port-1"))
                                                .build())
                                        .build())
                                .build()))
                        .build()))
                .build();
        return new GroupBuilder()
                .setGroupId(new GroupId(groupIdValue))
                .setBuckets(buckets)
                .build();
    }

    public static Flow createFlow(final String flowIdValue, final int priority) {
        return new FlowBuilder()
                .setId(new FlowId(flowIdValue))
                .setPriority(Uint16.valueOf(priority))
                .setTableId(Uint8.valueOf(42))
                .setMatch(new MatchBuilder().build())
                .build();
    }

    public static Flow createFlowWithInstruction(final String flowIdValue, final int priority) {
        return new FlowBuilder()
                .setId(new FlowId(flowIdValue))
                .setPriority(Uint16.valueOf(priority))
                .setTableId(Uint8.valueOf(42))
                .setMatch(new MatchBuilder().build())
                .setInstructions(new InstructionsBuilder()
                        .setInstruction(List.of(new InstructionBuilder()
                                .setInstruction(new ApplyActionsCaseBuilder()
                                        .setApplyActions(new ApplyActionsBuilder()
                                                .setAction(List.of(new ActionBuilder()
                                                        .setAction(new OutputActionCaseBuilder()
                                                                .setOutputAction(new OutputActionBuilder()
                                                                        .setOutputNodeConnector(new Uri("ut-port-1"))
                                                                        .build())
                                                                .build())
                                                        .build()))
                                                .build())
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    public static Meter createMeter(final Uint32 meterIdValue) {
        return new MeterBuilder()
                .setMeterId(new MeterId(meterIdValue))
                .build();
    }

    public static Meter createMeterWithBody(final Uint32 meterIdValue) {
        return new MeterBuilder()
                .setMeterId(new MeterId(meterIdValue))
                .setMeterBandHeaders(new MeterBandHeadersBuilder()
                        .setMeterBandHeader(BindingMap.of(new MeterBandHeaderBuilder()
                                .setBandId(new BandId(Uint32.valueOf(42)))
                                .setBandType(new DropBuilder()
                                        .setDropRate(Uint32.valueOf(43))
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    public static Group createGroupWithPreconditions(final long groupIdValue, final long... requiredId) {
        return new GroupBuilder()
                .setGroupId(new GroupId(Uint32.valueOf(groupIdValue)))
                .setBuckets(new BucketsBuilder()
                    .setBucket(BindingMap.of(new BucketBuilder()
                        .setBucketId(new BucketId(Uint32.ZERO))
                        .setAction(Arrays.stream(requiredId)
                            .mapToObj(groupIdPrecondition -> new ActionBuilder()
                                .setAction(new GroupActionCaseBuilder()
                                    .setGroupAction(new GroupActionBuilder()
                                        .setGroupId(Uint32.valueOf(groupIdPrecondition))
                                        .build())
                                    .build())
                                .build())
                            .collect(ImmutableList.toImmutableList()))
                        .build()))
                    .build())
                .build();
    }
}
