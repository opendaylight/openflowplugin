/**
 * Copyright (c) 2016 Cisco Systems, Inc. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.frsync.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCase;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupAction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.BandId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.band.type.band.type.DropBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.MeterBandHeadersBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.meter.meter.band.headers.MeterBandHeaderBuilder;

/**
 * Provides create methods for dataObjects involved in
 * {@link org.opendaylight.controller.md.sal.binding.api.DataTreeChangeListener} by inventory.
 */
public class DSInputFactory {
    public static Group createGroup(final long groupIdValue) {
        final Buckets buckets = new BucketsBuilder()
                .setBucket(Collections.<Bucket>emptyList())
                .build();
        return new GroupBuilder()
                .setGroupId(new GroupId(groupIdValue))
                .setBuckets(buckets)
                .build();
    }

    public static Group createGroupWithAction(final long groupIdValue) {
        final Buckets buckets = new BucketsBuilder()
                .setBucket(Collections.singletonList(new BucketBuilder()
                        .setAction(Collections.singletonList(new ActionBuilder()
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
                .setPriority(priority)
                .setTableId((short) 42)
                .setMatch(new MatchBuilder().build())
                .build();
    }

    public static Flow createFlowWithInstruction(final String flowIdValue, final int priority) {
        return new FlowBuilder()
                .setId(new FlowId(flowIdValue))
                .setPriority(priority)
                .setTableId((short) 42)
                .setMatch(new MatchBuilder().build())
                .setInstructions(new InstructionsBuilder()
                        .setInstruction(Collections.singletonList(new InstructionBuilder()
                                .setInstruction(new ApplyActionsCaseBuilder()
                                        .setApplyActions(new ApplyActionsBuilder()
                                                .setAction(Collections.singletonList(new ActionBuilder()
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

    public static Meter createMeter(final Long meterIdValue) {
        return new MeterBuilder()
                .setMeterId(new MeterId(meterIdValue))
                .build();
    }

    public static Meter createMeterWithBody(final Long meterIdValue) {
        return new MeterBuilder()
                .setMeterId(new MeterId(meterIdValue))
                .setMeterBandHeaders(new MeterBandHeadersBuilder()
                        .setMeterBandHeader(Collections.singletonList(new MeterBandHeaderBuilder()
                                .setBandId(new BandId(42L))
                                .setBandType(new DropBuilder()
                                        .setDropRate(43L)
                                        .build())
                                .build()))
                        .build())
                .build();
    }

    public static Group createGroupWithPreconditions(final long groupIdValue, final long... requiredId) {
        final List<Action> actionBag = new ArrayList<>();
        for (long groupIdPrecondition : requiredId) {
            final GroupAction groupAction = new GroupActionBuilder()
                    .setGroupId(groupIdPrecondition)
                    .build();
            final GroupActionCase groupActionCase = new GroupActionCaseBuilder()
                    .setGroupAction(groupAction)
                    .build();
            final Action action = new ActionBuilder()
                    .setAction(groupActionCase)
                    .build();
            actionBag.add(action);
        }

        final Bucket bucket = new BucketBuilder()
                .setAction(actionBag)
                .build();
        final Buckets buckets = new BucketsBuilder()
                .setBucket(Collections.singletonList(bucket))
                .build();

        return new GroupBuilder()
                .setGroupId(new GroupId(groupIdValue))
                .setBuckets(buckets)
                .build();
    }
}
