/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.impl.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.Meter;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.meters.MeterBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.MeterRef;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.AddMetersBatchOutputBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.BatchMeterOutputListGrouping;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.meters.service.rev160316.batch.meter.output.list.grouping.BatchFailedMetersOutputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcError;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;

/**
 * Test for {@link MeterUtil}.
 */
public class MeterUtilTest {

    public static final NodeId DUMMY_NODE_ID = new NodeId("dummyNodeId");
    private static final MeterId DUMMY_METER_ID = new MeterId(42L);
    private static final MeterId DUMMY_METER_ID_2 = new MeterId(43L);

    @Test
    public void testBuildGroupPath() throws Exception {
        final InstanceIdentifier<Node> nodePath = InstanceIdentifier
                .create(Nodes.class)
                .child(Node.class, new NodeKey(DUMMY_NODE_ID));

        final MeterRef meterRef = MeterUtil.buildMeterPath(nodePath, DUMMY_METER_ID);
        final InstanceIdentifier<?> meterRefValue = meterRef.getValue();
        Assert.assertEquals(DUMMY_NODE_ID, meterRefValue.firstKeyOf(Node.class).getId());
        Assert.assertEquals(DUMMY_METER_ID, meterRefValue.firstKeyOf(Meter.class).getMeterId());
    }

    @Test
    public void testCreateCumulatingFunction() throws Exception {
        final Function<List<RpcResult<String>>, RpcResult<List<BatchFailedMetersOutput>>> function =
                MeterUtil.createCumulativeFunction(Lists.newArrayList(
                        createBatchMeter(DUMMY_METER_ID),
                        createBatchMeter(DUMMY_METER_ID_2)));

        final RpcResult<List<BatchFailedMetersOutput>> output = function.apply(Lists.newArrayList(
                RpcResultBuilder.success("a").build(),
                RpcResultBuilder.<String>failed()
                        .withError(RpcError.ErrorType.APPLICATION, "ut-meter-error")
                        .build()));

        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals(1, output.getResult().size());
        Assert.assertEquals(DUMMY_METER_ID_2, output.getResult().get(0).getMeterId());
        Assert.assertEquals(1, output.getResult().get(0).getBatchOrder().intValue());
    }

    private org.opendaylight.yang.gen.v1.urn.opendaylight.meter.types.rev130918.Meter createBatchMeter(final MeterId meterId) {
        return new MeterBuilder()
                .setMeterId(meterId)
                .build();
    }

    @Test
    public void testMETER_ADD_TRANSFORM__failure() throws Exception {
        final RpcResult<List<BatchFailedMetersOutput>> input = createBatchOutcomeWithError();
        checkBatchErrorOutcomeTransformation(MeterUtil.METER_ADD_TRANSFORM.apply(input));
    }

    @Test
    public void testMETER_ADD_TRANSFORM__success() throws Exception {
        final RpcResult<List<BatchFailedMetersOutput>> input = createEmptyBatchOutcome();
        checkBatchSuccessOutcomeTransformation(MeterUtil.METER_ADD_TRANSFORM.apply(input));
    }

    @Test
    public void testMETER_REMOVE_TRANSFORM__failure() throws Exception {
        final RpcResult<List<BatchFailedMetersOutput>> input = createBatchOutcomeWithError();
        checkBatchErrorOutcomeTransformation(MeterUtil.METER_REMOVE_TRANSFORM.apply(input));
    }

    @Test
    public void testFLOW_REMOVE_TRANSFORM__success() throws Exception {
        final RpcResult<List<BatchFailedMetersOutput>> input = createEmptyBatchOutcome();
        checkBatchSuccessOutcomeTransformation(MeterUtil.METER_REMOVE_TRANSFORM.apply(input));
    }

    @Test
    public void testFLOW_UPDATE_TRANSFORM__failure() throws Exception {
        final RpcResult<List<BatchFailedMetersOutput>> input = createBatchOutcomeWithError();
        checkBatchErrorOutcomeTransformation(MeterUtil.METER_UPDATE_TRANSFORM.apply(input));
    }

    @Test
    public void testFLOW_UPDATE_TRANSFORM__success() throws Exception {
        final RpcResult<List<BatchFailedMetersOutput>> input = createEmptyBatchOutcome();
        checkBatchSuccessOutcomeTransformation(MeterUtil.METER_UPDATE_TRANSFORM.apply(input));
    }

    private <T extends BatchMeterOutputListGrouping> void checkBatchSuccessOutcomeTransformation(final RpcResult<T> output) {
        Assert.assertTrue(output.isSuccessful());
        Assert.assertEquals(0, output.getResult().getBatchFailedMetersOutput().size());
        Assert.assertEquals(0, output.getErrors().size());
    }

    private RpcResult<List<BatchFailedMetersOutput>> createEmptyBatchOutcome() {
        return RpcResultBuilder
                .<List<BatchFailedMetersOutput>>success(Collections.<BatchFailedMetersOutput>emptyList())
                .build();
    }

    private RpcResult<List<BatchFailedMetersOutput>> createBatchOutcomeWithError() {
        return RpcResultBuilder.<List<BatchFailedMetersOutput>>failed()
                .withError(RpcError.ErrorType.APPLICATION, "ut-flowAddFail")
                .withResult(Collections.singletonList(new BatchFailedMetersOutputBuilder()
                        .setMeterId(DUMMY_METER_ID)
                        .build()))
                .build();
    }

    private <T extends BatchMeterOutputListGrouping> void checkBatchErrorOutcomeTransformation(final RpcResult<T> output) {
        Assert.assertFalse(output.isSuccessful());
        Assert.assertEquals(1, output.getResult().getBatchFailedMetersOutput().size());
        Assert.assertEquals(DUMMY_METER_ID, output.getResult().getBatchFailedMetersOutput().get(0).getMeterId());

        Assert.assertEquals(1, output.getErrors().size());
    }

    @Test
    public void testCreateComposingFunction_success_success() throws Exception {
        final Function<Pair<RpcResult<AddMetersBatchOutput>, RpcResult<Void>>, RpcResult<AddMetersBatchOutput>> compositeFunction =
                MeterUtil.createComposingFunction();

        final RpcResult<AddMetersBatchOutput> addGroupBatchOutput = createAddMetersBatchSuccessOutput();
        final RpcResult<Void> barrierOutput = RpcResultBuilder.<Void>success().build();
        final Pair<RpcResult<AddMetersBatchOutput>, RpcResult<Void>> input = Pair.of(addGroupBatchOutput, barrierOutput);
        final RpcResult<AddMetersBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertTrue(composite.isSuccessful());
        Assert.assertEquals(0, composite.getErrors().size());
        Assert.assertEquals(0, composite.getResult().getBatchFailedMetersOutput().size());
    }

    @Test
    public void testCreateComposingFunction_failure_success() throws Exception {
        final Function<Pair<RpcResult<AddMetersBatchOutput>, RpcResult<Void>>, RpcResult<AddMetersBatchOutput>> compositeFunction =
                MeterUtil.createComposingFunction();

        final RpcResult<AddMetersBatchOutput> addGroupBatchOutput = createAddMetersBatchFailureOutcome();
        final RpcResult<Void> barrierOutput = RpcResultBuilder.<Void>success().build();
        final Pair<RpcResult<AddMetersBatchOutput>, RpcResult<Void>> input = Pair.of(addGroupBatchOutput, barrierOutput);
        final RpcResult<AddMetersBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertFalse(composite.isSuccessful());
        Assert.assertEquals(1, composite.getErrors().size());
        Assert.assertEquals(1, composite.getResult().getBatchFailedMetersOutput().size());
    }

    @Test
    public void testCreateComposingFunction_success_failure() throws Exception {
        final Function<Pair<RpcResult<AddMetersBatchOutput>, RpcResult<Void>>, RpcResult<AddMetersBatchOutput>> compositeFunction =
                MeterUtil.createComposingFunction();

        final RpcResult<AddMetersBatchOutput> addGroupBatchOutput = createAddMetersBatchSuccessOutput();
        final RpcResult<Void> barrierOutput = createBarrierFailureOutcome();
        final Pair<RpcResult<AddMetersBatchOutput>, RpcResult<Void>> input = Pair.of(addGroupBatchOutput, barrierOutput);
        final RpcResult<AddMetersBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertFalse(composite.isSuccessful());
        Assert.assertEquals(1, composite.getErrors().size());
        Assert.assertEquals(0, composite.getResult().getBatchFailedMetersOutput().size());
    }

    @Test
    public void testCreateComposingFunction_failure_failure() throws Exception {
        final Function<Pair<RpcResult<AddMetersBatchOutput>, RpcResult<Void>>, RpcResult<AddMetersBatchOutput>> compositeFunction =
                MeterUtil.createComposingFunction();

        final RpcResult<AddMetersBatchOutput> addGroupBatchOutput = createAddMetersBatchFailureOutcome();
        final RpcResult<Void> barrierOutput = createBarrierFailureOutcome();
        final Pair<RpcResult<AddMetersBatchOutput>, RpcResult<Void>> input = Pair.of(addGroupBatchOutput, barrierOutput);
        final RpcResult<AddMetersBatchOutput> composite = compositeFunction.apply(input);

        Assert.assertFalse(composite.isSuccessful());
        Assert.assertEquals(2, composite.getErrors().size());
        Assert.assertEquals(1, composite.getResult().getBatchFailedMetersOutput().size());
    }

    private RpcResult<Void> createBarrierFailureOutcome() {
        return RpcResultBuilder.<Void>failed()
                .withError(RpcError.ErrorType.APPLICATION, "ut-barrier-error")
                .build();
    }

    private RpcResult<AddMetersBatchOutput> createAddMetersBatchSuccessOutput() {
        return RpcResultBuilder
                .success(new AddMetersBatchOutputBuilder()
                        .setBatchFailedMetersOutput(Collections.<BatchFailedMetersOutput>emptyList())
                        .build())
                .build();
    }

    private RpcResult<AddMetersBatchOutput> createAddMetersBatchFailureOutcome() {
        final RpcResult<List<BatchFailedMetersOutput>> batchOutcomeWithError = createBatchOutcomeWithError();
        return RpcResultBuilder.<AddMetersBatchOutput>failed()
                .withResult(new AddMetersBatchOutputBuilder()
                        .setBatchFailedMetersOutput(batchOutcomeWithError.getResult())
                        .build())
                .withRpcErrors(batchOutcomeWithError.getErrors())
                .build();
    }
}